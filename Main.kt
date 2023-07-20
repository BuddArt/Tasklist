package tasklist

import kotlin.system.exitProcess
import kotlinx.datetime.*
import com.squareup.moshi.*
import java.io.File
import java.lang.reflect.ParameterizedType
import javax.print.attribute.standard.JobPriority

val listOfList = mutableListOf(mutableListOf<String>())
var dueTag = ""
var priority = ""
var dateAndTime = LocalDateTime(0, 1, 1, 1, 1)
var localDate: LocalDate = LocalDate(0,1,1)


fun main() {
    val file = File("tasklist.json")
    if (file.exists()) readJson()
    while(true) {
        println("Input an action (add, print, edit, delete, end):")
        when (readln().lowercase()) {
            "add" -> add()
            "print" -> print()
            "delete" -> delete()
            "edit" -> edit()
            "end" -> {
                println("Tasklist exiting!")
                saveJson()
                exitProcess(1)
            }
            else -> println("The input action is invalid")
        }
    }

}

fun add() {
    askPriority()
    setupDate()
    setupTime()
    println("Input a new task (enter a blank line to end):")
    val listOfTask = mutableListOf<String>()
    listOfTask.add(localDate.toString())
    listOfTask.add(dateAndTime.toString().substringAfter("T"))
    listOfTask.add(priority)
    listOfTask.add(dueTag)
    while(true) {
        val input = readln()
        if (input.isBlank() && listOfTask.size == 4) {
            println("The task is blank")
            break
        }
        if (input.isBlank()) break
        if (input.length > 44) {
            val inpCh = input.chunked(44)
            for (i in inpCh.indices) {
                listOfTask.add(inpCh[i])
            }
        } else listOfTask.add(input.trim())
    }
    if (listOfTask.isNotEmpty()) {
        listOfList.add(listOfTask)
    }
}

fun print() {
    if (listOfList.size == 1) {
        println("No tasks have been input")
    } else {
        println("""
            +----+------------+-------+---+---+--------------------------------------------+
            | N  |    Date    | Time  | P | D |                   Task                     |
            +----+------------+-------+---+---+--------------------------------------------+
        """.trimIndent())
        for (i in 1..listOfList.lastIndex) {
            println("| ${i.toString().padEnd(2)} | ${listOfList[i][0]} | ${listOfList[i][1]} | ${listOfList[i][2]} | ${listOfList[i][3]} |${listOfList[i][4].padEnd(44)}|")
            for (j in 5..listOfList[i].lastIndex) {
                println("|    |            |       |   |   |${listOfList[i][j].padEnd(44)}|")
            }
            println("+----+------------+-------+---+---+--------------------------------------------+")
        }
    }
}

fun askPriority() {
    while (true) {
        println("Input the task priority (C, H, N, L):")
        priority = readln().uppercase()
        when (priority) {
            "C" -> { priority = "\u001B[101m \u001B[0m"; break }
            "H" -> { priority = "\u001B[103m \u001B[0m"; break }
            "N" -> { priority = "\u001B[102m \u001B[0m"; break }
            "L" -> { priority = "\u001B[104m \u001B[0m"; break }
        }
    }
}

fun setupDate() {
    while (true) {
        try {
            println("Input the date (yyyy-mm-dd):")
            val (year, month, dayOfMonth) = readln().split("-")
            localDate = LocalDate(year.toInt(), month.toInt(), dayOfMonth.toInt())
            break
        } catch (e: IllegalArgumentException) {
            println("The input date is invalid")
        } catch (e: IndexOutOfBoundsException) {
            println("The input date is invalid")
        }
    }
    val currentDate = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0")).date
    val numberOfDays = currentDate.daysUntil(localDate)
    when {
        numberOfDays > 0 -> dueTag = "\u001B[102m \u001B[0m"
        numberOfDays < 0 -> dueTag = "\u001B[101m \u001B[0m"
        numberOfDays == 0 -> dueTag = "\u001B[103m \u001B[0m"
    }
}

fun setupTime() {
    var date = localDate
    while (true) {
        try {
        println("Input the time (hh:mm):")
        val (hour, minute) = readln().split(":")
            dateAndTime = LocalDateTime(date.year, date.month, date.dayOfMonth, hour.toInt(), minute.toInt())
            break
        } catch (e: IllegalArgumentException) {
            println("The input time is invalid")
        } catch (e: IndexOutOfBoundsException) {
            println("The input time is invalid")
        }
    }
}

fun delete() {
    print()
    if (listOfList.size != 1) {
        while (true) {
            try {
                println("Input the task number (1-${listOfList.lastIndex}):")
                val taskIndex = readln().toInt()
                if (taskIndex !in 1..listOfList.lastIndex) {
                    println("Invalid task number")
                } else {
                    listOfList.removeAt(taskIndex)
                    println("The task is deleted")
                    break
                }
            } catch (e: NumberFormatException) {
                println("Invalid task number")
            }
        }
    }
}

fun edit() {
    print()
    if (listOfList.size != 1) {
        loop@ while (true) {
            println("Input the task number (1-${listOfList.lastIndex}):")
            val taskIndex = readln()
            try {
                if (taskIndex.toInt() !in 1..listOfList.lastIndex) {
                    println("Invalid task number")
                } else {
                    while (true) {
                        println("Input a field to edit (priority, date, time, task):")
                        val editField = readln()
                        when (editField) {
                            "priority" -> {
                                askPriority()
                                listOfList[taskIndex.toInt()][2] = priority
                                println("The task is changed")
                                break@loop
                            }
                            "date" -> {
                                setupDate()
                                listOfList[taskIndex.toInt()][0] = localDate.toString()
                                listOfList[taskIndex.toInt()][3] = dueTag
                                println("The task is changed")
                                break@loop
                            }
                            "time" -> {
                                setupTime()
                                listOfList[taskIndex.toInt()][1] = dateAndTime.toString().substringAfter("T")
                                println("The task is changed")
                                break@loop
                            }
                            "task" -> {
                                println("Input a new task (enter a blank line to end):")
                                val listOfTask = mutableListOf<String>()
                                listOfTask.add(localDate.toString())
                                listOfTask.add(dateAndTime.toString().substringAfter("T"))
                                listOfTask.add(priority)
                                listOfTask.add(dueTag)

                                while(true) {
                                    val input = readln()
                                    if (input.isBlank() && listOfTask.size == 4) {
                                        println("The task is blank")
                                        break
                                    }
                                    if (input.isBlank()) break
                                    if (input.length > 44) {
                                        val inpCh = input.chunked(44)
                                        for (i in inpCh.indices) {
                                            listOfTask.add(inpCh[i])
                                        }
                                    } else listOfTask.add(input.trim())
                                }
                                if (listOfTask.isNotEmpty()) {
                                    listOfList[taskIndex.toInt()] = listOfTask
                                }
                                println("The task is changed")
                                break@loop
                            }
                            else -> println("Invalid field")
                        }
                    }
                }
            } catch (e: NumberFormatException) {
                println("Invalid task number")
            }
        }
    }
}

class Task(var date: String, var time: String, var priority: String, var dueTag: String, var tasks: MutableList<String>)

fun saveJson() {
    val listForJson: MutableList<Task> = mutableListOf()
    for (i in 1..listOfList.lastIndex) {
        var listT: MutableList<String> = mutableListOf()
        for (j in 4..listOfList[i].lastIndex) {
            listT.add(listOfList[i][j])
        }
        val task = Task(listOfList[i][0], listOfList[i][1], listOfList[i][2], listOfList[i][3], listT)
        listForJson.add(task)
    }

    val jsonFile = File("tasklist.json")
    val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    val type: ParameterizedType = Types.newParameterizedType(List::class.java, Task::class.java)
    val taskListAdapter: JsonAdapter<List<Task?>> = moshi.adapter(type)
    jsonFile.writeText(taskListAdapter.toJson(listForJson))
}

fun readJson() {
    val jsonFile = File("tasklist.json").readText()
    val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    val type: ParameterizedType = Types.newParameterizedType(List::class.java, Task::class.java)
    val taskListAdapter: JsonAdapter<List<Task?>> = moshi.adapter(type)
    val listFromJson = taskListAdapter.fromJson(jsonFile)

    if (listFromJson != null) {
        for (i in listFromJson.indices) {
            listOfList.add(mutableListOf())
            listOfList[i + 1].add(listFromJson[i]?.date.toString())
            listOfList[i + 1].add(listFromJson[i]?.time.toString())
            listOfList[i + 1].add(listFromJson[i]?.priority.toString())
            listOfList[i + 1].add(listFromJson[i]?.dueTag.toString())
            for (j in listFromJson[i]?.tasks?.indices!!) {
                listOfList[i + 1].add(listFromJson[i]?.tasks?.get(j).toString())
            }
        }
    }
}
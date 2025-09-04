package com.rollinup.server.util

import com.rollinup.server.model.Task

object Utils {
    fun Task.taskAsRow(): String {
        return """
            <tr>
                <td>$name</td>
                <td>$descriptions</td>
                <td>$priority</td>
            </tr>
        """.trimIndent()
    }

    fun List<Task>.taskAsTable(): String {
        return this.joinToString(
            prefix = "<table rules=\"all\">",
            postfix = "</table>",
            separator = "\n",
            transform = { task -> task.taskAsRow() }
        )
    }
}
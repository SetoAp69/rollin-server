package com.rollinup.server.model

object TaskRepository {
    private val tasks = mutableListOf(
        Task("cleaning", "Clean the house", Priority.LOW),
        Task("gardening", "Mow the lawn", Priority.MEDIUM),
        Task("shopping", "Buy the groceries", Priority.HIGH),
        Task("painting", "Paint the fence", Priority.MEDIUM)
    )

    fun allTasks() = tasks

    fun taskByPriority(priority: Priority) = tasks.filter { it.priority == priority }

    fun taskByName(name: String) = tasks.find { it.name == name }

    fun addTask(task: Task) {
        if (taskByName(task.name) != null) {
            throw IllegalStateException("Task name already exist")
        } else {
            tasks.add(task)
        }
    }

    fun removeTask(name: String) {
        tasks.removeIf { it.name == name }
    }
}

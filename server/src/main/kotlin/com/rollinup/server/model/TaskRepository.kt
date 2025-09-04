package com.rollinup.server.model

object TaskRepository {
    private val tasks = mutableListOf(
        Task(
            name = "Cleaning",
            descriptions = "Cleanin' out my closet",
            priority = Priority.VITAL
        ),
        Task(
            name = "Ballin",
            descriptions = "Puttin' my balls in a G",
            priority = Priority.LOW
        ),
        Task(
            name = "Wood Working",
            descriptions = "Working on my WOOD",
            priority = Priority.HIGH
        )
    )

    fun allTasks() = tasks

    fun taskByPriority(priority: Priority) = tasks.filter { it.priority == priority }

    fun taskByName(name: String) = tasks.find{ it.name == name }

    fun addTask(task: Task) {
        if(taskByName(task.name)!=null){
            throw IllegalStateException("Task name already exist")
        }else{
            tasks.add(task)
        }
    }
}

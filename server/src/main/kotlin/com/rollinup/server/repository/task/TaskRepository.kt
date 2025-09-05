package com.rollinup.server.repository.task

import com.rollinup.server.model.Priority
import com.rollinup.server.model.Task

interface TaskRepository {
    suspend fun getTask():List<Task>
    suspend fun getTaskByPriority(priority: Priority):List<Task>
    suspend fun getTaskByName(name:String):List<Task>
    suspend fun addTask(task:Task)
    suspend fun removeTask(name:String)
    suspend fun search(searchQuery:String):List<Task>
}
package com.rollinup.server.datasource.database.repository.task

import com.rollinup.server.model.Priority
import com.rollinup.server.model.Task
import com.rollinup.server.datasource.database.model.task.TaskDAO
import com.rollinup.server.datasource.database.model.task.TaskTable
import com.rollinup.server.datasource.database.model.task.daoToModel
import com.rollinup.server.util.Utils

class TaskRepositoryImpl : TaskRepository {

    override suspend fun getTask(): List<Task> {
        return Utils.suspendTransaction {
            TaskDAO.all().map(::daoToModel)
        }
    }

    override suspend fun getTaskByPriority(priority: Priority): List<Task> {
        return Utils.suspendTransaction {
            TaskDAO
                .find { (TaskTable.priority eq priority.value) }
                .map(::daoToModel)
        }
    }

    override suspend fun getTaskByName(name: String): List<Task> {
        return Utils.suspendTransaction {
            TaskDAO
                .find { (TaskTable.name eq name) }
                .map(::daoToModel)
        }
    }

    override suspend fun addTask(task: Task) {
        return Utils.suspendTransaction {
            TaskDAO.new {
                name = task.name
                description = task.descriptions
                priority = task.priority.name
            }
        }
    }

    override suspend fun removeTask(name: String) {
        return Utils.suspendTransaction {
            TaskDAO.find { TaskTable.name eq name }.firstOrNull()?.delete()
        }
    }

    override suspend fun search(searchQuery: String): List<Task> {
        return Utils.suspendTransaction {
            TaskDAO
                .find {
                    (TaskTable.name regexp searchQuery)
                }
                .map(::daoToModel)
        }
    }

}
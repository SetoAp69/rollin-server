package com.rollinup.server.repository.task

import com.rollinup.server.model.Priority
import com.rollinup.server.model.Task
import com.rollinup.server.model.task.TaskDAO
import com.rollinup.server.model.task.TaskTable
import com.rollinup.server.model.task.daoToModel
import com.rollinup.server.model.task.suspendTransaction
import io.ktor.server.application.Application
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.ktor.plugin.koinModule

class TaskRepositoryImpl : TaskRepository {

    override suspend fun getTask(): List<Task> {
        return suspendTransaction {
            TaskDAO.all().map(::daoToModel)
        }
    }

    override suspend fun getTaskByPriority(priority: Priority): List<Task> {
        return suspendTransaction {
            TaskDAO
                .find { (TaskTable.priority eq priority.name) }
                .map(::daoToModel)
        }
    }

    override suspend fun getTaskByName(name: String): List<Task> {
        return suspendTransaction {
            TaskDAO
                .find { (TaskTable.name eq name) }
                .map(::daoToModel)
        }
    }

    override suspend fun addTask(task: Task) {
        return suspendTransaction {
            TaskDAO.new {
                name = task.name
                description = task.descriptions
                priority = task.priority.name
            }
        }
    }

    override suspend fun removeTask(name: String) {
        return suspendTransaction {
            TaskDAO.find { TaskTable.name eq name }.firstOrNull()?.delete()
        }
    }

    override suspend fun search(searchQuery: String): List<Task> {
        return suspendTransaction {
            TaskDAO
                .find {
                    (TaskTable.name regexp searchQuery)
                }
                .map(::daoToModel)
        }
    }

}
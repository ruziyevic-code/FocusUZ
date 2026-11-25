package uz.focusuz.app.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FocusViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val dao = FocusDatabase.get(application).dao()

    val tasks = dao.tasks().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val notes = dao.notes().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val sessions = dao.sessions().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private fun execute(action: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                action()
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = "Saqlashda xatolik yuz berdi"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun addTask(title: String) {
        if (title.isBlank()) return
        execute {
            dao.addTask(FocusTask(title = title.trim()))
        }
    }

    fun completeTask(task: FocusTask) = execute {
        dao.updateTask(
            task.copy(
                completed = true,
                completedAt = System.currentTimeMillis()
            )
        )
    }

    fun restoreTask(task: FocusTask) = execute {
        dao.updateTask(
            task.copy(
                completed = false,
                completedAt = null,
                archived = false
            )
        )
    }

    fun editTask(task: FocusTask, title: String) {
        if (title.isBlank()) return
        execute {
            dao.updateTask(task.copy(title = title.trim()))
        }
    }

    fun archiveTask(task: FocusTask) = execute {
        dao.updateTask(task.copy(archived = true))
    }

    fun deleteTask(task: FocusTask) = execute {
        dao.deleteTask(task)
    }

    fun saveNote(note: FocusNote) = execute {
        val updated = note.copy(
            updatedAt = System.currentTimeMillis()
        )
        if (updated.id == 0L) {
            dao.addNote(updated)
        } else {
            dao.updateNote(updated)
        }
    }

    fun deleteNote(note: FocusNote) = execute {
        dao.deleteNote(note)
    }

    fun saveSession(seconds: Long, type: String) {
        if (seconds <= 0) return
        execute {
            dao.addSession(
                FocusSession(
                    durationSeconds = seconds,
                    type = type
                )
            )
        }
    }
}

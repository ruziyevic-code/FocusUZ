package uz.focusuz.app.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uz.focusuz.app.data.FocusTask
import uz.focusuz.app.data.FocusViewModel

@Composable
fun FocusTasks(vm: FocusViewModel) {
    val tasks by vm.tasks.collectAsState()
    val scope = rememberCoroutineScope()

    var input by rememberSaveable { mutableStateOf("") }
    var history by rememberSaveable { mutableStateOf(false) }
    var editing by remember { mutableStateOf<FocusTask?>(null) }
    var deleting by remember { mutableStateOf<FocusTask?>(null) }
    var editText by remember { mutableStateOf("") }

    val pending = remember { mutableStateListOf<Long>() }
    val hiding = remember { mutableStateListOf<Long>() }

    val active = tasks.filter { !it.completed && !it.archived }
    val completed = tasks.filter { it.completed && !it.archived }
    val total = active.size + completed.size

    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FocusTitle("Vazifalar")

        FocusCard {
            FocusCaption("BAJARILGAN VAZIFALAR")
            FocusTitle("${completed.size} / $total")
            LinearProgressIndicator(
                progress = {
                    if (total == 0) 0f
                    else completed.size.toFloat() / total
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("Yangi vazifa") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            FocusButton("+", {
                if (input.isNotBlank()) {
                    vm.addTask(input)
                    input = ""
                }
            })
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(active, key = { it.id }) { task ->
                AnimatedVisibility(
                    visible = task.id !in hiding,
                    exit = fadeOut(tween(450)) +
                        shrinkVertically(tween(450))
                ) {
                    FocusCard {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = task.id in pending,
                                enabled = task.id !in pending,
                                onCheckedChange = {
                                    if (task.id !in pending) {
                                        pending.add(task.id)
                                        scope.launch {
                                            delay(250)
                                            hiding.add(task.id)
                                            delay(450)
                                            vm.completeTask(task)
                                            delay(150)
                                            pending.remove(task.id)
                                            hiding.remove(task.id)
                                        }
                                    }
                                }
                            )

                            Text(
                                task.title,
                                modifier = Modifier.weight(1f),
                                fontSize = 14.sp
                            )

                            TextButton(
                                enabled = task.id !in pending,
                                onClick = {
                                    editing = task
                                    editText = task.title
                                }
                            ) {
                                Text("✎")
                            }

                            TextButton(
                                enabled = task.id !in pending,
                                onClick = { deleting = task }
                            ) {
                                Text("×")
                            }
                        }
                    }
                }
            }

            if (active.isEmpty()) {
                item {
                    FocusCaption(
                        "Faol vazifalar yo‘q"
                    )
                }
            }

            item {
                TextButton(onClick = { history = !history }) {
                    Text(
                        if (history) "Tarixni yopish ↑"
                        else "Bajarilganlar (${completed.size}) ↓"
                    )
                }
            }

            if (history) {
                items(completed, key = { it.id }) { task ->
                    FocusCard {
                        Text(task.title, fontSize = 14.sp)
                        Row {
                            TextButton(
                                onClick = { vm.restoreTask(task) }
                            ) {
                                Text("Tiklash")
                            }
                            TextButton(
                                onClick = { deleting = task }
                            ) {
                                Text("O‘chirish")
                            }
                        }
                    }
                }
            }
        }
    }

    editing?.let { task ->
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("Vazifani tahrirlash") },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it }
                )
            },
            confirmButton = {
                TextButton(
                    enabled = editText.isNotBlank(),
                    onClick = {
                        vm.editTask(task, editText)
                        editing = null
                    }
                ) {
                    Text("Saqlash")
                }
            },
            dismissButton = {
                TextButton(onClick = { editing = null }) {
                    Text("Bekor qilish")
                }
            }
        )
    }

    deleting?.let { task ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Vazifani o‘chirish?") },
            text = { Text(task.title) },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteTask(task)
                    deleting = null
                }) {
                    Text("O‘chirish")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) {
                    Text("Bekor qilish")
                }
            }
        )
    }
}

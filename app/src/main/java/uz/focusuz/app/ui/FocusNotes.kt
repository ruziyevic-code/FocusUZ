package uz.focusuz.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.focusuz.app.data.FocusNote
import uz.focusuz.app.data.FocusViewModel

@Composable
fun FocusNotes(vm: FocusViewModel) {
    val notes by vm.notes.collectAsState()
    var search by rememberSaveable { mutableStateOf("") }
    var editing by remember { mutableStateOf<FocusNote?>(null) }
    var deleting by remember { mutableStateOf<FocusNote?>(null) }
    var title by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }

    val visible = notes.filter {
        !it.archived &&
        (it.title.contains(search, true) ||
         it.content.contains(search, true))
    }

    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FocusTitle("Zametkalar")

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text("Qidirish...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        FocusButton(
            text = "+ Yangi zametka",
            onClick = {
                title = ""
                content = ""
                editing = FocusNote()
            },
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(visible, key = { it.id }) { note ->
                FocusCard(
                    modifier = Modifier.clickable {
                        title = note.title
                        content = note.content
                        editing = note
                    }
                ) {
                    if (note.pinned) {
                        FocusCaption("Mahkamlangan")
                    }

                    Text(
                        note.title.ifBlank { "Sarlavhasiz" },
                        fontSize = 16.sp
                    )

                    Text(
                        note.content,
                        fontSize = 13.sp,
                        maxLines = 4
                    )

                    Row {
                        TextButton(onClick = {
                            vm.saveNote(
                                note.copy(pinned = !note.pinned)
                            )
                        }) {
                            Text(
                                if (note.pinned) "Ajratish"
                                else "Mahkamlash"
                            )
                        }

                        TextButton(onClick = {
                            deleting = note
                        }) {
                            Text("O‘chirish")
                        }
                    }
                }
            }

            if (visible.isEmpty()) {
                item {
                    FocusCaption("Zametkalar topilmadi")
                }
            }
        }
    }

    editing?.let { note ->
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("Zametka") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("Sarlavha") }
                    )

                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        placeholder = { Text("Fikrlaringiz...") },
                        minLines = 5,
                        maxLines = 8
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = title.isNotBlank() ||
                              content.isNotBlank(),
                    onClick = {
                        vm.saveNote(
                            note.copy(
                                title = title.trim(),
                                content = content
                            )
                        )
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

    deleting?.let { note ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Zametkani o‘chirish?") },
            text = {
                Text("Bu amalni qaytarib bo‘lmaydi.")
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteNote(note)
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

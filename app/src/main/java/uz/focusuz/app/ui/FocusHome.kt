package uz.focusuz.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.focusuz.app.data.FocusViewModel

@Composable
fun FocusNavIcon(index: Int) {
    val color = LocalContentColor.current

    Canvas(Modifier.size(22.dp)) {
        val w = size.width
        val h = size.height
        val s = 1.8.dp.toPx()

        fun line(x1: Float, y1: Float, x2: Float, y2: Float) {
            drawLine(
                color,
                Offset(w*x1, h*y1),
                Offset(w*x2, h*y2),
                s
            )
        }

        when (index) {
            0 -> {
                line(.1f,.45f,.5f,.1f)
                line(.5f,.1f,.9f,.45f)
                line(.2f,.4f,.2f,.9f)
                line(.8f,.4f,.8f,.9f)
                line(.2f,.9f,.8f,.9f)
            }
            1 -> {
                drawCircle(color, w*.4f, style=Stroke(s))
                line(.5f,.5f,.5f,.23f)
                line(.5f,.5f,.72f,.5f)
            }
            2 -> {
                drawRect(color, style=Stroke(s))
                line(.2f,.5f,.43f,.72f)
                line(.43f,.72f,.8f,.25f)
            }
            3 -> {
                line(.2f,.9f,.2f,.55f)
                line(.5f,.9f,.5f,.15f)
                line(.8f,.9f,.8f,.35f)
            }
            else -> {
                line(.1f,.25f,.9f,.25f)
                line(.1f,.5f,.9f,.5f)
                line(.1f,.75f,.9f,.75f)
            }
        }
    }
}

@Composable
fun FocusHome(vm: FocusViewModel) {
    val timer = remember { FocusTimerState() }
    FocusTimerEngine(timer, vm)

    var selected by rememberSaveable {
        mutableStateOf(0)
    }

    val tasks by vm.tasks.collectAsState()
    val sessions by vm.sessions.collectAsState()

    val labels = listOf(
        "Asosiy", "Fokus", "Vazifalar",
        "Statistika", "Yana"
    )

    val active = tasks.filter { !it.archived }
    val done = active.count { it.completed }
    val progress = if (active.isEmpty()) 0f
        else done.toFloat() / active.size

    val focusMinutes = sessions
        .filter { it.type == "focus" }
        .sumOf { it.durationSeconds } / 60

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                modifier = Modifier.heightIn(min = 64.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                labels.forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected = selected == index,
                        onClick = { selected = index },
                        icon = { FocusNavIcon(index) },
                        label = {
                            Text(label, fontSize = 10.sp)
                        },
                        alwaysShowLabel = true
                    )
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 14.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "FocusUZ",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    FocusCaption("ULTIMATE 3.0")
                }
                TextButton(onClick = { selected = 4 }) {
                    Text("Sozlamalar")
                }
            }

            if (selected == 0) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FocusTitle("Bugungi rejangiz")
                    }
                    item {
                        FocusCard {
                            FocusCaption("VAZIFALAR NATIJASI")
                            Text(
                                "${(progress * 100).toInt()}%",
                                fontSize = 30.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth()
                            )
                            FocusCaption("$done / ${active.size} bajarildi")
                        }
                    }
                    item {
                        FocusCard {
                            FocusCaption("UMUMIY FOKUS VAQTI")
                            FocusTitle("$focusMinutes min")
                        }
                    }
                    item {
                        FocusTitle("Vazifalar")
                    }
                    item {
                        FocusButton(
                            text = "Vazifalarni ochish",
                            onClick = { selected = 2 },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else if (selected == 1) {
                FocusTimer(timer)
            } else if (selected == 2) {
                FocusTasks(vm)
            } else {
                FocusTitle(labels[selected])
                Spacer(Modifier.height(12.dp))
                FocusCaption("Bo‘lim keyingi bosqichda ulanadi")
            }
        }
    }
}

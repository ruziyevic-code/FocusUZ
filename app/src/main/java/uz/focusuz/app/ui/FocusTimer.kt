package uz.focusuz.app.ui

import android.os.SystemClock
import android.content.Intent
import android.os.Build
import uz.focusuz.app.service.FocusTimerService
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import uz.focusuz.app.data.FocusViewModel

class FocusTimerState {
    var rest by mutableStateOf(false)
    var minutes by mutableStateOf(25)
    var remaining by mutableStateOf(1500L)
    var deadline by mutableStateOf(0L)

    val running: Boolean
        get() = deadline > 0

    fun select(value: Int) {
        deadline = 0
        minutes = value
        remaining = value * 60L
    }

    fun mode(value: Boolean) {
        rest = value
        select(if (value) 5 else 25)
    }

    fun toggle() {
        if (running) {
            remaining = (
                (deadline - SystemClock.elapsedRealtime() + 999) / 1000
            ).coerceAtLeast(0)
            deadline = 0
        } else {
            if (remaining == 0L) {
                remaining = minutes * 60L
            }
            deadline = SystemClock.elapsedRealtime() +
                remaining * 1000
        }
    }

    fun reset() {
        select(minutes)
    }
}

@Composable
fun FocusTimerEngine(
    state: FocusTimerState,
    vm: FocusViewModel
) {
    val context = LocalContext.current

    LaunchedEffect(state.deadline) {

        val service = Intent(
            context,
            FocusTimerService::class.java
        )

        if (state.running) {
            val left = (
                state.deadline -
                SystemClock.elapsedRealtime()
            ).coerceAtLeast(0L)

            if (left > 0L) {
                service.putExtra(
                    "deadline",
                    System.currentTimeMillis() + left
                )

                if (Build.VERSION.SDK_INT >= 26) {
                    context.startForegroundService(service)
                } else {
                    context.startService(service)
                }
            }
        } else {
            context.stopService(service)
        }

        while (state.running) {
            state.remaining = (
                (state.deadline -
                    SystemClock.elapsedRealtime() + 999) / 1000
            ).coerceAtLeast(0)

            if (state.remaining == 0L) {
                state.deadline = 0

                context.stopService(service)
                FocusAlert.play(context)

                vm.saveSession(
                    state.minutes * 60L,
                    if (state.rest) "rest" else "focus"
                )
                break
            }

            delay(200)
        }
    }
}

@Composable
fun FocusTimer(state: FocusTimerState) {
    val primary = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.surfaceVariant

    val progress by animateFloatAsState(
        targetValue = (
            state.remaining.toFloat() /
                (state.minutes * 60f)
        ).coerceIn(0f, 1f),
        animationSpec = tween(700),
        label = "timer"
    )

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !state.rest,
                onClick = { state.mode(false) },
                label = { Text("Fokus") },
                enabled = !state.running,
                modifier = Modifier.weight(1f)
            )

            FilterChip(
                selected = state.rest,
                onClick = { state.mode(true) },
                label = { Text("Dam olish") },
                enabled = !state.running,
                modifier = Modifier.weight(1f)
            )
        }

        Box(
            Modifier.size(190.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.fillMaxSize().padding(8.dp)) {
                drawCircle(
                    color = track,
                    style = Stroke(7.dp.toPx())
                )

                drawArc(
                    color = primary,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(
                        width = 7.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FocusCaption(
                    if (state.rest) "TANAFFUS"
                    else "FOKUS VAQTI"
                )

                Text(
                    "%02d:%02d".format(
                        state.remaining / 60,
                        state.remaining % 60
                    ),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.SemiBold
                )

                FocusCaption(
                    if (state.running) "Davom etmoqda"
                    else if (state.remaining == 0L) "Yakunlandi"
                    else "Boshlashga tayyor"
                )
            }
        }

        val options = if (state.rest)
            listOf(5, 10, 15)
        else listOf(15, 25, 45, 60)

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            options.forEach { value ->
                FilterChip(
                    selected = state.minutes == value,
                    enabled = !state.running,
                    onClick = { state.select(value) },
                    label = { Text("$value", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        FocusButton(
            text = if (state.running) "Pauza"
                else if (
                    state.remaining in 1 until
                    state.minutes * 60L
                ) "Davom ettirish"
                else "Boshlash",
            onClick = { state.toggle() },
            modifier = Modifier.fillMaxWidth()
        )

        TextButton(onClick = { state.reset() }) {
            Text("Qayta boshlash")
        }

        if (state.rest) {
            FocusCard {
                FocusTitle("Tanaffus tavsiyalari")
                Text("Suv ichish", fontSize = 13.sp)
                Text("Biroz yurish", fontSize = 13.sp)
                Text("Ko‘zlarni dam oldirish", fontSize = 13.sp)
            }
        }
    }
}

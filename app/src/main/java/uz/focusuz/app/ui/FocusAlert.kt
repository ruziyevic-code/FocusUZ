package uz.focusuz.app.ui

import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object FocusAlert {

    fun play(context: Context) {
        val prefs = context.getSharedPreferences(
            "focus_settings",
            Context.MODE_PRIVATE
        )

        if (prefs.getBoolean("sound", true)) {
            runCatching {
                val uri = RingtoneManager.getDefaultUri(
                    RingtoneManager.TYPE_NOTIFICATION
                )

                RingtoneManager.getRingtone(
                    context, uri
                )?.play()
            }
        }

        if (prefs.getBoolean("vibration", true)) {
            runCatching {
                val vibrator =
                    if (Build.VERSION.SDK_INT >= 31) {
                        context.getSystemService(
                            VibratorManager::class.java
                        ).defaultVibrator
                    } else {
                        context.getSystemService(
                            Context.VIBRATOR_SERVICE
                        ) as Vibrator
                    }

                if (vibrator.hasVibrator()) {
                    vibrator.vibrate(
                        VibrationEffect.createWaveform(
                            longArrayOf(0, 200, 150, 200),
                            -1
                        )
                    )
                }
            }
        }
    }
}

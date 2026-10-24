package com.sumetph.bcontrol

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast

/** Ongoing notification in the shade: level bar, -/+ and preset buttons. Tap body opens the slider. */
object Panel {
    private const val CHANNEL = "panel"
    private const val ID = 1
    const val ACTION_PRESET = "com.sumetph.bcontrol.PRESET"
    const val ACTION_STEP = "com.sumetph.bcontrol.STEP"
    const val EXTRA = "value"

    private val PRESET_IDS = intArrayOf(R.id.preset0, R.id.preset1, R.id.preset2, R.id.preset3)

    // ponytail: shows the level as of the last change made here; system slider changes aren't reflected until next tap.
    fun show(ctx: Context) {
        val nm = ctx.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CHANNEL, "แผงความสว่าง", NotificationManager.IMPORTANCE_LOW))

        val level = Brightness.get(ctx.contentResolver)
        val views = RemoteViews(ctx.packageName, R.layout.panel).apply {
            setTextViewText(R.id.label, "ความสว่าง $level / ${Brightness.MAX}")
            setProgressBar(R.id.level, Brightness.MAX, level, false)
            setOnClickPendingIntent(R.id.minus, broadcast(ctx, ACTION_STEP, -Brightness.STEP, 0))
            setOnClickPendingIntent(R.id.plus, broadcast(ctx, ACTION_STEP, Brightness.STEP, 1))
            Presets.SLOTS.forEach { slot ->
                setTextViewText(PRESET_IDS[slot], Presets.get(ctx, slot).toString())
                setOnClickPendingIntent(PRESET_IDS[slot], broadcast(ctx, ACTION_PRESET, slot, 10 + slot))
            }
        }
        val open = PendingIntent.getActivity(
            ctx, 0, Intent(ctx, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = Notification.Builder(ctx, CHANNEL)
            .setSmallIcon(R.drawable.ic_brightness)
            .setCustomContentView(views)
            .setCustomBigContentView(views)
            .setContentIntent(open)
            .setOngoing(true)
            .setShowWhen(false)
            .build()
        nm.notify(ID, notification)
    }

    private fun broadcast(ctx: Context, action: String, value: Int, requestCode: Int): PendingIntent {
        val intent = Intent(ctx, PanelReceiver::class.java).setAction(action).putExtra(EXTRA, value)
        return PendingIntent.getBroadcast(
            ctx, requestCode, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }
}

class PanelReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val value = intent.getIntExtra(Panel.EXTRA, 0)
        try {
            when (intent.action) {
                Panel.ACTION_PRESET -> Brightness.set(ctx.contentResolver, Presets.get(ctx, value))
                Panel.ACTION_STEP -> Brightness.set(ctx.contentResolver, Brightness.get(ctx.contentResolver) + value)
            }
        } catch (e: RuntimeException) {
            Toast.makeText(ctx, "ตั้งความสว่างไม่สำเร็จ: ${e.message}", Toast.LENGTH_LONG).show()
        }
        Panel.show(ctx) // also covers BOOT_COMPLETED
    }
}

package com.sumetph.bcontrol

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var label: TextView
    private lateinit var seek: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }
        setContentView(root)

        if (!Settings.System.canWrite(this)) {
            showGrantHelp(root)
            return
        }

        label = TextView(this).apply { textSize = 20f }
        seek = SeekBar(this).apply {
            min = Brightness.MIN
            max = Brightness.MAX
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar, value: Int, fromUser: Boolean) {
                    if (fromUser) apply(value)
                }
                override fun onStartTrackingTouch(s: SeekBar) {}
                override fun onStopTrackingTouch(s: SeekBar) = Panel.show(this@MainActivity)
            })
        }
        root.addView(label)
        root.addView(LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            addView(stepButton("−", -Brightness.STEP))
            addView(seek, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(stepButton("+", Brightness.STEP))
        })
        root.addView(TextView(this).apply { text = "แตะ = ใช้ค่า, กดค้าง = บันทึกค่าปัจจุบัน" })
        Presets.SLOTS.forEach { root.addView(presetButton(it)) }

        refresh(Brightness.get(contentResolver))
        Panel.show(this)
    }

    private fun stepButton(label: String, delta: Int) = Button(this).apply {
        text = label
        setOnClickListener {
            apply(seek.progress + delta)
            Panel.show(context)
        }
    }

    private fun presetButton(slot: Int): Button {
        return Button(this).apply {
            text = "Preset ${slot + 1}: ${Presets.get(context, slot)}"
            setOnClickListener {
                apply(Presets.get(context, slot))
                Panel.show(context)
            }
            setOnLongClickListener {
                Presets.save(context, slot, seek.progress)
                text = "Preset ${slot + 1}: ${seek.progress}"
                Panel.show(context)
                true
            }
        }
    }

    private fun apply(value: Int) {
        try {
            val v = value.coerceIn(Brightness.MIN, Brightness.MAX)
            Brightness.set(contentResolver, v)
            refresh(v)
        } catch (e: RuntimeException) {
            Toast.makeText(this, "ตั้งความสว่างไม่สำเร็จ: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun refresh(value: Int) {
        label.text = "ความสว่าง: $value / ${Brightness.MAX}"
        seek.progress = value
    }

    private fun showGrantHelp(root: LinearLayout) {
        root.addView(TextView(this).apply {
            textSize = 16f
            text = "แอปยังไม่มีสิทธิ์แก้ไขการตั้งค่าระบบ กดปุ่มด้านล่างแล้วเปิดอนุญาต จากนั้นเปิดแอปใหม่"
        })
        root.addView(Button(this).apply {
            text = "เปิดหน้าอนุญาต"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:$packageName")))
            }
        })
    }
}

package com.myskripsi.gokos.ui.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.myskripsi.gokos.R

class CustomPasswordEditText : AppCompatEditText, View.OnTouchListener {

    private lateinit var eyeIcon: Drawable
    private lateinit var eyeOffIcon: Drawable
    private var isPasswordVisible = false

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        // --- PERBAIKAN DI SINI: Gunakan .mutate() ---
        // .mutate() memastikan drawable ini memiliki state sendiri dan tidak berbagi
        // dengan drawable lain di aplikasi, mencegah bug penggambaran ulang.
        eyeIcon = ContextCompat.getDrawable(context, R.drawable.ic_eye_24)!!.mutate()
        eyeOffIcon = ContextCompat.getDrawable(context, R.drawable.ic_eye_off)!!.mutate()

        // Atur listener sentuhan
        setOnTouchListener(this)

        // Panggil toggle sekali untuk memastikan ikon awal sudah benar
        // berdasarkan XML (jika XML sudah set ic_eye_24, ini akan
        // memastikan state internal konsisten).
        updateIcon()

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                error = if ((s?.length ?: 0) < 8) {
                    context.getString(R.string.password_too_short)
                } else {
                    null
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val drawableEnd = compoundDrawablesRelative[2]
            if (drawableEnd != null) {
                if (event.rawX >= (right - drawableEnd.bounds.width() - paddingRight)) {
                    togglePasswordVisibility()
                    return true
                }
            }
        }
        return false
    }

    // Fungsi baru untuk memisahkan logika toggle dan update ikon
    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        updateIcon()
        setSelection(text?.length ?: 0)
    }

    // Fungsi baru yang menangani pembaruan UI (inputType dan ikon)
    private fun updateIcon() {
        val startDrawable = compoundDrawablesRelative[0]
        if (isPasswordVisible) {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            setCompoundDrawablesRelativeWithIntrinsicBounds(startDrawable, null, eyeOffIcon, null)
        } else {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setCompoundDrawablesRelativeWithIntrinsicBounds(startDrawable, null, eyeIcon, null)
        }
    }
}
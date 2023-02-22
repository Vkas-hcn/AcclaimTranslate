package com.vkas.secondtranslation.utils

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import com.vkas.secondtranslation.R

object CopyUtils {
    //保存文本内容到剪切板
    fun copyClicks(context: Context, text: String?) {
        val cbm =context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cbm.text = text
        Toast.makeText(context, context.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
    }
}
package com.suraj.simplenotes.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.suraj.simplenotes.R

class NotesDialog(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_note)
        setCanceledOnTouchOutside(false)
    }
}
package com.suraj.simplenotes.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.suraj.simplenotes.R
import com.suraj.simplenotes.models.Note
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class NotesAdapter(private val context: Context, private val notesList: ArrayList<Note>) : RecyclerView.Adapter<NotesAdapter.MyViewHolder>() {

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var note: TextView = view.findViewById(R.id.note)
        var dot: TextView = view.findViewById(R.id.dot)
        var timestamp: TextView = view.findViewById(R.id.timestamp)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_item_note, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val note = notesList[position]
        holder.note.text = note.note

        // Displaying dot from HTML character code
        holder.dot.text = Html.fromHtml("&#8226;")

        // Formatting and displaying timestamp
        holder.timestamp.text = formatDate(note.timestamp)
    }

    override fun getItemCount(): Int {
        return notesList.size
    }

    /**
     * Formatting timestamp to `MMM d` format
     * Input: 2018-02-21 00:15:42
     * Output: Feb 21
     */
    @SuppressLint("SimpleDateFormat")
    private fun formatDate(dateStr: String): String {
        try {
            val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val date: Date = fmt.parse(dateStr)
            val fmtOut = SimpleDateFormat("MMM d")
            return fmtOut.format(date)
        } catch (e: ParseException) {
        }
        return ""
    }

}
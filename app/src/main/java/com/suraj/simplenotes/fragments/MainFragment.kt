package com.suraj.simplenotes.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.ClipDrawable.HORIZONTAL
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.suraj.simplenotes.R
import com.suraj.simplenotes.adapter.NotesAdapter
import com.suraj.simplenotes.databases.DatabaseHelper
import com.suraj.simplenotes.models.Note
import com.suraj.simplenotes.utils.RecyclerTouchListener


class MainFragment : Fragment() {

    lateinit var mGoogleSignInClient: GoogleSignInClient
    private var mAdapter: NotesAdapter? = null
    private val notesList: ArrayList<Note> = arrayListOf()
    private var noNotesView: TextView? = null
    private var db: DatabaseHelper? = null
    private var name: String = ""
    private var email: String = ""

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        mGoogleSignInClient= GoogleSignIn.getClient(requireActivity(), gso)

        (activity as AppCompatActivity?)!!.setSupportActionBar(view.findViewById(R.id.toolbar))
        (activity as AppCompatActivity?)!!.supportActionBar?.setDisplayHomeAsUpEnabled(false)

        val sharedPreference =  requireActivity().getSharedPreferences("User_data", Context.MODE_PRIVATE)
        name = sharedPreference.getString("username","user").toString()
        email = sharedPreference.getString("email","null").toString()

        view.findViewById<TextView>(R.id.text_welcome).text = "Welcome $name"

        db = DatabaseHelper(requireContext())
        val itemDecor = DividerItemDecoration(requireContext(), HORIZONTAL)

        val recyclerView : RecyclerView = view.findViewById(R.id.recycler_notes)
        noNotesView = view.findViewById(R.id.empty_notes_view)

        notesList.addAll(db!!.readAllNotes(email))

        mAdapter = NotesAdapter(requireContext(), notesList)

        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = mLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = mAdapter
        recyclerView.addItemDecoration(itemDecor)

        toggleEmptyNotes()

        view.findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            showNoteDialog(false, null, -1)
        }

        recyclerView.addOnItemTouchListener(
                RecyclerTouchListener(requireContext(),
                        recyclerView, object : RecyclerTouchListener.ClickListener {
                    override fun onClick(view: View?, position: Int) {
                        //Do Nothing
                    }

                    override fun onLongClick(view: View?, position: Int) {
                        showActionsDialog(position)
                    }
                })
        )

        view.findViewById<TextView>(R.id.text_logout).setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            mGoogleSignInClient.signOut()
            Log.e("Current User",FirebaseAuth.getInstance().currentUser.toString())
            Navigation.findNavController(view).navigate(R.id.action_fragment_main_to_fragment_signin)
        }
    }

    private fun showNoteDialog(shouldUpdate: Boolean, note: Note?, position: Int) {
        val layoutInflaterAndroid =
            LayoutInflater.from(requireContext())
        val view: View = layoutInflaterAndroid.inflate(R.layout.dialog_note, null)
        val alertDialogBuilderUserInput = AlertDialog.Builder(requireContext())
        alertDialogBuilderUserInput.setView(view)
        val inputNote = view.findViewById<EditText>(R.id.note)
        val dialogTitle = view.findViewById<TextView>(R.id.dialog_title)
        dialogTitle.text =
            if (!shouldUpdate) getString(R.string.lbl_new_note_title) else getString(R.string.lbl_edit_note_title)
        if (shouldUpdate && note != null) {
            inputNote.setText(note.note)
        }
        alertDialogBuilderUserInput
            .setCancelable(false)
            .setPositiveButton(
                    if (shouldUpdate) "update" else "save"
            ) { _, _ -> }
            .setNegativeButton(
                    "cancel"
            ) { dialogBox, _ -> dialogBox.cancel() }

        val alertDialog = alertDialogBuilderUserInput.create()
        alertDialog.show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(View.OnClickListener {
            if (TextUtils.isEmpty(inputNote.text.toString())) {
                Toast.makeText(requireContext(), "Cannot save empty note!", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            } else {
                alertDialog.dismiss()
            }
            if (shouldUpdate && note != null) {
                Toast.makeText(requireContext(), "Note updated!", Toast.LENGTH_SHORT).show()
                updateNote(inputNote.text.toString(), position)
            } else {
                createNote(inputNote.text.toString(),email)
            }
        })
    }

    private fun showActionsDialog(position: Int) {
        val colors = arrayOf<CharSequence>("Edit", "Delete")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose option")
        builder.setItems(colors) { _, which ->
            if (which == 0) {
                showNoteDialog(true, notesList[position], position)
            } else {
               deleteNote(position)
            }
        }
        builder.show()
    }

    private fun createNote(note: String, email: String) {
        val id = db!!.insertNote(note, email)
        val n: Note? = db!!.getNote(id)
        if (n != null) {
            notesList.add(0, n)
            mAdapter!!.notifyDataSetChanged()
            toggleEmptyNotes()
        }
    }

    private fun updateNote(note: String, position: Int) {
        val n = notesList[position]
        n.note = note
        db?.updateNote(n)
        notesList[position] = n
        mAdapter!!.notifyItemChanged(position)
        toggleEmptyNotes()
    }

    private fun deleteNote(position: Int) {
        db!!.deleteNote(notesList[position])
        notesList.removeAt(position)
        mAdapter!!.notifyItemRemoved(position)
        toggleEmptyNotes()
    }

    private fun toggleEmptyNotes() {
        if (db?.getNotesCount()!! > 0) {
            noNotesView!!.visibility = View.GONE
        } else {
            noNotesView!!.visibility = View.VISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_exit -> {
                requireActivity().finishAffinity()
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
        return true
    }

}
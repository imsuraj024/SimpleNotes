package com.suraj.simplenotes.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.suraj.simplenotes.R


class SignInFragment : Fragment() {

    lateinit var mGoogleSignInClient: GoogleSignInClient
    val Req_Code:Int=123
    var firebaseAuth= FirebaseAuth.getInstance()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_in, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient=GoogleSignIn.getClient(requireActivity(), gso)

        firebaseAuth= FirebaseAuth.getInstance()

        view.findViewById<SignInButton>(R.id.btn_sign_in).setSize(SignInButton.SIZE_STANDARD)

        view.findViewById<SignInButton>(R.id.btn_sign_in).setOnClickListener {
            signIn()
        }

    }

    private fun signIn() {

        val signInIntent:Intent=mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, Req_Code)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==Req_Code){
            val task:Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult(task)
        }
    }

    private fun handleResult(completedTask: Task<GoogleSignInAccount>){
        try {
            val account: GoogleSignInAccount? =completedTask.getResult(ApiException::class.java)
            if (account != null) {
                UpdateUI(account)
            }
        } catch (e: ApiException){
            Toast.makeText(requireContext(), e.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun UpdateUI(account: GoogleSignInAccount){
        val credential= GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task->
            if(task.isSuccessful) {
                Log.e("Email", account.email.toString())
                Log.e("Username", account.displayName.toString())
                val sharedPreference =  requireActivity().getSharedPreferences("User_data", Context.MODE_PRIVATE)
                val editor = sharedPreference.edit()
                editor.putString("email", account.email.toString())
                editor.putString("username", account.displayName.toString())
                editor.apply()
                view?.let { Navigation.findNavController(it).navigate(R.id.action_fragment_signin_to_fragment_main) }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        if (currentUser != null){
            view?.let { Navigation.findNavController(it).navigate(R.id.action_fragment_signin_to_fragment_main) }
        }
    }


}
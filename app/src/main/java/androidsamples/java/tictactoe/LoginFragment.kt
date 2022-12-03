package androidsamples.java.tictactoe

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*


class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var userReference: CollectionReference
    private lateinit var pd: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        userReference = FirebaseFirestore.getInstance().collection("users")
        //if a user is logged in, go to Dashboard
        if (auth.currentUser != null) {
            Navigation.findNavController(requireView()).navigate(R.id.action_login_successful);
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        email = view.findViewById(R.id.edit_email)
        pd = ProgressDialog(context)
        password = view.findViewById(R.id.edit_password)
        pd.setMessage("Loading...")
        pd.setTitle("Authentication")

        view.findViewById<View>(R.id.btn_log_in).setOnClickListener {
            pd.show()
            if (email.text.toString().isEmpty() || password.text.toString().isEmpty()) {
                Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString())
                .addOnCompleteListener { task: Task<AuthResult> ->
                    if (!task.isSuccessful) {
                        if (task.exception is FirebaseAuthUserCollisionException) {
                            login(
                                email.text.toString(), password.text.toString()
                            )
                            Log.i("User created", task.result.user!!.uid)
                        } else {
                            Toast.makeText(
                                context, task.exception!!.message, Toast.LENGTH_SHORT
                            ).show()
                            task.exception!!.printStackTrace()
                        }
                    } else {
                        NavHostFragment.findNavController(this)
                            .navigate(R.id.action_login_successful)
                        try {
                            userReference.document(task.result.user!!.uid)
                                .set(mapOf("won" to 0, "draw" to 0, "lost" to 0))
                                .addOnSuccessListener {
                                    Log.d("TICTACTOEAUTH", "SUCESS")
                                }
                        } catch (e: java.lang.Error) {
                            Log.d("TICTACTOEAUTH", e.toString())
                        }

                        Toast.makeText(context, "User Registered", Toast.LENGTH_SHORT).show()
                    }
                    pd.dismiss()
                }
        }
        return view
    } // No options menu in login fragment.

    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    Log.i("LOGIN", "SUCCESS")
                    Log.i(
                        "User logged in",
                        Objects.requireNonNull(Objects.requireNonNull(task.result)!!.user)!!.uid
                    )
                    Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                    NavHostFragment.findNavController(this).navigate(R.id.action_login_successful)
                } else {
                    Log.i("LOGIN", "FAIL")
                    Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()

                }
                pd.dismiss()
            }
    }
}
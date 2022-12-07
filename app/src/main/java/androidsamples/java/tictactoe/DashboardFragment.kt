package androidsamples.java.tictactoe

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore


class DashboardFragment
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
    : Fragment() {

    private lateinit var mNavController: NavController
    private lateinit var auth: FirebaseAuth
    private lateinit var gameReference: CollectionReference
    private lateinit var userReference: CollectionReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var won: TextView
    private lateinit var lost: TextView
    private lateinit var info: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setHasOptionsMenu(true) // Needed to display the action menu for this fragment
        gameReference = FirebaseFirestore.getInstance().collection("games")
        userReference = FirebaseFirestore.getInstance().collection("users")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mNavController = Navigation.findNavController(view)

        recyclerView = view.findViewById(R.id.list)
        won = view.findViewById(R.id.won_score)
        lost = view.findViewById(R.id.lost_score)
        info = view.findViewById(R.id.open_display)

        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            mNavController.navigate(R.id.action_need_auth)
            return
        }

        val gameList: MutableList<GameModel> = mutableListOf()
        @Suppress("UNCHECKED_CAST")
        gameReference.addSnapshotListener { value, _ ->
            gameList.clear()
            if (value != null) {
                for (shot in value.documents) {
                    if (shot.data != null) {
                        Log.d(TAG, "onViewCreated: ${shot.data}")
                        val game = GameModel(
                            shot.data!!["gameState"] as List<String>,
                            shot.data!!["open"] as Boolean,
                            shot.data!!["currentHost"] as String,
                            shot.data!!["challenger"] as String,
                            (shot.data!!["turn"] as Long).toInt(),
                            shot.data!!["gameId"] as String
                        )
                        if (game.isOpen)
                            gameList.add(game)
                        Log.d(TAG, "onViewCreated: $game")
                    }
                }
            }

            recyclerView.adapter =
                OpenGamesAdapter(gameList as ArrayList<GameModel>)
            recyclerView.layoutManager = LinearLayoutManager(context)
            info.text = if (gameList.isEmpty()) "No Open Games Available :(" else "Open Games"
        }

        userReference.document(auth.currentUser!!.uid).addSnapshotListener { value, _ ->
            won.text = value?.get("won").toString()
            lost.text = value?.get("lost").toString()
        }

        // Show a dialog when the user clicks the "new game" button
        view.findViewById<View>(R.id.fab_new_game).setOnClickListener {

            // A listener for the positive and negative buttons of the dialog
            val listener = DialogInterface.OnClickListener { _: DialogInterface?, which: Int ->
                var gameType = "No type"
                var gameId = ""
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    gameType = getString(R.string.two_player)
                    gameReference.add(
                        GameModel(
                            currentHost = auth.currentUser!!.uid,
                            gameId = gameId,
                            challenger = ""
                        )
                    ).addOnSuccessListener { documentReference ->
                        gameId = documentReference.id
                        Log.d(TAG, "onViewCreated: $gameId")
                        gameReference.document(gameId).update("gameId", gameId)
                        val action: NavDirections =
                            DashboardFragmentDirections.actionGame(gameType, gameId)
                        mNavController.navigate(action)
                    }
                    Log.i("FIREBASE", "Value set")
                } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                    gameType = getString(R.string.one_player)
                }
                Log.d(TAG, "New Game: $gameType")
            }

            // create the dialog
            val dialog = AlertDialog.Builder(requireActivity())
                .setTitle(R.string.new_game)
                .setMessage(R.string.new_game_dialog_message)
                .setPositiveButton(R.string.two_player, listener)
                .setNegativeButton(R.string.one_player, listener)
                .setNeutralButton(R.string.cancel) { d: DialogInterface, _: Int -> d.dismiss() }
                .create()
            dialog.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_logout, menu)
        // this action menu is handled in MainActivity
    }

    companion object {
        private const val TAG = "DashboardFragment"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_logout) {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                .navigate(R.id.action_need_auth)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
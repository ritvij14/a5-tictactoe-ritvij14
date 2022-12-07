package androidsamples.java.tictactoe

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*


class GameFragment : Fragment() {

    private val mButtons = arrayOfNulls<Button>(GRID_SIZE)
    private var mNavController: NavController? = null
    private lateinit var display: TextView
    private var isSinglePlayer = true
    private var myChar = "X"
    private var otherChar = "O"
    private var myTurn = true
    private var gameState = arrayOf("", "", "", "", "", "", "", "", "")
    private var gameEnded = false
    private lateinit var game: GameModel
    private var isHost = true
    private lateinit var gameReference: CollectionReference
    private lateinit var userReference: CollectionReference

    companion object {
        private const val TAG = "GameFragment"
        private const val GRID_SIZE = 9
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // Needed to display the action menu for this fragment

        // Extract the argument passed with the action in a type-safe way
        val args = GameFragmentArgs.fromBundle((requireArguments()))
        Log.d(TAG, "New game type = " + args.gameType)
        isSinglePlayer = (args.gameType == "One-Player")

        gameReference = FirebaseFirestore.getInstance().collection("games")
        userReference = FirebaseFirestore.getInstance().collection("users")

        // Handle the back press by adding a confirmation dialog
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(TAG, "Back pressed")

                if (!gameEnded) {
                    val dialog = AlertDialog.Builder(requireActivity())
                        .setTitle(R.string.confirm)
                        .setMessage(R.string.forfeit_game_dialog_message)
                        .setPositiveButton(R.string.yes) { _, _ ->
                            if (!isSinglePlayer) {
                                var userData: DocumentSnapshot? = null
                                userReference.document(FirebaseAuth.getInstance().currentUser!!.uid)
                                    .get().addOnSuccessListener { doc ->
                                        userData = doc
                                    }
                                var lost = userData?.get("lost") as Int
                                lost += 1
                                userReference.document(FirebaseAuth.getInstance().currentUser!!.uid)
                                    .update("lost", lost)
                            }
                            mNavController!!.popBackStack()
                        }
                        .setNegativeButton(
                            R.string.cancel
                        ) { d: DialogInterface, _ -> d.dismiss() }
                        .create()
                    dialog.show()
                } else {
                    assert(parentFragment != null)
                    NavHostFragment.findNavController(parentFragment!!).navigateUp()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        display = view.findViewById(R.id.display_tv)

        if (!isSinglePlayer) {
            @Suppress("UNCHECKED_CAST")
            gameReference.document(GameFragmentArgs.fromBundle((requireArguments())).gameId)
                .addSnapshotListener { value, _ ->
                    game = GameModel(
                        value?.get("gameState") as List<String>,
                        value["open"] as Boolean,
                        value["currentHost"] as String,
                        value["challenger"] as String,
                        (value["turn"] as Long).toInt(),
                        value["gameId"] as String
                    )
                    Log.d(TAG, "onViewCreated: ${game.gameState}")
                    gameState = game.gameState.toTypedArray()
                    var hostMail: String
                    var challengerMail = ""

                    view.findViewById<TextView>(R.id.display_game_id).text =
                        "Game ID: ${game.gameId}"
                    userReference.document(value["currentHost"] as String).get()
                        .addOnSuccessListener {
                            hostMail = it["email"] as String
                            view.findViewById<TextView>(R.id.display_host).text = "Host: $hostMail"
                        }
                    if (value["challenger"] != "" && value["challenger"] != null) {
                        userReference.document(value["challenger"] as String).get()
                            .addOnSuccessListener {
                                challengerMail = it["email"] as String
                                view.findViewById<TextView>(R.id.display_challenger).text =
                                    "Challenger: $challengerMail"
                            }
                    }

                    if (game.turn == 1) {
                        if (game.currentHost == FirebaseAuth.getInstance().currentUser!!.uid) {
                            isHost = true
                            myTurn = true
                            myChar = "X"
                            otherChar = "O"
                        } else {
                            isHost = false
                            myTurn = false
                            myChar = "O"
                            otherChar = "X"
                            gameReference.document(game.gameId)
                                .update("challenger", FirebaseAuth.getInstance().currentUser!!.uid)
                        }
                    } else {
                        if (game.currentHost != FirebaseAuth.getInstance().currentUser!!.uid) {
                            myTurn = true
                            myChar = "O"
                            otherChar = "X"
                            isHost = false
                            gameReference.document(game.gameId)
                                .update("challenger", FirebaseAuth.getInstance().currentUser!!.uid)
                        } else {
                            isHost = true
                            myTurn = false
                            myChar = "X"
                            otherChar = "O"
                        }
                    }
                    if (!isSinglePlayer) {
                        var check = false
                        for (s in gameState) {
                            if (s.isNotEmpty()) {
                                check = true
                                break
                            }
                        }
                        if (!check) {
                            waitForOtherPlayer()
                        }
                    } else {
                        display.setText(R.string.your_turn)
                    }
                }
        }

        mNavController = Navigation.findNavController(view)
        mButtons[0] = view.findViewById(R.id.button0)
        mButtons[1] = view.findViewById(R.id.button1)
        mButtons[2] = view.findViewById(R.id.button2)
        mButtons[3] = view.findViewById(R.id.button3)
        mButtons[4] = view.findViewById(R.id.button4)
        mButtons[5] = view.findViewById(R.id.button5)
        mButtons[6] = view.findViewById(R.id.button6)
        mButtons[7] = view.findViewById(R.id.button7)
        mButtons[8] = view.findViewById(R.id.button8)
        for (i in mButtons.indices) {
            mButtons[i]?.setOnClickListener(View.OnClickListener { v: View? ->
                if (myTurn) {
                    Log.d(TAG, "Button $i clicked")
                    (v as Button).text = myChar
                    gameState[i] = myChar
                    v.setClickable(false)
                    display.setText(R.string.waiting)
                    if (!isSinglePlayer) {
                        updateDB()
                        myTurn = updateTurn(game.turn)
                    }
                    val win: Int = checkWin()
                    if (win == 1 || win == -1) {
                        endGame(win)
                        return@OnClickListener
                    } else if (checkDraw()) {
                        endGame(0)
                        return@OnClickListener
                    }
                    myTurn = !myTurn
                    if (isSinglePlayer) {
                        computerPlayerMove()
                    } else {
                        waitForOtherPlayer()
                    }
                } else {
                    Toast.makeText(context, "Please wait for your turn!", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun checkDraw(): Boolean {
        if (checkWin() != 0) return false
        Log.i("CHECKING WIN IN DRAW", "Complete: " + checkWin())
        for (i in 0..8) {
            if (gameState[i].isEmpty()) {
                return false
            }
        }
        return true
    }

    private fun endGame(win: Int) {
        when (win) {
            1 -> {
                display.setText(R.string.you_win)
                if (!gameEnded) {
                    var userData: DocumentSnapshot?
                    userReference.document(FirebaseAuth.getInstance().currentUser!!.uid)
                        .get().addOnSuccessListener { doc ->
                            userData = doc
                            var won = (userData?.get("won") as Long).toInt()
                            won += 1
                            userReference.document(FirebaseAuth.getInstance().currentUser!!.uid)
                                .update("won", won)
                        }
                }
            }
            -1 -> {
                display.setText(R.string.you_lose)
                if (!gameEnded) {
                    var userData: DocumentSnapshot? = null
                    userReference.document(FirebaseAuth.getInstance().currentUser!!.uid)
                        .get().addOnSuccessListener { doc ->
                            userData = doc
                            var lost = userData?.get("lost") as Int
                            lost += 1
                            userReference.document(FirebaseAuth.getInstance().currentUser!!.uid)
                                .update("lost", lost)
                        }

                }
            }
            0 -> display.setText(R.string.draw)
            else -> {
                display.setText(R.string.error)
                Log.i("CHECKING DRAW", "Error: $win")
            }
        }
        for (i in 0..8) {
            mButtons[i]!!.isClickable = false
        }
        gameEnded = true
        if (!isSinglePlayer) updateDB()
    }

    private fun waitForOtherPlayer() {
        display.setText(R.string.waiting)
        @Suppress("UNCHECKED_CAST")
        gameReference.document(game.gameId).addSnapshotListener { value, _ ->
            val l = GameModel(
                value?.get("gameState") as List<String>,
                value["open"] as Boolean,
                value["currentHost"] as String,
                value["challenger"] as String,
                (value["turn"] as Long).toInt(),
                value["gameId"] as String,
            )
            game.updateGameState(l)
            gameState = game.gameState.toTypedArray()
            updateUI()
            myTurn = updateTurn(game.turn)
            display.setText(R.string.your_turn)
            val win = checkWin()
            if (win == 1 || win == -1) endGame(win) else if (checkDraw()) endGame(0)
        }

    }

    private fun updateTurn(turn: Int): Boolean {
        return turn == 1 == isHost
    }

    private fun updateUI() {
        for (i in 0..8) {
            val v = gameState[i]
            if (v.isNotEmpty()) {
                mButtons[i]!!.text = v
                mButtons[i]!!.isClickable = false
            }
        }
    }

    private fun updateDB() {
        gameReference.document(game.gameId).update("gameState", gameState.toList())
        gameReference.document(game.gameId).update("open", !gameEnded)

        if (game.turn == 1) {
            game.turn = 2
        } else {
            game.turn = 1
        }
        gameReference.document(game.gameId).update("turn", game.turn)
    }

    private fun computerPlayerMove() {
        val rand = Random()
        var x: Int = rand.nextInt(9)
        if (checkDraw()) {
            endGame(0)
            return
        }
        while (gameState[x].isNotEmpty()) x = rand.nextInt(9)
        Log.i("CHECKING CONDITIONS", "Complete")
        gameState[x] = otherChar
        mButtons[x]!!.text = otherChar
        mButtons[x]!!.isClickable = false
        myTurn = !myTurn
        display.setText(R.string.your_turn)
        val win = checkWin()
        if (win == 1 || win == -1) endGame(win) else if (checkDraw()) endGame(0)
    }

    private fun checkWin(): Int {
        val winChar: String =
            if (gameState[0] == gameState[1] && gameState[1] == gameState[2] && gameState[0].isNotEmpty()) gameState[0] else if (gameState[3] == gameState[4] && gameState[4] == gameState[5] && gameState[3].isNotEmpty()
            ) gameState[3] else if (gameState[6] == gameState[7] && gameState[7] == gameState[8] && gameState[6].isNotEmpty()
            ) gameState[6] else if (gameState[0] == gameState[3] && gameState[3] == gameState[6] && gameState[0].isNotEmpty()
            ) gameState[0] else if (gameState[4] == gameState[1] && gameState[1] == gameState[7] && gameState[1].isNotEmpty()
            ) gameState[1] else if (gameState[2] == gameState[5] && gameState[5] == gameState[8] && gameState[2].isNotEmpty()
            ) gameState[2] else if (gameState[0] == gameState[4] && gameState[4] == gameState[8] && gameState[0].isNotEmpty()
            ) gameState[0] else if (gameState[6] == gameState[4] && gameState[4] == gameState[2] && gameState[2].isNotEmpty()
            ) gameState[2] else return 0
        return if (winChar == myChar) 1 else -1
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_logout, menu)
        // this action menu is handled in MainActivity
    }
}
package androidsamples.java.tictactoe

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView

class OpenGamesAdapter(
    private val list: ArrayList<GameModel>,
    private val navController: NavController
) :
    RecyclerView.Adapter<OpenGamesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.populate(list[position].gameId, position + 1, navController)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(private val mView: View) : RecyclerView.ViewHolder(
        mView
    ) {
        private val mIdView: TextView = mView.findViewById(R.id.item_number)
        private val mContentView: TextView = mView.findViewById(R.id.content)

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }

        @SuppressLint("SetTextI18n")
        fun populate(game: String, i: Int, nav: NavController) {
            mContentView.text = game
            mIdView.text = "#$i"
            mView.setOnClickListener {
                val action: NavDirections =
                    DashboardFragmentDirections.actionGame("Two-Player", game)
                Navigation.findNavController(mView).navigate(action)
            }
        }
    }
}
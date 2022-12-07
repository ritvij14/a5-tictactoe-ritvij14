package androidsamples.java.tictactoe

data class GameModel(
    var gameState: List<String> = listOf("", "", "", "", "", "", "", "", ""),
    var isOpen: Boolean = true,
    var currentHost: String,
    var challenger: String,
    var turn: Int = 1,
    var gameId: String,
) {
    fun updateGameState(o: GameModel) {
        gameState = o.gameState
        turn = o.turn
    }
}

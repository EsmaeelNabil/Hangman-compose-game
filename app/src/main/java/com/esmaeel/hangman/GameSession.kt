package com.esmaeel.hangman

data class GameSession(
    val currentWord: String = "",
    val guesses: MutableList<String> = mutableListOf(),
    val triesLeft: Int = GameManager.MAX_STEPS,
    val score: Int = 0,
    val keyboardKeys: MutableList<String> = mutableListOf(),
    val displayWord: String = "",
    val gameState: GameState = GameState.Playing,
    val wordsLeft: Int = 0
)
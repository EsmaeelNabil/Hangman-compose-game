package com.esmaeel.hangman

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(private val gameManager: GameManager) : ViewModel() {
    val gameSession = mutableStateOf(
        GameSession(
            currentWord = gameManager.getRandomWord(),
            keyboardKeys = gameManager.currentKeyboardChars,
            displayWord = gameManager.getDisplayWord(),
            gameState = GameState.Playing,
            score = gameManager.score,
            wordsLeft = gameManager.listOfWords.size
        )
    )

    fun nextWord() {
        gameManager.newGame()
        gameSession.value = GameSession(
            currentWord = gameManager.getRandomWord(),
            keyboardKeys = gameManager.currentKeyboardChars,
            triesLeft = GameManager.MAX_STEPS,
            score = gameManager.score,
            displayWord = gameManager.getDisplayWord(),
            gameState = GameState.Playing,
            wordsLeft = gameManager.listOfWords.size
        )
    }

    fun resetTheGame() {
        gameManager.reset()
        gameSession.value = GameSession(
            currentWord = gameManager.getRandomWord(),
            keyboardKeys = gameManager.currentKeyboardChars,
            triesLeft = GameManager.MAX_STEPS,
            score = gameManager.score,
            displayWord = gameManager.getDisplayWord(),
            gameState = GameState.Playing,
            wordsLeft = gameManager.listOfWords.size
        )
    }

    fun guessWithChar(char: String) {
        with(gameSession.value) {
            gameManager.updateTires()
            val exceededTries = gameManager.hasExceededTries()

            val newDisplay = gameManager.getDisplayWord(guesses.also { it.add(char) })

            val newState = if (exceededTries) GameState.Lost else {
                if (newDisplay.contains("-"))
                    GameState.Playing
                else {
                    gameManager.onWin(currentWord)
                    GameState.Win
                }
            }

            gameSession.value = gameSession.value.copy(
                triesLeft = gameManager.tries,
                score = gameManager.score,
                keyboardKeys = keyboardKeys.also { it.remove(char) },
                displayWord = if (exceededTries) displayWord else newDisplay,
                gameState = newState,
                wordsLeft = gameManager.listOfWords.size
            )
        }
    }

}
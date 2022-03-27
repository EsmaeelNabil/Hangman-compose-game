package com.esmaeel.hangman

import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameManager @Inject constructor() {
    private val words =
        listOf("egypt", "germany", "america", "play", "programming", "running", "music")


    var listOfWords = words.toMutableList()

    companion object {
        const val MAX_STEPS = 20
    }


//    I want to be challenge only with unique words,
//    I want to how many unique words I can still guess.

    var latestRandomWord = ""
    var tries = 10
    var score = 0

    fun onWin(currentWord: String) {
        score += 10
        listOfWords.remove(currentWord)
    }

    fun updateTires(): Int {
        tries--
        return tries
    }

    fun hasExceededTries() = tries <= 0

    val currentKeyboardChars
        get() = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toLowerCase(Locale.current).split("").drop(1)
            .dropLast(1)
            .toMutableList()

    fun reset() {
        tries = 10
        listOfWords = words.toMutableList()
    }

    fun newGame() {
        tries = 10
    }

    fun getRandomWord(): String {
        if (listOfWords.isEmpty())
            reset()
        return listOfWords.random().also {
            latestRandomWord = it
        }
    }

    /**
     * returns ----- or -a-r-c depending on the guesses and the last random word for the session
     */
    fun getDisplayWord(guesses: List<String> = listOf()): String {
        val display = StringBuilder()
        latestRandomWord.forEach { char ->
            if (guesses.contains(char.toString()))
                display.append(char)
            else display.append("-")
        }
        return display.toString()
    }


}
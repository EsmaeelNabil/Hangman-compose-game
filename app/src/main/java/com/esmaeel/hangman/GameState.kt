package com.esmaeel.hangman

sealed class GameState() {
    object Playing : GameState()
    object Win : GameState()
    object Lost : GameState()
}
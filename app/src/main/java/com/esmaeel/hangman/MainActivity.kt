package com.esmaeel.hangman

import android.os.Bundle
import android.widget.Space
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.esmaeel.hangman.ui.theme.HangmanTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val vm: GameViewModel by viewModels()

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HangmanTheme {
                GameView(vm)
            }
        }
    }
}

@Composable
fun ColorAnimatedHolder(
    modifier: Modifier = Modifier,
    gameState: GameState,
    content: @Composable () -> Unit
) {
    val transition = updateTransition(gameState, label = "gameState transition")
    val color by transition.animateColor(
        label = "Color transition Animation",
        transitionSpec = {
            when {
                GameState.Playing isTransitioningTo GameState.Lost ->
                    spring(stiffness = 50f)
                else ->
                    tween(durationMillis = 1000)
            }
        },
    ) { state ->
        when (state) {
            is GameState.Lost -> Color.Red
            is GameState.Win -> Color.Green
            else -> Color.White
        }
    }

    Surface(modifier = modifier.fillMaxSize(), color = color, content = content)

}

@OptIn(ExperimentalFoundationApi::class)
@Preview(showBackground = true)
@Composable
fun GameView(vm: GameViewModel = viewModel()) {

    val session = vm.gameSession.value

    ColorAnimatedHolder(gameState = session.gameState) {

        CenteredColumn(
            Modifier
                .fillMaxWidth()
                .scrollable(
                    state = rememberScrollState(),
                    orientation = Orientation.Vertical
                )
        ) {

            InfoComposable(session = session)

            Spacer(modifier = Modifier.height(26.dp))

            when (session.gameState) {

                is GameState.Playing -> PlayingComposable(
                    modifier = Modifier.fillMaxWidth(),
                    session = session,
                    onGuess = {
                        vm.guessWithChar(it)
                    },
                )

                is GameState.Lost -> LostComposable(session)

                is GameState.Win -> Text(
                    "YOU WIN YAY !!",
                    style = TextStyle(color = Color.Black, fontSize = 50.sp)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { vm.nextWord() }) {
                    val text = if (session.wordsLeft == 0) "Start New Game" else "New Word"
                    Text(text)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = { vm.resetTheGame() }) {
                    Text("Reset")
                }
            }


        }
    }

}

@Composable
fun InfoComposable(modifier: Modifier = Modifier, session: GameSession) {
    Surface(shape = RoundedCornerShape(20.dp), elevation = 10.dp) {
        CenteredColumn(
            modifier
                .fillMaxWidth()
                .wrapContentSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            CounterComposable(
                message = "Score   : ",
                count = session.score,
                counterSize = 70,
                messageStyle = TextStyle(fontSize = 70.sp)
            )
            AnimatedVisibility(visible = (session.gameState is GameState.Win).not()) {
                CounterComposable(
                    message = "Tries Left   : ",
                    count = session.triesLeft,
                    counterSize = 20,
                    messageStyle = TextStyle(fontSize = 20.sp)
                )
            }
            CounterComposable(
                message = "Words Left : ",
                count = session.wordsLeft,
                counterSize = 20,
                messageStyle = TextStyle(fontSize = 20.sp)
            )
        }
    }
}

@Composable
fun CounterComposable(
    modifier: Modifier = Modifier,
    message: String = "",
    messageStyle: TextStyle = TextStyle(),
    count: Int = 0,
    counterSize: Int = 20,
    fontWeight: FontWeight = FontWeight.Bold
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(text = message, style = messageStyle)
        AnimatedCounter(count = count, size = counterSize, fontWeight = fontWeight)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedCounter(
    count: Int, size: Int = 50,
    fontWeight: FontWeight = FontWeight.Medium
) {
    AnimatedContent(
        targetState = count,
        transitionSpec = {
            // Compare the incoming number with the previous number.
            if (targetState > initialState) {
                // If the target number is larger, it slides up and fades in
                // while the initial (smaller) number slides up and fades out.
                slideInVertically { height -> height } + fadeIn() with
                        slideOutVertically { height -> -height } + fadeOut()
            } else {
                // If the target number is smaller, it slides down and fades in
                // while the initial number slides down and fades out.
                slideInVertically { height -> -height } + fadeIn() with
                        slideOutVertically { height -> height } + fadeOut()
            }.using(
                // Disable clipping since the faded slide-in/out should
                // be displayed out of bounds.
                SizeTransform(clip = false)
            )
        }
    ) { targetCount ->
        Text(
            text = "$targetCount",
            style = TextStyle(fontSize = size.sp, fontWeight = fontWeight)
        )
    }
}

@Composable
fun PlayingComposable(
    modifier: Modifier = Modifier,
    session: GameSession,
    onGuess: (String) -> Unit
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(20.dp), elevation = 10.dp) {
        CenteredColumn {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Choose a letter to win!")
            Spacer(modifier = Modifier.height(16.dp))

            // TODO: Remove this
            DisplayWord(
                displayWord = "Hint : ${session.currentWord.getHint()}",
                size = 12,
                fontWeight = FontWeight.ExtraBold
            )
            DisplayWord(
                displayWord = session.displayWord, size = 50,
                fontWeight = FontWeight.ExtraBold
            )
            DisplayWord(
                displayWord = session.guesses.joinToString(separator = ","),
                size = 20
            )

            KeyBoardKeys(session.keyboardKeys) {
                onGuess(it)
            }
        }
    }
}

private fun String.getHint(): String {
    return this.replace(this.first().toString(), "-")
        .replace(this.last().toString(), "-")
        .replaceFirst(this[this.length / 2].toString(), "-")
}

@Composable
fun LostComposable(session: GameSession) {
    CenteredColumn {
        Text("You Lost!!")

        DisplayWord(
            displayWord = session.currentWord,
            size = 40,
            fontWeight = FontWeight.ExtraBold
        )
        DisplayWord(
            displayWord = session.displayWord, size = 40,
            fontWeight = FontWeight.ExtraBold
        )
        DisplayWord(
            displayWord = session.guesses.joinToString(separator = ","),
            size = 20
        )
    }
}

@Composable
fun CenteredColumn(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable ColumnScope.() -> Unit
) = Column(modifier, verticalArrangement, horizontalAlignment, content)


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeyBoardKeys(keyboardKeys: MutableList<String>, onKeyPress: (String) -> Unit) {
    LazyVerticalGrid(
        cells = GridCells.Fixed(8),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.Center
    ) {

        items(keyboardKeys) { char ->
            KeyboardKey(
                char,
                onKeyPress = { onKeyPress(it) }
            )

        }
    }
}

@Preview(showBackground = true)
@Composable
fun DisplayWord(
    modifier: Modifier = Modifier,
    displayWord: String = "-a-m-r-e",
    size: Int = 50,
    fontWeight: FontWeight = FontWeight.Medium
) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = displayWord,
            style = TextStyle(fontSize = size.sp, letterSpacing = 8.sp, fontWeight = fontWeight)
        )
    }
}


@Composable
fun KeyboardKey(char: String = "a", onKeyPress: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .clickable { onKeyPress(char) }
            .size(50.dp)
            .padding(8.dp), shape = RoundedCornerShape(8.dp),
        elevation = 10.dp
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = char,
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            )
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    HangmanTheme {
        Greeting("Android")
    }
}
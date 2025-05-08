package com.example.snake.ui.screens

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.snake.R
import com.example.snake.model.Direction
import com.example.snake.model.FieldSize
import com.example.snake.viewmodel.GameViewModel
import com.example.snake.viewmodel.SettingsViewModel
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.xr.compose.testing.toDp

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun GameScreen(
    gameViewModel: GameViewModel,
    settingsViewModel: SettingsViewModel
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val gameState = gameViewModel.gameState
    val cellSize = 20.dp
    val fieldSize = settingsViewModel.settings.fieldSize
    var boxSizePx by remember { mutableStateOf(IntSize.Zero) }
    val cellSizePx = remember(boxSizePx, fieldSize) {
        if (fieldSize.width != 0 && fieldSize.height != 0)
            IntSize(
                width = boxSizePx.width / fieldSize.width,
                height = boxSizePx.height / fieldSize.height
            )
        else IntSize.Zero
    }

    // Lógica de reinicio del juego si los ajustes cambian
    LaunchedEffect(settingsViewModel.settings) {
        gameViewModel.startGame(settingsViewModel.settings)
    }
    if(isLandscape) {//landscape
        Box(
            modifier = Modifier.fillMaxSize()
        ){
            Image(//imatge fondo
                painter = painterResource(id = R.drawable.background), // ← cambia esto al nombre de tu imagen
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                //horizontalAlignment = Alignment.CenterHorizontally,
                //verticalArrangement = Arrangement.Top
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(fieldSize.width.toFloat() / fieldSize.height.toFloat())
                        .onGloballyPositioned { coordinates ->
                            boxSizePx = coordinates.size
                        }
                ) {
                    val backgroundRes = when (fieldSize) {
                        FieldSize.SMALL -> R.drawable.map_10x10_snake
                        FieldSize.MEDIUM -> R.drawable.map_15x15_snake
                        FieldSize.LARGE -> R.drawable.map_20x20_snake
                    }

                    val headPainter = painterResource(id = R.drawable.snake_head)
                    val bodyPainter = painterResource(id = R.drawable.snake_body)
                    val tailPainter = painterResource(id = R.drawable.snake_tail)
                    val foodPainter = painterResource(id = R.drawable.apple_snake)

                    Image(
                        painter = painterResource(id = backgroundRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )

                    // Dibuixar la menjar
                    Image(
                        painter = foodPainter,
                        contentDescription = stringResource(R.string.apple_description),
                        modifier = Modifier
                            .size(
                                width = cellSizePx.width.toDp(),
                                height = cellSizePx.height.toDp()
                            )
                            .offset {
                                IntOffset(
                                    x = cellSizePx.width * gameState.food.x,
                                    y = cellSizePx.height * gameState.food.y
                                )
                            }
                    )

                    // Dibuixar la serp
                    val snake = gameState.snake.body

                    snake.forEachIndexed { index, position ->
                        val painter: Painter
                        val rotation: Float

                        when (index) {
                            0 -> { // Head
                                painter = headPainter
                                // Aquí puedes personalizar los ángulos exactos como quieras
                                rotation = when (gameState.snake.direction) {
                                    Direction.UP -> 0f    // imagen ya está hacia arriba
                                    Direction.DOWN -> 180f
                                    Direction.LEFT -> 270f
                                    Direction.RIGHT -> 90f
                                }
                            }

                            snake.lastIndex -> { // Tail
                                painter = tailPainter
                                val prev = snake[index - 1]
                                rotation = when {
                                    prev.x < position.x -> 90f     // derecha
                                    prev.x > position.x -> 270f    // izquierda
                                    prev.y < position.y -> 180f    // abajo
                                    else -> 0f                     // arriba
                                }
                            }

                            else -> { // Body
                                painter = bodyPainter
                                val prev = snake[index - 1]
                                val next = snake[index + 1]
                                rotation = when {
                                    prev.x == next.x -> 180f  // vertical (imagen apunta abajo)
                                    prev.y == next.y -> 90f   // horizontal
                                    else -> 0f
                                }
                            }
                        }


                        Image(
                            painter = painter,
                            contentDescription = null,
                            modifier = Modifier
                                .size(
                                    width = cellSizePx.width.toDp(),
                                    height = cellSizePx.height.toDp()
                                )
                                .offset {
                                    IntOffset(
                                        x = cellSizePx.width * position.x,
                                        y = cellSizePx.height * position.y
                                    )
                                }
                                .graphicsLayer {
                                    rotationZ = rotation
                                }
                        )
                    }

                }
                // Botones en forma de cruz
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(onClick = { gameViewModel.changeDirection(Direction.UP) }) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = stringResource(R.string.direction_up))
                    }
                    Row(
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(onClick = { gameViewModel.changeDirection(Direction.LEFT) }) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = stringResource(R.string.direction_left))
                        }
                        Spacer(modifier = Modifier.width(24.dp))
                        IconButton(onClick = { gameViewModel.changeDirection(Direction.RIGHT) }) {
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = stringResource(R.string.direction_right))
                        }
                    }
                    IconButton(onClick = { gameViewModel.changeDirection(Direction.DOWN) }) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = stringResource(R.string.direction_down))
                    }
                }
                Text(stringResource(id = R.string.game_score, gameState.score))
                if (settingsViewModel.settings.timerEnabled && !gameState.isGameOver) {
                    Text(stringResource(id = R.string.game_timer, gameViewModel.remainingTime))
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (gameState.isGameOver) {
                    val message = if (gameState.isGameWon)
                        stringResource(id = R.string.game_win)
                    else
                        stringResource(id = R.string.game_over)


                    Text(message, color = if (gameState.isGameWon) Color.Green else Color.Red)
                    //enviar mail
                    val context = LocalContext.current
                    Button(onClick = {
                        val subject = context.getString(R.string.email_subject_win)
                        if (gameState.isGameWon) R.string.email_subject_win else R.string.email_subject_lose

                        val resultText = if (gameState.isGameOver)
                            context.getString(R.string.email_result_lose)
                        else
                            context.getString(R.string.email_result_win)

                        val message = context.getString(
                            R.string.email_body,
                            settingsViewModel.settings.username,
                            gameState.score,
                            resultText
                        )

                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "message/rfc822"
                            putExtra(Intent.EXTRA_EMAIL, arrayOf(settingsViewModel.settings.recipientEmail))
                            putExtra(Intent.EXTRA_SUBJECT, subject)
                            putExtra(Intent.EXTRA_TEXT, message)
                        }

                        try {
                            context.startActivity(Intent.createChooser(intent, context.getString(R.string.email_chooser_title)))
                        } catch (e: Exception) {
                            Toast.makeText(context, context.getString(R.string.email_no_app), Toast.LENGTH_SHORT).show()
                        }

                    }) {
                        Text(stringResource(R.string.settings_send))
                    }
                }

            }
        }
    }else{//portrait
        Box(
            modifier = Modifier.fillMaxSize()
        ){
            Image(//imatge fondo
                painter = painterResource(id = R.drawable.background), // ← cambia esto al nombre de tu imagen
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(stringResource(id = R.string.game_score, gameState.score))
                if (settingsViewModel.settings.timerEnabled && !gameState.isGameOver) {
                    Text(stringResource(id = R.string.game_timer, gameViewModel.remainingTime))
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(fieldSize.width.toFloat() / fieldSize.height.toFloat())
                        .onGloballyPositioned { coordinates ->
                            boxSizePx = coordinates.size
                        }
                ) {
                    val backgroundRes = when (fieldSize) {
                        FieldSize.SMALL -> R.drawable.map_10x10_snake
                        FieldSize.MEDIUM -> R.drawable.map_15x15_snake
                        FieldSize.LARGE -> R.drawable.map_20x20_snake
                    }

                    val headPainter = painterResource(id = R.drawable.snake_head)
                    val bodyPainter = painterResource(id = R.drawable.snake_body)
                    val tailPainter = painterResource(id = R.drawable.snake_tail)
                    val foodPainter = painterResource(id = R.drawable.apple_snake)

                    Image(
                        painter = painterResource(id = backgroundRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Dibuixar la menjar
                    Image(
                        painter = foodPainter,
                        contentDescription = stringResource(R.string.apple_description),
                        modifier = Modifier
                            .size(
                                width = cellSizePx.width.toDp(),
                                height = cellSizePx.height.toDp()
                            )
                            .offset {
                                IntOffset(
                                    x = cellSizePx.width * gameState.food.x,
                                    y = cellSizePx.height * gameState.food.y
                                )
                            }
                    )

                    // Dibuixar la serp
                    val snake = gameState.snake.body

                    snake.forEachIndexed { index, position ->
                        val painter: Painter
                        val rotation: Float

                        when (index) {
                            0 -> { // Head
                                painter = headPainter
                                // Aquí puedes personalizar los ángulos exactos como quieras
                                rotation = when (gameState.snake.direction) {
                                    Direction.UP -> 0f    // imagen ya está hacia arriba
                                    Direction.DOWN -> 180f
                                    Direction.LEFT -> 270f
                                    Direction.RIGHT -> 90f
                                }
                            }

                            snake.lastIndex -> { // Tail
                                painter = tailPainter
                                val prev = snake[index - 1]
                                rotation = when {
                                    prev.x < position.x -> 90f     // derecha
                                    prev.x > position.x -> 270f    // izquierda
                                    prev.y < position.y -> 180f    // abajo
                                    else -> 0f                     // arriba
                                }
                            }

                            else -> { // Body
                                painter = bodyPainter
                                val prev = snake[index - 1]
                                val next = snake[index + 1]
                                rotation = when {
                                    prev.x == next.x -> 180f  // vertical (imagen apunta abajo)
                                    prev.y == next.y -> 90f   // horizontal
                                    else -> 0f
                                }
                            }
                        }


                        Image(
                            painter = painter,
                            contentDescription = null,
                            modifier = Modifier
                                .size(
                                    width = cellSizePx.width.toDp(),
                                    height = cellSizePx.height.toDp()
                                )
                                .offset {
                                    IntOffset(
                                        x = cellSizePx.width * position.x,
                                        y = cellSizePx.height * position.y
                                    )
                                }
                                .graphicsLayer {
                                    rotationZ = rotation
                                }
                        )
                    }

                }

                if (gameState.isGameOver) {
                    val message = if (gameState.isGameWon)
                        stringResource(id = R.string.game_win)
                    else
                        stringResource(id = R.string.game_over)

                    Text(message, color = if (gameState.isGameWon) Color.Green else Color.Red)


                    //enviar mail
                    val context = LocalContext.current
                    Button(onClick = {
                        val subject: String = context.getString(R.string.email_subject_win)
                        if (gameState.isGameWon) R.string.email_subject_win else R.string.email_subject_lose

                        val resultText = if (gameState.isGameOver)
                            context.getString(R.string.email_result_lose)
                        else
                            context.getString(R.string.email_result_win)

                        val message = context.getString(
                            R.string.email_body,
                            settingsViewModel.settings.username,
                            gameState.score,
                            resultText
                        )

                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "message/rfc822"
                            putExtra(Intent.EXTRA_EMAIL, arrayOf(settingsViewModel.settings.recipientEmail))
                            putExtra(Intent.EXTRA_SUBJECT, subject)
                            putExtra(Intent.EXTRA_TEXT, message)
                        }

                        try {
                            context.startActivity(Intent.createChooser(intent, context.getString(R.string.email_chooser_title)))
                        } catch (e: Exception) {
                            Toast.makeText(context, context.getString(R.string.email_no_app), Toast.LENGTH_SHORT).show()
                        }

                    }) {
                        Text(stringResource(R.string.settings_send))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botones en forma de cruz
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(onClick = { gameViewModel.changeDirection(Direction.UP) }) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = stringResource(R.string.direction_up))
                    }
                    Row(
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(onClick = { gameViewModel.changeDirection(Direction.LEFT) }) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = stringResource(R.string.direction_left))
                        }
                        Spacer(modifier = Modifier.width(24.dp))
                        IconButton(onClick = { gameViewModel.changeDirection(Direction.RIGHT) }) {
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = stringResource(R.string.direction_right))
                        }
                    }
                    IconButton(onClick = { gameViewModel.changeDirection(Direction.DOWN) }) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = stringResource(R.string.direction_down))
                    }
                }
            }
        }
    }
}

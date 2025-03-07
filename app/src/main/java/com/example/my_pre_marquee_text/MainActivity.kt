package com.example.my_pre_marquee_text

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Main()
        }
    }
}

enum class Config {
    Text, Size, Speed, Color
}

@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main() {
    val context = LocalContext.current
    val sharePreferences = context.getSharedPreferences("marquee_config", Context.MODE_PRIVATE)
    val textColors = listOf(
        Color.Black, Color(0xffF50007), Color(0xFFB3BD63), Color(0xffC9A5F5), Color(0xffF56C9F)
    )

    val textPos = remember { Animatable(0f) }
    var textWidth by remember { mutableFloatStateOf(0f) }
    var screenWidth by remember { mutableFloatStateOf(0f) }
    var textInitial by rememberSaveable { mutableStateOf(true) }
    var currentTime by rememberSaveable { mutableStateOf("") }

    var openSheet by remember { mutableStateOf(false) }
    var sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var textFieldValue by rememberSaveable {
        mutableStateOf(
            sharePreferences.getString(
                Config.Text.name, ""
            ) ?: ""
        )
    }
    var textFontSizeSlider by rememberSaveable {
        mutableFloatStateOf(
            sharePreferences.getFloat(
                Config.Size.name, 20f
            )
        )
    }
    var textMovingSpeedSlider by rememberSaveable {
        mutableFloatStateOf(
            sharePreferences.getFloat(
                Config.Speed.name, 5000f
            )
        )
    }
    var textColor by rememberSaveable {
        mutableIntStateOf(
            sharePreferences.getInt(
                Config.Color.name, 0
            )
        )
    }

    LaunchedEffect(Unit) {
        while (textInitial) {
            currentTime =
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            delay(1000L)
        }
    }

    LaunchedEffect(textMovingSpeedSlider) {
        textPos.snapTo(-textWidth)
        while (isActive) {
            val currentSpeed = textMovingSpeedSlider.toInt()
            textPos.animateTo(
                targetValue = screenWidth, animationSpec = tween(
                    durationMillis = 16000 - currentSpeed, easing = LinearEasing
                )
            )
            textPos.snapTo(-textWidth)
        }
    }

    LaunchedEffect(textFieldValue, textFontSizeSlider, textMovingSpeedSlider, textColor) {
        sharePreferences.edit().apply {
            putString(Config.Text.name, textFieldValue)
            putFloat(Config.Size.name, textFontSizeSlider)
            putFloat(Config.Speed.name, textMovingSpeedSlider)
            putInt(Config.Color.name, textColor)
        }.apply()
    }

    LaunchedEffect(textFieldValue) {
        if (textFieldValue.isNotEmpty()) {
            textInitial = false
        } else {
            textInitial = true
        }
    }


    if (openSheet) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { openSheet = false }) {
            LazyColumn(
                Modifier
                    .padding(horizontal = 20.dp)
                    .height(350.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    OutlinedTextField(value = textFieldValue,
                        singleLine = true,
                        placeholder = { Text("Marquee Text") },
                        label = { Text("Marquee Text") },
                        modifier = Modifier.fillMaxWidth(),
                        onValueChange = {
                            textInitial = false
                            textFieldValue = it
                        })
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Font Size")
                        Spacer(Modifier.width(20.dp))
                        Slider(
                            value = textFontSizeSlider, onValueChange = {
                                textFontSizeSlider = it
                            }, valueRange = 20f..100f
                        )
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Moving Speed")
                        Spacer(Modifier.width(20.dp))
                        Slider(
                            value = textMovingSpeedSlider, onValueChange = {
                                textMovingSpeedSlider = it
                            }, valueRange = 500f..15000f
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        itemsIndexed(textColors) { index, item ->
                            Box(modifier = Modifier
                                .padding(end = 10.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    if (textColor == index) 2.dp else 0.dp,
                                    Color(0xff7DAEF5),
                                    RoundedCornerShape(10.dp)
                                )
                                .background(item)
                                .size(70.dp)
                                .clickable {
                                    textColor = index
                                }) {
                                Column(Modifier.align(Alignment.BottomEnd)) {
                                    AnimatedVisibility(textColor == index) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "check",
                                            modifier = Modifier
                                                .padding(5.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xff7DAEF5))
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = {
                            sharePreferences.edit().clear().apply()
                            textInitial = true
                            textFieldValue = ""
                            textFontSizeSlider = 20f
                            textMovingSpeedSlider = 5000f
                            textColor = 0
                        }, modifier = Modifier.fillMaxWidth(), colors = ButtonColors(
                            Color.Red, Color.White, Color.Red, Color.White
                        )
                    ) { Text("Reset") }
                }
            }

        }
    }
    Box(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .onGloballyPositioned {
                screenWidth = it.size.width.toFloat()
            }) {
        IconButton(onClick = {
            openSheet = true
        }) {
            Icon(Icons.Default.Settings, contentDescription = "Open Setting Bottom Sheet")
        }
        Text(if (textInitial) currentTime else textFieldValue,
            fontWeight = FontWeight.Bold,
            fontSize = textFontSizeSlider.sp,
            color = textColors.get(textColor),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(with(LocalDensity.current) { textPos.value.toDp() }, 0.dp)
                .onGloballyPositioned {
                    textWidth = it.size.width.toFloat()
                })
    }
}
package com.example.auratrackr.features.onboarding.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val DarkPurple = Color(0xFF2C2B3C)
private val LightYellow = Color(0xFFF7F6CF)
private val LightBlue = Color(0xFFD9F1F2)
private val OffWhite = Color(0xFFF8F8F8)

@OptIn(ExperimentalPagerApi::class)
@Composable
fun PersonalInfoScreen(
    onFinished: (Int, Int) -> Unit,
    onBack: () -> Unit
) {
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    var weightInKg by remember { mutableStateOf(70) }
    var heightInCm by remember { mutableStateOf(170) }

    Scaffold(
        containerColor = OffWhite,
        bottomBar = {
            OnboardingBottomNav(
                currentPage = pagerState.currentPage,
                onBack = {
                    if (pagerState.currentPage > 0) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    } else {
                        onBack()
                    }
                },
                onNext = {
                    if (pagerState.currentPage < 1) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onFinished(weightInKg, heightInCm)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(top = 32.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress Indicator
            Row {
                StepIndicator(isActive = pagerState.currentPage == 0)
                Spacer(modifier = Modifier.width(8.dp))
                StepIndicator(isActive = pagerState.currentPage == 1)
            }

            Spacer(modifier = Modifier.height(48.dp))

            HorizontalPager(
                count = 2,
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                userScrollEnabled = false // Control navigation with buttons
            ) { page ->
                when (page) {
                    0 -> WeightInputStep(
                        initialWeight = weightInKg,
                        onWeightChange = { weightInKg = it }
                    )
                    1 -> HeightInputStep(
                        initialHeight = heightInCm,
                        onHeightChange = { heightInCm = it }
                    )
                }
            }
        }
    }
}

@Composable
fun WeightInputStep(initialWeight: Int, onWeightChange: (Int) -> Unit) {
    var selectedUnit by remember { mutableStateOf("kg") }
    val isKg = selectedUnit == "kg"

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("What is your weight?", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DarkPurple)
        Spacer(modifier = Modifier.height(32.dp))
        UnitSelector(
            units = listOf("lb", "kg"),
            selectedUnit = selectedUnit,
            onUnitSelected = { selectedUnit = it }
        )
        Spacer(modifier = Modifier.height(32.dp))
        RulerCard(
            color = LightYellow,
            value = if (isKg) initialWeight else (initialWeight * 2.20462).roundToInt(),
            range = if (isKg) 30..200 else 66..440,
            onValueChange = {
                onWeightChange(if (isKg) it else (it / 2.20462).roundToInt())
            },
            unit = selectedUnit
        )
    }
}

@Composable
fun HeightInputStep(initialHeight: Int, onHeightChange: (Int) -> Unit) {
    var selectedUnit by remember { mutableStateOf("cm") }
    val isCm = selectedUnit == "cm"

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("What is your height?", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DarkPurple)
        Spacer(modifier = Modifier.height(32.dp))
        UnitSelector(
            units = listOf("inches", "cm"),
            selectedUnit = selectedUnit,
            onUnitSelected = { selectedUnit = it }
        )
        Spacer(modifier = Modifier.height(32.dp))
        RulerCard(
            color = LightBlue,
            value = if (isCm) initialHeight else (initialHeight / 2.54).roundToInt(),
            range = if (isCm) 120..220 else 47..87,
            onValueChange = {
                onHeightChange(if (isCm) it else (it * 2.54).roundToInt())
            },
            unit = selectedUnit
        )
    }
}

@Composable
fun RulerCard(
    color: Color,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    unit: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$value",
                fontSize = 80.sp,
                fontWeight = FontWeight.Bold,
                color = DarkPurple,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            RulerSlider(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                range = range,
                onValueChange = onValueChange,
                unit = unit
            )
        }
    }
}

@Composable
fun RulerSlider(
    modifier: Modifier = Modifier,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    unit: String
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var rulerWidth by remember { mutableStateOf(0) }
    val tickSpacing = 12.dp
    val totalItems = range.last - range.first + 1

    LaunchedEffect(Unit) {
        val initialIndex = value - range.first
        listState.scrollToItem(initialIndex, -rulerWidth / 2 + (tickSpacing.value / 2).toInt())
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val centerOffset = listState.firstVisibleItemScrollOffset
            val centerIndex = listState.firstVisibleItemIndex + (centerOffset / tickSpacing.value).roundToInt()
            onValueChange(range.first + centerIndex)
            coroutineScope.launch {
                listState.animateScrollToItem(centerIndex, -rulerWidth / 2 + (tickSpacing.value / 2).toInt())
            }
        }
    }

    Box(
        modifier = modifier
            .height(100.dp)
            .onSizeChanged { rulerWidth = it.width },
        contentAlignment = Alignment.Center
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = (rulerWidth / 2).dp)
        ) {
            items(totalItems) { index ->
                val currentValue = range.first + index
                val isMajorTick = currentValue % 10 == 0
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(tickSpacing)
                ) {
                    Canvas(modifier = Modifier.fillMaxHeight(0.6f)) {
                        drawLine(
                            color = DarkPurple.copy(alpha = 0.5f),
                            start = Offset(x = size.width / 2, y = 0f),
                            end = Offset(x = size.width / 2, y = if (isMajorTick) size.height else size.height / 2),
                            strokeWidth = 2f,
                            cap = StrokeCap.Round
                        )
                    }
                    if (isMajorTick) {
                        Text(
                            text = "$currentValue",
                            color = DarkPurple.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawLine(
                color = DarkPurple,
                start = Offset(x = center.x, y = center.y - 50),
                end = Offset(x = center.x, y = center.y + 10),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = DarkPurple,
                radius = 8f,
                center = Offset(x = center.x, y = center.y + 20)
            )
        }
        Text(
            text = unit,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
            color = DarkPurple,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun UnitSelector(
    units: List<String>,
    selectedUnit: String,
    onUnitSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.White)
            .padding(4.dp)
    ) {
        units.forEach { unit ->
            val isSelected = unit == selectedUnit
            val backgroundColor by animateColorAsState(if (isSelected) DarkPurple else Color.Transparent, label = "UnitSelectorBackground")
            val contentColor by animateColorAsState(if (isSelected) Color.White else DarkPurple, label = "UnitSelectorContent")

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .clickable { onUnitSelected(unit) }
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = unit, color = contentColor, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun OnboardingBottomNav(
    currentPage: Int,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val buttonText = if (currentPage == 1) "Start Now" else "Next"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkPurple)
        }

        Button(
            onClick = onNext,
            modifier = Modifier.height(56.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = DarkPurple),
            contentPadding = PaddingValues(horizontal = 32.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(buttonText, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White.copy(0.5f))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White.copy(0.3f))
            }
        }
    }
}

@Composable
fun StepIndicator(isActive: Boolean) {
    val width by animateDpAsState(targetValue = if (isActive) 32.dp else 16.dp, label = "StepIndicatorWidth")
    val color by animateColorAsState(targetValue = if (isActive) DarkPurple else Color.LightGray, label = "StepIndicatorColor")
    Box(
        modifier = Modifier
            .height(4.dp)
            .width(width)
            .clip(CircleShape)
            .background(color)
    )
}

@Preview(showBackground = true)
@Composable
fun PersonalInfoScreenPreview() {
    PersonalInfoScreen(onFinished = { _, _ -> }, onBack = {})
}

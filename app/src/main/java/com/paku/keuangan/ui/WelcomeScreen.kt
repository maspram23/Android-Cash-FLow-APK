package com.paku.keuangan.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paku.keuangan.R
import com.paku.keuangan.i18n.LocalStrings
import com.paku.keuangan.ui.theme.Coral
import com.paku.keuangan.ui.theme.CoralDeep
import com.paku.keuangan.ui.theme.CoralSoft
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val s = LocalStrings.current
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.82f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 260f),
        label = "splashScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(420),
        label = "splashAlpha"
    )
    val infinite = rememberInfiniteTransition(label = "splashDots")
    val secondarySoft = MaterialTheme.colorScheme.secondaryContainer

    LaunchedEffect(Unit) {
        visible = true
        delay(1150)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                color = CoralSoft.copy(alpha = 0.55f),
                radius = size.width * 0.55f,
                center = Offset(size.width * 0.92f, size.height * 0.08f)
            )
            drawCircle(
                color = secondarySoft.copy(alpha = 0.4f),
                radius = size.width * 0.4f,
                center = Offset(size.width * 0.05f, size.height * 0.94f)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alpha)
                .scale(scale)
        ) {
            Surface(
                shape = RoundedCornerShape(38.dp),
                color = Color.White,
                shadowElevation = 18.dp
            ) {
                Image(
                    painter = painterResource(R.drawable.paku_logo),
                    contentDescription = s.logoDescription,
                    modifier = Modifier
                        .size(142.dp)
                        .padding(18.dp)
                )
            }
            Spacer(Modifier.height(22.dp))
            Text(
                text = s.appName,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = s.appTagline,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(40.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                repeat(3) { index ->
                    val dotAlpha by infinite.animateFloat(
                        initialValue = 0.22f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(650, delayMillis = index * 130),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot$index"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .alpha(dotAlpha)
                            .background(Coral, CircleShape)
                    )
                }
            }
        }
        Text(
            text = s.splashTagline,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 42.dp)
        )
    }
}

@Composable
fun OnboardingScreen(
    onFinish: (name: String, withDemo: Boolean) -> Unit
) {
    val s = LocalStrings.current
    var name by remember { mutableStateOf("") }
    var withDemo by remember { mutableStateOf(true) }
    var entered by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val secondarySoft = MaterialTheme.colorScheme.secondaryContainer

    LaunchedEffect(Unit) {
        delay(80)
        entered = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                color = CoralSoft.copy(alpha = 0.6f),
                radius = size.width * 0.62f,
                center = Offset(size.width * 1.02f, -size.height * 0.02f)
            )
            drawCircle(
                color = secondarySoft.copy(alpha = 0.45f),
                radius = size.width * 0.42f,
                center = Offset(-size.width * 0.1f, size.height * 0.9f)
            )
        }

        AnimatedVisibility(
            visible = entered,
            enter = fadeIn(tween(500)) + slideInVertically(
                animationSpec = tween(500),
                initialOffsetY = { it / 22 }
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = Color.White,
                    shadowElevation = 12.dp
                ) {
                    Image(
                        painter = painterResource(R.drawable.paku_logo),
                        contentDescription = s.logoDescription,
                        modifier = Modifier
                            .size(112.dp)
                            .padding(14.dp)
                    )
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    text = s.onboardingTitle,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = s.onboardingBody,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(Modifier.height(26.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = s.onboardingNameQuestion,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            if (name.isEmpty()) {
                                Text(
                                    text = s.defaultUserName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            BasicTextField(
                                value = name,
                                onValueChange = { if (it.length <= 24) name = it },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { focusManager.clearFocus() }
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(Modifier.height(22.dp))

                Text(
                    text = s.onboardingStartQuestion,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(Modifier.height(10.dp))
                ChoiceCard(
                    selected = withDemo,
                    emoji = "✨",
                    title = s.onboardingDemoTitle,
                    subtitle = s.onboardingDemoSubtitle,
                    badge = s.onboardingDemoBadge,
                    onClick = { withDemo = true }
                )
                Spacer(Modifier.height(9.dp))
                ChoiceCard(
                    selected = !withDemo,
                    emoji = "🌱",
                    title = s.onboardingEmptyTitle,
                    subtitle = s.onboardingEmptySubtitle,
                    onClick = { withDemo = false }
                )

                Spacer(Modifier.height(26.dp))
                GradientButton(
                    text = s.onboardingCta,
                    onClick = {
                        focusManager.clearFocus()
                        onFinish(name, withDemo)
                    },
                    icon = PakuIcons.ChevronRight
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = s.onboardingPrivacy,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ChoiceCard(
    selected: Boolean,
    emoji: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    badge: String? = null
) {
    val accent = MaterialTheme.colorScheme.primary
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) accent.copy(alpha = 0.08f)
        else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) accent.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = 0.09f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 20.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (badge != null) {
                        Spacer(Modifier.width(7.dp))
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .border(
                        width = if (selected) 6.dp else 1.5.dp,
                        color = if (selected) accent else MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    )
                    .padding(3.dp)
                    .background(
                        color = if (selected) Color.White else Color.Transparent,
                        shape = CircleShape
                    )
            )
        }
    }
}

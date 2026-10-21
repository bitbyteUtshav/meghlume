package com.meghlume.browser.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meghlume.browser.engine.TabState
import java.net.URI

@Composable
fun SafariBottomBar(
    tabState: TabState?,
    tabsCount: Int,
    isCollapsed: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateForward: () -> Unit,
    onReloadOrStop: () -> Unit,
    onSubmitUrl: (String) -> Unit,
    onToggleTabSwitcher: () -> Unit,
    onOpenMenu: () -> Unit,
    onOpenQrScanner: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var inputQuery by remember(tabState?.currentUrl) { 
        mutableStateOf(tabState?.currentUrl ?: "") 
    }
    BackHandler(enabled = isEditing) { isEditing = false }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val barHeight by animateDpAsState(
        targetValue = if (isCollapsed && !isEditing) 44.dp else 56.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "barHeight"
    )

    val surfaceColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
    val borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Optional Loading Progress Bar atop the pill
        if (tabState?.isLoading == true) {
            LinearProgressIndicator(
                progress = { tabState.progress / 100f },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(2.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.Transparent
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Glassmorphic Floating Pill Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = Color.Black.copy(alpha = 0.25f),
                    ambientColor = Color.Black.copy(alpha = 0.1f)
                )
                .clip(RoundedCornerShape(28.dp))
                .background(surfaceColor)
                .border(1.dp, borderColor, RoundedCornerShape(28.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (!isEditing) {
                        isEditing = true
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                IconButton(
                    onClick = onNavigateBack,
                    enabled = tabState?.canGoBack == true,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = if (tabState?.canGoBack == true) 
                            MaterialTheme.colorScheme.onSurface 
                        else 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Forward Button
                IconButton(
                    onClick = onNavigateForward,
                    enabled = tabState?.canGoForward == true,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowForwardIos,
                        contentDescription = "Forward",
                        tint = if (tabState?.canGoForward == true) 
                            MaterialTheme.colorScheme.onSurface 
                        else 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Center Address & Search Field
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isEditing) {
                        BasicTextField(
                            value = inputQuery,
                            onValueChange = { inputQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Uri,
                                imeAction = ImeAction.Go
                            ),
                            keyboardActions = KeyboardActions(
                                onGo = {
                                    isEditing = false
                                    focusManager.clearFocus()
                                    onSubmitUrl(inputQuery)
                                }
                            ),
                            singleLine = true
                        )
                        LaunchedEffect(Unit) {
                            focusRequester.requestFocus()
                        }
                    } else {
                        // Display Host / Domain with Lock Icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (tabState?.isSecure == true) {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = "Secure",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                            }
                            Text(
                                text = extractDisplayHost(tabState?.currentUrl ?: ""),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.2.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // QR scanner (visible while typing an address)
                if (isEditing) {
                    IconButton(
                        onClick = {
                            isEditing = false
                            onOpenQrScanner()
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.QrCodeScanner,
                            contentDescription = "Scan QR",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Reload or Stop
                IconButton(
                    onClick = onReloadOrStop,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (tabState?.isLoading == true) 
                            Icons.Rounded.Close 
                        else 
                            Icons.Rounded.Refresh,
                        contentDescription = "Reload",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Tab Switcher Button with Badge Count
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(onClick = onToggleTabSwitcher),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .border(
                                width = 1.8.dp,
                                color = MaterialTheme.colorScheme.onSurface,
                                shape = RoundedCornerShape(6.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabsCount.toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

private fun extractDisplayHost(url: String): String {
    if (url.isBlank() || url == "about:blank") return "Search or enter website"
    return try {
        val uri = URI(url)
        val host = uri.host ?: url
        host.removePrefix("www.")
    } catch (e: Exception) {
        url
    }
}

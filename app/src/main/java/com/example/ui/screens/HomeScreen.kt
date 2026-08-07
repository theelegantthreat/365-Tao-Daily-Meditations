package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.audio.BackgroundSoundManager
import com.example.data.model.TaoMeditation
import com.example.data.model.UserSettings
import com.example.ui.theme.*
import com.example.ui.viewmodel.TaoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: TaoViewModel) {
    val activeMeditation by viewModel.activeMeditation.collectAsState()
    val allMeditations by viewModel.allMeditations.collectAsState()
    val settings by viewModel.userSettings.collectAsState()
    val isLoading by viewModel.isLoadingMeditation.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val completedCount = remember(allMeditations) { allMeditations.count { it.isCompleted } }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var showDayPickerSheet by remember { mutableStateOf(false) }
    var showCustomizerSheet by remember { mutableStateOf(false) }
    var showJournalSection by remember { mutableStateOf(false) }

    // File pickers
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.updateBackgroundStyle("CUSTOM_IMAGE", it.toString())
        }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.updateSoundStyle("CUSTOM_MP3", it.toString())
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Dynamic Background Render with Clean Minimalism soft watermarks
        RenderBackground(settings = settings)

        // 2. Main Scaffold Layout
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    title = {
                        Text(
                            text = "365 Tao",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Medium,
                                letterSpacing = (-0.5).sp,
                                fontFamily = FontFamily.Serif,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { showDayPickerSheet = true },
                            modifier = Modifier.testTag("menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Browse 365 days of wisdom",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.forceRefreshMeditation() },
                            modifier = Modifier.testTag("refresh_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Regenerate Meditation via AI",
                                tint = ZenPrimary
                            )
                        }
                    }
                )
            },
            bottomBar = {
                // Customized Minimal Navigation Bar matching design HTML exactly
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .drawBehind {
                            // Subtle top border line
                            drawLine(
                                color = ZenBorder,
                                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                ) {
                    // Today Item (Active Home state)
                    NavigationBarItem(
                        selected = !showJournalSection,
                        onClick = { showJournalSection = false },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Today",
                                tint = if (!showJournalSection) Color(0xFF141F07) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        },
                        label = {
                            Text(
                                "Today",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = if (!showJournalSection) Color(0xFF141F07) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = ZenActivePill
                        )
                    )

                    // Archive Item
                    NavigationBarItem(
                        selected = false,
                        onClick = { showDayPickerSheet = true },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = "Archive",
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        },
                        label = {
                            Text(
                                "Archive",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                            )
                        }
                    )

                    // Journal Item
                    NavigationBarItem(
                        selected = showJournalSection,
                        onClick = { showJournalSection = !showJournalSection },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = "Journal",
                                tint = if (showJournalSection) Color(0xFF141F07) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        },
                        label = {
                            Text(
                                "Journal",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = if (showJournalSection) Color(0xFF141F07) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = ZenActivePill
                        )
                    )

                    // Settings/Visual Customizer Item
                    NavigationBarItem(
                        selected = showCustomizerSheet,
                        onClick = { showCustomizerSheet = true },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        },
                        label = {
                            Text(
                                "Settings",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                            )
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = ZenPrimary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Aligning with the Tao...",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }
                } else if (activeMeditation != null) {
                    val meditation = activeMeditation!!

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp)
                    ) {
                        // 1. Progress Indicator Header (Matches HTML perfectly!)
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text(
                                        text = String.format("%03d", meditation.day),
                                        style = MaterialTheme.typography.displayMedium.copy(
                                            fontSize = 48.sp,
                                            fontFamily = FontFamily.Serif,
                                            fontStyle = FontStyle.Italic,
                                            fontWeight = FontWeight.Light,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                                            lineHeight = 48.sp
                                        )
                                    )
                                    Text(
                                        text = "/ 365",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.5.sp,
                                            color = ZenSecondary,
                                            fontSize = 14.sp
                                        ),
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                // Custom Progress Bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(ZenBorder)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(meditation.day / 365f)
                                            .fillMaxHeight()
                                            .background(ZenPrimary)
                                    )
                                }
                            }
                        }

                        // NEW: 365-day Cycle Completion Progress Tracker
                        item {
                            CycleCompletionTrackerCard(completedCount = completedCount)
                        }

                        // 2. The Meditation (Verse)
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(48.dp)
                                        .height(1.dp)
                                        .background(ZenPrimary.copy(alpha = 0.3f))
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "“${meditation.verse}”",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = FontFamily.Serif,
                                        fontStyle = FontStyle.Italic,
                                        color = Color(0xFF31302B),
                                        lineHeight = 32.sp,
                                        fontSize = 22.sp
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // 3. Sage Commentary Card
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF2EFE5)
                                ),
                                shape = RoundedCornerShape(28.dp),
                                border = BorderStroke(1.dp, ZenBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoStories,
                                            contentDescription = null,
                                            tint = ZenSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "COMMENTARY",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.5.sp,
                                                color = ZenSecondary
                                            )
                                        )
                                    }
                                    Text(
                                        text = meditation.commentary,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = Color(0xFF48473E),
                                            lineHeight = 24.sp
                                        )
                                    )
                                }
                            }
                        }

                        // 4. Compact Control / Quick Customizer Bar (Matches Customization bar)
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White.copy(alpha = 0.5f))
                                    .border(1.dp, ZenBorder, RoundedCornerShape(20.dp))
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 1. Audio Button
                                    IconButton(
                                        onClick = { showCustomizerSheet = true },
                                        modifier = Modifier.weight(1f).testTag("quick_audio_button")
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = if (settings.soundType == "SILENT") {
                                                    Icons.Outlined.VolumeMute
                                                } else if (settings.isAmbientPlaying) {
                                                    Icons.Default.VolumeUp
                                                } else {
                                                    Icons.Default.VolumeOff
                                                },
                                                contentDescription = "Audio settings",
                                                tint = ZenPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = if (settings.soundType == "SILENT") {
                                                    "AUDIO"
                                                } else if (settings.isAmbientPlaying) {
                                                    "PLAYING"
                                                } else {
                                                    "PAUSED"
                                                },
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 8.sp,
                                                    color = ZenSecondary
                                                )
                                            )
                                        }
                                    }

                                    VerticalDivider(modifier = Modifier.height(24.dp), color = ZenBorder)

                                    // 2. Favorite Toggle
                                    IconButton(
                                        onClick = { viewModel.toggleFavorite() },
                                        modifier = Modifier.weight(1f).testTag("favorite_button")
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = if (meditation.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                                contentDescription = "Favorite",
                                                tint = if (meditation.isFavorite) ZenTertiary else ZenPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = "FAVORITE",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 8.sp,
                                                    color = ZenSecondary
                                                )
                                            )
                                        }
                                    }

                                    VerticalDivider(modifier = Modifier.height(24.dp), color = ZenBorder)

                                    // 3. Complete Toggle
                                    IconButton(
                                        onClick = { viewModel.toggleCompleted() },
                                        modifier = Modifier.weight(1f).testTag("completed_button")
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = if (meditation.isCompleted) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                                                contentDescription = "Mark Complete",
                                                tint = ZenPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = if (meditation.isCompleted) "DONE" else "MEDITATE",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 8.sp,
                                                    color = ZenSecondary
                                                )
                                            )
                                        }
                                    }

                                    VerticalDivider(modifier = Modifier.height(24.dp), color = ZenBorder)

                                    // 4. Reflect / Journal Toggle
                                    IconButton(
                                        onClick = { showJournalSection = !showJournalSection },
                                        modifier = Modifier.weight(1f).testTag("journal_toggle_button")
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = if (showJournalSection) Icons.Default.EditNote else Icons.Outlined.EditNote,
                                                contentDescription = "Journal reflections",
                                                tint = ZenPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = "JOURNAL",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 8.sp,
                                                    color = ZenSecondary
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 5. User Note / Journal Section (collapsible)
                        if (showJournalSection) {
                            item {
                                var noteText by remember(meditation.day) { mutableStateOf(meditation.userNote) }

                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFF2EFE5)
                                    ),
                                    shape = RoundedCornerShape(28.dp),
                                    border = BorderStroke(1.dp, ZenBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp)
                                    ) {
                                        Text(
                                            text = "My Personal Reflections",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = ZenPrimary
                                            ),
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )

                                        OutlinedTextField(
                                            value = noteText,
                                            onValueChange = { noteText = it },
                                            placeholder = {
                                                Text(
                                                    "Pen down your insights, feelings, or how you will practice Wu Wei today...",
                                                    color = ZenSecondary.copy(alpha = 0.6f),
                                                    fontSize = 14.sp
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(110.dp)
                                                .testTag("journal_input"),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                                                focusedBorderColor = ZenPrimary,
                                                unfocusedBorderColor = ZenBorder,
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White.copy(alpha = 0.6f)
                                            ),
                                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                            keyboardActions = KeyboardActions(onDone = {
                                                viewModel.saveUserNote(noteText)
                                                focusManager.clearFocus()
                                            })
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Button(
                                            onClick = {
                                                viewModel.saveUserNote(noteText)
                                                focusManager.clearFocus()
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = ZenPrimary,
                                                contentColor = ZenOnPrimary
                                            ),
                                            shape = RoundedCornerShape(50),
                                            modifier = Modifier
                                                .align(Alignment.End)
                                                .testTag("save_journal_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Save,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Save Reflection", fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Slide-up Day Picker Sheet
        if (showDayPickerSheet) {
            ModalBottomSheet(
                onDismissRequest = { showDayPickerSheet = false },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = "Browse 365 Days of Wisdom",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxHeight(0.6f)
                            .padding(bottom = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allMeditations) { med ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (med.day == activeMeditation?.day)
                                            ZenActivePill
                                        else
                                            Color.Transparent
                                    )
                                    .clickable {
                                        viewModel.selectDay(med.day)
                                        showDayPickerSheet = false
                                    }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = med.formattedDay,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (med.day == activeMeditation?.day)
                                            Color(0xFF141F07)
                                        else
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = med.title,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (med.day == activeMeditation?.day)
                                            FontWeight.Bold
                                        else
                                            FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                if (med.isCompleted) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Completed",
                                        tint = ZenPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                if (med.isFavorite) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Favorited",
                                        tint = ZenTertiary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Slide-up Customization Settings Sheet
        if (showCustomizerSheet) {
            ModalBottomSheet(
                onDismissRequest = { showCustomizerSheet = false },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Customize Sanctuary",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // IMAGE BACKGROUND SECTION
                    Text(
                        text = "Visual Atmosphere",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BackgroundOptionCard(
                            label = "Misty Mountain",
                            selected = settings.bgType == "MISTY_MOUNTAINS",
                            imageResId = R.drawable.img_bg_mountains_1782754451099,
                            onClick = { viewModel.updateBackgroundStyle("MISTY_MOUNTAINS", null) }
                        )
                        BackgroundOptionCard(
                            label = "Bamboo Grove",
                            selected = settings.bgType == "BAMBOO_GROVE",
                            imageResId = R.drawable.img_bg_bamboo_1782754464039,
                            onClick = { viewModel.updateBackgroundStyle("BAMBOO_GROVE", null) }
                        )
                        BackgroundOptionCard(
                            label = "Zen Garden",
                            selected = settings.bgType == "ZEN_GARDEN",
                            imageResId = R.drawable.img_bg_zen_garden_1782756842302,
                            onClick = { viewModel.updateBackgroundStyle("ZEN_GARDEN", null) }
                        )
                        BackgroundOptionCard(
                            label = "Misty Lake",
                            selected = settings.bgType == "MISTY_LAKE",
                            imageResId = R.drawable.img_bg_misty_lake_1782756854903,
                            onClick = { viewModel.updateBackgroundStyle("MISTY_LAKE", null) }
                        )
                        BackgroundOptionCard(
                            label = "Cosmic Sky",
                            selected = settings.bgType == "COSMIC_HARMONY",
                            imageResId = R.drawable.img_bg_cosmic_harmony_1782756868198,
                            onClick = { viewModel.updateBackgroundStyle("COSMIC_HARMONY", null) }
                        )
                        BackgroundOptionCard(
                            label = "Solid Jade",
                            selected = settings.bgType == "SOLID",
                            imageResId = null,
                            solidColor = MaterialTheme.colorScheme.background,
                            onClick = { viewModel.updateBackgroundStyle("SOLID", null) }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, ZenBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("custom_image_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = ZenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (settings.bgType == "CUSTOM_IMAGE") "Change Custom Background..." else "Add Custom Image Background...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = ZenBorder)

                    // AUDIO BACKGROUND SECTION
                    Text(
                        text = "Ambient Soundscapes",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // --- Ambient Player Card (Now Playing) ---
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (settings.soundType == "SILENT") 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f)
                            else 
                                ZenPrimary.copy(alpha = 0.08f)
                        ),
                        border = BorderStroke(
                            width = 1.dp, 
                            color = if (settings.soundType == "SILENT") ZenBorder else ZenPrimary.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (settings.soundType == "SILENT") "AMBIENT PLAYER" else "NOW PLAYING",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (settings.soundType == "SILENT") 
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                            else 
                                                ZenPrimary,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                    Text(
                                        text = if (settings.soundType == "SILENT") {
                                            "Silence"
                                        } else if (settings.soundType == "CUSTOM_MP3") {
                                            "Custom Soundscape"
                                        } else {
                                            BackgroundSoundManager.PRESET_SOUNDS[settings.soundType] ?: "Silence"
                                        },
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                                
                                // Play/Pause Button
                                FilledIconButton(
                                    onClick = { viewModel.toggleAmbientPlayback() },
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = if (settings.soundType == "SILENT") 
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                        else 
                                            ZenPrimary,
                                        contentColor = if (settings.soundType == "SILENT") 
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        else 
                                            Color.White
                                    ),
                                    modifier = Modifier.size(48.dp).testTag("play_pause_ambient_button")
                                ) {
                                    Icon(
                                        imageVector = if (settings.soundType != "SILENT" && settings.isAmbientPlaying) 
                                            Icons.Filled.Pause 
                                        else 
                                            Icons.Filled.PlayArrow,
                                        contentDescription = if (settings.soundType != "SILENT" && settings.isAmbientPlaying) "Pause" else "Play",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            if (settings.soundType != "SILENT") {
                                Spacer(modifier = Modifier.height(16.dp))

                                // Volume Slider inside the player card
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (settings.soundVolume > 0f) {
                                                viewModel.updateVolume(0f)
                                            } else {
                                                viewModel.updateVolume(0.5f)
                                            }
                                        },
                                        modifier = Modifier.size(24.dp).testTag("mute_unmute_button")
                                    ) {
                                        Icon(
                                            imageVector = if (settings.soundVolume == 0f) 
                                                Icons.Default.VolumeMute 
                                            else 
                                                Icons.Default.VolumeUp,
                                            contentDescription = "Mute Toggle",
                                            tint = ZenPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${(settings.soundVolume * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        ),
                                        modifier = Modifier.width(36.dp)
                                    )
                                    Slider(
                                        value = settings.soundVolume,
                                        onValueChange = { viewModel.updateVolume(it) },
                                        valueRange = 0f..1f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = ZenPrimary,
                                            activeTrackColor = ZenPrimary,
                                            inactiveTrackColor = ZenBorder
                                        ),
                                        modifier = Modifier.weight(1f).testTag("volume_slider")
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        BackgroundSoundManager.PRESET_SOUNDS.forEach { (type, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.updateSoundStyle(type, null) }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = settings.soundType == type,
                                    onClick = { viewModel.updateSoundStyle(type, null) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = ZenPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }

                        // Custom MP3 Picker
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { audioPickerLauncher.launch("audio/*") }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.soundType == "CUSTOM_MP3",
                                onClick = { audioPickerLauncher.launch("audio/*") },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = ZenPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Select Custom MP3 Sound...",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (settings.soundType == "CUSTOM_MP3") FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // 5. Error Message Snackbar Alert
        if (errorMessage != null) {
            Snackbar(
                action = {
                    TextButton(onClick = { viewModel.clearErrorMessage() }) {
                        Text("Dismiss", color = ZenPrimary)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(text = errorMessage!!)
            }
        }
    }
}

@Composable
fun BackgroundOptionCard(
    label: String,
    selected: Boolean,
    imageResId: Int?,
    solidColor: Color? = null,
    customUri: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(115.dp)
            .height(85.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) ZenPrimary else ZenBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (solidColor != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(solidColor)
                )
            } else if (customUri != null) {
                AsyncImage(
                    model = customUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (imageResId != null) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.LightGray)
                )
            }
            
            // Soft overlay gradient at the bottom for text readability
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(vertical = 4.dp, horizontal = 6.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 9.sp
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun RowScope.BackgroundOptionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected) ZenActivePill
                else Color.White.copy(alpha = 0.5f)
            )
            .border(1.dp, ZenBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (selected) Color(0xFF141F07) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        )
    }
}

@Composable
fun RenderBackground(settings: UserSettings) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (settings.bgType) {
            "SOLID" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                )
            }
            "MISTY_MOUNTAINS" -> {
                Image(
                    painter = painterResource(id = R.drawable.img_bg_mountains_1782754451099),
                    contentDescription = "Misty Mountains background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f))
                )
            }
            "BAMBOO_GROVE" -> {
                Image(
                    painter = painterResource(id = R.drawable.img_bg_bamboo_1782754464039),
                    contentDescription = "Bamboo Grove background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f))
                )
            }
            "ZEN_GARDEN" -> {
                Image(
                    painter = painterResource(id = R.drawable.img_bg_zen_garden_1782756842302),
                    contentDescription = "Zen Garden background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f))
                )
            }
            "MISTY_LAKE" -> {
                Image(
                    painter = painterResource(id = R.drawable.img_bg_misty_lake_1782756854903),
                    contentDescription = "Misty Lake background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f))
                )
            }
            "COSMIC_HARMONY" -> {
                Image(
                    painter = painterResource(id = R.drawable.img_bg_cosmic_harmony_1782756868198),
                    contentDescription = "Cosmic Harmony background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f))
                )
            }
            "CUSTOM_IMAGE" -> {
                if (!settings.customBgUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = settings.customBgUri,
                        contentDescription = "Custom visual background",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    )
                }
            }
        }
    }
}

@Composable
fun CycleCompletionTrackerCard(completedCount: Int) {
    val progress = completedCount / 365f
    val percentage = (progress * 100).toInt()
    
    // Determine milestone label
    val milestoneLabel = when {
        completedCount == 0 -> "Journey Begins (Wu Wei Aspirant)"
        completedCount in 1..5 -> "First Steps (Wu Wei Aspirant)"
        completedCount in 6..20 -> "Seeking Harmony (Mindful Scholar)"
        completedCount in 21..50 -> "Gaining Balance (Balanced Practitioner)"
        completedCount in 51..100 -> "Steady Stream (Zen Disciple)"
        completedCount in 101..200 -> "Flowing River (Wisdom Sage)"
        completedCount in 201..364 -> "Ocean of Calm (Enlightened Scholar)"
        else -> "One with the Tao (Perfect Harmony)"
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.85f)
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, ZenBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cycle_completion_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Visual Progress Circle/Ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(64.dp)
            ) {
                CircularProgressIndicator(
                    progress = { if (progress < 0.01f && completedCount > 0) 0.01f else progress },
                    color = ZenPrimary,
                    trackColor = ZenBorder,
                    strokeWidth = 6.dp,
                    modifier = Modifier.fillMaxSize().testTag("cycle_progress_ring")
                )
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = ZenPrimary,
                        fontSize = 12.sp
                    )
                )
            }

            // Text info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "365-DAY CYCLE PROGRESS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = ZenSecondary
                    )
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = "$completedCount of 365 days completed",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = ZenPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = milestoneLabel,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ZenPrimary,
                            fontWeight = FontWeight.Medium,
                            fontStyle = FontStyle.Italic
                        )
                    )
                }
            }
        }
    }
}


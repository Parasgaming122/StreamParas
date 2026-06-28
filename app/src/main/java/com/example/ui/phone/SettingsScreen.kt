package com.example.ui.phone

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.data.api.PlayerSources
import com.example.data.local.Prefs
import com.example.data.repository.MediaRepository
import com.example.ui.navigation.Routes
import com.example.ui.theme.AccentPresets
import com.example.ui.theme.LocalStreambertColors
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    var tmdbKey by mutableStateOf("")
    var isTvMode by mutableStateOf(false)
    var theme by mutableStateOf("dark")
    var accentColor by mutableStateOf("#e50914")
    var tmdbLang by mutableStateOf("en-US")
    var ageLimit by mutableStateOf("")
    var ratingCountry by mutableStateOf("US")
    var watchedThreshold by mutableStateOf(20)
    var historyEnabled by mutableStateOf(true)
    var playerSource by mutableStateOf("")
    var autoplayNextEnabled by mutableStateOf(true)
    var autoplayNextDuration by mutableStateOf(5)
    var introSkipMode by mutableStateOf("off")
    var compactMode by mutableStateOf(false)

    fun loadSettings() {
        viewModelScope.launch {
            tmdbKey = Prefs.getTmdbKey(getApplication()) ?: ""
            isTvMode = Prefs.isTvMode(getApplication())
            theme = Prefs.getTheme(getApplication())
            accentColor = Prefs.getAccentColor(getApplication())
            tmdbLang = Prefs.getTmdbLang(getApplication())
            ageLimit = Prefs.getAgeLimit(getApplication())
            ratingCountry = Prefs.getRatingCountry(getApplication())
            watchedThreshold = Prefs.getWatchedThreshold(getApplication())
            historyEnabled = Prefs.isHistoryEnabled(getApplication())
            playerSource = Prefs.getPlayerSource(getApplication()).ifEmpty { PlayerSources.NON_ANIME_DEFAULT }
            autoplayNextEnabled = Prefs.isAutoplayNextEnabled(getApplication())
            autoplayNextDuration = Prefs.getAutoplayNextDuration(getApplication())
            introSkipMode = Prefs.getIntroSkipMode(getApplication())
            compactMode = Prefs.isCompactMode(getApplication())
        }
    }

    fun saveTmdbKey() {
        viewModelScope.launch {
            Prefs.setTmdbKey(getApplication(), tmdbKey)
            MediaRepository.configureApi(getApplication())
        }
    }

    fun setTvModeValue(value: Boolean) {
        isTvMode = value
        viewModelScope.launch { Prefs.setTvMode(getApplication(), value) }
    }

    fun setThemeValue(value: String) {
        theme = value
        viewModelScope.launch { Prefs.setTheme(getApplication(), value) }
    }

    fun setAccentColorValue(value: String) {
        accentColor = value
        viewModelScope.launch { Prefs.setAccentColor(getApplication(), value) }
    }

    fun setTmdbLangValue(value: String) {
        tmdbLang = value
        viewModelScope.launch { Prefs.setTmdbLang(getApplication(), value) }
    }

    fun setAgeLimitValue(value: String) {
        ageLimit = value
        viewModelScope.launch { Prefs.setAgeLimit(getApplication(), value) }
    }

    fun setRatingCountryValue(value: String) {
        ratingCountry = value
        viewModelScope.launch { Prefs.setRatingCountry(getApplication(), value) }
    }

    fun setWatchedThresholdValue(value: Int) {
        watchedThreshold = value
        viewModelScope.launch { Prefs.setWatchedThreshold(getApplication(), value) }
    }

    fun setHistoryEnabledValue(value: Boolean) {
        historyEnabled = value
        viewModelScope.launch { Prefs.setHistoryEnabled(getApplication(), value) }
    }

    fun setPlayerSourceValue(value: String) {
        playerSource = value
        viewModelScope.launch { Prefs.setPlayerSource(getApplication(), value) }
    }

    fun setAutoplayNextValue(value: Boolean) {
        autoplayNextEnabled = value
        viewModelScope.launch { Prefs.setAutoplayNextEnabled(getApplication(), value) }
    }

    fun setAutoplayNextDurationValue(value: Int) {
        autoplayNextDuration = value
        viewModelScope.launch { Prefs.setAutoplayNextDuration(getApplication(), value) }
    }

    fun setIntroSkipValue(value: String) {
        introSkipMode = value
        viewModelScope.launch { Prefs.setIntroSkipMode(getApplication(), value) }
    }

    fun setCompactModeValue(value: Boolean) {
        compactMode = value
        viewModelScope.launch { Prefs.setCompactMode(getApplication(), value) }
    }

    fun clearCache() {
        // Cache is automatically wiped on TmdbApi configuration/restart
        viewModelScope.launch {
            MediaRepository.configureApi(getApplication())
        }
    }

    fun clearProgress() {
        viewModelScope.launch {
            Prefs.setWatchProgress(getApplication(), emptyMap())
            Prefs.setWatched(getApplication(), emptyMap())
        }
    }

    fun resetApp() {
        viewModelScope.launch {
            Prefs.setTmdbKey(getApplication(), "")
            Prefs.setTvMode(getApplication(), false)
            Prefs.setSetupDone(getApplication(), false)
            Prefs.setTheme(getApplication(), "dark")
            Prefs.setAccentColor(getApplication(), "#e50914")
            Prefs.setTmdbLang(getApplication(), "en-US")
            Prefs.setAgeLimit(getApplication(), "")
            Prefs.setRatingCountry(getApplication(), "US")
            Prefs.setWatchedThreshold(getApplication(), 20)
            Prefs.setHistoryEnabled(getApplication(), true)
            Prefs.setPlayerSource(getApplication(), "")
            Prefs.setWatchHistory(getApplication(), emptyList())
            Prefs.setSaved(getApplication(), emptyMap())
            Prefs.setSavedOrder(getApplication(), emptyList())
            Prefs.setSearchHistory(getApplication(), emptyList())
            Prefs.setWatchProgress(getApplication(), emptyMap())
            Prefs.setWatched(getApplication(), emptyMap())
        }
    }
}

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = viewModel(),
    onSettingsChanged: () -> Unit
) {
    val colors = LocalStreambertColors.current
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    LaunchedEffect(Unit) {
        viewModel.loadSettings()
    }

    var keyVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.text,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // General Section
        SettingsHeader("General Settings")

        // TMDB API Token Row
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Text("TMDB API Read Access Token", color = colors.text2, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = viewModel.tmdbKey,
                    onValueChange = { viewModel.tmdbKey = it },
                    visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { keyVisible = !keyVisible }) {
                            Icon(
                                if (keyVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = "Toggle Key",
                                tint = colors.text2
                            )
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text,
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.border
                    ),
                    modifier = Modifier.weight(1.0f).testTag("settings_key_field")
                )
                Button(
                    onClick = {
                        viewModel.saveTmdbKey()
                        Toast.makeText(context, "API Token saved!", Toast.LENGTH_SHORT).show()
                        onSettingsChanged()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                    modifier = Modifier.height(52.dp).testTag("save_key_button")
                ) {
                    Text("Save", color = Color.White)
                }
            }
        }

        // TV Mode Switch Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("TV UI Mode", color = colors.text, style = MaterialTheme.typography.titleMedium)
                Text("D-Pad optimization (requires app restart)", color = colors.text2, style = MaterialTheme.typography.bodySmall)
            }
            Switch(
                checked = viewModel.isTvMode,
                onCheckedChange = {
                    viewModel.setTvModeValue(it)
                    onSettingsChanged()
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.accent,
                    checkedTrackColor = colors.accent.copy(alpha = 0.4f)
                ),
                modifier = Modifier.testTag("tv_mode_switch")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Appearance Section
        SettingsHeader("Appearance")

        // Theme dropdown picker
        var themeExpanded by remember { mutableStateOf(false) }
        val themesList = listOf("dark", "amoled", "mocha", "slate", "light")
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column {
                Text("Theme Style", color = colors.text2, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surface2, RoundedCornerShape(8.dp))
                        .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                        .clickable { themeExpanded = true }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(viewModel.theme.uppercase(), color = colors.text)
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Theme", tint = colors.text2)
                }
            }
            DropdownMenu(
                expanded = themeExpanded,
                onDismissRequest = { themeExpanded = false },
                modifier = Modifier.background(colors.surface2)
            ) {
                themesList.forEach { t ->
                    DropdownMenuItem(
                        text = { Text(t.uppercase(), color = colors.text) },
                        onClick = {
                            viewModel.setThemeValue(t)
                            themeExpanded = false
                            onSettingsChanged()
                        }
                    )
                }
            }
        }

        // Accent Circle presets selector
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
            Text("Accent Color Selection", color = colors.text, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                AccentPresets.all.forEach { (name, color) ->
                    val colorHex = "#%06X".format((color.value shr 32).toInt() and 0x00FFFFFF)
                    val isSelected = viewModel.accentColor.equals(colorHex, ignoreCase = true)
                    
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable {
                                viewModel.setAccentColorValue(colorHex)
                                onSettingsChanged()
                            }
                    )
                }
            }
        }

        // Compact Mode Switch
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Compact Card Layout", color = colors.text, style = MaterialTheme.typography.titleMedium)
                Text("Reduce card sizes to fit more rows", color = colors.text2, style = MaterialTheme.typography.bodySmall)
            }
            Switch(
                checked = viewModel.compactMode,
                onCheckedChange = { viewModel.setCompactModeValue(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.accent,
                    checkedTrackColor = colors.accent.copy(alpha = 0.4f)
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Parental Controls Section
        SettingsHeader("Parental Controls")

        // Age limits options dropdown
        var limitExpanded by remember { mutableStateOf(false) }
        val limits = listOf("All", "8", "12", "13", "14", "15", "16", "17", "18")
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column {
                Text("Parental Content Age Limit", color = colors.text2, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surface2, RoundedCornerShape(8.dp))
                        .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                        .clickable { limitExpanded = true }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (viewModel.ageLimit.isEmpty()) "No restriction" else "Limit to ${viewModel.ageLimit}+", color = colors.text)
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Age Limit", tint = colors.text2)
                }
            }
            DropdownMenu(
                expanded = limitExpanded,
                onDismissRequest = { limitExpanded = false },
                modifier = Modifier.background(colors.surface2)
            ) {
                limits.forEach { l ->
                    DropdownMenuItem(
                        text = { Text(if (l == "All") "No restriction" else "Age $l+", color = colors.text) },
                        onClick = {
                            viewModel.setAgeLimitValue(if (l == "All") "" else l)
                            limitExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Playback Preferences Section
        SettingsHeader("Playback Settings")

        // Player stream source selector
        var sourceExpanded by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column {
                Text("Default Playback Stream Source", color = colors.text2, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surface2, RoundedCornerShape(8.dp))
                        .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                        .clickable { sourceExpanded = true }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(viewModel.playerSource.uppercase(), color = colors.text)
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Source", tint = colors.text2)
                }
            }
            DropdownMenu(
                expanded = sourceExpanded,
                onDismissRequest = { sourceExpanded = false },
                modifier = Modifier.background(colors.surface2)
            ) {
                PlayerSources.sources.forEach { src ->
                    DropdownMenuItem(
                        text = { Text(src.label, color = colors.text) },
                        onClick = {
                            viewModel.setPlayerSourceValue(src.id)
                            sourceExpanded = false
                        }
                    )
                }
            }
        }

        // Auto-watched threshold Row
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Text("Auto-Watched Threshold (seconds)", color = colors.text2, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = viewModel.watchedThreshold.toString(),
                onValueChange = {
                    val intVal = it.toIntOrNull() ?: 20
                    viewModel.setWatchedThresholdValue(intVal)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.text,
                    unfocusedTextColor = colors.text,
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = colors.border
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Autoplay switch Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Autoplay Next Episode", color = colors.text, style = MaterialTheme.typography.titleMedium)
                Text("Automatically queue and play sequels", color = colors.text2, style = MaterialTheme.typography.bodySmall)
            }
            Switch(
                checked = viewModel.autoplayNextEnabled,
                onCheckedChange = { viewModel.setAutoplayNextValue(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.accent,
                    checkedTrackColor = colors.accent.copy(alpha = 0.4f)
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Backup and Restore Section
        SettingsHeader("Backup & Restore")
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    viewModel.viewModelScope.launch {
                        val data = Prefs.exportAll(context)
                        clipboard.setText(AnnotatedString(data))
                        Toast.makeText(context, "Backup JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.surface2),
                modifier = Modifier.weight(1.0f).height(48.dp)
            ) {
                Text("Export Backup", color = colors.text)
            }

            Button(
                onClick = {
                    viewModel.viewModelScope.launch {
                        val clipText = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                        val pasteData = clipText?.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                        if (pasteData.isNotEmpty() && Prefs.importAll(context, pasteData)) {
                            viewModel.loadSettings()
                            Toast.makeText(context, "Import successful!", Toast.LENGTH_SHORT).show()
                            onSettingsChanged()
                        } else {
                            Toast.makeText(context, "Failed to import. Invalid JSON clipboard data.", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.surface2),
                modifier = Modifier.weight(1.0f).height(48.dp)
            ) {
                Text("Import Backup", color = colors.text)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reset Settings section
        SettingsHeader("Storage & Data")

        Button(
            onClick = {
                viewModel.clearCache()
                Toast.makeText(context, "Cache and image buffers cleared!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = colors.surface3),
            modifier = Modifier.fillMaxWidth().height(48.dp).padding(vertical = 4.dp)
        ) {
            Text("Clear API Cache", color = colors.text)
        }

        Button(
            onClick = {
                viewModel.clearProgress()
                Toast.makeText(context, "Watch histories and progression cleared!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            modifier = Modifier.fillMaxWidth().height(48.dp).padding(vertical = 4.dp)
        ) {
            Text("Clear Watch Progress", color = MaterialTheme.colorScheme.onErrorContainer)
        }

        Button(
            onClick = {
                viewModel.resetApp()
                Toast.makeText(context, "Application resets completely. Please configure a key.", Toast.LENGTH_LONG).show()
                onSettingsChanged()
                navController.navigate(Routes.SETUP) {
                    popUpTo(0) { inclusive = true }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth().height(48.dp).padding(vertical = 4.dp).testTag("reset_app_button")
        ) {
            Text("Reset App", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun SettingsHeader(title: String) {
    val colors = LocalStreambertColors.current
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = title,
            color = colors.accent,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        HorizontalDivider(color = colors.border, thickness = 1.dp, modifier = Modifier.padding(vertical = 6.dp))
    }
}

package com.example.gymtracker.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymtracker.ui.GymViewModel
import com.example.gymtracker.ui.i18n.AppLanguage
import com.example.gymtracker.ui.i18n.LocalAppStrings
import com.example.gymtracker.ui.theme.AppThemeColor
import com.example.gymtracker.ui.theme.BackgroundDark
import com.example.gymtracker.ui.theme.BorderDark
import com.example.gymtracker.ui.theme.PrimaryNeon
import com.example.gymtracker.ui.theme.SecondaryAccent
import com.example.gymtracker.ui.theme.SurfaceDark
import com.example.gymtracker.ui.theme.SurfaceVariantDark
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class MenuSubScreen {
    ROOT,
    EXERCISES_DATABASE,
    AI_EXPORT,
    BACKUP_RESTORE,
    SETTINGS
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MenuScreen(
    viewModel: GymViewModel,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var subScreen by remember { mutableStateOf(MenuSubScreen.ROOT) }

    if (subScreen != MenuSubScreen.ROOT) {
        BackHandler {
            subScreen = MenuSubScreen.ROOT
        }
    }

    when (subScreen) {
        MenuSubScreen.ROOT -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = strings.menuTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(6.dp))

                // КНОПКА «БАЗА УПРАЖНЕНИЙ»
                Card(
                    onClick = { subScreen = MenuSubScreen.EXERCISES_DATABASE },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.AutoMirrored.Filled.List,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(strings.exerciseDatabase, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(strings.exerciseDatabaseDesc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                    }
                }

                // КНОПКА «ЭКСПОРТ ДЛЯ ИИ» (AI DATA EXPORT)
                Card(
                    onClick = { subScreen = MenuSubScreen.AI_EXPORT },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(strings.aiExportTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(strings.aiExportDesc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                    }
                }

                // КНОПКА «РЕЗЕРВНОЕ КОПИРОВАНИЕ»
                Card(
                    onClick = { subScreen = MenuSubScreen.BACKUP_RESTORE },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(strings.backupRestoreTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(strings.backupRestoreDesc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                    }
                }

                // КНОПКА «НАСТРОЙКИ»
                Card(
                    onClick = { subScreen = MenuSubScreen.SETTINGS },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = null,
                                tint = SecondaryAccent,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(strings.settingsTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(strings.settingsDesc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // КАРТОЧКА «О ПРИЛОЖЕНИИ»
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = strings.creatorsLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "${strings.appVersionLabel.substringBeforeLast(" ")} ${com.example.gymtracker.BuildConfig.VERSION_NAME}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        MenuSubScreen.EXERCISES_DATABASE -> {
            Column(modifier = modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { subScreen = MenuSubScreen.ROOT }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.cancelBtn)
                    }
                    Text(strings.exerciseDatabase, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                ExercisesScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
            }
        }

        MenuSubScreen.AI_EXPORT -> {
            AiExportView(
                viewModel = viewModel,
                onBack = { subScreen = MenuSubScreen.ROOT },
                modifier = modifier
            )
        }

        MenuSubScreen.BACKUP_RESTORE -> {
            BackupRestoreView(
                viewModel = viewModel,
                onBack = { subScreen = MenuSubScreen.ROOT },
                modifier = modifier
            )
        }

        MenuSubScreen.SETTINGS -> {
            val currentThemeColor by viewModel.themeColor.collectAsState()
            val currentLanguage by viewModel.appLanguage.collectAsState()
            val isWorkoutNotificationEnabled by viewModel.isWorkoutNotificationEnabled.collectAsState()
            val isSetTagsEnabled by viewModel.isSetTagsEnabled.collectAsState()
            val availableFeelings by viewModel.availableFeelings.collectAsState()
            val availableTags by viewModel.availableTags.collectAsState()

            var showAddFeelingDialog by remember { mutableStateOf(false) }
            var newFeelingText by remember { mutableStateOf("") }

            var showAddTagDialog by remember { mutableStateOf(false) }
            var newTagText by remember { mutableStateOf("") }

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { subScreen = MenuSubScreen.ROOT }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.cancelBtn)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.settingsTitle, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }

                // КАРТОЧКА: ВЫБОР ЯЗЫКА ПРИЛОЖЕНИЯ
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(strings.languageSettingTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(
                                    strings.languageSettingDesc,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppLanguage.entries.forEach { lang ->
                                val isSelected = currentLanguage == lang
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { viewModel.setAppLanguage(lang) },
                                    color = if (isSelected) SurfaceVariantDark else SurfaceDark,
                                    shape = RoundedCornerShape(12.dp),
                                    border = if (isSelected) {
                                        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                    } else {
                                        BorderStroke(1.dp, BorderDark)
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(lang.flag, fontSize = 20.sp)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = lang.nativeName,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    fontSize = 14.sp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                )
                                                if (lang.displayName != lang.nativeName) {
                                                    Text(
                                                        text = lang.displayName,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }

                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // КАРТОЧКА: УВЕДОМЛЕНИЕ АКТИВНОЙ ТРЕНИРОВКИ В ШТОРКЕ
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(strings.notificationSettingTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    strings.notificationSettingDesc,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = isWorkoutNotificationEnabled,
                            onCheckedChange = { viewModel.setWorkoutNotificationEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                // КАРТОЧКА: ТЕГИ И ОЩУЩЕНИЯ В ПОДХОДАХ
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Label,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Теги и ощущения", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "Показывать кнопки тегов и типа сета при записи подходов",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = isSetTagsEnabled,
                                onCheckedChange = { viewModel.setTagsEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        if (isSetTagsEnabled) {
                            // Блок: Ощущения / Тип сета
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Ощущения / Тип сета:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    availableFeelings.forEach { feeling ->
                                        FilterChip(
                                            selected = true,
                                            onClick = { },
                                            label = { Text(feeling, fontSize = 12.sp) },
                                            trailingIcon = {
                                                if (availableFeelings.size > 1) {
                                                    Icon(
                                                        Icons.Default.Close,
                                                        contentDescription = "Удалить",
                                                        modifier = Modifier
                                                            .size(14.dp)
                                                            .clickable { viewModel.removeFeeling(feeling) }
                                                    )
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                    Surface(
                                        modifier = Modifier.clickable {
                                            newFeelingText = ""
                                            showAddFeelingDialog = true
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Добавить", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }

                            // Блок: Теги подхода
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Теги подхода:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    availableTags.forEach { tag ->
                                        FilterChip(
                                            selected = true,
                                            onClick = { },
                                            label = { Text(tag, fontSize = 12.sp) },
                                            trailingIcon = {
                                                if (availableTags.size > 1) {
                                                    Icon(
                                                        Icons.Default.Close,
                                                        contentDescription = "Удалить",
                                                        modifier = Modifier
                                                            .size(14.dp)
                                                            .clickable { viewModel.removeTag(tag) }
                                                    )
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                    Surface(
                                        modifier = Modifier.clickable {
                                            newTagText = ""
                                            showAddTagDialog = true
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Добавить", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }

                            TextButton(
                                onClick = {
                                    viewModel.resetFeelingsToDefault()
                                    viewModel.resetTagsToDefault()
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Сбросить по умолчанию", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }

                // КАРТОЧКА: ВЫБОР ЦВЕТОВОЙ СХЕМЫ
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(strings.colorSchemeTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(
                                    strings.colorSchemeDesc,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppThemeColor.entries.forEach { themeOption ->
                                val isSelected = currentThemeColor == themeOption
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { viewModel.setThemeColor(themeOption) },
                                    color = if (isSelected) SurfaceVariantDark else SurfaceDark,
                                    shape = RoundedCornerShape(12.dp),
                                    border = if (isSelected) {
                                        BorderStroke(1.5.dp, themeOption.primary)
                                    } else {
                                        BorderStroke(1.dp, BorderDark)
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(themeOption.primary)
                                                    .border(
                                                        2.dp,
                                                        if (isSelected) Color.White.copy(alpha = 0.9f) else Color.Transparent,
                                                        CircleShape
                                                    )
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = themeOption.title,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 14.sp,
                                                color = if (isSelected) themeOption.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = themeOption.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (showAddFeelingDialog) {
                    AlertDialog(
                        onDismissRequest = { showAddFeelingDialog = false },
                        title = { Text("Новое ощущение / тип сета") },
                        text = {
                            OutlinedTextField(
                                value = newFeelingText,
                                onValueChange = { newFeelingText = it },
                                label = { Text("Название (например: Памп)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        confirmButton = {
                            Button(onClick = {
                                if (newFeelingText.isNotBlank()) {
                                    viewModel.addFeeling(newFeelingText.trim())
                                    showAddFeelingDialog = false
                                }
                            }) {
                                Text(strings.saveBtn)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showAddFeelingDialog = false }) {
                                Text(strings.cancelBtn)
                            }
                        }
                    )
                }

                if (showAddTagDialog) {
                    AlertDialog(
                        onDismissRequest = { showAddTagDialog = false },
                        title = { Text("Новый тег подхода") },
                        text = {
                            OutlinedTextField(
                                value = newTagText,
                                onValueChange = { newTagText = it },
                                label = { Text("Название тега (например: Резина)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        confirmButton = {
                            Button(onClick = {
                                if (newTagText.isNotBlank()) {
                                    viewModel.addTag(newTagText.trim())
                                    showAddTagDialog = false
                                }
                            }) {
                                Text(strings.saveBtn)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showAddTagDialog = false }) {
                                Text(strings.cancelBtn)
                            }
                        }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// ПОДЭКРАН: ЭКСПОРТ ДЛЯ ИИ
// -------------------------------------------------------------------------------------------------
@Composable
private fun AiExportView(
    viewModel: GymViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val completedSessions by viewModel.completedSessions.collectAsState()
    val allExercises by viewModel.exercises.collectAsState()

    var selectedFormat by remember { mutableStateOf("Markdown") }
    var exportText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    LaunchedEffect(selectedFormat, completedSessions, allExercises) {
        isGenerating = true
        exportText = if (selectedFormat == "Markdown") {
            viewModel.exportHistoryMarkdown()
        } else {
            viewModel.exportHistoryJson()
        }
        isGenerating = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.cancelBtn)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(strings.aiExportTitle, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        // Переключатель формата
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedFormat == "Markdown",
                onClick = { selectedFormat = "Markdown" },
                label = { Text("Markdown (для ChatGPT / Claude)") }
            )
            FilterChip(
                selected = selectedFormat == "JSON",
                onClick = { selectedFormat = "JSON" },
                label = { Text("JSON (сырые данные)") }
            )
        }

        // Превью данных
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Text(
                    text = if (isGenerating) "Генерация выгрузки..." else exportText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                )
            }
        }

        // Кнопки действий: Копировать и Поделиться
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    clipboard.setText(AnnotatedString(exportText))
                    Toast.makeText(context, "Выгрузка скопирована в буфер!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Скопировать")
            }

            OutlinedButton(
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "GymHelper AI Data Export")
                        putExtra(Intent.EXTRA_TEXT, exportText)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Поделиться экспортом"))
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Поделиться")
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// ПОДЭКРАН: РЕЗЕРВНОЕ КОПИРОВАНИЕ
// -------------------------------------------------------------------------------------------------
@Composable
private fun BackupRestoreView(
    viewModel: GymViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showSuccessDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var showConfirmRestoreDialog by remember { mutableStateOf(false) }
    var jsonToRestore by remember { mutableStateOf("") }

    // Launcher для сохранения JSON в файл через системный файловый менеджер
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val ok = viewModel.exportBackupToUri(uri)
                if (ok) {
                    successMessage = "Резервная копия успешно экспортирована в файл!"
                    showSuccessDialog = true
                } else {
                    Toast.makeText(context, "Ошибка записи бэкапа", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Launcher для чтения JSON из файла
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val json = reader.readText()
                reader.close()
                jsonToRestore = json
                showConfirmRestoreDialog = true
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка чтения файла: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.cancelBtn)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(strings.backupRestoreTitle, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        // Карточка: Полный бэкап в JSON
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Экспорт всей базы (Snapshot)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Text(
                    text = "Сохраняет тренировки, все подходы, программы, упражнения и пользовательские фишки в один JSON файл.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = {
                        val fileName = "gym_backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.json"
                        exportLauncher.launch(fileName)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Сохранить бэкап в файл (JSON)")
                }
            }
        }

        // Карточка: Восстановление из JSON
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Восстановление из JSON", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Text(
                    text = "Импортирует данные из сохраненного бэкапа. Существующие записи будут аккуратно дополнены без потери текущих.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                    onClick = {
                        importLauncher.launch(arrayOf("application/json", "text/*"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Выбрать файл бэкапа для восстановления")
                }
            }
        }
    }

    if (showConfirmRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmRestoreDialog = false },
            title = { Text("Подтверждение восстановления", fontWeight = FontWeight.Bold) },
            text = { Text("Вы собираетесь импортировать данные из выбранного файла бэкапа. Продолжить?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val res = viewModel.restoreBackupFromJson(jsonToRestore)
                            showConfirmRestoreDialog = false
                            if (res.isSuccess) {
                                successMessage = res.getOrNull() ?: "База данных успешно восстановлена!"
                                showSuccessDialog = true
                            } else {
                                Toast.makeText(context, "Ошибка: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                ) {
                    Text("Восстановить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmRestoreDialog = false }) { Text("Отмена") }
            }
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryNeon, modifier = Modifier.size(36.dp)) },
            title = { Text("Успешно!", fontWeight = FontWeight.Bold) },
            text = { Text(successMessage) },
            confirmButton = {
                Button(onClick = { showSuccessDialog = false }) { Text("OK") }
            }
        )
    }
}

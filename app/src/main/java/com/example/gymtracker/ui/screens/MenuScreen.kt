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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
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

@Composable
fun MenuScreen(
    viewModel: GymViewModel,
    modifier: Modifier = Modifier
) {
    var subScreen by remember { mutableStateOf(MenuSubScreen.ROOT) }
    var autoStopRest by remember { mutableStateOf(true) }

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
                    text = "Меню",
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
                                tint = PrimaryNeon,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text("База упражнений", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Шторки упражнений, фишки, иконки и памятки", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                    }
                }

                // КНОПКА «ЭКСПОРТ ДЛЯ ИИ (AI DATA EXPORT)»
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
                                tint = PrimaryNeon,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text("Экспорт для ИИ (AI Export)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Выгрузка для ChatGPT, Claude и Gemini (MD / JSON)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                tint = PrimaryNeon,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text("Резервное копирование", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Экспорт базы в JSON, импорт, автосохранение", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                Text("Настройки", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Цветовая схема, таймеры, поведение отдыха", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            text = "Создатель: reapman",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Версия 1.0.1 (Stable)",
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                    Text("База упражнений", fontWeight = FontWeight.Bold, fontSize = 18.sp)
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

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { subScreen = MenuSubScreen.ROOT }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Настройки", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }

                // СЕКЦИЯ: ВЫБОР ЦВЕТОВОЙ СХЕМЫ
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
                                Text("Цветовая схема", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(
                                    "Акцентный цвет кнопок, иконок и элементов",
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
                                                contentDescription = "Выбрано",
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

                // СЕКЦИЯ: ТАЙМЕРЫ И ПОВЕДЕНИЕ
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Авто-отключение отдыха", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Сбрасывать секундомер отдыха при переходе к следующему упражнению",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = autoStopRest,
                            onCheckedChange = { autoStopRest = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AiExportView(
    viewModel: GymViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var isJsonMode by remember { mutableStateOf(false) }
    var exportContent by remember { mutableStateOf("Загрузка данных...") }

    fun refreshContent(json: Boolean) {
        coroutineScope.launch {
            exportContent = if (json) {
                viewModel.getAiExportJson()
            } else {
                viewModel.getAiExportMarkdown()
            }
        }
    }

    LaunchedEffect(isJsonMode) {
        refreshContent(isJsonMode)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ШАПКА
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = "Экспорт для ИИ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Выгрузка истории тренировок для LLM",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ПЕРЕКЛЮЧАТЕЛЬ ФОРМАТА (MARKDOWN / JSON)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !isJsonMode,
                onClick = { isJsonMode = false },
                label = { Text("Markdown (.md) для чата") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryNeon.copy(alpha = 0.2f),
                    selectedLabelColor = PrimaryNeon
                )
            )
            FilterChip(
                selected = isJsonMode,
                onClick = { isJsonMode = true },
                label = { Text("JSON (.json) для агентов") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryNeon.copy(alpha = 0.2f),
                    selectedLabelColor = PrimaryNeon
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ИНФОРМАЦИОННАЯ ПЛАШКА
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Text(
                text = "⚡ Данные включают все завершенные тренировки, упражнения, фишки, веса, повторения, теги интенсивности и заметки. Скопируй текст и вставь в ChatGPT, Claude или Gemini для глубокого анализа прогресса.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(12.dp),
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ОБЛАСТЬ ПРЕДПРОСМОТРА ТЕКСТА
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Text(
                    text = exportContent,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // КНОПКИ ДЕЙСТВИЙ (КОПИРОВАТЬ / ПОДЕЛИТЬСЯ)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    clipboardManager.setText(AnnotatedString(exportContent))
                    Toast.makeText(context, "Скопировано в буфер обмена!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryNeon,
                    contentColor = BackgroundDark
                )
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Скопировать", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, exportContent)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Поделиться журналом тренировок")
                    context.startActivity(shareIntent)
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

@Composable
private fun BackupRestoreView(
    viewModel: GymViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var pendingRestoreJson by remember { mutableStateOf<String?>(null) }
    var isOperating by remember { mutableStateOf(false) }

    // Лаунчер для сохранения файла бэкапа
    var currentBackupJson by remember { mutableStateOf<String?>(null) }
    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null && currentBackupJson != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(currentBackupJson!!.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "Резервная копия успешно сохранена!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка сохранения: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Лаунчер для выбора файла бэкапа
    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.use { input ->
                    BufferedReader(InputStreamReader(input, Charsets.UTF_8)).readText()
                }
                if (!content.isNullOrBlank() && content.contains("\"GymHelper\"")) {
                    pendingRestoreJson = content
                    showRestoreConfirmDialog = true
                } else {
                    Toast.makeText(context, "Выбранный файл не является резервной копией GymHelper!", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка чтения файла: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
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
        // Шапка
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Резервное копирование", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        // КАРТОЧКА: СОЗДАНИЕ РЕЗЕРВНОЙ КОПИИ
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = PrimaryNeon, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Создать резервную копию", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Text(
                    text = "Экспорт всех данных (папки, программы, упражнения, фишки и вся история тренировок с подходами) в надежный JSON-файл.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isOperating = true
                                val json = viewModel.createFullBackup()
                                currentBackupJson = json
                                val timeFormat = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
                                saveFileLauncher.launch("gym_backup_${timeFormat.format(Date())}.json")
                                isOperating = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isOperating
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("В файл")
                    }

                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                isOperating = true
                                val json = viewModel.createFullBackup()
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "GymHelper Backup")
                                    putExtra(Intent.EXTRA_TEXT, json)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Поделиться бэкапом"))
                                isOperating = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isOperating
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Поделиться")
                    }
                }
            }
        }

        // КАРТОЧКА: ВОССТАНОВЛЕНИЕ ИЗ ФАЙЛА
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, tint = SecondaryAccent, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Восстановление из файла", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Text(
                    text = "Загрузите файл резервной копии .json, чтобы восстановить все программы и историю на этом или новом устройстве.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = {
                        pickFileLauncher.launch("application/json")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryAccent),
                    enabled = !isOperating
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Выбрать файл .json для восстановления", fontWeight = FontWeight.Bold)
                }
            }
        }

        // КАРТОЧКА: АВТОМАТИЧЕСКАЯ ЗАЩИТА (УРОВЕНЬ 1 И 3)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = PrimaryNeon, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Система защиты данных", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Surface(
                        color = PrimaryNeon.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "АКТИВНО",
                            color = PrimaryNeon,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "• Crash Recovery: если телефон выключится во время тренировки, таймер и прогресс не пропадут.\n" +
                           "• Автобэкап: после каждого завершения тренировки автоматически сохраняется файл gym_backup_auto.json.\n" +
                           "• Google Auto Backup: системное шифрованное резервное копирование в Google Drive.\n" +
                           "• SQLite WAL-режим: повышенная надежность и защита базы данных от повреждений при сбоях.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // ДИАЛОГ ПОДТВЕРЖДЕНИЯ ВОССТАНОВЛЕНИЯ
    if (showRestoreConfirmDialog && pendingRestoreJson != null) {
        AlertDialog(
            onDismissRequest = {
                showRestoreConfirmDialog = false
                pendingRestoreJson = null
            },
            title = {
                Text("Восстановить данные?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Внимание: все текущие тренировки, упражнения и программы будут заменены данными из выбранной резервной копии. Это действие необратимо.\n\nПродолжить восстановление?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val json = pendingRestoreJson!!
                        showRestoreConfirmDialog = false
                        pendingRestoreJson = null
                        coroutineScope.launch {
                            isOperating = true
                            val success = viewModel.restoreFromBackup(json)
                            isOperating = false
                            if (success) {
                                Toast.makeText(context, "База данных успешно восстановлена!", Toast.LENGTH_LONG).show()
                                onBack()
                            } else {
                                Toast.makeText(context, "Ошибка при восстановлении базы данных", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Восстановить", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRestoreConfirmDialog = false
                    pendingRestoreJson = null
                }) {
                    Text("Отмена")
                }
            }
        )
    }
}

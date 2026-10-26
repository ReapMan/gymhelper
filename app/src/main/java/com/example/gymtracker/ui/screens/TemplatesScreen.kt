package com.example.gymtracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymtracker.data.model.Exercise
import com.example.gymtracker.data.model.ProgramFolder
import com.example.gymtracker.data.model.WorkoutTemplate
import com.example.gymtracker.data.model.WorkoutTemplateItem
import com.example.gymtracker.ui.GymViewModel
import com.example.gymtracker.ui.components.ExerciseIcons
import com.example.gymtracker.ui.components.ExercisePickerModal
import com.example.gymtracker.ui.i18n.LocalAppStrings
import com.example.gymtracker.ui.theme.PrimaryNeon
import com.example.gymtracker.ui.theme.SecondaryAccent
import com.example.gymtracker.ui.theme.SurfaceDark
import com.example.gymtracker.ui.theme.SurfaceVariantDark

@Composable
fun TemplatesScreen(
    viewModel: GymViewModel,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val folders by viewModel.folders.collectAsState()
    val allTemplates by viewModel.templates.collectAsState()
    val allExercises by viewModel.exercises.collectAsState()
    val muscleGroups by viewModel.muscleGroups.collectAsState()

    var selectedFolderId by remember { mutableStateOf<Long?>(folders.firstOrNull()?.id) }

    var showCreateProgramDialog by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var folderToEdit by remember { mutableStateOf<ProgramFolder?>(null) }
    var folderToDelete by remember { mutableStateOf<ProgramFolder?>(null) }
    var templateToRename by remember { mutableStateOf<WorkoutTemplate?>(null) }
    var templateToDelete by remember { mutableStateOf<WorkoutTemplate?>(null) }

    // Детальное управление тренировкой (добавление/удаление упражнений в неё)
    var managingTemplate by remember { mutableStateOf<WorkoutTemplate?>(null) }

    if (selectedFolderId == null && folders.isNotEmpty()) {
        selectedFolderId = folders.first().id
    }

    val currentFolder = folders.find { it.id == selectedFolderId }
    val currentTemplates = allTemplates.filter { it.folderId == selectedFolderId }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateProgramDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Создать тренировку")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ВЕРХНЯЯ СТРОКА: ПАПКИ
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Папки:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = { showCreateFolderDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.newFolderBtn, fontSize = 13.sp)
                }
            }

            // ПАПКИ-ВКЛАДКИ (С возможностью редактировать и удалять!)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(folders) { f ->
                    val isSel = selectedFolderId == f.id
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedFolderId = f.id },
                        label = { Text(f.name) },
                        leadingIcon = {
                            Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        trailingIcon = {
                            if (isSel) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Редактировать папку",
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { folderToEdit = f }
                                    )
                                    if (folders.size > 1) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Удалить папку",
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { folderToDelete = f }
                                        )
                                    }
                                }
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryNeon.copy(alpha = 0.2f),
                            selectedLabelColor = PrimaryNeon
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // СПИСОК ТРЕНИРОВОК ВНУТРИ ВЫБРАННОЙ ПАПКИ
            if (currentTemplates.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "В этой папке пока нет тренировок",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Нажми кнопку «+» снизу, чтобы добавить тренировочный день в папку «${currentFolder?.name ?: ""}».",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentTemplates, key = { it.id }) { template ->
                        TemplateCard(
                            template = template,
                            viewModel = viewModel,
                            onStart = { viewModel.startWorkoutFromTemplate(template) },
                            onRename = { templateToRename = template },
                            onEdit = { managingTemplate = template },
                            onDelete = { templateToDelete = template }
                        )
                    }
                }
            }
        }

        // ДИАЛОГ СОЗДАНИЯ ПАПКИ
        if (showCreateFolderDialog) {
            var fName by remember { mutableStateOf("") }
            var fDesc by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showCreateFolderDialog = false },
                title = { Text("Новая папка") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = fName,
                            onValueChange = { fName = it },
                            label = { Text("Название папки (например: Мой сплит)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = fDesc,
                            onValueChange = { fDesc = it },
                            label = { Text("Описание (опционально)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (fName.isNotBlank()) {
                            viewModel.createFolder(fName.trim(), fDesc.trim())
                            showCreateFolderDialog = false
                        }
                    }) {
                        Text("Создать")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateFolderDialog = false }) { Text("Отмена") }
                }
            )
        }

        // ДИАЛОГ РЕДАКТИРОВАНИЯ ПАПКИ
        if (folderToEdit != null) {
            var fName by remember(folderToEdit) { mutableStateOf(folderToEdit!!.name) }
            var fDesc by remember(folderToEdit) { mutableStateOf(folderToEdit!!.description) }

            AlertDialog(
                onDismissRequest = { folderToEdit = null },
                title = { Text("Переименовать папку", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = fName,
                            onValueChange = { fName = it },
                            label = { Text("Название папки") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = fDesc,
                            onValueChange = { fDesc = it },
                            label = { Text("Описание (опционально)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (fName.isNotBlank()) {
                            viewModel.updateFolder(folderToEdit!!.id, fName.trim(), fDesc.trim())
                            folderToEdit = null
                        }
                    }) {
                        Text("Сохранить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { folderToEdit = null }) { Text("Отмена") }
                }
            )
        }

        // ДИАЛОГ ПЕРЕИМЕНОВАНИЯ ТРЕНИРОВКИ (ПЛАНА)
        if (templateToRename != null) {
            var tName by remember(templateToRename) { mutableStateOf(templateToRename!!.name) }
            var tDesc by remember(templateToRename) { mutableStateOf(templateToRename!!.description) }

            AlertDialog(
                onDismissRequest = { templateToRename = null },
                title = { Text("Переименовать план", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = tName,
                            onValueChange = { tName = it },
                            label = { Text("Название тренировки") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = tDesc,
                            onValueChange = { tDesc = it },
                            label = { Text("Описание (опционально)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (tName.isNotBlank()) {
                            viewModel.updateTemplate(templateToRename!!.id, tName.trim(), tDesc.trim())
                            templateToRename = null
                        }
                    }) {
                        Text("Сохранить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { templateToRename = null }) { Text("Отмена") }
                }
            )
        }

        // ДИАЛОГ ПОДТВЕРЖДЕНИЯ УДАЛЕНИЯ ПАПКИ
        if (folderToDelete != null) {
            AlertDialog(
                onDismissRequest = { folderToDelete = null },
                title = { Text("Удалить папку?") },
                text = { Text("Точно удалить папку «${folderToDelete!!.name}»? Все тренировки внутри этой папки также будут удалены.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteFolder(folderToDelete!!.id)
                            folderToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Удалить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { folderToDelete = null }) { Text("Отмена") }
                }
            )
        }

        // ДИАЛОГ ПОДТВЕРЖДЕНИЯ УДАЛЕНИЯ ТРЕНИРОВКИ ИЗ ПАПКИ
        if (templateToDelete != null) {
            AlertDialog(
                onDismissRequest = { templateToDelete = null },
                title = { Text("Удалить тренировку?") },
                text = { Text("Точно удалить тренировку «${templateToDelete!!.name}» из этой папки?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteTemplate(templateToDelete!!.id)
                            templateToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Удалить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { templateToDelete = null }) { Text("Отмена") }
                }
            )
        }

        // ДИАЛОГ СОЗДАНИЯ ТРЕНИРОВКИ В ПАПКУ (Скрин 5: Новая тренировка в *имя папки*)
        if (showCreateProgramDialog) {
            var pName by remember { mutableStateOf("") }
            var pDesc by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showCreateProgramDialog = false },
                title = {
                    Text("Новая тренировка в «${currentFolder?.name ?: "папку"}»", fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = pName,
                            onValueChange = { pName = it },
                            label = { Text("Название (например: День 1: Спина)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = pDesc,
                            onValueChange = { pDesc = it },
                            label = { Text("Описание (опционально)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (pName.isNotBlank()) {
                                viewModel.createTemplate(pName, pDesc, selectedFolderId, emptyList())
                                showCreateProgramDialog = false
                            }
                        },
                        enabled = pName.isNotBlank()
                    ) {
                        Text("Создать")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateProgramDialog = false }) { Text("Отмена") }
                }
            )
        }

        // МОДАЛЬНЫЙ РЕДАКТОР УПРАЖНЕНИЙ ВНУТРИ ТРЕНИРОВКИ
        if (managingTemplate != null) {
            TemplateExerciseManagerModal(
                template = managingTemplate!!,
                viewModel = viewModel,
                allExercises = allExercises,
                muscleGroups = muscleGroups,
                onDismiss = { managingTemplate = null }
            )
        }
    }
}

@Composable
private fun TemplateCard(
    template: WorkoutTemplate,
    viewModel: GymViewModel,
    onStart: () -> Unit,
    onRename: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val strings = LocalAppStrings.current
    var itemsCount by remember { mutableStateOf(0) }
    LaunchedEffect(template.id) {
        itemsCount = viewModel.getTemplateItems(template.id).size
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = template.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        IconButton(onClick = onRename, modifier = Modifier.size(28.dp)) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Переименовать план",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = "${strings.exercisesInPlan}: $itemsCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Редактировать упражнения", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            if (template.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.startThisWorkout, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TemplateExerciseManagerModal(
    template: WorkoutTemplate,
    viewModel: GymViewModel,
    allExercises: List<Exercise>,
    muscleGroups: List<com.example.gymtracker.data.model.MuscleGroup>,
    onDismiss: () -> Unit
) {
    var items by remember { mutableStateOf<List<WorkoutTemplateItem>>(emptyList()) }
    var showAddExerciseFromBase by remember { mutableStateOf(false) }
    var showRenameDialogInModal by remember { mutableStateOf(false) }
    var currentTemplateName by remember(template) { mutableStateOf(template.name) }
    var currentTemplateDesc by remember(template) { mutableStateOf(template.description) }
    var exerciseToDeleteFromPlan by remember { mutableStateOf<Exercise?>(null) }
    val coroutineScope = rememberCoroutineScope()

    fun refreshItems() {
        coroutineScope.launch {
            items = viewModel.getTemplateItems(template.id)
        }
    }

    LaunchedEffect(template.id) {
        items = viewModel.getTemplateItems(template.id)
    }

    if (showRenameDialogInModal) {
        var tName by remember { mutableStateOf(currentTemplateName) }
        var tDesc by remember { mutableStateOf(currentTemplateDesc) }

        AlertDialog(
            onDismissRequest = { showRenameDialogInModal = false },
            title = { Text("Переименовать план", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = tName,
                        onValueChange = { tName = it },
                        label = { Text("Название тренировки") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tDesc,
                        onValueChange = { tDesc = it },
                        label = { Text("Описание (опционально)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (tName.isNotBlank()) {
                        viewModel.updateTemplate(template.id, tName.trim(), tDesc.trim())
                        currentTemplateName = tName.trim()
                        currentTemplateDesc = tDesc.trim()
                        showRenameDialogInModal = false
                    }
                }) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialogInModal = false }) { Text("Отмена") }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(currentTemplateName, fontWeight = FontWeight.Black)
                    Text("План упражнений", fontSize = 13.sp, color = SecondaryAccent)
                }
                IconButton(onClick = { showRenameDialogInModal = true }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Переименовать план",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Упражнения (${items.size}):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Button(
                        onClick = { showAddExerciseFromBase = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Из базы", fontSize = 12.sp)
                    }
                }

                if (items.isEmpty()) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("В плане пока нет упражнений.\nНажми «+ Из базы».", textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        itemsIndexed(items, key = { _, itm -> itm.id }) { index, itm ->
                            val ex = allExercises.find { it.id == itm.exerciseId }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = ExerciseIcons.getIcon(ex?.icon),
                                            contentDescription = null,
                                            tint = PrimaryNeon,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(ex?.name ?: "Упражнение", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                viewModel.moveExerciseInTemplate(template.id, items, index, index - 1)
                                                refreshItems()
                                            },
                                            enabled = index > 0,
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.KeyboardArrowUp,
                                                contentDescription = "Выше",
                                                tint = if (index > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                viewModel.moveExerciseInTemplate(template.id, items, index, index + 1)
                                                refreshItems()
                                            },
                                            enabled = index < items.size - 1,
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Ниже",
                                                tint = if (index < items.size - 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(4.dp))

                                        IconButton(
                                            onClick = { exerciseToDeleteFromPlan = ex },
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Убрать",
                                                tint = MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Готово") }
        }
    )

    // ПОДТВЕРЖДЕНИЕ УДАЛЕНИЯ УПРАЖНЕНИЯ ИЗ ПЛАНА ТРЕНИРОВКИ
    if (exerciseToDeleteFromPlan != null) {
        AlertDialog(
            onDismissRequest = { exerciseToDeleteFromPlan = null },
            title = { Text("Убрать упражнение из плана?") },
            text = { Text("Убрать «${exerciseToDeleteFromPlan!!.name}» из этой программы?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeExerciseFromTemplate(template.id, exerciseToDeleteFromPlan!!.id)
                        exerciseToDeleteFromPlan = null
                        refreshItems()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Убрать")
                }
            },
            dismissButton = {
                TextButton(onClick = { exerciseToDeleteFromPlan = null }) { Text("Отмена") }
            }
        )
    }

    // ВЫБОР УПРАЖНЕНИЯ ИЗ БАЗЫ (СОВРЕМЕННЫЙ ПИКЕР)
    if (showAddExerciseFromBase) {
        ExercisePickerModal(
            allExercises = allExercises,
            muscleGroups = muscleGroups,
            onDismiss = { showAddExerciseFromBase = false },
            onSelectExercises = { selectedList ->
                selectedList.forEach { ex ->
                    if (items.none { it.exerciseId == ex.id }) {
                        viewModel.addExerciseToTemplate(template.id, ex.id, null)
                    }
                }
                showAddExerciseFromBase = false
                refreshItems()
            },
            onCreateCustomExercise = { name, groupId ->
                viewModel.createCustomExercise(name, groupId) { created ->
                    viewModel.addExerciseToTemplate(template.id, created.id, null)
                    refreshItems()
                }
            }
        )
    }
}

package com.example.gymtracker.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymtracker.ui.GymViewModel
import com.example.gymtracker.ui.ScreenTab
import com.example.gymtracker.ui.components.PastWorkoutDialog
import com.example.gymtracker.ui.theme.BackgroundDark

@Composable
fun LiveWorkoutScreen(
    viewModel: GymViewModel,
    modifier: Modifier = Modifier
) {
    val activeSession by viewModel.activeSession.collectAsState()
    val isInExerciseFocus by viewModel.isInExerciseFocus.collectAsState()
    val selectedExercise by viewModel.selectedExercise.collectAsState()

    var showPastWorkoutDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(BackgroundDark)) {
        if (activeSession == null) {
            // ЭКРАН СТАРТА ТРЕНИРОВКИ
            StartWorkoutPanel(
                onStartLive = { viewModel.startLiveWorkout() },
                onStartFromTemplate = { viewModel.switchTab(ScreenTab.TEMPLATES) },
                onRecordPast = { showPastWorkoutDialog = true },
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            // ТРЕНИРОВКА АКТИВНА: либо режим фокуса на упражнении, либо роадмап
            if (isInExerciseFocus && selectedExercise != null) {
                ExerciseFocusView(
                    viewModel = viewModel,
                    exercise = selectedExercise!!
                )
            } else {
                WorkoutRoadmapView(
                    viewModel = viewModel,
                    session = activeSession!!
                )
            }
        }

        if (showPastWorkoutDialog) {
            PastWorkoutDialog(
                onDismiss = { showPastWorkoutDialog = false },
                onConfirm = { title, start, end, notes ->
                    viewModel.recordPastWorkout(title, start, end, notes, emptyList())
                }
            )
        }
    }
}

@Composable
private fun StartWorkoutPanel(
    onStartLive: () -> Unit,
    onStartFromTemplate: () -> Unit,
    onRecordPast: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                modifier = Modifier.size(42.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Начать тренировку",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Выбери формат записи для зала:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Быстрый старт
        Button(
            onClick = onStartLive,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Свободная тренировка", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Из заготовленной программы
        Button(
            onClick = onStartFromTemplate,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Запустить из программы",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Записать прошедшую тренировку
        OutlinedButton(
            onClick = onRecordPast,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(50.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.History, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Записать прошедшую", fontSize = 15.sp)
        }
    }
}

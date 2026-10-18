package com.example.gymtracker.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymtracker.data.model.WorkoutSession
import com.example.gymtracker.ui.GymViewModel
import com.example.gymtracker.ui.ScreenTab
import com.example.gymtracker.ui.theme.BackgroundDark
import com.example.gymtracker.ui.theme.BorderDark
import com.example.gymtracker.ui.theme.PrimaryNeon
import com.example.gymtracker.ui.theme.SurfaceDark
import com.example.gymtracker.ui.theme.SurfaceVariantDark

@Composable
fun MainAppScreen(
    viewModel: GymViewModel
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val isWorkoutMinimized by viewModel.isWorkoutMinimized.collectAsState()
    val workoutSeconds by viewModel.workoutSeconds.collectAsState()

    // Если тренировки нет, либо она свернута в фон — нижняя панель навигации доступна!
    val showBottomBar = activeSession == null || isWorkoutMinimized

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundDark,
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // ПЛАВАЮЩИЙ БАННЕР АКТИВНОЙ ТРЕНИРОВКИ В ФОНЕ (ПОВЕРХ ВСЕХ ВКЛАДОК)
                if (activeSession != null && isWorkoutMinimized) {
                    WorkoutRunningBanner(
                        session = activeSession!!,
                        workoutSeconds = workoutSeconds,
                        onReturn = { viewModel.maximizeWorkout() }
                    )
                }

                if (showBottomBar) {
                    NavigationBar(
                        containerColor = SurfaceDark,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        NavigationBarItem(
                            selected = currentTab == ScreenTab.WORKOUT,
                            onClick = {
                                if (activeSession != null) {
                                    viewModel.maximizeWorkout()
                                } else {
                                    viewModel.switchTab(ScreenTab.WORKOUT)
                                }
                            },
                            icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Зал") },
                            label = { Text("Тренировка", fontWeight = if (currentTab == ScreenTab.WORKOUT) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                indicatorColor = PrimaryNeon
                            )
                        )

                        NavigationBarItem(
                            selected = currentTab == ScreenTab.TEMPLATES,
                            onClick = { viewModel.switchTab(ScreenTab.TEMPLATES) },
                            icon = { Icon(Icons.Default.Bookmark, contentDescription = "Программы") },
                            label = { Text("Программы", fontWeight = if (currentTab == ScreenTab.TEMPLATES) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                indicatorColor = PrimaryNeon
                            )
                        )

                        NavigationBarItem(
                            selected = currentTab == ScreenTab.HISTORY,
                            onClick = { viewModel.switchTab(ScreenTab.HISTORY) },
                            icon = { Icon(Icons.Default.History, contentDescription = "История") },
                            label = { Text("История", fontWeight = if (currentTab == ScreenTab.HISTORY) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                indicatorColor = PrimaryNeon
                            )
                        )

                        NavigationBarItem(
                            selected = currentTab == ScreenTab.MENU,
                            onClick = { viewModel.switchTab(ScreenTab.MENU) },
                            icon = { Icon(Icons.Default.Menu, contentDescription = "Меню") },
                            label = { Text("Меню", fontWeight = if (currentTab == ScreenTab.MENU) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                indicatorColor = PrimaryNeon
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        when (currentTab) {
            ScreenTab.WORKOUT -> LiveWorkoutScreen(viewModel = viewModel, modifier = modifier)
            ScreenTab.TEMPLATES -> TemplatesScreen(viewModel = viewModel, modifier = modifier)
            ScreenTab.HISTORY -> HistoryScreen(viewModel = viewModel, modifier = modifier)
            ScreenTab.MENU -> MenuScreen(viewModel = viewModel, modifier = modifier)
        }
    }
}

@Composable
fun WorkoutRunningBanner(
    session: WorkoutSession,
    workoutSeconds: Long,
    onReturn: () -> Unit,
    modifier: Modifier = Modifier
) {
    fun formatSeconds(sec: Long): String {
        val h = sec / 3600
        val m = (sec % 3600) / 60
        val s = sec % 60
        return if (h > 0) "%02d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onReturn() }
            .border(1.dp, PrimaryNeon.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = PrimaryNeon.copy(alpha = 0.2f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = PrimaryNeon,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Идет тренировка",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = PrimaryNeon
                        )
                        Text(
                            text = " • ${formatSeconds(workoutSeconds)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = session.title,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Button(
                onClick = onReturn,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryNeon,
                    contentColor = BackgroundDark
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("К тренировке", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

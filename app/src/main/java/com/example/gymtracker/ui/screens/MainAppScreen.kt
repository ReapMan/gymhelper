package com.example.gymtracker.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymtracker.data.model.WorkoutSession
import com.example.gymtracker.ui.GymViewModel
import com.example.gymtracker.ui.ScreenTab
import com.example.gymtracker.ui.i18n.LocalAppStrings
import com.example.gymtracker.ui.theme.BackgroundDark
import com.example.gymtracker.ui.theme.SurfaceDark

@Composable
fun MainAppScreen(
    viewModel: GymViewModel
) {
    val strings = LocalAppStrings.current

    val currentTab by viewModel.currentTab.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val isWorkoutMinimized by viewModel.isWorkoutMinimized.collectAsState()
    val workoutSeconds by viewModel.workoutSeconds.collectAsState()

    // Если тренировки нет, либо она свернута в фон — нижняя панель навигации доступна
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
                            icon = { Icon(Icons.Default.FitnessCenter, contentDescription = strings.navWorkout) },
                            label = {
                                Text(
                                    strings.navWorkout,
                                    fontWeight = if (currentTab == ScreenTab.WORKOUT) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                indicatorColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        NavigationBarItem(
                            selected = currentTab == ScreenTab.TEMPLATES,
                            onClick = { viewModel.switchTab(ScreenTab.TEMPLATES) },
                            icon = { Icon(Icons.Default.Bookmark, contentDescription = strings.navPrograms) },
                            label = {
                                Text(
                                    strings.navPrograms,
                                    fontWeight = if (currentTab == ScreenTab.TEMPLATES) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                indicatorColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        NavigationBarItem(
                            selected = currentTab == ScreenTab.HISTORY,
                            onClick = { viewModel.switchTab(ScreenTab.HISTORY) },
                            icon = { Icon(Icons.Default.History, contentDescription = strings.navHistory) },
                            label = {
                                Text(
                                    strings.navHistory,
                                    fontWeight = if (currentTab == ScreenTab.HISTORY) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                indicatorColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        NavigationBarItem(
                            selected = currentTab == ScreenTab.MENU,
                            onClick = { viewModel.switchTab(ScreenTab.MENU) },
                            icon = { Icon(Icons.Default.Menu, contentDescription = strings.navMenu) },
                            label = {
                                Text(
                                    strings.navMenu,
                                    fontWeight = if (currentTab == ScreenTab.MENU) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                indicatorColor = MaterialTheme.colorScheme.primary
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
    val strings = LocalAppStrings.current

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
            .border(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                shape = RoundedCornerShape(16.dp)
            ),
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
            // Левая часть: Иконка + Название тренировки + Статус
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.title.ifBlank { strings.defaultWorkoutTitle },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = strings.workoutRunning,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                }
            }

            // Правая часть: Горизонтальный таймер-чип с моноширинным шрифтом + кнопка перехода
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                modifier = Modifier.clickable { onReturn() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = formatSeconds(workoutSeconds),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        softWrap = false,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = strings.returnToWorkout,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

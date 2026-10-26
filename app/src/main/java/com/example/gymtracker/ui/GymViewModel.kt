package com.example.gymtracker.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.data.backup.GymBackupManager
import com.example.gymtracker.data.dao.GymDao
import com.example.gymtracker.data.export.AiExportManager
import com.example.gymtracker.data.model.Exercise
import com.example.gymtracker.data.model.ExerciseHistoryDay
import com.example.gymtracker.data.model.ExerciseVariation
import com.example.gymtracker.data.model.MuscleGroup
import com.example.gymtracker.data.model.ProgramFolder
import com.example.gymtracker.data.model.WorkoutSession
import com.example.gymtracker.data.model.WorkoutSet
import com.example.gymtracker.data.model.WorkoutTemplate
import com.example.gymtracker.data.model.WorkoutTemplateItem
import com.example.gymtracker.ui.theme.AppThemeColor
import com.example.gymtracker.ui.i18n.AppLanguage
import com.example.gymtracker.service.WorkoutNotificationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class ScreenTab {
    WORKOUT,
    TEMPLATES,
    HISTORY,
    MENU
}


class GymViewModel(
    private val gymDao: GymDao,
    private val context: Context? = null
) : ViewModel() {

    // Текущая вкладка навигации
    private val _currentTab = MutableStateFlow(ScreenTab.WORKOUT)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    fun switchTab(tab: ScreenTab) {
        _currentTab.value = tab
    }

    // Цветовая тема приложения
    private val themePrefs = context?.getSharedPreferences("gym_theme_prefs", Context.MODE_PRIVATE)
    private val _themeColor = MutableStateFlow(
        AppThemeColor.fromId(themePrefs?.getString("theme_color_id", AppThemeColor.NEON_GREEN.id))
    )
    val themeColor: StateFlow<AppThemeColor> = _themeColor.asStateFlow()

    fun setThemeColor(color: AppThemeColor) {
        _themeColor.value = color
        themePrefs?.edit()?.putString("theme_color_id", color.id)?.apply()
    }

    // Язык приложения
    private val appPrefs = context?.getSharedPreferences("gym_app_prefs", Context.MODE_PRIVATE)
    private val _appLanguage = MutableStateFlow(
        AppLanguage.fromCode(appPrefs?.getString("app_language_code", AppLanguage.RUSSIAN.code) ?: "ru")
    )
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    fun setAppLanguage(language: AppLanguage) {
        _appLanguage.value = language
        appPrefs?.edit()?.putString("app_language_code", language.code)?.apply()
    }

    // Уведомление активной тренировки в шторке
    private val _isWorkoutNotificationEnabled = MutableStateFlow(
        appPrefs?.getBoolean("workout_notification_enabled", true) ?: true
    )
    val isWorkoutNotificationEnabled: StateFlow<Boolean> = _isWorkoutNotificationEnabled.asStateFlow()

    fun setWorkoutNotificationEnabled(enabled: Boolean) {
        _isWorkoutNotificationEnabled.value = enabled
        appPrefs?.edit()?.putBoolean("workout_notification_enabled", enabled)?.apply()
        if (enabled && activeSession.value != null) {
            startWorkoutNotification()
        } else {
            stopWorkoutNotification()
        }
    }

    fun startWorkoutNotification() {
        val ctx = context ?: return
        if (!_isWorkoutNotificationEnabled.value) return
        val session = activeSession.value ?: return
        WorkoutNotificationService.start(ctx, session.title, session.startTime)
    }

    fun updateWorkoutNotification() {
        val ctx = context ?: return
        if (!_isWorkoutNotificationEnabled.value) return
        val session = activeSession.value ?: return
        val isResting = !_isPerformingSet.value
        WorkoutNotificationService.updateRest(ctx, isResting, _restSeconds.value, session.title)
    }

    fun stopWorkoutNotification() {
        val ctx = context ?: return
        WorkoutNotificationService.stop(ctx)
    }

    fun renameActiveSession(newTitle: String) {
        val trimmed = newTitle.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            activeSession.value?.let { session ->
                gymDao.updateSession(session.copy(title = trimmed))
                if (_isWorkoutNotificationEnabled.value) {
                    val ctx = context ?: return@launch
                    WorkoutNotificationService.updateRest(ctx, !_isPerformingSet.value, _restSeconds.value, trimmed)
                }
            }
        }
    }

    // Свернута ли тренировка в фон (позволяет пользоваться вкладками навигации)
    private val _isWorkoutMinimized = MutableStateFlow(false)
    val isWorkoutMinimized: StateFlow<Boolean> = _isWorkoutMinimized.asStateFlow()

    fun minimizeWorkout() {
        _isWorkoutMinimized.value = true
    }

    fun maximizeWorkout() {
        _isWorkoutMinimized.value = false
        _currentTab.value = ScreenTab.WORKOUT
    }

    // Все фишки каталога для шторок в базе упражнений
    private val _allVariations = MutableStateFlow<List<ExerciseVariation>>(emptyList())
    val allVariations: StateFlow<List<ExerciseVariation>> = _allVariations.asStateFlow()

    fun refreshAllVariations() {
        viewModelScope.launch {
            _allVariations.value = gymDao.getAllVariations()
        }
    }

    // --- ДАННЫЕ ИЗ БАЗЫ ---
    val muscleGroups: StateFlow<List<MuscleGroup>> = gymDao.getAllMuscleGroups()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val exercises: StateFlow<List<Exercise>> = gymDao.getAllExercises()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val activeSession: StateFlow<WorkoutSession?> = gymDao.getActiveSession()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val completedSessions: StateFlow<List<WorkoutSession>> = gymDao.getCompletedSessions()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val folders: StateFlow<List<ProgramFolder>> = gymDao.getAllFolders()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val templates: StateFlow<List<WorkoutTemplate>> = gymDao.getAllTemplates()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Подходы текущей сессии
    private val _activeSessionSets = MutableStateFlow<List<WorkoutSet>>(emptyList())
    val activeSessionSets: StateFlow<List<WorkoutSet>> = _activeSessionSets.asStateFlow()

    // --- РОАДМАП ТРЕНИРОВКИ ---
    private val _workoutPlan = MutableStateFlow<List<Exercise>>(emptyList())
    val workoutPlan: StateFlow<List<Exercise>> = _workoutPlan.asStateFlow()

    fun moveExerciseInPlan(fromIndex: Int, toIndex: Int) {
        val current = _workoutPlan.value.toMutableList()
        if (fromIndex !in current.indices || toIndex !in current.indices) return
        val item = current.removeAt(fromIndex)
        current.add(toIndex, item)
        _workoutPlan.value = current
    }

    // --- РЕЖИМ ФОКУСА НА УПРАЖНЕНИИ ---
    private val _isInExerciseFocus = MutableStateFlow(false)
    val isInExerciseFocus: StateFlow<Boolean> = _isInExerciseFocus.asStateFlow()

    private val _selectedExercise = MutableStateFlow<Exercise?>(null)
    val selectedExercise: StateFlow<Exercise?> = _selectedExercise.asStateFlow()

    private val _exerciseVariations = MutableStateFlow<List<ExerciseVariation>>(emptyList())
    val exerciseVariations: StateFlow<List<ExerciseVariation>> = _exerciseVariations.asStateFlow()

    private val _selectedVariation = MutableStateFlow<ExerciseVariation?>(null)
    val selectedVariation: StateFlow<ExerciseVariation?> = _selectedVariation.asStateFlow()

    private val _lastSetHint = MutableStateFlow<WorkoutSet?>(null)
    val lastSetHint: StateFlow<WorkoutSet?> = _lastSetHint.asStateFlow()

    // --- СЕКУНДОМЕРЫ И ФАЗЫ (ПОДХОД / ОТДЫХ) ---
    private val _workoutSeconds = MutableStateFlow(0L)
    val workoutSeconds: StateFlow<Long> = _workoutSeconds.asStateFlow()

    // Секундомеры для каждого упражнения (не сбрасываются при выходе в роадмап!)
    private val exerciseDurationMap = mutableMapOf<Long, Long>()
    private val _currentExerciseSeconds = MutableStateFlow(0L)
    val currentExerciseSeconds: StateFlow<Long> = _currentExerciseSeconds.asStateFlow()

    private val _restSeconds = MutableStateFlow(0L)
    val restSeconds: StateFlow<Long> = _restSeconds.asStateFlow()

    private val _isPerformingSet = MutableStateFlow(false)
    val isPerformingSet: StateFlow<Boolean> = _isPerformingSet.asStateFlow()

    private var tickerJob: Job? = null

    init {
        refreshAllVariations()
        viewModelScope.launch {
            activeSession.collect { session ->
                if (session != null) {
                    val elapsed = (System.currentTimeMillis() - session.startTime).coerceAtLeast(0L) / 1000L
                    _workoutSeconds.value = elapsed
                    if (tickerJob == null || tickerJob?.isActive != true) {
                        _isWorkoutMinimized.value = true
                        startTickers()
                        startWorkoutNotification()
                    }
                    gymDao.getSetsForSession(session.id).collect { sets ->
                        _activeSessionSets.value = sets
                        val existingPlanIds = _workoutPlan.value.map { it.id }.toSet()
                        val newExerciseIds = sets.map { it.exerciseId }.distinct().filter { !existingPlanIds.contains(it) }
                        if (newExerciseIds.isNotEmpty()) {
                            val newExercises = exercises.value.filter { newExerciseIds.contains(it.id) }
                            _workoutPlan.value = _workoutPlan.value + newExercises
                        }
                    }
                } else {
                    _activeSessionSets.value = emptyList()
                    _workoutPlan.value = emptyList()
                    _isWorkoutMinimized.value = false
                    stopTickers()
                    stopWorkoutNotification()
                }
            }
        }
    }

    private fun startTickers() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val session = activeSession.value
                if (session != null) {
                    _workoutSeconds.value = (System.currentTimeMillis() - session.startTime) / 1000L

                    // Секундомер упражнения тикает непрерывно в фоне, даже если открыт роадмап
                    val curEx = _selectedExercise.value
                    if (curEx != null) {
                        val currentExTime = (exerciseDurationMap[curEx.id] ?: 0L) + 1
                        exerciseDurationMap[curEx.id] = currentExTime
                        _currentExerciseSeconds.value = currentExTime
                    }

                    // Секундомер отдыха тикает непрерывно в фоне
                    if (!_isPerformingSet.value) {
                        _restSeconds.value += 1
                        if (_isWorkoutNotificationEnabled.value && _restSeconds.value % 5 == 0L) {
                            updateWorkoutNotification()
                        }
                    }
                }

            }
        }
    }

    private fun stopTickers() {
        tickerJob?.cancel()
        _workoutSeconds.value = 0L
        _currentExerciseSeconds.value = 0L
        exerciseDurationMap.clear()
        _restSeconds.value = 0L
        _isPerformingSet.value = false
    }

    // --- РОАДМАП ТРЕНИРОВКИ ---

    fun addExerciseToPlan(exercise: Exercise) {
        if (!_workoutPlan.value.any { it.id == exercise.id }) {
            _workoutPlan.value = _workoutPlan.value + exercise
        }
    }

    fun addExercisesToPlan(exercisesToAdd: List<Exercise>) {
        val currentPlan = _workoutPlan.value.toMutableList()
        val currentIds = currentPlan.map { it.id }.toSet()
        val toAdd = exercisesToAdd.filter { !currentIds.contains(it.id) }
        if (toAdd.isNotEmpty()) {
            currentPlan.addAll(toAdd)
            _workoutPlan.value = currentPlan
        }
    }

    fun createCustomExercise(name: String, muscleGroupId: Long, onCreated: (Exercise) -> Unit) {
        viewModelScope.launch {
            val ex = Exercise(
                name = name.trim(),
                muscleGroupId = muscleGroupId,
                notes = "",
                icon = "dumbbell"
            )
            val id = gymDao.insertExercise(ex)
            val created = ex.copy(id = id)
            onCreated(created)
        }
    }

    fun removeExerciseFromPlan(exercise: Exercise) {
        _workoutPlan.value = _workoutPlan.value.filter { it.id != exercise.id }
        if (_selectedExercise.value?.id == exercise.id) {
            exitExerciseFocus()
        }
    }

    fun moveExerciseUp(index: Int) {
        if (index > 0 && index < _workoutPlan.value.size) {
            val list = _workoutPlan.value.toMutableList()
            val item = list.removeAt(index)
            list.add(index - 1, item)
            _workoutPlan.value = list
        }
    }

    fun moveExerciseDown(index: Int) {
        if (index >= 0 && index < _workoutPlan.value.size - 1) {
            val list = _workoutPlan.value.toMutableList()
            val item = list.removeAt(index)
            list.add(index + 1, item)
            _workoutPlan.value = list
        }
    }

    fun replaceExerciseInPlan(oldExercise: Exercise, newExercise: Exercise) {
        val list = _workoutPlan.value.toMutableList()
        val idx = list.indexOfFirst { it.id == oldExercise.id }
        if (idx != -1) {
            list[idx] = newExercise
            _workoutPlan.value = list
            if (_selectedExercise.value?.id == oldExercise.id) {
                enterExerciseFocus(newExercise)
            }
        }
    }

    // --- ФОКУС НА УПРАЖНЕНИИ ---

    fun enterExerciseFocus(exercise: Exercise) {
        _selectedExercise.value = exercise
        _isInExerciseFocus.value = true
        _currentExerciseSeconds.value = exerciseDurationMap[exercise.id] ?: 0L

        addExerciseToPlan(exercise)

        viewModelScope.launch {
            val variations = gymDao.getVariationsList(exercise.id)
            _exerciseVariations.value = variations
            _selectedVariation.value = variations.firstOrNull()
            updateLastSetHint(exercise.id, _selectedVariation.value?.id)
        }
    }

    fun exitExerciseFocus() {
        _isInExerciseFocus.value = false
    }

    fun goToNextExercise() {
        // При переходе к следующему упражнению выключается отдых
        _restSeconds.value = 0L
        _isPerformingSet.value = false

        val plan = _workoutPlan.value
        val currentIndex = plan.indexOfFirst { it.id == _selectedExercise.value?.id }
        if (currentIndex != -1 && currentIndex < plan.size - 1) {
            enterExerciseFocus(plan[currentIndex + 1])
        } else {
            exitExerciseFocus()
        }
    }

    fun selectVariation(variation: ExerciseVariation?) {
        _selectedVariation.value = variation
        _selectedExercise.value?.let { ex ->
            updateLastSetHint(ex.id, variation?.id)
        }
    }

    private fun updateLastSetHint(exerciseId: Long, variationId: Long?) {
        viewModelScope.launch {
            val currentSessionId = activeSession.value?.id ?: 0L
            _lastSetHint.value = gymDao.getLastSetForVariation(exerciseId, variationId, currentSessionId)
        }
    }

    suspend fun loadExerciseHistory(exerciseId: Long, variationId: Long?): List<ExerciseHistoryDay> {
        return gymDao.getExerciseHistory(exerciseId, variationId)
    }

    // --- ФАЗЫ ПОДХОДОВ И ТАЙМЕРЫ ---

    fun startSet() {
        _isPerformingSet.value = true
        _restSeconds.value = 0L
        updateWorkoutNotification()
    }

    fun recordSetAndStartRest(
        weight: Float,
        reps: Int,
        tags: List<String> = emptyList(),
        intensity: String = "",
        notes: String = ""
    ) {
        val session = activeSession.value ?: return
        val exercise = selectedExercise.value ?: return
        val variation = selectedVariation.value

        viewModelScope.launch {
            val existingSets = _activeSessionSets.value.filter { it.exerciseId == exercise.id }
            val nextSetNumber = existingSets.size + 1
            val newSet = WorkoutSet(
                sessionId = session.id,
                exerciseId = exercise.id,
                variationId = variation?.id,
                setNumber = nextSetNumber,
                weightKg = weight,
                reps = reps,
                tags = tags.joinToString(", "),
                intensityStatus = intensity,
                notes = notes,
                isCompleted = true
            )
            gymDao.insertSet(newSet)
            updateLastSetHint(exercise.id, variation?.id)

            _isPerformingSet.value = false
            _restSeconds.value = 0L
            updateWorkoutNotification()
        }
    }

    fun deleteSet(set: WorkoutSet) {
        viewModelScope.launch {
            gymDao.deleteSet(set)
        }
    }

    // --- СЕССИИ ТРЕНИРОВОК ---

    fun startLiveWorkout(title: String = "Тренировка") {
        viewModelScope.launch {
            gymDao.createSession(title = title, startTime = System.currentTimeMillis())
            _workoutPlan.value = emptyList()
            _isInExerciseFocus.value = false
            startTickers()
        }
    }

    fun startWorkoutFromTemplate(template: WorkoutTemplate) {
        viewModelScope.launch {
            gymDao.createSession(title = template.name, startTime = System.currentTimeMillis())
            val items = gymDao.getTemplateItems(template.id)
            val planExercises = items.mapNotNull { item ->
                exercises.value.find { it.id == item.exerciseId }
            }
            _workoutPlan.value = planExercises
            _isInExerciseFocus.value = false
            _currentTab.value = ScreenTab.WORKOUT
            startTickers()
        }
    }

    fun recordPastWorkout(title: String, startTime: Long, endTime: Long, notes: String, sets: List<WorkoutSet>) {
        viewModelScope.launch {
            val sessionId = gymDao.createSession(
                title = title.ifBlank { "Прошедшая тренировка" },
                startTime = startTime,
                endTime = endTime,
                notes = notes
            )
            sets.forEach { set ->
                gymDao.insertSet(set.copy(sessionId = sessionId, isCompleted = true))
            }
            _currentTab.value = ScreenTab.HISTORY
        }
    }

    fun finishWorkout(notes: String = "") {
        viewModelScope.launch {
            activeSession.value?.let { session ->
                gymDao.updateSession(
                    session.copy(
                        endTime = System.currentTimeMillis(),
                        notes = notes.ifBlank { session.notes }
                    )
                )
                stopTickers()
                _selectedExercise.value = null
                _selectedVariation.value = null
                _lastSetHint.value = null
                _isInExerciseFocus.value = false
                _workoutPlan.value = emptyList()
                _currentTab.value = ScreenTab.HISTORY
                context?.let { ctx ->
                    GymBackupManager.saveAutoBackup(ctx, gymDao)
                }
            }
        }
    }

    fun updateSessionDetails(sessionId: Long, title: String, notes: String, startTime: Long, endTime: Long?) {
        viewModelScope.launch {
            gymDao.updateSessionDetails(sessionId, title, notes, startTime, endTime)
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            gymDao.deleteSession(sessionId)
        }
    }

    suspend fun getSetsForHistorySession(sessionId: Long): List<WorkoutSet> {
        return gymDao.getSetsForSessionSync(sessionId)
    }

    // --- КАТАЛОГ: ГРУППЫ, УПРАЖНЕНИЯ, ФИШКИ ---

    fun addMuscleGroup(name: String) {
        viewModelScope.launch {
            gymDao.insertMuscleGroup(name)
        }
    }

    fun addExerciseSimple(name: String, muscleGroupId: Long) {
        viewModelScope.launch {
            gymDao.insertExercise(
                Exercise(name = name.trim(), muscleGroupId = muscleGroupId, notes = "")
            )
        }
    }

    fun updateExerciseNotes(exercise: Exercise, newNotes: String) {
        viewModelScope.launch {
            gymDao.updateExercise(exercise.copy(notes = newNotes))
            if (_selectedExercise.value?.id == exercise.id) {
                _selectedExercise.value = exercise.copy(notes = newNotes)
            }
        }
    }

    fun addVariationToExercise(exerciseId: Long, name: String) {
        viewModelScope.launch {
            gymDao.insertVariation(ExerciseVariation(exerciseId = exerciseId, name = name.trim()))
            if (_selectedExercise.value?.id == exerciseId) {
                val variations = gymDao.getVariationsList(exerciseId)
                _exerciseVariations.value = variations
            }
            refreshAllVariations()
        }
    }

    fun renameVariation(id: Long, newName: String, exerciseId: Long) {
        viewModelScope.launch {
            gymDao.updateVariationName(id, newName.trim())
            if (_selectedExercise.value?.id == exerciseId) {
                val variations = gymDao.getVariationsList(exerciseId)
                _exerciseVariations.value = variations
            }
            refreshAllVariations()
        }
    }

    fun deleteVariation(id: Long, exerciseId: Long) {
        viewModelScope.launch {
            gymDao.deleteVariation(id)
            if (_selectedExercise.value?.id == exerciseId) {
                val variations = gymDao.getVariationsList(exerciseId)
                _exerciseVariations.value = variations
            }
            refreshAllVariations()
        }
    }

    fun updateExerciseFull(exerciseId: Long, name: String, notes: String, icon: String, muscleGroupId: Long) {
        viewModelScope.launch {
            val updated = Exercise(
                id = exerciseId,
                name = name.trim(),
                notes = notes.trim(),
                icon = icon,
                muscleGroupId = muscleGroupId
            )
            gymDao.updateExercise(updated)
            if (_selectedExercise.value?.id == exerciseId) {
                _selectedExercise.value = updated
            }
        }
    }

    fun deleteExercise(id: Long) {
        viewModelScope.launch {
            gymDao.deleteExercise(id)
            if (_selectedExercise.value?.id == id) {
                _selectedExercise.value = null
            }
        }
    }

    // --- ПАПКИ И ПРОГРАММЫ (СПЛИТЫ) ---

    fun createFolder(name: String, description: String = "") {
        viewModelScope.launch {
            gymDao.insertFolder(name, description)
        }
    }

    fun updateFolder(folderId: Long, name: String, description: String = "") {
        viewModelScope.launch {
            gymDao.updateFolder(folderId, name, description)
        }
    }

    fun deleteFolder(folderId: Long) {
        viewModelScope.launch {
            gymDao.deleteFolder(folderId)
        }
    }

    fun createTemplate(name: String, description: String, folderId: Long?, items: List<WorkoutTemplateItem>) {
        viewModelScope.launch {
            gymDao.createTemplate(name, description, folderId, items)
        }
    }

    suspend fun getTemplateItems(templateId: Long): List<WorkoutTemplateItem> {
        return gymDao.getTemplateItems(templateId)
    }

    fun addExerciseToTemplate(templateId: Long, exerciseId: Long, variationId: Long?, targetSets: Int = 3) {
        viewModelScope.launch {
            gymDao.addExerciseToTemplate(templateId, exerciseId, variationId, targetSets)
        }
    }

    fun removeExerciseFromTemplate(templateId: Long, exerciseId: Long) {
        viewModelScope.launch {
            gymDao.removeExerciseFromTemplate(templateId, exerciseId)
        }
    }

    fun deleteTemplate(templateId: Long) {
        viewModelScope.launch {
            gymDao.deleteTemplate(templateId)
        }
    }

    fun updateTemplate(templateId: Long, name: String, description: String = "") {
        viewModelScope.launch {
            gymDao.updateTemplate(templateId, name, description)
        }
    }

    fun moveExerciseInTemplate(templateId: Long, items: List<WorkoutTemplateItem>, fromIndex: Int, toIndex: Int) {
        if (fromIndex !in items.indices || toIndex !in items.indices) return
        val mutable = items.toMutableList()
        val item = mutable.removeAt(fromIndex)
        mutable.add(toIndex, item)
        viewModelScope.launch {
            gymDao.reorderTemplateItems(templateId, mutable)
        }
    }

    // --- ПОДХОДЫ: ОБНОВЛЕНИЕ И СИНХРОНИЗАЦИЯ ---

    fun updateSet(set: WorkoutSet) {
        viewModelScope.launch {
            gymDao.updateSet(set)
        }
    }

    suspend fun updateSetSync(set: WorkoutSet) {
        gymDao.updateSet(set)
    }

    suspend fun deleteSetSync(set: WorkoutSet) {
        gymDao.deleteSet(set)
    }

    suspend fun getVariationsForExerciseSync(exerciseId: Long): List<ExerciseVariation> {
        return gymDao.getVariationsList(exerciseId)
    }

    // --- РЕЗЕРВНОЕ КОПИРОВАНИЕ И ВОССТАНОВЛЕНИЕ (BACKUP & RESTORE) ---

    suspend fun createFullBackup(): String {
        return GymBackupManager.createFullBackupJson(gymDao)
    }

    suspend fun restoreFromBackup(jsonString: String): Boolean {
        val success = gymDao.restoreDatabaseFromBackup(jsonString)
        if (success) {
            refreshAllVariations()
        }
        return success
    }

    suspend fun getAiExportMarkdown(): String {
        val exercisesList = exercises.value
        val variationsList = gymDao.getAllVariations()
        val sessionsList = completedSessions.value
        val setsList = gymDao.getAllSets()
        return AiExportManager.generateMarkdown(
            sessions = sessionsList,
            allSets = setsList,
            exercises = exercisesList,
            variations = variationsList
        )
    }

    suspend fun getAiExportJson(): String {
        val exercisesList = exercises.value
        val variationsList = gymDao.getAllVariations()
        val sessionsList = completedSessions.value
        val setsList = gymDao.getAllSets()
        return AiExportManager.generateJson(
            sessions = sessionsList,
            allSets = setsList,
            exercises = exercisesList,
            variations = variationsList
        )
    }

    suspend fun exportHistoryMarkdown(): String = getAiExportMarkdown()
    suspend fun exportHistoryJson(): String = getAiExportJson()

    suspend fun exportBackupToUri(uri: android.net.Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val ctx = context ?: return@withContext false
            val json = createFullBackup()
            ctx.contentResolver.openOutputStream(uri)?.use { os ->
                os.write(json.toByteArray(Charsets.UTF_8))
                os.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun restoreBackupFromJson(jsonString: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val success = restoreFromBackup(jsonString)
            if (success) {
                Result.success("База данных успешно восстановлена!")
            } else {
                Result.failure(Exception("Не удалось восстановить базу данных"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addSetToHistorySession(
        sessionId: Long,
        exerciseId: Long,
        weight: Float,
        reps: Int,
        tags: List<String> = emptyList(),
        intensity: String = "",
        notes: String = "",
        variationId: Long? = null
    ) {
        val existing = gymDao.getSetsForSessionSync(sessionId).filter { it.exerciseId == exerciseId }
        val nextSetNum = existing.size + 1
        val set = WorkoutSet(
            sessionId = sessionId,
            exerciseId = exerciseId,
            variationId = variationId,
            setNumber = nextSetNum,
            weightKg = weight,
            reps = reps,
            tags = tags.joinToString(", "),
            intensityStatus = intensity,
            notes = notes,
            isCompleted = true
        )
        gymDao.insertSet(set)
    }
}


class GymViewModelFactory(
    private val gymDao: GymDao,
    private val context: Context? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GymViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GymViewModel(gymDao, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

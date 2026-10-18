package com.example.gymtracker.data.dao

import com.example.gymtracker.data.model.Exercise
import com.example.gymtracker.data.model.ExerciseHistoryDay
import com.example.gymtracker.data.model.ExerciseVariation
import com.example.gymtracker.data.model.MuscleGroup
import com.example.gymtracker.data.model.ProgramFolder
import com.example.gymtracker.data.model.WorkoutSession
import com.example.gymtracker.data.model.WorkoutSet
import com.example.gymtracker.data.model.WorkoutTemplate
import com.example.gymtracker.data.model.WorkoutTemplateItem
import kotlinx.coroutines.flow.Flow

interface GymDao {
    // Muscle Groups (Группы упражнений)
    fun getAllMuscleGroups(): Flow<List<MuscleGroup>>
    suspend fun insertMuscleGroup(name: String): Long
    suspend fun deleteMuscleGroup(id: Long)

    // Exercises
    fun getAllExercises(): Flow<List<Exercise>>
    suspend fun insertExercise(exercise: Exercise): Long
    suspend fun updateExercise(exercise: Exercise)
    suspend fun deleteExercise(id: Long)

    // Variations (Фишки)
    fun getVariationsForExercise(exerciseId: Long): Flow<List<ExerciseVariation>>
    suspend fun getVariationsList(exerciseId: Long): List<ExerciseVariation>
    suspend fun getAllVariations(): List<ExerciseVariation>
    suspend fun insertVariation(variation: ExerciseVariation): Long
    suspend fun updateVariationName(id: Long, name: String)
    suspend fun deleteVariation(id: Long)

    // Sessions (Live & Retrospective)
    fun getActiveSession(): Flow<WorkoutSession?>
    fun getCompletedSessions(): Flow<List<WorkoutSession>>
    suspend fun createSession(title: String, startTime: Long, endTime: Long? = null, notes: String = ""): Long
    suspend fun updateSession(session: WorkoutSession)
    suspend fun updateSessionDetails(sessionId: Long, title: String, notes: String, startTime: Long, endTime: Long?)
    suspend fun deleteSession(sessionId: Long)

    // Sets
    fun getSetsForSession(sessionId: Long): Flow<List<WorkoutSet>>
    suspend fun getSetsForSessionSync(sessionId: Long): List<WorkoutSet>
    suspend fun getAllSets(): List<WorkoutSet>
    suspend fun insertSet(set: WorkoutSet): Long
    suspend fun updateSet(set: WorkoutSet)
    suspend fun deleteSet(set: WorkoutSet)

    // Поиск последнего подхода по фишке
    suspend fun getLastSetForVariation(exerciseId: Long, variationId: Long?, excludeSessionId: Long): WorkoutSet?

    // Полная история упражнения по фишке для анализа пирамид/елочек
    suspend fun getExerciseHistory(exerciseId: Long, variationId: Long?): List<ExerciseHistoryDay>

    // Folders (Папки программ)
    fun getAllFolders(): Flow<List<ProgramFolder>>
    suspend fun insertFolder(name: String, description: String = ""): Long
    suspend fun updateFolder(folderId: Long, name: String, description: String)
    suspend fun deleteFolder(folderId: Long)

    // Templates (Тренировки внутри папок)
    fun getAllTemplates(): Flow<List<WorkoutTemplate>>
    fun getTemplatesForFolder(folderId: Long?): Flow<List<WorkoutTemplate>>
    suspend fun getTemplateItems(templateId: Long): List<WorkoutTemplateItem>
    suspend fun createTemplate(name: String, description: String, folderId: Long?, items: List<WorkoutTemplateItem>): Long
    suspend fun updateTemplate(templateId: Long, name: String, description: String)
    suspend fun addExerciseToTemplate(templateId: Long, exerciseId: Long, variationId: Long?, targetSets: Int)
    suspend fun removeExerciseFromTemplate(templateId: Long, exerciseId: Long)
    suspend fun reorderTemplateItems(templateId: Long, items: List<WorkoutTemplateItem>)
    suspend fun deleteTemplate(templateId: Long)

    // Backup & Restore
    suspend fun getAllMuscleGroupsSync(): List<MuscleGroup>
    suspend fun getAllExercisesSync(): List<Exercise>
    suspend fun getAllFoldersSync(): List<ProgramFolder>
    suspend fun getAllTemplatesSync(): List<WorkoutTemplate>
    suspend fun getAllTemplateItemsSync(): List<WorkoutTemplateItem>
    suspend fun getAllSessionsSync(): List<WorkoutSession>
    suspend fun restoreDatabaseFromBackup(jsonString: String): Boolean
}


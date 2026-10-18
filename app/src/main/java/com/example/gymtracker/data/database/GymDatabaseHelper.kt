package com.example.gymtracker.data.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.gymtracker.data.dao.GymDao
import com.example.gymtracker.data.model.Exercise
import com.example.gymtracker.data.model.ExerciseHistoryDay
import com.example.gymtracker.data.model.ExerciseVariation
import com.example.gymtracker.data.model.MuscleGroup
import com.example.gymtracker.data.model.ProgramFolder
import com.example.gymtracker.data.model.WorkoutSession
import com.example.gymtracker.data.model.WorkoutSet
import com.example.gymtracker.data.model.WorkoutTemplate
import com.example.gymtracker.data.model.WorkoutTemplateItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject

class GymDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION), GymDao {

    companion object {
        private const val DATABASE_NAME = "gym_tracker_v3.db"
        private const val DATABASE_VERSION = 1

        @Volatile
        private var instance: GymDatabaseHelper? = null

        fun getInstance(context: Context): GymDatabaseHelper {
            return instance ?: synchronized(this) {
                instance ?: GymDatabaseHelper(context.applicationContext).also { instance = it }
            }
        }
    }

    private val muscleGroupsFlow = MutableStateFlow<List<MuscleGroup>>(emptyList())
    private val exercisesFlow = MutableStateFlow<List<Exercise>>(emptyList())
    private val activeSessionFlow = MutableStateFlow<WorkoutSession?>(null)
    private val completedSessionsFlow = MutableStateFlow<List<WorkoutSession>>(emptyList())
    private val currentSessionSetsFlow = MutableStateFlow<List<WorkoutSet>>(emptyList())
    private val foldersFlow = MutableStateFlow<List<ProgramFolder>>(emptyList())
    private val templatesFlow = MutableStateFlow<List<WorkoutTemplate>>(emptyList())
    private var currentObservedSessionId: Long = -1

    init {
        refreshAll()
    }

    private fun refreshAll() {
        refreshMuscleGroups()
        refreshExercises()
        refreshActiveSession()
        refreshCompletedSessions()
        refreshFolders()
        refreshTemplates()
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
        db.enableWriteAheadLogging()
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE muscle_groups (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE exercises (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                muscleGroupId INTEGER NOT NULL,
                notes TEXT NOT NULL DEFAULT '',
                icon TEXT NOT NULL DEFAULT 'dumbbell',
                FOREIGN KEY (muscleGroupId) REFERENCES muscle_groups (id) ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE exercise_variations (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                exerciseId INTEGER NOT NULL,
                name TEXT NOT NULL,
                FOREIGN KEY (exerciseId) REFERENCES exercises (id) ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE workout_sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL DEFAULT 'Тренировка',
                startTime INTEGER NOT NULL,
                endTime INTEGER,
                notes TEXT NOT NULL DEFAULT ''
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE workout_sets (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                sessionId INTEGER NOT NULL,
                exerciseId INTEGER NOT NULL,
                variationId INTEGER,
                setNumber INTEGER NOT NULL,
                weightKg REAL NOT NULL,
                reps INTEGER NOT NULL,
                tags TEXT NOT NULL DEFAULT '',
                intensityStatus TEXT NOT NULL DEFAULT '',
                notes TEXT NOT NULL DEFAULT '',
                isCompleted INTEGER NOT NULL DEFAULT 1,
                timestamp INTEGER NOT NULL,
                FOREIGN KEY (sessionId) REFERENCES workout_sessions (id) ON DELETE CASCADE,
                FOREIGN KEY (exerciseId) REFERENCES exercises (id) ON DELETE CASCADE,
                FOREIGN KEY (variationId) REFERENCES exercise_variations (id) ON DELETE SET NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE program_folders (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                description TEXT NOT NULL DEFAULT ''
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE workout_templates (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                folderId INTEGER,
                name TEXT NOT NULL,
                description TEXT NOT NULL DEFAULT '',
                FOREIGN KEY (folderId) REFERENCES program_folders (id) ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE workout_template_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                templateId INTEGER NOT NULL,
                exerciseId INTEGER NOT NULL,
                variationId INTEGER,
                targetSets INTEGER NOT NULL DEFAULT 3,
                orderIndex INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY (templateId) REFERENCES workout_templates (id) ON DELETE CASCADE,
                FOREIGN KEY (exerciseId) REFERENCES exercises (id) ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_workout_sets_session ON workout_sets(sessionId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_workout_sets_exercise ON workout_sets(exerciseId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_exercise_variations_exercise ON exercise_variations(exerciseId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_workout_templates_folder ON workout_templates(folderId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_workout_template_items_template ON workout_template_items(templateId)")

        seedInitialData(db)
    }

    private fun seedInitialData(db: SQLiteDatabase) {
        val groups = listOf("Грудь", "Спина", "Ноги", "Плечи", "Руки", "Пресс")
        val groupIds = mutableMapOf<String, Long>()

        for (g in groups) {
            val cv = ContentValues().apply { put("name", g) }
            val id = db.insert("muscle_groups", null, cv)
            groupIds[g] = id
        }

        fun addEx(name: String, group: String, notes: String = "", variations: List<String> = emptyList()): Long {
            val gId = groupIds[group] ?: return 0L
            val cv = ContentValues().apply {
                put("name", name)
                put("muscleGroupId", gId)
                put("notes", notes)
            }
            val exId = db.insert("exercises", null, cv)
            for (v in variations) {
                val vCv = ContentValues().apply {
                    put("exerciseId", exId)
                    put("name", v)
                }
                db.insert("exercise_variations", null, vCv)
            }
            return exId
        }

        val e1 = addEx("Тяга верхнего блока", "Спина", "Контролировать сведение лопаток", listOf("Широкая рукоять", "V-образная (параллельная)", "Обратный хват", "Одной рукой"))
        val e2 = addEx("Жим штанги лёжа", "Грудь", "Сведение лопаток, мост", listOf("Классический хват", "Узкий хват", "В Смите"))
        val e3 = addEx("Жим гантелей", "Грудь", "Локти под 45 градусов", listOf("На горизонтальной", "На наклонной 30°", "На наклонной 45°"))
        val e4 = addEx("Подтягивания", "Спина", "", listOf("Широкий прямой", "Параллельный", "Обратный", "С весом"))
        val e5 = addEx("Приседания со штангой", "Ноги", "", listOf("На спине", "Фронтальные", "В Смите"))
        val e6 = addEx("Подъем на бицепс", "Руки", "", listOf("Прямой гриф", "EZ-гриф", "Гантели с супинацией", "Молот"))

        // Дефолтная папка со сплитом
        val fCv = ContentValues().apply {
            put("name", "Классический сплит (3 дня)")
            put("description", "Разделение по мышечным группам")
        }
        val folderId = db.insert("program_folders", null, fCv)

        fun addProg(fId: Long, name: String, desc: String, exIds: List<Long>) {
            val tCv = ContentValues().apply {
                put("folderId", fId)
                put("name", name)
                put("description", desc)
            }
            val tId = db.insert("workout_templates", null, tCv)
            exIds.forEachIndexed { idx, exId ->
                val iCv = ContentValues().apply {
                    put("templateId", tId)
                    put("exerciseId", exId)
                    put("targetSets", 3)
                    put("orderIndex", idx)
                }
                db.insert("workout_template_items", null, iCv)
            }
        }

        addProg(folderId, "День 1: Грудь и Бицепс", "Жим лежа + подъем на бицепс", listOf(e2, e3, e6))
        addProg(folderId, "День 2: Спина", "Тяга верхнего блока + подтягивания", listOf(e1, e4))
        addProg(folderId, "День 3: Ноги", "Приседания и база", listOf(e5))
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        try {
            db.execSQL("PRAGMA foreign_keys = ON;")
            // Clean up any historical orphaned records left over before foreign keys were enforced
            db.execSQL("DELETE FROM workout_sets WHERE sessionId NOT IN (SELECT id FROM workout_sessions)")
            db.execSQL("DELETE FROM workout_template_items WHERE templateId NOT IN (SELECT id FROM workout_templates)")
            db.execSQL("DELETE FROM workout_templates WHERE folderId IS NOT NULL AND folderId NOT IN (SELECT id FROM program_folders)")
        } catch (_: Exception) {}
        try {
            db.execSQL("ALTER TABLE exercises ADD COLUMN icon TEXT NOT NULL DEFAULT 'dumbbell'")
        } catch (_: Exception) {}
        try {
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_workout_sets_session ON workout_sets(sessionId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_workout_sets_exercise ON workout_sets(exerciseId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_exercise_variations_exercise ON exercise_variations(exerciseId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_workout_templates_folder ON workout_templates(folderId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_workout_template_items_template ON workout_template_items(templateId)")
        } catch (_: Exception) {}
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS workout_template_items")
        db.execSQL("DROP TABLE IF EXISTS workout_templates")
        db.execSQL("DROP TABLE IF EXISTS program_folders")
        db.execSQL("DROP TABLE IF EXISTS workout_sets")
        db.execSQL("DROP TABLE IF EXISTS workout_sessions")
        db.execSQL("DROP TABLE IF EXISTS exercise_variations")
        db.execSQL("DROP TABLE IF EXISTS exercises")
        db.execSQL("DROP TABLE IF EXISTS muscle_groups")
        onCreate(db)
    }

    // --- REFRESH HELPERS ---

    private fun refreshMuscleGroups() {
        val list = mutableListOf<MuscleGroup>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id, name FROM muscle_groups ORDER BY name ASC", null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(MuscleGroup(it.getLong(0), it.getString(1)))
            }
        }
        muscleGroupsFlow.value = list
    }

    private fun refreshExercises() {
        val list = mutableListOf<Exercise>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id, name, muscleGroupId, notes, icon FROM exercises ORDER BY name ASC", null)
        cursor.use {
            while (it.moveToNext()) {
                val icon = if (it.columnCount > 4 && !it.isNull(4)) it.getString(4) else "dumbbell"
                list.add(Exercise(it.getLong(0), it.getString(1), it.getLong(2), it.getString(3) ?: "", icon))
            }
        }
        exercisesFlow.value = list
    }

    private fun refreshActiveSession() {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, title, startTime, endTime, notes FROM workout_sessions WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1",
            null
        )
        val session = cursor.use {
            if (it.moveToNext()) {
                WorkoutSession(
                    id = it.getLong(0),
                    title = it.getString(1) ?: "Тренировка",
                    startTime = it.getLong(2),
                    endTime = if (it.isNull(3)) null else it.getLong(3),
                    notes = it.getString(4) ?: ""
                )
            } else null
        }
        activeSessionFlow.value = session
        if (session != null) {
            refreshSetsForSession(session.id)
        } else {
            currentSessionSetsFlow.value = emptyList()
        }
    }

    private fun refreshCompletedSessions() {
        val list = mutableListOf<WorkoutSession>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, title, startTime, endTime, notes FROM workout_sessions WHERE endTime IS NOT NULL ORDER BY startTime DESC",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    WorkoutSession(
                        id = it.getLong(0),
                        title = it.getString(1) ?: "Тренировка",
                        startTime = it.getLong(2),
                        endTime = if (it.isNull(3)) null else it.getLong(3),
                        notes = it.getString(4) ?: ""
                    )
                )
            }
        }
        completedSessionsFlow.value = list
    }

    private fun refreshSetsForSession(sessionId: Long) {
        val list = mutableListOf<WorkoutSet>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, sessionId, exerciseId, variationId, setNumber, weightKg, reps, tags, intensityStatus, notes, isCompleted, timestamp FROM workout_sets WHERE sessionId = ? ORDER BY exerciseId ASC, setNumber ASC",
            arrayOf(sessionId.toString())
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    WorkoutSet(
                        id = it.getLong(0),
                        sessionId = it.getLong(1),
                        exerciseId = it.getLong(2),
                        variationId = if (it.isNull(3)) null else it.getLong(3),
                        setNumber = it.getInt(4),
                        weightKg = it.getFloat(5),
                        reps = it.getInt(6),
                        tags = it.getString(7) ?: "",
                        intensityStatus = it.getString(8) ?: "",
                        notes = it.getString(9) ?: "",
                        isCompleted = it.getInt(10) == 1,
                        timestamp = it.getLong(11)
                    )
                )
            }
        }
        currentObservedSessionId = sessionId
        currentSessionSetsFlow.value = list
    }

    private fun refreshFolders() {
        val list = mutableListOf<ProgramFolder>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id, name, description FROM program_folders ORDER BY name ASC", null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(ProgramFolder(it.getLong(0), it.getString(1), it.getString(2) ?: ""))
            }
        }
        foldersFlow.value = list
    }

    private fun refreshTemplates() {
        val list = mutableListOf<WorkoutTemplate>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id, folderId, name, description FROM workout_templates ORDER BY name ASC", null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    WorkoutTemplate(
                        id = it.getLong(0),
                        folderId = if (it.isNull(1)) null else it.getLong(1),
                        name = it.getString(2),
                        description = it.getString(3) ?: ""
                    )
                )
            }
        }
        templatesFlow.value = list
    }

    // --- DAO IMPLEMENTATION ---

    override fun getAllMuscleGroups(): Flow<List<MuscleGroup>> = muscleGroupsFlow.asStateFlow()

    override suspend fun insertMuscleGroup(name: String): Long = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply { put("name", name.trim()) }
        val id = writableDatabase.insertWithOnConflict("muscle_groups", null, cv, SQLiteDatabase.CONFLICT_IGNORE)
        refreshMuscleGroups()
        id
    }

    override suspend fun deleteMuscleGroup(id: Long) = withContext(Dispatchers.IO) {
        writableDatabase.delete("muscle_groups", "id = ?", arrayOf(id.toString()))
        refreshMuscleGroups()
        refreshExercises()
    }

    override fun getAllExercises(): Flow<List<Exercise>> = exercisesFlow.asStateFlow()

    override suspend fun insertExercise(exercise: Exercise): Long = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            put("name", exercise.name.trim())
            put("muscleGroupId", exercise.muscleGroupId)
            put("notes", exercise.notes.trim())
            put("icon", exercise.icon)
        }
        val id = writableDatabase.insert("exercises", null, cv)
        refreshExercises()
        id
    }

    override suspend fun updateExercise(exercise: Exercise) = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            put("name", exercise.name.trim())
            put("muscleGroupId", exercise.muscleGroupId)
            put("notes", exercise.notes.trim())
            put("icon", exercise.icon)
        }
        writableDatabase.update("exercises", cv, "id = ?", arrayOf(exercise.id.toString()))
        refreshExercises()
    }

    override suspend fun deleteExercise(id: Long) = withContext(Dispatchers.IO) {
        writableDatabase.delete("exercises", "id = ?", arrayOf(id.toString()))
        refreshExercises()
    }

    override fun getVariationsForExercise(exerciseId: Long): Flow<List<ExerciseVariation>> {
        val list = mutableListOf<ExerciseVariation>()
        val cursor = readableDatabase.rawQuery(
            "SELECT id, exerciseId, name FROM exercise_variations WHERE exerciseId = ? ORDER BY name ASC",
            arrayOf(exerciseId.toString())
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(ExerciseVariation(it.getLong(0), it.getLong(1), it.getString(2)))
            }
        }
        return MutableStateFlow(list).asStateFlow()
    }

    override suspend fun getVariationsList(exerciseId: Long): List<ExerciseVariation> = withContext(Dispatchers.IO) {
        val list = mutableListOf<ExerciseVariation>()
        val cursor = readableDatabase.rawQuery(
            "SELECT id, exerciseId, name FROM exercise_variations WHERE exerciseId = ? ORDER BY name ASC",
            arrayOf(exerciseId.toString())
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(ExerciseVariation(it.getLong(0), it.getLong(1), it.getString(2)))
            }
        }
        list
    }

    override suspend fun getAllVariations(): List<ExerciseVariation> = withContext(Dispatchers.IO) {
        val list = mutableListOf<ExerciseVariation>()
        val cursor = readableDatabase.rawQuery(
            "SELECT id, exerciseId, name FROM exercise_variations ORDER BY name ASC",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(ExerciseVariation(it.getLong(0), it.getLong(1), it.getString(2)))
            }
        }
        list
    }

    override suspend fun insertVariation(variation: ExerciseVariation): Long = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            put("exerciseId", variation.exerciseId)
            put("name", variation.name.trim())
        }
        writableDatabase.insert("exercise_variations", null, cv)
    }

    override suspend fun updateVariationName(id: Long, name: String) = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            put("name", name.trim())
        }
        writableDatabase.update("exercise_variations", cv, "id = ?", arrayOf(id.toString()))
        Unit
    }

    override suspend fun deleteVariation(id: Long): Unit = withContext(Dispatchers.IO) {
        writableDatabase.delete("exercise_variations", "id = ?", arrayOf(id.toString()))
        Unit
    }

    override fun getActiveSession(): Flow<WorkoutSession?> = activeSessionFlow.asStateFlow()

    override fun getCompletedSessions(): Flow<List<WorkoutSession>> = completedSessionsFlow.asStateFlow()

    override suspend fun createSession(title: String, startTime: Long, endTime: Long?, notes: String): Long = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            put("title", title.trim())
            put("startTime", startTime)
            if (endTime != null) put("endTime", endTime) else putNull("endTime")
            put("notes", notes.trim())
        }
        val id = writableDatabase.insert("workout_sessions", null, cv)
        refreshActiveSession()
        refreshCompletedSessions()
        id
    }

    override suspend fun updateSession(session: WorkoutSession) = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            put("title", session.title)
            put("startTime", session.startTime)
            if (session.endTime != null) put("endTime", session.endTime) else putNull("endTime")
            put("notes", session.notes)
        }
        writableDatabase.update("workout_sessions", cv, "id = ?", arrayOf(session.id.toString()))
        refreshActiveSession()
        refreshCompletedSessions()
    }

    override suspend fun updateSessionDetails(sessionId: Long, title: String, notes: String, startTime: Long, endTime: Long?) = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            put("title", title.trim())
            put("notes", notes.trim())
            put("startTime", startTime)
            if (endTime != null) put("endTime", endTime) else putNull("endTime")
        }
        writableDatabase.update("workout_sessions", cv, "id = ?", arrayOf(sessionId.toString()))
        refreshActiveSession()
        refreshCompletedSessions()
    }

    override suspend fun deleteSession(sessionId: Long) = withContext(Dispatchers.IO) {
        writableDatabase.delete("workout_sets", "sessionId = ?", arrayOf(sessionId.toString()))
        writableDatabase.delete("workout_sessions", "id = ?", arrayOf(sessionId.toString()))
        refreshActiveSession()
        refreshCompletedSessions()
    }

    override fun getSetsForSession(sessionId: Long): Flow<List<WorkoutSet>> {
        if (currentObservedSessionId != sessionId) {
            refreshSetsForSession(sessionId)
        }
        return currentSessionSetsFlow.asStateFlow()
    }

    override suspend fun getSetsForSessionSync(sessionId: Long): List<WorkoutSet> = withContext(Dispatchers.IO) {
        val list = mutableListOf<WorkoutSet>()
        val cursor = readableDatabase.rawQuery(
            "SELECT id, sessionId, exerciseId, variationId, setNumber, weightKg, reps, tags, intensityStatus, notes, isCompleted, timestamp FROM workout_sets WHERE sessionId = ? ORDER BY exerciseId ASC, setNumber ASC",
            arrayOf(sessionId.toString())
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    WorkoutSet(
                        id = it.getLong(0),
                        sessionId = it.getLong(1),
                        exerciseId = it.getLong(2),
                        variationId = if (it.isNull(3)) null else it.getLong(3),
                        setNumber = it.getInt(4),
                        weightKg = it.getFloat(5),
                        reps = it.getInt(6),
                        tags = it.getString(7) ?: "",
                        intensityStatus = it.getString(8) ?: "",
                        notes = it.getString(9) ?: "",
                        isCompleted = it.getInt(10) == 1,
                        timestamp = it.getLong(11)
                    )
                )
            }
        }
        list
    }

    override suspend fun insertSet(set: WorkoutSet): Long = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            put("sessionId", set.sessionId)
            put("exerciseId", set.exerciseId)
            if (set.variationId != null) put("variationId", set.variationId) else putNull("variationId")
            put("setNumber", set.setNumber)
            put("weightKg", set.weightKg)
            put("reps", set.reps)
            put("tags", set.tags)
            put("intensityStatus", set.intensityStatus)
            put("notes", set.notes)
            put("isCompleted", if (set.isCompleted) 1 else 0)
            put("timestamp", set.timestamp)
        }
        val id = writableDatabase.insert("workout_sets", null, cv)
        refreshSetsForSession(set.sessionId)
        id
    }

    override suspend fun updateSet(set: WorkoutSet) = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            if (set.variationId != null) put("variationId", set.variationId) else putNull("variationId")
            put("weightKg", set.weightKg)
            put("reps", set.reps)
            put("tags", set.tags)
            put("intensityStatus", set.intensityStatus)
            put("notes", set.notes)
            put("isCompleted", if (set.isCompleted) 1 else 0)
        }
        writableDatabase.update("workout_sets", cv, "id = ?", arrayOf(set.id.toString()))
        refreshSetsForSession(set.sessionId)
    }

    override suspend fun deleteSet(set: WorkoutSet) = withContext(Dispatchers.IO) {
        writableDatabase.delete("workout_sets", "id = ?", arrayOf(set.id.toString()))
        refreshSetsForSession(set.sessionId)
    }

    override suspend fun getLastSetForVariation(exerciseId: Long, variationId: Long?, excludeSessionId: Long): WorkoutSet? = withContext(Dispatchers.IO) {
        val query = if (variationId != null) {
            "SELECT id, sessionId, exerciseId, variationId, setNumber, weightKg, reps, tags, intensityStatus, notes, isCompleted, timestamp FROM workout_sets WHERE exerciseId = ? AND variationId = ? AND sessionId != ? ORDER BY timestamp DESC LIMIT 1"
        } else {
            "SELECT id, sessionId, exerciseId, variationId, setNumber, weightKg, reps, tags, intensityStatus, notes, isCompleted, timestamp FROM workout_sets WHERE exerciseId = ? AND variationId IS NULL AND sessionId != ? ORDER BY timestamp DESC LIMIT 1"
        }

        val args = if (variationId != null) {
            arrayOf(exerciseId.toString(), variationId.toString(), excludeSessionId.toString())
        } else {
            arrayOf(exerciseId.toString(), excludeSessionId.toString())
        }

        val cursor = readableDatabase.rawQuery(query, args)
        cursor.use {
            if (it.moveToNext()) {
                WorkoutSet(
                    id = it.getLong(0),
                    sessionId = it.getLong(1),
                    exerciseId = it.getLong(2),
                    variationId = if (it.isNull(3)) null else it.getLong(3),
                    setNumber = it.getInt(4),
                    weightKg = it.getFloat(5),
                    reps = it.getInt(6),
                    tags = it.getString(7) ?: "",
                    intensityStatus = it.getString(8) ?: "",
                    notes = it.getString(9) ?: "",
                    isCompleted = it.getInt(10) == 1,
                    timestamp = it.getLong(11)
                )
            } else null
        }
    }

    override suspend fun getExerciseHistory(exerciseId: Long, variationId: Long?): List<ExerciseHistoryDay> = withContext(Dispatchers.IO) {
        val query = if (variationId != null) {
            """
                SELECT s.id, s.title, s.startTime, ws.id, ws.sessionId, ws.exerciseId, ws.variationId, ws.setNumber, ws.weightKg, ws.reps, ws.tags, ws.intensityStatus, ws.notes, ws.isCompleted, ws.timestamp 
                FROM workout_sets ws
                INNER JOIN workout_sessions s ON ws.sessionId = s.id
                WHERE ws.exerciseId = ? AND ws.variationId = ?
                ORDER BY s.startTime DESC, ws.setNumber ASC
            """.trimIndent()
        } else {
            """
                SELECT s.id, s.title, s.startTime, ws.id, ws.sessionId, ws.exerciseId, ws.variationId, ws.setNumber, ws.weightKg, ws.reps, ws.tags, ws.intensityStatus, ws.notes, ws.isCompleted, ws.timestamp 
                FROM workout_sets ws
                INNER JOIN workout_sessions s ON ws.sessionId = s.id
                WHERE ws.exerciseId = ? AND ws.variationId IS NULL
                ORDER BY s.startTime DESC, ws.setNumber ASC
            """.trimIndent()
        }

        val args = if (variationId != null) arrayOf(exerciseId.toString(), variationId.toString()) else arrayOf(exerciseId.toString())
        val cursor = readableDatabase.rawQuery(query, args)

        val dayMap = linkedMapOf<Long, MutableList<WorkoutSet>>()
        val dayMeta = mutableMapOf<Long, Pair<String, Long>>()

        cursor.use {
            while (it.moveToNext()) {
                val sId = it.getLong(0)
                val sTitle = it.getString(1) ?: "Тренировка"
                val sTime = it.getLong(2)
                dayMeta[sId] = Pair(sTitle, sTime)

                val set = WorkoutSet(
                    id = it.getLong(3),
                    sessionId = it.getLong(4),
                    exerciseId = it.getLong(5),
                    variationId = if (it.isNull(6)) null else it.getLong(6),
                    setNumber = it.getInt(7),
                    weightKg = it.getFloat(8),
                    reps = it.getInt(9),
                    tags = it.getString(10) ?: "",
                    intensityStatus = it.getString(11) ?: "",
                    notes = it.getString(12) ?: "",
                    isCompleted = it.getInt(13) == 1,
                    timestamp = it.getLong(14)
                )
                dayMap.getOrPut(sId) { mutableListOf() }.add(set)
            }
        }

        dayMap.map { (sId, sets) ->
            val meta = dayMeta[sId] ?: Pair("Тренировка", 0L)
            ExerciseHistoryDay(
                sessionId = sId,
                sessionTitle = meta.first,
                timestamp = meta.second,
                sets = sets
            )
        }
    }

    // --- FOLDERS ---

    override fun getAllFolders(): Flow<List<ProgramFolder>> = foldersFlow.asStateFlow()

    override suspend fun insertFolder(name: String, description: String): Long = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            put("name", name.trim())
            put("description", description.trim())
        }
        val id = writableDatabase.insert("program_folders", null, cv)
        refreshFolders()
        id
    }

    override suspend fun deleteFolder(folderId: Long) = withContext(Dispatchers.IO) {
        val templatesInFolder = templatesFlow.value.filter { it.folderId == folderId }
        for (t in templatesInFolder) {
            writableDatabase.delete("workout_template_items", "templateId = ?", arrayOf(t.id.toString()))
        }
        writableDatabase.delete("workout_templates", "folderId = ?", arrayOf(folderId.toString()))
        writableDatabase.delete("program_folders", "id = ?", arrayOf(folderId.toString()))
        refreshFolders()
        refreshTemplates()
    }

    // --- TEMPLATES ---

    override fun getAllTemplates(): Flow<List<WorkoutTemplate>> = templatesFlow.asStateFlow()

    override fun getTemplatesForFolder(folderId: Long?): Flow<List<WorkoutTemplate>> {
        val list = if (folderId == null) {
            templatesFlow.value.filter { it.folderId == null }
        } else {
            templatesFlow.value.filter { it.folderId == folderId }
        }
        return MutableStateFlow(list).asStateFlow()
    }

    override suspend fun getTemplateItems(templateId: Long): List<WorkoutTemplateItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<WorkoutTemplateItem>()
        val cursor = readableDatabase.rawQuery(
            "SELECT id, templateId, exerciseId, variationId, targetSets, orderIndex FROM workout_template_items WHERE templateId = ? ORDER BY orderIndex ASC",
            arrayOf(templateId.toString())
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    WorkoutTemplateItem(
                        id = it.getLong(0),
                        templateId = it.getLong(1),
                        exerciseId = it.getLong(2),
                        variationId = if (it.isNull(3)) null else it.getLong(3),
                        targetSets = it.getInt(4),
                        orderIndex = it.getInt(5)
                    )
                )
            }
        }
        list
    }

    override suspend fun createTemplate(name: String, description: String, folderId: Long?, items: List<WorkoutTemplateItem>): Long = withContext(Dispatchers.IO) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val cv = ContentValues().apply {
                if (folderId != null) put("folderId", folderId) else putNull("folderId")
                put("name", name.trim())
                put("description", description.trim())
            }
            val tId = db.insert("workout_templates", null, cv)
            items.forEachIndexed { index, item ->
                val iCv = ContentValues().apply {
                    put("templateId", tId)
                    put("exerciseId", item.exerciseId)
                    if (item.variationId != null) put("variationId", item.variationId) else putNull("variationId")
                    put("targetSets", item.targetSets)
                    put("orderIndex", index)
                }
                db.insert("workout_template_items", null, iCv)
            }
            db.setTransactionSuccessful()
            refreshTemplates()
            tId
        } finally {
            db.endTransaction()
        }
    }

    override suspend fun updateFolder(folderId: Long, name: String, description: String) = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            put("name", name.trim())
            put("description", description.trim())
        }
        writableDatabase.update("program_folders", cv, "id = ?", arrayOf(folderId.toString()))
        refreshFolders()
    }

    override suspend fun updateTemplate(templateId: Long, name: String, description: String) = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            put("name", name.trim())
            put("description", description.trim())
        }
        writableDatabase.update("workout_templates", cv, "id = ?", arrayOf(templateId.toString()))
        refreshTemplates()
    }

    override suspend fun addExerciseToTemplate(templateId: Long, exerciseId: Long, variationId: Long?, targetSets: Int): Unit = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            put("templateId", templateId)
            put("exerciseId", exerciseId)
            if (variationId != null) put("variationId", variationId) else putNull("variationId")
            put("targetSets", targetSets)
        }
        writableDatabase.insert("workout_template_items", null, cv)
        Unit
    }

    override suspend fun removeExerciseFromTemplate(templateId: Long, exerciseId: Long): Unit = withContext(Dispatchers.IO) {
        writableDatabase.delete("workout_template_items", "templateId = ? AND exerciseId = ?", arrayOf(templateId.toString(), exerciseId.toString()))
        Unit
    }

    override suspend fun reorderTemplateItems(templateId: Long, items: List<WorkoutTemplateItem>): Unit = withContext(Dispatchers.IO) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            items.forEachIndexed { index, item ->
                val cv = ContentValues().apply {
                    put("orderIndex", index)
                }
                db.update("workout_template_items", cv, "id = ?", arrayOf(item.id.toString()))
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        refreshTemplates()
    }

    override suspend fun deleteTemplate(templateId: Long) = withContext(Dispatchers.IO) {
        writableDatabase.delete("workout_template_items", "templateId = ?", arrayOf(templateId.toString()))
        writableDatabase.delete("workout_templates", "id = ?", arrayOf(templateId.toString()))
        refreshTemplates()
    }

    override suspend fun getAllSets(): List<WorkoutSet> = withContext(Dispatchers.IO) {
        val list = mutableListOf<WorkoutSet>()
        val cursor = readableDatabase.rawQuery(
            "SELECT id, sessionId, exerciseId, variationId, setNumber, weightKg, reps, tags, intensityStatus, notes, isCompleted, timestamp FROM workout_sets ORDER BY sessionId ASC, setNumber ASC",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    WorkoutSet(
                        id = it.getLong(0),
                        sessionId = it.getLong(1),
                        exerciseId = it.getLong(2),
                        variationId = if (it.isNull(3)) null else it.getLong(3),
                        setNumber = it.getInt(4),
                        weightKg = it.getFloat(5),
                        reps = it.getInt(6),
                        tags = it.getString(7) ?: "",
                        intensityStatus = it.getString(8) ?: "",
                        notes = it.getString(9) ?: "",
                        isCompleted = it.getInt(10) == 1,
                        timestamp = it.getLong(11)
                    )
                )
            }
        }
        list
    }

    override suspend fun getAllMuscleGroupsSync(): List<MuscleGroup> = withContext(Dispatchers.IO) {
        val list = mutableListOf<MuscleGroup>()
        val cursor = readableDatabase.rawQuery("SELECT id, name FROM muscle_groups ORDER BY name ASC", null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(MuscleGroup(it.getLong(0), it.getString(1)))
            }
        }
        list
    }

    override suspend fun getAllExercisesSync(): List<Exercise> = withContext(Dispatchers.IO) {
        val list = mutableListOf<Exercise>()
        val cursor = readableDatabase.rawQuery("SELECT id, name, muscleGroupId, notes, icon FROM exercises ORDER BY name ASC", null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    Exercise(
                        id = it.getLong(0),
                        name = it.getString(1),
                        muscleGroupId = it.getLong(2),
                        notes = it.getString(3) ?: "",
                        icon = it.getString(4) ?: "dumbbell"
                    )
                )
            }
        }
        list
    }

    override suspend fun getAllFoldersSync(): List<ProgramFolder> = withContext(Dispatchers.IO) {
        val list = mutableListOf<ProgramFolder>()
        val cursor = readableDatabase.rawQuery("SELECT id, name, description FROM program_folders ORDER BY id ASC", null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(ProgramFolder(it.getLong(0), it.getString(1), it.getString(2) ?: ""))
            }
        }
        list
    }

    override suspend fun getAllTemplatesSync(): List<WorkoutTemplate> = withContext(Dispatchers.IO) {
        val list = mutableListOf<WorkoutTemplate>()
        val cursor = readableDatabase.rawQuery("SELECT id, folderId, name, description FROM workout_templates ORDER BY id ASC", null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    WorkoutTemplate(
                        id = it.getLong(0),
                        folderId = if (it.isNull(1)) null else it.getLong(1),
                        name = it.getString(2),
                        description = it.getString(3) ?: ""
                    )
                )
            }
        }
        list
    }

    override suspend fun getAllTemplateItemsSync(): List<WorkoutTemplateItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<WorkoutTemplateItem>()
        val cursor = readableDatabase.rawQuery("SELECT id, templateId, exerciseId, variationId, targetSets, orderIndex FROM workout_template_items ORDER BY templateId ASC, orderIndex ASC", null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    WorkoutTemplateItem(
                        id = it.getLong(0),
                        templateId = it.getLong(1),
                        exerciseId = it.getLong(2),
                        variationId = if (it.isNull(3)) null else it.getLong(3),
                        targetSets = it.getInt(4),
                        orderIndex = it.getInt(5)
                    )
                )
            }
        }
        list
    }

    override suspend fun getAllSessionsSync(): List<WorkoutSession> = withContext(Dispatchers.IO) {
        val list = mutableListOf<WorkoutSession>()
        val cursor = readableDatabase.rawQuery("SELECT id, title, startTime, endTime, notes FROM workout_sessions ORDER BY startTime DESC", null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    WorkoutSession(
                        id = it.getLong(0),
                        title = it.getString(1) ?: "Тренировка",
                        startTime = it.getLong(2),
                        endTime = if (it.isNull(3)) null else it.getLong(3),
                        notes = it.getString(4) ?: ""
                    )
                )
            }
        }
        list
    }

    override suspend fun restoreDatabaseFromBackup(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        val root = try {
            JSONObject(jsonString)
        } catch (e: Exception) {
            return@withContext false
        }

        val db = writableDatabase
        db.beginTransaction()
        try {
            db.execSQL("PRAGMA foreign_keys = OFF")

            // 1. Очистка старых данных
            db.delete("workout_sets", null, null)
            db.delete("workout_template_items", null, null)
            db.delete("workout_templates", null, null)
            db.delete("program_folders", null, null)
            db.delete("workout_sessions", null, null)
            db.delete("exercise_variations", null, null)
            db.delete("exercises", null, null)
            db.delete("muscle_groups", null, null)

            // 2. Группы мышц
            val groupsArray = root.optJSONArray("muscleGroups")
            if (groupsArray != null) {
                for (i in 0 until groupsArray.length()) {
                    val obj = groupsArray.getJSONObject(i)
                    val cv = ContentValues().apply {
                        put("id", obj.getLong("id"))
                        put("name", obj.getString("name"))
                    }
                    db.insert("muscle_groups", null, cv)
                }
            }

            // 3. Упражнения
            val exercisesArray = root.optJSONArray("exercises")
            if (exercisesArray != null) {
                for (i in 0 until exercisesArray.length()) {
                    val obj = exercisesArray.getJSONObject(i)
                    val cv = ContentValues().apply {
                        put("id", obj.getLong("id"))
                        put("name", obj.getString("name"))
                        put("muscleGroupId", obj.getLong("muscleGroupId"))
                        put("notes", obj.optString("notes", ""))
                        put("icon", obj.optString("icon", "dumbbell"))
                    }
                    db.insert("exercises", null, cv)
                }
            }

            // 4. Фишки
            val variationsArray = root.optJSONArray("variations")
            if (variationsArray != null) {
                for (i in 0 until variationsArray.length()) {
                    val obj = variationsArray.getJSONObject(i)
                    val cv = ContentValues().apply {
                        put("id", obj.getLong("id"))
                        put("exerciseId", obj.getLong("exerciseId"))
                        put("name", obj.getString("name"))
                    }
                    db.insert("exercise_variations", null, cv)
                }
            }

            // 5. Папки
            val foldersArray = root.optJSONArray("folders")
            if (foldersArray != null) {
                for (i in 0 until foldersArray.length()) {
                    val obj = foldersArray.getJSONObject(i)
                    val cv = ContentValues().apply {
                        put("id", obj.getLong("id"))
                        put("name", obj.getString("name"))
                        put("description", obj.optString("description", ""))
                    }
                    db.insert("program_folders", null, cv)
                }
            }

            // 6. Шаблоны / программы
            val templatesArray = root.optJSONArray("templates")
            if (templatesArray != null) {
                for (i in 0 until templatesArray.length()) {
                    val obj = templatesArray.getJSONObject(i)
                    val cv = ContentValues().apply {
                        put("id", obj.getLong("id"))
                        if (obj.has("folderId") && !obj.isNull("folderId")) {
                            put("folderId", obj.getLong("folderId"))
                        } else {
                            putNull("folderId")
                        }
                        put("name", obj.getString("name"))
                        put("description", obj.optString("description", ""))
                    }
                    db.insert("workout_templates", null, cv)
                }
            }

            // 7. Элементы шаблонов
            val templateItemsArray = root.optJSONArray("templateItems")
            if (templateItemsArray != null) {
                for (i in 0 until templateItemsArray.length()) {
                    val obj = templateItemsArray.getJSONObject(i)
                    val cv = ContentValues().apply {
                        put("id", obj.getLong("id"))
                        put("templateId", obj.getLong("templateId"))
                        put("exerciseId", obj.getLong("exerciseId"))
                        if (obj.has("variationId") && !obj.isNull("variationId")) {
                            put("variationId", obj.getLong("variationId"))
                        } else {
                            putNull("variationId")
                        }
                        put("targetSets", obj.optInt("targetSets", 3))
                        put("orderIndex", obj.optInt("orderIndex", 0))
                    }
                    db.insert("workout_template_items", null, cv)
                }
            }

            // 8. Сессии тренировок
            val sessionsArray = root.optJSONArray("sessions")
            if (sessionsArray != null) {
                for (i in 0 until sessionsArray.length()) {
                    val obj = sessionsArray.getJSONObject(i)
                    val cv = ContentValues().apply {
                        put("id", obj.getLong("id"))
                        put("title", obj.optString("title", "Тренировка"))
                        put("startTime", obj.getLong("startTime"))
                        if (obj.has("endTime") && !obj.isNull("endTime")) {
                            put("endTime", obj.getLong("endTime"))
                        } else {
                            putNull("endTime")
                        }
                        put("notes", obj.optString("notes", ""))
                    }
                    db.insert("workout_sessions", null, cv)
                }
            }

            // 9. Подходы
            val setsArray = root.optJSONArray("sets")
            if (setsArray != null) {
                for (i in 0 until setsArray.length()) {
                    val obj = setsArray.getJSONObject(i)
                    val cv = ContentValues().apply {
                        put("id", obj.getLong("id"))
                        put("sessionId", obj.getLong("sessionId"))
                        put("exerciseId", obj.getLong("exerciseId"))
                        if (obj.has("variationId") && !obj.isNull("variationId")) {
                            put("variationId", obj.getLong("variationId"))
                        } else {
                            putNull("variationId")
                        }
                        put("setNumber", obj.getInt("setNumber"))
                        put("weightKg", obj.getDouble("weightKg").toFloat())
                        put("reps", obj.getInt("reps"))
                        put("tags", obj.optString("tags", ""))
                        put("intensityStatus", obj.optString("intensityStatus", ""))
                        put("notes", obj.optString("notes", ""))
                        put("isCompleted", if (obj.optBoolean("isCompleted", true)) 1 else 0)
                        put("timestamp", obj.optLong("timestamp", System.currentTimeMillis()))
                    }
                    db.insert("workout_sets", null, cv)
                }
            }

            // Sanitize any potential orphaned records from malformed backup
            db.execSQL("DELETE FROM workout_sets WHERE sessionId NOT IN (SELECT id FROM workout_sessions)")
            db.execSQL("DELETE FROM workout_template_items WHERE templateId NOT IN (SELECT id FROM workout_templates)")
            db.execSQL("DELETE FROM workout_templates WHERE folderId IS NOT NULL AND folderId NOT IN (SELECT id FROM program_folders)")
            db.execSQL("PRAGMA foreign_keys = ON")
            db.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.endTransaction()
            refreshAll()
        }
    }
}


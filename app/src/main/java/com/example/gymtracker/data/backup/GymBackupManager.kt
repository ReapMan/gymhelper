package com.example.gymtracker.data.backup

import android.content.Context
import com.example.gymtracker.data.dao.GymDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object GymBackupManager {

    suspend fun createFullBackupJson(gymDao: GymDao): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("app", "GymHelper")
        root.put("timestamp", System.currentTimeMillis())

        // 1. Muscle groups
        val groupsArray = JSONArray()
        for (g in gymDao.getAllMuscleGroupsSync()) {
            val obj = JSONObject()
            obj.put("id", g.id)
            obj.put("name", g.name)
            groupsArray.put(obj)
        }
        root.put("muscleGroups", groupsArray)

        // 2. Exercises
        val exercisesArray = JSONArray()
        for (e in gymDao.getAllExercisesSync()) {
            val obj = JSONObject()
            obj.put("id", e.id)
            obj.put("name", e.name)
            obj.put("muscleGroupId", e.muscleGroupId)
            obj.put("notes", e.notes)
            obj.put("icon", e.icon)
            exercisesArray.put(obj)
        }
        root.put("exercises", exercisesArray)

        // 3. Variations (Фишки)
        val variationsArray = JSONArray()
        for (v in gymDao.getAllVariations()) {
            val obj = JSONObject()
            obj.put("id", v.id)
            obj.put("exerciseId", v.exerciseId)
            obj.put("name", v.name)
            variationsArray.put(obj)
        }
        root.put("variations", variationsArray)

        // 4. Folders
        val foldersArray = JSONArray()
        for (f in gymDao.getAllFoldersSync()) {
            val obj = JSONObject()
            obj.put("id", f.id)
            obj.put("name", f.name)
            obj.put("description", f.description)
            foldersArray.put(obj)
        }
        root.put("folders", foldersArray)

        // 5. Templates
        val templatesArray = JSONArray()
        for (t in gymDao.getAllTemplatesSync()) {
            val obj = JSONObject()
            obj.put("id", t.id)
            if (t.folderId != null) obj.put("folderId", t.folderId) else obj.put("folderId", JSONObject.NULL)
            obj.put("name", t.name)
            obj.put("description", t.description)
            templatesArray.put(obj)
        }
        root.put("templates", templatesArray)

        // 6. Template items
        val templateItemsArray = JSONArray()
        for (ti in gymDao.getAllTemplateItemsSync()) {
            val obj = JSONObject()
            obj.put("id", ti.id)
            obj.put("templateId", ti.templateId)
            obj.put("exerciseId", ti.exerciseId)
            if (ti.variationId != null) obj.put("variationId", ti.variationId) else obj.put("variationId", JSONObject.NULL)
            obj.put("targetSets", ti.targetSets)
            obj.put("orderIndex", ti.orderIndex)
            templateItemsArray.put(obj)
        }
        root.put("templateItems", templateItemsArray)

        // 7. Sessions
        val sessionsArray = JSONArray()
        for (s in gymDao.getAllSessionsSync()) {
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("title", s.title)
            obj.put("startTime", s.startTime)
            if (s.endTime != null) obj.put("endTime", s.endTime) else obj.put("endTime", JSONObject.NULL)
            obj.put("notes", s.notes)
            sessionsArray.put(obj)
        }
        root.put("sessions", sessionsArray)

        // 8. Sets
        val setsArray = JSONArray()
        for (ws in gymDao.getAllSets()) {
            val obj = JSONObject()
            obj.put("id", ws.id)
            obj.put("sessionId", ws.sessionId)
            obj.put("exerciseId", ws.exerciseId)
            if (ws.variationId != null) obj.put("variationId", ws.variationId) else obj.put("variationId", JSONObject.NULL)
            obj.put("setNumber", ws.setNumber)
            obj.put("weightKg", ws.weightKg)
            obj.put("reps", ws.reps)
            obj.put("tags", ws.tags)
            obj.put("intensityStatus", ws.intensityStatus)
            obj.put("notes", ws.notes)
            obj.put("isCompleted", ws.isCompleted)
            obj.put("timestamp", ws.timestamp)
            setsArray.put(obj)
        }
        root.put("sets", setsArray)

        root.toString(2)
    }

    suspend fun saveAutoBackup(context: Context, gymDao: GymDao) = withContext(Dispatchers.IO) {
        try {
            val json = createFullBackupJson(gymDao)
            val file = File(context.filesDir, "gym_backup_auto.json")
            file.writeText(json)
        } catch (_: Exception) {}
    }
}

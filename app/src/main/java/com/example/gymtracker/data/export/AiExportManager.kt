package com.example.gymtracker.data.export

import com.example.gymtracker.data.model.Exercise
import com.example.gymtracker.data.model.ExerciseVariation
import com.example.gymtracker.data.model.WorkoutSession
import com.example.gymtracker.data.model.WorkoutSet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AiExportManager {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun generateMarkdown(
        sessions: List<WorkoutSession>,
        allSets: List<WorkoutSet>,
        exercises: List<Exercise>,
        variations: List<ExerciseVariation>
    ): String {
        val exMap = exercises.associateBy { it.id }
        val varMap = variations.associateBy { it.id }
        val setsBySession = allSets.groupBy { it.sessionId }

        val sb = StringBuilder()
        sb.appendLine("# ЖУРНАЛ ТРЕНИРОВОК ДЛЯ ИИ-АНАЛИЗА")
        sb.appendLine("> Дата формирования выгрузки: ${dateFormat.format(Date())}")
        sb.appendLine("> Всего завершенных тренировок: ${sessions.size}")
        sb.appendLine()
        sb.appendLine("---")
        sb.appendLine()

        for (session in sessions.sortedBy { it.startTime }) {
            val dateStr = dateFormat.format(Date(session.startTime))
            val durationMin = if (session.endTime != null) {
                ((session.endTime - session.startTime) / 60000L).coerceAtLeast(1)
            } else 0

            sb.appendLine("## 🏋️ ${session.title}")
            sb.appendLine("- **Дата**: $dateStr")
            if (durationMin > 0) {
                sb.appendLine("- **Длительность**: $durationMin мин")
            }
            if (session.notes.isNotBlank()) {
                sb.appendLine("- **Заметка к тренировке**: ${session.notes}")
            }
            sb.appendLine()

            val sessionSets = setsBySession[session.id] ?: emptyList()
            if (sessionSets.isEmpty()) {
                sb.appendLine("*Подходы не зафиксированы.*")
                sb.appendLine()
            } else {
                val setsByExercise = sessionSets.groupBy { it.exerciseId }
                for ((exId, sets) in setsByExercise) {
                    val ex = exMap[exId]
                    val exName = ex?.name ?: "Упражнение #$exId"
                    sb.appendLine("### $exName")
                    if (ex != null && ex.notes.isNotBlank()) {
                        sb.appendLine("  *Настройки тренажера / памятка: ${ex.notes}*")
                    }

                    for (s in sets.sortedBy { it.setNumber }) {
                        val vName = s.variationId?.let { varMap[it]?.name }
                        val tagsList = mutableListOf<String>()
                        if (!vName.isNullOrBlank()) tagsList.add("Фишка: $vName")
                        if (s.tags.isNotBlank()) tagsList.add("Теги: ${s.tags}")
                        if (s.intensityStatus.isNotBlank()) tagsList.add("Интенсивность: ${s.intensityStatus}")
                        if (s.notes.isNotBlank()) tagsList.add("Заметка: ${s.notes}")

                        val meta = if (tagsList.isNotEmpty()) " [${tagsList.joinToString(" | ")}]" else ""
                        val weightStr = if (s.weightKg % 1.0f == 0.0f) "${s.weightKg.toInt()}" else "${s.weightKg}"
                        sb.appendLine("- Подход ${s.setNumber}: ${weightStr} кг × ${s.reps} повт.$meta")
                    }
                    sb.appendLine()
                }
            }
            sb.appendLine("---")
            sb.appendLine()
        }

        return sb.toString()
    }

    fun generateJson(
        sessions: List<WorkoutSession>,
        allSets: List<WorkoutSet>,
        exercises: List<Exercise>,
        variations: List<ExerciseVariation>
    ): String {
        val exMap = exercises.associateBy { it.id }
        val varMap = variations.associateBy { it.id }
        val setsBySession = allSets.groupBy { it.sessionId }

        val sb = StringBuilder()
        sb.append("[\n")
        val sessionCount = sessions.size
        sessions.sortedBy { it.startTime }.forEachIndexed { sIdx, session ->
            val dateStr = dateFormat.format(Date(session.startTime))
            val durationMin = if (session.endTime != null) {
                ((session.endTime - session.startTime) / 60000L).coerceAtLeast(1)
            } else 0
            val sessionSets = setsBySession[session.id] ?: emptyList()

            sb.append("  {\n")
            sb.append("    \"id\": ${session.id},\n")
            sb.append("    \"title\": \"${escapeJson(session.title)}\",\n")
            sb.append("    \"date\": \"$dateStr\",\n")
            sb.append("    \"durationMinutes\": $durationMin,\n")
            sb.append("    \"notes\": \"${escapeJson(session.notes)}\",\n")
            sb.append("    \"exercises\": [\n")

            val setsByExercise = sessionSets.groupBy { it.exerciseId }
            val exEntries = setsByExercise.entries.toList()
            exEntries.forEachIndexed { eIdx, (exId, sets) ->
                val ex = exMap[exId]
                val exName = ex?.name ?: "Упражнение #$exId"
                sb.append("      {\n")
                sb.append("        \"exerciseName\": \"${escapeJson(exName)}\",\n")
                sb.append("        \"cueNotes\": \"${escapeJson(ex?.notes ?: "")}\",\n")
                sb.append("        \"sets\": [\n")

                sets.sortedBy { it.setNumber }.forEachIndexed { setIdx, s ->
                    val vName = s.variationId?.let { varMap[it]?.name } ?: ""
                    sb.append("          {\n")
                    sb.append("            \"setNumber\": ${s.setNumber},\n")
                    sb.append("            \"weightKg\": ${s.weightKg},\n")
                    sb.append("            \"reps\": ${s.reps},\n")
                    sb.append("            \"variation\": \"${escapeJson(vName)}\",\n")
                    sb.append("            \"tags\": \"${escapeJson(s.tags)}\",\n")
                    sb.append("            \"intensity\": \"${escapeJson(s.intensityStatus)}\",\n")
                    sb.append("            \"notes\": \"${escapeJson(s.notes)}\"\n")
                    sb.append("          }${if (setIdx < sets.size - 1) "," else ""}\n")
                }

                sb.append("        ]\n")
                sb.append("      }${if (eIdx < exEntries.size - 1) "," else ""}\n")
            }

            sb.append("    ]\n")
            sb.append("  }${if (sIdx < sessionCount - 1) "," else ""}\n")
        }
        sb.append("]\n")
        return sb.toString()
    }

    private fun escapeJson(str: String): String {
        return str
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}

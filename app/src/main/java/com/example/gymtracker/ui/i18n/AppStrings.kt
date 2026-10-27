package com.example.gymtracker.ui.i18n

import androidx.compose.runtime.staticCompositionLocalOf

data class AppStrings(
    // Bottom Navigation
    val navWorkout: String,
    val navPrograms: String,
    val navHistory: String,
    val navMenu: String,

    // Live Workout & Roadmap
    val startWorkout: String,
    val startLiveSubtitle: String,
    val startLiveButton: String,
    val startFromTemplateButton: String,
    val recordPastButton: String,
    val workoutPlanEmpty: String,
    val workoutPlanEmptyDesc: String,
    val selectExercise: String,
    val addExercise: String,
    val finishWorkout: String,
    val finishWorkoutDialogTitle: String,
    val finishWorkoutDialogMsg: String,
    val workoutNotesPlaceholder: String,
    val finishBtn: String,
    val cancelBtn: String,
    val deleteExerciseConfirmTitle: String,
    val deleteExerciseConfirmMsg: String,
    val replaceExercise: String,
    val deleteFromPlan: String,
    val moveUp: String,
    val moveDown: String,
    val defaultWorkoutTitle: String,
    val totalDuration: String,
    val renameWorkout: String,
    val renameWorkoutPlaceholder: String,
    val saveBtn: String,

    // Minimized Banner & Status
    val workoutRunning: String,
    val returnToWorkout: String,

    // Focus View (Active Exercise)
    val toRoadmap: String,
    val exitFocus: String,
    val setLabel: String,
    val repsLabel: String,
    val weightLabel: String,
    val kgUnit: String,
    val repsUnit: String,
    val startSet: String,
    val finishSet: String,
    val recordedSets: String,
    val noSetsYet: String,
    val noSetsYetDesc: String,
    val restTimer: String,
    val restTimerRunning: String,
    val prevWorkoutHistory: String,
    val noHistoryYet: String,
    val editNotes: String,
    val notesSaved: String,
    val variationsTitle: String,
    val standardVariation: String,
    val addVariationBtn: String,
    val addVariationTitle: String,
    val variationNamePlaceholder: String,

    // Set Types & Tags
    val setTypeNormal: String,
    val setTypeWarmup: String,
    val setTypeDrop: String,
    val setTypeFailure: String,
    val tagEasy: String,
    val tagFailure: String,
    val tagWarmup: String,
    val tagDrop: String,

    // Edit Set Dialog
    val editSetTitle: String,
    val deleteSetConfirmTitle: String,
    val deleteSetConfirmMsg: String,
    val deleteBtn: String,

    // Exercise Picker
    val chooseExerciseTitle: String,
    val searchPlaceholder: String,
    val allMuscleGroups: String,
    val createCustomExercise: String,
    val createCustomExerciseShort: String,
    val addSelectedCount: String,
    val noExercisesFound: String,
    val newExerciseNamePlaceholder: String,
    val selectMuscleGroup: String,
    val createAndAddBtn: String,

    // Templates / Programs
    val programsTitle: String,
    val newFolderBtn: String,
    val newProgramBtn: String,
    val startThisWorkout: String,
    val noProgramsYet: String,
    val editProgramTitle: String,
    val deleteProgramConfirmTitle: String,
    val exercisesInPlan: String,

    // History
    val historyTitle: String,
    val totalWorkouts: String,
    val noHistoryTitle: String,
    val noHistoryDesc: String,
    val exportForAi: String,

    // Menu & Settings
    val menuTitle: String,
    val exerciseDatabase: String,
    val exerciseDatabaseDesc: String,
    val aiExportTitle: String,
    val aiExportDesc: String,
    val backupRestoreTitle: String,
    val backupRestoreDesc: String,
    val settingsTitle: String,
    val settingsDesc: String,
    val colorSchemeTitle: String,
    val colorSchemeDesc: String,
    val languageSettingTitle: String,
    val languageSettingDesc: String,
    val notificationSettingTitle: String,
    val notificationSettingDesc: String,
    val creatorsLabel: String,
    val appVersionLabel: String,

    // Notification Texts
    val notifChannelName: String,
    val notifWorkoutActive: String,
    val notifRestTime: String,
    val notifOpenApp: String,

    // Muscle Groups
    val mgChest: String,
    val mgBack: String,
    val mgLegs: String,
    val mgShoulders: String,
    val mgBiceps: String,
    val mgTriceps: String,
    val mgAbs: String,
    val mgOther: String
)

val LocalAppStrings = staticCompositionLocalOf<AppStrings> {
    error("No AppStrings provided")
}

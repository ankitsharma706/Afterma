package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.entities.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CycleEntry(
    val date: String,
    val mood: String,
    val flow: String,
    val sleep: String,
    val ovulation: Boolean,
    val medication: Boolean,
    val painIntensity: Int,
    val energyVitality: Int,
    val crampsSeverity: Int,
    val waterIntake: String,
    val symptoms: List<String>,
    val notes: String
)

data class LactationLog(
    val timestamp: String,
    val type: String,
    val side: String,
    val quantityMl: Int,
    val durationMin: Int,
    val response: String
)

data class CommunityPost(
    val id: Int,
    val authorName: String,
    val weeksPostpartum: String,
    val tag: String,
    val content: String,
    val likesCount: Int,
    val commentsCount: Int
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val moodEntryDao = database.moodEntryDao()
    private val therapySessionDao = database.therapySessionDao()
    private val recipeDao = database.recipeDao()
    private val learningArticleDao = database.learningArticleDao()
    private val mindfulnessSessionDao = database.mindfulnessSessionDao()

    // Authentication & Onboarding
    private val _currentUserEmail = MutableStateFlow("")
    val currentUserEmail: StateFlow<String> = _currentUserEmail.asStateFlow()

    private val _currentUserName = MutableStateFlow("")
    val currentUserName: StateFlow<String> = _currentUserName.asStateFlow()

    private val _userLoggedIn = MutableStateFlow(false)
    val userLoggedIn: StateFlow<Boolean> = _userLoggedIn.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(false)
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    // Database states exposed as StateFlows
    val moodEntries: StateFlow<List<MoodEntry>> = moodEntryDao.getAllMoodEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val therapySessions: StateFlow<List<TherapySession>> = therapySessionDao.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recipes: StateFlow<List<RecipeEntity>> = recipeDao.getAllRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteRecipes: StateFlow<List<RecipeEntity>> = recipeDao.getFavoriteRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val articles: StateFlow<List<LearningArticle>> = learningArticleDao.getAllArticles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mindfulnessHistory: StateFlow<List<MindfulnessSession>> = mindfulnessSessionDao.getAllMindfulnessSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reactively synchronized recovery progress index
    val healingProgressIndex: StateFlow<Int> = moodEntries
        .map { list -> list.firstOrNull()?.recoveryProgress ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // In-memory states
    private val _cycleEntries = MutableStateFlow<List<CycleEntry>>(emptyList())
    val cycleEntries: StateFlow<List<CycleEntry>> = _cycleEntries.asStateFlow()

    private val _lactationLogs = MutableStateFlow<List<LactationLog>>(emptyList())
    val lactationLogs: StateFlow<List<LactationLog>> = _lactationLogs.asStateFlow()

    // Initial Community Posts
    private val initialPosts = listOf(
        CommunityPost(
            id = 1,
            authorName = "Aria Montgomery",
            weeksPostpartum = "4 weeks postpartum",
            tag = "Postpartum Recovery",
            content = "First walk outside today without the stroller, just breathing in the fresh air. Recovery is slow, but celebrating the small victories! 🌸",
            likesCount = 12,
            commentsCount = 3
        ),
        CommunityPost(
            id = 2,
            authorName = "Elena Gilbert",
            weeksPostpartum = "2 weeks postpartum",
            tag = "Lactation Support",
            content = "To all the mums struggling with latching: it gets easier! Fennel tea has been a lifesaver for my supply. You've got this! ☕️",
            likesCount = 24,
            commentsCount = 8
        ),
        CommunityPost(
            id = 3,
            authorName = "Chloe Decker",
            weeksPostpartum = "6 weeks postpartum",
            tag = "Mental Wellness",
            content = "Highly recommend talking to a perinatal therapist. Just had my first session and it felt so good to express my fears without judgment.",
            likesCount = 18,
            commentsCount = 5
        )
    )

    private val _communityPosts = MutableStateFlow<List<CommunityPost>>(initialPosts)
    val communityPosts: StateFlow<List<CommunityPost>> = _communityPosts.asStateFlow()

    // Actions/Methods
    fun loginUser(email: String, name: String) {
        _currentUserEmail.value = email
        _currentUserName.value = name
        _userLoggedIn.value = true
    }

    fun registerUser(email: String, name: String) {
        _currentUserEmail.value = email
        _currentUserName.value = name
        _userLoggedIn.value = true
    }

    fun logout() {
        _currentUserEmail.value = ""
        _currentUserName.value = ""
        _userLoggedIn.value = false
    }

    fun completeOnboarding() {
        _onboardingCompleted.value = true
    }

    fun logDailyStatus(
        moodScore: Int,
        energyLevel: Int,
        recoveryProgress: Int,
        note: String,
        symptoms: List<String>
    ) {
        viewModelScope.launch {
            val entry = MoodEntry(
                moodScore = moodScore,
                energyLevel = energyLevel,
                recoveryProgress = recoveryProgress,
                emotionalNote = note,
                symptoms = symptoms.joinToString(", ")
            )
            moodEntryDao.insertMoodEntry(entry)
        }
    }

    fun logCycleEntry(entry: CycleEntry) {
        _cycleEntries.value = _cycleEntries.value + entry
    }

    fun logLactationLog(log: LactationLog) {
        _lactationLogs.value = _lactationLogs.value + log
    }

    fun bookAppointment(id: Int) {
        viewModelScope.launch {
            therapySessionDao.setBookingStatus(id, true)
        }
    }

    fun cancelAppointment(id: Int) {
        viewModelScope.launch {
            // Retrieve current sessions, update target session, and save
            val sessions = therapySessions.value
            sessions.find { it.id == id }?.let { session ->
                therapySessionDao.updateSession(session.copy(isBooked = false, status = "Available"))
            }
        }
    }

    fun toggleRecipeFavorite(id: Int, isFav: Boolean) {
        viewModelScope.launch {
            recipeDao.toggleFavorite(id, isFav)
        }
    }

    fun toggleArticleSaved(id: Int, isSaved: Boolean) {
        viewModelScope.launch {
            learningArticleDao.toggleSaved(id, isSaved)
        }
    }

    fun logMindfulnessSession(type: String, durationSeconds: Int, feedback: String) {
        viewModelScope.launch {
            val session = MindfulnessSession(
                type = type,
                durationSeconds = durationSeconds,
                feedback = feedback
            )
            mindfulnessSessionDao.insertMindfulnessSession(session)
        }
    }

    fun shareCommunityPost(content: String, tag: String) {
        val newPost = CommunityPost(
            id = _communityPosts.value.size + 1,
            authorName = _currentUserName.value.ifBlank { "Anonymous Mother" },
            weeksPostpartum = "New Mother",
            tag = tag,
            content = content,
            likesCount = 0,
            commentsCount = 0
        )
        _communityPosts.value = listOf(newPost) + _communityPosts.value
    }

    fun likeCommunityPost(id: Int) {
        _communityPosts.value = _communityPosts.value.map { post ->
            if (post.id == id) {
                post.copy(likesCount = post.likesCount + 1)
            } else {
                post
            }
        }
    }
}

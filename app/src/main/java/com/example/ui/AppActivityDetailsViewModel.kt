package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.MetricType
import com.example.data.model.AppUsageModel
import com.example.data.model.DailySummaryModel
import com.example.data.model.DayChartBarModel
import com.example.data.repository.WellbeingRepository
import com.example.widget.DigitalWellbeingWidget
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AppActivityDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WellbeingRepository(application)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private val _selectedMetric = MutableStateFlow(MetricType.SCREEN_TIME)
    val selectedMetric: StateFlow<MetricType> = _selectedMetric.asStateFlow()

    private val _selectedDate = MutableStateFlow(repository.getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    private val _showEditSheet = MutableStateFlow(false)
    val showEditSheet: StateFlow<Boolean> = _showEditSheet.asStateFlow()

    private val _isDefaultApp = MutableStateFlow(repository.isDefaultWellbeingApp())
    val isDefaultApp: StateFlow<Boolean> = _isDefaultApp.asStateFlow()

    private val _showSetDefaultPrompt = MutableStateFlow(!repository.isDefaultWellbeingApp() && repository.hasCompletedOnboarding())
    val showSetDefaultPrompt: StateFlow<Boolean> = _showSetDefaultPrompt.asStateFlow()

    private val _showFirstLaunchOnboarding = MutableStateFlow(!repository.hasCompletedOnboarding())
    val showFirstLaunchOnboarding: StateFlow<Boolean> = _showFirstLaunchOnboarding.asStateFlow()

    private val _chartBars = MutableStateFlow<List<DayChartBarModel>>(emptyList())
    val chartBars: StateFlow<List<DayChartBarModel>> = _chartBars.asStateFlow()

    val dailySummary: StateFlow<DailySummaryModel?> = _selectedDate
        .flatMapLatest { date ->
            repository.getDailySummaryFlow(date)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        viewModelScope.launch {
            repository.initDefaultDataIfNeeded()
            checkPermission()
            refreshChartBars()
        }
    }

    fun checkPermission() {
        _hasPermission.value = repository.hasUsagePermission()
    }

    fun selectOnboardingChoice(mode: String) {
        viewModelScope.launch {
            repository.setSelectedWellbeingMode(mode)
            if (mode == "NEW_WELLBEING") {
                repository.switchToRealSystemUsage()
            }
            _showFirstLaunchOnboarding.value = false
            _isDefaultApp.value = true
            _showSetDefaultPrompt.value = false
            refreshChartBars()
        }
    }

    fun openSetDefaultPrompt() {
        _showSetDefaultPrompt.value = true
    }

    fun closeSetDefaultPrompt() {
        _showSetDefaultPrompt.value = false
    }

    fun markAsDefaultApp() {
        repository.setDefaultWellbeingApp(true)
        _isDefaultApp.value = true
        _showSetDefaultPrompt.value = false
    }

    fun setMetric(metric: MetricType) {
        _selectedMetric.value = metric
        refreshChartBars()
    }

    fun selectDate(dateStr: String) {
        _selectedDate.value = dateStr
        refreshChartBars()
    }

    fun navigateDate(deltaDays: Int) {
        try {
            val dateObj = dateFormat.parse(_selectedDate.value) ?: Date()
            val cal = Calendar.getInstance()
            cal.time = dateObj
            cal.add(Calendar.DAY_OF_YEAR, deltaDays)
            val newDateStr = dateFormat.format(cal.time)
            selectDate(newDateStr)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openEditSheet() {
        _showEditSheet.value = true
    }

    fun closeEditSheet() {
        _showEditSheet.value = false
    }

    fun saveCustomOverride(
        isCustom: Boolean,
        screenTimeMins: Int,
        notifications: Int,
        opens: Int,
        appList: List<AppUsageModel>
    ) {
        viewModelScope.launch {
            val dateStr = _selectedDate.value
            repository.saveCustomOverride(
                dateStr = dateStr,
                isCustomOverride = isCustom,
                screenTimeMins = screenTimeMins,
                notifications = notifications,
                opens = opens,
                appList = appList
            )
            refreshChartBars()
            closeEditSheet()

            // Broadcast Glance widget update
            DigitalWellbeingWidget.updateWidget(getApplication<Application>().applicationContext)
        }
    }

    private fun refreshChartBars() {
        viewModelScope.launch {
            _chartBars.value = repository.getWeekChartBars(_selectedDate.value, _selectedMetric.value)
        }
    }
}

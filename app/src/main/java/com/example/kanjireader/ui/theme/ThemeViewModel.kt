package com.example.kanjireader.ui.theme

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class ThemeViewModel(private val appContext: Application) : ViewModel() {

    private val _isDarkTheme = mutableStateOf(true)
    val isDarkTheme: State<Boolean> = _isDarkTheme

    private val _currentMode = mutableStateOf(ThemePreferences.ThemeMode.AUTO)
    val currentMode: State<ThemePreferences.ThemeMode> = _currentMode

    private var sensorManager: SensorManager? = null
    private var lightSensor: Sensor? = null
    private var sensorRegistered = false

    private val DARK_THRESHOLD_LUX = 15f
    private val LIGHT_THRESHOLD_LUX = 45f
    private val STABLE_TIME_MS = 10000L

    private var pendingThemeSwitch: Boolean? = null
    private var switchJob: kotlinx.coroutines.Job? = null

    private fun isNightTime(): Boolean {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return hour >= 22 || hour < 4
    }

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (_currentMode.value != ThemePreferences.ThemeMode.AUTO) return
            if (isNightTime()) {
                if (_isDarkTheme.value != true) _isDarkTheme.value = true
                return
            }
            val lux = event.values[0]
            val shouldBeDark = lux < DARK_THRESHOLD_LUX
            val shouldBeLight = lux > LIGHT_THRESHOLD_LUX

            val targetDark = if (shouldBeDark) true else if (shouldBeLight) false else null

            if (targetDark != null && targetDark != _isDarkTheme.value) {
                scheduleThemeSwitch(targetDark)
            } else if (shouldBeDark == false && shouldBeLight == false && pendingThemeSwitch != null) {
                cancelPendingSwitch()
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    private fun scheduleThemeSwitch(toDark: Boolean) {
        if (pendingThemeSwitch == toDark) return
        cancelPendingSwitch()
        pendingThemeSwitch = toDark
        switchJob = viewModelScope.launch {
            delay(STABLE_TIME_MS)
            if (pendingThemeSwitch == toDark) {
                _isDarkTheme.value = toDark
                pendingThemeSwitch = null
            }
        }
    }

    private fun cancelPendingSwitch() {
        switchJob?.cancel()
        switchJob = null
        pendingThemeSwitch = null
    }

    init {
        sensorManager = appContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
        loadInitialTheme()
    }

    private fun loadInitialTheme() {
        viewModelScope.launch {
            val mode = ThemePreferences.getThemeMode(appContext).first()
            _currentMode.value = mode
            updateThemeForMode(mode)
        }
    }

    private fun updateThemeForMode(mode: ThemePreferences.ThemeMode) {
        when (mode) {
            ThemePreferences.ThemeMode.LIGHT -> {
                unregisterSensor()
                _isDarkTheme.value = false
            }
            ThemePreferences.ThemeMode.DARK -> {
                unregisterSensor()
                _isDarkTheme.value = true
            }
            ThemePreferences.ThemeMode.AUTO -> {
                registerSensor()
                if (isNightTime()) {
                    _isDarkTheme.value = true
                } else {
                    _isDarkTheme.value = true
                }
            }
        }
    }

    private fun registerSensor() {
        if (lightSensor != null && !sensorRegistered) {
            sensorManager?.registerListener(sensorListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
            sensorRegistered = true
        }
    }

    private fun unregisterSensor() {
        if (sensorRegistered) {
            sensorManager?.unregisterListener(sensorListener)
            sensorRegistered = false
        }
        cancelPendingSwitch()
    }

    fun setThemeMode(mode: ThemePreferences.ThemeMode) {
        viewModelScope.launch {
            ThemePreferences.setThemeMode(appContext, mode)
            _currentMode.value = mode
            updateThemeForMode(mode)
        }
    }

    override fun onCleared() {
        super.onCleared()
        unregisterSensor()
    }
}

class ThemeViewModelFactory(private val appContext: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ThemeViewModel(appContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
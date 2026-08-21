package com.example.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

class StepSensorHelper(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounterSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val accelerometerSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _liveSteps = MutableStateFlow(0)
    val liveSteps: StateFlow<Int> = _liveSteps.asStateFlow()

    private var initialStepOffset = -1
    private var lastMagnitude = 0.0
    private var lastStepTimestamp = 0L

    fun startListening(currentBaseSteps: Int = 0) {
        _liveSteps.value = currentBaseSteps
        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
        } else if (accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        try {
            sensorManager.unregisterListener(this)
        } catch (e: Exception) {
            // Ignored
        }
    }

    fun addManualSteps(count: Int) {
        _liveSteps.value += count
    }

    fun setSteps(count: Int) {
        _liveSteps.value = count
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSteps = event.values[0].toInt()
            if (initialStepOffset == -1) {
                initialStepOffset = totalSteps - _liveSteps.value
            }
            val calculated = (totalSteps - initialStepOffset).coerceAtLeast(0)
            _liveSteps.value = calculated
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0].toDouble()
            val y = event.values[1].toDouble()
            val z = event.values[2].toDouble()
            val magnitude = sqrt(x * x + y * y + z * z)
            val delta = magnitude - lastMagnitude
            lastMagnitude = magnitude

            val now = System.currentTimeMillis()
            // Peak detection threshold for step motion
            if (delta > 3.8 && (now - lastStepTimestamp > 300)) {
                lastStepTimestamp = now
                _liveSteps.value += 1
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

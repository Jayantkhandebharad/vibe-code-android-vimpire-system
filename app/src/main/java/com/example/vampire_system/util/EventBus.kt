package com.example.vampire_system.util

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData

/**
 * Simple event bus for communicating between fragments
 * Used to notify when new level tasks are added so the home screen can refresh
 */
object EventBus {
    private val _taskAddedEvent = MutableLiveData<Boolean>()
    val taskAddedEvent: LiveData<Boolean> = _taskAddedEvent
    
    fun notifyTaskAdded() {
        _taskAddedEvent.postValue(true)
    }
    
    fun clearTaskAddedEvent() {
        _taskAddedEvent.postValue(false)
    }
}

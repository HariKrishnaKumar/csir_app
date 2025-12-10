package com.example.glasscalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class CalendarActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ❌ No setContentView(R.layout.activity_calendar)
        // ✅ Show your Compose Glass calendar instead
        setContent {
            GlassCalendarScreen()
        }
    }
}

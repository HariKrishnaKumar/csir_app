package com.example.glasscalendar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ReminderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)

        val rvAdmin = findViewById<RecyclerView>(R.id.rvAdminReminders)
        val rvUser = findViewById<RecyclerView>(R.id.rvUserReminders)

        rvAdmin.layoutManager = LinearLayoutManager(this)
        rvUser.layoutManager = LinearLayoutManager(this)

        // TODO: Replace with real admin data (API)
        val adminReminders = listOf(
            ReminderItem(
                id = 1L,
                title = "CSIR Foundation Day",
                description = "All staff event scheduled by admin.",
                timeLabel = "12 Jan, 10:00 AM"
            ),
            ReminderItem(
                id = 2L,
                title = "Safety Training",
                description = "Mandatory lab safety session.",
                timeLabel = "18 Jan, 03:00 PM"
            )
        )

        // TODO: Replace with user-created reminders from DB
        val userReminders = listOf(
            ReminderItem(
                id = 101L,
                title = "Team Meeting",
                description = "Reminder created by you.",
                timeLabel = "Tomorrow, 11:00 AM"
            ),
            ReminderItem(
                id = 102L,
                title = "Submit Report",
                description = "Upload weekly report.",
                timeLabel = "Friday, 05:00 PM"
            )
        )

        val adminColor = getColor(R.color.reminder_admin_bg)
        val userColor = getColor(R.color.reminder_user_bg)

        val adminAdapter = ReminderAdapter(adminReminders, adminColor)
        val userAdapter = ReminderAdapter(userReminders, userColor)

        rvAdmin.adapter = adminAdapter
        rvUser.adapter = userAdapter
    }
}

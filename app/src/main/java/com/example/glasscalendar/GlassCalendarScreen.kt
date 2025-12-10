package com.example.glasscalendar

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ===============================
// COLORS + FONT
// ===============================
object CalendarColors {
    val Background1 = Color(0xFFEDD1B0)
    val Background2 = Color(0xFFEDD78F)
    val Background3 = Color(0xFFF8FD89)

    val TextPrimary = Color(0xFF000000)
    val TextSecondary = Color(0x99000000)
    val TextFaded = Color(0x66000000)

    val TodayBackground = Color(0x1A000000)
    val TodayBorder = Color(0xFF000000)

    val CardFill = Color(0xE6FFF7E3)
    val CardBorder = Color(0x66FFFFFF)

    val FabFill = Color(0xCCFFFFFF)

    val UserEventFill = Color(0xE6E3F2FF)
}

private val roboto = FontFamily.SansSerif

// ===============================
// MODELS
// ===============================
enum class CalendarMode { MONTH, YEAR }

data class Event(
    val id: Int? = null,
    val title: String,
    val description: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val isUserAdded: Boolean = true
)

sealed class EventListItem {
    data class SectionHeader(val title: String) : EventListItem()
    data class EventRow(val event: Event) : EventListItem()
}

// ===============================
// ROOT SCREEN
// ===============================
@Composable
fun GlassCalendarScreen() {
    val context = LocalContext.current
    val dbHelper = remember { EventsDbHelper(context) }
    val nowCal = remember { Calendar.getInstance() }

    var mode by remember { mutableStateOf(CalendarMode.MONTH) }
    var currentYear by remember { mutableStateOf(nowCal.get(Calendar.YEAR)) }
    var currentMonth by remember { mutableStateOf(nowCal.get(Calendar.MONTH)) }

    var events by remember { mutableStateOf<List<Event>>(emptyList()) }

    LaunchedEffect(Unit) {
        events = dbHelper.getAllEvents()
    }

    val onDeleteEvent: (Event) -> Unit = { event ->
        event.id?.let { id ->
            cancelEventReminder(context, id)
            dbHelper.deleteEvent(id)
            events = dbHelper.getAllEvents()
        }
    }

    val currentEvent = findCurrentEvent(events)
    val prioritizedItems = buildPrioritizedList(events)

    val monthName = getMonthName(currentMonth)

    val todayYear = nowCal.get(Calendar.YEAR)
    val todayMonth = nowCal.get(Calendar.MONTH)
    val todayDay = nowCal.get(Calendar.DAY_OF_MONTH)
    val selectedDay =
        if (currentYear == todayYear && currentMonth == todayMonth) todayDay else -1

    val goToCurrentMonth: () -> Unit = {
        currentYear = todayYear
        currentMonth = todayMonth
        mode = CalendarMode.MONTH
    }

    var showAddDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        CalendarColors.Background1,
                        CalendarColors.Background2,
                        CalendarColors.Background3
                    )
                )
            )
    ) {
        if (mode == CalendarMode.MONTH) {
            MonthViewScreen(
                monthIndex = currentMonth,
                monthName = monthName,
                year = currentYear,
                selectedDay = selectedDay,
                currentEvent = currentEvent,
                prioritizedItems = prioritizedItems,
                onModeChange = { mode = it },
                onGoToCurrentMonth = goToCurrentMonth,
                onDeleteEvent = onDeleteEvent
            )
        } else {
            YearViewScreen(
                year = currentYear,
                selectedMonth = currentMonth,
                onYearChange = { currentYear = it },
                onMonthSelected = {
                    currentMonth = it
                    mode = CalendarMode.MONTH
                },
                onModeChange = { mode = it },
                onGoToCurrentMonth = goToCurrentMonth
            )
        }

        GlassFab(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            showAddDialog = true
        }

        if (showAddDialog) {
            AddEventDialog(
                onDismiss = { showAddDialog = false },
                onSave = { title, description, startMillis, endMillis ->
                    if (title.isBlank()) return@AddEventDialog
                    val newEvent = Event(
                        title = title.trim(),
                        description = description.trim(),
                        startTimeMillis = startMillis,
                        endTimeMillis = endMillis,
                        isUserAdded = true
                    )
                    val newId = dbHelper.insertEvent(newEvent).toInt()
                    events = dbHelper.getAllEvents()
                    scheduleEventReminder(context, newId, newEvent)
                    showAddDialog = false
                }
            )
        }
    }
}

// ===============================
// GLASS FAB
// ===============================
@Composable
fun GlassFab(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(CalendarColors.FabFill)
            .border(1.dp, CalendarColors.CardBorder, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add event",
            tint = CalendarColors.TextPrimary
        )
    }
}

// ===============================
// MONTH VIEW
// ===============================
@Composable
fun MonthViewScreen(
    monthIndex: Int,
    monthName: String,
    year: Int,
    selectedDay: Int,
    currentEvent: Event?,
    prioritizedItems: List<EventListItem>,
    onModeChange: (CalendarMode) -> Unit,
    onGoToCurrentMonth: () -> Unit,
    onDeleteEvent: (Event) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GlassMonthCard(
            monthIndex = monthIndex,
            monthName = monthName,
            year = year,
            selectedDay = selectedDay,
            currentEvent = currentEvent,
            onMenuSelect = onModeChange,
            onGoToCurrentMonth = onGoToCurrentMonth
        )

        prioritizedItems.forEach {
            when (it) {
                is EventListItem.SectionHeader -> Text(
                    text = it.title,
                    color = CalendarColors.TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = roboto,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                is EventListItem.EventRow -> EventRowItem(
                    event = it.event,
                    onDelete = onDeleteEvent
                )
            }
        }
    }
}

// ===============================
// YEAR VIEW
// ===============================
@Composable
fun YearViewScreen(
    year: Int,
    selectedMonth: Int,
    onYearChange: (Int) -> Unit,
    onMonthSelected: (Int) -> Unit,
    onModeChange: (CalendarMode) -> Unit,
    onGoToCurrentMonth: () -> Unit
) {
    val monthShortNames = listOf(
        "Jan", "Feb", "Mar",
        "Apr", "May", "Jun",
        "Jul", "Aug", "Sep",
        "Oct", "Nov", "Dec"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(999.dp))
                .background(CalendarColors.CardFill)
                .border(
                    1.dp,
                    CalendarColors.CardBorder,
                    RoundedCornerShape(999.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clip(RoundedCornerShape(999.dp))
                    .background(CalendarColors.CardFill)
                    .border(
                        1.dp,
                        CalendarColors.CardBorder,
                        RoundedCornerShape(999.dp)
                    )
                    .clickable { }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Holidays",
                    color = CalendarColors.TextSecondary,
                    fontSize = 13.sp,
                    fontFamily = roboto
                )
            }

            Row(
                modifier = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(CalendarColors.CardFill)
                        .border(
                            1.dp,
                            CalendarColors.CardBorder,
                            RoundedCornerShape(9.dp)
                        )
                        .clickable { onYearChange(year - 1) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("<", color = CalendarColors.TextPrimary, fontSize = 16.sp)
                }

                Text(
                    text = year.toString(),
                    color = CalendarColors.TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = roboto
                )

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(CalendarColors.CardFill)
                        .border(
                            1.dp,
                            CalendarColors.CardBorder,
                            RoundedCornerShape(9.dp)
                        )
                        .clickable { onYearChange(year + 1) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(">", color = CalendarColors.TextPrimary, fontSize = 16.sp)
                }
            }

            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                CalendarModeMenuButton(
                    currentMode = CalendarMode.YEAR,
                    onModeChange = onModeChange,
                    onGoToCurrentMonth = onGoToCurrentMonth
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        for (row in 0 until 4) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(3) { col ->
                    val idx = row * 3 + col
                    val name = monthShortNames[idx]
                    val selected = idx == selectedMonth

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.2f)
                            .clip(RoundedCornerShape(22.dp))
                            .background(CalendarColors.CardFill)
                            .border(
                                1.dp,
                                CalendarColors.CardBorder,
                                RoundedCornerShape(22.dp)
                            )
                            .clickable { onMonthSelected(idx) },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = name,
                            color = CalendarColors.TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            fontFamily = roboto,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

// ===============================
// MONTH CARD
// ===============================
@Composable
fun GlassMonthCard(
    monthIndex: Int,
    monthName: String,
    year: Int,
    selectedDay: Int,
    currentEvent: Event?,
    onMenuSelect: (CalendarMode) -> Unit,
    onGoToCurrentMonth: () -> Unit
) {
    val shape = RoundedCornerShape(32.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CalendarColors.CardFill)
            .border(1.dp, CalendarColors.CardBorder, shape)
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(Modifier.width(1.dp))
                CalendarModeMenuButton(
                    currentMode = CalendarMode.MONTH,
                    onModeChange = onMenuSelect,
                    onGoToCurrentMonth = onGoToCurrentMonth
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "$monthName $year",
                color = CalendarColors.TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = roboto
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                    Text(
                        text = it,
                        color = CalendarColors.TextSecondary,
                        fontSize = 14.sp,
                        fontFamily = roboto,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            CalendarGrid(
                year = year,
                monthIndex = monthIndex,
                selectedDay = selectedDay
            )

            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(CalendarColors.CardFill)
                    .border(
                        1.dp,
                        CalendarColors.CardBorder,
                        RoundedCornerShape(24.dp)
                    )
                    .padding(14.dp)
            ) {
                if (currentEvent != null) {
                    Column(
                        verticalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "Current event",
                            color = CalendarColors.TextSecondary,
                            fontSize = 13.sp,
                            fontFamily = roboto
                        )
                        Text(
                            text = currentEvent.title,
                            color = CalendarColors.TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = roboto
                        )
                        if (currentEvent.description.isNotBlank()) {
                            Text(
                                text = currentEvent.description,
                                color = CalendarColors.TextFaded,
                                fontSize = 12.sp,
                                fontFamily = roboto
                            )
                        }
                    }
                } else {
                    Text(
                        text = "No upcoming events",
                        color = CalendarColors.TextSecondary,
                        fontSize = 13.sp,
                        fontFamily = roboto,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
            }
        }
    }
}

// ===============================
// MENU BUTTON
// ===============================
@Composable
fun CalendarModeMenuButton(
    currentMode: CalendarMode,
    onModeChange: (CalendarMode) -> Unit,
    onGoToCurrentMonth: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(CalendarColors.CardFill)
                .border(1.dp, CalendarColors.CardBorder, RoundedCornerShape(14.dp))
                .clickable { expanded = true },
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .width(14.dp)
                        .height(1.5.dp)
                        .background(CalendarColors.TextPrimary)
                )
                Box(
                    Modifier
                        .width(10.dp)
                        .height(1.5.dp)
                        .background(CalendarColors.TextPrimary)
                )
                Box(
                    Modifier
                        .width(14.dp)
                        .height(1.5.dp)
                        .background(CalendarColors.TextPrimary)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        "Current Month",
                        fontFamily = roboto,
                        color = CalendarColors.TextPrimary
                    )
                },
                onClick = {
                    onGoToCurrentMonth()
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        "Month view",
                        fontFamily = roboto,
                        color = CalendarColors.TextPrimary
                    )
                },
                onClick = {
                    onModeChange(CalendarMode.MONTH)
                    expanded = false
                },
                enabled = currentMode != CalendarMode.MONTH
            )
            DropdownMenuItem(
                text = {
                    Text(
                        "Year view",
                        fontFamily = roboto,
                        color = CalendarColors.TextPrimary
                    )
                },
                onClick = {
                    onModeChange(CalendarMode.YEAR)
                    expanded = false
                },
                enabled = currentMode != CalendarMode.YEAR
            )
        }
    }
}

// ===============================
// EVENT ROW
// ===============================
@Composable
fun EventRowItem(
    event: Event,
    onDelete: (Event) -> Unit
) {
    val format = SimpleDateFormat("EEE, dd MMM • hh:mm a", Locale.getDefault())
    val startFormatted = format.format(Date(event.startTimeMillis))

    val fillColor = if (event.isUserAdded) {
        CalendarColors.UserEventFill
    } else {
        CalendarColors.CardFill
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(fillColor)
            .border(1.dp, CalendarColors.CardBorder, RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = event.title,
                color = CalendarColors.TextPrimary,
                fontSize = 16.sp,
                fontFamily = roboto
            )
            Text(
                text = startFormatted,
                color = CalendarColors.TextSecondary,
                fontSize = 12.sp,
                fontFamily = roboto
            )
            if (event.description.isNotBlank()) {
                Text(
                    text = event.description,
                    color = CalendarColors.TextSecondary,
                    fontSize = 12.sp,
                    fontFamily = roboto
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Delete",
                    color = Color(0xFFD32F2F),
                    fontSize = 12.sp,
                    fontFamily = roboto,
                    modifier = Modifier.clickable { onDelete(event) }
                )
            }
        }
    }
}

// ===============================
// CALENDAR GRID
// ===============================
@Composable
fun CalendarGrid(year: Int, monthIndex: Int, selectedDay: Int) {
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, monthIndex)
        set(Calendar.DAY_OF_MONTH, 1)
    }

    val firstDay = cal.get(Calendar.DAY_OF_WEEK)
    val leading = firstDay - 1
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val totalCells = leading + daysInMonth
    val rows = (totalCells + 6) / 7
    var dayNum = 1

    Column {
        repeat(rows) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(7) { col ->
                    val cellIndex = rowIndex * 7 + col
                    if (cellIndex < leading || dayNum > daysInMonth) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                        )
                    } else {
                        val isSelected = dayNum == selectedDay
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(CalendarColors.TodayBackground)
                                        .border(
                                            1.dp,
                                            CalendarColors.TodayBorder,
                                            CircleShape
                                        )
                                )
                            }
                            Text(
                                text = dayNum.toString(),
                                color = CalendarColors.TextPrimary,
                                fontSize = 14.sp,
                                fontFamily = roboto
                            )
                        }
                        dayNum++
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

// ===============================
// ADD EVENT DIALOG
// ===============================
@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, startMillis: Long, endMillis: Long) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val startCal = remember { Calendar.getInstance() }
    val endCal = remember { Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 1) } }

    var startMillis by remember { mutableStateOf(startCal.timeInMillis) }
    var endMillis by remember { mutableStateOf(endCal.timeInMillis) }

    val dateTimeFormat = remember {
        SimpleDateFormat("EEE, dd MMM yyyy • hh:mm a", Locale.getDefault())
    }

    fun pickDateTime(initialMillis: Long, onResult: (Long) -> Unit) {
        val cal = Calendar.getInstance().apply { timeInMillis = initialMillis }

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        cal.set(Calendar.MINUTE, minute)
                        cal.set(Calendar.SECOND, 0)
                        cal.set(Calendar.MILLISECOND, 0)
                        onResult(cal.timeInMillis)
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    false
                ).show()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New event",
                fontFamily = roboto,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )

                Text(
                    text = "Start",
                    fontFamily = roboto,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CalendarColors.CardFill)
                        .border(
                            1.dp,
                            CalendarColors.CardBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            pickDateTime(startMillis) {
                                startMillis = it
                                if (endMillis < startMillis) endMillis = startMillis
                            }
                        }
                        .padding(10.dp)
                ) {
                    Text(
                        text = dateTimeFormat.format(Date(startMillis)),
                        color = CalendarColors.TextPrimary,
                        fontFamily = roboto
                    )
                }

                Text(
                    text = "End",
                    fontFamily = roboto,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CalendarColors.CardFill)
                        .border(
                            1.dp,
                            CalendarColors.CardBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            pickDateTime(endMillis) {
                                endMillis = if (it < startMillis) startMillis else it
                            }
                        }
                        .padding(10.dp)
                ) {
                    Text(
                        text = dateTimeFormat.format(Date(endMillis)),
                        color = CalendarColors.TextPrimary,
                        fontFamily = roboto
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title.trim(), description.trim(), startMillis, endMillis)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ===============================
// HELPERS + ALARMS
// ===============================
private fun getMonthName(idx: Int): String {
    val cal = Calendar.getInstance().apply { set(Calendar.MONTH, idx) }
    return SimpleDateFormat("MMMM", Locale.getDefault()).format(cal.time)
}

private fun findCurrentEvent(events: List<Event>): Event? {
    val now = System.currentTimeMillis()
    return events.filter { it.startTimeMillis >= now }.minByOrNull { it.startTimeMillis }
}

private fun buildPrioritizedList(events: List<Event>): List<EventListItem> {
    if (events.isEmpty()) return emptyList()

    val now = System.currentTimeMillis()
    val oneDay = 24L * 60 * 60 * 1000

    val cal = Calendar.getInstance().apply { timeInMillis = now }
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)

    val todayStart = cal.timeInMillis
    val tomorrowStart = todayStart + oneDay
    val weekEnd = todayStart + 7 * oneDay
    val nextMonthEnd = weekEnd + 30 * oneDay

    val todayEvents = events
        .filter { it.startTimeMillis in todayStart until tomorrowStart }
        .sortedBy { it.startTimeMillis }

    val weekEvents = events
        .filter { it.startTimeMillis in tomorrowStart until weekEnd }
        .sortedBy { it.startTimeMillis }

    val nextMonthEvents = events
        .filter { it.startTimeMillis in weekEnd until nextMonthEnd }
        .sortedBy { it.startTimeMillis }

    val futureEvents = events
        .filter { it.startTimeMillis >= nextMonthEnd }
        .sortedBy { it.startTimeMillis }

    val result = mutableListOf<EventListItem>()

    if (todayEvents.isNotEmpty()) {
        result += EventListItem.SectionHeader("Today")
        result += todayEvents.map { EventListItem.EventRow(it) }
    }

    if (weekEvents.isNotEmpty()) {
        result += EventListItem.SectionHeader("This week")
        result += weekEvents.map { EventListItem.EventRow(it) }
    }

    if (nextMonthEvents.isNotEmpty()) {
        result += EventListItem.SectionHeader("Next month")
        result += nextMonthEvents.map { EventListItem.EventRow(it) }
    }

    if (futureEvents.isNotEmpty()) {
        result += EventListItem.SectionHeader("Future months")
        result += futureEvents.map { EventListItem.EventRow(it) }
    }

    return result
}

/**
 * Schedules an exact alarm for 5 minutes before the event start.
 * If it's already too late for 5 minutes earlier, it falls back to the event time.
 * If even the event time is in the past, it does nothing.
 */
private fun scheduleEventReminder(context: Context, eventId: Int, event: Event) {
    val now = System.currentTimeMillis()
    val leadMillis = 5 * 60 * 1000L

    // First try: 5 minutes before
    var triggerTime = event.startTimeMillis - leadMillis

    if (triggerTime <= now) {
        // Too late for 5 mins before → fall back to event time
        triggerTime = event.startTimeMillis
        if (triggerTime <= now) {
            // Even event time is in the past → don't schedule
            return
        }
    }

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val intent = Intent(context, EventReminderReceiver::class.java).apply {
        putExtra("event_id", eventId)
        putExtra("title", event.title)
        putExtra("description", event.description)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        eventId,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // Works in Doze / app closed
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    } else {
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }
}

private fun cancelEventReminder(context: Context, eventId: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, EventReminderReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        eventId,
        intent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )
    if (pendingIntent != null) {
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}

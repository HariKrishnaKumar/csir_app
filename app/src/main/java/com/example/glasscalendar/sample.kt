//package com.example.glasscalendar
//
//package com.example.glasscalendar
//
//import androidx.compose.animation.AnimatedContent
//import androidx.compose.animation.ExperimentalAnimationApi
//import androidx.compose.animation.core.tween
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//import androidx.compose.animation.slideInHorizontally
//import androidx.compose.animation.slideOutHorizontally
//import androidx.compose.animation.togetherWith
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material3.DropdownMenu
//import androidx.compose.material3.DropdownMenuItem
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontFamily
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import java.text.SimpleDateFormat
//import java.util.Calendar
//import java.util.Date
//import java.util.Locale
//
//// ===============================
//// 🔥 CENTRALIZED COLOR CONTROLLER
//// ===============================
//object CalendarColors {
//    val Background1 = Color(0xFFEDD1B0)   // Gradient start
//    val Background2 = Color(0xFFEDD78F)   // Gradient middle
//    val Background3 = Color(0xFFF8FD89)   // Gradient end
//
//    // Text colors
//    val TextPrimary = Color(0xFF000000)   // Black
//    val TextSecondary = Color(0x99000000) // 60% black
//    val TextFaded = Color(0x66000000)     // 40% black
//
//    // Selected day bubble
//    val TodayBackground = Color(0x33222222)
//    val TodayBorder = Color(0xFF000000)
//
//    // Glass borders
//    val GlassBorder = Color(0x33000000)
//    val GlassFill = Color(0x22FFFFFF)
//}
//
//// System default Android Roboto
//private val roboto = FontFamily.SansSerif
//
//// =============== DATA MODELS ===============
//enum class CalendarMode { MONTH, YEAR }
//
//data class Event(
//    val id: Int,
//    val title: String,
//    val description: String,
//    val timeMillis: Long
//)
//
//sealed class EventListItem {
//    data class SectionHeader(val title: String) : EventListItem()
//    data class EventRow(val event: Event) : EventListItem()
//}
//
//// =============================================
//// 🔥 ROOT SCREEN WITH WARM THEME BACKGROUND
//// =============================================
//@OptIn(ExperimentalAnimationApi::class)
//@Composable
//fun GlassCalendarScreen() {
//    val nowCal = remember { Calendar.getInstance() }
//
//    var mode by remember { mutableStateOf(CalendarMode.MONTH) }
//    var currentYear by remember { mutableStateOf(nowCal.get(Calendar.YEAR)) }
//    var currentMonth by remember { mutableStateOf(nowCal.get(Calendar.MONTH)) }
//
//    val events = remember { generateSampleEvents() }
//    val currentEvent = remember(events) { findCurrentEvent(events) }
//    val prioritizedItems = remember(events) { buildPrioritizedList(events) }
//
//    val monthName = remember(currentMonth) { getMonthName(currentMonth) }
//
//    val todayY = nowCal.get(Calendar.YEAR)
//    val todayM = nowCal.get(Calendar.MONTH)
//    val todayD = nowCal.get(Calendar.DAY_OF_MONTH)
//
//    val selectedDay = if (currentYear == todayY && currentMonth == todayM) todayD else -1
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(
//                Brush.verticalGradient(
//                    listOf(
//                        CalendarColors.Background1,
//                        CalendarColors.Background2,
//                        CalendarColors.Background3
//                    )
//                )
//            )
//    ) {
//        AnimatedContent(
//            targetState = mode,
//            transitionSpec = {
//                val dir = if (targetState == CalendarMode.YEAR) 1 else -1
//                slideInHorizontally(
//                    initialOffsetX = { full -> dir * full / 2 },
//                    animationSpec = tween(250)
//                ) + fadeIn(tween(250)) togetherWith
//                        slideOutHorizontally(
//                            targetOffsetX = { full -> -dir * full / 2 },
//                            animationSpec = tween(250)
//                        ) + fadeOut(tween(250))
//            },
//            modifier = Modifier.fillMaxSize(),
//            label = "mode-animation"
//        ) { state ->
//            when (state) {
//                CalendarMode.MONTH -> MonthViewScreen(
//                    monthIndex = currentMonth,
//                    monthName = monthName,
//                    year = currentYear,
//                    selectedDay = selectedDay,
//                    currentEvent = currentEvent,
//                    prioritizedItems = prioritizedItems,
//                    onModeChange = { mode = it }
//                )
//
//                CalendarMode.YEAR -> YearViewScreen(
//                    year = currentYear,
//                    selectedMonth = currentMonth,
//                    onYearChange = { currentYear = it },
//                    onMonthSelected = {
//                        currentMonth = it
//                        mode = CalendarMode.MONTH
//                    },
//                    onModeChange = { mode = it }
//                )
//            }
//        }
//    }
//}
//
//// =============================================
//// 🔥 MONTH VIEW
//// =============================================
//@Composable
//fun MonthViewScreen(
//    monthIndex: Int,
//    monthName: String,
//    year: Int,
//    selectedDay: Int,
//    currentEvent: Event?,
//    prioritizedItems: List<EventListItem>,
//    onModeChange: (CalendarMode) -> Unit
//) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .verticalScroll(rememberScrollState())
//            .padding(24.dp),
//        verticalArrangement = Arrangement.spacedBy(16.dp)
//    ) {
//        GlassMonthCard(
//            monthIndex,
//            monthName,
//            year,
//            selectedDay,
//            currentEvent,
//            onMenuSelect = onModeChange
//        )
//
//        prioritizedItems.forEach {
//            when (it) {
//                is EventListItem.SectionHeader -> Text(
//                    text = it.title,
//                    color = CalendarColors.TextPrimary,
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.SemiBold,
//                    fontFamily = roboto,
//                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
//                )
//
//                is EventListItem.EventRow -> EventRowItem(it.event)
//            }
//        }
//    }
//}
//
//// =============================================
//// 🔥 YEAR VIEW — With Holidays button + centered year
//// =============================================
//@Composable
//fun YearViewScreen(
//    year: Int,
//    selectedMonth: Int,
//    onYearChange: (Int) -> Unit,
//    onMonthSelected: (Int) -> Unit,
//    onModeChange: (CalendarMode) -> Unit
//) {
//    val monthShortNames = listOf(
//        "Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"
//    )
//
//    Column(
//        modifier = Modifier.fillMaxSize().padding(24.dp)
//    ) {
//
//        // ========= TOP BAR =========
//        Box(modifier = Modifier.fillMaxWidth()) {
//
//            // LEFT: HOLIDAYS BUTTON
//            Box(
//                modifier = Modifier
//                    .align(Alignment.CenterStart)
//                    .clip(RoundedCornerShape(999.dp))
//                    .background(CalendarColors.GlassFill)
//                    .border(1.dp, CalendarColors.GlassBorder, RoundedCornerShape(999.dp))
//                    .clickable {}
//                    .padding(horizontal = 14.dp, vertical = 6.dp)
//            ) {
//                Text(
//                    text = "Holidays",
//                    color = CalendarColors.TextPrimary,
//                    fontSize = 13.sp,
//                    fontFamily = roboto
//                )
//            }
//
//            // CENTER YEAR SELECTOR
//            Row(
//                modifier = Modifier.align(Alignment.Center),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(16.dp)
//            ) {
//
//                // LEFT button
//                Box(
//                    modifier = Modifier
//                        .size(32.dp)
//                        .clip(RoundedCornerShape(10.dp))
//                        .background(CalendarColors.GlassFill)
//                        .border(1.dp, CalendarColors.GlassBorder, RoundedCornerShape(10.dp))
//                        .clickable { onYearChange(year - 1) },
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text("<", color = CalendarColors.TextPrimary, fontSize = 18.sp)
//                }
//
//                Text(
//                    text = year.toString(),
//                    color = CalendarColors.TextPrimary,
//                    fontSize = 24.sp,
//                    fontWeight = FontWeight.Bold,
//                    fontFamily = roboto
//                )
//
//                Box(
//                    modifier = Modifier
//                        .size(32.dp)
//                        .clip(RoundedCornerShape(10.dp))
//                        .background(CalendarColors.GlassFill)
//                        .border(1.dp, CalendarColors.GlassBorder, RoundedCornerShape(10.dp))
//                        .clickable { onYearChange(year + 1) },
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(">", color = CalendarColors.TextPrimary, fontSize = 18.sp)
//                }
//            }
//
//            // RIGHT: MENU BUTTON
//            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
//                CalendarModeMenuButton(
//                    currentMode = CalendarMode.YEAR,
//                    onModeChange = onModeChange
//                )
//            }
//        }
//
//        Spacer(Modifier.height(24.dp))
//
//        // ========= MONTH GRID =========
//        for (row in 0 until 4) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                repeat(3) { col ->
//                    val idx = row * 3 + col
//                    val name = monthShortNames[idx]
//                    val selected = idx == selectedMonth
//
//                    Box(
//                        modifier = Modifier
//                            .weight(1f)
//                            .aspectRatio(1.2f)
//                            .clip(RoundedCornerShape(20.dp))
//                            .background(
//                                Brush.verticalGradient(
//                                    listOf(
//                                        if (selected) Color(0x99FFFFFF) else Color(0x44FFFFFF),
//                                        Color(0x22FFFFFF)
//                                    )
//                                )
//                            )
//                            .border(
//                                1.dp,
//                                CalendarColors.GlassBorder,
//                                RoundedCornerShape(20.dp)
//                            )
//                            .clickable { onMonthSelected(idx) },
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = name,
//                            color = CalendarColors.TextPrimary,
//                            fontSize = 18.sp,
//                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
//                            fontFamily = roboto
//                        )
//                    }
//                }
//            }
//            Spacer(Modifier.height(12.dp))
//        }
//    }
//}
//
//// =============================================
//// 🔥 MONTH CARD (main top card)
//// =============================================
//@Composable
//fun GlassMonthCard(
//    monthIndex: Int,
//    monthName: String,
//    year: Int,
//    selectedDay: Int,
//    currentEvent: Event?,
//    onMenuSelect: (CalendarMode) -> Unit
//) {
//    val shape = RoundedCornerShape(30.dp)
//
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .heightIn(min = 360.dp, max = 520.dp)
//            .clip(shape)
//            .background(Color(0x44FFFFFF))
//            .border(1.dp, CalendarColors.GlassBorder, shape),
//        contentAlignment = Alignment.TopCenter
//    ) {
//
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(22.dp)
//        ) {
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Spacer(Modifier.width(1.dp))
//                CalendarModeMenuButton(
//                    currentMode = CalendarMode.MONTH,
//                    onModeChange = onMenuSelect
//                )
//            }
//
//            Spacer(Modifier.height(20.dp))
//
//            Text(
//                text = "$monthName $year",
//                color = CalendarColors.TextPrimary,
//                fontSize = 26.sp,
//                fontWeight = FontWeight.Bold,
//                fontFamily = roboto
//            )
//
//            Spacer(Modifier.height(16.dp))
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                listOf("S", "M", "T", "W", "T", "F", "S").forEach {
//                    Text(
//                        text = it,
//                        color = CalendarColors.TextSecondary,
//                        fontSize = 14.sp,
//                        fontFamily = roboto,
//                        modifier = Modifier.weight(1f),
//                        textAlign = TextAlign.Center
//                    )
//                }
//            }
//
//            Spacer(Modifier.height(10.dp))
//
//            CalendarGrid(
//                year = year,
//                monthIndex = monthIndex,
//                selectedDay = selectedDay
//            )
//
//            Spacer(Modifier.height(18.dp))
//
//            // Bottom event card
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(80.dp)
//                    .clip(RoundedCornerShape(24.dp))
//                    .background(Color(0x33FFFFFF))
//                    .border(
//                        1.dp,
//                        CalendarColors.GlassBorder,
//                        RoundedCornerShape(24.dp)
//                    )
//                    .padding(16.dp)
//            ) {
//                if (currentEvent != null) {
//                    Column(Modifier.fillMaxSize()) {
//                        Text(
//                            "Current event",
//                            color = Color(0xFFAA0000),
//                            fontSize = 14.sp,
//                            fontFamily = roboto
//                        )
//                        Text(
//                            currentEvent.title,
//                            color = CalendarColors.TextPrimary,
//                            fontSize = 18.sp,
//                            fontFamily = roboto
//                        )
//                        Text(
//                            currentEvent.description,
//                            color = CalendarColors.TextSecondary,
//                            fontSize = 12.sp,
//                            fontFamily = roboto
//                        )
//                    }
//                } else {
//                    Text(
//                        "No upcoming events",
//                        color = CalendarColors.TextSecondary,
//                        fontSize = 13.sp,
//                        fontFamily = roboto
//                    )
//                }
//            }
//        }
//    }
//}
//
//// =============================================
//// 🔥 MENU BUTTON (Month / Year)
//// =============================================
//@Composable
//fun CalendarModeMenuButton(
//    currentMode: CalendarMode,
//    onModeChange: (CalendarMode) -> Unit
//) {
//    var expanded by remember { mutableStateOf(false) }
//
//    Box {
//        Box(
//            modifier = Modifier
//                .size(36.dp)
//                .clip(RoundedCornerShape(12.dp))
//                .background(Color(0x33FFFFFF))
//                .border(1.dp, CalendarColors.GlassBorder, RoundedCornerShape(12.dp))
//                .clickable { expanded = true },
//            contentAlignment = Alignment.Center
//        ) {
//            Column(
//                verticalArrangement = Arrangement.spacedBy(3.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Box(
//                    Modifier.width(14.dp).height(1.5.dp)
//                        .background(CalendarColors.TextPrimary)
//                )
//                Box(
//                    Modifier.width(10.dp).height(1.5.dp)
//                        .background(CalendarColors.TextPrimary)
//                )
//                Box(
//                    Modifier.width(14.dp).height(1.5.dp)
//                        .background(CalendarColors.TextPrimary)
//                )
//            }
//        }
//
//        DropdownMenu(
//            expanded = expanded,
//            onDismissRequest = { expanded = false }
//        ) {
//            DropdownMenuItem(
//                text = { Text("Month", fontFamily = roboto, color = CalendarColors.TextPrimary) },
//                onClick = {
//                    onModeChange(CalendarMode.MONTH)
//                    expanded = false
//                },
//                enabled = currentMode != CalendarMode.MONTH
//            )
//            DropdownMenuItem(
//                text = { Text("Year", fontFamily = roboto, color = CalendarColors.TextPrimary) },
//                onClick = {
//                    onModeChange(CalendarMode.YEAR)
//                    expanded = false
//                },
//                enabled = currentMode != CalendarMode.YEAR
//            )
//        }
//    }
//}
//
//// =============================================
//// 🔥 EVENT ROW
//// =============================================
//@Composable
//fun EventRowItem(event: Event) {
//    val format = SimpleDateFormat("EEE, dd MMM • hh:mm a", Locale.getDefault())
//    val formatted = format.format(Date(event.timeMillis))
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clip(RoundedCornerShape(18.dp))
//            .background(Color(0x33FFFFFF))
//            .border(
//                1.dp,
//                CalendarColors.GlassBorder,
//                RoundedCornerShape(18.dp)
//            )
//            .padding(14.dp)
//    ) {
//        Text(
//            event.title,
//            color = CalendarColors.TextPrimary,
//            fontSize = 16.sp,
//            fontFamily = roboto
//        )
//        Text(
//            formatted,
//            color = CalendarColors.TextSecondary,
//            fontSize = 12.sp,
//            fontFamily = roboto
//        )
//        if (event.description.isNotBlank()) {
//            Text(
//                event.description,
//                color = CalendarColors.TextSecondary,
//                fontSize = 12.sp,
//                fontFamily = roboto
//            )
//        }
//    }
//}
//
//// =============================================
//// 🔥 REAL CALENDAR GRID
//// =============================================
//@Composable
//fun CalendarGrid(year: Int, monthIndex: Int, selectedDay: Int) {
//    val cal = Calendar.getInstance().apply {
//        set(Calendar.YEAR, year)
//        set(Calendar.MONTH, monthIndex)
//        set(Calendar.DAY_OF_MONTH, 1)
//    }
//
//    val firstDay = cal.get(Calendar.DAY_OF_WEEK)
//    val leading = firstDay - 1
//    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
//
//    Column {
//        val rows = ((leading + daysInMonth) + 6) / 7
//        var dayIndex = 1
//
//        repeat(rows) { row ->
//            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
//                repeat(7) { col ->
//                    val cellIndex = row * 7 + col
//
//                    if (cellIndex < leading || dayIndex > daysInMonth) {
//                        Box(Modifier.weight(1f).height(32.dp))
//                    } else {
//                        val isToday = dayIndex == selectedDay
//
//                        Box(
//                            Modifier.weight(1f).height(32.dp),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            if (isToday) {
//                                Box(
//                                    modifier = Modifier
//                                        .size(30.dp)
//                                        .clip(CircleShape)
//                                        .background(CalendarColors.TodayBackground)
//                                        .border(
//                                            1.dp,
//                                            CalendarColors.TodayBorder,
//                                            CircleShape
//                                        )
//                                )
//                            }
//                            Text(
//                                text = dayIndex.toString(),
//                                color = CalendarColors.TextPrimary,
//                                fontSize = 14.sp,
//                                fontFamily = roboto
//                            )
//                        }
//
//                        dayIndex++
//                    }
//                }
//            }
//            Spacer(Modifier.height(6.dp))
//        }
//    }
//}
//
//// =============================================
//// 🔥 PRIORITIZATION
//// =============================================
//private fun getMonthName(idx: Int): String {
//    val cal = Calendar.getInstance().apply { set(Calendar.MONTH, idx) }
//    return SimpleDateFormat("MMMM", Locale.getDefault()).format(cal.time)
//}
//
//private fun findCurrentEvent(events: List<Event>): Event? {
//    val now = System.currentTimeMillis()
//    return events.filter { it.timeMillis >= now }.minByOrNull { it.timeMillis }
//}
//
//private fun buildPrioritizedList(events: List<Event>): List<EventListItem> {
//    val now = System.currentTimeMillis()
//    val oneDay = 24L * 60 * 60 * 1000
//
//    val cal = Calendar.getInstance().apply { timeInMillis = now }
//    cal.set(Calendar.HOUR_OF_DAY, 0)
//    cal.set(Calendar.MINUTE, 0)
//    cal.set(Calendar.SECOND, 0)
//    cal.set(Calendar.MILLISECOND, 0)
//    val todayStart = cal.timeInMillis
//    val tomorrowStart = todayStart + oneDay
//    val weekEnd = todayStart + 7 * oneDay
//    val nextMonthEnd = weekEnd + 30 * oneDay
//
//    val current = findCurrentEvent(events)
//    val result = mutableListOf<EventListItem>()
//
//    // TODAY
//    if (current != null && current.timeMillis in now until tomorrowStart) {
//        result += EventListItem.SectionHeader("Today")
//        result += EventListItem.EventRow(current)
//    }
//
//    val remaining = events.filter { it.id != current?.id }
//
//    val week = remaining.filter { it.timeMillis in tomorrowStart until weekEnd }
//    if (week.isNotEmpty()) {
//        result += EventListItem.SectionHeader("This week")
//        result += week.sortedBy { it.timeMillis }.map { EventListItem.EventRow(it) }
//    }
//
//    val nextMonth = remaining.filter { it.timeMillis in weekEnd until nextMonthEnd }
//    if (nextMonth.isNotEmpty()) {
//        result += EventListItem.SectionHeader("Next month")
//        result += nextMonth.sortedBy { it.timeMillis }.map { EventListItem.EventRow(it) }
//    }
//
//    val future = remaining.filter { it.timeMillis >= nextMonthEnd }
//    if (future.isNotEmpty()) {
//        result += EventListItem.SectionHeader("Future months")
//        result += future.sortedBy { it.timeMillis }.map { EventListItem.EventRow(it) }
//    }
//
//    return result
//}
//
//// =============================================
//// 🔥 SAMPLE EVENTS FOR TESTING
//// =============================================
//private fun generateSampleEvents(): List<Event> {
//    val now = System.currentTimeMillis()
//    val oneHour = 60L * 60 * 1000
//    val oneDay = 24L * 60 * 60 * 1000
//
//    return listOf(
//        Event(1, "Stand-up meeting", "Daily sync", now + 2 * oneHour),
//        Event(2, "Client demo", "Calendar demo", now + 3 * oneDay),
//        Event(3, "Exam", "Software Testing", now + 10 * oneDay),
//        Event(4, "Project deadline", "Submit Calendar App", now + 25 * oneDay),
//        Event(5, "Vacation", "Trip with friends", now + 60 * oneDay)
//    )
//}

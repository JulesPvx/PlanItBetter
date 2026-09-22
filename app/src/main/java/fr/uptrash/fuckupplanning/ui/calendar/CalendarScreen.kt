@file:OptIn(ExperimentalTime::class)

package fr.uptrash.fuckupplanning.ui.calendar

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.uptrash.fuckupplanning.R
import fr.uptrash.fuckupplanning.data.model.Event
import fr.uptrash.fuckupplanning.data.repository.MMIYear
import fr.uptrash.fuckupplanning.data.repository.RestaurantMenuRepository
import fr.uptrash.fuckupplanning.data.repository.TPGroup
import fr.uptrash.fuckupplanning.ui.theme.AppTheme
import fr.uptrash.fuckupplanning.ui.theme.CustomAppTheme
import fr.uptrash.fuckupplanning.ui.theme.ThemeMode
import fr.uptrash.fuckupplanning.ui.theme.ThemeViewModel
import fr.uptrash.fuckupplanning.ui.theme.appThemes
import fr.uptrash.fuckupplanning.ui.theme.blueLightScheme
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    calendarViewModel: CalendarViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val uiState by calendarViewModel.uiState.collectAsStateWithLifecycle()
    val themeState by themeViewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding())
    ) {
        CalendarHeader(
            viewMode = uiState.viewMode,
            selectedDate = uiState.selectedDate,
            onViewModeChange = { calendarViewModel.switchViewMode(it) },
            onPreviousClick = { calendarViewModel.navigatePrevious() },
            onNextClick = { calendarViewModel.navigateNext() }
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.error_format, uiState.error ?: ""),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                calendarViewModel.loadEvents()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }

            else -> {
                when (uiState.viewMode) {
                    CalendarViewMode.DAY -> DayView(
                        selectedDate = uiState.selectedDate,
                        events = uiState.events,
                        onEventClick = { calendarViewModel.selectEvent(it) },
                        paddingValues = paddingValues
                    )

                    CalendarViewMode.WEEK -> WeekView(
                        selectedDate = uiState.selectedDate,
                        events = uiState.events,
                        onEventClick = { calendarViewModel.selectEvent(it) },
                        showFullDay = { calendarViewModel.selectDayForCourseList(it) },
                        paddingValues = paddingValues
                    )

                    CalendarViewMode.MONTH -> MonthView(
                        selectedDate = uiState.selectedDate,
                        events = uiState.events,
                        onDateClick = { calendarViewModel.selectDayForCourseList(it) },
                        onEventClick = { calendarViewModel.selectEvent(it) },
                        paddingValues = paddingValues
                    )
                }
            }
        }
    }

    // Event Detail Modal
    uiState.selectedEvent?.let { event ->
        ModalBottomSheet(
            onDismissRequest = { calendarViewModel.dismissEventDetail() },
            containerColor = MaterialTheme.colorScheme.surface,
            contentWindowInsets = { WindowInsets(0.dp, 0.dp, 0.dp, 0.dp) },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            EventDetailView(
                event = event,
                onDismiss = { calendarViewModel.dismissEventDetail() }
            )
        }
    }

    // Day Course List Modal
    uiState.selectedDayForCourseList?.let { selectedDate ->
        val dayEvents = uiState.events.filter { it.startDateTime.date == selectedDate }
            .sortedBy { it.startDateTime }
        ModalBottomSheet(
            onDismissRequest = { calendarViewModel.dismissDayCourseList() },
            containerColor = MaterialTheme.colorScheme.surface,
            contentWindowInsets = { WindowInsets(0.dp, 0.dp, 0.dp, 0.dp) },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            DayCourseListView(
                date = selectedDate,
                events = dayEvents,
                onEventClick = { event ->
                    calendarViewModel.dismissDayCourseList()
                    calendarViewModel.selectEvent(event)
                },
                onDismiss = { calendarViewModel.dismissDayCourseList() }
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarHeader(
    viewMode: CalendarViewMode,
    selectedDate: LocalDate,
    onViewModeChange: (CalendarViewMode) -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousClick, modifier = Modifier.size(40.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.previous))
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formatDateRange(selectedDate, viewMode),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (viewMode == CalendarViewMode.DAY) {
                    Text(
                        text = getDayOfWeekDisplayName(selectedDate.dayOfWeek, full = true),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onNextClick, modifier = Modifier.size(40.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, stringResource(R.string.next))
            }
        }

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            val modes = listOf(CalendarViewMode.DAY, CalendarViewMode.WEEK, CalendarViewMode.MONTH)
            modes.forEachIndexed { index, mode ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                    onClick = { onViewModeChange(mode) },
                    selected = viewMode == mode,
                    icon = {}
                ) {
                    Text(
                        text = when (mode) {
                            CalendarViewMode.DAY -> stringResource(R.string.day)
                            CalendarViewMode.WEEK -> stringResource(R.string.week)
                            CalendarViewMode.MONTH -> stringResource(R.string.month)
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun DayView(
    selectedDate: LocalDate,
    events: List<Event>,
    onEventClick: (Event) -> Unit,
    paddingValues: PaddingValues
) {
    val dayEvents = events.filter { it.startDateTime.date == selectedDate }.sortedBy { it.startDateTime }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 4.dp,
            bottom = paddingValues.calculateBottomPadding() + 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (dayEvents.isEmpty()) {
            item { EmptyCalendarState(stringResource(R.string.no_events_for_day)) }
        } else {
            item { DayTimeSummary(events = dayEvents) }
            items(dayEvents) { event ->
                EnhancedEventCard(event = event, onClick = { onEventClick(event) })
            }
        }
    }
}


@Composable
fun DayTimeSummary(modifier: Modifier = Modifier, events: List<Event>) {
    if (events.isEmpty()) return
    val sortedEvents = events.sortedBy { it.startDateTime }
    val firstEvent = sortedEvents.first()
    val lastEvent = sortedEvents.last()
    val totalWorkingMinutes = events.sumOf { event ->
        val start = event.startDateTime.hour * 60 + event.startDateTime.minute
        val end = event.endDateTime.hour * 60 + event.endDateTime.minute
        end - start
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${formatTime(firstEvent.startDateTime)} – ${formatTime(lastEvent.endDateTime)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${events.size} · ${formatDuration(totalWorkingMinutes)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
fun WeekView(
    selectedDate: LocalDate,
    events: List<Event>,
    onEventClick: (Event) -> Unit,
    paddingValues: PaddingValues,
    showFullDay: (LocalDate) -> Unit = { _ -> }
) {
    val startOfWeek = getWeekStart(selectedDate)
    val weekDays = (0..4).map { startOfWeek.plus(it, DateTimeUnit.DAY) }
    val weekEvents = events
        .filter { event -> weekDays.any { day -> event.startDateTime.date == day } }
        .groupBy { it.startDateTime.date }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
	        top = 4.dp,
	        bottom = paddingValues.calculateBottomPadding() + 16.dp,
            start = 16.dp,
            end = 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(weekDays) { day ->
            WeekDayCard(
                date = day,
                events = weekEvents[day].orEmpty().sortedBy { it.startDateTime },
                onEventClick = onEventClick,
                showFullDay = showFullDay
            )
        }
    }
}


@OptIn(ExperimentalTime::class)
@Composable
fun MonthView(
    selectedDate: LocalDate,
    events: List<Event>,
    onDateClick: (LocalDate) -> Unit,
    onEventClick: (Event) -> Unit,
    paddingValues: PaddingValues
) {
    val firstDayOfMonth = LocalDate(selectedDate.year, selectedDate.month, 1)

    // Calculate the first day to show (start of week containing first day of month)
    val startDay =
        firstDayOfMonth.minus(firstDayOfMonth.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)

    // Calculate all days to show, but only include weekdays (Monday-Friday)
    val allDays = (0..41).map { startDay.plus(it, DateTimeUnit.DAY) }
    val daysToShow = allDays.filter { date ->
        date.dayOfWeek != DayOfWeek.SATURDAY && date.dayOfWeek != DayOfWeek.SUNDAY
    }

    val monthEvents = events.filter { event ->
        daysToShow.any { day -> event.startDateTime.date == day }
    }.groupBy { it.startDateTime.date }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
            ).forEach { dow ->
                Text(
                    text = getDayOfWeekDisplayName(dow, full = false),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        )

        // Calendar grid with only weekdays (5 columns)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 16.dp)
        ) {
            // Group weekdays into weeks (5 days per row)
            val weekdayChunks = daysToShow.chunked(5)
            items(weekdayChunks) { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    week.forEach { date ->
                        EnhancedMonthDayCell(
                            date = date,
                            isCurrentMonth = date.month == selectedDate.month,
                            isSelected = date == selectedDate,
                            isToday = date == Clock.System.now()
                                .toLocalDateTime(TimeZone.currentSystemDefault()).date,
                            events = monthEvents[date] ?: emptyList(),
                            onDateClick = onDateClick,
                            onEventClick = onEventClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Fill remaining space if week has less than 5 days
                    repeat(5 - week.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
@Composable
fun EnhancedMonthDayCell(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    events: List<Event>,
    onDateClick: (LocalDate) -> Unit,
    onEventClick: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        !isCurrentMonth -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.surface
    }
    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        !isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = modifier
            .height(78.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .then(
                if (isToday) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                else Modifier
            )
            .clickable { onDateClick(date) }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = date.day.toString(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isToday) MaterialTheme.colorScheme.primary else contentColor
        )
        if (events.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            val first = events.minBy { it.startDateTime }
            val last = events.maxBy { it.endDateTime }
            Text(
                text = "${formatTime(first.startDateTime)}–${formatTime(last.endDateTime)}",
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                maxLines = 1
            )
            Text(
                text = events.size.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
fun WeekDayCard(
    date: LocalDate,
    events: List<Event>,
    onEventClick: (Event) -> Unit,
    showFullDay: (LocalDate) -> Unit
) {
    val isToday = date == Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
    val visibleEvents = events.take(3)
    val first = events.firstOrNull()
    val last = events.lastOrNull()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = getDayOfWeekDisplayName(date.dayOfWeek, full = false).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = " ${date.day}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            if (first != null && last != null) {
                Text(
                    text = "${formatTime(first.startDateTime)}–${formatTime(last.endDateTime)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (visibleEvents.isEmpty()) {
            Text(
                text = stringResource(R.string.no_events),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            visibleEvents.forEach { event ->
                CompactEventRow(event = event, onClick = { onEventClick(event) }, compact = true)
            }
            if (events.size > visibleEvents.size) {
                Text(
                    text = stringResource(R.string.more_events_format, events.size - visibleEvents.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showFullDay(date) }
                        .padding(top = 3.dp)
                )
            }
        }
    }
}


@Composable
fun EnhancedCompactEventItem(event: Event, onClick: () -> Unit, isLast: Boolean) {
    CompactEventRow(event = event, onClick = onClick, compact = true)
}

@Composable
private fun CompactEventRow(
    event: Event,
    onClick: () -> Unit,
    compact: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(if (compact) 10.dp else 12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = if (compact) 5.dp else 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(if (compact) 34.dp else 44.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(courseTypeColor(event.courseType))
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = event.summary,
                    style = if (compact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (!event.courseType.isNullOrBlank()) {
                    Text(
                        text = event.courseType,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = courseTypeColor(event.courseType)
                    )
                }
            }
            Text(
                text = "${formatTime(event.startDateTime)} – ${formatTime(event.endDateTime)}" +
                        event.location?.takeIf { it.isNotBlank() }?.let { " · $it" }.orEmpty(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun courseTypeColor(courseType: String?): Color = when (courseType) {
    "CM" -> MaterialTheme.colorScheme.primary
    "TDB" -> MaterialTheme.colorScheme.secondary
    "TD" -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.outline
}

@Composable
private fun EmptyCalendarState(text: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}


@Composable
fun EnhancedEventCard(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(44.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(courseTypeColor(event.courseType))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = event.summary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${formatTime(event.startDateTime)} – ${formatTime(event.endDateTime)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!event.courseType.isNullOrBlank()) {
                    Text(
                        text = event.courseType,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = courseTypeColor(event.courseType)
                    )
                }
            }
            val secondary = listOfNotNull(
                event.location?.takeIf { it.isNotBlank() },
                event.instructor?.takeIf { it.isNotBlank() }
            ).joinToString(" · ")
            if (secondary.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = secondary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


@Composable
fun EventDetailView(
    event: Event,
    onDismiss: () -> Unit
) {
    val durationMinutes = event.endDateTime.hour * 60 + event.endDateTime.minute -
            (event.startDateTime.hour * 60 + event.startDateTime.minute)

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = event.summary,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (!event.courseType.isNullOrBlank()) {
                        Text(
                            text = event.courseType,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = courseTypeColor(event.courseType)
                        )
                    }
                }
                Text(
                    text = "${formatDate(event.startDateTime.date)} · ${getDayOfWeekDisplayName(event.startDateTime.date.dayOfWeek, full = true)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailMetric(stringResource(R.string.start), formatTime(event.startDateTime), Modifier.weight(1f))
                DetailMetric(stringResource(R.string.end), formatTime(event.endDateTime), Modifier.weight(1f))
                DetailMetric(stringResource(R.string.duration), formatDuration(durationMinutes), Modifier.weight(1f))
            }
        }
        if (!event.location.isNullOrBlank()) {
            item { DetailLine(Icons.Default.LocationOn, stringResource(R.string.location), event.location) }
        }
        if (!event.instructor.isNullOrBlank()) {
            item { DetailLine(Icons.Default.Person, stringResource(R.string.instructor), event.instructor) }
        }
        if (event.groups.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.groups), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(event.groups.joinToString(" · "), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        if (!event.notes.isNullOrBlank()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.notes), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(event.notes, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        if (!event.lastUpdated.isNullOrBlank()) {
            item {
                Text(
                    text = "${stringResource(R.string.last_updated)}: ${event.lastUpdated}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DetailMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DetailLine(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}


@Composable
fun EnhancedDetailSection(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        content()
    }
}


@Composable
fun TimeDisplayCard(label: String, time: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(time, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}


@Composable
fun DayCourseListView(
    date: LocalDate,
    events: List<Event>,
    onEventClick: (Event) -> Unit,
    onDismiss: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(formatDate(date), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    "${getDayOfWeekDisplayName(date.dayOfWeek, full = true)} · ${events.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (events.isEmpty()) {
            item { EmptyCalendarState(stringResource(R.string.no_courses_for_day)) }
        } else {
            items(events) { event ->
                DayCourseItem(event = event, onClick = { onEventClick(event) })
            }
        }
    }
}


@Composable
fun DayCourseItem(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(formatTime(event.startDateTime), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(formatTime(event.endDateTime), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(courseTypeColor(event.courseType))
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        event.summary,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (!event.courseType.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(event.courseType, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = courseTypeColor(event.courseType))
                    }
                }
                val secondary = listOfNotNull(
                    event.location?.takeIf { it.isNotBlank() },
                    event.instructor?.takeIf { it.isNotBlank() }
                ).joinToString(" · ")
                if (secondary.isNotBlank()) {
                    Text(secondary, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}


@Composable
fun RestaurantMenuView(
    menu: Map<String, List<RestaurantMenuRepository.MenuItem>>,
    isLoading: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(
                    topStart = 28.dp,
                    topEnd = 28.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                )
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.menu_of_the_day),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = stringResource(R.string.crous_r_u_crousty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        item {
            Surface(
                color = MaterialTheme.colorScheme.surface
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else if (error != null) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = error, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = onRefresh) {
                                Text(text = stringResource(R.string.retry))
                            }
                        }
                    }
                } else {
                    // Show menu content
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (menu.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.no_menu_available),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            menu.forEach { (category, items) ->
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Render each MenuItem with optional points badge
                                    items.forEach { menuItem ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = menuItem.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )

                                            Spacer(modifier = Modifier.weight(1f))

                                            // Points badge if available
                                            menuItem.points?.let { pts ->
                                                Surface(
                                                    color = MaterialTheme.colorScheme.primary.copy(
                                                        alpha = 0.12f
                                                    ),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text(
                                                        text = "$pts pts",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.padding(
                                                            horizontal = 8.dp,
                                                            vertical = 4.dp
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Settings View
@Composable
fun SettingsView(
    selectedThemeMode: ThemeMode,
    selectedTheme: AppTheme,
    selectedTPGroup: TPGroup,
    selectedMMIYear: MMIYear,
    onTPGroupChange: (TPGroup) -> Unit,
    onMMIYearChange: (MMIYear) -> Unit,
    onThemeChange: (AppTheme) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(
                    topStart = 28.dp,
                    topEnd = 28.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = stringResource(R.string.settings_desc),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.settings),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = stringResource(R.string.configure_your_calendar_preferences),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Main content area
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // MMI Year Selection Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.School,
                                            contentDescription = "MMI Year",
                                            tint = MaterialTheme.colorScheme.tertiaryContainer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = stringResource(R.string.mmi_year_selection),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiaryContainer
                                    )
                                    Text(
                                        text = stringResource(R.string.select_your_mmi_year),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(
                                            alpha = 0.8f
                                        )
                                    )
                                }
                            }

                            // MMI Year Selection
                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf(
                                    MMIYear.MMI1,
                                    MMIYear.MMI2,
                                    MMIYear.MMI3
                                ).forEachIndexed { index, mmiYear ->
                                    SegmentedButton(
                                        shape = SegmentedButtonDefaults.itemShape(
                                            index = index,
                                            count = 3
                                        ),
                                        onClick = { onMMIYearChange(mmiYear) },
                                        selected = selectedMMIYear == mmiYear,
                                        colors = SegmentedButtonDefaults.colors(
                                            activeContainerColor = MaterialTheme.colorScheme.tertiary,
                                            activeContentColor = MaterialTheme.colorScheme.onTertiary,
                                            inactiveContainerColor = MaterialTheme.colorScheme.surface,
                                            inactiveContentColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    ) {
                                        Text(
                                            text = when (mmiYear) {
                                                MMIYear.MMI1 -> stringResource(R.string.mmi1_label)
                                                MMIYear.MMI2 -> stringResource(R.string.mmi2_label)
                                                MMIYear.MMI3 -> stringResource(R.string.mmi3_label)
                                            },
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (selectedMMIYear == mmiYear) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            // Current selection info
                            Surface(
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.current_year),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                    when (selectedMMIYear) {
                                        MMIYear.MMI1 -> {
                                            Text(
                                                text = stringResource(R.string.showing_mmi1_calendar),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        MMIYear.MMI2 -> {
                                            Text(
                                                text = stringResource(R.string.showing_mmi2_calendar),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        MMIYear.MMI3 -> {
                                            Text(
                                                text = stringResource(R.string.showing_mmi3_calendar),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // TP Group Filter Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.FilterList,
                                            contentDescription = "Filter",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = stringResource(R.string.tp_group_filter),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = stringResource(R.string.choose_which_tp_group_events_to_display),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                            alpha = 0.8f
                                        )
                                    )
                                }
                            }

                            // TP Group Selection
                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf(
                                    TPGroup.ALL,
                                    TPGroup.TP1,
                                    TPGroup.TP2,
                                    TPGroup.TP3,
                                    TPGroup.TP4
                                ).forEachIndexed { index, tpGroup ->
                                    SegmentedButton(
                                        shape = SegmentedButtonDefaults.itemShape(
                                            index = index,
                                            count = 5
                                        ),
                                        onClick = { onTPGroupChange(tpGroup) },
                                        selected = selectedTPGroup == tpGroup,
                                        colors = SegmentedButtonDefaults.colors(
                                            activeContainerColor = MaterialTheme.colorScheme.secondary,
                                            activeContentColor = MaterialTheme.colorScheme.onSecondary,
                                            inactiveContainerColor = MaterialTheme.colorScheme.surface,
                                            inactiveContentColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    ) {
                                        Text(
                                            text = when (tpGroup) {
                                                TPGroup.ALL -> stringResource(R.string.all_label)
                                                TPGroup.TP1 -> stringResource(R.string.tp1_label)
                                                TPGroup.TP2 -> stringResource(R.string.tp2_label)
                                                TPGroup.TP3 -> stringResource(R.string.tp3_label)
                                                TPGroup.TP4 -> stringResource(R.string.tp4_label)
                                            },
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (selectedTPGroup == tpGroup) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            // Current selection info
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.current_filter),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    when (selectedTPGroup) {
                                        TPGroup.ALL -> {
                                            Text(
                                                text = stringResource(R.string.showing_all_events_regardless_of_tp_group),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        TPGroup.TP1 -> {
                                            Text(
                                                text = stringResource(R.string.showing_tp1_tda_cm_courses_and_general_events),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        TPGroup.TP2 -> {
                                            Text(
                                                text = stringResource(R.string.showing_tp2_tda_cm_courses_and_general_events),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        TPGroup.TP3 -> {
                                            Text(
                                                text = stringResource(R.string.showing_tp3_tdb_cm_courses_and_general_events),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        TPGroup.TP4 -> {
                                            Text(
                                                text = stringResource(R.string.showing_tp4_tdb_cm_courses_and_general_events),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Theme Selection Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.ColorLens,
                                            contentDescription = "Theme",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = stringResource(R.string.theme_selection),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = stringResource(R.string.choose_your_preferred_theme),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                            alpha = 0.8f
                                        )
                                    )
                                }
                            }

                            // Theme Selection
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),

                                ) {
                                appThemes.forEach { appTheme ->
                                    val isSelected = selectedTheme == appTheme
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(
                                                    alpha = 0.2f
                                                ),
                                                shape = CircleShape
                                            )
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary else if (appTheme is CustomAppTheme) appTheme.light.primary else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                                    val dynamicColor = isSystemInDarkTheme()
                                                    if (dynamicColor) dynamicDarkColorScheme(context).primary else dynamicLightColorScheme(
                                                        context
                                                    ).primary
                                                } else {
                                                    blueLightScheme.primary
                                                }
                                            )
                                            .clickable {
                                                onThemeChange(appTheme)
                                            }
                                    )
                                }
                            }

                            // System/Dark/Light mode
                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ThemeMode.entries.forEachIndexed { index, themeMode ->
                                    val isSelected = selectedThemeMode == themeMode
                                    SegmentedButton(
                                        shape = SegmentedButtonDefaults.itemShape(
                                            index = index,
                                            count = 3
                                        ),
                                        onClick = {
                                            onThemeModeChange(themeMode)
                                        },
                                        selected = isSelected,
                                        colors = SegmentedButtonDefaults.colors(
                                            activeContainerColor = MaterialTheme.colorScheme.primary,
                                            activeContentColor = MaterialTheme.colorScheme.onPrimary,
                                            inactiveContainerColor = MaterialTheme.colorScheme.surface,
                                            inactiveContentColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    ) {
                                        Text(
                                            text = when (themeMode) {
                                                ThemeMode.LIGHT -> stringResource(R.string.light_theme)
                                                ThemeMode.DARK -> stringResource(R.string.dark_theme)
                                                ThemeMode.SYSTEM -> stringResource(R.string.system_default)
                                            },
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Future settings can be added here
                    Text(
                        text = stringResource(R.string.more_settings_will_be_available_in_future_updates),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Bottom padding
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

// Helper functions
private fun formatDateRange(date: LocalDate, viewMode: CalendarViewMode): String {
    return when (viewMode) {
        CalendarViewMode.DAY -> formatDate(date)
        CalendarViewMode.WEEK -> {
            val startOfWeek = getWeekStart(date)
            val endOfWeek = startOfWeek.plus(4, DateTimeUnit.DAY)
            "${formatDate(startOfWeek)} - ${formatDate(endOfWeek)}"
        }

        CalendarViewMode.MONTH -> {
            // "Month Year" format
            "${date.month.name.lowercase().replaceFirstChar { it.titlecase() }} ${date.year}"
        }
    }
}

private fun formatDate(date: LocalDate): String {
    return "${date.day.toString().padStart(2, '0')}/" +
            "${date.month.number.toString().padStart(2, '0')}/" +
            "${date.year}"
}

private fun formatTime(dateTime: LocalDateTime): String {
    return "${dateTime.hour.toString().padStart(2, '0')}:" +
            dateTime.minute.toString().padStart(2, '0')
}

@Composable
private fun getDayOfWeekDisplayName(dayOfWeek: DayOfWeek, full: Boolean): String {
    return when (dayOfWeek) {
        DayOfWeek.MONDAY -> if (full) stringResource(R.string.monday) else stringResource(R.string.mon)
        DayOfWeek.TUESDAY -> if (full) stringResource(R.string.tuesday) else stringResource(R.string.tue)
        DayOfWeek.WEDNESDAY -> if (full) stringResource(R.string.wednesday) else stringResource(R.string.wed)
        DayOfWeek.THURSDAY -> if (full) stringResource(R.string.thursday) else stringResource(R.string.thu)
        DayOfWeek.FRIDAY -> if (full) stringResource(R.string.friday) else stringResource(R.string.fri)
        DayOfWeek.SATURDAY -> if (full) stringResource(R.string.saturday) else stringResource(R.string.sat)
        DayOfWeek.SUNDAY -> if (full) stringResource(R.string.sunday) else stringResource(R.string.sun)
    }
}

private fun formatDuration(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h${minutes}min"
        hours > 0 -> "${hours}h"
        else -> "${minutes}min"
    }
}

private fun getWeekStart(date: LocalDate): LocalDate {
    return when (date.dayOfWeek) {
        DayOfWeek.SATURDAY, DayOfWeek.SUNDAY -> {
            // Move to next Monday
            val daysToNextMonday = 8 - date.dayOfWeek.isoDayNumber
            date.plus(daysToNextMonday, DateTimeUnit.DAY)
        }

        else -> {
            // Start of current week (Monday)
            date.minus(date.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)
        }
    }
}
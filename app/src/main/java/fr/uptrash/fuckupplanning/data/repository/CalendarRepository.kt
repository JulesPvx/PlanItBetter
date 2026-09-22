package fr.uptrash.fuckupplanning.data.repository

import fr.uptrash.fuckupplanning.data.model.Event
import fr.uptrash.fuckupplanning.data.network.ApiService
import fr.uptrash.fuckupplanning.util.ICalParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarRepository @Inject constructor(
    private val apiService: ApiService,
    private val parser: ICalParser,
    private val settingsRepository: SettingsRepository
) {
    suspend fun getEvents(): Result<List<Event>> = withContext(Dispatchers.IO) {
        try {
            val selectedMMIYear = settingsRepository.selectedMMIYearFlow.first()

            // Retrieve dynamic URLs based on the selected MMI year
            val s1Url = settingsRepository.getS1Url(selectedMMIYear).first()
            val s2Url = settingsRepository.getS2Url(selectedMMIYear).first()

            val s1ICalData = apiService.getICalDataFromUrl(s1Url)
            val s2ICalData = apiService.getICalDataFromUrl(s2Url)

            val s1Events = parser.parseICalData(s1ICalData)
            val s2Events = parser.parseICalData(s2ICalData)

            val allEvents = (s1Events + s2Events).sortedBy { it.startDateTime }

            Result.success(allEvents)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
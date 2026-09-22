package fr.uptrash.fuckupplanning.ui.configuration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.uptrash.fuckupplanning.data.repository.MMIYear
import fr.uptrash.fuckupplanning.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UrlConfigViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _s1Url = MutableStateFlow("")
    val s1Url: StateFlow<String> = _s1Url.asStateFlow()

    private val _s2Url = MutableStateFlow("")
    val s2Url: StateFlow<String> = _s2Url.asStateFlow()

    private val _selectedYear = MutableStateFlow(MMIYear.MMI2)
    val selectedYear: StateFlow<MMIYear> = _selectedYear.asStateFlow()

    init {
        loadUrlsForYear(MMIYear.MMI2)
    }

    fun selectYear(year: MMIYear) {
        _selectedYear.value = year
        loadUrlsForYear(year)
    }

    private fun loadUrlsForYear(year: MMIYear) {
        viewModelScope.launch {
            _s1Url.value = settingsRepository.getS1Url(year).first()
            _s2Url.value = settingsRepository.getS2Url(year).first()
        }
    }

    fun updateS1Url(url: String) {
        _s1Url.value = url
    }

    fun updateS2Url(url: String) {
        _s2Url.value = url
    }

    fun saveUrls() {
        viewModelScope.launch {
            settingsRepository.saveS1Url(_selectedYear.value, _s1Url.value)
            settingsRepository.saveS2Url(_selectedYear.value, _s2Url.value)
        }
    }

    fun resetUrls() {
        viewModelScope.launch {
            val currentYear = _selectedYear.value
            settingsRepository.resetUrls(currentYear)
            loadUrlsForYear(currentYear)
        }
    }
}
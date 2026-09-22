package fr.uptrash.fuckupplanning.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import fr.uptrash.fuckupplanning.ui.theme.AppTheme
import fr.uptrash.fuckupplanning.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class TPGroup {
    ALL,
    TP1,
    TP2,
    TP3,
    TP4
}

enum class MMIYear {
    MMI1,
    MMI2,
    MMI3
}

interface SettingsRepository {
    val selectedTPGroupFlow: Flow<TPGroup>
    val selectedMMIYearFlow: Flow<MMIYear>
    val selectedAppThemeFlow: Flow<AppTheme>
    val selectedAppThemeModeFlow: Flow<ThemeMode>
    suspend fun saveSelectedTPGroup(tpGroup: TPGroup)
    suspend fun saveSelectedMMIYear(mmiYear: MMIYear)
    suspend fun saveSelectedAppTheme(appTheme: AppTheme)
    suspend fun saveSelectedAppThemeMode(themeMode: ThemeMode)
    suspend fun saveS1Url(year: MMIYear, url: String)
    suspend fun saveS2Url(year: MMIYear, url: String)
    fun getS1Url(year: MMIYear): Flow<String>
    fun getS2Url(year: MMIYear): Flow<String>
    suspend fun resetUrls(year: MMIYear)
}

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private companion object {
        val KEY_SELECTED_TP = stringPreferencesKey("selected_tp_group")
        val KEY_SELECTED_MMI_YEAR = stringPreferencesKey("selected_mmi_year")
        val KEY_SELECTED_APP_THEME = stringPreferencesKey("selected_app_theme")
        val KEY_SELECTED_APP_THEME_MODE = stringPreferencesKey("selected_app_theme_mode")
        private const val BASE_URL = "https://upplanning.appli.univ-poitiers.fr/"
    }

    override val selectedTPGroupFlow: Flow<TPGroup> = dataStore.data
        .map { prefs ->
            val stored = prefs[KEY_SELECTED_TP] ?: TPGroup.ALL.name
            try {
                TPGroup.valueOf(stored)
            } catch (e: IllegalArgumentException) {
                TPGroup.ALL
            }
        }

    override val selectedMMIYearFlow: Flow<MMIYear> = dataStore.data
        .map { prefs ->
            val stored = prefs[KEY_SELECTED_MMI_YEAR] ?: MMIYear.MMI2.name
            try {
                MMIYear.valueOf(stored)
            } catch (e: IllegalArgumentException) {
                MMIYear.MMI2
            }
        }

    override val selectedAppThemeFlow: Flow<AppTheme> = dataStore.data
        .map { prefs ->
            val stored = prefs[KEY_SELECTED_APP_THEME] ?: AppTheme.SYSTEM.key
            try {
                AppTheme.fromKey(stored)
            } catch (e: IllegalArgumentException) {
                AppTheme.SYSTEM
            }
        }

    override val selectedAppThemeModeFlow: Flow<ThemeMode> = dataStore.data
        .map { prefs ->
            val stored = prefs[KEY_SELECTED_APP_THEME_MODE] ?: ThemeMode.SYSTEM.key
            try {
                ThemeMode.entries.first { it.key == stored }
            } catch (e: NoSuchElementException) {
                ThemeMode.SYSTEM
            }
        }

    override suspend fun saveSelectedTPGroup(tpGroup: TPGroup) {
        dataStore.edit { prefs ->
            prefs[KEY_SELECTED_TP] = tpGroup.name
        }
    }

    override suspend fun saveSelectedMMIYear(mmiYear: MMIYear) {
        dataStore.edit { prefs ->
            prefs[KEY_SELECTED_MMI_YEAR] = mmiYear.name
        }
    }

    override suspend fun saveSelectedAppTheme(appTheme: AppTheme) {
        dataStore.edit { prefs ->
            prefs[KEY_SELECTED_APP_THEME] = appTheme.key
        }
    }

    override suspend fun saveSelectedAppThemeMode(themeMode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[KEY_SELECTED_APP_THEME_MODE] = themeMode.key
        }
    }

    override fun getS1Url(year: MMIYear): Flow<String> {
        val key = stringPreferencesKey("s1_url_${year.name}")
        val defaultUrl = when (year) {
            MMIYear.MMI1 -> "${BASE_URL}jsp/custom/modules/plannings/anonymous_cal.jsp?resources=18300&projectId=17&calType=ical&nbWeeks=28"
            MMIYear.MMI2 -> "${BASE_URL}jsp/custom/modules/plannings/anonymous_cal.jsp?resources=21212&projectId=17&calType=ical&nbWeeks=28"
            MMIYear.MMI3 -> "${BASE_URL}jsp/custom/modules/plannings/anonymous_cal.jsp?resources=2450&projectId=17&calType=ical&nbWeeks=28"
        }

        return dataStore.data.map { preferences ->
            preferences[key] ?: defaultUrl
        }
    }

    override fun getS2Url(year: MMIYear): Flow<String> {
        val key = stringPreferencesKey("s2_url_${year.name}")
        val defaultUrl = when (year) {
            MMIYear.MMI1 -> "${BASE_URL}jsp/custom/modules/plannings/anonymous_cal.jsp?resources=21211&projectId=17&calType=ical&nbWeeks=28"
            MMIYear.MMI2 -> "${BASE_URL}jsp/custom/modules/plannings/anonymous_cal.jsp?resources=21298&projectId=17&calType=ical&nbWeeks=28"
            MMIYear.MMI3 -> "${BASE_URL}jsp/custom/modules/plannings/anonymous_cal.jsp?resources=2471&projectId=17&calType=ical&nbWeeks=28"
        }

        return dataStore.data.map { preferences ->
            preferences[key] ?: defaultUrl
        }
    }

    override suspend fun saveS1Url(year: MMIYear, url: String) {
        val key = stringPreferencesKey("s1_url_${year.name}")
        dataStore.edit { preferences ->
            preferences[key] = url
        }
    }

    override suspend fun saveS2Url(year: MMIYear, url: String) {
        val key = stringPreferencesKey("s2_url_${year.name}")
        dataStore.edit { preferences ->
            preferences[key] = url
        }
    }

    override suspend fun resetUrls(year: MMIYear) {
        val s1Key = stringPreferencesKey("s1_url_${year.name}")
        val s2Key = stringPreferencesKey("s2_url_${year.name}")
        dataStore.edit { preferences ->
            preferences.remove(s1Key)
            preferences.remove(s2Key)
        }
    }
}

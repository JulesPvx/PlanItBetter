package fr.uptrash.fuckupplanning.ui.configuration

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.uptrash.fuckupplanning.R
import fr.uptrash.fuckupplanning.data.repository.MMIYear

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrlConfigScreen(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    viewModel: UrlConfigViewModel = hiltViewModel()
) {
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val s1Url by viewModel.s1Url.collectAsStateWithLifecycle()
    val s2Url by viewModel.s2Url.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.url_configuration),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(MMIYear.MMI1, MMIYear.MMI2, MMIYear.MMI3).forEachIndexed { index, mmiYear ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = 3),
                    onClick = { viewModel.selectYear(mmiYear) },
                    selected = selectedYear == mmiYear
                ) {
                    Text(text = mmiYear.name)
                }
            }
        }

        OutlinedTextField(
            value = s1Url,
            onValueChange = { viewModel.updateS1Url(it) },
            label = { Text(stringResource(R.string.semester_1_url)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = s2Url,
            onValueChange = { viewModel.updateS2Url(it) },
            label = { Text(stringResource(R.string.semester_2_url)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Updated button row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { viewModel.resetUrls() },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.reset))
            }

            Button(
                onClick = { viewModel.saveUrls() }
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.save_urls))
            }
        }
    }
}
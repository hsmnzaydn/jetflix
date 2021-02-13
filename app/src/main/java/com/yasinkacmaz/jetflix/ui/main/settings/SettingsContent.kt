package com.yasinkacmaz.jetflix.ui.main.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yasinkacmaz.jetflix.R
import com.yasinkacmaz.jetflix.ui.common.loading.LoadingRow
import com.yasinkacmaz.jetflix.util.transformation.SizeTransformation
import dev.chrisbanes.accompanist.coil.CoilImage
import kotlinx.coroutines.ExperimentalCoroutinesApi

const val SETTINGS_DIALOG_TAG = "SettingsDialog"
private const val FLAG_ID = "flag"
private const val TICK_ID = "tickIcon"
private const val DROPDOWN_ID = "dropdownIcon"
private const val RESIZE_PERCENT = 25
private val LocalSizeTransformation = staticCompositionLocalOf<SizeTransformation>()

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun SettingsContent(onDialogDismiss: () -> Unit) {
    val settingsViewModel: SettingsViewModel = viewModel()
    settingsViewModel.fetchLanguages()
    val uiState = settingsViewModel.uiState.collectAsState().value
    val selectedLanguage = settingsViewModel.selectedLanguage.collectAsState(initial = Language.default)
    SettingsDialog(
        uiState,
        selectedLanguage,
        onDialogDismiss = onDialogDismiss,
        onLanguageSelected = { settingsViewModel.onLanguageSelected(it) }
    )
}

@Composable
fun SettingsDialog(
    uiState: SettingsViewModel.UiState,
    selectedLanguage: State<Language>,
    onLanguageSelected: (Language) -> Unit,
    onDialogDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDialogDismiss) {
        Card(
            shape = MaterialTheme.shapes.medium,
            backgroundColor = MaterialTheme.colors.surface,
            contentColor = MaterialTheme.colors.surface,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { testTag = SETTINGS_DIALOG_TAG }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (uiState.showLoading) {
                    LoadingRow(title = stringResource(R.string.fetching_languages))
                } else {
                    val transformation = remember { SizeTransformation(percent = RESIZE_PERCENT) }
                    Providers(LocalSizeTransformation provides transformation) {
                        LanguageRow(uiState.languages, selectedLanguage.value, onLanguageSelected)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun LanguageRow(
    languages: List<Language>,
    selectedLanguage: Language,
    onLanguageSelected: (Language) -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        var showDropdown by remember { mutableStateOf(false) }
        Text(stringResource(R.string.language))
        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false }
        ) {
            languages.forEach { language ->
                val selected = language == selectedLanguage
                DropdownItem(language.englishName, language.flagUrl, selected) {
                    onLanguageSelected(language)
                    showDropdown = false
                }
            }
        }
        ToggleContent(selectedLanguage.englishName, selectedLanguage.flagUrl) {
            showDropdown = true
        }
    }
}

@Composable
private fun ToggleContent(countryName: String, flagUrl: String, onClick: () -> Unit) {
    val flagContent = flagContent(flagUrl, countryName)
    val arrowContent = iconContent(DROPDOWN_ID, Icons.Default.ArrowDropDown)
    Text(
        text = buildAnnotatedString {
            appendInlineContent(FLAG_ID)
            append(countryName)
            appendInlineContent(DROPDOWN_ID)
        },
        inlineContent = mapOf(arrowContent, flagContent),
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun DropdownItem(countryName: String, flagUrl: String, selected: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(enabled = !selected, onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = buildAnnotatedString {
                if (selected) {
                    appendInlineContent(TICK_ID)
                }
                appendInlineContent(FLAG_ID)
                append(countryName)
            },
            inlineContent = inlineContent(flagUrl, countryName, selected)
        )
    }
}

private fun inlineContent(flagUrl: String, countryName: String, selected: Boolean): Map<String, InlineTextContent> {
    val flagContent = flagContent(flagUrl, countryName)
    return if (selected) mapOf(iconContent(TICK_ID, Icons.Default.Done), flagContent) else mapOf(flagContent)
}

private fun iconContent(id: String, icon: ImageVector) = id to InlineTextContent(
    placeholder = Placeholder(
        width = 2.em,
        height = 1.em,
        placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
    ),
    children = {
        Image(
            imageVector = icon,
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface)
        )
    }
)

private fun flagContent(flagUrl: String, countryName: String) = FLAG_ID to InlineTextContent(
    placeholder = Placeholder(
        width = 2.em,
        height = 1.em,
        placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
    ),
    children = {
        val sizeTransformation = LocalSizeTransformation.current
        /**
         * We resize country flag bitmap by [RESIZE_PERCENT] percent to improve render performance.
         * Then we set contentScale as [FixedScale] with 100/[RESIZE_PERCENT] value to match the original size.
         * Why?: Dropdown menu does not use LazyColumn and we are rendering all of the country flags at once :(
         */
        CoilImage(
            modifier = Modifier.padding(end = 8.dp),
            data = flagUrl,
            contentScale = FixedScale(100f / RESIZE_PERCENT),
            contentDescription = stringResource(id = R.string.flag_content_description, countryName),
            requestBuilder = { transformations(sizeTransformation) }
        )
    }
)

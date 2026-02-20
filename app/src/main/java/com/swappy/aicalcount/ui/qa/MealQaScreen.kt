package com.swappy.aicalcount.ui.qa

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swappy.aicalcount.R

@Composable
fun MealQaScreen(
    loading: Boolean,
    answer: String?,
    onSendQuestion: (String) -> Unit,
    onPredefinedClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var question by rememberSaveable { mutableStateOf("") }
    val predefined = listOf(
        stringResource(R.string.qa_predefined_how_many_calories),
        stringResource(R.string.qa_predefined_macros),
        stringResource(R.string.qa_predefined_healthy),
        stringResource(R.string.qa_predefined_substitute),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = stringResource(R.string.qa_title),
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp),
        ) {
            items(predefined) { label ->
                FilterChip(
                    selected = false,
                    onClick = { onPredefinedClick(label) },
                    label = { Text(label, maxLines = 1) },
                )
            }
        }
        OutlinedTextField(
            value = question,
            onValueChange = { question = it },
            placeholder = { Text(stringResource(R.string.qa_hint)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            minLines = 2,
        )
        androidx.compose.material3.Button(
            onClick = {
                if (question.isNotBlank()) {
                    onSendQuestion(question.trim())
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading && question.isNotBlank(),
        ) {
            Text(stringResource(R.string.qa_send))
        }
        if (loading) {
            LinearProgressIndicator(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp))
        }
        answer?.let {
            Text(
                text = it,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}

@Preview(showBackground = true, name = "Meal Q&A")
@Composable
fun MealQaScreenPreview() {
    com.swappy.aicalcount.ui.theme.AiCalCountTheme {
        MealQaScreen(
            loading = false,
            answer = null,
            onSendQuestion = {},
            onPredefinedClick = {},
        )
    }
}

package com.drake.droidblox.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ExtendedSlider(
    title: String,
    subtitle: String,
    value: String?,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    interactive: Boolean = true,
    onValueChangeFinished: (String) -> Unit,
    valueLabel: (Float) -> String = { it.toInt().toString() }
) {
    val initial = value?.toFloatOrNull()?.coerceIn(valueRange) ?: valueRange.start
    var sliderValue by remember(value, valueRange) { mutableStateOf(initial) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TitleWithSubtitle(title, subtitle, modifier = Modifier.weight(1f))
                Text(
                    text = valueLabel(sliderValue),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(8.dp))
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                onValueChangeFinished = { onValueChangeFinished(sliderValue.toInt().toString()) },
                valueRange = valueRange,
                steps = steps,
                enabled = interactive
            )
        }
    }
}

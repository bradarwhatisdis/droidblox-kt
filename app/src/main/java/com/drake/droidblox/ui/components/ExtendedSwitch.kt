package com.drake.droidblox.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ExtendedSwitch(
    title: String,
    subtitle: String,
    enabled: Boolean = false,
    interactive: Boolean = true,
    onClick: ((value: Boolean) -> Unit) = {}
) {
    var toggled by remember { mutableStateOf(enabled) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TitleWithSubtitle(title, subtitle, modifier = Modifier.weight(1f))
            Switch(
                checked = toggled,
                onCheckedChange = {
                    toggled = it
                    onClick(it)
                },
                enabled = interactive
            )
        }
    }
}

package com.qpeyba.surf_slop_summer_school_2026.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.qpeyba.surf_slop_summer_school_2026.domain.model.EquipmentType
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.Terracotta
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextPrimary

@Composable
fun EquipmentSelector(
    selected: EquipmentType,
    onSelectionChanged: (EquipmentType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.selectableGroup()) {
        EquipmentType.entries.forEach { type ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = type == selected,
                        onClick = { onSelectionChanged(type) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = type == selected,
                    onClick = null,
                    colors = RadioButtonDefaults.colors(selectedColor = Terracotta)
                )
                Text(
                    text = when (type) {
                        EquipmentType.OWN -> "Своя экипировка"
                        EquipmentType.RENTAL -> "Прокат"
                    },
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

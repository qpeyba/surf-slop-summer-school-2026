package com.qpeyba.surf_slop_summer_school_2026.ui.sheet.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ChefButton
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextPrimary
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDateSheet(
    onDismiss: () -> Unit,
    onApply: (() -> Unit)? = null,
    viewModel: FilterDateViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    val datePickerState = rememberDatePickerState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Фильтр по дате",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.quickFilter == QuickFilter.TODAY,
                    onClick = { viewModel.onEvent(FilterDateEvent.QuickFilterSelected(QuickFilter.TODAY)) },
                    label = { Text("Сегодня") }
                )
                FilterChip(
                    selected = state.quickFilter == QuickFilter.TOMORROW,
                    onClick = { viewModel.onEvent(FilterDateEvent.QuickFilterSelected(QuickFilter.TOMORROW)) },
                    label = { Text("Завтра") }
                )
                FilterChip(
                    selected = state.quickFilter == QuickFilter.THIS_WEEK,
                    onClick = { viewModel.onEvent(FilterDateEvent.QuickFilterSelected(QuickFilter.THIS_WEEK)) },
                    label = { Text("Эта неделя") }
                )
                FilterChip(
                    selected = state.quickFilter == QuickFilter.NEXT_WEEK,
                    onClick = { viewModel.onEvent(FilterDateEvent.QuickFilterSelected(QuickFilter.NEXT_WEEK)) },
                    label = { Text("След. неделя") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            DatePicker(state = datePickerState)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { viewModel.onEvent(FilterDateEvent.ResetPressed) }) {
                    Text("Сбросить", color = TextSecondary)
                }
                ChefButton(text = "Применить", onClick = { viewModel.onEvent(FilterDateEvent.ApplyPressed); onDismiss() })
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

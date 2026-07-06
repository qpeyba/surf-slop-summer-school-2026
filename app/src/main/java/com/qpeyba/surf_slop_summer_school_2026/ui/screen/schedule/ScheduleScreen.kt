package com.qpeyba.surf_slop_summer_school_2026.ui.screen.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ChefTopAppBar
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ClassCard
import com.qpeyba.surf_slop_summer_school_2026.ui.components.DateStrip
import com.qpeyba.surf_slop_summer_school_2026.ui.components.EmptyState
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ErrorState
import com.qpeyba.surf_slop_summer_school_2026.ui.components.LoadingSkeleton
import com.qpeyba.surf_slop_summer_school_2026.ui.components.SkeletonVariant
import com.qpeyba.surf_slop_summer_school_2026.util.UiState
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    onClassClick: (String) -> Unit,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        ChefTopAppBar(
            title = "Шеф-стол",
            actions = {
                androidx.compose.material3.IconButton(onClick = { viewModel.onEvent(ScheduleEvent.OpenFilter) }) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Фильтр"
                    )
                }
            }
        )

        val dates = (0 until 7).map { LocalDate.now().plusDays(it.toLong()) }
        DateStrip(
            dates = dates,
            selectedDate = state.selectedDate,
            onDateSelected = { viewModel.onEvent(ScheduleEvent.DateSelected(it)) },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        when (val classes = state.classes) {
            is UiState.Loading -> {
                LoadingSkeleton(variant = SkeletonVariant.LIST)
            }
            is UiState.Empty -> {
                EmptyState(
                    illustration = Icons.Default.SearchOff,
                    title = "Нет доступных классов",
                    subtitle = "Попробуйте выбрать другую дату",
                    actionLabel = "Выбрать другую дату",
                    onAction = { viewModel.onEvent(ScheduleEvent.DateSelected(LocalDate.now())) }
                )
            }
            is UiState.Error -> {
                ErrorState(
                    message = classes.message,
                    onRetry = { viewModel.onEvent(ScheduleEvent.Refresh) }
                )
            }
            is UiState.Success -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(classes.data) { slot ->
                            ClassCard(
                                slot = slot,
                                onCardClick = onClassClick
                            )
                        }
                    }
                }
            }
        }
    }
}

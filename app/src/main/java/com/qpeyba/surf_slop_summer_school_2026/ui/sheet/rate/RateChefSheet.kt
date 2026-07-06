package com.qpeyba.surf_slop_summer_school_2026.ui.sheet.rate

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qpeyba.surf_slop_summer_school_2026.domain.model.BookingWithSlot
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ChefButton
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ChefInfoRow
import com.qpeyba.surf_slop_summer_school_2026.ui.components.StarRating
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.Terracotta
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextPrimary
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextSecondary
import com.qpeyba.surf_slop_summer_school_2026.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateChefSheet(
    bookingWithSlot: BookingWithSlot,
    onDismiss: () -> Unit,
    onConfirmed: () -> Unit,
    viewModel: RateChefViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(bookingWithSlot) {
        viewModel.init(bookingWithSlot)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (state.isEditing) "Изменить оценку" else "Оценить шефа",
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = bookingWithSlot.slot.menu ?: "Программа",
                fontSize = 16.sp,
                color = TextPrimary
            )
            Text(
                text = DateFormatter.toReadableDateTime(bookingWithSlot.slot.dateTime),
                fontSize = 13.sp,
                color = TextSecondary
            )
            if (bookingWithSlot.slot.instructor != null) {
                ChefInfoRow(name = bookingWithSlot.slot.instructor.name, rating = bookingWithSlot.slot.instructor.rating)
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            StarRating(
                rating = state.rating,
                onRatingChanged = { viewModel.onEvent(RateChefEvent.RatingChanged(it)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.text,
                onValueChange = { viewModel.onEvent(RateChefEvent.TextChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Поделитесь впечатлениями (необязательно)") },
                supportingText = { Text("${state.text.length}/500") },
                minLines = 3,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Terracotta,
                    cursorColor = Terracotta
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            ChefButton(
                text = if (state.isEditing) "Сохранить" else "Отправить",
                onClick = {
                    viewModel.onEvent(RateChefEvent.SubmitPressed)
                    onConfirmed()
                },
                enabled = state.rating > 0 && !state.isSubmitting,
                isLoading = state.isSubmitting
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

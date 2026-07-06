package com.qpeyba.surf_slop_summer_school_2026.ui.sheet.cancel

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qpeyba.surf_slop_summer_school_2026.domain.model.BookingWithSlot
import com.qpeyba.surf_slop_summer_school_2026.domain.model.CancellationVariant
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ChefButton
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ChefDestructiveButton
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ChefInfoRow
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ChefSecondaryButton
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.DestructiveRed
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.SuccessGreen
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextPrimary
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextSecondary
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.WarningOrange
import com.qpeyba.surf_slop_summer_school_2026.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CancelBookingSheet(
    bookingWithSlot: BookingWithSlot,
    onDismiss: () -> Unit,
    onConfirmed: (String) -> Unit,
    viewModel: CancelBookingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(bookingWithSlot) {
        viewModel.init(bookingWithSlot)
    }

    val booking = state.bookingWithSlot
    if (booking == null) return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Отменить бронь?",
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = booking.slot.menu ?: "Программа",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = TextPrimary
            )
            Text(
                text = DateFormatter.toReadableDateTime(booking.slot.dateTime),
                fontSize = 14.sp,
                color = TextSecondary
            )
            if (booking.slot.instructor != null) {
                ChefInfoRow(name = booking.slot.instructor.name, rating = booking.slot.instructor.rating)
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            state.cancellationInfo?.let { info ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (icon, iconColor) = when (info.variant) {
                        CancellationVariant.A -> Icons.Default.CheckCircle to SuccessGreen
                        CancellationVariant.B -> Icons.Default.Warning to WarningOrange
                        CancellationVariant.STARTED -> Icons.Default.Warning to DestructiveRed
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = info.warningText,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (state.cancellationInfo?.canCancel == true) {
                ChefDestructiveButton(
                    text = "Да, отменить",
                    onClick = {
                        viewModel.onEvent(CancelBookingEvent.ConfirmPressed)
                        onConfirmed(booking.booking.id)
                    },
                    enabled = !state.isProcessing
                )
                Spacer(modifier = Modifier.height(8.dp))
                ChefSecondaryButton(
                    text = "Оставить",
                    onClick = { onDismiss() }
                )
            } else {
                ChefButton(
                    text = "Понятно",
                    onClick = onDismiss
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

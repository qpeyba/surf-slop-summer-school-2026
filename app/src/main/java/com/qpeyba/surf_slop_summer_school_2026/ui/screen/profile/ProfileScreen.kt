package com.qpeyba.surf_slop_summer_school_2026.ui.screen.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qpeyba.surf_slop_summer_school_2026.domain.model.EquipmentType
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ChefButton
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ChefChip
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ChefDestructiveButton
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ChefSecondaryButton
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ChefTopAppBar
import com.qpeyba.surf_slop_summer_school_2026.ui.components.EquipmentSelector
import com.qpeyba.surf_slop_summer_school_2026.ui.components.ErrorState
import com.qpeyba.surf_slop_summer_school_2026.ui.components.LoadingSkeleton
import com.qpeyba.surf_slop_summer_school_2026.ui.components.LoyaltyProgressBar
import com.qpeyba.surf_slop_summer_school_2026.ui.components.SkeletonVariant
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextPrimary
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextSecondary
import com.qpeyba.surf_slop_summer_school_2026.util.PhoneMask
import com.qpeyba.surf_slop_summer_school_2026.util.UiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        ChefTopAppBar(title = "Профиль")

        when (val userState = state.user) {
            is UiState.Loading -> {
                LoadingSkeleton(variant = SkeletonVariant.DETAIL)
            }
            is UiState.Error -> {
                ErrorState(message = userState.message)
            }
            is UiState.Empty -> { }
            is UiState.Success -> {
                val user = userState.data

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = PhoneMask.apply(user.phone),
                        fontSize = 16.sp,
                        color = TextPrimary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    if (state.isAdmin) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Администратор",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = com.qpeyba.surf_slop_summer_school_2026.ui.theme.Terracotta,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Лояльность",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LoyaltyProgressBar(
                        points = user.loyaltyPoints,
                        status = user.loyaltyStatus
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Аллергии",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (user.allergies.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            user.allergies.forEach { allergy ->
                                ChefChip(
                                    text = allergy,
                                    onRemove = { viewModel.onEvent(ProfileEvent.RemoveAllergy(allergy)) }
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.OutlinedTextField(
                            value = state.allergiesInput,
                            onValueChange = { },  // TODO: add allergy input handler
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Аллергия") },
                            singleLine = true
                        )
                        ChefButton(
                            text = "+",
                            onClick = { viewModel.onEvent(ProfileEvent.AddAllergy(state.allergiesInput)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Снаряжение по умолчанию",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    EquipmentSelector(
                        selected = if (user.ownEquipment) EquipmentType.OWN else EquipmentType.RENTAL,
                        onSelectionChanged = { viewModel.onEvent(ProfileEvent.EquipmentChanged(it)) }
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    ChefSecondaryButton(
                        text = "Мои брони",
                        onClick = { viewModel.onEvent(ProfileEvent.MyBookingsPressed) }
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    ChefDestructiveButton(
                        text = "Выйти",
                        onClick = {
                            viewModel.onEvent(ProfileEvent.LogoutPressed)
                            onLogout()
                        }
                    )
                }
            }
        }
    }
}

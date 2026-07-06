package com.qpeyba.surf_slop_summer_school_2026.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.Card
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.DestructiveRed
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.Terracotta
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextPrimary
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.TextSecondary

@Composable
fun ChefButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(32.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Terracotta,
            contentColor = Card,
            disabledContainerColor = Terracotta.copy(alpha = 0.4f),
            disabledContentColor = Card.copy(alpha = 0.6f)
        )
    ) {
        Text(
            text = if (isLoading) "..." else text,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun ChefSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(32.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = TextPrimary
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun ChefDestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(32.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DestructiveRed,
            contentColor = Card,
            disabledContainerColor = DestructiveRed.copy(alpha = 0.4f),
            disabledContentColor = Card.copy(alpha = 0.6f)
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

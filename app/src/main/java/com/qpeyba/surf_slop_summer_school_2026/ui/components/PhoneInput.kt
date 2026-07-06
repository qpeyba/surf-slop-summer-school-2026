package com.qpeyba.surf_slop_summer_school_2026.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.qpeyba.surf_slop_summer_school_2026.util.PhoneMask

@Composable
fun PhoneInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    OutlinedTextField(
        value = PhoneMask.apply(value),
        onValueChange = { newValue ->
            val digits = newValue.filter { it.isDigit() }
            if (digits.length <= 11) {
                onValueChange(digits)
            }
        },
        modifier = modifier.fillMaxWidth(),
        label = { Text("Телефон") },
        placeholder = { Text("+7 ___ ___-__-__") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        singleLine = true,
        isError = isError,
        supportingText = if (errorMessage != null) {{ Text(errorMessage) }} else null,
        shape = RoundedCornerShape(16.dp)
    )
}

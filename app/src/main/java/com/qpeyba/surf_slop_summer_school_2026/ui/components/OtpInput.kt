package com.qpeyba.surf_slop_summer_school_2026.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.DestructiveRed
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.Terracotta

@Composable
fun OtpInput(
    code: String,
    onCodeChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    boxSize: Dp = 48.dp,
    codeLength: Int = 6
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(codeLength) { index ->
            val char = code.getOrNull(index)?.toString() ?: ""
            OutlinedTextField(
                value = char,
                onValueChange = { newValue ->
                    if (newValue.length <= 1) {
                        val newCode = code.toCharArray().let {
                            val arr = it + CharArray(codeLength - it.size) { ' ' }
                            arr[index] = newValue.firstOrNull() ?: ' '
                            String(arr).trimEnd().trim()
                        }
                        onCodeChanged(newCode)
                    }
                },
                modifier = Modifier.size(boxSize),
                textStyle = TextStyle(
                    textAlign = TextAlign.Center,
                    fontSize = 24.sp
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                isError = isError,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Terracotta,
                    cursorColor = Terracotta
                )
            )
        }
    }
}

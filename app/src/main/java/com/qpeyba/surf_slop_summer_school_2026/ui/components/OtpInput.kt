package com.qpeyba.surf_slop_summer_school_2026.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
    codeLength: Int = 4
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
                modifier = Modifier.width(84.dp).height(52.dp),
                textStyle = TextStyle(
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                isError = isError,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Terracotta,
                    cursorColor = Terracotta
                )
            )
        }
    }
}

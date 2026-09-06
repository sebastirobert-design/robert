package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Explicit color specifications for form inputs
val InputTextColor = Color(0xFF111827) // Dark black (#111827)
val InputBackgroundColor = Color(0xFFFFFFFF) // Pure white (#ffffff)
val InputFocusBorderColor = Color(0xFF1E3A8A) // Active/focus border (#1e3a8a)
val InputUnfocusedBorderColor = Color(0xFFCBD5E1) // Crisp gray border
val InputLabelUnfocusedColor = Color(0xFF4B5563) // Legible gray label
val InputPlaceholderColor = Color(0xFF9CA3AF)

// Typography with minimum 15px font size and ample line height for Tamil script
val AppInputTextStyle = TextStyle(
    color = InputTextColor,
    fontSize = 15.5.sp, // At least 15px
    lineHeight = 24.sp, // Ample line-height so Tamil glyphs and diacritics don't clip
    fontWeight = FontWeight.Normal
)

@Composable
fun appTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = InputTextColor,
    unfocusedTextColor = InputTextColor,
    disabledTextColor = InputTextColor.copy(alpha = 0.6f),
    errorTextColor = InputTextColor,
    focusedContainerColor = InputBackgroundColor,
    unfocusedContainerColor = InputBackgroundColor,
    disabledContainerColor = InputBackgroundColor,
    errorContainerColor = InputBackgroundColor,
    focusedBorderColor = InputFocusBorderColor, // #1e3a8a active focus state
    unfocusedBorderColor = InputUnfocusedBorderColor,
    focusedLabelColor = InputFocusBorderColor, // #1e3a8a active label
    unfocusedLabelColor = InputLabelUnfocusedColor,
    cursorColor = InputFocusBorderColor,
    selectionColors = TextSelectionColors(
        handleColor = InputFocusBorderColor,
        backgroundColor = InputFocusBorderColor.copy(alpha = 0.25f)
    ),
    focusedPlaceholderColor = InputPlaceholderColor,
    unfocusedPlaceholderColor = InputPlaceholderColor,
    focusedLeadingIconColor = InputFocusBorderColor,
    unfocusedLeadingIconColor = InputLabelUnfocusedColor,
    focusedTrailingIconColor = InputFocusBorderColor,
    unfocusedTrailingIconColor = InputLabelUnfocusedColor
)

/**
 * Standardized OutlinedTextField for all form inputs across the app.
 * - Explicit text color #111827 and background color #ffffff on light and dark themes.
 * - Font-size >= 15.5sp and line-height 24sp preventing Tamil character clipping.
 * - Active / focus state with border-color #1e3a8a.
 */
@Composable
fun AppOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: (@Composable () -> Unit)? = null,
    placeholder: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    supportingText: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    shape: Shape = RoundedCornerShape(10.dp)
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .background(InputBackgroundColor, shape)
            .defaultMinSize(minHeight = if (singleLine) 56.dp else 92.dp),
        textStyle = AppInputTextStyle,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        supportingText = supportingText,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        enabled = enabled,
        readOnly = readOnly,
        shape = shape,
        colors = appTextFieldColors()
    )
}

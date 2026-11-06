package com.freewdcmkt.bck.components.freewd


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FreewdLoadingDialog(text: String) {
    ModalBottomSheet(
        onDismissRequest = { },
        sheetGesturesEnabled = false,
        properties = ModalBottomSheetProperties(
            shouldDismissOnClickOutside = false,
            shouldDismissOnBackPress = false,
        )
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LoadingIndicator()
            Text(text, modifier = Modifier.padding(horizontal = 8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreewdEditDialog(
    modifier: Modifier = Modifier,
    text: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    title: String,
    maxLength: Int? = null,
    maxLines: Int = 1,
    hintMsg1: String?,
    hintMsg2: String?
) {
    val textState = rememberSaveable(text, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text = text, selection = TextRange(0, text.length)))
    }
    val focusRequester = remember { FocusRequester() }

    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )


    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets.ime }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = textState.value,
                onValueChange = { newValue ->
                    textState.value = if (maxLength == null || newValue.text.length <= maxLength) {
                        newValue
                    } else {
                        newValue.copy(text = newValue.text.take(maxLength))
                    }
                },
                maxLines = maxLines,
                modifier = modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
            maxLength?.let {
                Text(
                    "${textState.value.text.length} / $maxLength",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (hintMsg1 != null) TextButton(onClick = onDismiss) { Text(hintMsg1) }

                Spacer(modifier = Modifier.width(8.dp))

                if (hintMsg2 != null) TextButton(
                    onClick = { onConfirm(textState.value.text) },
                    enabled = textState.value.text.isNotBlank()
                ) { Text(hintMsg2) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreewdModalBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    msg: String,
    cancelHint: String?,
    confirmHint: String?
) {

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            ContentText(msg)

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (cancelHint != null) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text(cancelHint)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (confirmHint != null) TextButton(
                    onClick = onConfirm
                ) {
                    Text(
                        text = confirmHint,
                        color = Color.Red
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreewdBottomSheetDialog(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
    rowContent: @Composable RowScope.() -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            content = content
        )
        Row(content = rowContent)
    }
}

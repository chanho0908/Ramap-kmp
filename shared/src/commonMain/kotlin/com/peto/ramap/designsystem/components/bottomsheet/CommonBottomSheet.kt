package com.peto.ramap.designsystem.components.bottomsheet

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.peto.ramap.theme.CommonColor
import com.peto.ramap.theme.GrayColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonBottomSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    config: CommonBottomSheetConfig = CommonBottomSheetConfig(),
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!visible) return

    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = false,
        )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = config.shape,
        containerColor = CommonColor.White,
        tonalElevation = 0.dp,
        scrimColor = config.scrimColor,
        dragHandle = {
            if (config.showHandle) {
                BottomSheetDefaults.DragHandle(
                    color = GrayColor.C100,
                )
            }
        },
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = maxHeight * config.maxHeightFraction),
                content = content,
            )
        }
    }
}

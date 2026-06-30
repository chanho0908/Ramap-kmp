package com.peto.ramap.ui.map.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.peto.ramap.core.extension.toFixedOneDecimal
import com.peto.ramap.designsystem.text.AppText
import com.peto.ramap.theme.AppTextStyle
import com.peto.ramap.theme.GrayColor
import org.jetbrains.compose.resources.stringResource
import ramap.shared.generated.resources.Res
import ramap.shared.generated.resources.shop_detail_kakao_rating_label

@Composable
fun RatingChip(
    rating: Double,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = GrayColor.C050,
        shape = RoundedCornerShape(999.dp),
        modifier = modifier,
    ) {
        AppText(
            text =
                "${stringResource(Res.string.shop_detail_kakao_rating_label)} ${rating.toFixedOneDecimal()}",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = AppTextStyle.B3,
            color = GrayColor.C500,
        )
    }
}

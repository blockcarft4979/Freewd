package com.freewdcmkt.bck.components.freewd

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freewdcmkt.bck.R


@Composable
fun FreewdCheckBox(checked: Boolean, text: String, onCheckBoxChanged: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.clickable(
            onClick = { onCheckBoxChanged(!checked) }, indication = null,
            interactionSource = remember { MutableInteractionSource() },
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckBoxChanged
        )
        Text(text, fontSize = 12.sp)
    }

}

//@Composable
//fun FreewdAgreementCheckBox(
//    checked: Boolean,
//    onCheckChanged: (Boolean) -> Unit,
//    onUrlClick: () -> Unit,
//) {
//    FlowRow(
//       // verticalAlignment = Alignment.CenterVertically,
//        verticalArrangement = Arrangement.Center,
//        modifier = Modifier.padding(vertical = 4.dp)
//    ) {
//        Checkbox(
//            checked = checked,
//            onCheckedChange = onCheckChanged
//        )
//
//        // 普通前缀文本
//        Text(text = stringResource(R.string.agree_prefix), fontSize = 14.sp)
//
//        // 可点击的用户协议
//        Text(
//            text = stringResource(R.string.agree_user_agreement),
//            fontSize = 14.sp,
//            color = MaterialTheme.colorScheme.primary,
//            fontWeight = FontWeight.Bold,
//            textDecoration = TextDecoration.Underline,
//            modifier = Modifier.clickable(
//                onClick = onUrlClick, indication = null,
//                interactionSource = remember { MutableInteractionSource() })
//
//        )
//
//        // 中间连接词
//        Text(text = stringResource(R.string.agree_middle), fontSize = 14.sp)
//
//        // 可点击的隐私政策
//        Text(
//            text = stringResource(R.string.agree_privacy_policy),
//            fontSize = 14.sp,
//            color = MaterialTheme.colorScheme.primary,
//            fontWeight = FontWeight.Bold,
//            textDecoration = TextDecoration.Underline,
//            modifier = Modifier.clickable(
//                onClick = onUrlClick, indication = null,
//                interactionSource = remember { MutableInteractionSource() })
//        )
//    }
//}
@Composable
fun FreewdAgreementCheckBox(
    checked: Boolean,
    onCheckChanged: (Boolean) -> Unit,
    onUrlClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckChanged
        )

        // 整段话作为一个 AnnotatedString，支持多语言顺序
        val agreementText = buildAnnotatedString {
            withStyle(SpanStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)) {
                append(stringResource(R.string.agree_prefix))
            }

            pushStringAnnotation(tag = "user_agreement", annotation = "")
            withStyle(
                SpanStyle(
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(stringResource(R.string.agree_user_agreement))
            }
            pop()

            withStyle(SpanStyle(fontSize = 14.sp,color = MaterialTheme.colorScheme.onSurface)) {
                append(stringResource(R.string.agree_middle))
            }

            pushStringAnnotation(tag = "privacy_policy", annotation = "")
            withStyle(
                SpanStyle(
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(stringResource(R.string.agree_privacy_policy))
            }
            pop()
        }

        ClickableText(
            text = agreementText,
            modifier = Modifier.fillMaxWidth(),
            onClick = { offset ->
                agreementText.getStringAnnotations(
                    tag = "user_agreement",
                    start = offset,
                    end = offset
                ).firstOrNull()?.let { onUrlClick() }

                agreementText.getStringAnnotations(
                    tag = "privacy_policy",
                    start = offset,
                    end = offset
                ).firstOrNull()?.let { onUrlClick() }
            }
        )
    }
}

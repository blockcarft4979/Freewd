package com.freewdcmkt.bck.components.freewd

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.ui.theme.FreewdTheme
import com.freewdcmkt.bck.util.formatTime
import com.mikepenz.markdown.coil2.Coil2ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownTypography
import java.util.regex.Pattern

@Composable
fun TitleText(text: String) {
    Text(
        text = text, style = TextStyle(
            fontSize = 15.sp,
            lineHeight = 20.sp,
            //platformStyle = PlatformTextStyle(includeFontPadding = false)
        ), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface
    )
}


@Composable
fun UsernameText(text: String) {
    Text(
        fontWeight = FontWeight.Bold,
        text = text,
        color = FreewdTheme.usernameColor,
        style = TextStyle(
            fontSize = 14.sp,
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun DateText(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(3.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        )
        Text(
            text = formatTime(text),
            style = TextStyle(
                fontSize = 10.sp,
                lineHeight = 16.sp,
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            ),
            color = Color.Gray,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun LevelText(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(3.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(0.4f))
        )
        Text(
            text = formatTime(text),
            style = TextStyle(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary.copy(0.95f),
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun UidText(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        )
        Text(
            text = formatTime(text),
            style = TextStyle(
                fontSize = 10.sp,
                lineHeight = 16.sp,
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            ),
            color = Color.Gray,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ContentText(text: String) {
    val annotatedString = buildAnnotatedString {
        val urlPattern = Pattern.compile(
            "((https?|ftp)://|www\\.)[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=]+", Pattern.CASE_INSENSITIVE
        )
        val matcher = urlPattern.matcher(text)
        var lastIndex = 0

        while (matcher.find()) {
            append(text.substring(lastIndex, matcher.start()))

            val url = text.substring(matcher.start(), matcher.end())
            withLink(
                LinkAnnotation.Url(
                    url = url, styles = TextLinkStyles(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary.copy(0.8f),
                            textDecoration = TextDecoration.Underline
                        )
                    )
                )
            ) {
                if (url.startsWith("https://community.freewd.top/u/page?id=")) {
                    append(stringResource(R.string.freewd_feed_hint))
                } else {
                    append(url)
                }
            }

            lastIndex = matcher.end()
        }
        append(text.substring(lastIndex))
    }

    Text(
        text = annotatedString, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun ContentMarkdown(text: String) {
    Markdown(
        content = text, typography = markdownTypography(
            h1 = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            h2 = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            h3 = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            h4 = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            h5 = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            h6 = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        ), imageTransformer = Coil2ImageTransformerImpl
    )
}
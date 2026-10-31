package com.freewdcmkt.bck.components.freewd

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.api.Link
import com.freewdcmkt.bck.components.freewd.LoadingCard
import com.freewdcmkt.bck.ui.theme.FreewdTheme
import com.freewdcmkt.bck.util.time.formatTime
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.utils.resolveImageLink
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.gfm.GFMTokenTypes
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
                if (url.startsWith(Link.FEED_LINK)) {
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
        content = text,
        imageTransformer = Coil3ImageTransformerImpl,
        typography = markdownTypography(
            h1 = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            h2 = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            h3 = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            h4 = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            h5 = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            h6 = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        ),

        components = markdownComponents(
            image = { imageData ->

                val imageUrl = imageData.node.resolveImageLink(
                    imageData.content,
                    referenceLinkHandler = null
                )

                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = stringResource(R.string.loading_hint),
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(Color.LightGray.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                    loading = {
                        LoadingCard()
                    },

                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.load_error_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },

                    success = {
                        SubcomposeAsyncImageContent()
                    }
                )
            },
            table = { tableData ->
                val rows = tableData.node.children.filter { rowNode ->
                    rowNode.children.any { it.type == GFMTokenTypes.CELL }
                }

                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)) {
                    rows.forEachIndexed { index, rowNode ->
                        val isHeader = (index == 0)
                        val headerBgColor = MaterialTheme.colorScheme.surfaceVariant
                        val headerTextColor = MaterialTheme.colorScheme.onSurfaceVariant

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isHeader) headerBgColor else Color.Transparent)
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = if (isHeader) Alignment.CenterVertically else Alignment.Top
                        ) {
                            rowNode.children.forEach { cell ->
                                if (cell.type == GFMTokenTypes.CELL) {
                                    Box(modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)) {
                                        MarkdownTableBasicText(
                                            content = tableData.content,
                                            cell = cell,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isHeader) headerTextColor else MaterialTheme.colorScheme.onSurface
                                            ),
                                            maxLines = Int.MAX_VALUE,
                                            overflow = TextOverflow.Visible
                                        )
                                    }
                                }
                            }
                        }
                        // 只有在真实的行下面才画线
                        HorizontalDivider(
                            color = Color(0xFFE5E7EB).copy(alpha = 0.5f),
                            thickness = 1.dp
                        )
                    }
                }
            }
        )
    )
}

// 辅助 Composable：从 CELL 节点中提取文本并渲染
@Composable
fun MarkdownTableBasicText(
    content: String,
    cell: ASTNode,
    style: TextStyle,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Visible
) {
    val cellText = cell.children
        .filter { it.type != MarkdownTokenTypes.EOL }
        .joinToString("") { it.getTextInNode(content).toString() }

    Text(
        text = cellText.trim(),
        style = style,
        maxLines = maxLines,
        overflow = overflow,
    )
}
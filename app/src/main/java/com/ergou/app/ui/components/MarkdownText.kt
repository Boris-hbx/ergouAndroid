package com.ergou.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 轻量级 Markdown 渲染
 * 支持：## 标题、**加粗**、*斜体*、`行内代码`、```代码块```、- 列表
 */
@Composable
fun MarkdownText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val annotatedString = remember(text) { parseMarkdown(text, color) }
    Text(
        text = annotatedString,
        modifier = modifier
    )
}

private fun parseMarkdown(text: String, defaultColor: Color): AnnotatedString {
    val lines = text.split('\n')
    return buildAnnotatedString {
        var inCodeBlock = false
        var codeBlockContent = StringBuilder()

        for ((lineIdx, line) in lines.withIndex()) {
            // 代码块开始/结束
            if (line.trimStart().startsWith("```")) {
                if (inCodeBlock) {
                    // 结束代码块
                    val code = codeBlockContent.toString().trimEnd()
                    if (code.isNotEmpty()) {
                        withStyle(SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            background = defaultColor.copy(alpha = 0.1f)
                        )) {
                            append(code)
                        }
                    }
                    codeBlockContent = StringBuilder()
                    inCodeBlock = false
                } else {
                    inCodeBlock = true
                }
                if (lineIdx < lines.lastIndex) append('\n')
                continue
            }

            if (inCodeBlock) {
                if (codeBlockContent.isNotEmpty()) codeBlockContent.append('\n')
                codeBlockContent.append(line)
                continue
            }

            val trimmed = line.trimStart()

            // 标题 ## / ### / ####
            when {
                trimmed.startsWith("#### ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp)) {
                        appendInlineMarkdown(trimmed.removePrefix("#### "), defaultColor)
                    }
                }
                trimmed.startsWith("### ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp)) {
                        appendInlineMarkdown(trimmed.removePrefix("### "), defaultColor)
                    }
                }
                trimmed.startsWith("## ") -> {
                    if (lineIdx > 0) append('\n')
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                        appendInlineMarkdown(trimmed.removePrefix("## "), defaultColor)
                    }
                }
                trimmed.startsWith("# ") -> {
                    if (lineIdx > 0) append('\n')
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
                        appendInlineMarkdown(trimmed.removePrefix("# "), defaultColor)
                    }
                }
                // 列表项 - / *
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    append("  \u2022 ")
                    appendInlineMarkdown(trimmed.substring(2), defaultColor)
                }
                // 数字列表
                trimmed.matches(Regex("""^\d+\.\s.*""")) -> {
                    val content = trimmed.replaceFirst(Regex("""^\d+\.\s"""), "")
                    val num = trimmed.substringBefore(".")
                    append("  $num. ")
                    appendInlineMarkdown(content, defaultColor)
                }
                else -> {
                    appendInlineMarkdown(line, defaultColor)
                }
            }
            if (lineIdx < lines.lastIndex) append('\n')
        }

        // 未关闭的代码块
        if (inCodeBlock && codeBlockContent.isNotEmpty()) {
            withStyle(SpanStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                background = defaultColor.copy(alpha = 0.1f)
            )) {
                append(codeBlockContent.toString().trimEnd())
            }
        }
    }
}

private fun AnnotatedString.Builder.appendInlineMarkdown(text: String, defaultColor: Color) {
    var i = 0
    val len = text.length

    while (i < len) {
        when {
            // 行内代码 `...`
            text[i] == '`' -> {
                val endIdx = text.indexOf('`', i + 1)
                if (endIdx != -1) {
                    withStyle(SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        background = defaultColor.copy(alpha = 0.1f)
                    )) {
                        append(text.substring(i + 1, endIdx))
                    }
                    i = endIdx + 1
                } else {
                    append(text[i])
                    i++
                }
            }
            // 加粗 **...**
            i + 1 < len && text[i] == '*' && text[i + 1] == '*' -> {
                val endIdx = text.indexOf("**", i + 2)
                if (endIdx != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(text.substring(i + 2, endIdx))
                    }
                    i = endIdx + 2
                } else {
                    append(text[i])
                    i++
                }
            }
            // 斜体 *...*
            text[i] == '*' -> {
                val endIdx = text.indexOf('*', i + 1)
                if (endIdx != -1 && endIdx > i + 1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(text.substring(i + 1, endIdx))
                    }
                    i = endIdx + 1
                } else {
                    append(text[i])
                    i++
                }
            }
            else -> {
                append(text[i])
                i++
            }
        }
    }
}

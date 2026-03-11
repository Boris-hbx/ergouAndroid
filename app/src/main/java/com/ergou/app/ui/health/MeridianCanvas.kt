package com.ergou.app.ui.health

import android.graphics.Typeface
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sqrt

private const val PARTICLE_COUNT = 7
private const val GRID_SPACING_DP = 40f

@Composable
fun MeridianDiagram(
    activeMeridians: List<Meridian>,
    viewSide: String,
    selectedAcupoint: Acupoint?,
    onAcupointTap: (Acupoint?) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val density = LocalDensity.current

    val gridSpacingPx = with(density) { GRID_SPACING_DP.dp.toPx() }
    val outlineWidthPx = with(density) { 1.5.dp.toPx() }
    val meridianWidthPx = with(density) { 3.dp.toPx() }
    val primaryMeridianWidthPx = with(density) { 4.dp.toPx() }
    val acupointRadiusPx = with(density) { 4.dp.toPx() }
    val keyAcupointRadiusPx = with(density) { 6.dp.toPx() }
    val selectedOuterRadiusPx = with(density) { 12.dp.toPx() }
    val selectedInnerRadiusPx = with(density) { 10.dp.toPx() }
    val hitTestRadiusPx = with(density) { 30.dp.toPx() }
    val particleOuterRadiusPx = with(density) { 8.dp.toPx() }
    val particleCoreRadiusPx = with(density) { 3.dp.toPx() }
    val labelOffsetPx = with(density) { 8.dp.toPx() }
    val labelSizePx = with(density) { 11.sp.toPx() }
    val tooltipCornerPx = with(density) { 6.dp.toPx() }
    val tooltipPaddingH = with(density) { 10.dp.toPx() }
    val tooltipPaddingV = with(density) { 8.dp.toPx() }
    val tooltipTitleSizePx = with(density) { 13.sp.toPx() }
    val tooltipSubSizePx = with(density) { 11.sp.toPx() }

    // Theme colors
    val bgColor = if (isDark) Color(0xFF1A1A2E) else Color(0xFFFAFAFA)
    val gridColor = if (isDark) Color(0xFF2A2A3E) else Color(0xFFF0F0F0)
    val outlineColor = if (isDark) Color(0xFF4A4A5E) else Color(0xFFD1D5DB)
    val spineColor = if (isDark) Color(0xFF4A4A5E) else Color(0xFFD1D5DB)
    val tooltipBg = if (isDark) Color(0xFF2A2A3E) else Color(0xFFFFFFFF)
    val tooltipText = if (isDark) Color(0xFFE5E5E5) else Color(0xFF1F2937)

    // Body outline data
    val bodyOutline = if (viewSide == "front") MeridianData.front else MeridianData.back

    // Pre-create native paint objects
    val labelPaint = remember(isDark) {
        android.graphics.Paint().apply {
            isAntiAlias = true
            textSize = labelSizePx
        }
    }
    val tooltipTitlePaint = remember(isDark) {
        android.graphics.Paint().apply {
            isAntiAlias = true
            textSize = tooltipTitleSizePx
            typeface = Typeface.DEFAULT_BOLD
            color = tooltipText.toArgb()
        }
    }
    val tooltipSubPaint = remember(isDark) {
        android.graphics.Paint().apply {
            isAntiAlias = true
            textSize = tooltipSubSizePx
            color = tooltipText.toArgb()
        }
    }
    val glowPaint = remember {
        android.graphics.Paint().apply {
            isAntiAlias = true
            style = android.graphics.Paint.Style.STROKE
        }
    }

    // Collect all acupoints with their screen positions for hit testing
    val allAcupointsWithPositions = remember(activeMeridians, viewSide) {
        buildList {
            for (meridian in activeMeridians) {
                for (acupoint in meridian.acupoints) {
                    val pos = if (viewSide == "front") acupoint.positionFront else acupoint.positionBack
                    if (pos != null) {
                        add(Triple(acupoint, pos, false)) // false = not mirrored
                        if (meridian.limbType == "hand" || meridian.limbType == "foot") {
                            add(Triple(acupoint, Pt(1f - pos.x, pos.y), true))
                        }
                    }
                }
            }
        }
    }

    // Qi particle animation
    val infiniteTransition = rememberInfiniteTransition(label = "qi_particles")
    val animationOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "qi_offset"
    )

    // Tap detection modifier
    val tapModifier = Modifier.pointerInput(allAcupointsWithPositions, selectedAcupoint) {
        detectTapGestures { tapOffset ->
            val w = size.width.toFloat()
            val h = size.height.toFloat()
            var tappedAcupoint: Acupoint? = null
            var minDist = Float.MAX_VALUE

            for ((acupoint, pos, _) in allAcupointsWithPositions) {
                val px = pos.x * w
                val py = pos.y * h
                val dx = tapOffset.x - px
                val dy = tapOffset.y - py
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < hitTestRadiusPx && dist < minDist) {
                    minDist = dist
                    tappedAcupoint = acupoint
                }
            }

            if (tappedAcupoint != null) {
                onAcupointTap(
                    if (tappedAcupoint.id == selectedAcupoint?.id) null else tappedAcupoint
                )
            } else {
                onAcupointTap(null)
            }
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .then(tapModifier)
    ) {
        val w = size.width
        val h = size.height

        // 1. Background
        drawRect(color = bgColor)

        // 2. Grid
        var gx = gridSpacingPx
        while (gx < w) {
            drawLine(gridColor, Offset(gx, 0f), Offset(gx, h), strokeWidth = 1f)
            gx += gridSpacingPx
        }
        var gy = gridSpacingPx
        while (gy < h) {
            drawLine(gridColor, Offset(0f, gy), Offset(w, gy), strokeWidth = 1f)
            gy += gridSpacingPx
        }

        // 3. Body outline
        val outlineParts = mapOf(
            "head" to bodyOutline.head,
            "neckLeft" to bodyOutline.neckLeft, "neckRight" to bodyOutline.neckRight,
            "torsoLeft" to bodyOutline.torsoLeft, "torsoRight" to bodyOutline.torsoRight,
            "armLeftOuter" to bodyOutline.armLeftOuter, "armLeftInner" to bodyOutline.armLeftInner,
            "armRightOuter" to bodyOutline.armRightOuter, "armRightInner" to bodyOutline.armRightInner,
            "legLeftOuter" to bodyOutline.legLeftOuter, "legLeftInner" to bodyOutline.legLeftInner,
            "legRightOuter" to bodyOutline.legRightOuter, "legRightInner" to bodyOutline.legRightInner
        )
        for ((partName, points) in outlineParts) {
            if (points.size < 2) continue
            val closePath = (partName == "head")
            drawSmoothPath(points, size, closePath, outlineColor, outlineWidthPx)
        }

        // 4. Spine line for back view
        if (viewSide == "back") {
            val spineStart = Offset(0.5f * w, 0.12f * h)
            val spineEnd = Offset(0.5f * w, 0.42f * h)
            drawLine(
                color = spineColor,
                start = spineStart,
                end = spineEnd,
                strokeWidth = outlineWidthPx,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
            )
        }

        // 5. Meridian paths and particles
        for (meridian in activeMeridians) {
            val pathPoints = if (viewSide == "front") meridian.pathFront else meridian.pathBack
            if (pathPoints.size < 2) continue

            val mColor = parseColorSafe(meridian.color)
            val isPrimary = meridian.acupoints.any { detail ->
                // Consider primary if it has key acupoints
                detail.isKey
            } || activeMeridians.size <= 3
            val strokeW = if (isPrimary) primaryMeridianWidthPx else meridianWidthPx

            // Draw main path
            if (isPrimary) {
                drawSmoothPathWithGlow(pathPoints, size, mColor, strokeW, glowPaint)
            } else {
                drawSmoothPath(pathPoints, size, false, mColor, strokeW)
            }

            // Draw mirrored path for hand/foot meridians
            if (meridian.limbType == "hand" || meridian.limbType == "foot") {
                val mirrored = mirrorPoints(pathPoints)
                if (isPrimary) {
                    drawSmoothPathWithGlow(mirrored, size, mColor, strokeW, glowPaint)
                } else {
                    drawSmoothPath(mirrored, size, false, mColor, strokeW)
                }
            }

            // Qi particles on main path
            drawQiParticles(
                pathPoints, size, mColor, animationOffset,
                meridian.direction, particleOuterRadiusPx, particleCoreRadiusPx
            )

            // Qi particles on mirrored path
            if (meridian.limbType == "hand" || meridian.limbType == "foot") {
                val mirrored = mirrorPoints(pathPoints)
                drawQiParticles(
                    mirrored, size, mColor, animationOffset,
                    meridian.direction, particleOuterRadiusPx, particleCoreRadiusPx
                )
            }

            // Meridian label at start of path
            val startPt = pathPoints.first()
            val startX = startPt.x * w
            val startY = startPt.y * h
            val isLeftSide = startPt.x < 0.5f
            labelPaint.color = mColor.toArgb()
            labelPaint.textAlign = if (isLeftSide) {
                android.graphics.Paint.Align.LEFT
            } else {
                android.graphics.Paint.Align.RIGHT
            }
            val labelX = if (isLeftSide) startX + labelOffsetPx else startX - labelOffsetPx
            drawContext.canvas.nativeCanvas.drawText(
                meridian.shortName, labelX, startY + labelSizePx / 3f, labelPaint
            )

            // Mirrored label
            if (meridian.limbType == "hand" || meridian.limbType == "foot") {
                val mirStartX = (1f - startPt.x) * w
                val mirIsLeft = (1f - startPt.x) < 0.5f
                labelPaint.textAlign = if (mirIsLeft) {
                    android.graphics.Paint.Align.LEFT
                } else {
                    android.graphics.Paint.Align.RIGHT
                }
                val mirLabelX = if (mirIsLeft) mirStartX + labelOffsetPx else mirStartX - labelOffsetPx
                drawContext.canvas.nativeCanvas.drawText(
                    meridian.shortName, mirLabelX, startY + labelSizePx / 3f, labelPaint
                )
            }
        }

        // 6. Acupoints
        for (meridian in activeMeridians) {
            val mColor = parseColorSafe(meridian.color)
            for (acupoint in meridian.acupoints) {
                val pos = (if (viewSide == "front") acupoint.positionFront else acupoint.positionBack)
                    ?: continue
                val px = pos.x * w
                val py = pos.y * h
                val radius = if (acupoint.isKey) keyAcupointRadiusPx else acupointRadiusPx
                val isSelected = selectedAcupoint?.id == acupoint.id

                drawAcupointDot(
                    Offset(px, py), mColor, radius, isSelected,
                    selectedOuterRadiusPx, selectedInnerRadiusPx
                )

                // Mirrored acupoint
                if (meridian.limbType == "hand" || meridian.limbType == "foot") {
                    val mirPx = (1f - pos.x) * w
                    drawAcupointDot(
                        Offset(mirPx, py), mColor, radius, isSelected,
                        selectedOuterRadiusPx, selectedInnerRadiusPx
                    )
                }
            }
        }

        // 7. Tooltip for selected acupoint
        if (selectedAcupoint != null) {
            val pos = if (viewSide == "front") selectedAcupoint.positionFront
            else selectedAcupoint.positionBack
            if (pos != null) {
                val px = pos.x * w
                val py = pos.y * h

                val titleText = "${selectedAcupoint.name} (${selectedAcupoint.id})"
                val subText = selectedAcupoint.functions.firstOrNull() ?: ""

                val titleWidth = tooltipTitlePaint.measureText(titleText)
                val subWidth = if (subText.isNotEmpty()) tooltipSubPaint.measureText(subText) else 0f
                val contentWidth = maxOf(titleWidth, subWidth)
                val tooltipW = contentWidth + tooltipPaddingH * 2
                val tooltipH = if (subText.isNotEmpty()) {
                    tooltipTitleSizePx + tooltipSubSizePx + tooltipPaddingV * 2 + 4f
                } else {
                    tooltipTitleSizePx + tooltipPaddingV * 2
                }

                // Position tooltip above the acupoint
                var tooltipX = px - tooltipW / 2f
                var tooltipY = py - selectedOuterRadiusPx - tooltipH - 8f

                // Keep within canvas bounds
                if (tooltipX < 4f) tooltipX = 4f
                if (tooltipX + tooltipW > w - 4f) tooltipX = w - 4f - tooltipW
                if (tooltipY < 4f) tooltipY = py + selectedOuterRadiusPx + 8f

                // Shadow
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.15f),
                    topLeft = Offset(tooltipX + 2f, tooltipY + 2f),
                    size = Size(tooltipW, tooltipH),
                    cornerRadius = CornerRadius(tooltipCornerPx)
                )

                // Background
                drawRoundRect(
                    color = tooltipBg,
                    topLeft = Offset(tooltipX, tooltipY),
                    size = Size(tooltipW, tooltipH),
                    cornerRadius = CornerRadius(tooltipCornerPx)
                )

                // Title
                drawContext.canvas.nativeCanvas.drawText(
                    titleText,
                    tooltipX + tooltipPaddingH,
                    tooltipY + tooltipPaddingV + tooltipTitleSizePx * 0.85f,
                    tooltipTitlePaint
                )

                // Subtitle
                if (subText.isNotEmpty()) {
                    drawContext.canvas.nativeCanvas.drawText(
                        subText,
                        tooltipX + tooltipPaddingH,
                        tooltipY + tooltipPaddingV + tooltipTitleSizePx + 4f + tooltipSubSizePx * 0.85f,
                        tooltipSubPaint
                    )
                }
            }
        }
    }
}

// ── Helper functions ──

private fun DrawScope.drawSmoothPath(
    points: List<Pt>,
    canvasSize: Size,
    closePath: Boolean,
    color: Color,
    strokeWidth: Float
) {
    if (points.size < 2) return
    val path = buildSmoothPath(points, canvasSize, closePath)
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth)
    )
}

private fun DrawScope.drawSmoothPathWithGlow(
    points: List<Pt>,
    canvasSize: Size,
    color: Color,
    strokeWidth: Float,
    glowPaint: android.graphics.Paint
) {
    if (points.size < 2) return
    val path = buildSmoothPath(points, canvasSize, false)

    // Draw glow using native canvas
    val nativePath = android.graphics.Path()
    val iter = androidx.compose.ui.graphics.PathIterator(path)
    val pts = FloatArray(8)
    while (iter.hasNext()) {
        when (iter.next(pts)) {
            androidx.compose.ui.graphics.PathSegment.Type.Move ->
                nativePath.moveTo(pts[0], pts[1])
            androidx.compose.ui.graphics.PathSegment.Type.Line ->
                nativePath.lineTo(pts[2], pts[3])
            androidx.compose.ui.graphics.PathSegment.Type.Quadratic ->
                nativePath.quadTo(pts[2], pts[3], pts[4], pts[5])
            androidx.compose.ui.graphics.PathSegment.Type.Cubic ->
                nativePath.cubicTo(pts[2], pts[3], pts[4], pts[5], pts[6], pts[7])
            androidx.compose.ui.graphics.PathSegment.Type.Close ->
                nativePath.close()
            else -> {}
        }
    }

    glowPaint.apply {
        this.color = color.toArgb()
        this.strokeWidth = strokeWidth + 4f
        this.style = android.graphics.Paint.Style.STROKE
        this.maskFilter = android.graphics.BlurMaskFilter(8f, android.graphics.BlurMaskFilter.Blur.NORMAL)
        this.alpha = 80
    }
    drawContext.canvas.nativeCanvas.drawPath(nativePath, glowPaint)

    // Draw the crisp line on top
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth)
    )
}

private fun buildSmoothPath(
    points: List<Pt>,
    canvasSize: Size,
    closePath: Boolean
): Path {
    val path = Path()
    val w = canvasSize.width
    val h = canvasSize.height

    if (closePath) {
        // Closed path (e.g., head)
        val first = points[0]
        val second = points[1]
        val startX = (first.x + second.x) / 2f * w
        val startY = (first.y + second.y) / 2f * h
        path.moveTo(startX, startY)

        val len = points.size
        for (i in 1 until len) {
            val cp = points[i]
            val next = points[(i + 1) % len]
            val endX = (cp.x + next.x) / 2f * w
            val endY = (cp.y + next.y) / 2f * h
            path.quadraticTo(cp.x * w, cp.y * h, endX, endY)
        }
        // Final curve back to start
        val lastCp = points[0]
        path.quadraticTo(lastCp.x * w, lastCp.y * h, startX, startY)
        path.close()
    } else {
        // Open path
        path.moveTo(points[0].x * w, points[0].y * h)

        if (points.size == 2) {
            path.lineTo(points[1].x * w, points[1].y * h)
        } else {
            for (i in 0 until points.size - 2) {
                val cp = points[i + 1]
                val next = points[i + 2]
                val endX = (cp.x + next.x) / 2f * w
                val endY = (cp.y + next.y) / 2f * h
                path.quadraticTo(cp.x * w, cp.y * h, endX, endY)
            }
            // Last segment
            val secondToLast = points[points.size - 2]
            val last = points[points.size - 1]
            path.quadraticTo(
                secondToLast.x * w, secondToLast.y * h,
                last.x * w, last.y * h
            )
        }
    }

    return path
}

private fun pointAlongPath(points: List<Pt>, t: Float, canvasSize: Size): Offset {
    if (points.size < 2) return Offset(
        points.firstOrNull()?.let { it.x * canvasSize.width } ?: 0f,
        points.firstOrNull()?.let { it.y * canvasSize.height } ?: 0f
    )

    val w = canvasSize.width
    val h = canvasSize.height

    // Compute segment lengths
    val segments = mutableListOf<Float>()
    for (i in 0 until points.size - 1) {
        val dx = (points[i + 1].x - points[i].x) * w
        val dy = (points[i + 1].y - points[i].y) * h
        segments.add(sqrt(dx * dx + dy * dy))
    }
    val totalLength = segments.sum()
    if (totalLength == 0f) return Offset(points[0].x * w, points[0].y * h)

    val targetDist = t.coerceIn(0f, 1f) * totalLength
    var accumulated = 0f

    for (i in segments.indices) {
        val segLen = segments[i]
        if (accumulated + segLen >= targetDist) {
            val segT = if (segLen > 0f) (targetDist - accumulated) / segLen else 0f
            val x = (points[i].x + (points[i + 1].x - points[i].x) * segT) * w
            val y = (points[i].y + (points[i + 1].y - points[i].y) * segT) * h
            return Offset(x, y)
        }
        accumulated += segLen
    }

    return Offset(points.last().x * w, points.last().y * h)
}

private fun mirrorPoints(points: List<Pt>): List<Pt> =
    points.map { Pt(1f - it.x, it.y) }

private fun DrawScope.drawQiParticles(
    points: List<Pt>,
    canvasSize: Size,
    color: Color,
    animationOffset: Float,
    direction: String,
    outerRadius: Float,
    coreRadius: Float
) {
    for (i in 0 until PARTICLE_COUNT) {
        var t = (i.toFloat() / PARTICLE_COUNT + animationOffset) % 1f
        if (direction == "centripetal") {
            t = 1f - t
        }
        val pos = pointAlongPath(points, t, canvasSize)
        // Outer glow
        drawCircle(
            color = color.copy(alpha = 0.3f),
            radius = outerRadius,
            center = pos
        )
        // Inner core
        drawCircle(
            color = color,
            radius = coreRadius,
            center = pos
        )
    }
}

private fun DrawScope.drawAcupointDot(
    center: Offset,
    color: Color,
    radius: Float,
    isSelected: Boolean,
    selectedOuterRadius: Float,
    selectedInnerRadius: Float
) {
    if (isSelected) {
        // White ring
        drawCircle(
            color = Color.White,
            radius = selectedOuterRadius,
            center = center
        )
        // Colored fill
        drawCircle(
            color = color,
            radius = selectedInnerRadius,
            center = center
        )
    } else {
        drawCircle(
            color = color,
            radius = radius,
            center = center
        )
    }
}

private fun parseColorSafe(colorLong: Long): Color {
    return try {
        Color(colorLong)
    } catch (_: Exception) {
        Color(0xFF888888)
    }
}

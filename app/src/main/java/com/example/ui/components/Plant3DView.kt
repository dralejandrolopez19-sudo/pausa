package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.example.ui.theme.PausaPlantAura
import com.example.ui.theme.PausaSunYellow
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Interactive 3D Plant Growth Renderer with perspective projection, continuous level interpolation
 * from Level 1.0 (Semilla) -> Level 2.0 (Brote) -> Level 3.0 (En crecimiento) -> Level 4.0 (Floreciendo)
 * -> Level 5.0 (Árbol), Z-buffer depth sorting, cylindrical 3D shaded terracotta pot, wind sway,
 * and interactive horizontal drag rotation.
 */
@Composable
fun Plant3DIllustration(
    targetGrowthStage: Float, // Continuous 1.0f .. 5.0f
    modifier: Modifier = Modifier,
    showAuraCircle: Boolean = false,
    isWilted: Boolean = false,
    enableAutoTurntable: Boolean = true,
    enableDragRotation: Boolean = true
) {
    // Smoothly morph growth stage whenever targetGrowthStage changes (1.0 -> 5.0)
    val animatedStage by animateFloatAsState(
        targetValue = targetGrowthStage.coerceIn(1f, 5f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "plant_3d_growth_stage"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "plant_3d_motion")

    // Continuous 3D turntable rotation angle (radians)
    val autoAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "turntable_angle"
    )

    // Gentle organic wind sway & floating breath
    val windPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wind_phase"
    )

    var userDragAngle by remember { mutableFloatStateOf(0f) }

    val dragModifier = if (enableDragRotation) {
        Modifier.pointerInput(Unit) {
            detectHorizontalDragGestures { change, dragAmount ->
                change.consume()
                userDragAngle += dragAmount * 0.015f
            }
        }
    } else {
        Modifier
    }

    val effectiveYaw = (if (enableAutoTurntable) autoAngle else 0.45f) + userDragAngle

    Canvas(modifier = modifier.then(dragModifier)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val minDim = minOf(w, h)

        // 1. Background 3D Aura Sphere & Ground Shadow
        if (showAuraCircle) {
            val auraRadius = minDim * 0.45f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = if (isWilted) {
                        listOf(Color(0xFF2B5240), Color(0xFF1D3B2C))
                    } else if (com.example.ui.theme.isPausaDarkThemeActive) {
                        listOf(Color(0xFF233B2E), PausaPlantAura, Color(0xFF15241B))
                    } else {
                        listOf(Color(0xFFF1F5EC), PausaPlantAura, Color(0xFFD6E2D0))
                    },
                    center = Offset(cx - auraRadius * 0.2f, h * 0.40f),
                    radius = auraRadius * 1.25f
                ),
                radius = auraRadius,
                center = Offset(cx, h * 0.47f)
            )
        }

        // Ground contact shadow ellipse underneath the 3D pot
        val shadowWidth = w * 0.36f
        val shadowHeight = h * 0.055f
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x40143023), Color.Transparent),
                center = Offset(cx, h * 0.895f),
                radius = shadowWidth * 0.65f
            ),
            topLeft = Offset(cx - shadowWidth / 2f, h * 0.87f),
            size = Size(shadowWidth, shadowHeight)
        )

        // 2. 3D Terracotta Pot (Back Rim + Frustum Body + Soil + Front Rim)
        val potTopY = h * 0.69f
        val potBottomY = h * 0.885f
        val potTopHalfW = w * 0.165f
        val potBottomHalfW = w * 0.12f
        val ellipseDepthTop = h * 0.036f
        val ellipseDepthBottom = h * 0.026f

        // Pot Body with 3D cylindrical horizontal shading
        val potBodyPath = Path().apply {
            moveTo(cx - potTopHalfW, potTopY)
            lineTo(cx + potTopHalfW, potTopY)
            lineTo(cx + potBottomHalfW, potBottomY)
            quadraticTo(cx, potBottomY + ellipseDepthBottom, cx - potBottomHalfW, potBottomY)
            close()
        }
        drawPath(
            path = potBodyPath,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF9C7E5E), // Left specular highlight
                    Color(0xFF7B6348), // Mid terracotta
                    Color(0xFF594530)  // Right cylindrical shadow
                ),
                startX = cx - potTopHalfW,
                endX = cx + potTopHalfW
            )
        )

        // Pot Upper Rim Collar (3D Cylinder Ring)
        val rimHalfW = potTopHalfW * 1.12f
        val rimThickness = h * 0.034f
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFFAD8E6B),
                    Color(0xFF8F7456),
                    Color(0xFF68523B)
                ),
                startX = cx - rimHalfW,
                endX = cx + rimHalfW
            ),
            topLeft = Offset(cx - rimHalfW, potTopY - rimThickness * 0.45f),
            size = Size(rimHalfW * 2f, rimThickness),
            cornerRadius = CornerRadius(rimThickness / 2f, rimThickness / 2f)
        )

        // Dark 3D Soil Basin Ellipse on top of the pot
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF5C4938), Color(0xFF3A2C20)),
                center = Offset(cx - potTopHalfW * 0.2f, potTopY),
                radius = potTopHalfW
            ),
            topLeft = Offset(cx - potTopHalfW * 0.96f, potTopY - ellipseDepthTop * 0.55f),
            size = Size(potTopHalfW * 1.92f, ellipseDepthTop * 1.1f)
        )

        // 3. Build 3D Botanical Elements based on continuous animatedStage (1.0f .. 5.0f)
        val stage = animatedStage
        // Normalize progress from 1.0 to 5.0:
        val t1to5 = ((stage - 1f) / 4f).coerceIn(0f, 1f)
        // Tree phase (4.0 -> 5.0)
        val treeMorph = ((stage - 4.0f) / 1.0f).coerceIn(0f, 1f)
        // Blossom phase (3.2 -> 4.3, stays partially in 5.0)
        val flowerMorph = when {
            stage < 3.15f -> 0f
            stage <= 4.15f -> (stage - 3.15f).coerceIn(0f, 1f)
            else -> (1f - (stage - 4.15f) * 0.45f).coerceIn(0.45f, 1f)
        }

        // Stem height grows smoothly from Level 1 (short sprout) to Level 5 (tall trunk)
        val stemBaseY = potTopY - h * 0.005f
        val maxStemHeight = h * (0.14f + 0.34f * smoothStep(0f, 0.75f, t1to5))
        val stemTopY = stemBaseY - maxStemHeight
        val swayOffsetX = sin(windPhase) * (w * 0.018f) * (0.3f + 0.7f * t1to5) +
            (if (isWilted) w * 0.035f else 0f)

        // Draw 3D Stem / Trunk with cylindrical lighting & bark transition in Level 5
        draw3DStemAndBranches(
            cx = cx,
            stemBaseY = stemBaseY,
            stemTopY = stemTopY,
            swayOffsetX = swayOffsetX,
            w = w,
            h = h,
            stage = stage,
            treeMorph = treeMorph,
            yaw = effectiveYaw,
            isWilted = isWilted
        )

        // Collect 3D depth-sorted primitives (leaves, canopy spheres, 3D flowers, floating pollen)
        val renderQueue = mutableListOf<DepthPrimitive>()

        // Define phyllotaxis spiral leaf nodes up the stem
        // Each leaf appears at a specific birthStage between 1.0 and 4.2
        val leafSpecs = listOf(
            // Level 1: 2 baby cotyledon leaves
            Leaf3DSpec(birthStage = 1.0f, heightFrac = 0.78f, baseAzimuth = 0.0f, maxLen = 0.11f, maxWidth = 0.065f, pitchDeg = 28f),
            Leaf3DSpec(birthStage = 1.0f, heightFrac = 0.78f, baseAzimuth = PI.toFloat(), maxLen = 0.11f, maxWidth = 0.065f, pitchDeg = 28f),
            // Level 2: 3 sprout leaves
            Leaf3DSpec(birthStage = 1.35f, heightFrac = 0.52f, baseAzimuth = 1.1f, maxLen = 0.19f, maxWidth = 0.095f, pitchDeg = 22f),
            Leaf3DSpec(birthStage = 1.65f, heightFrac = 0.62f, baseAzimuth = 3.6f, maxLen = 0.19f, maxWidth = 0.095f, pitchDeg = 20f),
            Leaf3DSpec(birthStage = 1.85f, heightFrac = 0.92f, baseAzimuth = 5.2f, maxLen = 0.17f, maxWidth = 0.088f, pitchDeg = 42f),
            // Level 3: Lush growing leaves (6+ full 3D leaves)
            Leaf3DSpec(birthStage = 2.15f, heightFrac = 0.36f, baseAzimuth = 0.5f, maxLen = 0.24f, maxWidth = 0.115f, pitchDeg = 15f),
            Leaf3DSpec(birthStage = 2.40f, heightFrac = 0.42f, baseAzimuth = 2.7f, maxLen = 0.24f, maxWidth = 0.115f, pitchDeg = 18f),
            Leaf3DSpec(birthStage = 2.65f, heightFrac = 0.72f, baseAzimuth = 4.4f, maxLen = 0.22f, maxWidth = 0.110f, pitchDeg = 32f),
            Leaf3DSpec(birthStage = 2.85f, heightFrac = 0.84f, baseAzimuth = 1.9f, maxLen = 0.21f, maxWidth = 0.105f, pitchDeg = 36f),
            // Level 4: Additional upper foliage crown
            Leaf3DSpec(birthStage = 3.15f, heightFrac = 0.58f, baseAzimuth = 5.8f, maxLen = 0.23f, maxWidth = 0.112f, pitchDeg = 25f),
            Leaf3DSpec(birthStage = 3.45f, heightFrac = 0.96f, baseAzimuth = 0.8f, maxLen = 0.19f, maxWidth = 0.098f, pitchDeg = 48f),
            Leaf3DSpec(birthStage = 3.70f, heightFrac = 0.98f, baseAzimuth = 3.9f, maxLen = 0.19f, maxWidth = 0.098f, pitchDeg = 48f)
        )

        leafSpecs.forEachIndexed { idx, spec ->
            val rawGrowth = if (spec.birthStage == 1.0f) {
                1f
            } else {
                ((stage - spec.birthStage) / 0.55f).coerceIn(0f, 1f)
            }
            if (rawGrowth > 0.01f) {
                val growthEase = smoothStep(0f, 1f, rawGrowth)
                // In Level 5 (Tree), lower leaves tuck slightly into the canopy
                val canopyScale = if (spec.heightFrac < 0.5f) (1f - treeMorph * 0.45f) else 1f
                val finalScale = growthEase * canopyScale

                val attachY = stemBaseY - (stemBaseY - stemTopY) * spec.heightFrac
                val attachX = cx + swayOffsetX * (spec.heightFrac * spec.heightFrac)

                val angle3D = spec.baseAzimuth + effectiveYaw
                val cosA = cos(angle3D)
                val sinA = sin(angle3D) // Z depth (-1 back .. +1 front)

                val leafLenPx = minDim * spec.maxLen * finalScale
                val leafWidthPx = minDim * spec.maxWidth * finalScale

                // Project 3D leaf tip onto 2D screen with foreshortening and pitch
                val pitchRad = (spec.pitchDeg - (if (isWilted) 28f else 0f) + sin(windPhase + idx) * 5f) * (PI.toFloat() / 180f)
                val horizReach = leafLenPx * cos(pitchRad)
                val vertRise = leafLenPx * sin(pitchRad)

                // Foreshortening: when leaf points toward/away from camera (cosA near 0), horizontal X projection narrows,
                // while vertical tilt (sinA * foreshortenY) gives true 3D perspective
                val tipX = attachX + cosA * horizReach
                val tipY = attachY - vertRise + sinA * (leafLenPx * 0.24f)

                renderQueue.add(
                    DepthPrimitive(
                        zDepth = sinA * 0.8f + spec.heightFrac * 0.1f,
                        draw = {
                            draw3DLeaf(
                                start = Offset(attachX, attachY),
                                tip = Offset(tipX, tipY),
                                width = leafWidthPx * (0.58f + 0.42f * kotlin.math.abs(sinA.coerceAtLeast(0.2f))),
                                zDepth = sinA,
                                isWilted = isWilted
                            )
                        }
                    )
                )
            }
        }

        // Level 4+: 3D Blossoms rotating in 3D space around the foliage
        if (flowerMorph > 0.02f && !isWilted) {
            val flowerNodes = listOf(
                Triple(0.62f, 0.4f, 0.16f),
                Triple(0.75f, 2.1f, 0.17f),
                Triple(0.88f, 3.8f, 0.14f),
                Triple(0.54f, 5.1f, 0.18f),
                Triple(0.98f, 1.2f, 0.09f)
            )
            flowerNodes.forEachIndexed { idx, (hFrac, azim, radFrac) ->
                val fAngle = azim + effectiveYaw
                val cosF = cos(fAngle)
                val sinF = sin(fAngle)
                val orbitR = minDim * radFrac * (1f + treeMorph * 0.35f)
                val anchorY = stemBaseY - (stemBaseY - stemTopY) * hFrac - (treeMorph * h * 0.06f)
                val anchorX = cx + swayOffsetX * hFrac

                val fx = anchorX + cosF * orbitR
                val fy = anchorY + sinF * (orbitR * 0.32f) - h * 0.03f
                val blossomScale = smoothStep(0f, 1f, (flowerMorph * 1.2f - idx * 0.08f).coerceIn(0f, 1f))

                if (blossomScale > 0.01f) {
                    renderQueue.add(
                        DepthPrimitive(
                            zDepth = sinF * 0.9f + 0.15f,
                            draw = {
                                draw3DBlossom(
                                    center = Offset(fx, fy),
                                    radius = minDim * 0.045f * blossomScale,
                                    zDepth = sinF,
                                    rotation = effectiveYaw * 0.5f + idx
                                )
                            }
                        )
                    )
                }
            }
        }

        // Level 5: 3D Volumetric Tree Canopy Clusters orbiting in 3D space
        if (treeMorph > 0.01f) {
            val canopyScale = smoothStep(0f, 1f, treeMorph)
            val canopyCenterY = stemTopY - h * 0.03f
            val canopyCenterX = cx + swayOffsetX

            // 7 volumetric 3D foliage puffs in a 3D crown
            val puffs = listOf(
                CanopyPuff3D(azimuth = 0.0f, orbitR = 0.13f, yOffset = 0.02f, radius = 0.145f),
                CanopyPuff3D(azimuth = 1.25f, orbitR = 0.135f, yOffset = 0.01f, radius = 0.15f),
                CanopyPuff3D(azimuth = 2.50f, orbitR = 0.13f, yOffset = 0.02f, radius = 0.145f),
                CanopyPuff3D(azimuth = 3.75f, orbitR = 0.135f, yOffset = 0.01f, radius = 0.15f),
                CanopyPuff3D(azimuth = 5.00f, orbitR = 0.13f, yOffset = 0.02f, radius = 0.145f),
                CanopyPuff3D(azimuth = 0.6f, orbitR = 0.06f, yOffset = -0.07f, radius = 0.165f),
                CanopyPuff3D(azimuth = 3.7f, orbitR = 0.05f, yOffset = -0.09f, radius = 0.155f)
            )

            puffs.forEach { puff ->
                val a = puff.azimuth + effectiveYaw
                val cosP = cos(a)
                val sinP = sin(a)
                val px = canopyCenterX + cosP * (minDim * puff.orbitR * canopyScale)
                val py = canopyCenterY + (minDim * puff.yOffset * canopyScale) + sinP * (minDim * 0.035f * canopyScale)
                val pr = minDim * puff.radius * canopyScale

                renderQueue.add(
                    DepthPrimitive(
                        zDepth = sinP * 0.85f,
                        draw = {
                            val lightBoost = ((sinP + 1f) * 0.5f).coerceIn(0f, 1f)
                            val topHighlight = lerpColor(Color(0xFF3E7B56), Color(0xFF7BC091), lightBoost)
                            val midTone = lerpColor(Color(0xFF28583E), Color(0xFF4B8E65), lightBoost)
                            val deepShade = Color(0xFF1B3F2B)

                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(topHighlight, midTone, deepShade),
                                    center = Offset(px - pr * 0.28f, py - pr * 0.28f),
                                    radius = pr * 1.15f
                                ),
                                radius = pr,
                                center = Offset(px, py)
                            )
                        }
                    )
                )
            }
        }

        // Execute painter's algorithm (back-to-front Z-ordering)
        renderQueue.sortedBy { it.zDepth }.forEach { primitive ->
            primitive.draw(this)
        }
    }
}

private class DepthPrimitive(
    val zDepth: Float,
    val draw: DrawScope.() -> Unit
)

private data class Leaf3DSpec(
    val birthStage: Float,
    val heightFrac: Float,
    val baseAzimuth: Float,
    val maxLen: Float,
    val maxWidth: Float,
    val pitchDeg: Float
)

private data class CanopyPuff3D(
    val azimuth: Float,
    val orbitR: Float,
    val yOffset: Float,
    val radius: Float
)

private fun DrawScope.draw3DStemAndBranches(
    cx: Float,
    stemBaseY: Float,
    stemTopY: Float,
    swayOffsetX: Float,
    w: Float,
    h: Float,
    stage: Float,
    treeMorph: Float,
    yaw: Float,
    isWilted: Boolean
) {
    // Stem thickness scales from slender sprout in Level 1 to sturdy trunk in Level 5
    val baseHalfW = w * (0.014f + 0.028f * treeMorph + 0.006f * ((stage - 1f) / 4f))
    val topHalfW = baseHalfW * (0.45f + 0.25f * treeMorph)

    val stemLeftColor = if (isWilted) Color(0xFF8E9B79) else lerpColor(Color(0xFF4E8E68), Color(0xFF8C694A), treeMorph)
    val stemMidColor = if (isWilted) Color(0xFF758263) else lerpColor(Color(0xFF2E6346), Color(0xFF6B4F35), treeMorph)
    val stemRightShadow = if (isWilted) Color(0xFF586349) else lerpColor(Color(0xFF1B3F2B), Color(0xFF473321), treeMorph)

    val ctrlX = cx + swayOffsetX * 0.45f
    val ctrlY = (stemBaseY + stemTopY) * 0.5f

    val trunkPath = Path().apply {
        moveTo(cx - baseHalfW, stemBaseY)
        quadraticTo(ctrlX - baseHalfW * 0.7f, ctrlY, cx + swayOffsetX - topHalfW, stemTopY)
        lineTo(cx + swayOffsetX + topHalfW, stemTopY)
        quadraticTo(ctrlX + baseHalfW * 0.7f, ctrlY, cx + baseHalfW, stemBaseY)
        close()
    }

    drawPath(
        path = trunkPath,
        brush = Brush.horizontalGradient(
            colors = listOf(stemLeftColor, stemMidColor, stemRightShadow),
            startX = cx - baseHalfW * 1.5f,
            endX = cx + baseHalfW * 1.5f + swayOffsetX
        )
    )

    // 3D Tree Branches in Level 4.2..5.0
    if (treeMorph > 0.05f) {
        val branchAngles = listOf(0.4f, 2.4f, 4.3f)
        branchAngles.forEachIndexed { idx, baseA ->
            val a = baseA + yaw
            val cosB = cos(a)
            val sinB = sin(a)
            val startY = stemBaseY - (stemBaseY - stemTopY) * (0.68f + idx * 0.08f)
            val startX = cx + swayOffsetX * 0.65f
            val endX = startX + cosB * (w * 0.11f * treeMorph)
            val endY = startY - (h * 0.085f * treeMorph) + sinB * (h * 0.02f * treeMorph)

            drawLine(
                color = stemMidColor,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = baseHalfW * 0.85f * treeMorph,
                cap = StrokeCap.Round
            )
        }
    }
}

private fun DrawScope.draw3DLeaf(
    start: Offset,
    tip: Offset,
    width: Float,
    zDepth: Float, // -1 (back) to +1 (front)
    isWilted: Boolean
) {
    val dx = tip.x - start.x
    val dy = tip.y - start.y
    val nx = -dy
    val ny = dx
    val len = sqrt(nx * nx + ny * ny).coerceAtLeast(1f)
    val ux = (nx / len) * width
    val uy = (ny / len) * width

    val midX = (start.x + tip.x) * 0.5f
    val midY = (start.y + tip.y) * 0.5f

    // 3D lighting based on zDepth: front leaves catch warm sunlight, back leaves are deeper forest green
    val depthFactor = ((zDepth + 1f) * 0.5f).coerceIn(0f, 1f)
    val upperHalfColor = if (isWilted) {
        lerpColor(Color(0xFF7B8769), Color(0xFFA6B08E), depthFactor)
    } else {
        lerpColor(Color(0xFF2D6344), Color(0xFF76BA8B), depthFactor)
    }
    val lowerHalfColor = if (isWilted) {
        lerpColor(Color(0xFF667056), Color(0xFF8E9B79), depthFactor)
    } else {
        lerpColor(Color(0xFF204B33), Color(0xFF4A8C62), depthFactor)
    }

    // Upper blade half
    val upperHalf = Path().apply {
        moveTo(start.x, start.y)
        quadraticTo(midX + ux, midY + uy, tip.x, tip.y)
        lineTo(start.x, start.y)
        close()
    }
    drawPath(path = upperHalf, color = upperHalfColor)

    // Lower blade half (shaded 3D fold)
    val lowerHalf = Path().apply {
        moveTo(start.x, start.y)
        quadraticTo(midX - ux, midY - uy, tip.x, tip.y)
        lineTo(start.x, start.y)
        close()
    }
    drawPath(path = lowerHalf, color = lowerHalfColor)

    // Specular 3D central vein
    drawLine(
        color = if (depthFactor > 0.5f) Color(0x66D8F3E0) else Color(0x44143023),
        start = start,
        end = Offset(start.x + dx * 0.84f, start.y + dy * 0.84f),
        strokeWidth = (width * 0.11f).coerceAtLeast(1.5f),
        cap = StrokeCap.Round
    )
}

private fun DrawScope.draw3DBlossom(
    center: Offset,
    radius: Float,
    zDepth: Float,
    rotation: Float
) {
    val depthFactor = ((zDepth + 1f) * 0.5f).coerceIn(0f, 1f)
    val petalColor = lerpColor(Color(0xFFD9AE34), PausaSunYellow, depthFactor)
    val petalHighlight = Color(0xFFFFE899)

    // 5 3D Petals
    for (i in 0 until 5) {
        val angle = rotation + i * (2f * PI.toFloat() / 5f)
        val px = center.x + cos(angle) * (radius * 0.65f)
        val py = center.y + sin(angle) * (radius * 0.52f) // Foreshortened 3D petal ring
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(petalHighlight, petalColor),
                center = Offset(px - radius * 0.1f, py - radius * 0.1f),
                radius = radius * 0.6f
            ),
            radius = radius * 0.48f,
            center = Offset(px, py)
        )
    }

    // 3D Pistil Center
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFF2994A), Color(0xFFB85D19)),
            center = Offset(center.x - radius * 0.1f, center.y - radius * 0.1f),
            radius = radius * 0.4f
        ),
        radius = radius * 0.34f,
        center = center
    )
}

private fun smoothStep(edge0: Float, edge1: Float, x: Float): Float {
    val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}

private fun lerpColor(start: Color, stop: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = start.red + (stop.red - start.red) * f,
        green = start.green + (stop.green - start.green) * f,
        blue = start.blue + (stop.blue - start.blue) * f,
        alpha = start.alpha + (stop.alpha - start.alpha) * f
    )
}

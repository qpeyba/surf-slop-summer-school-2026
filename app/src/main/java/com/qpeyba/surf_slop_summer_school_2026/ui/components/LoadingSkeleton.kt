package com.qpeyba.surf_slop_summer_school_2026.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class SkeletonVariant {
    CARD,
    LIST,
    DETAIL
}

@Composable
fun LoadingSkeleton(
    variant: SkeletonVariant,
    modifier: Modifier = Modifier
) {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 200f, 0f),
        end = Offset(translateAnim, 0f)
    )

    fun Modifier.shimmer(): Modifier = this.clip(RoundedCornerShape(32.dp)).background(brush)

    when (variant) {
        SkeletonVariant.CARD -> {
            Column(
                modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(16.dp)).background(brush))
                Box(modifier = Modifier.fillMaxWidth().height(24.dp).clip(RoundedCornerShape(8.dp)).background(brush))
                Box(modifier = Modifier.fillMaxWidth(0.7f).height(16.dp).clip(RoundedCornerShape(8.dp)).background(brush))
                Box(modifier = Modifier.fillMaxWidth(0.5f).height(16.dp).clip(RoundedCornerShape(8.dp)).background(brush))
            }
        }
        SkeletonVariant.LIST -> {
            Column(
                modifier = modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(3) {
                    Box(modifier = Modifier.fillMaxWidth().height(280.dp).shimmer())
                }
            }
        }
        SkeletonVariant.DETAIL -> {
            Column(
                modifier = modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(16.dp)).background(brush))
                Box(modifier = Modifier.fillMaxWidth(0.9f).height(28.dp).clip(RoundedCornerShape(8.dp)).background(brush))
                Box(modifier = Modifier.fillMaxWidth(0.6f).height(20.dp).clip(RoundedCornerShape(8.dp)).background(brush))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.width(80.dp).height(24.dp).clip(RoundedCornerShape(8.dp)).background(brush))
                    Box(modifier = Modifier.width(80.dp).height(24.dp).clip(RoundedCornerShape(8.dp)).background(brush))
                }
                Box(modifier = Modifier.fillMaxWidth().height(36.dp).shimmer())
                Box(modifier = Modifier.fillMaxWidth().height(56.dp).shimmer())
            }
        }
    }
}

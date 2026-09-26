package com.freewdcmkt.bck.components.freewd

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun IconTextButton(
    modifier: Modifier = Modifier,
    icon: Int,
    description: String? = null,
    text: String,
    onClick: () -> Unit,
    pulseOnClick: Boolean = false,
) {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    Row(
        modifier = modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) {
                onClick()
                if (pulseOnClick) {
                    scope.launch {
                        scale.animateTo(
                            targetValue = 1.2f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                        scale.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    }
                }

            }
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }) {
            Icon(
                painter = painterResource(icon),
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = description,
                modifier = Modifier
                    .size(22.dp)
            )
            Text(text = text, fontSize = 14.sp)
        }

    }
}
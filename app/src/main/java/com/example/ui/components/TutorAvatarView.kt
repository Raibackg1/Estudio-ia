package com.example.ui.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.example.ui.viewmodel.TutorAvatarState

@Composable
fun TutorAvatarView(
    avatarState: TutorAvatarState,
    isCameraOn: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (avatarState == TutorAvatarState.SPEAKING || avatarState == TutorAvatarState.LISTENING) 1.08f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val haloGlow: Color by infiniteTransition.animateColor(
        initialValue = when (avatarState) {
            TutorAvatarState.SPEAKING -> CyanAccent
            TutorAvatarState.LISTENING -> EmeraldSuccess
            TutorAvatarState.THINKING -> AmberWarning
            TutorAvatarState.IDLE -> IndigoLight
        },
        targetValue = when (avatarState) {
            TutorAvatarState.SPEAKING -> IndigoPrimary
            TutorAvatarState.LISTENING -> CyanAccent
            TutorAvatarState.THINKING -> RoseHighlight
            TutorAvatarState.IDLE -> CyanAccent
        },
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloColor"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(190.dp)
            .testTag("tutor_avatar_container")
    ) {
        // Outer Pulsing Aura
        Box(
            modifier = Modifier
                .size(180.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            haloGlow.copy(alpha = 0.45f),
                            haloGlow.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Middle Glowing Ring
        Box(
            modifier = Modifier
                .size(145.dp)
                .clip(CircleShape)
                .border(
                    width = 3.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(haloGlow, IndigoPrimary, CyanAccent, haloGlow)
                    ),
                    shape = CircleShape
                )
                .padding(4.dp)
        ) {
            // Tutor Avatar Image or Fallback Vector
            Image(
                painter = painterResource(id = R.drawable.img_avatar_tutor),
                contentDescription = "Sofía - Tu Tutora IA",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }

        // Status Floating Badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 10.dp)
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Slate900, Slate800)
                    )
                )
                .border(1.dp, haloGlow, CircleShape)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = when (avatarState) {
                        TutorAvatarState.SPEAKING -> Icons.Default.RecordVoiceOver
                        TutorAvatarState.LISTENING -> Icons.Default.Mic
                        TutorAvatarState.THINKING -> Icons.Default.Psychology
                        TutorAvatarState.IDLE -> Icons.Default.School
                    },
                    contentDescription = null,
                    tint = haloGlow,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = when (avatarState) {
                        TutorAvatarState.SPEAKING -> "Sofía Explicando"
                        TutorAvatarState.LISTENING -> "Escuchándote..."
                        TutorAvatarState.THINKING -> "Razonando..."
                        TutorAvatarState.IDLE -> "Sofía en Línea"
                    },
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

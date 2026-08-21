package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.StudyShareIntentHandler
import com.example.ui.theme.WhatsAppGreen

@Composable
fun WhatsAppShareButton(
    title: String,
    content: String,
    category: String = "Estudio",
    label: String = "Compartir en WhatsApp",
    isOutlined: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (isOutlined) {
        OutlinedButton(
            onClick = {
                StudyShareIntentHandler.shareAiSummary(context, title, content, category)
            },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = WhatsAppGreen
            ),
            border = BorderStroke(1.dp, WhatsAppGreen),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = modifier
                .minimumInteractiveComponentSize()
                .testTag("whatsapp_share_btn_outlined")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "WhatsApp",
                    modifier = Modifier.size(16.dp),
                    tint = WhatsAppGreen
                )
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = WhatsAppGreen
                )
            }
        }
    } else {
        Button(
            onClick = {
                StudyShareIntentHandler.shareAiSummary(context, title, content, category)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = WhatsAppGreen,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            modifier = modifier
                .minimumInteractiveComponentSize()
                .testTag("whatsapp_share_btn")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "WhatsApp",
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

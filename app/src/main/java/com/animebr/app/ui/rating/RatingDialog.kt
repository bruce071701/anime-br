package com.animebr.app.ui.rating

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

data class RatingContent(
    val emoji: String,
    val title: String,
    val description: String,
    val guidanceText: String? = null,
    val buttonText: String,
    val buttonEnabled: Boolean
)

private fun getRatingContent(rating: Int): RatingContent {
    return when (rating) {
        0 -> RatingContent(
            emoji = "😃",
            title = "Agradecemos o seu apoio!",
            description = "Ficaríamos muito gratos se você puder nos avaliar.",
            guidanceText = "O melhor que podemos ter",
            buttonText = "Avaliação",
            buttonEnabled = false
        )
        1 -> RatingContent(
            emoji = "😢",
            title = "Ah, sentimos muito…",
            description = "Fique à vontade para enviar seus comentários.",
            buttonText = "Avaliação",
            buttonEnabled = true
        )
        2 -> RatingContent(
            emoji = "😞",
            title = "Ah, sentimos muito…",
            description = "Fique à vontade para enviar seus comentários.",
            buttonText = "Avaliação",
            buttonEnabled = true
        )
        3 -> RatingContent(
            emoji = "😮",
            title = "Ah, sentimos muito…",
            description = "Fique à vontade para enviar seus comentários.",
            buttonText = "Avaliação",
            buttonEnabled = true
        )
        4 -> RatingContent(
            emoji = "😊",
            title = "Muito obrigado!",
            description = "Seu apoio é a nossa maior motivação!",
            buttonText = "Avaliação",
            buttonEnabled = true
        )
        5 -> RatingContent(
            emoji = "🥰",
            title = "Muito obrigado!",
            description = "Seu apoio é a nossa maior motivação!",
            buttonText = "Avaliar no Google Play",
            buttonEnabled = true
        )
        else -> getRatingContent(0)
    }
}

@Composable
fun RatingDialog(
    onDismiss: () -> Unit,
    onRatingSubmitted: (Int) -> Unit
) {
    var currentRating by remember { mutableIntStateOf(0) }
    val content = getRatingContent(currentRating)
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF2A2A2A))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Emoji
            Text(
                text = content.emoji,
                fontSize = 56.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = content.title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = content.description,
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            // Guidance text (only for empty rating)
            if (content.guidanceText != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = content.guidanceText,
                    color = Color(0xFFFF6B35),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Star rating row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..5) {
                    Icon(
                        imageVector = if (i <= currentRating) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = "Star $i",
                        tint = if (i <= currentRating) Color(0xFFFFD700) else Color.Gray,
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { currentRating = i }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit button
            Button(
                onClick = {
                    if (currentRating == 5) {
                        openGooglePlayStore(context)
                    } else {
                        Toast.makeText(
                            context,
                            "Obrigado pelo seu feedback.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    onRatingSubmitted(currentRating)
                },
                enabled = content.buttonEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF6B35),
                    disabledContainerColor = Color(0xFF555555),
                    contentColor = Color.White,
                    disabledContentColor = Color(0xFF999999)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = content.buttonText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun openGooglePlayStore(context: Context) {
    val packageName = context.packageName
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

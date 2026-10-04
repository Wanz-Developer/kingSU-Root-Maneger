package com.kingsu.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kingsu.model.TweakOption
import com.kingsu.ui.theme.DarkCardBorder
import com.kingsu.ui.theme.DarkSurface
import com.kingsu.ui.theme.DarkSurfaceVariant
import com.kingsu.ui.theme.KingGoldPrimary
import com.kingsu.ui.theme.NeonCyan
import com.kingsu.ui.theme.NeonGreen
import com.kingsu.ui.theme.TextMuted
import com.kingsu.ui.theme.TextPrimary
import com.kingsu.ui.theme.TextSecondary

@Composable
fun TweaksScreen(
    tweaks: List<TweakOption>,
    onApplyTweak: (TweakOption) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Kernel & System Tweakers",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = KingGoldPrimary
            )
        )
        Text(
            text = "Optimasi tingkat lanjut langsung ke kernel & sistem via Root Shell",
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tweaks) { tweak ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, if (tweak.isApplied) NeonGreen else DarkCardBorder, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (tweak.isApplied) Icons.Default.CheckCircle else Icons.Default.FlashOn,
                                    contentDescription = null,
                                    tint = if (tweak.isApplied) NeonGreen else KingGoldPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = tweak.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = tweak.category.title,
                                        fontSize = 10.sp,
                                        color = NeonCyan,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Button(
                                onClick = { onApplyTweak(tweak) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (tweak.isApplied) NeonGreen.copy(alpha = 0.2f) else DarkSurfaceVariant,
                                    contentColor = if (tweak.isApplied) NeonGreen else KingGoldPrimary
                                )
                            ) {
                                Text(
                                    text = if (tweak.isApplied) "Applied" else "Apply",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = tweak.description,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$ ${tweak.command}",
                            fontSize = 10.sp,
                            color = TextMuted,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSurfaceVariant)
                                .padding(6.dp)
                        )
                    }
                }
            }
        }
    }
}

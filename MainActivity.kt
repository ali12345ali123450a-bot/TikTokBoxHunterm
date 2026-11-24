package com.boxhunter.app

import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val Gold = Color(0xFFFFC247)
private val Bg = Color(0xFF090B10)
private val Panel = Color(0xFF171B24)
private val Muted = Color(0xFF9AA3B2)

data class BoxLive(val creator: String, val coins: Int, val url: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BoxHunterApp(
            onOpenUrl = { url -> openUrl(url) },
            onAlert = { alertUser() }
        ) }
    }

    private fun openUrl(raw: String) {
        val normalized = if (raw.startsWith("http://") || raw.startsWith("https://")) raw else "https://$raw"
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(normalized)))
    }

    private fun alertUser() {
        try {
            val tone = ToneGenerator(AudioManager.STREAM_ALARM, 85)
            tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 900)
            window.decorView.postDelayed({ tone.release() }, 1100)
        } catch (_: Exception) {}
        try {
            val vibrator = if (android.os.Build.VERSION.SDK_INT >= 31) {
                getSystemService(VibratorManager::class.java).defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(VIBRATOR_SERVICE) as Vibrator
            }
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 250, 120, 350), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(600)
            }
        } catch (_: Exception) {}
    }
}

@Composable
private fun BoxHunterApp(onOpenUrl: (String) -> Unit, onAlert: () -> Unit) {
    var secondsInput by remember { mutableStateOf("30") }
    var remaining by remember { mutableIntStateOf(30) }
    var running by remember { mutableStateOf(false) }
    var rounds by remember { mutableIntStateOf(0) }

    var creator by remember { mutableStateOf("") }
    var coinsText by remember { mutableStateOf("200") }
    var liveUrl by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("الكل") }
    val lives = remember { mutableStateListOf<BoxLive>() }

    LaunchedEffect(running) {
        while (running && remaining > 0) {
            delay(1000)
            remaining--
        }
        if (running && remaining == 0) {
            running = false
            rounds++
            onAlert()
        }
    }

    val shownLives = lives.filter {
        when (filter) {
            "أقل من 200" -> it.coins < 200
            "200 بالضبط" -> it.coins == 200
            "أكثر من 200" -> it.coins > 200
            else -> true
        }
    }.sortedByDescending { it.coins }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Gold, background = Bg, surface = Panel,
        onPrimary = Color.Black, onBackground = Color.White, onSurface = Color.White
    )) {
        Column(
            modifier = Modifier.fillMaxSize().background(Bg).padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bolt, null, tint = Gold, modifier = Modifier.size(30.dp))
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text("BOX HUNTER", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Text("TIKTOK LIVE BOX ASSISTANT", color = Muted, fontSize = 10.sp)
                }
                Text("V2.0", color = Gold, fontWeight = FontWeight.Bold)
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 18.dp, bottom = 20.dp)
            ) {
                item {
                    Surface(color = Panel, shape = RoundedCornerShape(22.dp)) {
                        Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("مؤقت الصندوق", color = Muted, fontSize = 12.sp)
                            Text("%02d:%02d".format(remaining / 60, remaining % 60), color = Gold, fontSize = 52.sp, fontWeight = FontWeight.ExtraBold)
                            OutlinedTextField(
                                value = secondsInput,
                                onValueChange = { v -> if (v.length <= 4 && v.all(Char::isDigit)) secondsInput = v },
                                label = { Text("المدة بالثواني") },
                                enabled = !running,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = {
                                    val n = (secondsInput.toIntOrNull() ?: 30).coerceIn(1, 3600)
                                    secondsInput = n.toString(); remaining = n; running = true
                                }, enabled = !running, colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.Black), modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.Timer, null); Spacer(Modifier.width(5.dp)); Text("ابدأ")
                                }
                                OutlinedButton(onClick = {
                                    running = false
                                    remaining = (secondsInput.toIntOrNull() ?: 30).coerceIn(1, 3600)
                                }, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.Refresh, null); Text("إعادة")
                                }
                            }
                            Text("عند انتهاء المؤقت يصدر تنبيه — الضغط النهائي منك", color = Muted, fontSize = 10.sp)
                        }
                    }
                }

                item {
                    Text("البثوث والصناديق 🎁", color = Gold, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Text("أضف رابط البث وقيمة الصندوق، ثم صفِّ النتائج حسب العملات.", color = Muted, fontSize = 11.sp)
                }
                item {
                    Surface(color = Panel, shape = RoundedCornerShape(18.dp)) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(creator, { creator = it }, label = { Text("اسم صاحب البث") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(coinsText, { v -> if (v.all(Char::isDigit)) coinsText = v }, label = { Text("عدد العملات") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(liveUrl, { liveUrl = it }, label = { Text("رابط البث على TikTok") }, placeholder = { Text("https://www.tiktok.com/@user/live") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            Button(
                                onClick = {
                                    val amount = coinsText.toIntOrNull()
                                    if (creator.isNotBlank() && amount != null && liveUrl.isNotBlank()) {
                                        lives.add(BoxLive(creator.trim(), amount, liveUrl.trim()))
                                        creator = ""; coinsText = "200"; liveUrl = ""
                                    }
                                },
                                enabled = creator.isNotBlank() && (coinsText.toIntOrNull() != null) && liveUrl.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.Black),
                                modifier = Modifier.fillMaxWidth()
                            ) { Icon(Icons.Default.Add, null); Text("أضف البث للقائمة") }
                        }
                    }
                }
                item {
                    Text("فلترة العملات", color = Color.White, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf("الكل", "أقل من 200", "200 بالضبط", "أكثر من 200").forEach { option ->
                            FilterChip(
                                selected = filter == option,
                                onClick = { filter = option },
                                label = { Text(option, fontSize = 10.sp) }
                            )
                        }
                    }
                }
                if (shownLives.isEmpty()) {
                    item {
                        Surface(color = Panel, shape = RoundedCornerShape(16.dp)) {
                            Text("لا توجد بثوث في هذه القائمة. أضف رابط بث وقيمة الصندوق لعرضه هنا.", color = Muted, modifier = Modifier.padding(16.dp))
                        }
                    }
                } else {
                    items(shownLives) { live ->
                        Surface(color = Panel, shape = RoundedCornerShape(16.dp)) {
                            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(live.creator, color = Color.White, fontWeight = FontWeight.Bold)
                                        Text("${live.coins} عملة", color = Gold, fontSize = 13.sp)
                                    }
                                    IconButton(onClick = { lives.remove(live) }) { Icon(Icons.Default.Delete, null, tint = Muted) }
                                }
                                OutlinedButton(onClick = { onOpenUrl(live.url) }, modifier = Modifier.fillMaxWidth()) {
                                    Icon(Icons.Default.OpenInNew, null); Spacer(Modifier.width(6.dp)); Text("افتح رابط البث")
                                }
                            }
                        }
                    }
                }
                item {
                    Text("الجولات المكتملة: $rounds", color = Muted, fontSize = 11.sp)
                    Text("ملاحظة: هذه النسخة تحفظ قائمة يدوية داخل الجلسة؛ لا تكتشف البثوث أو العملات تلقائياً.", color = Muted, fontSize = 10.sp)
                }
            }
        }
    }
}

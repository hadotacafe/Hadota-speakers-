package com.hadotacafe.speakers

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SettingsBluetooth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Brown = Color(0xFF211812)
private val Panel = Color(0xFF30231B)
private val Gold = Color(0xFFD6AE62)
private val Cream = Color(0xFFF4E8D2)
private val Muted = Color(0xFFB9A995)

class MainActivity : ComponentActivity() {
    private val permission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { loadDevices() }
    private val deviceNames = mutableStateListOf<String>()
    private var permissionGranted by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Gold, background = Brown, surface = Panel, onSurface = Cream)) {
                SpeakerScreen(
                    devices = deviceNames,
                    onRefresh = { refreshDevices() },
                    onBluetoothSettings = { startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) },
                    onPermission = { requestBluetoothPermission() },
                    permissionGranted = permissionGranted,
                    context = this
                )
            }
        }
        requestBluetoothPermission()
    }

    private fun requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= 31 && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            permission.launch(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            permissionGranted = true
            loadDevices()
        }
    }

    private fun refreshDevices() {
        if (Build.VERSION.SDK_INT >= 31 && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestBluetoothPermission()
        } else loadDevices()
    }

    private fun loadDevices() {
        deviceNames.clear()
        try {
            val adapter = BluetoothAdapter.getDefaultAdapter()
            if (adapter != null) deviceNames.addAll(adapter.bondedDevices.map { it.name ?: "Bluetooth device" }.sorted())
            permissionGranted = true
        } catch (_: SecurityException) {
            permissionGranted = false
        }
    }
}

@Composable
private fun SpeakerScreen(
    devices: List<String>,
    onRefresh: () -> Unit,
    onBluetoothSettings: () -> Unit,
    onPermission: () -> Unit,
    permissionGranted: Boolean,
    context: Context
) {
    val audio = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }
    var volume by remember { mutableFloatStateOf(audio.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()) }
    var selectedPreset by remember { mutableStateOf("Lounge") }
    val presets = listOf("Lounge", "Jazz", "Chill")

    Column(
        Modifier.fillMaxSize().background(Brown).padding(horizontal = 20.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("HADOTA", color = Gold, fontSize = 13.sp, letterSpacing = 4.sp, fontWeight = FontWeight.Bold)
                Text("Speakers", color = Cream, fontSize = 30.sp, fontWeight = FontWeight.SemiBold)
                Text("تحكم صوت الكافيه", color = Muted, fontSize = 14.sp)
            }
            Icon(Icons.Default.Bluetooth, null, tint = Gold, modifier = Modifier.size(34.dp))
        }

        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("مستوى الصوت الرئيسي", color = Cream, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("${(volume / maxVolume * 100).toInt()}%", color = Gold, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = volume, onValueChange = {
                        volume = it
                        audio.setStreamVolume(AudioManager.STREAM_MUSIC, it.toInt(), 0)
                    }, valueRange = 0f..maxVolume.toFloat(),
                    colors = SliderDefaults.colors(thumbColor = Gold, activeTrackColor = Gold, inactiveTrackColor = Muted)
                )
                Text("يتحكم في صوت الوسائط على الهاتف والجهاز المتصل.", color = Muted, fontSize = 12.sp)
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("أجهزة البلوتوث المحفوظة", color = Cream, fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.weight(1f))
                    IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, "تحديث", tint = Gold) }
                }
                if (!permissionGranted) {
                    Text("يلزم السماح بصلاحية البلوتوث لعرض الأجهزة.", color = Muted)
                    Button(onClick = onPermission) { Text("السماح") }
                } else if (devices.isEmpty()) {
                    Text("لا توجد أجهزة مقترنة. اقترن بالسماعة من إعدادات البلوتوث أولاً.", color = Muted, fontSize = 13.sp)
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 180.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(devices) { name ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bluetooth, null, tint = Gold)
                                Spacer(Modifier.width(10.dp))
                                Text(name, color = Cream, modifier = Modifier.weight(1f))
                                Text("مقترن", color = Muted, fontSize = 11.sp)
                            }
                        }
                    }
                }
                OutlinedButton(onClick = onBluetoothSettings, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.SettingsBluetooth, null)
                    Spacer(Modifier.width(8.dp))
                    Text("فتح إعدادات البلوتوث")
                }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("أوضاع الكافيه", color = Cream, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    presets.forEach { preset ->
                        FilterChip(
                            selected = selectedPreset == preset,
                            onClick = { selectedPreset = preset },
                            label = { Text(preset) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Gold, selectedLabelColor = Brown)
                        )
                    }
                }
                Text("اختيار الوضع يحفظ تفضيلك داخل التطبيق حالياً؛ لا يغيّر معادل الصوت في التطبيقات الأخرى.", color = Muted, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.weight(1f))
        Text("تنبيه: أندرويد لا يتيح عادةً التحكم المستقل في مستوى كل سماعة بلوتوث من تطبيق خارجي. التحكم الحالي هو مستوى صوت الوسائط العام.", color = Muted, fontSize = 11.sp)
    }
}

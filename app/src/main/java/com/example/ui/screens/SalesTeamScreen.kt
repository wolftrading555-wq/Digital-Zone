package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SalesOfficer
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.SkyBlueAccent
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.OilViewModel

@Composable
fun SalesTeamScreen(
    viewModel: OilViewModel
) {
    val officers by viewModel.salesOfficers.collectAsState()
    var editingOfficer by remember { mutableStateOf<SalesOfficer?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val totalTargets = officers.sumOf { it.monthlyTarget }
    val totalAchieved = officers.sumOf { it.currentSales }
    val overallPercentage = if (totalTargets > 0) ((totalAchieved / totalTargets) * 100).toInt() else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Sales Team & Quotas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Monthly Targets & Field Tracking", style = MaterialTheme.typography.bodySmall, color = AmberGold)
                }
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_officer_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Officer", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Cumulative Performance Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SlateBorder, AmberGold.copy(alpha = 0.3f))))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Overall Team Quota", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(viewModel.formatCurrency(totalAchieved), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AmberGold)
                            Text("Target: ${viewModel.formatCurrency(totalTargets)}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AmberGold.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "$overallPercentage%",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = AmberGold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { (overallPercentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = AmberGold,
                        trackColor = SlateBackground
                    )
                }
            }
        }

        // Section Title
        item {
            Text("Officer Performance Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        // Officers List (Ali, Ahmed, Usman)
        items(officers) { officer ->
            val percentage = officer.achievementPercentage
            val progressColor = when {
                percentage >= 90 -> EmeraldSuccess
                percentage >= 75 -> AmberGold
                else -> SkyBlueAccent
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("officer_item_${officer.name.lowercase()}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SlateBorder, progressColor.copy(alpha = 0.3f))))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(progressColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = officer.name.take(1),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = progressColor
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(officer.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                                Text(officer.territory, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = progressColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "$percentage%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = progressColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Target & Current Sales
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Achieved Sales", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(viewModel.formatCurrency(officer.currentSales), fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Monthly Target", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(viewModel.formatCurrency(officer.monthlyTarget), fontWeight = FontWeight.Bold, color = AmberGold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Recovery Done", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(viewModel.formatCurrency(officer.currentRecovery), fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { (percentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = progressColor,
                        trackColor = SlateBackground
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { editingOfficer = officer },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                        ) {
                            Icon(Icons.Default.TrackChanges, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Change Target", fontSize = 12.sp)
                        }

                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${officer.phone}"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SlateBackground)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = SkyBlueAccent, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Set Target Dialog
    editingOfficer?.let { officer ->
        var targetText by remember { mutableStateOf(officer.monthlyTarget.toInt().toString()) }

        AlertDialog(
            onDismissRequest = { editingOfficer = null },
            title = { Text("Update Target for ${officer.name}", fontWeight = FontWeight.Bold, color = AmberGold) },
            text = {
                Column {
                    Text("Set monthly sales quota in PKR:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = targetText,
                        onValueChange = { targetText = it },
                        label = { Text("Monthly Quota (PKR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("target_input_field"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberGold, unfocusedBorderColor = SlateBorder)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newTarget = targetText.toDoubleOrNull() ?: officer.monthlyTarget
                        viewModel.updateOfficerTarget(officer, newTarget)
                        editingOfficer = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black)
                ) {
                    Text("Save Quota", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingOfficer = null }) { Text("Cancel", color = TextSecondary) }
            },
            containerColor = SlateSurfaceCard
        )
    }

    // Add Sales Officer Dialog
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var territory by remember { mutableStateOf("") }
        var targetText by remember { mutableStateOf("500000") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Sales Officer", fontWeight = FontWeight.Bold, color = AmberGold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Officer Name") },
                        modifier = Modifier.fillMaxWidth().testTag("officer_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberGold, unfocusedBorderColor = SlateBorder)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberGold, unfocusedBorderColor = SlateBorder)
                    )
                    OutlinedTextField(
                        value = territory,
                        onValueChange = { territory = it },
                        label = { Text("Assigned Territory / Area") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberGold, unfocusedBorderColor = SlateBorder)
                    )
                    OutlinedTextField(
                        value = targetText,
                        onValueChange = { targetText = it },
                        label = { Text("Monthly Target (PKR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberGold, unfocusedBorderColor = SlateBorder)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = targetText.toDoubleOrNull() ?: 500000.0
                        viewModel.addSalesOfficer(
                            SalesOfficer(
                                name = name,
                                phone = phone,
                                territory = territory,
                                monthlyTarget = target,
                                currentSales = 0.0,
                                currentRecovery = 0.0
                            ),
                            onSuccess = { showAddDialog = false }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                    enabled = name.isNotBlank()
                ) {
                    Text("Add Officer", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel", color = TextSecondary) }
            },
            containerColor = SlateSurfaceCard
        )
    }
}

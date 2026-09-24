package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RecoveryPayment
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
fun CreditRecoveryScreen(
    viewModel: OilViewModel,
    onLogRecoveryClick: () -> Unit
) {
    val recoveries by viewModel.recoveries.collectAsState()
    val dealers by viewModel.dealers.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedReceipt by remember { mutableStateOf<RecoveryPayment?>(null) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val totalRecovered = recoveries.sumOf { it.amount }
    val cashCount = recoveries.filter { it.paymentMode == "Cash" }.sumOf { it.amount }
    val chequeCount = recoveries.filter { it.paymentMode == "Cheque" }.sumOf { it.amount }
    val onlineCount = recoveries.filter { it.paymentMode == "Online" }.sumOf { it.amount }

    val filtered = recoveries.filter {
        it.dealerName.contains(searchQuery, ignoreCase = true) ||
                it.receiptNo.contains(searchQuery, ignoreCase = true) ||
                it.collectedBy.contains(searchQuery, ignoreCase = true)
    }

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
                    Text("Credit & Recovery", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Total Collections: ${viewModel.formatCurrency(totalRecovered)}", style = MaterialTheme.typography.bodySmall, color = EmeraldSuccess)
                }
                Button(
                    onClick = onLogRecoveryClick,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("log_recovery_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Record Cash/Cheque", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Recovery Breakdown Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SlateBorder, EmeraldSuccess.copy(alpha = 0.3f))))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Collection Channels Breakdown", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(EmeraldSuccess))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cash", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            Text(viewModel.formatCurrency(cashCount), fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AmberGold))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cheque", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            Text(viewModel.formatCurrency(chequeCount), fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SkyBlueAccent))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Online/Bank", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            Text(viewModel.formatCurrency(onlineCount), fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by receipt number, dealer, officer...", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recovery_search_field"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldSuccess,
                    unfocusedBorderColor = SlateBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )
        }

        // List Header
        item {
            Text(
                text = "Collection Receipts (${filtered.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // Recovery Receipts list
        items(filtered) { rec ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedReceipt = rec }
                    .testTag("recovery_receipt_${rec.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SlateBorder, EmeraldSuccess.copy(alpha = 0.2f))))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    when (rec.paymentMode) {
                                        "Cash" -> EmeraldSuccess.copy(alpha = 0.15f)
                                        "Cheque" -> AmberGold.copy(alpha = 0.15f)
                                        else -> SkyBlueAccent.copy(alpha = 0.15f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (rec.paymentMode) {
                                    "Cash" -> Icons.Default.Money
                                    "Cheque" -> Icons.Default.Receipt
                                    else -> Icons.Default.CreditCard
                                },
                                contentDescription = null,
                                tint = when (rec.paymentMode) {
                                    "Cash" -> EmeraldSuccess
                                    "Cheque" -> AmberGold
                                    else -> SkyBlueAccent
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(rec.dealerName, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(
                                text = "${rec.receiptNo} • Mode: ${rec.paymentMode}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            if (rec.referenceNo.isNotBlank()) {
                                Text("Ref: ${rec.referenceNo}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = viewModel.formatCurrency(rec.amount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess
                        )
                        Text(
                            text = viewModel.formatDate(rec.date),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Official Receipt Modal
    selectedReceipt?.let { rec ->
        AlertDialog(
            onDismissRequest = { selectedReceipt = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Payment Voucher", fontWeight = FontWeight.Bold, color = AmberGold)
                        Text(rec.receiptNo, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Surface(shape = RoundedCornerShape(6.dp), color = EmeraldSuccess.copy(alpha = 0.2f)) {
                        Text(
                            text = "CLEARED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Divider(color = SlateBorder)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Dealer / Customer:", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                        Text(rec.dealerName, color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Amount Recovered:", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                        Text(viewModel.formatCurrency(rec.amount), color = EmeraldSuccess, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Payment Mode:", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                        Text(rec.paymentMode, color = TextPrimary)
                    }
                    if (rec.referenceNo.isNotBlank()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Reference / Cheque:", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            Text(rec.referenceNo, color = TextPrimary)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Collected By:", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                        Text(rec.collectedBy, color = TextPrimary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Date & Time:", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                        Text(viewModel.formatDate(rec.date), color = TextPrimary)
                    }
                    if (rec.notes.isNotBlank()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Remarks:", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            Text(rec.notes, color = TextSecondary)
                        }
                    }
                    Divider(color = SlateBorder)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val shareText = """
                            *ENGINE OIL PAYMENT RECEIPT*
                            Receipt No: ${rec.receiptNo}
                            Dealer: ${rec.dealerName}
                            Amount: ${viewModel.formatCurrency(rec.amount)}
                            Payment Mode: ${rec.paymentMode} ${if (rec.referenceNo.isNotBlank()) "(${rec.referenceNo})" else ""}
                            Collected By: ${rec.collectedBy}
                            Date: ${viewModel.formatDate(rec.date)}
                            Thank you for your payment!
                        """.trimIndent()
                        clipboardManager.setText(AnnotatedString(shareText))
                        Toast.makeText(context, "Receipt copied for WhatsApp sharing", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share Voucher")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedReceipt = null }) {
                    Text("Close", color = TextSecondary)
                }
            },
            containerColor = SlateSurfaceCard
        )
    }
}

package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Dealer
import com.example.data.model.GpsVisitLog
import com.example.data.model.RecoveryPayment
import com.example.data.model.SaleOrder
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
import com.example.ui.theme.VioletGps
import com.example.ui.viewmodel.OilViewModel

@Composable
fun DealersScreen(
    viewModel: OilViewModel,
    selectedDealerId: Long? = null,
    onSelectDealer: (Long?) -> Unit,
    onAddDealerClick: () -> Unit,
    onCollectPaymentForDealer: (Long) -> Unit,
    onNewSaleForDealer: (Long) -> Unit
) {
    if (selectedDealerId != null) {
        DealerLedgerView(
            dealerId = selectedDealerId,
            viewModel = viewModel,
            onBack = { onSelectDealer(null) },
            onCollectPayment = { onCollectPaymentForDealer(selectedDealerId) },
            onNewSale = { onNewSaleForDealer(selectedDealerId) }
        )
    } else {
        DealerListView(
            viewModel = viewModel,
            onSelectDealer = onSelectDealer,
            onAddDealerClick = onAddDealerClick,
            onCollectPayment = onCollectPaymentForDealer
        )
    }
}

@Composable
private fun DealerListView(
    viewModel: OilViewModel,
    onSelectDealer: (Long) -> Unit,
    onAddDealerClick: () -> Unit,
    onCollectPayment: (Long) -> Unit
) {
    val dealers by viewModel.dealers.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var editingDealer by remember { mutableStateOf<Dealer?>(null) }
    var dealerToDelete by remember { mutableStateOf<Dealer?>(null) }
    val context = LocalContext.current

    val totalOutstanding = dealers.sumOf { it.outstandingBalance }
    val totalCreditLimit = dealers.sumOf { it.creditLimit }

    val filteredDealers = dealers.filter { d ->
        d.displayName.contains(searchQuery, ignoreCase = true) ||
                d.ownerName.contains(searchQuery, ignoreCase = true) ||
                d.displayContact.contains(searchQuery, ignoreCase = true) ||
                d.displayLocation.contains(searchQuery, ignoreCase = true) ||
                d.city.contains(searchQuery, ignoreCase = true)
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
                    Text("Dealers & Credit Accounts", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Total Market Outstanding: ${viewModel.formatCurrency(totalOutstanding)}", style = MaterialTheme.typography.bodySmall, color = CrimsonDanger)
                }
                Button(
                    onClick = onAddDealerClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_dealer_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Dealer", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SlateBorder, CrimsonDanger.copy(alpha = 0.3f))))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Active Dealers", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text("${dealers.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(SlateBorder))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Outstanding", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text(viewModel.formatCurrency(totalOutstanding), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CrimsonDanger)
                    }
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(SlateBorder))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Credit Limit", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text(viewModel.formatCurrency(totalCreditLimit), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SkyBlueAccent)
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search shop, owner, city (Montgomery Rd, Badami Bagh)...", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dealer_search_field"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AmberGold,
                    unfocusedBorderColor = SlateBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )
        }

        // Dealer cards
        items(filteredDealers) { dealer ->
            val usageRatio = if (dealer.creditLimit > 0) (dealer.outstandingBalance / dealer.creditLimit).toFloat().coerceIn(0f, 1f) else 0f
            val isOverdue = dealer.outstandingBalance > dealer.creditLimit * 0.85
            val visitsCount by viewModel.getDealerVisitsCount(dealer.id).collectAsState(initial = 0)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectDealer(dealer.id) }
                    .testTag("dealer_card_${dealer.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(SlateBorder, if (isOverdue) CrimsonDanger.copy(alpha = 0.5f) else AmberGold.copy(alpha = 0.2f))
                    )
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = dealer.shopName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isOverdue) CrimsonDanger.copy(alpha = 0.2f) else EmeraldSuccess.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (isOverdue) "OVERDUE" else "ACTIVE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOverdue) CrimsonDanger else EmeraldSuccess,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = VioletGps.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = VioletGps,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "$visitsCount Visits",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = VioletGps
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${dealer.ownerName} • ${dealer.phone} • ${dealer.city}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                text = dealer.address,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                maxLines = 1
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { editingDealer = dealer },
                                modifier = Modifier.size(28.dp).testTag("edit_dealer_${dealer.id}")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = { dealerToDelete = dealer },
                                modifier = Modifier.size(28.dp).testTag("delete_dealer_${dealer.id}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CrimsonDanger.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Outstanding vs Limit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Outstanding Balance", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(viewModel.formatCurrency(dealer.outstandingBalance), fontWeight = FontWeight.Bold, color = CrimsonDanger, fontSize = 16.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Credit Limit", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(viewModel.formatCurrency(dealer.creditLimit), fontWeight = FontWeight.SemiBold, color = TextSecondary, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { usageRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (isOverdue) CrimsonDanger else AmberGold,
                        trackColor = SlateBackground
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Card Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onSelectDealer(dealer.id) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ledger", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { onCollectPayment(dealer.id) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess, contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Collect", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${dealer.phone}"))
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

    editingDealer?.let { dealerToEdit ->
        AddDealerDialog(
            viewModel = viewModel,
            existingDealer = dealerToEdit,
            onDismiss = { editingDealer = null }
        )
    }

    dealerToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { dealerToDelete = null },
            title = { Text("Delete Dealer Record", fontWeight = FontWeight.Bold, color = CrimsonDanger) },
            text = {
                Text(
                    "Are you sure you want to delete '${target.displayName}' (${target.displayLocation}) from dealer records? Current balance: ${viewModel.formatCurrency(target.effectiveBalance)}.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDealer(target.id) {
                            dealerToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonDanger, contentColor = Color.White),
                    modifier = Modifier.testTag("confirm_delete_dealer_button")
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { dealerToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SlateSurfaceCard
        )
    }
}

@Composable
private fun DealerLedgerView(
    dealerId: Long,
    viewModel: OilViewModel,
    onBack: () -> Unit,
    onCollectPayment: () -> Unit,
    onNewSale: () -> Unit
) {
    val dealers by viewModel.dealers.collectAsState()
    val dealer = dealers.find { it.id == dealerId }
    val orders by viewModel.getDealerOrders(dealerId).collectAsState(initial = emptyList())
    val recoveries by viewModel.getDealerRecoveries(dealerId).collectAsState(initial = emptyList())
    val visits: List<GpsVisitLog> by viewModel.getDealerVisits(dealerId).collectAsState(initial = emptyList())
    val officers by viewModel.salesOfficers.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: Ledger, 1: GPS Visits History
    var showLogVisitDialog by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    if (dealer == null) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Button(onClick = onBack) { Text("Back to Dealers") }
            Text("Dealer not found.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Bar
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(dealer.shopName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("${dealer.ownerName} • ${dealer.address}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }

        // Outstanding & Credit Card
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
                            Text("Current Outstanding Balance", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                            Text(viewModel.formatCurrency(dealer.outstandingBalance), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = CrimsonDanger)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Available Credit", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            val avail = (dealer.creditLimit - dealer.outstandingBalance).coerceAtLeast(0.0)
                            Text(viewModel.formatCurrency(avail), fontWeight = FontWeight.SemiBold, color = EmeraldSuccess)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = onCollectPayment,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess, contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Collect Cash/Cheque", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onNewSale,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Bill", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // WhatsApp Reminder Generator Button
        item {
            OutlinedButton(
                onClick = {
                    val summaryText = """
                        *ENGINE OIL STATEMENT / REMINDER*
                        Shop: ${dealer.shopName}
                        Owner: ${dealer.ownerName}
                        Outstanding Balance: ${viewModel.formatCurrency(dealer.outstandingBalance)}
                        Credit Limit: ${viewModel.formatCurrency(dealer.creditLimit)}
                        Please arrange payment at your earliest convenience. Thank you for your partnership!
                    """.trimIndent()
                    clipboardManager.setText(AnnotatedString(summaryText))
                    Toast.makeText(context, "Ledger summary copied to clipboard!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberGold)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copy Statement / WhatsApp Reminder")
            }
        }

        // Tabs: Ledger Activity vs GPS Visits History
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedTab == 0) AmberGold else SlateSurfaceCard,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(if (selectedTab == 0) AmberGold else SlateBorder)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTab = 0 }
                        .testTag("tab_dealer_ledger")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = if (selectedTab == 0) Color.Black else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Ledger (${orders.size + recoveries.size})",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 0) Color.Black else TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedTab == 1) VioletGps else SlateSurfaceCard,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(if (selectedTab == 1) VioletGps else SlateBorder)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTab = 1 }
                        .testTag("tab_dealer_visits")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = if (selectedTab == 1) Color.White else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "GPS Visits (${visits.size})",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 1) Color.White else TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        if (selectedTab == 0) {
            item {
                Text("Ledger Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            // Invoices (Debit)
            items(orders) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceCard)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(4.dp), color = AmberGold.copy(alpha = 0.2f)) {
                                    Text("INVOICE", style = MaterialTheme.typography.labelSmall, color = AmberGold, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(order.invoiceNo, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Text(viewModel.formatDate(order.date), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            if (order.notes.isNotBlank()) {
                                Text(order.notes, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("+${viewModel.formatCurrency(order.netAmount)}", fontWeight = FontWeight.Bold, color = CrimsonDanger)
                            Text("Paid: ${viewModel.formatCurrency(order.paidAmount)}", style = MaterialTheme.typography.labelSmall, color = EmeraldSuccess)
                        }
                    }
                }
            }

            // Recoveries (Credit)
            items(recoveries) { rec ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceCard)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(4.dp), color = EmeraldSuccess.copy(alpha = 0.2f)) {
                                    Text("RECOVERY", style = MaterialTheme.typography.labelSmall, color = EmeraldSuccess, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(rec.receiptNo, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Text("${rec.paymentMode} • ${viewModel.formatDate(rec.date)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            if (rec.referenceNo.isNotBlank()) {
                                Text("Ref: ${rec.referenceNo}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("-${viewModel.formatCurrency(rec.amount)}", fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                            Text("By: ${rec.collectedBy}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                    }
                }
            }

            if (orders.isEmpty() && recoveries.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SlateSurfaceCard)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No Ledger Activity Yet", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Invoices and payment recoveries will appear here.", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                }
            }
        } else {
            // TAB 1: GPS Field Visits History for this Dealer
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Field Visits History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("${visits.size} logged GPS visits for this dealer", style = MaterialTheme.typography.bodySmall, color = VioletGps)
                    }

                    Button(
                        onClick = { showLogVisitDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = VioletGps, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("log_visit_for_dealer_button")
                    ) {
                        Icon(Icons.Default.AddLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Visit", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            if (visits.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SlateSurfaceCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SlateBorder))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(VioletGps.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = VioletGps, modifier = Modifier.size(28.dp))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No Field Visits Recorded Yet", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(
                                "Sales officers can check in and capture live GPS coordinates during site visits to ${dealer.shopName}.",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { showLogVisitDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = VioletGps, contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Check-In Now", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(visits) { visit ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dealer_visit_item_${visit.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SlateSurfaceCard),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(SlateBorder, VioletGps.copy(alpha = 0.3f)))
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Visited by: ${visit.salesOfficerName}",
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            fontSize = 15.sp
                                        )
                                    }
                                    Text(
                                        text = viewModel.formatDate(visit.timestamp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (visit.purpose) {
                                        "Order Booking" -> AmberGold.copy(alpha = 0.2f)
                                        "Payment Recovery" -> EmeraldSuccess.copy(alpha = 0.2f)
                                        else -> VioletGps.copy(alpha = 0.2f)
                                    }
                                ) {
                                    Text(
                                        text = visit.purpose,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = when (visit.purpose) {
                                            "Order Booking" -> AmberGold
                                            "Payment Recovery" -> EmeraldSuccess
                                            else -> VioletGps
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Verified GPS Coordinates with Open Map Action
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SlateBackground,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.MyLocation,
                                            contentDescription = null,
                                            tint = VioletGps,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = "GPS: %.4f° N, %.4f° E".format(visit.latitude, visit.longitude),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = VioletGps
                                            )
                                            Text(
                                                text = "Verified Location Geo-Stamp",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted
                                            )
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val mapUri = Uri.parse("https://maps.google.com/?q=${visit.latitude},${visit.longitude}")
                                            val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
                                            try {
                                                context.startActivity(mapIntent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Coordinates: ${visit.latitude}, ${visit.longitude}", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VioletGps),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Map", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (visit.orderBookedAmount > 0 || visit.recoveryCollected > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    if (visit.orderBookedAmount > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = AmberGold.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "Order: +${viewModel.formatCurrency(visit.orderBookedAmount)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = AmberGold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                    if (visit.recoveryCollected > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = EmeraldSuccess.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "Recovery: ${viewModel.formatCurrency(visit.recoveryCollected)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldSuccess,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (visit.remarks.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Notes: ${visit.remarks}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showLogVisitDialog) {
        var selectedOfficer by remember { mutableStateOf(officers.firstOrNull()?.name ?: "Ali") }
        var selectedPurpose by remember { mutableStateOf("Order Booking") }
        var bookedAmountText by remember { mutableStateOf("0") }
        var recoveryAmountText by remember { mutableStateOf("0") }
        var remarksText by remember { mutableStateOf("") }
        var officerDropdownExpanded by remember { mutableStateOf(false) }

        val purposes = listOf("Order Booking", "Payment Recovery", "Stock Audit", "General Visit")

        AlertDialog(
            onDismissRequest = { showLogVisitDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AddLocation, contentDescription = null, tint = VioletGps, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Field Visit Check-In", fontWeight = FontWeight.Bold, color = VioletGps)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Dealer: ${dealer.shopName}",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Location: ${dealer.address}, ${dealer.city}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    // Officer selection
                    OutlinedTextField(
                        value = selectedOfficer,
                        onValueChange = { selectedOfficer = it },
                        label = { Text("Sales Officer") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VioletGps, unfocusedBorderColor = SlateBorder)
                    )

                    // Purpose selector
                    Text("Visit Purpose", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        purposes.take(2).forEach { p ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedPurpose == p) VioletGps else SlateSurfaceCard,
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(if (selectedPurpose == p) VioletGps else SlateBorder)
                                ),
                                modifier = Modifier.weight(1f).clickable { selectedPurpose = p }
                            ) {
                                Text(
                                    text = p,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selectedPurpose == p) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedPurpose == p) Color.White else TextSecondary,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        purposes.drop(2).forEach { p ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedPurpose == p) VioletGps else SlateSurfaceCard,
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(if (selectedPurpose == p) VioletGps else SlateBorder)
                                ),
                                modifier = Modifier.weight(1f).clickable { selectedPurpose = p }
                            ) {
                                Text(
                                    text = p,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selectedPurpose == p) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedPurpose == p) Color.White else TextSecondary,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    // Amounts
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = bookedAmountText,
                            onValueChange = { bookedAmountText = it },
                            label = { Text("Order Booked (PKR)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberGold, unfocusedBorderColor = SlateBorder)
                        )
                        OutlinedTextField(
                            value = recoveryAmountText,
                            onValueChange = { recoveryAmountText = it },
                            label = { Text("Recovery (PKR)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldSuccess, unfocusedBorderColor = SlateBorder)
                        )
                    }

                    OutlinedTextField(
                        value = remarksText,
                        onValueChange = { remarksText = it },
                        label = { Text("Field Notes / Remarks") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VioletGps, unfocusedBorderColor = SlateBorder)
                    )

                    // GPS coordinate confirmation
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = VioletGps.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, tint = VioletGps, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Live GPS coordinates will be captured and verified automatically upon check-in.",
                                style = MaterialTheme.typography.labelSmall,
                                color = VioletGps
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val booked = bookedAmountText.toDoubleOrNull() ?: 0.0
                        val rec = recoveryAmountText.toDoubleOrNull() ?: 0.0
                        viewModel.captureLiveGpsAndLogVisit(
                            officerName = selectedOfficer,
                            dealerId = dealer.id,
                            dealerName = dealer.shopName,
                            address = "${dealer.address}, ${dealer.city}",
                            fallbackLat = 31.5204 + (dealer.id * 0.012),
                            fallbackLng = 74.3587 + (dealer.id * 0.015),
                            purpose = selectedPurpose,
                            orderBookedAmount = booked,
                            recoveryCollected = rec,
                            remarks = remarksText.ifBlank { "Routine field check-in" },
                            onSuccess = {
                                showLogVisitDialog = false
                                Toast.makeText(context, "GPS visit successfully recorded!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VioletGps, contentColor = Color.White),
                    modifier = Modifier.testTag("submit_dealer_visit_button")
                ) {
                    Text("Confirm Check-In", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogVisitDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SlateSurfaceCard
        )
    }
}

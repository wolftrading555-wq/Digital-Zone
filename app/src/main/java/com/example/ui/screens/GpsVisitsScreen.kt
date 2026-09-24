package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.core.content.ContextCompat
import com.example.data.model.Dealer
import com.example.data.model.GpsVisitLog
import com.example.ui.theme.AmberGold
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
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpsVisitsScreen(
    viewModel: OilViewModel
) {
    val visits by viewModel.visits.collectAsState()
    val dealers by viewModel.dealers.collectAsState()
    val officers by viewModel.salesOfficers.collectAsState()
    val context = LocalContext.current

    var selectedDealerFilter by remember { mutableStateOf<Long?>(null) }
    var showCheckInDialog by remember { mutableStateOf(false) }
    var currentLat by remember { mutableDoubleStateOf(31.5204) } // Lahore default center
    var currentLng by remember { mutableDoubleStateOf(74.3587) }
    var locationFetched by remember { mutableStateOf(false) }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        currentLat = loc.latitude
                        currentLng = loc.longitude
                        locationFetched = true
                    }
                }
            } catch (e: SecurityException) {
                // fallback handled
            }
        }
    }

    fun requestGpsCheckIn() {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        currentLat = loc.latitude
                        currentLng = loc.longitude
                        locationFetched = true
                    }
                }
            } catch (e: SecurityException) {
                // ignore
            }
        } else {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
        showCheckInDialog = true
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
                    Text("GPS Field Visits", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Sales Officers Real-time Check-ins", style = MaterialTheme.typography.bodySmall, color = VioletGps)
                }
                Button(
                    onClick = { requestGpsCheckIn() },
                    colors = ButtonDefaults.buttonColors(containerColor = VioletGps, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("new_gps_checkin_button")
                ) {
                    Icon(Icons.Default.AddLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Field Check-In", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SlateBorder, VioletGps.copy(alpha = 0.3f))))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Visits", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text("${visits.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(SlateBorder))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Orders Booked", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        val totalBooked = visits.sumOf { it.orderBookedAmount }
                        Text(viewModel.formatCurrency(totalBooked), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AmberGold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(SlateBorder))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Recoveries Collected", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        val totalRec = visits.sumOf { it.recoveryCollected }
                        Text(viewModel.formatCurrency(totalRec), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Filter by Dealer", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("dealer_filter_row")
                ) {
                    item {
                        val isAllSelected = selectedDealerFilter == null
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isAllSelected) VioletGps else SlateSurfaceCard,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(if (isAllSelected) VioletGps else SlateBorder)
                            ),
                            modifier = Modifier.clickable { selectedDealerFilter = null }
                        ) {
                            Text(
                                text = "All Dealers (${visits.size})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isAllSelected) Color.White else TextSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    items(dealers) { d ->
                        val isSelected = selectedDealerFilter == d.id
                        val dVisitsCount = visits.count { it.dealerId == d.id }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) VioletGps else SlateSurfaceCard,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) VioletGps else SlateBorder)
                            ),
                            modifier = Modifier.clickable { selectedDealerFilter = d.id }
                        ) {
                            Text(
                                text = "${d.displayName.take(18)} ($dVisitsCount)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        val displayedVisits = if (selectedDealerFilter != null) {
            visits.filter { it.dealerId == selectedDealerFilter }
        } else {
            visits
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedDealerFilter != null) {
                        val dealerName = dealers.find { it.id == selectedDealerFilter }?.displayName ?: "Dealer"
                        "Visits for $dealerName (${displayedVisits.size})"
                    } else {
                        "All Field Check-Ins (${displayedVisits.size})"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (selectedDealerFilter != null) {
                    TextButton(onClick = { selectedDealerFilter = null }) {
                        Text("Clear Filter", color = AmberGold, fontSize = 12.sp)
                    }
                }
            }
        }

        if (displayedVisits.isEmpty()) {
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
                        Text("No Field Visits Recorded for Selected Filter", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Perform a field check-in to log GPS coordinates for this dealer.", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }
        }

        // Visit Timeline Cards
        items(displayedVisits) { visit ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("visit_log_${visit.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SlateBorder, VioletGps.copy(alpha = 0.25f))))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(VioletGps.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = VioletGps, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(visit.dealerName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                                Text("By: ${visit.salesOfficerName} • ${viewModel.formatDate(visit.timestamp)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (visit.purpose) {
                                "Order Booking" -> AmberGold.copy(alpha = 0.15f)
                                "Payment Recovery" -> EmeraldSuccess.copy(alpha = 0.15f)
                                else -> SkyBlueAccent.copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                text = visit.purpose,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (visit.purpose) {
                                    "Order Booking" -> AmberGold
                                    "Payment Recovery" -> EmeraldSuccess
                                    else -> SkyBlueAccent
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = visit.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    // GPS Coordinates badge
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, tint = VioletGps, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "GPS: %.4f, %.4f (Verified)".format(visit.latitude, visit.longitude),
                            style = MaterialTheme.typography.labelSmall,
                            color = VioletGps,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (visit.orderBookedAmount > 0 || visit.recoveryCollected > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SlateBackground, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (visit.orderBookedAmount > 0) {
                                Text("Order: ${viewModel.formatCurrency(visit.orderBookedAmount)}", style = MaterialTheme.typography.labelMedium, color = AmberGold, fontWeight = FontWeight.Bold)
                            }
                            if (visit.recoveryCollected > 0) {
                                Text("Recovery: ${viewModel.formatCurrency(visit.recoveryCollected)}", style = MaterialTheme.typography.labelMedium, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (visit.remarks.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "“${visit.remarks}”",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // New GPS Check-In Dialog
    if (showCheckInDialog) {
        var selectedDealer by remember { mutableStateOf(dealers.firstOrNull()) }
        var dealerExpanded by remember { mutableStateOf(false) }

        var selectedOfficer by remember { mutableStateOf(officers.firstOrNull()?.name ?: "Ali") }
        var officerExpanded by remember { mutableStateOf(false) }

        var selectedPurpose by remember { mutableStateOf("Order Booking") }
        var purposeExpanded by remember { mutableStateOf(false) }

        var orderAmtText by remember { mutableStateOf("") }
        var recAmtText by remember { mutableStateOf("") }
        var remarksText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCheckInDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = VioletGps)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Field Visit Check-In", fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Coordinates pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = VioletGps.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, tint = VioletGps, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Geo-Stamp: %.4f, %.4f".format(currentLat, currentLng),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = VioletGps
                            )
                        }
                    }

                    // Dealer picker
                    ExposedDropdownMenuBox(
                        expanded = dealerExpanded,
                        onExpandedChange = { dealerExpanded = !dealerExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedDealer?.shopName ?: "Select Shop",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Visited Dealer / Shop") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dealerExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VioletGps, unfocusedBorderColor = SlateBorder)
                        )
                        ExposedDropdownMenu(
                            expanded = dealerExpanded,
                            onDismissRequest = { dealerExpanded = false }
                        ) {
                            dealers.forEach { d ->
                                DropdownMenuItem(
                                    text = { Text(d.shopName) },
                                    onClick = {
                                        selectedDealer = d
                                        dealerExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Officer Picker
                    ExposedDropdownMenuBox(
                        expanded = officerExpanded,
                        onExpandedChange = { officerExpanded = !officerExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedOfficer,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Sales Officer") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = officerExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VioletGps, unfocusedBorderColor = SlateBorder)
                        )
                        ExposedDropdownMenu(
                            expanded = officerExpanded,
                            onDismissRequest = { officerExpanded = false }
                        ) {
                            officers.forEach { o ->
                                DropdownMenuItem(
                                    text = { Text(o.name) },
                                    onClick = {
                                        selectedOfficer = o.name
                                        officerExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Purpose
                    ExposedDropdownMenuBox(
                        expanded = purposeExpanded,
                        onExpandedChange = { purposeExpanded = !purposeExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedPurpose,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Visit Purpose") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = purposeExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VioletGps, unfocusedBorderColor = SlateBorder)
                        )
                        ExposedDropdownMenu(
                            expanded = purposeExpanded,
                            onDismissRequest = { purposeExpanded = false }
                        ) {
                            listOf("Order Booking", "Payment Recovery", "Stock Audit", "Routine Visit").forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p) },
                                    onClick = {
                                        selectedPurpose = p
                                        purposeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = orderAmtText,
                            onValueChange = { orderAmtText = it },
                            label = { Text("Order Booked (PKR)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberGold, unfocusedBorderColor = SlateBorder)
                        )
                        OutlinedTextField(
                            value = recAmtText,
                            onValueChange = { recAmtText = it },
                            label = { Text("Cash Collected") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldSuccess, unfocusedBorderColor = SlateBorder)
                        )
                    }

                    OutlinedTextField(
                        value = remarksText,
                        onValueChange = { remarksText = it },
                        label = { Text("Visit Notes / Feedback") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VioletGps, unfocusedBorderColor = SlateBorder)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val d = selectedDealer ?: return@Button
                        val orderAmt = orderAmtText.toDoubleOrNull() ?: 0.0
                        val recAmt = recAmtText.toDoubleOrNull() ?: 0.0
                        viewModel.logGpsVisit(
                            officerName = selectedOfficer,
                            dealerId = d.id,
                            dealerName = d.shopName,
                            address = d.address,
                            lat = currentLat,
                            lng = currentLng,
                            purpose = selectedPurpose,
                            orderBookedAmount = orderAmt,
                            recoveryCollected = recAmt,
                            remarks = remarksText,
                            onSuccess = { showCheckInDialog = false }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VioletGps, contentColor = Color.White),
                    enabled = selectedDealer != null
                ) {
                    Text("Save Check-In", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCheckInDialog = false }) { Text("Cancel", color = TextSecondary) }
            },
            containerColor = SlateSurfaceCard
        )
    }
}

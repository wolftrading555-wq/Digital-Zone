package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Dealer
import com.example.data.model.Product
import com.example.data.model.SaleOrderItem
import com.example.data.model.SalesOfficer
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.SkyBlueAccent
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.OilViewModel

data class DraftOrderItem(
    val product: Product,
    var quantity: Int,
    var unitPrice: Double
) {
    val total: Double get() = quantity * unitPrice
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSaleDialog(
    dealers: List<Dealer>,
    officers: List<SalesOfficer>,
    products: List<Product>,
    viewModel: OilViewModel,
    onDismiss: () -> Unit
) {
    var selectedDealer by remember { mutableStateOf(dealers.firstOrNull()) }
    var dealerExpanded by remember { mutableStateOf(false) }

    var selectedOfficer by remember { mutableStateOf(officers.firstOrNull()) }
    var officerExpanded by remember { mutableStateOf(false) }

    val orderItems = remember { mutableStateListOf<DraftOrderItem>() }

    var discountText by remember { mutableStateOf("0") }
    var paidAmountText by remember { mutableStateOf("0") }
    var notesText by remember { mutableStateOf("") }

    var showProductPicker by remember { mutableStateOf(false) }

    val subtotal = orderItems.sumOf { it.total }
    val discount = discountText.toDoubleOrNull() ?: 0.0
    val netTotal = (subtotal - discount).coerceAtLeast(0.0)
    val paidAmount = paidAmountText.toDoubleOrNull() ?: 0.0
    val balance = (netTotal - paidAmount).coerceAtLeast(0.0)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp),
            shape = RoundedCornerShape(20.dp),
            color = SlateSurfaceCard,
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SlateBorder))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "New Sale Invoice",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AmberGold
                        )
                        Text(
                            text = "Book order and bill lubricants",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_sale_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Select Dealer
                ExposedDropdownMenuBox(
                    expanded = dealerExpanded,
                    onExpandedChange = { dealerExpanded = !dealerExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedDealer?.let { "${it.shopName} (${it.city})" } ?: "Select Dealer",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Customer / Dealer", color = TextSecondary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dealerExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("dealer_dropdown_trigger"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberGold,
                            unfocusedBorderColor = SlateBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = dealerExpanded,
                        onDismissRequest = { dealerExpanded = false }
                    ) {
                        dealers.forEach { dealer ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(dealer.shopName, fontWeight = FontWeight.Bold)
                                        Text("${dealer.ownerName} • Balance: ${viewModel.formatCurrency(dealer.outstandingBalance)}", style = MaterialTheme.typography.bodySmall)
                                    }
                                },
                                onClick = {
                                    selectedDealer = dealer
                                    dealerExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Select Sales Officer
                ExposedDropdownMenuBox(
                    expanded = officerExpanded,
                    onExpandedChange = { officerExpanded = !officerExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedOfficer?.let { "${it.name} (${it.territory})" } ?: "Select Sales Officer",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Booked By (Sales Officer)", color = TextSecondary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = officerExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberGold,
                            unfocusedBorderColor = SlateBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = officerExpanded,
                        onDismissRequest = { officerExpanded = false }
                    ) {
                        officers.forEach { officer ->
                            DropdownMenuItem(
                                text = { Text("${officer.name} - ${officer.territory}") },
                                onClick = {
                                    selectedOfficer = officer
                                    officerExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Items list header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Order Items (${orderItems.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Button(
                        onClick = { showProductPicker = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold.copy(alpha = 0.2f), contentColor = AmberGold),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_oil_item_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Lubricant", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (orderItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(SlateBackground.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No items added yet. Click '+ Add Lubricant'", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    orderItems.forEachIndexed { index, item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = SlateBackground)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.product.name, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1)
                                    Text(
                                        "${item.quantity} x ${viewModel.formatCurrency(item.unitPrice)} (${item.product.packSize})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                                Text(
                                    viewModel.formatCurrency(item.total),
                                    fontWeight = FontWeight.Bold,
                                    color = AmberGold
                                )
                                IconButton(onClick = { orderItems.removeAt(index) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CrimsonDanger, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pricing Summary Fields
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = discountText,
                        onValueChange = { discountText = it },
                        label = { Text("Discount (PKR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberGold, unfocusedBorderColor = SlateBorder)
                    )
                    OutlinedTextField(
                        value = paidAmountText,
                        onValueChange = { paidAmountText = it },
                        label = { Text("Cash Received (PKR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldSuccess, unfocusedBorderColor = SlateBorder)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Calculations Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateBackground)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal:", color = TextSecondary)
                            Text(viewModel.formatCurrency(subtotal), color = TextPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Net Bill Amount:", color = TextSecondary, fontWeight = FontWeight.Bold)
                            Text(viewModel.formatCurrency(netTotal), color = AmberGold, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Balance Added to Credit:", color = TextSecondary)
                            Text(viewModel.formatCurrency(balance), color = if (balance > 0) CrimsonDanger else EmeraldSuccess, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val dealer = selectedDealer ?: return@Button
                            val officer = selectedOfficer ?: return@Button
                            if (orderItems.isEmpty()) return@Button

                            val items = orderItems.map {
                                SaleOrderItem(
                                    orderId = 0,
                                    productId = it.product.id,
                                    productName = it.product.name,
                                    grade = it.product.grade,
                                    packSize = it.product.packSize,
                                    quantity = it.quantity,
                                    unitPrice = it.unitPrice,
                                    lineTotal = it.total
                                )
                            }
                            viewModel.createSaleOrder(
                                dealerId = dealer.id,
                                dealerName = dealer.shopName,
                                officerId = officer.id,
                                officerName = officer.name,
                                items = items,
                                discount = discount,
                                paidAmount = paidAmount,
                                notes = notesText,
                                onSuccess = onDismiss
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                        enabled = selectedDealer != null && orderItems.isNotEmpty(),
                        modifier = Modifier.testTag("submit_sale_order_button")
                    ) {
                        Text("Confirm & Book Sale", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Product Picker Dialog
    if (showProductPicker) {
        Dialog(onDismissRequest = { showProductPicker = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .heightIn(max = 500.dp),
                shape = RoundedCornerShape(16.dp),
                color = SlateSurfaceCard,
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SlateBorder))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Lubricant Product", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(products) { prod ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        val existing = orderItems.find { it.product.id == prod.id }
                                        if (existing != null) {
                                            existing.quantity += 1
                                        } else {
                                            orderItems.add(
                                                DraftOrderItem(
                                                    product = prod,
                                                    quantity = 1,
                                                    unitPrice = prod.wholesalePrice
                                                )
                                            )
                                        }
                                        showProductPicker = false
                                    },
                                colors = CardDefaults.cardColors(containerColor = SlateBackground)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(prod.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text(
                                            "Grade: ${prod.grade} • ${prod.packSize} • In Stock: ${prod.stockQty}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                    Text(
                                        viewModel.formatCurrency(prod.wholesalePrice),
                                        fontWeight = FontWeight.Bold,
                                        color = AmberGold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { showProductPicker = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Close", color = TextSecondary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewRecoveryDialog(
    dealers: List<Dealer>,
    officers: List<SalesOfficer>,
    viewModel: OilViewModel,
    preselectedDealerId: Long? = null,
    onDismiss: () -> Unit
) {
    var selectedDealer by remember {
        mutableStateOf(dealers.find { it.id == preselectedDealerId } ?: dealers.firstOrNull())
    }
    var dealerExpanded by remember { mutableStateOf(false) }

    var amountText by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf("Cash") }
    var referenceNo by remember { mutableStateOf("") }
    var collectedBy by remember { mutableStateOf(officers.firstOrNull()?.name ?: "Owner") }
    var officerExpanded by remember { mutableStateOf(false) }
    var notesText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Log Recovery Payment", fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                Text("Record cash / cheque collection from dealer", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Dealer Dropdown
                ExposedDropdownMenuBox(
                    expanded = dealerExpanded,
                    onExpandedChange = { dealerExpanded = !dealerExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedDealer?.let { "${it.shopName} (Bal: ${viewModel.formatCurrency(it.outstandingBalance)})" } ?: "Select Dealer",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Dealer Account") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dealerExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("recovery_dealer_picker"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldSuccess, unfocusedBorderColor = SlateBorder)
                    )
                    ExposedDropdownMenu(
                        expanded = dealerExpanded,
                        onDismissRequest = { dealerExpanded = false }
                    ) {
                        dealers.forEach { dealer ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(dealer.shopName, fontWeight = FontWeight.Bold)
                                        Text("Balance: ${viewModel.formatCurrency(dealer.outstandingBalance)}", color = CrimsonDanger, style = MaterialTheme.typography.bodySmall)
                                    }
                                },
                                onClick = {
                                    selectedDealer = dealer
                                    dealerExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Recovery Amount (PKR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recovery_amount_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldSuccess, unfocusedBorderColor = SlateBorder)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Payment Mode Chips
                Text("Payment Mode", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Cash", "Cheque", "Online").forEach { mode ->
                        val isSelected = selectedMode == mode
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) EmeraldSuccess.copy(alpha = 0.2f) else SlateBackground,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) EmeraldSuccess else SlateBorder)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedMode = mode }
                        ) {
                            Text(
                                text = mode,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) EmeraldSuccess else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Reference / Cheque No
                OutlinedTextField(
                    value = referenceNo,
                    onValueChange = { referenceNo = it },
                    label = { Text(if (selectedMode == "Cheque") "Cheque No & Bank" else "Ref / Slip No (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldSuccess, unfocusedBorderColor = SlateBorder)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Collected By
                OutlinedTextField(
                    value = collectedBy,
                    onValueChange = { collectedBy = it },
                    label = { Text("Collected By (Officer / Owner)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldSuccess, unfocusedBorderColor = SlateBorder)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notes / Remarks") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldSuccess, unfocusedBorderColor = SlateBorder)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dealer = selectedDealer ?: return@Button
                    val amount = amountText.toDoubleOrNull() ?: return@Button
                    viewModel.recordRecovery(
                        dealerId = dealer.id,
                        dealerName = dealer.shopName,
                        amount = amount,
                        paymentMode = selectedMode,
                        referenceNo = referenceNo,
                        collectedBy = collectedBy,
                        notes = notesText,
                        onSuccess = onDismiss
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess, contentColor = Color.White),
                enabled = selectedDealer != null && (amountText.toDoubleOrNull() ?: 0.0) > 0,
                modifier = Modifier.testTag("submit_recovery_button")
            ) {
                Text("Confirm Recovery", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = SlateSurfaceCard
    )
}

@Composable
fun AddProductDialog(
    viewModel: OilViewModel,
    existingProduct: Product? = null,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(existingProduct?.name ?: "") }
    var grade by remember { mutableStateOf(existingProduct?.grade ?: "20W-50") }
    var category by remember { mutableStateOf(existingProduct?.category ?: "Mineral") }
    var packSize by remember { mutableStateOf(existingProduct?.packSize ?: "4L") }
    var stockQtyText by remember { mutableStateOf(existingProduct?.stockQty?.toString() ?: "50") }
    var minStockText by remember { mutableStateOf(existingProduct?.minStockAlert?.toString() ?: "15") }
    var purchasePriceText by remember { mutableStateOf(existingProduct?.purchasePrice?.toInt()?.toString() ?: "4000") }
    var wholesalePriceText by remember { mutableStateOf(existingProduct?.wholesalePrice?.toInt()?.toString() ?: "4500") }
    var retailPriceText by remember { mutableStateOf(existingProduct?.retailPrice?.toInt()?.toString() ?: "5000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (existingProduct == null) "Add New Lubricant Item" else "Edit Lubricant Item",
                fontWeight = FontWeight.Bold,
                color = AmberGold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Brand & Title") },
                    modifier = Modifier.fillMaxWidth().testTag("product_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberGold, unfocusedBorderColor = SlateBorder)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = grade,
                        onValueChange = { grade = it },
                        label = { Text("Viscosity Grade (e.g. 20W-50)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberGold, unfocusedBorderColor = SlateBorder)
                    )
                    OutlinedTextField(
                        value = packSize,
                        onValueChange = { packSize = it },
                        label = { Text("Pack (1L, 4L, 20L)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberGold, unfocusedBorderColor = SlateBorder)
                    )
                }

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (Synthetic, Mineral, Heavy Diesel, ATF)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberGold, unfocusedBorderColor = SlateBorder)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stockQtyText,
                        onValueChange = { stockQtyText = it },
                        label = { Text("Current Stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberGold, unfocusedBorderColor = SlateBorder)
                    )
                    OutlinedTextField(
                        value = minStockText,
                        onValueChange = { minStockText = it },
                        label = { Text("Low Stock Alert") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberGold, unfocusedBorderColor = SlateBorder)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = purchasePriceText,
                        onValueChange = { purchasePriceText = it },
                        label = { Text("Purchase (Cost)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberGold, unfocusedBorderColor = SlateBorder)
                    )
                    OutlinedTextField(
                        value = wholesalePriceText,
                        onValueChange = { wholesalePriceText = it },
                        label = { Text("Wholesale Rate") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberGold, unfocusedBorderColor = SlateBorder)
                    )
                }

                OutlinedTextField(
                    value = retailPriceText,
                    onValueChange = { retailPriceText = it },
                    label = { Text("Retail Price (MRP)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberGold, unfocusedBorderColor = SlateBorder)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pPrice = purchasePriceText.toDoubleOrNull() ?: 0.0
                    val wPrice = wholesalePriceText.toDoubleOrNull() ?: 0.0
                    val rPrice = retailPriceText.toDoubleOrNull() ?: 0.0
                    val qty = stockQtyText.toIntOrNull() ?: 0
                    val minQty = minStockText.toIntOrNull() ?: 10

                    val product = Product(
                        id = existingProduct?.id ?: 0L,
                        name = name,
                        grade = grade,
                        category = category,
                        packSize = packSize,
                        stockQty = qty,
                        minStockAlert = minQty,
                        purchasePrice = pPrice,
                        wholesalePrice = wPrice,
                        retailPrice = rPrice
                    )
                    viewModel.addOrUpdateProduct(product, onDismiss)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("submit_product_button")
            ) {
                Text(if (existingProduct == null) "Save Product" else "Update", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        },
        containerColor = SlateSurfaceCard
    )
}

@Composable
fun AddDealerDialog(
    viewModel: OilViewModel,
    existingDealer: Dealer? = null,
    onDismiss: () -> Unit
) {
    var shopName by remember { mutableStateOf(existingDealer?.shopName ?: "") }
    var ownerName by remember { mutableStateOf(existingDealer?.ownerName ?: "") }
    var phone by remember { mutableStateOf(existingDealer?.phone ?: "") }
    var city by remember { mutableStateOf(existingDealer?.city ?: "Lahore") }
    var address by remember { mutableStateOf(existingDealer?.address ?: "") }
    var creditLimitText by remember { mutableStateOf(existingDealer?.creditLimit?.toInt()?.toString() ?: "300000") }
    var initialBalanceText by remember { mutableStateOf(existingDealer?.outstandingBalance?.toInt()?.toString() ?: "0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (existingDealer == null) "Add New Dealer / Shop" else "Edit Dealer Profile",
                fontWeight = FontWeight.Bold,
                color = AmberGold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    label = { Text("Shop / Trader Name") },
                    modifier = Modifier.fillMaxWidth().testTag("dealer_shop_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberGold, unfocusedBorderColor = SlateBorder)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("Owner Name") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberGold, unfocusedBorderColor = SlateBorder)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone / WhatsApp") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberGold, unfocusedBorderColor = SlateBorder)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberGold, unfocusedBorderColor = SlateBorder)
                    )
                    OutlinedTextField(
                        value = creditLimitText,
                        onValueChange = { creditLimitText = it },
                        label = { Text("Credit Limit (PKR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberGold, unfocusedBorderColor = SlateBorder)
                    )
                }

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Market Location / Address") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberGold, unfocusedBorderColor = SlateBorder)
                )

                if (existingDealer == null) {
                    OutlinedTextField(
                        value = initialBalanceText,
                        onValueChange = { initialBalanceText = it },
                        label = { Text("Opening Outstanding Balance (PKR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CrimsonDanger, unfocusedBorderColor = SlateBorder)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = creditLimitText.toDoubleOrNull() ?: 300000.0
                    val balance = if (existingDealer != null) existingDealer.outstandingBalance else (initialBalanceText.toDoubleOrNull() ?: 0.0)
                    val fullLocation = if (address.isNotBlank() && city.isNotBlank()) "$address, $city" else address.ifBlank { city }
                    val dealer = Dealer(
                        id = existingDealer?.id ?: 0L,
                        name = shopName,
                        shopName = shopName,
                        ownerName = ownerName,
                        contact = phone,
                        phone = phone,
                        location = fullLocation,
                        city = city,
                        address = address,
                        creditLimit = limit,
                        currentCreditBalance = balance,
                        outstandingBalance = balance
                    )
                    viewModel.addOrUpdateDealer(dealer, onDismiss)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                enabled = shopName.isNotBlank(),
                modifier = Modifier.testTag("submit_dealer_button")
            ) {
                Text(if (existingDealer == null) "Register Dealer" else "Update", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        },
        containerColor = SlateSurfaceCard
    )
}

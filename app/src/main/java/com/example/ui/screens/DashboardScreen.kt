package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DashboardMetricCard
import com.example.ui.components.SalesRecoveryChart
import com.example.ui.components.SalesTeamPerformanceCard
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
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.OilViewModel

@Composable
fun DashboardScreen(
    viewModel: OilViewModel,
    onOpenNewSale: () -> Unit,
    onOpenNewRecovery: () -> Unit
) {
    val stats by viewModel.dashboardStats.collectAsState()
    val officers by viewModel.salesOfficers.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val recoveries by viewModel.recoveries.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top App Header & Role Badge
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Engine Oil Business Manager",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Dashboard preview • Real-time Operations",
                        style = MaterialTheme.typography.bodySmall,
                        color = AmberGold
                    )
                }

                // Owner Badge with role switcher affordance
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = AmberGold.copy(alpha = 0.15f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(AmberGold, AmberGold.copy(alpha = 0.5f)))
                    ),
                    modifier = Modifier.clickable {
                        viewModel.setUserRole(if (userRole == "Owner") "Sales Officer" else "Owner")
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AmberGold)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = userRole,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AmberGold
                        )
                    }
                }
            }
        }

        // 4 Primary Metric Cards (2x2 Grid)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Row 1: Today's Sales & Today's Recovery
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardMetricCard(
                        title = "Today's Sales",
                        value = viewModel.formatCurrency(stats.todaySales),
                        subtitle = "Target: Rs. 200,000",
                        icon = Icons.Default.PointOfSale,
                        accentColor = AmberGold,
                        modifier = Modifier.weight(1f),
                        testTag = "card_today_sales",
                        onClick = onOpenNewSale
                    )

                    DashboardMetricCard(
                        title = "Today's Recovery",
                        value = viewModel.formatCurrency(stats.todayRecovery),
                        subtitle = "Collections received",
                        icon = Icons.Default.Payments,
                        accentColor = EmeraldSuccess,
                        modifier = Modifier.weight(1f),
                        testTag = "card_today_recovery",
                        onClick = onOpenNewRecovery
                    )
                }

                // Row 2: Total Outstanding & Stock Value
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardMetricCard(
                        title = "Total Outstanding",
                        value = viewModel.formatCurrency(stats.totalOutstanding),
                        subtitle = "Dealers Credit Ledger",
                        icon = Icons.Default.People,
                        accentColor = CrimsonDanger,
                        modifier = Modifier.weight(1f),
                        testTag = "card_total_outstanding",
                        onClick = { viewModel.navigateTo(AppScreen.Dealers) }
                    )

                    DashboardMetricCard(
                        title = "Stock Value",
                        value = viewModel.formatCurrency(stats.totalStockValue),
                        subtitle = "${stats.totalProductsCount} Lubricant SKUs",
                        icon = Icons.Default.Inventory2,
                        accentColor = SkyBlueAccent,
                        modifier = Modifier.weight(1f),
                        testTag = "card_stock_value",
                        onClick = { viewModel.navigateTo(AppScreen.Inventory) }
                    )
                }
            }
        }

        // Quick Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenNewSale,
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("action_new_sale")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Sale", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = onOpenNewRecovery,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("action_add_recovery")
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Recovery", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = { viewModel.navigateTo(AppScreen.GpsVisits) },
                    colors = ButtonDefaults.buttonColors(containerColor = SlateSurfaceCard, contentColor = VioletGps),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SlateBorder, VioletGps.copy(alpha = 0.5f)))),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("action_gps_visit")
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = VioletGps)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("GPS Visit", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Low Stock Warning Banner (if applicable)
        if (lowStockProducts.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(AppScreen.Inventory) },
                    colors = CardDefaults.cardColors(containerColor = CrimsonDanger.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CrimsonDanger, AmberGold)))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonDanger)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Low Stock Alert (${lowStockProducts.size} items)",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Replenish ${lowStockProducts.joinToString(", ") { it.grade }.take(40)}...",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = AmberGold, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Sales & Recovery Monthly Chart
        item {
            SalesRecoveryChart(
                data = stats.monthlyData,
                modifier = Modifier.fillMaxWidth().testTag("sales_recovery_chart")
            )
        }

        // Sales Team Performance Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sales Team Performance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelMedium,
                    color = AmberGold,
                    modifier = Modifier.clickable { viewModel.navigateTo(AppScreen.SalesTeam) }
                )
            }
        }

        // Sales Officers Cards (Ali, Ahmed, Usman)
        items(officers.size) { index ->
            val officer = officers[index]
            SalesTeamPerformanceCard(
                name = officer.name,
                percentage = officer.achievementPercentage,
                targetFormatted = viewModel.formatCurrency(officer.monthlyTarget),
                currentSalesFormatted = viewModel.formatCurrency(officer.currentSales),
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.navigateTo(AppScreen.SalesTeam) }
            )
        }

        // Today's Activity Feed
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Invoices & Recoveries",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${orders.size + recoveries.size} Entries",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }

        items(orders.take(3).size) { idx ->
            val order = orders[idx]
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AmberGold.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PointOfSale, contentDescription = null, tint = AmberGold, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(order.dealerName, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1)
                            Text("${order.invoiceNo} • by ${order.salesOfficerName}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(viewModel.formatCurrency(order.netAmount), fontWeight = FontWeight.Bold, color = AmberGold)
                        Text(viewModel.formatDate(order.date), style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }
                }
            }
        }

        items(recoveries.take(2).size) { idx ->
            val rec = recoveries[idx]
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(EmeraldSuccess.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(rec.dealerName, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1)
                            Text("${rec.paymentMode} • ${rec.receiptNo}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("+${viewModel.formatCurrency(rec.amount)}", fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                        Text(viewModel.formatDate(rec.date), style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

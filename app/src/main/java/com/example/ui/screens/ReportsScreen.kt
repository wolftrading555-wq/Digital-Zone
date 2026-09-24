package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
fun ReportsScreen(
    viewModel: OilViewModel
) {
    val stats by viewModel.dashboardStats.collectAsState()
    val products by viewModel.products.collectAsState()
    val dealers by viewModel.dealers.collectAsState()
    val officers by viewModel.salesOfficers.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val estGrossProfit = stats.todaySales * 0.138 // Approx 13.8% gross margin in lubricant distribution

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
                    Text("Executive Reports & P&L", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Financial Summary & Market Performance", style = MaterialTheme.typography.bodySmall, color = AmberGold)
                }
                OutlinedButton(
                    onClick = {
                        val report = """
                            *ENGINE OIL BUSINESS MANAGER - EXECUTIVE REPORT*
                            Date: Today
                            ---------------------------------
                            • Today's Sales: ${viewModel.formatCurrency(stats.todaySales)}
                            • Today's Recovery: ${viewModel.formatCurrency(stats.todayRecovery)}
                            • Est. Gross Margin: ${viewModel.formatCurrency(estGrossProfit)}
                            • Total Market Outstanding: ${viewModel.formatCurrency(stats.totalOutstanding)}
                            • Inventory Valuation: ${viewModel.formatCurrency(stats.totalStockValue)}
                            ---------------------------------
                            *Sales Team Targets:*
                            ${officers.joinToString("\n") { "• ${it.name}: ${it.achievementPercentage}% (Target: ${viewModel.formatCurrency(it.monthlyTarget)})" }}
                            ---------------------------------
                            Generated by Engine Oil Business Manager Pro
                        """.trimIndent()
                        clipboardManager.setText(AnnotatedString(report))
                        Toast.makeText(context, "Full report copied for WhatsApp sharing", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberGold),
                    modifier = Modifier.testTag("copy_report_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export")
                }
            }
        }

        // Executive Financial Dashboard Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SlateBorder, AmberGold.copy(alpha = 0.4f))))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Daily Financial Health", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Today's Billed Sales", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Text(viewModel.formatCurrency(stats.todaySales), fontWeight = FontWeight.Bold, color = AmberGold, fontSize = 18.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Estimated Profit (Gross)", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Text(viewModel.formatCurrency(estGrossProfit), fontWeight = FontWeight.Bold, color = EmeraldSuccess, fontSize = 18.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = SlateBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Market Credit At Risk", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Text(viewModel.formatCurrency(stats.totalOutstanding), fontWeight = FontWeight.Bold, color = CrimsonDanger)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Current Stock Assets", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Text(viewModel.formatCurrency(stats.totalStockValue), fontWeight = FontWeight.Bold, color = SkyBlueAccent)
                        }
                    }
                }
            }
        }

        // Top Moving Viscosity Grades
        item {
            Text("Lubricants Volume By Viscosity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        // Products breakdown list
        items(products) { prod ->
            val productValuation = prod.stockQty * prod.wholesalePrice
            val margin = prod.wholesalePrice - prod.purchasePrice
            val marginPercent = if (prod.purchasePrice > 0) ((margin / prod.purchasePrice) * 100).toInt() else 0

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceCard)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(4.dp), color = AmberGold.copy(alpha = 0.15f)) {
                                Text(prod.grade, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = AmberGold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(prod.name, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1)
                        }
                        Text("${prod.stockQty} units in stock • Pack: ${prod.packSize}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(viewModel.formatCurrency(productValuation), fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("+$marginPercent% Margin", style = MaterialTheme.typography.labelSmall, color = EmeraldSuccess)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AddDealerDialog
import com.example.ui.screens.AddProductDialog
import com.example.ui.screens.CreditRecoveryScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DealersScreen
import com.example.ui.screens.GpsVisitsScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.NewRecoveryDialog
import com.example.ui.screens.NewSaleDialog
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SalesTeamScreen
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.OilViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: OilViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val dealers by viewModel.dealers.collectAsState()
    val officers by viewModel.salesOfficers.collectAsState()
    val products by viewModel.products.collectAsState()

    var showNewSaleDialog by remember { mutableStateOf(false) }
    var showNewRecoveryDialog by remember { mutableStateOf(false) }
    var preselectedDealerIdForRecovery by remember { mutableStateOf<Long?>(null) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showAddDealerDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SlateBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentScreen) {
                            is AppScreen.Dashboard -> "Engine Oil Pro"
                            is AppScreen.Inventory -> "Inventory"
                            is AppScreen.Dealers -> "Dealers Ledger"
                            is AppScreen.DealerLedger -> "Account Ledger"
                            is AppScreen.CreditRecovery -> "Recovery & Credit"
                            is AppScreen.SalesTeam -> "Sales Targets"
                            is AppScreen.GpsVisits -> "Field Visits GPS"
                            is AppScreen.Reports -> "Business Reports"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AmberGold
                    )
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.Reports) },
                        modifier = Modifier.testTag("top_reports_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = "Reports",
                            tint = if (currentScreen is AppScreen.Reports) AmberGold else TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SlateSurface,
                    titleContentColor = AmberGold
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SlateSurface,
                contentColor = TextPrimary,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = currentScreen is AppScreen.Dashboard,
                    onClick = { viewModel.navigateTo(AppScreen.Dashboard) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AmberGold,
                        selectedTextColor = AmberGold,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = AmberGold.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_dashboard")
                )
                NavigationBarItem(
                    selected = currentScreen is AppScreen.Inventory,
                    onClick = { viewModel.navigateTo(AppScreen.Inventory) },
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = "Inventory") },
                    label = { Text("Stock", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AmberGold,
                        selectedTextColor = AmberGold,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = AmberGold.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_inventory")
                )
                NavigationBarItem(
                    selected = currentScreen is AppScreen.Dealers || currentScreen is AppScreen.DealerLedger,
                    onClick = { viewModel.navigateTo(AppScreen.Dealers) },
                    icon = { Icon(Icons.Default.People, contentDescription = "Dealers") },
                    label = { Text("Dealers", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AmberGold,
                        selectedTextColor = AmberGold,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = AmberGold.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_dealers")
                )
                NavigationBarItem(
                    selected = currentScreen is AppScreen.CreditRecovery,
                    onClick = { viewModel.navigateTo(AppScreen.CreditRecovery) },
                    icon = { Icon(Icons.Default.Payments, contentDescription = "Recovery") },
                    label = { Text("Recovery", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldSuccess,
                        selectedTextColor = EmeraldSuccess,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = EmeraldSuccess.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_recovery")
                )
                NavigationBarItem(
                    selected = currentScreen is AppScreen.SalesTeam,
                    onClick = { viewModel.navigateTo(AppScreen.SalesTeam) },
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Sales Team") },
                    label = { Text("Team", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AmberGold,
                        selectedTextColor = AmberGold,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = AmberGold.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_team")
                )
                NavigationBarItem(
                    selected = currentScreen is AppScreen.GpsVisits,
                    onClick = { viewModel.navigateTo(AppScreen.GpsVisits) },
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "GPS") },
                    label = { Text("GPS", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AmberGold,
                        selectedTextColor = AmberGold,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = AmberGold.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_gps")
                )
            }
        },
        floatingActionButton = {
            if (currentScreen is AppScreen.Dashboard || currentScreen is AppScreen.Inventory || currentScreen is AppScreen.Dealers) {
                FloatingActionButton(
                    onClick = { showNewSaleDialog = true },
                    containerColor = AmberGold,
                    contentColor = Color.Black,
                    modifier = Modifier.testTag("fab_quick_sale")
                ) {
                    Icon(Icons.Default.PointOfSale, contentDescription = "Quick Sale")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val screen = currentScreen) {
                is AppScreen.Dashboard -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onOpenNewSale = { showNewSaleDialog = true },
                        onOpenNewRecovery = {
                            preselectedDealerIdForRecovery = null
                            showNewRecoveryDialog = true
                        }
                    )
                }
                is AppScreen.Inventory -> {
                    InventoryScreen(
                        viewModel = viewModel,
                        onAddProductClick = { showAddProductDialog = true }
                    )
                }
                is AppScreen.Dealers -> {
                    DealersScreen(
                        viewModel = viewModel,
                        selectedDealerId = null,
                        onSelectDealer = { id ->
                            if (id != null) viewModel.navigateTo(AppScreen.DealerLedger(id))
                        },
                        onAddDealerClick = { showAddDealerDialog = true },
                        onCollectPaymentForDealer = { dealerId ->
                            preselectedDealerIdForRecovery = dealerId
                            showNewRecoveryDialog = true
                        },
                        onNewSaleForDealer = {
                            showNewSaleDialog = true
                        }
                    )
                }
                is AppScreen.DealerLedger -> {
                    DealersScreen(
                        viewModel = viewModel,
                        selectedDealerId = screen.dealerId,
                        onSelectDealer = { id ->
                            if (id == null) viewModel.navigateTo(AppScreen.Dealers)
                        },
                        onAddDealerClick = { showAddDealerDialog = true },
                        onCollectPaymentForDealer = { dealerId ->
                            preselectedDealerIdForRecovery = dealerId
                            showNewRecoveryDialog = true
                        },
                        onNewSaleForDealer = {
                            showNewSaleDialog = true
                        }
                    )
                }
                is AppScreen.CreditRecovery -> {
                    CreditRecoveryScreen(
                        viewModel = viewModel,
                        onLogRecoveryClick = {
                            preselectedDealerIdForRecovery = null
                            showNewRecoveryDialog = true
                        }
                    )
                }
                is AppScreen.SalesTeam -> {
                    SalesTeamScreen(viewModel = viewModel)
                }
                is AppScreen.GpsVisits -> {
                    GpsVisitsScreen(viewModel = viewModel)
                }
                is AppScreen.Reports -> {
                    ReportsScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Global Modal Dialogs
    if (showNewSaleDialog) {
        NewSaleDialog(
            dealers = dealers,
            officers = officers,
            products = products,
            viewModel = viewModel,
            onDismiss = { showNewSaleDialog = false }
        )
    }

    if (showNewRecoveryDialog) {
        NewRecoveryDialog(
            dealers = dealers,
            officers = officers,
            viewModel = viewModel,
            preselectedDealerId = preselectedDealerIdForRecovery,
            onDismiss = {
                showNewRecoveryDialog = false
                preselectedDealerIdForRecovery = null
            }
        )
    }

    if (showAddProductDialog) {
        AddProductDialog(
            viewModel = viewModel,
            onDismiss = { showAddProductDialog = false }
        )
    }

    if (showAddDealerDialog) {
        AddDealerDialog(
            viewModel = viewModel,
            onDismiss = { showAddDealerDialog = false }
        )
    }
}

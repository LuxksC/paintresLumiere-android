package com.luxkasTech.PaintresLumiere.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.feature.admin.AdminHomeRoute
import com.luxkasTech.PaintresLumiere.feature.admin.AdminSection
import com.luxkasTech.PaintresLumiere.feature.admin.form.ProductFormRoute
import com.luxkasTech.PaintresLumiere.feature.admin.group.AdminProductGroupRoute
import com.luxkasTech.PaintresLumiere.feature.admin.products.AdminProductsRoute
import com.luxkasTech.PaintresLumiere.core.designsystem.format.brlFormatted
import com.luxkasTech.PaintresLumiere.feature.cart.CartRoute
import com.luxkasTech.PaintresLumiere.feature.checkout.CheckoutRoute
import com.luxkasTech.PaintresLumiere.feature.checkout.OrderConfirmationScreen
import com.luxkasTech.PaintresLumiere.feature.category.CategoryProductsRoute
import com.luxkasTech.PaintresLumiere.feature.home.HomeRoute
import com.luxkasTech.PaintresLumiere.feature.orders.OrdersRoute
import com.luxkasTech.PaintresLumiere.data.payment.Payment
import com.luxkasTech.PaintresLumiere.data.payment.PaymentMethod
import com.luxkasTech.PaintresLumiere.feature.payment.BoletoPaymentRoute
import com.luxkasTech.PaintresLumiere.feature.payment.CardPaymentRoute
import com.luxkasTech.PaintresLumiere.feature.payment.PaymentMethodRoute
import com.luxkasTech.PaintresLumiere.feature.payment.PixPaymentRoute
import kotlinx.serialization.json.Json
import com.luxkasTech.PaintresLumiere.feature.orders.detail.OrderDetailRoute
import com.luxkasTech.PaintresLumiere.feature.product.details.ProductDetailsRoute
import com.luxkasTech.PaintresLumiere.feature.profile.ProfileMenuItem
import com.luxkasTech.PaintresLumiere.feature.profile.ProfileRoute
import com.luxkasTech.PaintresLumiere.feature.profile.settings.HelpRoute
import com.luxkasTech.PaintresLumiere.feature.profile.settings.NotificationsRoute
import com.luxkasTech.PaintresLumiere.feature.profile.settings.SecurityRoute
import com.luxkasTech.PaintresLumiere.feature.profile.edit.EditProfileRoute
import kotlinx.serialization.Serializable

// Tab graphs
@Serializable data object HomeGraph
@Serializable data object ProfileGraph
@Serializable data object OrdersGraph
@Serializable data object AdminGraph

// Destinations
@Serializable data object HomeDestination
@Serializable data class ProductDetailsDestination(val sku: String)
@Serializable data object CartDestination
@Serializable data object CheckoutDestination
@Serializable data class OrderConfirmationDestination(val orderId: String, val number: String, val total: String)
@Serializable data class CategoryProductsDestination(val category: String)
@Serializable data object ProfileDestination
@Serializable data object EditProfileDestination
@Serializable data object NotificationsDestination
@Serializable data object SecurityDestination
@Serializable data object HelpDestination
@Serializable data object OrdersDestination
@Serializable data class OrderDetailDestination(val orderId: String)
@Serializable data class PaymentMethodDestination(val orderId: String)

/** [payment] is the charge as JSON — it carries the QR / boleto data the screen shows immediately. */
@Serializable data class PixPaymentDestination(val orderId: String, val payment: String)
@Serializable data class BoletoPaymentDestination(val orderId: String, val payment: String)
@Serializable data class CardPaymentDestination(val orderId: String, val installments: Int)
@Serializable data object AdminDestination
@Serializable data object AdminProductsDestination
@Serializable data class AdminProductGroupDestination(val sku: String)
@Serializable data class AdminProductFormDestination(val sku: String? = null, val variantId: String? = null)

private enum class MainTab(
    val graph: Any,
    /** True for any destination inside this tab's graph. */
    val ownsDestination: (NavDestination) -> Boolean,
    /** True only for the tab's root screen. */
    val isRoot: (NavDestination) -> Boolean,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    Home(
        HomeGraph,
        { d -> d.hierarchy.any { it.hasRoute<HomeGraph>() } },
        { d -> d.hasRoute<HomeDestination>() },
        R.string.tab_home,
        Icons.Outlined.Home,
        Icons.Filled.Home,
    ),
    Orders(
        OrdersGraph,
        { d -> d.hierarchy.any { it.hasRoute<OrdersGraph>() } },
        { d -> d.hasRoute<OrdersDestination>() },
        R.string.tab_orders,
        Icons.AutoMirrored.Outlined.ReceiptLong,
        Icons.AutoMirrored.Filled.ReceiptLong,
    ),
    Admin(
        AdminGraph,
        { d -> d.hierarchy.any { it.hasRoute<AdminGraph>() } },
        { d -> d.hasRoute<AdminDestination>() },
        R.string.tab_admin,
        Icons.Outlined.AdminPanelSettings,
        Icons.Filled.AdminPanelSettings,
    ),
    Profile(
        ProfileGraph,
        { d -> d.hierarchy.any { it.hasRoute<ProfileGraph>() } },
        { d -> d.hasRoute<ProfileDestination>() },
        R.string.tab_profile,
        Icons.Outlined.Person,
        Icons.Filled.Person,
    ),
}

/**
 * Signed-in flow — the Android counterpart of iOS `MainTabCoordinator`. Each tab is a nested graph
 * with its own back stack, saved and restored when switching tabs. The bottom bar is shown only on
 * the tab roots; pushed screens (product details, …) get the full height.
 */
@Composable
fun MainFlow(
    isAdmin: Boolean,
    onAdminSectionSelected: (AdminSection) -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    val json = remember { Json { ignoreUnknownKeys = true; explicitNulls = false } }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    // The Admin tab only exists for confirmed admins (role from GET /profile).
    val tabs = MainTab.entries.filter { it != MainTab.Admin || isAdmin }
    val showBottomBar = destination == null || tabs.any { it.isRoot(destination) }

    Scaffold(
        containerColor = PLColor.backgroundPrimary,
        bottomBar = {
            if (showBottomBar) {
                MainBottomBar(tabs, destination) { tab -> navController.navigateToTab(tab.graph) }
            }
        },
    ) { padding ->
        Box(Modifier.padding(bottom = padding.calculateBottomPadding())) {
            NavHost(navController = navController, startDestination = HomeGraph) {
                navigation<HomeGraph>(startDestination = HomeDestination) {
                    composable<HomeDestination> {
                        HomeRoute(
                            onProductSelected = { sku -> navController.navigate(ProductDetailsDestination(sku)) },
                            onCartRequested = { navController.navigate(CartDestination) },
                            onCategorySelected = { navController.navigate(CategoryProductsDestination(it.raw)) },
                        )
                    }
                    composable<CategoryProductsDestination> {
                        CategoryProductsRoute(
                            onBack = { navController.popBackStack() },
                            onProductSelected = { sku -> navController.navigate(ProductDetailsDestination(sku)) },
                        )
                    }
                    composable<ProductDetailsDestination> {
                        ProductDetailsRoute(
                            onBack = { navController.popBackStack() },
                            onCartRequested = { navController.navigate(CartDestination) },
                        )
                    }
                    composable<CartDestination> {
                        CartRoute(
                            onBack = { navController.popBackStack() },
                            onBrowseCatalog = { navController.popBackStack(HomeDestination, inclusive = false) },
                            onCheckoutRequested = { navController.navigate(CheckoutDestination) },
                        )
                    }
                    composable<CheckoutDestination> {
                        CheckoutRoute(
                            onBack = { navController.popBackStack() },
                            onOrderPlaced = { order ->
                                navController.navigate(
                                    OrderConfirmationDestination(order.id, order.displayNumber, order.totalAmount.brlFormatted()),
                                ) {
                                    // Cart and checkout leave the stack: Back can never resubmit the order.
                                    popUpTo<CartDestination> { inclusive = true }
                                }
                            },
                        )
                    }
                    composable<OrderConfirmationDestination> { entry ->
                        val args = entry.toRoute<OrderConfirmationDestination>()
                        OrderConfirmationScreen(
                            orderNumber = args.number,
                            total = args.total,
                            onPayNow = {
                                navController.navigate(PaymentMethodDestination(args.orderId)) {
                                    popUpTo<OrderConfirmationDestination> { inclusive = true }
                                }
                            },
                            onViewOrder = {
                                navController.navigate(OrderDetailDestination(args.orderId)) {
                                    popUpTo<OrderConfirmationDestination> { inclusive = true }
                                }
                            },
                            onBackToCatalog = { navController.popBackStack(HomeDestination, inclusive = false) },
                        )
                    }
                }
                navigation<OrdersGraph>(startDestination = OrdersDestination) {
                    composable<OrdersDestination> {
                        OrdersRoute(
                            onOrderSelected = { id -> navController.navigate(OrderDetailDestination(id)) },
                            onBrowseCatalog = { navController.navigateToTab(HomeGraph) },
                        )
                    }
                    composable<OrderDetailDestination> {
                        OrderDetailRoute(
                            onBack = { navController.popBackStack() },
                            onResumePayment = { orderId, payment ->
                                val encoded = json.encodeToString(Payment.serializer(), payment)
                                navController.navigate(
                                    if (payment.method == PaymentMethod.PIX) {
                                        PixPaymentDestination(orderId, encoded)
                                    } else {
                                        BoletoPaymentDestination(orderId, encoded)
                                    },
                                )
                            },
                            onChoosePaymentMethod = { orderId -> navController.navigate(PaymentMethodDestination(orderId)) },
                        )
                    }
                    composable<PaymentMethodDestination> {
                        PaymentMethodRoute(
                            onBack = { navController.popBackStack() },
                            onChargeCreated = { orderId, payment ->
                                val target: Any = when (payment.method) {
                                    PaymentMethod.PIX -> PixPaymentDestination(orderId, json.encodeToString(Payment.serializer(), payment))
                                    PaymentMethod.BOLETO_INSTALLMENTS -> BoletoPaymentDestination(orderId, json.encodeToString(Payment.serializer(), payment))
                                    else -> OrderDetailDestination(orderId)
                                }
                                navController.navigate(target) { popUpTo<PaymentMethodDestination> { inclusive = true } }
                            },
                            onCardRequested = { orderId, installments ->
                                navController.navigate(CardPaymentDestination(orderId, installments))
                            },
                        )
                    }
                    composable<CardPaymentDestination> {
                        CardPaymentRoute(
                            onBack = { navController.popBackStack() },
                            onPaid = { id ->
                                navController.navigate(OrderDetailDestination(id)) { popUpTo<PaymentMethodDestination> { inclusive = true } }
                            },
                            onChooseAnotherMethod = { navController.popBackStack() },
                        )
                    }
                    composable<BoletoPaymentDestination> {
                        BoletoPaymentRoute(
                            onBack = { navController.popBackStack() },
                            onViewOrder = { id ->
                                navController.navigate(OrderDetailDestination(id)) { popUpTo<BoletoPaymentDestination> { inclusive = true } }
                            },
                        )
                    }
                    composable<PixPaymentDestination> {
                        PixPaymentRoute(
                            onBack = { navController.popBackStack() },
                            onViewOrder = { id ->
                                navController.navigate(OrderDetailDestination(id)) { popUpTo<PixPaymentDestination> { inclusive = true } }
                            },
                        )
                    }
                }
                navigation<AdminGraph>(startDestination = AdminDestination) {
                    composable<AdminDestination> {
                        AdminHomeRoute(
                            onSectionSelected = { section ->
                                when (section) {
                                    AdminSection.Products -> navController.navigate(AdminProductsDestination)
                                    else -> onAdminSectionSelected(section)
                                }
                            },
                        )
                    }
                    composable<AdminProductsDestination> {
                        AdminProductsRoute(
                            onBack = { navController.popBackStack() },
                            onProductSelected = { sku -> navController.navigate(AdminProductGroupDestination(sku)) },
                            onCreateProduct = { navController.navigate(AdminProductFormDestination()) },
                        )
                    }
                    composable<AdminProductGroupDestination> {
                        AdminProductGroupRoute(
                            onBack = { navController.popBackStack() },
                            onEditVariant = { sku, id -> navController.navigate(AdminProductFormDestination(sku, id)) },
                            onAddVariant = { sku -> navController.navigate(AdminProductFormDestination(sku)) },
                        )
                    }
                    composable<AdminProductFormDestination> {
                        ProductFormRoute(onFinished = { navController.popBackStack() })
                    }
                }
                navigation<ProfileGraph>(startDestination = ProfileDestination) {
                    composable<ProfileDestination> {
                        ProfileRoute(
                            onMenuItemSelected = { item ->
                                navController.navigate(
                                    when (item) {
                                        ProfileMenuItem.EditProfile -> EditProfileDestination
                                        ProfileMenuItem.Notifications -> NotificationsDestination
                                        ProfileMenuItem.Security -> SecurityDestination
                                        ProfileMenuItem.Help -> HelpDestination
                                    },
                                )
                            },
                        )
                    }
                    composable<EditProfileDestination> {
                        EditProfileRoute(onFinished = { navController.popBackStack() })
                    }
                    composable<NotificationsDestination> { NotificationsRoute(onBack = { navController.popBackStack() }) }
                    composable<SecurityDestination> { SecurityRoute(onBack = { navController.popBackStack() }) }
                    composable<HelpDestination> { HelpRoute(onBack = { navController.popBackStack() }) }
                }
            }
        }
    }
}

/** Switches tab keeping each tab's back stack (saved and restored). */
private fun NavHostController.navigateToTab(graph: Any) {
    navigate(graph) {
        popUpTo(this@navigateToTab.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun MainBottomBar(tabs: List<MainTab>, destination: NavDestination?, onSelect: (MainTab) -> Unit) {
    NavigationBar(containerColor = PLColor.backgroundSecondary) {
        tabs.forEach { tab ->
            val selected = destination?.let(tab.ownsDestination) == true
            NavigationBarItem(
                selected = selected,
                onClick = { if (!selected) onSelect(tab) },
                icon = { Icon(if (selected) tab.selectedIcon else tab.icon, contentDescription = null) },
                label = { Text(stringResource(tab.labelRes), style = PLFont.tabLabel) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PLColor.goldBright,
                    selectedTextColor = PLColor.goldBright,
                    indicatorColor = PLColor.backgroundHighlight,
                    unselectedIconColor = PLColor.textMuted,
                    unselectedTextColor = PLColor.textMuted,
                ),
            )
        }
    }
}

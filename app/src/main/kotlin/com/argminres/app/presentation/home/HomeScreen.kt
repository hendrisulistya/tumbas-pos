package com.argminres.app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale

// Map category names to icons
private fun categoryIcon(name: String): ImageVector = when {
    name.contains("Paket", ignoreCase = true) -> Icons.Default.LocalOffer
    name.contains("Makanan", ignoreCase = true) -> Icons.Default.Restaurant
    name.contains("Minuman", ignoreCase = true) -> Icons.Default.LocalDrink
    name.contains("Lain", ignoreCase = true) -> Icons.Default.MoreHoriz
    name == "All" -> Icons.Default.GridView
    else -> Icons.Default.Category
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    onNavigateToCart: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")) }
    val showFab by remember { derivedStateOf { uiState.cartItemCount > 0 } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PadangPOS") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                windowInsets = WindowInsets(left = 0.dp, top = 10.dp, right = 0.dp, bottom = 0.dp)
            )
        },
        floatingActionButton = {
            if (showFab) {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToCart,
                    icon = {
                        BadgedBox(
                            badge = {
                                Badge { Text(uiState.cartItemCount.toString()) }
                            }
                        ) {
                            Icon(Icons.Default.ShoppingCart, "Cart")
                        }
                    },
                    text = { Text("View Cart") },
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // ---- Category Sidebar (20%) ----
                CategorySidebar(
                    categories = uiState.categories,
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = viewModel::onCategorySelected,
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.20f)
                )

                // ---- Dish Grid (80%) ----
                if (uiState.dishes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(0.80f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Inventory,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "No dishes available",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 200.dp),
                        modifier = Modifier
                            .weight(0.80f)
                            .fillMaxHeight(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        gridItems(
                            items = uiState.dishes,
                            key = { it.dish.id },
                            contentType = { "product" }
                        ) { dishWithCategory ->
                            val cartQty = viewModel.getCartQuantity(dishWithCategory.dish.id)
                            ProductGridItem(
                                productWithCategory = dishWithCategory,
                                currencyFormatter = currencyFormatter,
                                cartQuantity = cartQty,
                                onAddToCart = { viewModel.addToCart(dishWithCategory) },
                                onIncrease = { viewModel.increaseQuantity(dishWithCategory.dish.id) },
                                onDecrease = { viewModel.decreaseQuantity(dishWithCategory.dish.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategorySidebar(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(categories.size) { index ->
                val category = categories[index]
                val isSelected = category == selectedCategory
                CategoryItem(
                    category = category,
                    icon = categoryIcon(category),
                    isSelected = isSelected,
                    onClick = { onCategorySelected(category) }
                )
            }
        }
    }
}

@Composable
private fun CategoryItem(
    category: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    val contentColor = if (isSelected)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .padding(vertical = 14.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = category,
            tint = contentColor,
            modifier = Modifier.size(36.dp)
        )
        Text(
            text = category,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = contentColor,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
fun ProductGridItem(
    productWithCategory: com.argminres.app.data.local.dao.DishWithCategory,
    currencyFormatter: NumberFormat,
    cartQuantity: Int,
    onAddToCart: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    val product = productWithCategory.dish

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Product Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (product.image != null) {
                    com.argminres.app.presentation.dish.ProductImageDisplay(
                        image = product.image!!,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }

            // Name & Price
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    currencyFormatter.format(product.price),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (product.stock <= 0) {
                    Text(
                        "Habis",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Add / Quantity Controls
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                if (cartQuantity == 0) {
                    Button(
                        onClick = onAddToCart,
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        enabled = product.stock > 0,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Add, "Add", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Tambah", style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledIconButton(
                            onClick = onDecrease,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.Remove, "Decrease", modifier = Modifier.size(16.dp))
                        }
                        Text(
                            cartQuantity.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        FilledIconButton(
                            onClick = onIncrease,
                            modifier = Modifier.size(30.dp),
                            enabled = cartQuantity < product.stock
                        ) {
                            Icon(Icons.Default.Add, "Increase", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

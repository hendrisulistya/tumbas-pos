package com.argminres.app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale

private val Blue600 = Color(0xFF1976D2)
private val Blue50  = Color(0xFFE3F2FD)
private val Blue100 = Color(0xFFBBDEFB)

private fun categoryIcon(name: String): ImageVector = when {
    name.contains("Paket", ignoreCase = true)   -> Icons.Default.LocalOffer
    name.contains("Makanan", ignoreCase = true) -> Icons.Default.Restaurant
    name.contains("Minuman", ignoreCase = true) -> Icons.Default.LocalDrink
    name.contains("Lain", ignoreCase = true)    -> Icons.Default.MoreHoriz
    name == "All"                               -> Icons.Default.GridView
    else                                        -> Icons.Default.Category
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
    var isSearching by remember { mutableStateOf(false) }
    val showFab by remember { derivedStateOf { uiState.cartItemCount > 0 } }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            if (isSearching) {
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            placeholder = { Text("Search dishes...", color = Color.White.copy(alpha = 0.7f)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                cursorColor = Color.White
                            ),
                            trailingIcon = {
                                IconButton(onClick = { 
                                    viewModel.onSearchQueryChange("")
                                    isSearching = false 
                                }) {
                                    Icon(Icons.Default.Close, "Close", tint = Color.White)
                                }
                            }
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Blue600
                    ),
                    windowInsets = WindowInsets(left = 0.dp, top = 10.dp, right = 0.dp, bottom = 0.dp)
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            "PadangPOS",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                    },
                    actions = {
                        IconButton(onClick = { isSearching = true }) {
                            Icon(Icons.Default.Search, "Search", tint = Color.White)
                        }
                        if (uiState.cartItemCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(containerColor = Color.White) {
                                        Text(uiState.cartItemCount.toString(), color = Blue600)
                                    }
                                },
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                IconButton(onClick = onNavigateToCart) {
                                    Icon(Icons.Default.ShoppingCart, "Cart", tint = Color.White)
                                }
                            }
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, "Settings", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Blue600
                    ),
                    windowInsets = WindowInsets(left = 0.dp, top = 10.dp, right = 0.dp, bottom = 0.dp)
                )
            }
        },
        floatingActionButton = {
            if (showFab) {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToCart,
                    icon = { Icon(Icons.Default.ShoppingCart, "Cart") },
                    text = { Text("View Cart (${uiState.cartItemCount})", fontWeight = FontWeight.SemiBold) },
                    containerColor = Blue600,
                    contentColor = Color.White,
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
                CircularProgressIndicator(color = Blue600)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.White)
            ) {
                // ── Category Sidebar ──────────────────────────────────────
                CategorySidebar(
                    categories = uiState.categories,
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = viewModel::onCategorySelected,
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.20f)
                )

                // ── Product Grid ─────────────────────────────────────────────
                if (uiState.filteredItems.isEmpty()) {
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
                                tint = Color(0xFFBBDEFB)
                            )
                            Text(
                                "No dishes available",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF9E9E9E)
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 200.dp),
                        modifier = Modifier
                            .weight(0.80f)
                            .fillMaxHeight(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        gridItems(
                            items = uiState.filteredItems,
                            key = { item -> "${if (item.isPackage) "pkg" else "dish"}_${item.id}" },
                            contentType = { "product" }
                        ) { item ->
                            val cartQty = viewModel.getCartQuantity(item.id, item.isPackage)
                            ProductGridItem(
                                item = item,
                                currencyFormatter = currencyFormatter,
                                cartQuantity = cartQty,
                                onAddToCart = { viewModel.addToCart(item) },
                                onIncrease = { viewModel.increaseQuantity(item.id, item.isPackage) },
                                onDecrease = { viewModel.decreaseQuantity(item.id, item.isPackage) }
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
        color = Blue50,
        tonalElevation = 0.dp
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (isSelected) Blue100 else Color.Transparent)
            .padding(vertical = 14.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = category,
            tint = if (isSelected) Blue600 else Color(0xFF9E9E9E),
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = category,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Blue600 else Color(0xFF616161),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(Blue600)
            )
        }
    }
}

@Composable
fun ProductGridItem(
    item: ProductItem,
    currencyFormatter: NumberFormat,
    cartQuantity: Int,
    onAddToCart: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    val isInCart = cartQuantity > 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isInCart) Blue50 else Color.White
        ),
        border = if (isInCart)
            androidx.compose.foundation.BorderStroke(1.5.dp, Blue600)
        else null
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Product Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(Blue50),
                contentAlignment = Alignment.Center
            ) {
                if (item.image != null) {
                    com.argminres.app.presentation.dish.ProductImageDisplay(
                        image = item.image!!,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        if (item.isPackage) Icons.Default.LocalOffer else Icons.Default.Restaurant,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Blue100
                    )
                }

                // Habis overlay
                if (item.stock <= 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xAAFFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Habis",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828)
                        )
                    }
                }

                // Cart badge
                if (isInCart) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(24.dp)
                            .background(Blue600, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            cartQuantity.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Name & Price
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Black,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    currencyFormatter.format(item.price),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Blue600
                )
            }

            // Add / Quantity Controls
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .padding(bottom = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                if (cartQuantity == 0) {
                    Button(
                        onClick = onAddToCart,
                        modifier = Modifier.fillMaxWidth().height(34.dp),
                        enabled = item.stock > 0,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Blue600,
                            disabledContainerColor = Color(0xFFE0E0E0)
                        )
                    ) {
                        Icon(Icons.Default.Add, "Add", modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(Modifier.width(4.dp))
                        Text("Tambah", style = MaterialTheme.typography.labelMedium, color = Color.White)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDecrease,
                            modifier = Modifier
                                .size(30.dp)
                                .background(Blue50, RoundedCornerShape(8.dp))
                                .border(1.dp, Blue100, RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Remove, "Decrease", modifier = Modifier.size(16.dp), tint = Blue600)
                        }
                        Text(
                            cartQuantity.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        IconButton(
                            onClick = onIncrease,
                            modifier = Modifier
                                .size(30.dp)
                                .background(
                                    if (cartQuantity < item.stock) Blue600 else Color(0xFFE0E0E0),
                                    RoundedCornerShape(8.dp)
                                ),
                            enabled = cartQuantity < item.stock
                        ) {
                            Icon(Icons.Default.Add, "Increase", modifier = Modifier.size(16.dp), tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

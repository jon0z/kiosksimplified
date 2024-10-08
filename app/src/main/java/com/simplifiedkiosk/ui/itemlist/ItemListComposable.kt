package com.simplifiedkiosk.ui.itemlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.simplifiedkiosk.model.Product

@Composable
fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")

        Text(
            text = "TRENDING PRODUCTS",
            style = MaterialTheme.typography.h6
        )

        Row {
            Icon(imageVector = Icons.Default.FavoriteBorder, contentDescription = "Favorite")
            Spacer(modifier = Modifier.width(16.dp))
            Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TopBarPreview() {
    TopBar()
}

@Composable
fun ProductListItem(
    product: Product,
    onFavoriteClick: () -> Unit,
    onItemClick: () -> Unit
) {
    val isFavorite by remember { mutableStateOf(product.isFavorite) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onItemClick() },
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(model = ImageRequest.Builder(LocalContext.current)
                .data(product.thumbnail)
                .crossfade(true)
                .build(), contentDescription = "thumbnail")


            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(text = product.title ?: "", style = MaterialTheme.typography.subtitle1)
                Text(
                    text = "$${product.price}",
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.primary
                )
            }

            IconButton(onClick = {
                onFavoriteClick()
            }) {
                isFavorite?.let {
                    Icon(
                        imageVector = if(it) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite"
                    )
                }

            }
        }
    }
}

@Preview
@Composable
fun ProductListItemPreview() {
    ProductListItem(product = Product(
        productId = 1,
        title = "Product 1",
        price = 10.0,
        description = "Description",
    ), onFavoriteClick = {}, onItemClick = {})
}

@Composable
fun ProductList(
    products: List<Product>,
    onFavoriteClick: (Product) -> Unit,
    onItemClick: (Product) -> Unit
) {
    LazyColumn {
        products.forEach {product ->
            item {
                ProductListItem(
                    product = product,
                    onFavoriteClick = { onFavoriteClick(product) },
                    onItemClick = { onItemClick(product) }
                )
            }

        }
    }
}

@Preview
@Composable
fun ProductListPreview() {
    ProductList(products = listOf(
        Product(
            productId = 1,
            title = "Product 1",
            price = 10.0,
            description = "Description",
        ),
        Product(
            productId = 2,
            title = "Product 2",
            price = 20.0,
            description = "Description",
        )
    ), onFavoriteClick = {}, onItemClick = {})
}

@Composable
fun BottomNavigationBar() {
    BottomNavigation {
        BottomNavigationItem(
            icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") },
            selected = false,
            onClick = { /* Handle navigation */ }
        )
        BottomNavigationItem(
            icon = { Icon(imageVector = Icons.AutoMirrored.Filled.List, contentDescription = "Categories") },
            selected = false,
            onClick = { /* Handle navigation */ }
        )
        BottomNavigationItem(
            icon = { Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Cart") },
            selected = false,
            onClick = { /* Handle navigation */ }
        )
        BottomNavigationItem(
            icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Profile") },
            selected = false,
            onClick = { /* Handle navigation */ }
        )
    }
}

@Preview
@Composable
fun BottomNavigationBarPreview() {
    BottomNavigationBar()
}

@Composable
fun ProductScreen(
    products: List<Product>,
    onFavoriteClick: (Product) -> Unit,
    onItemClick: (Product) -> Unit
) {
    Scaffold(
        topBar = { TopBar() },
        bottomBar = { BottomNavigationBar() }
    ) { paddingValues ->
        paddingValues.calculateBottomPadding()
        ProductList(
            products = products,
            onFavoriteClick = onFavoriteClick,
            onItemClick = onItemClick
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProductScreenPreview() {
    val products = listOf(
        Product(1, "iPhone X mobile", 1000.00, description = "Description"),
        Product(2, "Wireless Earphones", 350.00, description = "Description"),
        Product(3, "iwatch series 5", 550.00, description = "Description"),
        Product(4, "Laptop Cover", 150.00, description = "Description"),
        Product(5, "Mobile Cover", 50.00, description = "Description")
    )

    ProductScreen(
        products = products,
        onFavoriteClick = { product -> /* Handle favorite click */ },
        onItemClick = { product -> /* Handle item click */ }
    )
}
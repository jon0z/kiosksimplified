package com.simplifiedkiosk.ui.cart

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
        IconButton(onClick = { /* Handle back action */ }) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }

        Text(
            text = "My Cart",
            style = MaterialTheme.typography.h6
        )

        IconButton(onClick = { /* Handle favorite action */ }) {
            Icon(imageVector = Icons.Default.FavoriteBorder, contentDescription = "Favorite", tint = Color.Red)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewTopBar() {
    TopBar()
}

@Composable
fun CartItem(
    product: Product,
    onIncreaseQuantity: () -> Unit,
    onDecreaseQuantity: () -> Unit,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onItemClick() },
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            product.imageUrl?.let {
                Image(
                    painter = painterResource(id = it.toInt()),
                    contentDescription = product.title,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                product.title?.let {
                    Text(text = it, style = MaterialTheme.typography.subtitle1)
                }

                product.description?.let {
                    Text(text = it, style = MaterialTheme.typography.body2, color = Color.Gray)
                }
                Text(
                    text = "${product.price}$",
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.primary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecreaseQuantity) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Decrease")
                }
                Text(text = product.quantity.toString(), style = MaterialTheme.typography.body1)
                IconButton(onClick = onIncreaseQuantity) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Increase")
                }
            }
        }
    }
}

@Preview
@Composable
fun CartItemPreview() {
    CartItem(
        product = Product(
            productId = 1,
            title = "Product 1",
            price = "10.0",
            description = "Description",
            quantity = 1
        ),
        onIncreaseQuantity = { },
        onDecreaseQuantity = { },
        onItemClick = { }
    )
}

@Composable
fun RecommendationSection(recommendedItems: List<Product>, onItemClick: (Product) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Recommend", style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.Bold)

        LazyRow(
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            recommendedItems.forEach { product ->
                item {
                    Card(
                        modifier = Modifier
                            .size(80.dp)
                            .padding(8.dp)
                            .clickable { onItemClick(product) },
                        elevation = 4.dp
                    ) {
                        Image(
                            painter = painterResource(id = product.imageUrl?.toInt() ?: 0),
                            contentDescription = product.title,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, apiLevel = 34)
@Composable
fun RecommendationSectionPreview() {
    val recommendedProducts:List<Product> =listOf(
        Product(
            productId = 1,
            title = "Product 1",
            price = "10.0",
            description = "Description",
        ),
        Product(
            productId = 2,
            title = "Product 2",
            price = "20.0",
            description = "Description",
        ),
        Product(
            productId = 3,
            title = "Product 2",
            price = "20.0",
            description = "Description",
        )
    )

    RecommendationSection(recommendedItems = recommendedProducts, onItemClick = { })
}

@Composable
fun DiscountInput(onApplyDiscount: (String) -> Unit) {
    var discountCode by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = discountCode,
            onValueChange = { discountCode = it },
            label = { Text("Discount card") },
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = { onApplyDiscount(discountCode) },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFCCCB))
        ) {
            Text("Enter")
        }
    }
}

@Composable
fun SummarySection(subtotal: Double, discount: Double, delivery: String, total: Double) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Subtotal")
            Text(text = "${subtotal}$")
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Discount")
            Text(text = "-${discount}$")
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Delivery")
            Text(text = delivery)
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Total", fontWeight = FontWeight.Bold)
            Text(text = "${total}$", fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SummarySectionPreview() {
    SummarySection(subtotal = 100.0, discount = 20.0, delivery = "Free", total = 80.0)
}

@Composable
fun CheckoutButton(onCheckoutClick: () -> Unit) {
    Button(
        onClick = onCheckoutClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF6D4C41))
    ) {
        Text("Checkout", color = Color.White)
    }
}

@Preview
@Composable
fun CheckoutButtonPreview() {
    CheckoutButton(onCheckoutClick = { })
}

@Composable
fun CartScreen(
    products: List<Product>,
    recommendedItems: List<Product>,
    onIncreaseQuantity: (Product) -> Unit,
    onDecreaseQuantity: (Product) -> Unit,
    onItemClick: (Product) -> Unit,
    onApplyDiscount: (String) -> Unit,
    onRecommendedItemClick: (Product) -> Unit,
    onCheckoutClick: () -> Unit
) {
    Scaffold(
        topBar = { TopBar() }
    ) {
        it.calculateBottomPadding()
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn {
                products.forEach {
                    item {
                        CartItem(
                            product = it,
                            onIncreaseQuantity = { onIncreaseQuantity(it) },
                            onDecreaseQuantity = { onDecreaseQuantity(it) },
                            onItemClick = { onItemClick(it) }
                        )
                    }
                }
            }

            RecommendationSection(recommendedItems = recommendedItems, onItemClick = onRecommendedItemClick)

            DiscountInput(onApplyDiscount = onApplyDiscount)

            SummarySection(subtotal = 24.55, discount = 1.55, delivery = "Free", total = 23.00)

            CheckoutButton(onCheckoutClick = onCheckoutClick)
        }
    }
}

@Preview
@Composable
fun CartScreenPreview(){
    val products:List<Product> =listOf(
        Product(
            productId = 1,
            title = "Product 1",
            price = "10.0",
            description = "Description",
        ),
        Product(
            productId = 2,
            title = "Product 2",
            price = "20.0",
            description = "Description",
        ),
        Product(
            productId = 3,
            title = "Product 2",
            price = "20.0",
            description = "Description",
        )
    )

    CartScreen(
        products = products,
        recommendedItems = products,
        onIncreaseQuantity = {},
        onDecreaseQuantity = {},
        onItemClick = {},
        onApplyDiscount = {},
        onRecommendedItemClick = {}
    ) {
        
    }
}


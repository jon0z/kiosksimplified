package com.simplifiedkiosk.ui.itemlist

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.simplifiedkiosk.R
import com.simplifiedkiosk.model.Product


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ItemRow(
    product: Product,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .padding(8.dp)
            .clickable { onClick() },
    ) {
        Card {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.width(200.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = product.title ?: "",
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "$${product.price}",
                        maxLines = 1,
                    )

                }
                ThumbnailResourceDrawable(
                    resourceId = R.drawable.product_image_placeholder_48x48
                )

            }

        }
    }
}

@Composable
fun ThumbnailResourceDrawable(
    resourceId: Int,
    modifier: Modifier = Modifier,
    width: Int = 160,
    height: Int = 160,
) {
    val bitmap = BitmapFactory.decodeResource(LocalContext.current.resources, resourceId)
    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
    val imgBitmap = scaledBitmap.asImageBitmap()
    Image(
        bitmap = imgBitmap,
        contentDescription = "Thumbnail",
        modifier = modifier
    )
}

@Preview
@Composable
fun ItemRowPreview() {
    ItemRow(product = Product(
        productId = 1,
        title = "Product 1",
        description = "Description 1",
        price = "$10.00",
        imageUrl = "https://via.placeholder.com/150"
    ),
        isSelected = false,
        onClick = { }
    )
}

@Composable
fun ItemList(
    products: List<Product>,
    selectedItem: Product?,
    onSelectedItemChange: (Product) -> Unit) {
    LazyColumn {
        products.forEach { product ->
            item {
                ItemRow(
                    product = product,
                    isSelected = product == selectedItem,
                    onClick = { onSelectedItemChange(product) })
            }
        }
    }

}

@Preview
@Composable
fun ItemListPreview() {
    ItemList(
        products = listOf(
            Product(
                productId = 1,
                title = "Product 1",
                description = "Description 1",
                price = "$10.00",
                imageUrl = "https://via.placeholder.com/150"
            ),
            Product(
                productId = 2,
                title = "Product 2",
                description = "Description 2",
                price = "$20.00",
                imageUrl = "https://via.placeholder.com/150"
            ),
            Product(
                productId = 2,
                title = "Product 3",
                description = "Description 3",
                price = "$20.00",
                imageUrl = "https://via.placeholder.com/150"
            )
        ),
        selectedItem = null,
        onSelectedItemChange = { 1 }
    )
}
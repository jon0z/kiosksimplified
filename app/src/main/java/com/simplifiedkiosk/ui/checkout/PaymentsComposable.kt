package com.simplifiedkiosk.ui.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CreditCardForm(
    card: CreditCard?,
    onCardSave: (CreditCard) -> Unit,
    onCardCancel: () -> Unit
) {
    val cardState = remember { mutableStateOf(CreditCardState()) }

    Column(modifier = Modifier
        .background(Color.White)
        .fillMaxSize()
        .padding(8.dp)) {
        OutlinedTextField(
            value = cardState.value.number.text,
            onValueChange = { cardState.value.number = TextFieldValue(it) },
            label = { Text(text = "Card number") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
        )

        OutlinedTextField(
            value = cardState.value.nameOnCard.text,
            onValueChange = { cardState.value.nameOnCard = TextFieldValue(it) },
            label = { Text(text = "Name on card") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
        )

        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = cardState.value.expirationMonth.text,
                onValueChange = { cardState.value.expirationMonth = TextFieldValue(it) },
                label = { Text(text = "Expiration month") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
            )

            OutlinedTextField(
                value = cardState.value.expirationYear,
                onValueChange = { cardState.value.expirationYear = TextFieldValue(it.toString()) },
                label = { Text(text = "Expiration year") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            )
        }

        OutlinedTextField(
            value = cardState.value.cvv,
            onValueChange = { cardState.value.cvv = TextFieldValue(it.toString()) },
            label = { Text(text = "CVV") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
        )

        Spacer(Modifier.weight(1f))
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            Button(
                onClick = { onCardSave(cardState.value.toCreditCard()) },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(color = 0XFF007BFF),
                    contentColor = Color.White),
                modifier = Modifier.weight(1f)
                    .padding(end = 4.dp)
            ) {
                Text(text = "Save")
            }

            Button(
                onClick = { onCardCancel() },
                colors = ButtonDefaults.outlinedButtonColors(
                    backgroundColor = Color.White,
                    contentColor =  Color(color = 0XFF007BFF)),
                modifier = Modifier.weight(1f)
                    .padding(start = 4.dp)
            ) {
                Text(text = "Cancel")
            }
        }
    }
}

@Composable
fun CardDetails(card: CreditCard) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .background(Color.White)
        .padding(top = 24.dp),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = "Card number: ${card.number}")
        Text(text = "Name on card: ${card.nameOnCard}")
        Text(text = "Expiration month: ${card.expirationMonth}")
        Text(text = "Expiration year: ${card.expirationYear}")
        Text(text = "CVV: ${card.cvv}")
    }
}

@Preview
@Composable
fun PreviewCreditCardForm() {
    CreditCardForm(
        card = CreditCard(
            number = "1234 5678 9012 3456",
            nameOnCard = "John Doe",
            expirationMonth = 12,
            expirationYear = 2025,
            cvv = 123
        ),
        onCardSave = { },
        onCardCancel = { }
    )
}

@Preview
@Composable
fun PreviewCardDetails() {
    CardDetails(
        CreditCard(
            number = "1234 5678 9012 3456",
            nameOnCard = "John Doe",
            expirationMonth = 12,
            expirationYear = 2025,
            cvv = 123
        )
    )
}

data class CreditCardState(
    var number: TextFieldValue = TextFieldValue(""),
    var nameOnCard: TextFieldValue = TextFieldValue(""),
    var expirationMonth: TextFieldValue = TextFieldValue(""),
    var expirationYear: TextFieldValue = TextFieldValue(""),
    var cvv: TextFieldValue = TextFieldValue("")
)

fun CreditCardState.toCreditCard(): CreditCard {
    return CreditCard(
        number = number.text,
        nameOnCard = nameOnCard.text,
        expirationMonth = expirationMonth.text.toInt(),
        expirationYear = expirationYear.text.toInt(),
        cvv = cvv.text.toInt()
    )
}

data class CreditCard(
    val number: String,
    val nameOnCard: String,
    val expirationMonth: Int,
    val expirationYear: Int,
    val cvv: Int
)
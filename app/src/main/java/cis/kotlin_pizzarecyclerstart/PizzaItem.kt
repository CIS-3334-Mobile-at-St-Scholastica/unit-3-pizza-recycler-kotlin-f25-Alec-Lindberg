package cis.kotlin_pizzarecyclerstart

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


data class PizzaToppings(
    val size : Int,
    val toppings: String,
)

@Composable
fun PizzaItem(pizza: PizzaToppings) {
    Row(modifier = Modifier.padding(8.dp)) {
        Column {
            Text(text = "Size: ${pizza.size}")
            Text(text = "Toppings: ${pizza.toppings}")
        }
    }
}
// The full list of pizzas
@Composable
fun PizzaToppingsList(pizzaList: List<PizzaToppings>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(pizzaList) { pizza ->
            PizzaItem(pizza = pizza)
        }
    }
}
package cis.kotlin_pizzarecyclerstart

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Slider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle   // already needed

import cis.kotlin_pizzarecyclerstart.ui.theme.Kotlin_PizzaRecyclerStart_F25Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Kotlin_PizzaRecyclerStart_F25Theme {
                Surface(Modifier.fillMaxSize()) {
                    val context = LocalContext.current
                    val repo = remember {
                        PizzaRepositoryImpl(
                            PizzaDatabase.getInstance(context).pizzaDao()
                        )
                    }
                    val vm: MainViewModel = viewModel(factory = MainViewModelFactory(repo))

                    // Collect StateFlow exposed by VM (Flow path)
                    val pizzasUi by vm.pizzaToppingsUi.collectAsStateWithLifecycle()

                    // ★ CHANGED: actually render a screen + the list so UI isn't blank
                    Column(Modifier.fillMaxSize()) {
                        PizzaOrderScreen(vm)              // ★ NEW: show controls
                        HorizontalDivider(
                            Modifier.padding(vertical = 8.dp),
                            DividerDefaults.Thickness,
                            DividerDefaults.color
                        ) // ★ NEW: visual separation
                        PizzaToppingsList(pizzaList = pizzasUi)    // ★ MOVED inside Column
                    }

                    // (If you wanted only the list, you could just call PizzaToppingsList(pizzasUi))
                    // (If you wanted only the order controls, call PizzaOrderScreen(vm))
                }
            }
        }
    }
}

@Composable
fun PizzaOrderScreen(vm: MainViewModel) {
    val context = LocalContext.current

    // read simple UI state from VM
    val size = vm.size
    val chicken = vm.chicken
    val pepperoni = vm.pepperoni
    val greenPeppers = vm.greenPeppers

    // collect flows from VM
    val orderText by vm.orderText.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Pizza Order", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))

        // SIZE
        Column(Modifier.fillMaxWidth()) {
            Text("Size: ${size.toPretty()}", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = size.toIndex().toFloat(),
                onValueChange = { idx -> vm.updateSize(indexToSize(idx.toInt())) },
                valueRange = 0f..3f,
                steps = 2
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Small"); Text("X-Large")
            }
        }

        Spacer(Modifier.height(16.dp))

        // TOPPINGS
        Column(Modifier.fillMaxWidth()) {
            Text("Toppings", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(selected = chicken, onClick = { vm.toggleChicken() }, label = { Text("Chicken") })
                FilterChip(selected = pepperoni, onClick = { vm.togglePepperoni() }, label = { Text("Pepperoni") })
                FilterChip(selected = greenPeppers, onClick = { vm.toggleGreenPeppers() }, label = { Text("Green Peppers") })
            }
        }

        Spacer(Modifier.height(16.dp))

        // BUTTONS
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(modifier = Modifier.weight(1f), onClick = { vm.addToOrder() }) {
                Text("Add To Order")
            }
            Button(modifier = Modifier.weight(1f), onClick = {
                Toast.makeText(context, "Order placed!", Toast.LENGTH_SHORT).show()
                vm.clearOrder()
            }) {
                Text("Clear / Place Order")
            }
        }

        Spacer(Modifier.height(16.dp))

        // ORDER BOX (read-only)
        OutlinedTextField(
            value = orderText,
            onValueChange = { /* read-only */ },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            label = { Text("Order") },
            placeholder = { Text("Your order will appear here...") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Text
            ),
            readOnly = true,
            minLines = 6,
            maxLines = 20
        )
    }
}

/* --- Small helpers to keep UI strings clean --- */
private fun PizzaSize.toPretty(): String = when (this) {
    PizzaSize.SMALL -> "Small"
    PizzaSize.MEDIUM -> "Medium"
    PizzaSize.LARGE -> "Large"
    PizzaSize.XLARGE -> "X-Large"
}
private fun PizzaSize.toIndex(): Int = when (this) {
    PizzaSize.SMALL -> 0
    PizzaSize.MEDIUM -> 1
    PizzaSize.LARGE -> 2
    PizzaSize.XLARGE -> 3
}
private fun indexToSize(i: Int): PizzaSize = when (i.coerceIn(0, 3)) {
    0 -> PizzaSize.SMALL
    1 -> PizzaSize.MEDIUM
    2 -> PizzaSize.LARGE
    else -> PizzaSize.XLARGE
}

package cis.kotlin_pizzarecyclerstart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cis.kotlin_pizzarecyclerstart.PizzaSize.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: PizzaRepository
) : ViewModel() {

    // --- Source of truth: DB via repository (Flow -> StateFlow) ---
    val pizzas: StateFlow<List<Pizza>> =
        repository.observeAll()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    // If your UI still expects List<PizzaToppings>, derive it here.
    // (Change PizzaItem/PizzaToppingsList to accept Pizza to avoid mapping.)
    val pizzaToppingsUi: StateFlow<List<PizzaToppings>> =
        pizzas.map { list -> list.map { it.toUiModel() } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Example derived state you already had
    val orderCount: StateFlow<Int> =
        pizzas.map { it.size }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val orderText: StateFlow<String> =
        pizzas.map { list ->
            if (list.isEmpty()) "No pizzas in your order yet."
            else list.joinToString("\n") { it.description() }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "No pizzas in your order yet.")

    // ----- UI selection state (unchanged) -----
    private var _size = PizzaSize.MEDIUM
    val size: PizzaSize get() = _size

    private var _chicken = false
    val chicken: Boolean get() = _chicken

    private var _pepperoni = false
    val pepperoni: Boolean get() = _pepperoni

    private var _greenPeppers = false
    val greenPeppers: Boolean get() = _greenPeppers

    fun updateSize(newSize: PizzaSize) { _size = newSize }
    fun setChickenChecked(checked: Boolean) { _chicken = checked }
    fun setPepperoniChecked(checked: Boolean) { _pepperoni = checked }
    fun setGreenPeppersChecked(checked: Boolean) { _greenPeppers = checked }
    fun toggleChicken() { _chicken = !_chicken }
    fun togglePepperoni() { _pepperoni = !_pepperoni }
    fun toggleGreenPeppers() { _greenPeppers = !_greenPeppers }

    // ----- Actions -----
    fun addToOrder() {
        val toppings = buildSet {
            if (chicken) add(Topping.CHICKEN)
            if (pepperoni) add(Topping.PEPPERONI)
            if (greenPeppers) add(Topping.GREEN_PEPPERS)
        }
        val pizza = Pizza(size = size, toppings = toppings)
        viewModelScope.launch {
            repository.upsert(pizza)
            _chicken = false; _pepperoni = false; _greenPeppers = false
        }
    }

    fun clearOrder() {
        viewModelScope.launch {
            repository.deleteAll()
            resetSelectionsToDefaults()
        }
    }

    fun deletePizza(pizza: Pizza) {
        viewModelScope.launch { repository.delete(pizza) }
    }

    private fun resetSelectionsToDefaults() {
        _size = PizzaSize.MEDIUM
        _chicken = false
        _pepperoni = false
        _greenPeppers = false
    }
}

// Map your DB/entity model to the UI model if you still use PizzaToppings in the UI.
private fun Pizza.toUiModel(): PizzaToppings =
    PizzaToppings(
        // adjust how you want to show size; example: numeric inches or keep enum->string
        size = when (size) {
            SMALL -> 10
            MEDIUM -> 12
            LARGE -> 14
            XLARGE -> TODO()
        },
        toppings = toppings.joinToString(", ") { t ->
            when (t) {
                Topping.CHICKEN -> "Chicken"
                Topping.PEPPERONI -> "Pepperoni"
                Topping.GREEN_PEPPERS -> "Green Peppers"
            }
        }
    )

/** Simple factory for wiring without DI. */
class MainViewModelFactory(
    private val repository: PizzaRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

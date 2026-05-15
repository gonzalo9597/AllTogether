package com.example.alltogether.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alltogether.model.Gasto
import com.example.alltogether.network.AllTogetherService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel que cachea los gastos y el balance en memoria.
 *
 * Los datos sobreviven a recomposiciones, rotaciones de pantalla
 * y navegación a pantallas hijas (detalle, añadir gasto, ajustes).
 * Solo se recargan cuando se llama a recargar() explícitamente
 * (tras añadir, editar o eliminar un gasto).
 */
class DashboardViewModel : ViewModel() {

    private val service = AllTogetherService()
    private val limitePagina = 20

    private val _gastos = MutableStateFlow<List<Gasto>>(emptyList())
    val gastos: StateFlow<List<Gasto>> = _gastos

    private val _balance = MutableStateFlow<AllTogetherService.Balance?>(null)
    val balance: StateFlow<AllTogetherService.Balance?> = _balance

    private val _hayMas = MutableStateFlow(false)
    val hayMas: StateFlow<Boolean> = _hayMas

    private val _cargandoMas = MutableStateFlow(false)
    val cargandoMas: StateFlow<Boolean> = _cargandoMas

    private val _cargaInicial = MutableStateFlow(false)
    val cargaInicial: StateFlow<Boolean> = _cargaInicial

    private var offsetActual = 0
    private var idParejaActual = -1
    private var yaCargado = false

    /**
     * Carga inicial — solo llama a la API si no hay datos en caché.
     * Se invoca desde LaunchedEffect al abrir el Dashboard.
     */
    fun cargarSiNecesario(idPareja: Int) {
        if (yaCargado && idParejaActual == idPareja) return
        idParejaActual = idPareja
        recargar(idPareja)
    }

    /**
     * Fuerza recarga completa — se invoca al volver de añadir,
     * editar o eliminar un gasto.
     */
    fun recargar(idPareja: Int) {
        idParejaActual = idPareja
        viewModelScope.launch {
            _cargaInicial.value = true
            offsetActual = 0

            val resultado = withContext(Dispatchers.IO) {
                service.getGastos(idPareja, limitePagina, 0)
            }
            _gastos.value = resultado.gastos
            _hayMas.value = resultado.hayMas
            offsetActual = resultado.gastos.size

            val balanceResult = withContext(Dispatchers.IO) {
                service.getBalance(idPareja)
            }
            balanceResult.onSuccess { _balance.value = it }

            yaCargado = true
            _cargaInicial.value = false
        }
    }

    /**
     * Carga la siguiente página de gastos y los acumula.
     */
    fun cargarMas() {
        if (_cargandoMas.value || !_hayMas.value) return
        viewModelScope.launch {
            _cargandoMas.value = true
            val resultado = withContext(Dispatchers.IO) {
                service.getGastos(idParejaActual, limitePagina, offsetActual)
            }
            _gastos.value = _gastos.value + resultado.gastos
            _hayMas.value = resultado.hayMas
            offsetActual += resultado.gastos.size
            _cargandoMas.value = false
        }
    }
}

package edu.ucne.conumirapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { App() } }
    }
}

private object Routes {
    const val LIST = "list"
    const val CREATE = "create"
}

@Composable
fun App(vm: MainViewModel = viewModel()) {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Routes.LIST) {
        composable(Routes.LIST) {
            ListaScreen(
                vm = vm,
                onAddClick = { nav.navigate(Routes.CREATE) }
            )
        }
        composable(Routes.CREATE) {
            CrearScreen(
                vm = vm,
                onSaved = {
                    nav.popBackStack()
                    vm.cargar()
                },
                onCancel = { nav.popBackStack() }
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaScreen(vm: MainViewModel, onAddClick: () -> Unit) {
    val s by vm.ui.collectAsState()

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Prioridades") }) },
        floatingActionButton = { FloatingActionButton(onClick = onAddClick) { Text("+") } }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp).fillMaxSize()) {
            if (s.cargando) LinearProgressIndicator(Modifier.fillMaxWidth())
            s.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(8.dp))

            LazyColumn(Modifier.fillMaxSize()) {
                items(s.lista) { p ->
                    ElevatedCard(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text(p.titulo, style = MaterialTheme.typography.titleMedium)
                            p.descripcion?.let { Text(it) }
                            Text("Nivel: ${when (p.nivel) { 1 -> "Baja"; 2 -> "Media"; 3 -> "Alta"; else -> p.nivel }}")
                            Text("Completada: ${if (p.completada) "Sí" else "No"}")
                            Text("ID: ${p.id ?: "-"}")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearScreen(
    vm: MainViewModel,
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    val s by vm.ui.collectAsState()

    var intentSave by remember { mutableStateOf(false) }
    LaunchedEffect(s.cargando, s.error, intentSave) {
        if (intentSave && !s.cargando && s.error == null) {
            intentSave = false
            onSaved()
        }
    }

    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Nueva prioridad") }) }) { pad ->
        Column(Modifier.padding(pad).padding(16.dp).fillMaxSize()) {

            OutlinedTextField(
                value = s.titulo, onValueChange = vm::setTitulo,
                label = { Text("Título *") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = s.descripcion, onValueChange = vm::setDescripcion,
                label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Nivel: "); Spacer(Modifier.width(8.dp))
                NivelChip("Baja", 1, s.nivel == 1) { vm.setNivel(1) }
                Spacer(Modifier.width(6.dp))
                NivelChip("Media", 2, s.nivel == 2) { vm.setNivel(2) }
                Spacer(Modifier.width(6.dp))
                NivelChip("Alta", 3, s.nivel == 3) { vm.setNivel(3) }
            }

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = s.fechaVencimiento, onValueChange = vm::setFecha,
                label = { Text("Fecha Vto (ISO opcional)") },
                placeholder = { Text("2025-10-20T23:59:59Z") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = s.completada, onCheckedChange = vm::setCompletada)
                Text("Completada")
            }

            Spacer(Modifier.height(16.dp))
            Row {
                Button(onClick = { intentSave = true; vm.registrar() }, enabled = !s.cargando) {
                    Text("Guardar")
                }
                Spacer(Modifier.width(12.dp))
                OutlinedButton(onClick = onCancel, enabled = !s.cargando) { Text("Cancelar") }
            }

            if (s.cargando) { Spacer(Modifier.height(8.dp)); LinearProgressIndicator(Modifier.fillMaxWidth()) }
            s.error?.let { Spacer(Modifier.height(8.dp)); Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun NivelChip(text: String, value: Int, selected: Boolean, onClick: () -> Unit) {
    if (selected) Button(onClick = onClick) { Text(text) }
    else OutlinedButton(onClick = onClick) { Text(text) }
}

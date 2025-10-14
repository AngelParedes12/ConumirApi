package edu.ucne.conumirapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { AppScreen() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(vm: MainViewModel = viewModel()) {
    val s by vm.ui.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Prioridades (GET/POST)") }) }) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).fillMaxSize()
        ) {
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
                Checkbox(checked = s.completada, onCheckedChange = { vm.setCompletada(it) })
                Text("Completada")
            }

            Spacer(Modifier.height(8.dp))
            Row {
                Button(onClick = vm::registrar, enabled = !s.cargando) { Text("Registrar (POST)") }
                Spacer(Modifier.width(12.dp))
                OutlinedButton(onClick = vm::cargar, enabled = !s.cargando) { Text("Refrescar (GET)") }
            }

            if (s.cargando) { Spacer(Modifier.height(8.dp)); LinearProgressIndicator(Modifier.fillMaxWidth()) }
            s.error?.let { Spacer(Modifier.height(8.dp)); Text("Error: $it", color = MaterialTheme.colorScheme.error) }

            Spacer(Modifier.height(16.dp))
            Text("Listado:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            LazyColumn(Modifier.fillMaxSize()) {
                items(s.lista) { p ->
                    ElevatedCard(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text(p.titulo, style = MaterialTheme.typography.titleMedium)
                            p.descripcion?.let { Text(it) }
                            Text("Nivel: ${when(p.nivel){1->"Baja";2->"Media";3->"Alta";else->p.nivel}}")
                            Text("Completada: ${if (p.completada) "Sí" else "No"}")
                            Text("ID: ${p.id ?: "-"}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NivelChip(text: String, value: Int, selected: Boolean, onClick: () -> Unit) {
    if (selected) Button(onClick = onClick) { Text(text) }
    else OutlinedButton(onClick = onClick) { Text(text) }
}

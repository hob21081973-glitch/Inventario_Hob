package com.tuempresa.inventario.ui.report

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReportScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Módulo de Reportes PDF", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { /* Lógica para exportar Reporte General */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📄 Exportar Reporte de Ventas General")
        }

        Button(
            onClick = { /* Lógica para exportar Reporte Crédito */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("💳 Exportar Reporte de Ventas al Crédito")
        }

        Button(
            onClick = { /* Lógica para exportar Reporte Contado */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Text("💵 Exportar Reporte de Ventas al Contado")
        }

        Button(
            onClick = { /* Lógica para exportar Clientes con Crédito */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("👥 Exportar Reporte de Clientes con Crédito")
        }
    }
}

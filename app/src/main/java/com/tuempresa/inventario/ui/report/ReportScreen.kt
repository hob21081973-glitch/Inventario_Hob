package com.tuempresa.inventario.ui.report

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tuempresa.inventario.data.local.CustomerEntity
import com.tuempresa.inventario.data.local.ProductEntity
import com.tuempresa.inventario.data.local.SaleEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportScreen(
    sales: List<SaleEntity>,
    customers: List<CustomerEntity>,
    products: List<ProductEntity>
) {
    val context = LocalContext.current

    var pendingTitle by remember { mutableStateOf("") }
    var pendingDataList by remember { mutableStateOf<List<String>>(emptyList()) }

    // Selector de archivos para elegir carpeta y guardar
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        uri?.let { savePdfToUri(context, it, pendingTitle, pendingDataList) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Módulo de Reportes PDF", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                pendingTitle = "Reporte General de Ventas"
                pendingDataList = sales.map { "${it.date} - ${it.customerName} - ${it.productName} - L ${it.amount} (${it.saleType})" }
                launcher.launch("Reporte_General_Ventas.pdf")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📄 Exportar Reporte General")
        }

        Button(
            onClick = {
                val creditSales = sales.filter { it.saleType == "CREDITO" }
                pendingTitle = "Reporte de Ventas al Crédito"
                pendingDataList = creditSales.map { "${it.date} - ${it.customerName} - ${it.productName} - L ${it.amount}" }
                launcher.launch("Reporte_Ventas_Credito.pdf")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("💳 Exportar Ventas al Crédito")
        }

        Button(
            onClick = {
                val cashSales = sales.filter { it.saleType == "CONTADO" }
                pendingTitle = "Reporte de Ventas al Contado"
                pendingDataList = cashSales.map { "${it.date} - ${it.customerName} - ${it.productName} - L ${it.amount}" }
                launcher.launch("Reporte_Ventas_Contado.pdf")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Text("💵 Exportar Ventas al Contado")
        }

        Button(
            onClick = {
                pendingTitle = "Reporte de Inventario Actual"
                pendingDataList = products.map { "${it.name} - Stock: ${it.stock} - L ${it.price}" }
                launcher.launch("Reporte_Inventario_Actual.pdf")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📦 Exportar Inventario Actual")
        }
    }
}

fun savePdfToUri(context: Context, uri: Uri, title: String, dataList: List<String>) {
    val pdfDocument = PdfDocument()
    val paint = Paint().apply { textSize = 12f }
    val titlePaint = Paint().apply {
        textSize = 18f
        isFakeBoldText = true
    }
    val datePaint = Paint().apply { textSize = 10f }

    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    var page = pdfDocument.startPage(pageInfo)
    var canvas: Canvas = page.canvas

    var y = 50f
    canvas.drawText(title, 50f, y, titlePaint)
    y += 20f

    // Fecha de generación del reporte
    val currentDate = SimpleDateFormat("Fecha de generación: yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
    canvas.drawText(currentDate, 50f, y, datePaint)
    y += 30f

    if (dataList.isEmpty()) {
        canvas.drawText("No hay registros disponibles para este reporte.", 50f, y, paint)
    } else {
        for (item in dataList) {
            if (y > 780f) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 50f
            }
            canvas.drawText("• $item", 50f, y, paint)
            y += 22f
        }
    }

    pdfDocument.finishPage(page)

    try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }
        pdfDocument.close()
        Toast.makeText(context, "PDF guardado exitosamente", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        pdfDocument.close()
        Toast.makeText(context, "Error al guardar el PDF: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

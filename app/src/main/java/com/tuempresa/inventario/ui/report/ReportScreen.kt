package com.tuempresa.inventario.ui.report

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tuempresa.inventario.data.local.CustomerEntity
import com.tuempresa.inventario.data.local.ProductEntity
import com.tuempresa.inventario.data.local.SaleEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportScreen(
    sales: List<SaleEntity>,
    customers: List<CustomerEntity>,
    products: List<ProductEntity>
) {
    val context = LocalContext.current

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
                val data = if (sales.isEmpty()) listOf("No hay ventas registradas.") else sales.map { "${it.date} | Cliente: ${it.customerName} | Producto: ${it.productName} | Total: L ${it.amount} (${it.saleType})" }
                generateAndOpenPdf(context, "Reporte_General_Ventas.pdf", "Reporte General de Ventas", data)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📄 Ver/Exportar Reporte General")
        }

        Button(
            onClick = {
                val creditSales = sales.filter { it.saleType == "CREDITO" }
                val data = if (creditSales.isEmpty()) listOf("No hay ventas al crédito.") else creditSales.map { "${it.date} | Cliente: ${it.customerName} | Producto: ${it.productName} | Total: L ${it.amount}" }
                generateAndOpenPdf(context, "Reporte_Ventas_Credito.pdf", "Reporte de Ventas al Crédito", data)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("💳 Ver/Exportar Ventas al Crédito")
        }

        Button(
            onClick = {
                val cashSales = sales.filter { it.saleType == "CONTADO" }
                val data = if (cashSales.isEmpty()) listOf("No hay ventas al contado.") else cashSales.map { "${it.date} | Cliente: ${it.customerName} | Producto: ${it.productName} | Total: L ${it.amount}" }
                generateAndOpenPdf(context, "Reporte_Ventas_Contado.pdf", "Reporte de Ventas al Contado", data)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Text("💵 Ver/Exportar Ventas al Contado")
        }

        Button(
            onClick = {
                val data = if (products.isEmpty()) listOf("No hay productos en inventario.") else products.map { "Producto: ${it.name} | Stock: ${it.stock} | Precio: L ${it.price}" }
                generateAndOpenPdf(context, "Reporte_Inventario_Actual.pdf", "Reporte de Inventario Actual", data)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📦 Ver/Exportar Inventario Actual")
        }
    }
}

fun generateAndOpenPdf(context: Context, fileName: String, title: String, dataList: List<String>) {
    val pdfDocument = PdfDocument()
    val paint = Paint().apply { textSize = 11f }
    val titlePaint = Paint().apply {
        textSize = 16f
        isFakeBoldText = true
    }
    val datePaint = Paint().apply { textSize = 10f }

    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    var page = pdfDocument.startPage(pageInfo)
    var canvas: Canvas = page.canvas

    var y = 50f
    canvas.drawText(title, 40f, y, titlePaint)
    y += 20f

    val currentDate = SimpleDateFormat("Fecha de generación: yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
    canvas.drawText(currentDate, 40f, y, datePaint)
    y += 30f

    for (item in dataList) {
        if (y > 780f) {
            pdfDocument.finishPage(page)
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            y = 50f
        }
        canvas.drawText("• $item", 40f, y, paint)
        y += 20f
    }

    pdfDocument.finishPage(page)

    try {
        // Guardar de forma segura en el almacenamiento interno de la app
        val file = File(context.filesDir, fileName)
        val fos = FileOutputStream(file)
        pdfDocument.writeTo(fos)
        fos.flush()
        fos.close()
        pdfDocument.close()

        Toast.makeText(context, "PDF generado con éxito (${file.length()} bytes)", Toast.LENGTH_LONG).show()

        // Abrir el PDF automáticamente con cualquier app de lectura instalada en el teléfono
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Reporte guardado en memoria interna. Instale un visor de PDF.", Toast.LENGTH_LONG).show()
        }

    } catch (e: Exception) {
        pdfDocument.close()
        Toast.makeText(context, "Error al generar PDF: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

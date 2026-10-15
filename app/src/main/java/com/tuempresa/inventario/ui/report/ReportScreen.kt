package com.tuempresa.inventario.ui.report

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
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
                generatePdfReport(context, "Reporte_General_Ventas.pdf", "Reporte General de Ventas", sales.map { "${it.date} - ${it.customerName} - ${it.productName} - L ${it.amount} (${it.saleType})" })
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📄 Exportar Reporte General (Downloads)")
        }

        Button(
            onClick = {
                val creditSales = sales.filter { it.saleType == "CREDITO" }
                generatePdfReport(context, "Reporte_Ventas_Credito.pdf", "Reporte de Ventas al Crédito", creditSales.map { "${it.date} - ${it.customerName} - ${it.productName} - L ${it.amount}" })
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("💳 Exportar Ventas al Crédito (Downloads)")
        }

        Button(
            onClick = {
                val cashSales = sales.filter { it.saleType == "CONTADO" }
                generatePdfReport(context, "Reporte_Ventas_Contado.pdf", "Reporte de Ventas al Contado", cashSales.map { "${it.date} - ${it.customerName} - ${it.productName} - L ${it.amount}" })
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Text("💵 Exportar Ventas al Contado (Downloads)")
        }

        Button(
            onClick = {
                generatePdfReport(context, "Reporte_Inventario_Actual.pdf", "Reporte de Inventario Actual", products.map { "${it.name} - Stock: ${it.stock} - L ${it.price}" })
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📦 Exportar Inventario Actual (Downloads)")
        }
    }
}

fun generatePdfReport(context: Context, fileName: String, title: String, dataList: List<String>) {
    val pdfDocument = PdfDocument()
    val paint = Paint().apply {
        textSize = 14f
    }
    val titlePaint = Paint().apply {
        textSize = 18f
        isFakeBoldText = true // 🚀 Corrección aplicada aquí
    }

    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    var page = pdfDocument.startPage(pageInfo)
    var canvas: Canvas = page.canvas

    var y = 50f
    canvas.drawText(title, 50f, y, titlePaint)
    y += 40f

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
            y += 25f
        }
    }

    pdfDocument.finishPage(page)

    try {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }
        val file = File(downloadsDir, fileName)
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()
        Toast.makeText(context, "PDF guardado en Descargas: ${file.name}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        pdfDocument.close()
        Toast.makeText(context, "Error al generar PDF: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

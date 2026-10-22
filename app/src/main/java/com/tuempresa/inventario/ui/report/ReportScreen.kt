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
    var lastGeneratedFile by remember { mutableStateOf<File?>(null) }

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
                val data = if (sales.isEmpty()) {
                    listOf("No hay ventas registradas actualmente.")
                } else {
                    sales.map { "${it.date} - ${it.customerName} - ${it.productName} - L ${it.amount} (${it.saleType})" }
                }
                lastGeneratedFile = generateAndReturnPdf(context, "Reporte_General_Ventas.pdf", "Reporte General de Ventas", data)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📄 Generar Reporte General")
        }

        Button(
            onClick = {
                val creditSales = sales.filter { it.saleType == "CREDITO" }
                val data = if (creditSales.isEmpty()) {
                    listOf("No hay ventas al crédito registradas.")
                } else {
                    creditSales.map { "${it.date} - ${it.customerName} - ${it.productName} - L ${it.amount}" }
                }
                lastGeneratedFile = generateAndReturnPdf(context, "Reporte_Ventas_Credito.pdf", "Reporte de Ventas al Crédito", data)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("💳 Generar Ventas al Crédito")
        }

        Button(
            onClick = {
                val cashSales = sales.filter { it.saleType == "CONTADO" }
                val data = if (cashSales.isEmpty()) {
                    listOf("No hay ventas al contado registradas.")
                } else {
                    cashSales.map { "${it.date} - ${it.customerName} - ${it.productName} - L ${it.amount}" }
                }
                lastGeneratedFile = generateAndReturnPdf(context, "Reporte_Ventas_Contado.pdf", "Reporte de Ventas al Contado", data)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Text("💵 Generar Ventas al Contado")
        }

        Button(
            onClick = {
                val data = if (products.isEmpty()) {
                    listOf("No hay productos en inventario.")
                } else {
                    products.map { "${it.name} - Stock: ${it.stock} - L ${it.price}" }
                }
                lastGeneratedFile = generateAndReturnPdf(context, "Reporte_Inventario_Actual.pdf", "Reporte de Inventario Actual", data)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📦 Generar Inventario Actual")
        }

        // Si ya se generó un reporte, mostramos un botón directo para abrirlo en pantalla
        if (lastGeneratedFile != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    openPdfFile(context, lastGeneratedFile!!)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
            ) {
                Text("👁️ Abrir Último Reporte Generado")
            }
        }
    }
}

fun generateAndReturnPdf(context: Context, fileName: String, title: String, dataList: List<String>): File? {
    val pdfDocument = PdfDocument()
    try {
        val paint = Paint().apply { textSize = 12f }
        val titlePaint = Paint().apply {
            textSize = 16f
            isFakeBoldText = true
        }

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas: Canvas = page.canvas

        var y = 50f
        canvas.drawText(title, 40f, y, titlePaint)
        y += 25f

        val dateFormatted = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Fecha: $dateFormatted", 40f, y, paint)
        y += 35f

        for (item in dataList) {
            if (y > 780f) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 50f
            }
            canvas.drawText("• $item", 40f, y, paint)
            y += 22f
        }

        pdfDocument.finishPage(page)

        val file = File(context.filesDir, fileName)
        val fos = FileOutputStream(file)
        pdfDocument.writeTo(fos)
        fos.flush()
        fos.close()
        pdfDocument.close()

        Toast.makeText(context, "¡Reporte generado con éxito!", Toast.LENGTH_SHORT).show()
        
        // Abrir automáticamente el archivo generado
        openPdfFile(context, file)
        
        return file
    } catch (e: Exception) {
        try {
            pdfDocument.close()
        } catch (_: Exception) {}
        Toast.makeText(context, "Error al crear reporte: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        return null
    }
}

fun openPdfFile(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No se encontró un visor de PDF instalado en el dispositivo.", Toast.LENGTH_LONG).show()
    }
}

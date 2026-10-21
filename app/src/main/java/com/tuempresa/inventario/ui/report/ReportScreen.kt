package com.tuempresa.inventario.ui.report

import android.content.Context
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
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import com.tuempresa.inventario.data.local.CustomerEntity
import com.tuempresa.inventario.data.local.ProductEntity
import com.tuempresa.inventario.data.local.SaleEntity
import java.io.ByteArrayOutputStream
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

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        uri?.let { savePdfWithIText(context, it, pendingTitle, pendingDataList) }
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
                pendingDataList = if (sales.isEmpty()) {
                    listOf("No hay ventas registradas actualmente.")
                } else {
                    sales.map { "${it.date} | Cliente: ${it.customerName} | Producto: ${it.productName} | Total: L ${it.amount} (${it.saleType})" }
                }
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
                pendingDataList = if (creditSales.isEmpty()) {
                    listOf("No hay ventas al crédito registradas.")
                } else {
                    creditSales.map { "${it.date} | Cliente: ${it.customerName} | Producto: ${it.productName} | Total: L ${it.amount}" }
                }
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
                pendingDataList = if (cashSales.isEmpty()) {
                    listOf("No hay ventas al contado registradas.")
                } else {
                    cashSales.map { "${it.date} | Cliente: ${it.customerName} | Producto: ${it.productName} | Total: L ${it.amount}" }
                }
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
                pendingDataList = if (products.isEmpty()) {
                    listOf("No hay productos registrados en el inventario.")
                } else {
                    products.map { "Producto: ${it.name} | Stock: ${it.stock} | Precio: L ${it.price}" }
                }
                launcher.launch("Reporte_Inventario_Actual.pdf")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📦 Exportar Inventario Actual")
        }
    }
}

fun savePdfWithIText(context: Context, uri: Uri, title: String, dataList: List<String>) {
    try {
        val document = Document()
        val byteStream = ByteArrayOutputStream()
        
        PdfWriter.getInstance(document, byteStream)
        document.open()

        // Estilos de fuentes
        val titleFont = Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD)
        val dateFont = Font(Font.FontFamily.HELVETICA, 10f, Font.ITALIC)
        val bodyFont = Font(Font.FontFamily.HELVETICA, 11f, Font.NORMAL)

        // Título del Reporte
        val pTitle = Paragraph(title, titleFont).apply {
            alignment = Element.ALIGN_LEFT
            spacingAfter = 10f
        }
        document.add(pTitle)

        // Fecha de generación
        val currentDate = SimpleDateFormat("Fecha de generación: yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val pDate = Paragraph(currentDate, dateFont).apply {
            spacingAfter = 20f
        }
        document.add(pDate)

        // Contenido de ventas/productos
        for (item in dataList) {
            val pItem = Paragraph("• $item", bodyFont).apply {
                spacingAfter = 6f
            }
            document.add(pItem)
        }

        document.close()

        // Escribir el buffer binario iText directamente al URI destino
        val pdfBytes = byteStream.toByteArray()
        context.contentResolver.openOutputStream(uri, "w")?.use { outputStream ->
            outputStream.write(pdfBytes)
            outputStream.flush()
        }

        Toast.makeText(context, "PDF generado exitosamente (${pdfBytes.size / 1024} KB)", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error al generar PDF: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

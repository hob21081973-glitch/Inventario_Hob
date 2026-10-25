package com.tuempresa.inventario.ui.report

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
            .padding(16.dp)
    ) {
        Text("Módulo de Reportes Tabulares", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Visualización en formato de columnas (Nombre, Stock y Precio)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.height(16.dp))

        // Botones de generación de reportes
        Button(
            onClick = {
                lastGeneratedFile = generateTabularPdf(context, "Reporte_Inventario_Productos.pdf", "Reporte General de Stock y Precios", products)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📦 Generar Reporte Tabular de Productos")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(8.dp))

        // Previsualización Tabular en pantalla estilo Excel
        Text("Vista Previa (Tabla de Inventario)", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // Encabezados de la Tabla en Pantalla
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Nombre del producto", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold)
                Text("Stock", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text("Precio", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Filas de la Tabla en Pantalla
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (products.isEmpty()) {
                item {
                    Text("No hay productos registrados.", modifier = Modifier.padding(8.dp))
                }
            } else {
                items(products) { product ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = product.name, modifier = Modifier.weight(2f))
                            Text(text = "${product.stock}", modifier = Modifier.weight(1f))
                            Text(text = "L ${product.price}", modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Si ya se generó un reporte, mostramos un botón directo para abrirlo
        if (lastGeneratedFile != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    openPdfFile(context, lastGeneratedFile!!)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
            ) {
                Text("👁️ Abrir Último Reporte PDF Generado")
            }
        }
    }
}

fun generateTabularPdf(context: Context, fileName: String, title: String, products: List<ProductEntity>): File? {
    val pdfDocument = PdfDocument()
    try {
        val paint = Paint().apply { textSize = 12f }
        val boldPaint = Paint().apply { 
            textSize = 12f
            isFakeBoldText = true 
        }
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

        // Dibujar Cabecera de la Tabla en PDF
        canvas.drawText("Nombre del Producto", 40f, y, boldPaint)
        canvas.drawText("Stock", 350f, y, boldPaint)
        canvas.drawText("Precio", 460f, y, boldPaint)
        y += 8f
        
        // Línea divisora de la cabecera
        canvas.drawLine(40f, y, 555f, y, boldPaint)
        y += 20f

        // Recorrer productos y agregarlos en formato de columnas
        for (product in products) {
            if (y > 780f) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 50f
            }
            canvas.drawText(product.name, 40f, y, paint)
            canvas.drawText(product.stock.toString(), 350f, y, paint)
            canvas.drawText("L ${product.price}", 460f, y, paint)
            y += 22f
        }

        pdfDocument.finishPage(page)

        val file = File(context.filesDir, fileName)
        val fos = FileOutputStream(file)
        pdfDocument.writeTo(fos)
        fos.flush()
        fos.close()
        pdfDocument.close()

        Toast.makeText(context, "¡Reporte tabular generado con éxito!", Toast.LENGTH_SHORT).show()
        
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

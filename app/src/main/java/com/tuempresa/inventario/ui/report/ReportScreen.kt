fun generateSafePdf(context: Context, fileName: String, title: String, dataList: List<String>) {
    val pdfDocument = PdfDocument()
    try {
        val paint = Paint().apply { textSize = 12f }
        val titlePaint = Paint().apply {
            textSize = 16f
            isFakeBoldText = true
        }

        // Configurar página tamaño A4 estándar (595 x 842 píxeles)
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas: Canvas = page.canvas

        var y = 50f
        canvas.drawText(title, 40f, y, titlePaint)
        y += 25f

        // 🚀 Solución: Separar el texto estático de la fecha formateada para evitar conflicto con la letra 'e'
        val dateFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        canvas.drawText("Fecha: $dateFormatted", 40f, y, paint)
        y += 35f

        for (item in dataList) {
            // Si la línea se pasa del alto de la hoja, cerramos página y abrimos otra
            if (y > 780f) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 50f
            }
            canvas.drawText("• $item", 40f, y, paint)
            y = y + 22f
        }

        pdfDocument.finishPage(page)

        // Guardar de forma segura en la carpeta interna de archivos de la app
        val file = File(context.filesDir, fileName)
        val fos = FileOutputStream(file)
        pdfDocument.writeTo(fos)
        fos.flush()
        fos.close()
        pdfDocument.close()

        Toast.makeText(context, "¡Reporte guardado con éxito en la app!", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        try {
            pdfDocument.close()
        } catch (_: Exception) {}
        Toast.makeText(context, "Error al crear reporte: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}

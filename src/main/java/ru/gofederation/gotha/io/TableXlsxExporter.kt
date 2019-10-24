/*
 * This file is part of OpenGotha.
 *
 * OpenGotha is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * OpenGotha is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenGotha. If not, see <http://www.gnu.org/licenses/>.
 */

package ru.gofederation.gotha.io

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import ru.gofederation.gotha.util.GothaLocale
import ru.gofederation.gotha.util.I18N
import java.awt.Component
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileFilter
import javax.swing.table.TableModel

class TableXlsxExporter(gothaLocale: GothaLocale = GothaLocale.getCurrentLocale()) : I18N by gothaLocale {
    fun exportToFile(table: TableModel, parent: Component, filter: FileFilter?, ext: String) {
        val fc = JFileChooser()
        if (null != filter) fc.fileFilter = filter
        val option = fc.showSaveDialog(parent)
        if (option != JFileChooser.APPROVE_OPTION) return

        var file = fc.selectedFile
        val fileName = file.name.toLowerCase()
        if (!fileName.endsWith(".$ext")) {
            file = File(file.absolutePath + '.' + ext)
        }

        if (file.exists()) {
            val confirm = JOptionPane.showConfirmDialog(
                parent,
                tr("export.file_exists", file.absolutePath),
                tr("export.file_exists_title"),
                JOptionPane.YES_NO_OPTION)
            if (confirm != JOptionPane.YES_OPTION) return
        }

        FileOutputStream(file).use {
            export(table, it)
        }
    }

    fun export(table: TableModel, outputStream: OutputStream) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet()

        val header = sheet.createRow(0)
        for (nColumn in 0 until table.columnCount) {
            val cell = header.createCell(nColumn)
            cell.setCellValue(table.getColumnName(nColumn))
        }

        for (nRow in 0 until table.rowCount) {
            val row = sheet.createRow(nRow + 1)
            for (nColumn in 0 until table.columnCount) {
                val cell = row.createCell(nColumn)
                cell.setCellValue(table.getValueAt(nRow, nColumn).toString().trim())
            }
        }

        for (nColumn in 0 until table.columnCount) {
            sheet.autoSizeColumn(nColumn)
        }

        workbook.write(outputStream)
    }
}

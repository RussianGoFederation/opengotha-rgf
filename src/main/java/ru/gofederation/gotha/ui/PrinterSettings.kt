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

package ru.gofederation.gotha.ui

import net.miginfocom.swing.MigLayout
import ru.gofederation.gotha.printing.Printer
import ru.gofederation.gotha.ui.component.FloatInput
import ru.gofederation.gotha.util.addChangeListener
import java.awt.Component
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.print.PageFormat
import java.awt.print.Paper
import java.awt.print.Printable
import java.awt.print.PrinterJob
import java.text.NumberFormat
import javax.print.PrintService
import javax.print.PrintServiceLookup
import javax.print.attribute.HashPrintRequestAttributeSet
import javax.print.attribute.Size2DSyntax
import javax.print.attribute.standard.*
import javax.swing.*
import javax.swing.BorderFactory.createTitledBorder

class PrinterSettings(private val printable: Printable) : Panel() {
    override val preferencesNode = PREFERENCES_NODE

    private val printService = JComboBox<PrintService>()
    private val status = JLabel()
    private val type = JLabel()
    private val info = JLabel()

    private val printAll = JRadioButton(tr("printing.page_range.all"))
    private val printPages = JRadioButton(tr("printing.page_range.pages"))
    private val printFirstPage = JTextField()
    private val printLastPage = JTextField()

    private val numberOfCopies = JSpinner().apply {
        model = SpinnerNumberModel(preferences.getInt(COPIES, 1), 1, 100, 1)
        isEnabled = false
    }
    private val collate = JCheckBox(tr("printing.copies.collage")).apply {
        isSelected = preferences.getBoolean(COLLATE, true)
        isEnabled = false
    }

    private val mediaSize = JComboBox<Media>()
    private val mediaSource = JComboBox<String>().apply {
        isEnabled = false
    }

    private val portrait = JRadioButton(tr("printing.page_setup.orientation.portrait"))
    private val landscape = JRadioButton(tr("printing.page_setup.orientation.landscape"))

    private val marginFormat = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 1
    }
    private val marginTop = JFormattedTextField(marginFormat).apply {
        addChangeListener { invalidatePreview() }
    }
    private val marginLeft = JFormattedTextField(marginFormat).apply {
        addChangeListener { invalidatePreview() }
    }
    private val marginRight = JFormattedTextField(marginFormat).apply {
        addChangeListener { invalidatePreview() }
    }
    private val marginBottom = JFormattedTextField(marginFormat).apply {
        addChangeListener { invalidatePreview() }
    }

    private var marginTopValue by FloatInput(marginTop, marginFormat)
    private var marginLeftValue by FloatInput(marginLeft, marginFormat)
    private var marginRightValue by FloatInput(marginRight, marginFormat)
    private var marginBottomValue by FloatInput(marginBottom, marginFormat)

    init {
        marginTopValue = preferences.getInt(MARGIN_TOP, (2.5 * CM).toInt()).toFloat() / CM
        marginLeftValue = preferences.getInt(MARGIN_LEFT, (2.5 * CM).toInt()).toFloat() / CM
        marginRightValue = preferences.getInt(MARGIN_RIGHT, (2.5 * CM).toInt()).toFloat() / CM
        marginBottomValue = preferences.getInt(MARGIN_BOTTOM, (2.5 * CM).toInt()).toFloat() / CM
    }

    private val fontSelector = JComboBox<Font>()
    private val fontSize = JFormattedTextField(marginFormat).apply {
        text = marginFormat.format(preferences.getFloat(FONT_SIZE, 10.0f))
        addChangeListener { invalidatePreview() }
    }

    private val printPreview = PrintPreview().also { printPreview ->
        printPreview.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(evt: ComponentEvent) {
                super.componentShown(evt)

                printPreview.zoomFitWidth()
            }
        })
    }

    private val mediaSizes = arrayOf(
        Media("A4", MediaSizeName.ISO_A4),
        Media("Letter", MediaSizeName.NA_LETTER)
    )

    private val printFont: Font
        get() {
            val font = fontSelector.selectedItem as Font?
            return if (null != font) {
                font.deriveFont(marginFormat.parse(fontSize.text).toFloat())
            } else {
                JLabel().font.deriveFont(10f)
            }
        }

    private val pageFormat: PageFormat
        get() {
            val isoA5Size = MediaSize.getMediaSizeForName((mediaSize.selectedItem as Media).media)
            val size = isoA5Size.getSize(Size2DSyntax.INCH)
//            if (landscape.isSelected) {
//                size[0] = size[1].also { size[1] = size[0] }
//            }
            val paper = Paper()
            val margins = arrayOf(
                marginFormat.parse(marginLeft.text).toDouble(),
                marginFormat.parse(marginTop.text).toDouble(),
                marginFormat.parse(marginRight.text).toDouble(),
                marginFormat.parse(marginBottom.text).toDouble()
            ).map {
                it * 0.393701
            }
            paper.setSize(size[0] * 72.0, size[1] * 72.0)
            paper.setImageableArea(margins[0] * 72.0,
                                   margins[1] * 72.0,
                                   (size[0] - (margins[0] + margins[2])) * 72.0,
                                   (size[1] - (margins[1] + margins[3])) * 72.0)
            val pf = PageFormat()
            pf.orientation = if (landscape.isSelected) PageFormat.LANDSCAPE else PageFormat.PORTRAIT
            pf.paper = paper
            return pf
        }

    init {
        layout = MigLayout("insets dialog", "[]u[100:400:]", "[align top]u[]")

        add(JTabbedPane().apply {
            add(tr("printing.general"), JPanel(MigLayout("insets panel")).apply {
                add(JPanel(MigLayout("insets panel, wrap 2")).apply {
                    border = createTitledBorder(tr("printing.print_service"))

                    add(JLabel(tr("printing.print_service.name")))
                    add(printService.apply {
                        val printServices = PrintServiceLookup.lookupPrintServices(null, null)
                        model = DefaultComboBoxModel(printServices)
                        val printServiceName = preferences.get(PRINTER, "")
                        selectedItem = printServices.find { it.name == printServiceName }
                            ?: PrintServiceLookup.lookupDefaultPrintService().also {
                                updatePrinterInfoDisplay(it)
                            }

                        addActionListener {
                            updatePrinterInfoDisplay(selectedItem as PrintService)
                        }
                    }, "wmin $INPUT_L, push, grow")

                    add(JLabel(tr("printing.print_service.status")))
                    add(status, "grow")

                    add(JLabel(tr("printing.print_service.type")))
                    add(type, "grow")

                    add(JLabel(tr("printing.print_service.info")))
                    add(info)
                }, "grow, spanx 2, wrap")

                add(JPanel(MigLayout("insets panel")).apply {
                    border = createTitledBorder(tr("printing.page_range"))

                    ButtonGroup().also { group ->
                        add(printAll.apply {
                            group.add(this)
                            isSelected = true
                            isEnabled = false
                        }, "span, wrap")

                        add(printPages.apply {
                            group.add(this)
                            isEnabled = false

                        })
                    }

                    add(printFirstPage.apply { isEnabled = false }, inputSmallCC)

                    add(JLabel(tr("printing.page_range.pages_to")))

                    add(printLastPage.apply { isEnabled = false }, inputSmallCC)
                }, "grow")

                add(JPanel(MigLayout("insets panel, wrap 2")).apply {
                    border = createTitledBorder(tr("printing.copies"))

                    add(JLabel(tr("printing.copies.number")))

                    add(numberOfCopies, inputXSmallCC)

                    add(collate)
                }, "grow")
            })

            add(tr("printing.page_setup"), JPanel(MigLayout("insets panel", "[grow, fill]u[grow, fill]", "[]u[grow, fill]")).apply {
                add(JPanel(MigLayout("insets panel, wrap 2", "[][fill, grow]")).apply {
                    border = createTitledBorder(tr("printing.page_setup.media"))

                    add(JLabel(tr("printing.page_setup.media.size")))
                    add(mediaSize.apply {
                        model = DefaultComboBoxModel(mediaSizes)
                        selectedItem = mediaSizes.find {
                            val media = preferences.get(MEDIA_SIZE, MediaSizeName.ISO_A4.toString())
                            it.media.toString() == media
                        }
                        addActionListener {
                            invalidatePreview()
                        }
                    })

                    add(JLabel(tr("printing.page_setup.media.source")))
                    add(mediaSource)
                }, "growx, spanx 2, wrap")

                add(JPanel(MigLayout("insets panel, flowy")).apply {
                    border = createTitledBorder(tr("printing.page_setup.orientation"))

                    ButtonGroup().also { group ->
                        if (preferences.get(ORIENTATION, PORTRAIT) == PORTRAIT) {
                            portrait.isSelected = true
                        } else {
                            landscape.isSelected = true
                        }
                        add(portrait.apply {
                            addActionListener {
                                invalidatePreview()
                                printPreview.zoomFitWidth()
                            }
                            group.add(this)
                        })
                        add(landscape.apply {
                            addActionListener {
                                invalidatePreview()
                                printPreview.zoomFitWidth()
                            }
                            group.add(this)
                        })
                    }
                }, "growx")

                add(JPanel(MigLayout("insets panel, flowy", "[]u[]", "[][]u[][]u[][]")).apply {
                    border = createTitledBorder(tr("printing.page_setup.margins"))

                    add(JLabel(tr("printing.page_setup.margins.top")), "spanx 2, x top.x")
                    add(marginTop.apply {
                    }, "ax c, sg m, wmin $INPUT_S, spanx 2, id top")

                    add(JLabel(tr("printing.page_setup.margins.left")))
                    add(marginLeft, "sg m")

                    add(JLabel(tr("printing.page_setup.margins.bottom")), "x top.x, spanx 2")
                    add(marginBottom, "x top.x, sg m, spanx 2, wrap")

                    add(JLabel(tr("printing.page_setup.margins.right")))
                    add(marginRight, "sg m")
                }, "growx")
            })

            add(tr("printing.font"), JPanel(MigLayout("insets panel", "[grow, fill]u[]")).apply {
                add(fontSelector.apply {
                    val fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().allFonts
                    this.model = DefaultComboBoxModel(fonts)

                    this.selectedItem = fonts.find {
                        val fontName = preferences.get(FONT, JLabel().font.fontName)
                        it.fontName == fontName
                    }

                    this.renderer = object : DefaultListCellRenderer() {
                        override fun getListCellRendererComponent(list: JList<*>, value: Any,
                                                         index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
                            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)

                            val size = this.font.size
                            val font = (value as Font).deriveFont(size.toFloat())
                            text = font.getFontName(locale)
                            this.font = font
                            return this
                        }
                    }

                    if (printable is Printer) {
                        this.isEnabled = true
                        this.addItemListener {
                            invalidatePreview()
                        }
                    } else {
                        this.isEnabled = false
                    }
                })

                add(fontSize, inputSmallCC)
            })
        })

        add(printPreview, "spany 2, push, grow")

        add(JButton(tr("printing.print")).apply {
            addActionListener {
                savePreferences()

                val printerJob = PrinterJob.getPrinterJob()
                printerJob.printService = printService.selectedItem as PrintService
                printerJob.setPrintable(printable)
                printerJob.print(HashPrintRequestAttributeSet().apply {
                    val mediaSizeName = (mediaSize.selectedItem as Media).media
                    add(mediaSizeName)
                    add(if (portrait.isSelected) OrientationRequested.PORTRAIT else OrientationRequested.LANDSCAPE)
                    add(MediaPrintableArea(
                        marginLeftValue,
                        marginTopValue,
                        MediaSize.getMediaSizeForName(mediaSizeName).getX(CM) - marginLeftValue - marginRightValue,
                        MediaSize.getMediaSizeForName(mediaSizeName).getY(CM) - marginTopValue - marginBottomValue,
                        CM
                    ))
                })

                closeWindow()
            }
        }, "split, newline, tag ok")

        add(JButton(tr("btn.cancel")).apply {
            addActionListener {
                closeWindow()
            }
        }, "tag cancel")

        invalidatePreview()
    }

    private fun updatePrinterInfoDisplay(printService: PrintService) {
        val attributes = printService.attributes

        info.text = attributes.get(PrinterInfo::class.java)?.toString()
        status.text = when(attributes.get(PrinterIsAcceptingJobs::class.java)) {
            PrinterIsAcceptingJobs.ACCEPTING_JOBS -> tr("printing.printer_is_accepting_jobs")
            PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS -> tr("printing.printer_is_not_accepting_jobs")
            else -> ""
        }
    }

    private fun invalidatePreview() {
        if (printable is Printer) {
            printable.setFont(printFont)
        }
        printPreview.setPrintable(printable, pageFormat)
    }

    private fun savePreferences() {
        preferences.put(PRINTER, (printService.selectedItem as PrintService).name)
        preferences.putInt(MARGIN_TOP, (marginTopValue * CM).toInt())
        preferences.putInt(MARGIN_LEFT, (marginLeftValue * CM).toInt())
        preferences.putInt(MARGIN_RIGHT, (marginRightValue * CM).toInt())
        preferences.putInt(MARGIN_BOTTOM, (marginBottomValue * CM).toInt())
        preferences.put(MEDIA_SIZE, (mediaSize.selectedItem as Media).media.toString())
        preferences.put(ORIENTATION, if (portrait.isSelected) PORTRAIT else LANDSCAPE)
        preferences.put(FONT, (fontSelector.selectedItem as Font).fontName)
        preferences.putFloat(FONT_SIZE, marginFormat.parse(fontSize.text).toFloat())
//        preferences.putInt(COPIES, numberOfCopies.value as Int)
//        preferences.putBoolean(COLLATE, collate.isSelected)
    }

    data class Media(val name: String, val media: MediaSizeName) {
        override fun toString(): String {
            return name
        }
    }

    companion object {
        private const val PREFERENCES_NODE = "printing"
        private const val PRINTER = "printer"
        private const val MARGIN_TOP = "margin_top"
        private const val MARGIN_LEFT = "margin_left"
        private const val MARGIN_RIGHT = "margin_right"
        private const val MARGIN_BOTTOM = "margin_bottom"
        private const val MEDIA_SIZE = "media_size"
        private const val ORIENTATION = "orientation"
        private const val PORTRAIT = "portrait"
        private const val LANDSCAPE = "landscape"
        private const val FONT = "font"
        private const val FONT_SIZE = "font_size"
        private const val COPIES = "copies"
        private const val COLLATE = "collate"

        private const val CM: Int = Size2DSyntax.MM * 10
    }
}

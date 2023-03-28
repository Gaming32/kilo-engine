package io.github.gaming32.kiloengine.util

import io.github.gaming32.kiloengine.ui.ENGINE_NAME
import java.awt.Image
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import java.awt.datatransfer.ClipboardOwner
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.image.RenderedImage
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import javax.imageio.ImageIO

class Screenshot(val image: Image, val time: LocalDateTime) : Transferable {

    companion object {
        @JvmStatic
        fun capture(area: Rectangle) = Screenshot(Robot().createScreenCapture(area), LocalDateTime.now())
    }


    @Throws(UnsupportedFlavorException::class, IOException::class)
    override fun getTransferData(flavor: DataFlavor): Any = when(flavor) {
        DataFlavor.imageFlavor -> image
        DataFlavor.stringFlavor -> "$ENGINE_NAME screenshot taken at $time."

        else -> throw UnsupportedFlavorException(flavor)
    }

    override fun getTransferDataFlavors(): Array<DataFlavor> = arrayOf(DataFlavor.imageFlavor, DataFlavor.stringFlavor)

    override fun isDataFlavorSupported(flavor: DataFlavor): Boolean = transferDataFlavors.contains(flavor)

    fun save() {
        if (image !is RenderedImage) return

        try {
            ImageIO.write(image, "png", File("screenshots/$time".replace(Regex("[T:.]"), "-") + ".png"))
        } catch (e : Exception) {
            e.printStackTrace()
        }
    }

    fun copy(clipboardOwner: ClipboardOwner? = null) {
        try {
            Toolkit.getDefaultToolkit().systemClipboard.setContents(this, clipboardOwner)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
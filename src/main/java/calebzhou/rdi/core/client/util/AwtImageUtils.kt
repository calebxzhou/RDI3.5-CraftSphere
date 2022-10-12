package calebzhou.rdi.core.client.util

import calebzhou.rdi.core.client.RdiCore
import java.awt.Image
import javax.swing.ImageIcon

object AwtImageUtils {
    @JvmStatic
	fun createImage(path: String, description: String?): Image? {
        val imageURL = RdiCore::class.java.getResource(path)
        return if (imageURL == null) {
            System.err.println("Resource not found: $path")
            null
        } else {
            ImageIcon(imageURL, description).image
        }
    }
}

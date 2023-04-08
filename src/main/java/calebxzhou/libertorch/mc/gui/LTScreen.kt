package calebxzhou.libertorch.mc.gui

import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

/**
 * Created  on 2023-03-01,16:23.
 */
open class LTScreen : Screen {

    constructor(title: Component) : super(title)
    constructor(title: String): super(Component.literal(title))

}

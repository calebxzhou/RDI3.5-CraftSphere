package calebzhou.rdi.core.client.mixin;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(JoinMultiplayerScreen.class)
public class mMultiPlayerScreen extends Screen {
    private mMultiPlayerScreen(Component component) {
        super(component);
    }

    @Redirect(method = "init",at = @At(value = "INVOKE",ordinal = 1,target = "Lnet/minecraft/client/gui/screens/multiplayer/JoinMultiplayerScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;"))
    private GuiEventListener goRDI(JoinMultiplayerScreen instance, GuiEventListener guiEventListener){
        return addRenderableWidget(new Button(this.width / 2 - 50,
                this.height - 52, 100, 20,
                Component.literal("进入RDI服务器").withStyle(Style.EMPTY.withColor(TextColor.parseColor("GREEN"))),
                button -> calebzhou.rdi.core.client.misc.ServerConnector.connect()));
    }
}

package calebzhou.rdi.craftsphere.mixin;

import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(GameMenuScreen.class)
public class MixinGameMenuScreen extends Screen {

    protected MixinGameMenuScreen(Text title) {
        super(title);
    }
    /**
     * @author
     */
    @Overwrite
    private void initWidgets() {
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 102, this.height / 4 + 24 + -16, 204, 20, new TranslatableText("menu.returnToGame"), (button) -> {
            this.client.setScreen((Screen) null);
            this.client.mouse.lockCursor();
        }));
        /*this.addDrawableChild(new ButtonWidget(this.width / 2 - 102, this.height / 4 + 48 + -16, 98, 20, new TranslatableText("gui.advancements"), (button) -> {
            this.client.setScreen(new AdvancementsScreen(this.client.player.networkHandler.getAdvancementHandler()));
        }));
        this.addDrawableChild(new ButtonWidget(this.width / 2 + 4, this.height / 4 + 48 + -16, 98, 20, new TranslatableText("gui.stats"), (button) -> {
            this.client.setScreen(new StatsScreen(this, this.client.player.getStatHandler()));
        }));*/

        this.addDrawableChild(new ButtonWidget(this.width / 2 - 102, this.height / 4 + 96 + -16, 98, 20, new TranslatableText("menu.options"), (button) -> {
            this.client.setScreen(new OptionsScreen(this, this.client.options));
        }));

        Text text = this.client.isInSingleplayer() ? new TranslatableText("menu.returnToMenu") : new TranslatableText("menu.disconnect");
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 102, this.height / 4 + 120 + -16, 204, 20, text, (button) -> {
            boolean bl = this.client.isInSingleplayer();
            boolean bl2 = this.client.isConnectedToRealms();
            button.active = false;
            this.client.world.disconnect();
            if (bl) {
                this.client.disconnect(new SaveLevelScreen(new TranslatableText("menu.savingLevel")));
            } else {
                this.client.disconnect();
            }

            TitleScreen titleScreen = new TitleScreen();
            if (bl) {
                this.client.setScreen(titleScreen);
            } else if (bl2) {
                this.client.setScreen(new RealmsMainScreen(titleScreen));
            } else {
                this.client.setScreen(new MultiplayerScreen(titleScreen));
            }

        }));
    }
}

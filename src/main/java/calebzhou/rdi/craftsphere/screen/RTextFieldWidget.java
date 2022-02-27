package calebzhou.rdi.craftsphere.screen;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class RTextFieldWidget extends ClickableWidget implements Drawable, Element {
    public RTextFieldWidget(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
    }


}

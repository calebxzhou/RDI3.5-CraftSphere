package calebzhou.rdi.craftsphere.emojiful.render;

import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;

public  class CharacterProcessor implements FormattedCharSequence {

    public final int pos;
    public final Style style;
    public final int character;

    CharacterProcessor(int pos, Style style, int character) {
        this.pos = pos;
        this.style = style;
        this.character = character;
    }

    @Override
    public boolean accept(FormattedCharSink iCharacterConsumer) {
        return iCharacterConsumer.accept(pos, style, character);
    }
}


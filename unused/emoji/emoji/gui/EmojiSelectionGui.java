package calebzhou.rdi.core.client.emoji.gui;

import calebzhou.rdi.core.client.emoji.EmojiClientProxy;
import calebzhou.rdi.core.client.emoji.Emojiful;
import calebzhou.rdi.core.client.emoji.api.Emoji;
import calebzhou.rdi.core.client.emoji.api.EmojiCategory;
import calebzhou.rdi.core.client.screen.RdiChatScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class EmojiSelectionGui    {

    private int selectionPointer;
    private int categoryPointer;
    private RdiChatScreen chatScreen;
    private int openSelectionAreaEmoji;
    private boolean showingSelectionArea;
   // private EditBox fieldWidget;

    private Rect2i openSelectionArea;
    private Rect2i selectionArea;
    private Rect2i categorySelectionArea;
    private Rect2i emojiInfoArea;
    private Rect2i textFieldRectangle;

    private double lastMouseX;
    private double lastMouseY;
    private Emoji lastEmoji;
    private List<Emoji[]> filteredEmojis;

    public EmojiSelectionGui(RdiChatScreen screen) {
        this.selectionPointer = 1;
        this.categoryPointer = 0;
        this.chatScreen = screen;
        this.openSelectionAreaEmoji = 29;//小笑脸
        this.showingSelectionArea = false;
        int offset = 0;
       // if (ModList.get().isLoaded("quark")) offset = -80;
        this.openSelectionArea = new Rect2i(chatScreen.width - 14, chatScreen.height - 12, 12, 12);
        this.selectionArea = new Rect2i(chatScreen.width - 14 - 11*12 + offset , chatScreen.height - 16 - 10*11 - 4, 11*12 + 4, 10*11 + 4);
        this.categorySelectionArea = new Rect2i(this.selectionArea.getX(), this.selectionArea.getY() + 20, 22, this.selectionArea.getHeight() - 20);
        this.emojiInfoArea = new Rect2i(this.selectionArea.getX() + 22, this.selectionArea.getY() + this.selectionArea.getHeight() - 20,  this.selectionArea.getWidth() - 22,  20);
        this.textFieldRectangle = new Rect2i(selectionArea.getX() + 6, selectionArea.getY() + 6, selectionArea.getWidth() -12, 10);
        /*this.fieldWidget = new EditBox(EmojiClientProxy.oldFontRenderer, textFieldRectangle.getX(), textFieldRectangle.getY(), textFieldRectangle.getWidth(), textFieldRectangle.getHeight(), MutableComponent.create(new LiteralContents("")));
        this.fieldWidget.setEditable(true);
        this.fieldWidget.setVisible(true);*/
        this.filteredEmojis = new ArrayList<>();
    }


    public void render(PoseStack stack) {
        if (this.openSelectionAreaEmoji != -1)
			Minecraft.getInstance().font.draw(stack, Emojiful.EMOJI_MAP.get("Smileys & Emotion").get(openSelectionAreaEmoji).strings.get(0), openSelectionArea.getX(), openSelectionArea.getY(), 0);
        if (this.showingSelectionArea){
            drawRectangle(stack, this.selectionArea);
            drawRectangle(stack, this.categorySelectionArea);
            drawRectangle(stack, this.emojiInfoArea);
            for (int i = 0; i < 6; i++) {
                drawLine(stack, i * 12f, i + selectionPointer);
            }
            int progressY = (int) ((( this.emojiInfoArea.getY() - this.categorySelectionArea.getY() - 5) / ((double)getLineAmount())) * (selectionPointer)) ;
            drawRectangle(stack, new Rect2i(this.selectionArea.getX() + this.selectionArea.getWidth() - 2, this.categorySelectionArea.getY() + progressY, 1,5), 0xff525252);
            if (lastEmoji != null){
                Minecraft.getInstance().font.draw(stack, lastEmoji.strings.get(0), emojiInfoArea.getX() + 2, emojiInfoArea.getY() + 6, 0);
                StringBuilder builder = new StringBuilder();
                lastEmoji.strings.forEach(s -> builder.append(s).append(" "));
                float textScale = 0.5f;
                List<FormattedCharSequence> iTextPropertiesList = EmojiClientProxy.oldFontRenderer.split(FormattedText.of(builder.toString()), (int) ((emojiInfoArea.getWidth() - 18) *  (1/textScale)));
                float i = -iTextPropertiesList.size() / 2;
                stack.pushPose();
                stack.scale(textScale, textScale, textScale);
                for (FormattedCharSequence reorderingProcessor : iTextPropertiesList) {
                    StringBuilder stringBuilder = new StringBuilder();
                    reorderingProcessor.accept((p_accept_1_, p_accept_2_, ch) -> {
                        stringBuilder.append((char) ch);
                        return true;
                    });
                    EmojiClientProxy.oldFontRenderer.draw(stack, stringBuilder.toString(), (emojiInfoArea.getX() + 15) * (1/textScale), (emojiInfoArea.getY() + 8 + 4 * i)  * (1/textScale), 0x969696);
                    ++i;
                }
                stack.scale(1,1,1);
                stack.popPose();
            }
            progressY = (int) ((( this.categorySelectionArea.getHeight() - 10) / ((double)EmojiClientProxy.CATEGORIES.size() -6)) * (categoryPointer)) ;
            drawRectangle(stack, new Rect2i(this.categorySelectionArea.getX() + this.categorySelectionArea.getWidth() - 2, this.categorySelectionArea.getY() + progressY + 2, 1,5), 0xff525252);
            EmojiCategory firstCategory = getCategory(selectionPointer);
            for (int i = 0; i < EmojiClientProxy.CATEGORIES.size(); i++) {
                int selCategory = i + categoryPointer;
                if (selCategory < EmojiClientProxy.CATEGORIES.size()){
                    EmojiCategory category = EmojiClientProxy.CATEGORIES.get(selCategory);
                    Rect2i rec = new Rect2i(categorySelectionArea.getX() + 6, categorySelectionArea.getY() + 6 + i * 12, 11, 11);
                    if (category.equals(firstCategory)){
                        GuiComponent.fill(stack, rec.getX()-1, rec.getY()-2, rec.getX() + rec.getWidth(), rec.getY() + rec.getHeight() -1, 0x80000000);
                    }
                    if (rec.contains((int)lastMouseX, (int)lastMouseY) && Minecraft.getInstance().screen != null){
                        Minecraft.getInstance().screen.renderComponentTooltip(stack, Arrays.asList(MutableComponent.create(new LiteralContents((category.getChineseName())))),(int) lastMouseX,(int) lastMouseY);
                    }
                    if (EmojiClientProxy.SORTED_EMOJIS_FOR_SELECTION.containsKey(category) && EmojiClientProxy.SORTED_EMOJIS_FOR_SELECTION.get(category).size() > 0){
                        Minecraft.getInstance().font.draw(stack, EmojiClientProxy.SORTED_EMOJIS_FOR_SELECTION.get(category).get(0)[0].strings.get(0), categorySelectionArea.getX() + 6, categorySelectionArea.getY() + 6 + i * 12, 0);
                    }
                }
            }
           // fieldWidget.render(stack, (int)lastMouseX, (int)lastMouseY, 0);
        }
    }


    public boolean mouseClicked(double mouseX, double mouseY) {
        if (this.showingSelectionArea){
            /*if (textFieldRectangle.contains((int)mouseX, (int)mouseY)){
                fieldWidget.setFocus(true);
            } else {
                fieldWidget.setFocus(false);
            }*/
            if (categorySelectionArea.contains((int)mouseX, (int)mouseY)){
                for (int i = 0; i < 7; i++) {
                    int selCategory = i + categoryPointer;
                    if (selCategory < EmojiClientProxy.CATEGORIES.size()){
                        Rect2i rec = new Rect2i(categorySelectionArea.getX() + 6, categorySelectionArea.getY() + 6 + i * 12, 11, 11);
                        if (rec.contains((int)mouseX, (int)mouseY)){
                            EmojiCategory name = EmojiClientProxy.CATEGORIES.get(selCategory);
                            for (int i1 = 0; i1 < getLineAmount(); i1++) {
                                if (name.equals(getLineToDraw(i1))){
                                    this.selectionPointer = i1;
                                }
                            }
                        }
                    }
                }
                return true;
            }
            if (selectionArea.contains((int)mouseX, (int)mouseY)){
                for (int line = 0; line < 6; line++) {
                    Object object = getLineToDraw(line + selectionPointer);
                    if (object instanceof Emoji[]){
                        Emoji[] emojis = (Emoji[]) object;
                        for (int i = 0; i < emojis.length; i++) {
                            if (emojis[i] != null){
                                float x = (categorySelectionArea.getX() + categorySelectionArea.getWidth() + 2 + 12f * i);
                                float y = (categorySelectionArea.getY() + line * 12 + 2);//
                                Rect2i rec = new Rect2i((int) x, (int) y -1, 11, 11);
                                if (rec.contains((int)lastMouseX, (int)lastMouseY)){
                                    chatScreen.input.setValue(chatScreen.input.getValue() + emojis[i].getShorterString());
                                }
                            }
                        }
                    }
                }
                return true;
            }
        } else {
            if (openSelectionArea.contains((int)mouseX, (int)mouseY)){
                showSelectionArea();
                return true;
            }
        }
        return false;
    }


    public void mouseMoved(double mouseX, double mouseY) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
    }


    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (categorySelectionArea.contains((int)mouseX, (int)mouseY)){
            categoryPointer -= delta;
            categoryPointer = Mth.clamp(categoryPointer, 0, EmojiClientProxy.CATEGORIES.size() -6);
            return true;
        }
        if (selectionArea.contains((int)mouseX, (int)mouseY)){
            selectionPointer -= delta;
            selectionPointer = Mth.clamp(selectionPointer, 1, Math.max(1, getLineAmount() - 5));
            categoryPointer = Mth.clamp(Arrays.asList(EmojiClientProxy.CATEGORIES).indexOf(getCategory(selectionPointer)), 0, EmojiClientProxy.CATEGORIES.size() -6);
            return true;
        }
        return false;
    }


    public void drawRectangle(PoseStack stack, Rect2i rectangle2d){
        drawRectangle(stack, rectangle2d, Integer.MIN_VALUE);
    }

    public void drawRectangle(PoseStack stack, Rect2i rectangle2d, int value){
        GuiComponent.fill(stack, rectangle2d.getX(), rectangle2d.getY(), rectangle2d.getX() + rectangle2d.getWidth(), rectangle2d.getY() + rectangle2d.getHeight(), value);
    }

    public void showSelectionArea(){
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        showingSelectionArea = !showingSelectionArea;
    }


    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
//        if (fieldWidget.keyPressed(keyCode, scanCode, modifiers)){
//            updateFilter();
//            return true;
//        }
        return false;
    }


    public boolean charTyped(char c, int mod) {
        /*if (fieldWidget.charTyped(c, mod)){
            updateFilter();
            return true;
        }*/
        return false;
    }

    public void drawLine(PoseStack stack, float height, int line){
        Object lineToDraw = getLineToDraw(line);
        if (lineToDraw != null){
            if (lineToDraw instanceof EmojiCategory){
                float textScale = 1f;
                stack.scale(textScale, textScale, textScale);
                Minecraft.getInstance().font.draw(stack, ((EmojiCategory) lineToDraw).getName(), (categorySelectionArea.getX() + categorySelectionArea.getWidth() + 2) * (1/textScale), (categorySelectionArea.getY() + height + 2)* (1/textScale), 0x969696);
                stack.scale(1,1,1);
            } else {
                Emoji[] emojis = (Emoji[]) lineToDraw;
                for (int i = 0; i < emojis.length; i++) {
                    if (emojis[i] != null){
                        float x = (categorySelectionArea.getX() + categorySelectionArea.getWidth() + 2 + 12f * i);
                        float y = (categorySelectionArea.getY() + height + 2);//
                        Rect2i rec = new Rect2i((int) x, (int) y -1, 11, 11);
                        if (rec.contains((int)lastMouseX, (int)lastMouseY)){
                            lastEmoji = emojis[i];
                            GuiComponent.fill(stack, rec.getX()-1, rec.getY()-1, rec.getX() + rec.getWidth(), rec.getY() + rec.getHeight(), -2130706433);
                        }
                        Minecraft.getInstance().font.draw(stack, emojis[i].strings.get(0), x, y, 0x969696);
                    }
                }
            }
        }
    }

    public Object getLineToDraw(int line){
		for (EmojiCategory category : EmojiClientProxy.SORTED_EMOJIS_FOR_SELECTION.keySet()) {
			--line;
			if (line == 0) return category;
			for (Emoji[] emojis : EmojiClientProxy.SORTED_EMOJIS_FOR_SELECTION.get(category)) {
				--line;
				if (line == 0) return emojis;
			}
		}
        /*if (fieldWidget.getValue().isEmpty()){

        } else */{
            if (filteredEmojis.size() > line - 1 && line -1  >= 0){
                return filteredEmojis.get(line -1);
            }
        }
        return null;
    }

    public void updateFilter(){
        /*if (!fieldWidget.getValue().isEmpty()){
            selectionPointer = 1;
            filteredEmojis = new ArrayList<>();
            List<Emoji> emojis = Emojiful.EMOJI_LIST.stream().filter(emoji -> emoji.strings.stream().anyMatch(s -> s.toLowerCase().contains(fieldWidget.getValue().toLowerCase()))).collect(Collectors.toList());
            Emoji[] array = new Emoji[9];
            int i = 0;
            for (Emoji emoji : emojis) {
                array[i] = emoji;
                ++i;
                if (i >= array.length){
                    filteredEmojis.add(array);
                    array = new Emoji[9];
                    i = 0;
                }
            }
            if (i > 0){
                filteredEmojis.add(array);
            }
        }*/
    }

    public int getLineAmount(){
        return /*fieldWidget.getValue().isEmpty() ? */EmojiClientProxy.lineAmount /*: filteredEmojis.size()*/;
    }

    public EmojiCategory getCategory(int line){
        for (EmojiCategory category : EmojiClientProxy.SORTED_EMOJIS_FOR_SELECTION.keySet()) {
            --line;
            if (line == 0) return category;
            for (Emoji[] emojis : EmojiClientProxy.SORTED_EMOJIS_FOR_SELECTION.get(category)) {
                --line;
                if (line == 0) return category;
            }
        }
        return null;
    }

    public RdiChatScreen getChatScreen() {
        return chatScreen;
    }

   /* public EditBox getFieldWidget() {
        return fieldWidget;
    }*/
}
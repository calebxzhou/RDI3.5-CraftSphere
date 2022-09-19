package calebzhou.rdi.core.client.emoji;

import calebzhou.rdi.core.client.RdiCore;
import calebzhou.rdi.core.client.emoji.api.Emoji;
import calebzhou.rdi.core.client.util.ThreadPool;
import com.google.gson.JsonElement;
import calebzhou.rdi.core.client.emoji.api.EmojiCategory;
import calebzhou.rdi.core.client.emoji.api.EmojiFromTwitmoji;
import net.minecraft.client.gui.Font;

import java.util.*;
import java.util.stream.Collectors;

public class EmojiClientProxy {
    public static EmojiClientProxy INSTANCE = new EmojiClientProxy();

    public static Font oldFontRenderer ;
    public static List<String> ALL_EMOJIS = new ArrayList<>();
    public static HashMap<EmojiCategory, List<Emoji[]>> SORTED_EMOJIS_FOR_SELECTION = new LinkedHashMap<>();
    public static List<Emoji> EMOJI_WITH_TEXTS = new ArrayList<>();
    public static final List<EmojiCategory> CATEGORIES = new ArrayList<>();
    public static int lineAmount;

    /*public static EmojiSuggestionHelper emojiSuggestionHelper;
    public static EmojiSelectionGui emojiSelectionGui;*/

    public void init() {
		ThreadPool.newThread(()->{
			preInitEmojis();
			indexEmojis();
			RdiCore.LOGGER.info("Loaded " + Emojiful.EMOJI_LIST.size() + " emojis");

		});
    }
    private void indexEmojis(){
        ALL_EMOJIS = Emojiful.EMOJI_LIST.stream().map(emoji -> emoji.strings).flatMap(Collection::stream).collect(Collectors.toList());
        SORTED_EMOJIS_FOR_SELECTION = new LinkedHashMap<>();
        for (EmojiCategory category : CATEGORIES) {
            ++lineAmount;
            Emoji[] array = new Emoji[9];
            int i = 0;
            for (Emoji emoji : Emojiful.EMOJI_MAP.getOrDefault(category.getName(), new ArrayList<>())) {
                array[i] = emoji;
                ++i;
                if (i >= array.length){
                    SORTED_EMOJIS_FOR_SELECTION.computeIfAbsent(category, s -> new ArrayList<>()).add(array);
                    array = new Emoji[9];
                    i = 0;
                    ++lineAmount;
                }
            }
            if (i > 0){
                SORTED_EMOJIS_FOR_SELECTION.computeIfAbsent(category, s -> new ArrayList<>()).add(array);
                ++lineAmount;
            }
        }
    }


    /*public void render(ScreenEvent.DrawScreenEvent.Post event){
        if (emojiSuggestionHelper != null)
            emojiSuggestionHelper.render(event.getPoseStack());
        if (emojiSelectionGui != null){
            emojiSelectionGui.mouseMoved(event.getMouseX(), event.getMouseY());
            emojiSelectionGui.render(event.getPoseStack());
        }
    }


    public void onKeyPressed(ScreenEvent.KeyboardKeyPressedEvent event){
        if (emojiSuggestionHelper != null && emojiSuggestionHelper.keyPressed(event.getKeyCode(), event.getScanCode(), event.getModifiers()))
         event.setCanceled(true);
        if (emojiSelectionGui != null && emojiSelectionGui.keyPressed(event.getKeyCode(), event.getScanCode(), event.getModifiers())) event.setCanceled(true);
    }


    public void onClick(ScreenEvent.MouseClickedEvent.Pre event){
        if (emojiSelectionGui != null)
         emojiSelectionGui.mouseClicked(event.getMouseX(), event.getMouseY(), event.getButton());
    }


    public void onScroll(ScreenEvent.MouseScrollEvent.Pre event){
        if (emojiSelectionGui != null)
         emojiSelectionGui.mouseScrolled(event.getMouseX(), event.getMouseY(), event.getScrollDelta());
    }


    public void onClose(TickEvent.ClientTickEvent event){
        if (emojiSelectionGui != null && Minecraft.getInstance().screen != emojiSelectionGui.getChatScreen())
         emojiSelectionGui = null;
    }


    public void onCharTyped(ScreenEvent.KeyboardCharTypedEvent event){
        if (emojiSelectionGui != null && emojiSelectionGui.charTyped(event.getCodePoint(), event.getModifiers()))
         event.setCanceled(true);
    }




    public void onChatSend(ClientChatEvent event){
            String message = event.getMessage();
            for (Emoji emoji : EmojiClientProxy.EMOJI_WITH_TEXTS) {
                if (emoji.texts.size() > 0) message = message.replaceAll(emoji.getTextRegex(), emoji.getShorterString());
            }
            event.setMessage(message);
    }
*/
    /*
    public void onRecipesUpdated(RecipesUpdatedEvent event){
        CATEGORIES.removeIf(EmojiCategory::isWorldBased);
        Emojiful.EMOJI_LIST.removeIf(emoji -> emoji.worldBased);
        Emojiful.EMOJI_MAP.values().forEach(emojis -> emojis.removeIf(emoji -> emoji.worldBased));
        if (EmojifulConfig.getInstance().loadDatapack.get()){
            for (EmojiRecipe emojiRecipe : event.getRecipeManager().getAllRecipesFor(Emojiful.EMOJI_RECIPE_TYPE.get())) {
                EmojiFromGithub emoji = new EmojiFromGithub();
                emoji.name = emojiRecipe.getName();
                emoji.strings = new ArrayList<>();
                emoji.strings.add(":" + emojiRecipe.getName() + ":");
                emoji.location = emojiRecipe.getName();
                emoji.url = emojiRecipe.getUrl();
                emoji.worldBased = true;
                System.out.println(emoji.getUrl());
                Emojiful.EMOJI_MAP.computeIfAbsent(emojiRecipe.getCategory(), s -> new ArrayList<>()).add(emoji);
                Emojiful.EMOJI_LIST.add(emoji);
                if (CATEGORIES.stream().noneMatch(emojiCategory -> emojiCategory.getName().equalsIgnoreCase(emojiRecipe.getCategory()))){
                    CATEGORIES.add(0, new EmojiCategory(emojiRecipe.getCategory(), true));
                }
            }
            indexEmojis();
        }
    }
*/
    private void preInitEmojis() {
        CATEGORIES.addAll(Arrays.asList("Smileys & Emotion", "Animals & Nature", "Food & Drink", "Activities", "Travel & Places", "Objects", "Symbols", "Flags").stream().map(s -> new EmojiCategory(s, false)).collect(Collectors.toList()));
		try{
			for (JsonElement element : Emojiful.readJsonFromUrl("https://raw.githubusercontent.com/iamcal/emoji-data/master/emoji.json").getAsJsonArray()){
				if (element.getAsJsonObject().get("has_img_twitter").getAsBoolean()){
					EmojiFromTwitmoji emoji = new EmojiFromTwitmoji();
					emoji.name = element.getAsJsonObject().get("short_name").getAsString();
					emoji.location = element.getAsJsonObject().get("image").getAsString();
					emoji.sort =  element.getAsJsonObject().get("sort_order").getAsInt();
					element.getAsJsonObject().get("short_names").getAsJsonArray().forEach(jsonElement -> emoji.strings.add(":" + jsonElement.getAsString() + ":"));
					if (emoji.strings.contains(":face_with_symbols_on_mouth:")){
						emoji.strings.add(":swear:");
					}
					if (!element.getAsJsonObject().get("texts").isJsonNull()){
						element.getAsJsonObject().get("texts").getAsJsonArray().forEach(jsonElement -> emoji.texts.add(jsonElement.getAsString()));
					}
					Emojiful.EMOJI_MAP.computeIfAbsent(element.getAsJsonObject().get("category").getAsString(), s -> new ArrayList<>()).add(emoji);
					Emojiful.EMOJI_LIST.add(emoji);
					if (emoji.texts.size() > 0){
						EmojiClientProxy.EMOJI_WITH_TEXTS.add(emoji);
					}
				}
			}
			EmojiClientProxy.EMOJI_WITH_TEXTS.sort(Comparator.comparingInt(o -> o.sort));
			Emojiful.EMOJI_MAP.values().forEach(emojis -> emojis.sort(Comparator.comparingInt(o -> o.sort)));
		} catch (Exception e){
			Emojiful.error = true;
			RdiCore.LOGGER.catching(e);
		}
	}


}

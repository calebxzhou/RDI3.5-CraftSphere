package calebzhou.rdi.core.client.emoji.api;

public class EmojiCategory {

	private final String chineseName;
    private final String name;

    public EmojiCategory(String name, String chineseName) {
        this.name = name;
		this.chineseName = chineseName;
    }

    public String getName() {
        return name;
    }

	public String getChineseName() {
		return chineseName;
	}

}

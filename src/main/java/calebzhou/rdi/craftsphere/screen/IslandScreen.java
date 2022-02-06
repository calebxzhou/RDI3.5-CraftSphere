package calebzhou.rdi.craftsphere.screen;

import calebzhou.rdi.craftsphere.util.RandomUtils;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.PresetsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.option.*;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.GeneratorType;
import net.minecraft.resource.DataPackSettings;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.level.LevelInfo;

import java.util.function.Consumer;

public class IslandScreen extends BasicScreen {
    public IslandScreen(Screen titleScreen) {
        super("请选择操作：",titleScreen,true,false);
    }

    @Override
    protected void init() {
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 155, this.height / 6 + 48 - 6, 150, 20, new LiteralText("创建新的空岛"), (button) -> {
            handleCreate();
        }));
        this.addDrawableChild(new ButtonWidget(this.width / 2 + 5, this.height / 6 + 48 - 6, 150, 20, new TranslatableText("加入朋友的空岛"), (button) -> {
        this.client.setScreen(new JoinIslandScreen(this));
        }));

    }

    private void handleCreate() {
        String name = MinecraftClient.getInstance().getSession().getUsername();
        String iid = RandomUtils.getRandomIslandId();
        String worldName = "island-"+name+"-"+iid;
        String superflatPreset = "minecraft:air;minecraft:plains";
        /*DataPackSettings dataPackSettings =  new DataPackSettings(ImmutableList.of("vanilla"), ImmutableList.of());
        LevelInfo levelInfo = new LevelInfo(worldName, GameMode.SURVIVAL,false, Difficulty.HARD,false,new GameRules(),dataPackSettings);
        FlatChunkGeneratorConfig flatChunkGeneratorConfig = PresetsScreen.parsePresetString(registry, superflatPreset, this.config);

        GeneratorType generatorType = GeneratorType.fromGeneratorOptions(GeneratorOptions.createGenerator());*/
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

    }
}

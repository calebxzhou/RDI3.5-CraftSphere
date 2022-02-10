package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.RdiConfigure;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.resource.language.LanguageDefinition;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Mixin(LanguageManager.class)
public class MixinAncientChineseSupport {
    @Shadow @Final @Mutable public static String DEFAULT_LANGUAGE_CODE = RdiConfigure.getConfig().setDefaultLanguageToChinese?"zh_cn":"en_us";
    @Shadow @Mutable
    private static LanguageDefinition ENGLISH_US = RdiConfigure.getConfig().setDefaultLanguageToChinese?new LanguageDefinition("zh_cn", "中国", "简体中文", false): new LanguageDefinition("en_us", "US", "English", false);;

    @ModifyConstant(
            method = "Lnet/minecraft/client/resource/language/LanguageManager;<init>(Ljava/lang/String;)V",
            constant = @Constant(
                    stringValue = "en_us"
            )
    )
    private static String chn1(String constant){
        return RdiConfigure.getConfig().setDefaultLanguageToChinese?"zh_cn":"en_us";
    }
    @ModifyConstant(
            method = "Lnet/minecraft/client/resource/language/LanguageManager;reload(Lnet/minecraft/resource/ResourceManager;)V",
            constant = @Constant(
                    stringValue = "en_us"
            )
    )
    private static String chn2(String c){
        return RdiConfigure.getConfig().setDefaultLanguageToChinese?"zh_cn":"en_us";
    }

}
@Mixin(Language.class)
class MixinAncientChn2{
    @Shadow @Final @Mutable
    public static String DEFAULT_LANGUAGE = "zh_cn";
    @ModifyConstant(
            method = "Lnet/minecraft/util/Language;create()Lnet/minecraft/util/Language;",
            constant = @Constant(stringValue = "/assets/minecraft/lang/en_us.json",ordinal = 0)
    )
    private static String cn(String constant){
        return RdiConfigure.getConfig().setDefaultLanguageToChinese?"/assets/minecraft/lang/zh_cn.json":"/assets/minecraft/lang/en_us.json";
    }
    @ModifyConstant(
            method = "Lnet/minecraft/util/Language;create()Lnet/minecraft/util/Language;",
            constant = @Constant(stringValue = "/assets/minecraft/lang/en_us.json",ordinal = 1)
    )
    private static String cn1(String constant){
        return RdiConfigure.getConfig().setDefaultLanguageToChinese?"/assets/minecraft/lang/zh_cn.json":"/assets/minecraft/lang/en_us.json";
    }
    @ModifyConstant(
            method = "Lnet/minecraft/util/Language;create()Lnet/minecraft/util/Language;",
            constant = @Constant(stringValue = "/assets/minecraft/lang/en_us.json",ordinal = 2)
    )
    private static String cn2(String constant){
        return RdiConfigure.getConfig().setDefaultLanguageToChinese?"/assets/minecraft/lang/zh_cn.json":"/assets/minecraft/lang/en_us.json";
    }
}


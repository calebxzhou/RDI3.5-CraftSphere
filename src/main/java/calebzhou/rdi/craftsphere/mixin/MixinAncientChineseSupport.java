package calebzhou.rdi.craftsphere.mixin;

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
    @Shadow @Final @Mutable public static String DEFAULT_LANGUAGE_CODE = "zh_cn";
    @Shadow @Mutable
    private static LanguageDefinition ENGLISH_US =
            new LanguageDefinition("zh_cn", "中国", "简体中文", false);

    @ModifyConstant(
            method = "Lnet/minecraft/client/resource/language/LanguageManager;<init>(Ljava/lang/String;)V",
            constant = @Constant(
                    stringValue = "en_us"
            )
    )
    private static String chn1(String constant){
        return "zh_cn";
    }
    @ModifyConstant(
            method = "Lnet/minecraft/client/resource/language/LanguageManager;reload(Lnet/minecraft/resource/ResourceManager;)V",
            constant = @Constant(
                    stringValue = "en_us"
            )
    )
    private static String chn2(String c){
        return "zh_cn";
    }
    /*@Redirect(
            method = "Lnet/minecraft/client/resource/language/LanguageManager;reload(Lnet/minecraft/resource/ResourceManager;)V",
            at=@At(value = "INVOKE",
            target = "Ljava/util/Map;getOrDefault(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
            ordinal = 0)
    )
    private Object reloadIfAncientChn(Map languageDefs, Object currentLanguageCode, Object languageDefinition){
        System.out.println("当前语言代码"+currentLanguageCode);
        String code = "lzh";//文言文
        if(currentLanguageCode.equals(code)){
            ENGLISH_US=CHN;
            return CHN;
        }else{
            return languageDefs.getOrDefault(currentLanguageCode,languageDefinition);
        }
    }*/
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
        return "/assets/minecraft/lang/zh_cn.json";
    }
    @ModifyConstant(
            method = "Lnet/minecraft/util/Language;create()Lnet/minecraft/util/Language;",
            constant = @Constant(stringValue = "/assets/minecraft/lang/en_us.json",ordinal = 1)
    )
    private static String cn1(String constant){
        return "/assets/minecraft/lang/zh_cn.json";
    }
    @ModifyConstant(
            method = "Lnet/minecraft/util/Language;create()Lnet/minecraft/util/Language;",
            constant = @Constant(stringValue = "/assets/minecraft/lang/en_us.json",ordinal = 2)
    )
    private static String cn2(String constant){
        return "/assets/minecraft/lang/zh_cn.json";
    }
}
@Mixin(GameOptions.class)
class MixinAncientChn3{
    @Shadow @Mutable
    public String language = "zh_cn";
}

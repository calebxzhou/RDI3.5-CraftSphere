package calebzhou.rdi.craftsphere.util;

import static calebzhou.rdi.craftsphere.ExampleMod.MODID;

import net.minecraft.resources.ResourceLocation;

public class IdentifierUtils {
    public static ResourceLocation byClass(Class clazz){
        String name = clazz.getSimpleName().toLowerCase();
        return new ResourceLocation(MODID,name);
    }
}

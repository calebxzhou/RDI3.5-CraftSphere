package calebzhou.rdi.craftsphere.util;

import net.minecraft.resources.ResourceLocation;

import static calebzhou.rdi.craftsphere.ExampleMod.MODID;

public class IdentifierUtils {
    public static ResourceLocation byClass(Class clazz){
        String name = clazz.getSimpleName().toLowerCase();
        return new ResourceLocation(MODID,name);
    }
}

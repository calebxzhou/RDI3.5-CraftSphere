package calebzhou.rdi.craftsphere.util;

import net.minecraft.util.Identifier;

import static calebzhou.rdi.craftsphere.ExampleMod.MODID;

public class IdentifierUtils {
    public static Identifier byClass(Class clazz){
        String name = clazz.getSimpleName().toLowerCase();
        return new Identifier(MODID,name);
    }
}

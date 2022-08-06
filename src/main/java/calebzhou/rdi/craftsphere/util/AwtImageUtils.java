package calebzhou.rdi.craftsphere.util;

import calebzhou.rdi.craftsphere.ExampleMod;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class AwtImageUtils {
    public static Image createImage(String path, String description) {
        URL imageURL = ExampleMod.class.getResource(path);

        if (imageURL == null) {
            System.err.println("Resource not found: " + path);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }
}

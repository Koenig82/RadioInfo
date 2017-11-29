package Model;

import javax.swing.*;
import java.awt.*;

/**
 * This class handles images for the program and resizes them to fit in the
 * information panel.
 */
public class ImageScaler {

    /**
     * A static method to be used from anywhere by any method to resize an
     * ImageIcon.
     *
     * @param icon ImageIcon to resize
     * @return ImageIcon , Resized icon.
     */
    public static synchronized ImageIcon scaleImage(ImageIcon icon) {

        int width = icon.getIconWidth();
        int height = icon.getIconHeight();

        if(icon.getIconWidth() > 100) {
            width = 100;
            height = (width * icon.getIconHeight()) / icon.getIconWidth();
        }

        if(height > 100) {
            height = 100;
            width = (icon.getIconWidth() * height) / icon.getIconHeight();
        }

        return new ImageIcon(icon.getImage().getScaledInstance(width, height,
                Image.SCALE_DEFAULT));
    }
}
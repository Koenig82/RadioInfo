package Model;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * This class handles images for the program and resizes them to fit in the
 * information panel.
 */
public class ImageHandler {

    public static ImageIcon notAvailable;

    public ImageHandler() throws Exception{
        BufferedImage temp = null;
        try {
            temp = ImageIO.read(new File("src/images/notAvailable.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(temp != null){
            notAvailable = new ImageIcon(temp);
            notAvailable = ImageHandler.scaleImage(notAvailable);
        }else{
            throw new Exception("Unable to load default image");
        }
    }
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

    /**
     * This method converts a BufferedImage to an ImageIcon. It uses a
     * static method from the ImageHandler class.
     *
     * @param image BufferedImage to convert
     * @return ImageIcon , The converted image(standard image if unavailable)
     */
    public static synchronized ImageIcon convertToIcon(BufferedImage image){
        ImageIcon icon;
        if(image!=null){
            icon = new ImageIcon(image);
            icon = scaleImage(icon);
            return icon;
        }else{
            return notAvailable;
        }

    }
}
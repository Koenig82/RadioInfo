package Model;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;

/**
 * Domain class for a program. The program implements comparable to to be able
 * to sort programs based on their starting time.
 */
public class Program implements Comparable{
    public String name;
    public String description = "";
    public LocalDateTime start;
    public LocalDateTime end;
    public ImageIcon image;
    public String imageUrl;

    public Program(String name, LocalDateTime start, LocalDateTime end){
        this.name = name;

        this.start = start;
        this.end = end;
    }

    @Override
    public int compareTo(Object o) {
        return this.start.compareTo(((Program) o).start);
    }

    public void getProgramImage() throws Exception{
        BufferedImage image;
        try{
            URL url = new URL(imageUrl);
            image = ImageIO.read(url);
            this.image = ImageHandler.convertToIcon(image);
        } catch (MalformedURLException e) {
            System.err.println("Problem getting image for program " + this.name);
            this.image = ImageHandler.notAvailable;
        } catch (IOException e) {
            System.err.println("Problem reading program image");
            this.image = ImageHandler.notAvailable;
        }
    }
}

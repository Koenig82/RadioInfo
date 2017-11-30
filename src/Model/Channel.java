package Model;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.TreeSet;

/**
 * Domain class for the channel.
 */
public class Channel{

    public ImageIcon image;
    public String imageUrl;
    public String name;
    public String description = "";

    public TreeSet<Program> programs;
    public int id;

    public Channel(String name, int id){
        this.name = name;
        this.id = id;
        programs = new TreeSet<>();
    }

    /**
     * This method is used by collectChannels() to assign an image to a
     * channel from its image url.
     *
     * @throws IOException , Exception thrown when image could not be loaded.
     * @throws MalformedURLException , Exeption thrown when url is malformed
     */
    public void getChannelImage()throws Exception{
        BufferedImage image;
        try{
            URL url = new URL(imageUrl);
            image = ImageIO.read(url);
            this.image = ImageHandler.convertToIcon(image);
        } catch (MalformedURLException e) {
            System.err.println("Problem getting image for channel " + this.name);
            this.image = ImageHandler.notAvailable;
        } catch (IOException e) {
            System.err.println("Problem reading channel image");
            this.image = ImageHandler.notAvailable;
        }
    }

}
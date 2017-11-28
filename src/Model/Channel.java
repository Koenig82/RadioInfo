package Model;

import javax.swing.*;
import java.util.TreeSet;

/**
 * Domain class for the channel.
 */
public class Channel{
    public ImageIcon image;
    public String name;
    public String schedule;
    public String description = "";

    public TreeSet<Program> programs;
    public int id;

    public Channel(String name, int id, String schedule){
        this.name = name;
        this.id = id;
        this.schedule = schedule;
        programs = new TreeSet<>();
    }

}
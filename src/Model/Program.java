package Model;

import javax.swing.*;
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

    public Program(String name, LocalDateTime start, LocalDateTime end){
        this.name = name;

        this.start = start;
        this.end = end;
    }

    @Override
    public int compareTo(Object o) {
        return this.start.compareTo(((Program) o).start);
    }
}

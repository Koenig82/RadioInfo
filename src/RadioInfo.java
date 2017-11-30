import Controller.Controller;
import Controller.AutoUpdate;
import Model.ImageHandler;
import Model.XMLParser;
import View.GUI;
import javax.swing.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Main class of RadioInfo. The main method starts up the program
 * by initializing a gui and controller object with an XMLParser. The
 * automatic update object AutoUpdate is also scheduled to execute at an
 * interval.
 *
 * The gui starts up on Swing event dispatch thread.
 */
public class RadioInfo {

    public static void main(String[] args) throws Exception {

        ImageHandler imageHandler = new ImageHandler();
        XMLParser parser = new XMLParser();
        parser.parseStream("http://api.sr.se/api/v2/channels");
        parser.channelData = parser.collectChannels();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                GUI gui = new GUI(parser.channelData);
                Controller controller = new Controller(gui, parser);
                ScheduledExecutorService upDate = Executors
                        .newSingleThreadScheduledExecutor();
                upDate.scheduleAtFixedRate(new AutoUpdate(parser, gui)
                        , 0,3600, TimeUnit.SECONDS);
            }
        });
    }
}
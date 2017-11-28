package Controller;

import Model.XMLParser;
import View.GUI;

import javax.swing.*;

/**
 * A class that handles the auto updating feature of Radioinfo. It should be
 * called at desired interval with a proper method (ScheduledExecutorService).
 *
 * The run method simply updates the schedule.
 */
public class AutoUpdate implements Runnable {

    private XMLParser parser;
    private GUI gui;

    public AutoUpdate(XMLParser parser, GUI gui){
        this.parser = parser;
        this.gui = gui;
    }
    public void run(){

        if(parser.activeChannelId != 0){
            SwingWorker<Void,Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {

                    parser.updateSchedule(parser.channelData.get(parser
                            .activeChannelId));
                    return null;
                }

                @Override
                protected void done() {
                    gui.updateTable(parser.channelData.get(parser
                            .activeChannelId));
                }
            };

            worker.execute();
        }
    }
}

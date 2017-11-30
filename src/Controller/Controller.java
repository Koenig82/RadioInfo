package Controller;

import Model.XMLParser;
import View.GUI;
import View.JButtonInt;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The main controller class. It starts upp all the programs listeners
 * and associated actions. When buttons are pressed by the user, a swing worker
 * performs the actions.
 *
 * Each channel button is assigned an action listener and the program table
 * is assigned a mouse listener(TableListener) that detects mouse clicks in
 * each cell.
 */
public class Controller {

    GUI gui;
    XMLParser parser;

    public Controller(GUI gui, XMLParser parser){

        this.gui = gui;
        this.parser = parser;

        gui.table.addMouseListener(new TableListener(this));

        for (JButtonInt j : gui.channelSelect) {
            j.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    SwingWorker<Void,Void> worker = new SwingWorker<
                            Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {

                            parser.updateSchedule(parser.channelData.get(
                                    j.channelMapValue));
                            parser.channelData.get(j.channelMapValue).getChannelImage();
                            return null;
                        }

                        @Override
                        protected void done() {
                            gui.updateTable( parser.channelData.get(
                                    j.channelMapValue));
                        }
                    };
                    worker.execute();
                }
            });
        }
    }
}

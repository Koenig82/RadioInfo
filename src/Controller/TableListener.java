package Controller;

import Model.Program;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;

/**
 * This class implements the interface MouseListener and updates the
 * program information panel when it detects a mouse click on a program cell
 * in the program table.
 */
public class TableListener implements MouseListener{
    Controller controller;

    public TableListener(Controller controller){
        this.controller = controller;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

        JTable target = (JTable)e.getSource();

        SwingWorker<Program,Void> worker = new SwingWorker<Program, Void>() {
            @Override
            protected Program doInBackground() throws Exception {

                LocalDateTime dt = (LocalDateTime)((JTable) e.getSource())
                        .getValueAt(target.getSelectedRow(), 1);
                Program prog = controller.parser.getProgramByChannelIdAndStart
                        (controller.parser.activeChannelId, dt);
                prog.getProgramImage();
                return prog;
            }



            @Override
            protected void done() {
                Program p = null;
                try {
                    p = get();
                } catch (InterruptedException e1) {
                    System.err.println("System was interrupted");
                    e1.printStackTrace();
                } catch (ExecutionException e1) {
                    System.err.println("System execution error");
                    e1.printStackTrace();
                }
                controller.gui.upDateProgramView(p);
            }
        };
        worker.execute();
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}

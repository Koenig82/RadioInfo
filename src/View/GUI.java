package View;

import Model.Channel;
import Model.ImageScaler;
import Model.Program;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class handles gui methods, such as assembling the gui and updating
 * its graphics.
 */
public class GUI extends JFrame{


    private JPanel panelLeft;
    private JPanel panelRight;
    private JPanel upperLeft;
    private JPanel lowerLeft;
    private JPanel lowerLeftUp;

    private JLabel pictureArea;
    private JPanel buttonArea;
    private JButton refresh;

    public JPanel channels;

    public JTable table;
    public DefaultTableModel tableModel;

    private JTextArea info;

    private JMenuBar menuBar;
    private JMenu menu;
    private JMenuItem menuItem;

    private static final int FRAME_WIDTH = 1000;
    private static final int FRAME_HEIGHT = 500;

    //private ChannelData data;

    public ArrayList<JMenuItem> channelSelects;
    public ArrayList<JButtonInt> channelSelect;

    /**
     * Contructor method for the gui.
     *
     * @param data LinkedHashMap, The channeldata from the XMLParser object.
     */
    public GUI(HashMap<Integer, Channel> data){

        this.channelSelects = new ArrayList<>();
        this.channelSelect = new ArrayList<>();
        this.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        this.setLayout(new BorderLayout());

        //Right and left panel
        panelLeft = new JPanel();
        panelRight = new JPanel();
        panelRight.setBorder(BorderFactory.createTitledBorder("Program " +
                "schedule"));
        panelLeft.setLayout(new BorderLayout());
        panelRight.setLayout(new BorderLayout());
        this.add(panelLeft, BorderLayout.WEST);
        this.add(panelRight, BorderLayout.EAST);

        //upper left
        upperLeft = new JPanel();
        upperLeft.setLayout(new BorderLayout());
        upperLeft.setBorder(BorderFactory.createTitledBorder
                ("Radio stations"));

        //Lower left
        lowerLeft = new JPanel();
        lowerLeft.setLayout(new BorderLayout());

        panelLeft.add(lowerLeft, BorderLayout.SOUTH);
        panelLeft.add(upperLeft, BorderLayout.NORTH);

        //Lower left, upper
        lowerLeftUp = new JPanel();
        lowerLeftUp.setLayout(new FlowLayout(FlowLayout.CENTER,100,0));
        lowerLeft.setBorder(BorderFactory.createTitledBorder
                ("Channel & Program information"));

        lowerLeft.add(lowerLeftUp, BorderLayout.NORTH);

        //Lower left, lower
        JPanel lowerLeftBottom = new JPanel();

        lowerLeft.add(lowerLeftBottom, BorderLayout.SOUTH);
        refresh = new JButton("Refresh =>");

        pictureArea = new JLabel();
        pictureArea.setPreferredSize(new Dimension(50,50));

        try {
            BufferedImage img = ImageIO.read(new File("src/images/" +
                    "notAvaliable.jpg"));
            ImageIcon icon = new ImageIcon(img);
            ImageIcon newIcon = ImageScaler.scaleImage(icon);
            pictureArea = new JLabel(newIcon);
        } catch (IOException e) {
            e.printStackTrace();
        }

        lowerLeftUp.add(pictureArea);
        lowerLeftUp.add(refresh);

        info = new JTextArea();

        JScrollPane infoScroll = new JScrollPane(info);
        infoScroll.setPreferredSize(new Dimension(450,
                50));
        lowerLeftBottom.add(infoScroll, BorderLayout.CENTER);

        //adding channel list to upper left
        channels = new JPanel();
        channels.setLayout(new GridLayout(data.size(), 1));

        JScrollPane channelScroll = new JScrollPane(channels);
        channelScroll.setPreferredSize(new Dimension(FRAME_WIDTH/2-5,
                FRAME_HEIGHT/2-20));

        for(Map.Entry<Integer, Channel> c : data.entrySet()) {
            JButtonInt button = new JButtonInt(c.getValue().id);
            button.setText(c.getValue().name);
            button.setPreferredSize(new Dimension(20, 40));

            channelSelect.add(button);
            channels.add(button);
        }
        channelScroll.setVerticalScrollBarPolicy(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        upperLeft.add(channelScroll, BorderLayout.NORTH);

        //right side, program view
        String colNames[] = {"Program", "Start", "Slut"};
        Object[][] rows = {};
        tableModel = new DefaultTableModel(rows ,colNames);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        panelRight.add(scrollPane);
        table.setFillsViewportHeight(true);


        menuBar = createMenuBar();
        this.setJMenuBar(menuBar);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    /**
     * Creates the menubar
     *
     * @return JMEnuBar, the menubar.
     */
    public JMenuBar createMenuBar(){
        //Create the menu bar.
        menuBar = new JMenuBar();

        //Build the first menu.
        menu = new JMenu("RadioInfo");
        menu.setMnemonic(KeyEvent.VK_A);
        menu.getAccessibleContext().setAccessibleDescription(
                "The only menu in this program that has menu items");
        menuBar.add(menu);

        //a group of JMenuItems
        menuItem = new JMenuItem("Info",
                KeyEvent.VK_T);

        menuItem.getAccessibleContext().setAccessibleDescription("");
        menu.add(menuItem);

        menuItem = new JMenuItem("Exit");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_1, ActionEvent.ALT_MASK));
        menuItem.setMnemonic(KeyEvent.VK_B);
        menu.add(menuItem);

        return menuBar;
    }

    /**
     * This method updates the program table with information from a
     * Channel object.
     *
     * @param channel Channel, The Channel object to update program info from.
     */
    public void updateTable(Channel channel){
        //invokelater
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                tableModel.setRowCount(0);
                for(Program p : channel.programs){
                    tableModel.addRow(new Object[]{p.name, p.start, p.end});
                    renderRedCells(table);

                }
                pictureArea.setIcon(channel.image);
                info.setText(channel.description);
            }
        });
    }

    /**
     * This method renders old radio program cells red from updateTable method.
     *
     * @param table
     */
    private void renderRedCells(JTable table){
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            @Override
            public Component getTableCellRendererComponent(JTable table,
                                                           Object value,
                                                           boolean isSelected,
                                                           boolean hasFocus,
                                                           int row,
                                                           int col) {


                super.getTableCellRendererComponent(table, value, isSelected,
                        hasFocus, row, col);

                LocalDateTime endTime = (LocalDateTime) table.getModel()
                        .getValueAt(row, 2);
                if (endTime.isBefore(LocalDateTime.now())) {
                    setBackground(Color.RED);
                    setForeground(Color.BLACK);
                } else {
                    setBackground(table.getBackground());
                    setForeground(table.getForeground());
                }
                return this;
            }
        });
    }

    /**
     * This method updates the channel and program information panel
     * @param prog Program, The Program object to update info from.
     */
    public void upDateProgramView(Program prog){
        info.setText(prog.description);
        pictureArea.setIcon(prog.image);
    }
}
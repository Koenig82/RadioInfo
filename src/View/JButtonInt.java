package View;

import javax.swing.*;

/**
 * A modification of swing JButton class that also connects an identifying
 * integer to the button.
 */
public class JButtonInt extends JButton {

    public int channelMapValue;

    JButtonInt(int channelMapValue){
        super();
        this.channelMapValue=channelMapValue;
    }
}

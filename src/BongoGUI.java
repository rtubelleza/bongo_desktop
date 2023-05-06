import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class BongoGUI extends JFrame {
    private Theme theme;
    private BongoPanel bongoPanel;

    public BongoGUI() {
        // Default Init. Attributes.
        setTitle("Bongo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setAlwaysOnTop(true);
        setSize(500, 300);
        setLocationRelativeTo(null);
        setTheme(Theme.LIGHT); // Sets background as well

        // Position window in screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;
        Rectangle winSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int taskBarHeight = screenHeight - winSize.height;
        BongoGUI.setFrameCorner(this, screenWidth, screenHeight, taskBarHeight, ScreenCorner.BOTTOM_RIGHT); // Bottom right by default

        // Add Panel
        bongoPanel = new BongoPanel(getSize(), "/assets", this.theme, Board.KEYBOARD); // Populate entire frame
        add(bongoPanel);
        setVisible(true);
    }

    public static void setFrameCorner(Component component, int parentWidth, int parentHeight, int taskBarHeight, ScreenCorner corner)
            throws IllegalArgumentException {
        int x = 0, y = 0;
        int fWidth = component.getWidth();
        int fHeight = component.getHeight();
        switch (corner) {
            case TOP_LEFT:
                break;
            case BOTTOM_LEFT:
                y = parentHeight - fHeight - taskBarHeight; // Bottom border of the screen
                break;
            case TOP_RIGHT:
                x = parentWidth - fWidth;
                break;
            case BOTTOM_RIGHT: // By default, BOTTOM_RIGHT
                x = parentWidth - fWidth;
                y = parentHeight - fHeight - taskBarHeight;
                break;
        }
        component.setLocation(x, y);
    }

    private void setTheme(Theme theme) {
        this.theme = theme;
        setBackground(theme);
    }

    private void setBackground(Theme theme) {
        if (this.theme == Theme.DARK) {
            getContentPane().setBackground(Color.BLACK);
        } else {
            getContentPane().setBackground(Color.WHITE);
        }
    }

    public static void main(String[] args) {
        new BongoGUI();
    }

}

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class BongoPanel extends JPanel implements NativeKeyListener {
    private Theme theme;
    private Map<CatComponent, BufferedImage> baseCatAssets = new HashMap<>();
    private BufferedImage boardOnTable;
    private State leftPawState = State.RELEASE;
    private State rightPawState = State.RELEASE;
    private State mouthState = State.RELEASE;
    private Robot KeyExecutor;
    private int deletedCharLength = 0;
    private String os = System.getProperty("os.name").toLowerCase();

    public BongoPanel(Dimension parentSize, String assetDirectory, Theme theme, Board board) {
        setPreferredSize(parentSize);
        setOpaque(false);
        loadCatAssets(assetDirectory, theme);
        loadBoardAssets(assetDirectory, board);

        // Global Keyboard Listener
        try {
            GlobalScreen.registerNativeHook();
        } catch (Exception e) {
            System.out.println("Squash");
        }
        GlobalScreen.addNativeKeyListener(this);

        // Robot
        try {
            KeyExecutor = new Robot();
        } catch (AWTException awe) {
            System.out.println("Low level input control not granted.");
        }

        setFocusable(true);
        setVisible(true);

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        // CatHead
        g2d.drawImage(baseCatAssets.get(CatComponent.HEAD), -200, 0, this);

        // Table line
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(5));
        if (theme == Theme.DARK) {
            g2d.setColor(Color.WHITE);
        } else {
            g2d.setColor(Color.BLACK);
        }
        g2d.drawLine(0, 94, 500, 212);

        // Board
        g2d.drawImage(boardOnTable, -160, -25, this);

        // CatMouth
        g2d.drawImage(getComponentByState(baseCatAssets.get(CatComponent.MOUTH), mouthState), -200, 0, this);
        // RightPaw
        g2d.drawImage(getComponentByState(baseCatAssets.get(CatComponent.RIGHT_PAW), rightPawState), -200, 0, this);
        // LeftPaw
        g2d.drawImage(getComponentByState(baseCatAssets.get(CatComponent.LEFT_PAW), leftPawState), -200, 0, this);
    }

    private void loadCatAssets(String assetDirectoryPath, Theme theme) {
        // Load components by theme
        Map<CatComponent, BufferedImage> baseComponents = getCatComponents(assetDirectoryPath);
        for (CatComponent component : CatComponent.values()) {
            this.baseCatAssets.put(component, getComponentByTheme(baseComponents.get(component), theme));
        }
    }

    private void loadBoardAssets(String assetDirectoryPath, Board board) {
        // Load board by board
        boardOnTable = readImage(assetDirectoryPath + "/" + board.toFileString());
    }

    public void nativeKeyPressed(NativeKeyEvent e) {
        keyStateBinder(e, State.PRESS);
        // Layer volume control with ctrlz/backspace functionality
        keyRemapper(e);
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
        keyStateBinder(e, State.RELEASE);
    }

    private void keyStateBinder(NativeKeyEvent e, State state) {
        if (e.getKeyCode() == NativeKeyEvent.VC_SPACE) {
            mouthState = state; // toggle the flag
            repaint(); // redraw the panel to show the new image
        } else if (checkLeftKeyCodes(e.getKeyCode()) ||
                e.getKeyLocation() == NativeKeyEvent.KEY_LOCATION_LEFT) {
            leftPawState = state;
            repaint();
        } else {
            rightPawState = state;
            repaint();
        }
    }

    private void keyRemapper(NativeKeyEvent e) {
        int eventKeyCode = e.getKeyCode();
        if (checkVolumeCodes(eventKeyCode)) {
            Knob knobEvent = getVolumeMap(eventKeyCode);
            if (knobEvent == Knob.CLOCKWISE) {
                KeyExecutor.keyPress(KeyEvent.VK_CONTROL);
                KeyExecutor.keyPress(KeyEvent.VK_Z);
                KeyExecutor.keyRelease(KeyEvent.VK_CONTROL);
                KeyExecutor.keyRelease(KeyEvent.VK_Z);
            } else if (knobEvent == Knob.ANTI_CLOCKWISE) {
                KeyExecutor.keyPress(KeyEvent.VK_BACK_SPACE);
                KeyExecutor.keyRelease(KeyEvent.VK_BACK_SPACE);
                deletedCharLength++;
            } else {
                if (os.contains("windows")) {
                    KeyExecutor.keyPress(KeyEvent.VK_WINDOWS);
                    KeyExecutor.keyPress(KeyEvent.VK_SHIFT);
                    KeyExecutor.keyPress(KeyEvent.VK_S);
                    KeyExecutor.keyRelease(KeyEvent.VK_WINDOWS);
                    KeyExecutor.keyRelease(KeyEvent.VK_SHIFT);
                    KeyExecutor.keyRelease(KeyEvent.VK_S);
                } else if (os.contains("mac")) {
                    KeyExecutor.keyPress(KeyEvent.VK_META); // Add delay after maybe delay(200)
                    KeyExecutor.keyPress(KeyEvent.VK_SHIFT);
                    KeyExecutor.keyPress(KeyEvent.VK_4);
                    KeyExecutor.keyRelease(KeyEvent.VK_META); // Add delay after maybe delay(200)
                    KeyExecutor.keyRelease(KeyEvent.VK_SHIFT);
                    KeyExecutor.keyRelease(KeyEvent.VK_4);
                } else {
                    System.out.println("Non supported screenshot os");
                }
            }
        }
    }
    private char getDeletedChar() {
        // read the text that was selected before the BACK_SPACE key event
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);
        if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                String text = (String) contents.getTransferData(DataFlavor.stringFlavor);
                int length = text.length();
                if (length > 0) {
                    return text.charAt(length - 1); // return the last character of the selected text
                }
            } catch (IOException | UnsupportedFlavorException ex) {
                ex.printStackTrace();
            }
        }
        return '\0';
    }
    private boolean checkLeftKeyCodes(int keyCode) {
        if (keyCode >= 58 && keyCode <= 64) { // Check capslock + Row0
            return true;
        } else if (keyCode >= 1 && keyCode <= 7) { // Check escape + Row1
            return true;
        } else if (keyCode >= 15 && keyCode <= 21) { // Check Row2
            return true;
        } else if (keyCode >= 30 && keyCode <= 35) { // Check Row3
            return true;
        } else if (keyCode >= 42 && keyCode <= 48) { // Check Row4
            return true;
        } else {
            return false;
        }
    }

    private boolean checkVolumeCodes(int keyCode) {
        return keyCode == 57392 || keyCode == 57390 || keyCode == 57376;
    }

    private Knob getVolumeMap(int keyCode) {
        if (keyCode == 57392) {
            return Knob.CLOCKWISE;
        } else if (keyCode == 57390) {
            return Knob.ANTI_CLOCKWISE;
        } else {
            return Knob.PRESS;
        }
    }

    private BufferedImage readImage(String file) {
        // Gets the png image file
        BufferedImage image = null;
        try (InputStream inputStream = getClass().getResourceAsStream(file)) {
            assert inputStream != null;
            image = ImageIO.read(inputStream);
        } catch (IOException ioe) {
            System.out.println("Error occurred during reading.");
        }
        return image;
    }
    private Map<CatComponent, BufferedImage> getCatComponents(String assetDirectoryPath) {
        Map<CatComponent, BufferedImage> catMap = new HashMap<CatComponent, BufferedImage>();

        for (CatComponent component : CatComponent.values()) {
            catMap.put(component, readImage(assetDirectoryPath + "/" +component.toFileString()));
        }
        return catMap;
    }

    private BufferedImage getComponentByTheme(BufferedImage image, Theme theme) {
        int width = image.getWidth();
        int height = image.getHeight();
        // Theme extraction; LIGHT mode = top components
        if (theme == Theme.LIGHT) {
            return image.getSubimage(0, 0, width, height/2);
        } else {
            return image.getSubimage(0, height/2, width, height/2);
        }
    }

    private BufferedImage getComponentByState(BufferedImage image, State state) {
        int width = image.getWidth();
        int height = image.getHeight();
        // Theme extraction; LIGHT mode = top components
        if (state == State.RELEASE) {
            return image.getSubimage(0, 0, width/2, height); // LEFT image half
        } else {
            return image.getSubimage(width/2, 0, width/2, height); // RIGHT image half
        }
    }

}

package org.ybda;

import java.awt.*;
import java.awt.event.InputEvent;

public class Robotx extends Robot {
    private static final Robotx in;
    static {
        try {
            in = new Robotx();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    private Robotx(GraphicsDevice screen) throws AWTException {
        super();
    }
    private Robotx() throws AWTException {
        super();
    }

    public static Robotx in() {
        return in;
    }

    public static void leftClick(int x, int y) {
        in.mouseMove(x, y);
        leftClick();
    }

    public static void leftClick() {
        in.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        sleep(20);
        in.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    public static Point getMousePos() {
        return MouseInfo.getPointerInfo().getLocation().getLocation();
    }

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

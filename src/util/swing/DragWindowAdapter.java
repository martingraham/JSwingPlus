package util.swing;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DragWindowAdapter extends MouseAdapter {
    private final Component m_msgWnd;
    transient private int mousePrevX, mousePrevY;
    transient private int frameX, frameY;

    public DragWindowAdapter (final Component mw) {
        m_msgWnd = mw;
    }

    @Override
	public void mousePressed (final MouseEvent mEvent) {
        super.mousePressed(mEvent);
        mousePrevX = mEvent.getX();
        mousePrevY = mEvent.getY();
        frameX = 0;
        frameY = 0; 
    }

    @Override
	public void mouseDragged (final MouseEvent mEvent) {
        final int X = mEvent.getX();
        final int Y = mEvent.getY();
        final int MsgX = m_msgWnd.getX();
        final int MsgY = m_msgWnd.getY();

        final int moveX = X - mousePrevX;  // Negative if move left
        final int moveY = Y - mousePrevY;  // Negative if move down
        if (moveX == 0 && moveY == 0) return;
        mousePrevX = X - moveX;
        mousePrevY = Y - moveY;

        // mouseDragged caused by setLocation() on frame.
        if (frameX == MsgX && frameY == MsgY)
        {
            frameX = 0;
            frameY = 0;
            return;
        }

        // '-' would cause wrong direction for movement.
        final int newFrameX = MsgX + moveX;
        // '-' would cause wrong
        final int newFrameY = MsgY + moveY;

        frameX = newFrameX;
        frameY = newFrameY;
        m_msgWnd.setLocation(newFrameX, newFrameY);
    }
    
    @Override
	public void mouseMoved (final MouseEvent mEvent) {
    	// EMPTY
    }
}
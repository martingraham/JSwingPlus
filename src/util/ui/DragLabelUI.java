package util.ui;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.plaf.LabelUI;
import javax.swing.plaf.basic.BasicLabelUI;

import sun.swing.SwingUtilities2;
import util.GraphicsUtil;


public class DragLabelUI extends BasicLabelUI {

	private static DragLabelUI labelUI = new DragLabelUI();

	private Rectangle	paintIconR = new Rectangle();
	private Rectangle	paintTextR = new Rectangle();
	private Rectangle	paintViewR = new Rectangle();
    private Insets 		paintViewInsets = new Insets(0, 0, 0, 0);
	private Rectangle 	dragArea = new Rectangle ();
 
    public static LabelUI getInstance () { return labelUI; }
    
    
    @Override
	public void paint (final Graphics graphics, final JComponent comp) {        
        super.paint (graphics, comp);
        
        final JLabel label = (JLabel)comp;
        final Insets insets = label.getInsets (paintViewInsets);
        
		final FontMetrics fontMetrics = SwingUtilities2.getFontMetrics(label, graphics);        
		layout (label, fontMetrics, comp.getWidth(), comp.getHeight());
		
        final Graphics2D g2d = (Graphics2D)graphics;  
		final Paint old = g2d.getPaint();
        //g2d.setPaint (GraphicsUtil.DRAGSURFACE);
        
        final int minx = Math.min (paintTextR.x, paintIconR.x) - 2;
        g2d.setPaint (comp.getBackground());
        dragArea.setFrameFromDiagonal (insets.left, insets.top, minx, label.getHeight() - insets.bottom);
        if (dragArea.getWidth () >= 4) {
        	 g2d.fill (dragArea);
        	 g2d.setPaint (GraphicsUtil.DRAGSURFACE);
        	 g2d.fill (dragArea);
        }
        
        final int maxx = (int) Math.max (paintTextR.getMaxX(), paintIconR.getMaxX()) + 2;
        dragArea.setFrameFromDiagonal (maxx, insets.top, label.getWidth() - insets.right, label.getHeight() - insets.bottom);
        if (dragArea.getWidth () >= 4) {
        	g2d.fill (dragArea);
        }
        
        g2d.setPaint (old);
    }
    
    private String layout (final JLabel label, final FontMetrics fontMetrics,
            final int width, final int height) {
		final Insets insets = label.getInsets (paintViewInsets);
		final String text = label.getText();
		final Icon icon = (label.isEnabled()) ? label.getIcon() :
		                            label.getDisabledIcon();
		paintViewR.x = insets.left;
		paintViewR.y = insets.top;
		paintViewR.width = width - (insets.left + insets.right);
		paintViewR.height = height - (insets.top + insets.bottom);
		paintIconR.x = paintIconR.y = paintIconR.width = paintIconR.height = 0;
		paintTextR.x = paintTextR.y = paintTextR.width = paintTextR.height = 0;
		return layoutCL(label, fontMetrics, text, icon, paintViewR, paintIconR,
          paintTextR);
    }
}

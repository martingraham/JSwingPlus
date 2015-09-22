package example;

import io.DataPrep;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.plaf.basic.BasicTextUI;
import javax.swing.plaf.basic.BasicTextUI.BasicCaret;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.View;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.html.HTMLDocument;

import org.apache.log4j.Logger;


import util.GraphicsUtil;
import util.Messages;
import util.colour.ColorUtilities;
import util.swing.DragWindowAdapter;


public class JSwingExampleLauncherHTML {


	
	private final static Logger LOGGER = Logger.getLogger (JSwingExampleLauncherHTML.class);
	private final static String EXAMPLE_PROPERTIES = "example.exampleMainClasses";
	private final static String EXAMPLE_GRAPHIC_PROPS = "example.exampleGraphic";
	
	private final static int WIDTH = 640, HEIGHT = 600;
	 
	transient JFrame frame;
	transient JEditorPane htmlPane;
	transient Shape windowShape;
	
	
	static public void main (final String[] args) {
		new JSwingExampleLauncherHTML ();
	}
	
	public JSwingExampleLauncherHTML () {
		
		//JFrame.setDefaultLookAndFeelDecorated (true);
		
		final List<String> exampleKeys = Messages.getKeysWithPrefix (EXAMPLE_PROPERTIES, "Example");
		Collections.sort (exampleKeys);
		final List<String> exampleNames = new ArrayList<String> (); 
		final List<ExampleLaunchData> launchData = new ArrayList<ExampleLaunchData> ();
		
		for (String exampleNo : exampleKeys) {
			final String exampleName = Messages.getString (EXAMPLE_PROPERTIES, exampleNo);
			exampleNames.add (exampleName);
		}
		
		for (String exampleName : exampleNames) {
			final String className = Messages.getString (EXAMPLE_PROPERTIES, exampleName + "Main");
			final String dataFileName = Messages.getString (EXAMPLE_PROPERTIES, exampleName + "Data");
			final String shortDesc = Messages.getString (EXAMPLE_PROPERTIES, exampleName + "Desc");
			final String longDesc = Messages.getString (EXAMPLE_PROPERTIES, exampleName + "LongDesc");
			launchData.add (new ExampleLaunchData (className, dataFileName, shortDesc, longDesc));
		}
		
		SwingUtilities.invokeLater (
			new Runnable () {
				@Override
				public void run() {
					frame = new MyFrame ("Examples");
					frame.setLocation (100, 100);	
					frame.getContentPane().setLayout (new BorderLayout ());
					frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
					frame.setUndecorated (true);
					frame.addComponentListener (new MyFrameAdapter ());
					final DragWindowAdapter dragger = new DragWindowAdapter (frame);
					setWindowProperties (WIDTH, HEIGHT);

					
					htmlPane = new JEditorPane ();
					htmlPane.setBorder (null);
					htmlPane.setEditable (false);
					htmlPane.setOpaque (false);
					htmlPane.setBackground (GraphicsUtil.NULLCOLOUR);
					
					htmlPane.addMouseMotionListener (dragger);
					htmlPane.addMouseListener (dragger);
					htmlPane.addHyperlinkListener (new MyHyperlinkListener (launchData));
					
					htmlPane.setContentType ("text/html");	// This sets the pane's document as a HTMLDocument
					final HTMLDocument htmlDoc = (HTMLDocument)htmlPane.getDocument();	// So now we can do this
					
					// Next sets html doc base (which causes css file in html to be referenced as example/splash.css)
					htmlDoc.setBase (DataPrep.getInstance().getBaseURL (GraphicsUtil.IMAGEDIR));
					setCaret (htmlPane);
					//GraphicsUtil.setJDK6AATextProperties (htmlPane);
					
					
					//final URL cssURL = DataPrep.getInstance().getRelativeURL (Messages.getString (EXAMPLE_GRAPHIC_PROPS, "cssFile"));
			        //final StyleSheet styleSheet = htmlDoc.getStyleSheet();
			        //System.err.println (styleSheet.toString());
			        /*
			        if (cssURL != null) {
				        try {
				        	final InputStream iStream = DataPrep.getInstance().getInputStream (cssURL);
				            final BufferedReader bReader = DataPrep.getInstance().getBufferedReader (iStream);
				            styleSheet.loadRules (bReader, cssURL);
				            bReader.close ();
				        }
				        catch (final IOException iox) {
				            LOGGER.error ("error in reading css file", iox);
						}
			        }
					*/
					
			        // Make HTML from templates
			        final String tableOutput = makeExampleHTML (launchData);
			        final String splashHTMLFilename = Messages.getString (EXAMPLE_GRAPHIC_PROPS, "htmlFile");
			        final String splashHTML = DataPrep.getInstance().fileToString (splashHTMLFilename);
			        final MessageFormat splashFormat = new MessageFormat (splashHTML);
					Object[] values = new Object [1];
					values [0] = tableOutput;
					final String HTML = splashFormat.format (values);
					htmlPane.setText (HTML);
					LOGGER.debug ("Stylesheet:\n"+htmlDoc.getStyleSheet().toString());
							
					//setWindowProperties (Math.max (WIDTH, htmlPane.getPreferredSize().width), htmlPane.getPreferredSize().height);
					
					frame.getContentPane().add ("North", htmlPane);	// See myComponentAdapter
					frame.setVisible (true);
				}
			}
		);
	}
	
	
	String makeExampleHTML (final List<ExampleLaunchData> launchData) {
		final MessageFormat entryFormat = new MessageFormat (Messages.getString (EXAMPLE_GRAPHIC_PROPS, "exampleHTMLTemplate"));
		Object[] values = new Object [3];
		final StringBuilder sBuilder = new StringBuilder ();
		
		for (int buttonCount = 0; buttonCount < launchData.size(); buttonCount++) {
			final ExampleLaunchData launchDatum = launchData.get (buttonCount);
			values [0] = Integer.toString (buttonCount);
			values [1] = launchDatum.getDescription();
			values [2] = launchDatum.getLongDescription();
			sBuilder.append (entryFormat.format (values));
		}

		return sBuilder.toString();
	}
	
	
	void setWindowProperties (final int width, final int height) {
		frame.setSize (width, height);
		// Make frame transparent via reflection
		String version = System.getProperty("java.version");
		if (version.startsWith ("1.6")) {
			GraphicsUtil.setJDK6WindowProperties (frame, false, new RoundRectangle2D.Double (0, 0, width, height, 140, 140));
		}
		else if (version.startsWith ("1.7")) {
			GraphicsUtil.setJDK7WindowProperties (frame, false, 1.0f, new RoundRectangle2D.Double (0, 0, width, height, 140, 140));
		}
	}
	
	

	
	/**
	 * Set a caret for the JEditorPane with a bespoke selection highlighter
	 * @param jep
	 */
	void setCaret (final JEditorPane jep) {
		final HighlightPainter dhp = new MyHighlightPainter (getColour ("underlineColour1"), getColour ("underlineColour2"));
		final BasicCaret bCaret = new BasicTextUI.BasicCaret() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 8672009127676879801L;

			@Override
			protected Highlighter.HighlightPainter getSelectionPainter() {
				return dhp;
			}
		};
		jep.setCaret (bCaret);
	}
	
	
	class MyHyperlinkListener implements HyperlinkListener {
		
		transient List<ExampleLaunchData> launchData;		
		
		MyHyperlinkListener (final List<ExampleLaunchData> launchData) {
			this.launchData = launchData;
		}
		
		@Override
		public void hyperlinkUpdate (final HyperlinkEvent hEvent) {
			final JEditorPane htmlPane = (JEditorPane)hEvent.getSource ();
			
			if (hEvent.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				final String description = hEvent.getDescription();
				if (Character.isDigit (description.charAt(0))) {
					final Integer intVal = Integer.valueOf (description);
					if (intVal != null) {
						final  int val = intVal.intValue();
						launchData.get(val).launch();
					}
				}
				else if (description.equalsIgnoreCase ("close")) {
					// from camickr - http://www.camick.com/java/source/ExitAction.java
					final WindowEvent windowClosing = new WindowEvent(frame, WindowEvent.WINDOW_CLOSING);
					Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(windowClosing);
				}
			}
			else if (hEvent.getEventType() == HyperlinkEvent.EventType.ENTERED) {
				final Element elem = hEvent.getSourceElement();
				htmlPane.select (elem.getStartOffset(), elem.getEndOffset());
			}
			else if (hEvent.getEventType() == HyperlinkEvent.EventType.EXITED) {
				htmlPane.select (0, 0);
			}
		}	
	}

	
	Color getColour (final String colourDesc) {
		final String value = Messages.getString (EXAMPLE_GRAPHIC_PROPS, colourDesc);
		return (value.charAt(0) == Messages.ERROR_CHAR ? Color.gray : ColorUtilities.decodeWithAlpha (value));
	}
	
	
	
	static class MyFrame extends JFrame {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -4695808243727488857L;
		transient Color grad1, grad2, undercoat;
		
		MyFrame (final String title) {
			super (title);
			setColors ();
		}
		
		final void setColors () {
			
			final String[] keyStrings = {"washColour1", "washColour2", "undercoatColour"};
			Color[] colours = new Color [keyStrings.length];
			final Color[] defaults =  {new Color (255, 128, 0, 128), new Color (0, 0, 0, 64), new Color (100, 100, 0, 64)};
			for (int index = 0; index < keyStrings.length; index++) {
				final String value = Messages.getString (EXAMPLE_GRAPHIC_PROPS, keyStrings [index]);
				if (value.charAt(0) != Messages.ERROR_CHAR) {
					colours[index] = ColorUtilities.decodeWithAlpha (value);
				}
				if (colours[index] == null) {
					colours[index] = defaults[index];
				}
			}
			
			grad1 = colours [0];
			grad2 = colours [1];
			undercoat = colours [2];
		}
		
		
		/**
		 * Sets the undercoat colour, draws the contents and then
		 * draws a gradient wash on top.
		 */
		
		@Override
		public void paint (final Graphics graphics) {
			
			//this.setBackground (undercoat);
			super.paint (graphics);
			
			final Graphics2D g2D = (Graphics2D) graphics;
			final Paint oldPaint = g2D.getPaint ();
			
			final Paint newPaint = new GradientPaint (0, 0, grad1, 
					this.getWidth(), this.getHeight(), grad2, true);
			g2D.setPaint (newPaint);
			g2D.fillRect (0, 0, this.getWidth(), this.getHeight());
			
			g2D.setPaint (oldPaint);
		}
		
	}
	
	
	/**
	 * Resize the frame to fit the JEditorPane height
	 * JEditorPane must not be added to the "Center" box of a BorderLayout or
	 * it will just return the size the frame is currently at.
	 * @author cs22
	 *
	 */
	class MyFrameAdapter extends ComponentAdapter {
		
		
		//Timer timer;
		 /**
	     * Invoked when the component has been made visible.
	     */
		@Override
	    public void componentShown (final ComponentEvent cEvent) {	
			//final Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			final String opSystem = System.getProperty("os.name").toLowerCase();
			LOGGER.info ("Operating System: "+opSystem);
			if (!opSystem.contains("mac")) {
				final JFrame frame = (JFrame)cEvent.getComponent();
				final Dimension size = frame.getContentPane().getComponent(0).getSize();
				LOGGER.info ("HTML Page dimensions: [w: "+size.width+", h: "+size.height+"]");
				setWindowProperties (size.width, size.height);
			}
		}
	}
	

	
	
	
	class MyHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {
		
		transient Color secondColour;
		
		public MyHighlightPainter (final Color col, final Color col2) {
			super (col);
			secondColour = col2;
		}
        
    	// --- LayerPainter methods ----------------------------
        /**
         * Paints a portion of a highlight.
         *
         * @param graphics the graphics context
         * @param offs0 the starting model offset >= 0
         * @param offs1 the ending model offset >= offs1
         * @param bounds the bounding box of the view, which is not
		 *        necessarily the region to paint.
	         * @param comp the editor
		 * @param view View painting for
		 * @return region drawing occured in
	         */
		@Override
		public Shape paintLayer (final Graphics graphics, final int offs0, final int offs1,
				final Shape bounds, final JTextComponent comp, final View view) {
		    final Color color = getColor();

		    if (color == null) {
			graphics.setColor(comp.getSelectionColor());
		    }
		    else {
			graphics.setColor(color);
		    }
		    if (offs0 == view.getStartOffset() &&
			offs1 == view.getEndOffset()) {
				// Contained in view, can just use bounds.
				Rectangle alloc;
				if (bounds instanceof Rectangle) {
				    alloc = (Rectangle)bounds;
				}
				else {
				    alloc = bounds.getBounds();
				}
				final Graphics2D g2D = (Graphics2D)graphics;
				final Paint oldPaint = g2D.getPaint();         
				final Paint newPaint = new GradientPaint (alloc.x, alloc.y + alloc.height, color,
                						alloc.x + alloc.width, alloc.y, secondColour);
                g2D.setPaint (newPaint);
				graphics.fillRect(alloc.x, alloc.y + alloc.height - 5, alloc.width, 5);
				g2D.setPaint (oldPaint);
				return alloc;
			    }
			    else {
				// Should only render part of View.
				try {
				    // --- determine locations ---
	                final Shape shape = view.modelToView(offs0, Position.Bias.Forward,
	                                               offs1,Position.Bias.Backward,
	                                               bounds);
	                final Rectangle rect = (shape instanceof Rectangle) ?
	                              (Rectangle)shape : shape.getBounds();
	                graphics.fillRect (rect.x, rect.y + rect.height - 3, rect.width, 3);
	                return rect;
				} catch (BadLocationException ble) {
				    // can't render
				}
		    }
		    // Only if exception
		    return null;
		}
	}
}

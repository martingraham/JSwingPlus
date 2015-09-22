package util.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import util.GraphicsUtil;
import util.Messages;

public class InitialisationProgressBar extends JProgressBar {

     /**
	 * 
	 */
	private static final long serialVersionUID = 7993641885023017420L;
	JDialog jDialog;
	private final static int WIDTH = 288, HEIGHT = 48;
	private final Font font = Font.decode (Messages.getString (GraphicsUtil.GRAPHICPROPS, "titleFont"));
	private JLabel headerLabel;

   public InitialisationProgressBar (final String title) {
   		this (WIDTH, HEIGHT, title);
   }

   public InitialisationProgressBar (final int width, final int height, final String title) {

	   EventQueue.invokeLater (new Runnable() {
           public void run() {
           	setValue (0);
        	setIndeterminate (true);
			setStringPainted (true);

			final Box box = Box.createVerticalBox ();
			box.setBorder (BorderFactory.createLineBorder (Color.black, 3));
			box.setPreferredSize (new Dimension (width, height));

			setFont (font);

			headerLabel = new JLabel (title);
			box.add (headerLabel);
			box.add (InitialisationProgressBar.this);
			
   			jDialog = new JDialog ((Frame)null, false);
   			jDialog.setUndecorated (true);
   			jDialog.setLocation ((Toolkit.getDefaultToolkit().getScreenSize().width - width) >> 1,
   					(Toolkit.getDefaultToolkit().getScreenSize().height - 32 - height) >> 1);
   			jDialog.add (box);

   			jDialog.setAlwaysOnTop (true);
   			jDialog.setVisible (true);
   			jDialog.pack();
           }
       });
   }


   public void kill () {
	   EventQueue.invokeLater (new Runnable() {
           public void run() {
               setValue (getMinimum());
               jDialog.setVisible (false);
               jDialog.dispose();
           }
       });
   }

   public void setText (final String text) {
   	   EventQueue.invokeLater (new Runnable() {
           public void run() {
        	   setString (text);
        	   repaint ();
           }
       }); 
   }
   
   public void setHeaderLabel (final String text) {
   	   EventQueue.invokeLater (new Runnable() {
           public void run() {
        	   headerLabel.setText (text);
        	   repaint ();
           }
       }); 
   }
}
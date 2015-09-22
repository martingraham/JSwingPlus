package example.graph.renderers.node;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import example.graph.JGraphMrMen.MrMan;

import swingPlus.graph.JGraph;


public class MrManCellRenderer extends ImageGraphCellRenderer  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5944439037264442105L;

	protected static final Color BACKGROUND = new Color (224, 224, 255);
	
	protected JLabel nameLabel;
	protected JPanel imagePanel;
	
	Font mrmanfont = Font.decode("DejaVu Sans Condensed-bold-8");
	Font[] fonts = new Font [50];
	int fontCutoff = 4;
	
	public MrManCellRenderer () {
		super ();
		
		nameLabel = new JLabel ();
		nameLabel.setFont (mrmanfont);
		nameLabel.setHorizontalAlignment (SwingConstants.CENTER);
		nameLabel.setBackground (Color.white);
		nameLabel.setOpaque (true);
		
		imagePanel = new ImagePanel ();
		
		final GridBagLayout layout = new GridBagLayout ();
		this.setLayout (layout);
		final GridBagConstraints gbagConstraints = new GridBagConstraints ();
		gbagConstraints.fill = GridBagConstraints.BOTH;
		gbagConstraints.gridx = 0; gbagConstraints.gridy = 0; 
		gbagConstraints.gridheight = 1; gbagConstraints.gridwidth = 1;
		gbagConstraints.weightx = 1; gbagConstraints.weighty = 0;
		add (nameLabel, gbagConstraints);
		
		
		gbagConstraints.fill = GridBagConstraints.BOTH;
		gbagConstraints.gridx = 0; gbagConstraints.gridy = 1; 
		gbagConstraints.gridheight = 1; gbagConstraints.gridwidth = 1;
		gbagConstraints.weightx = 1; gbagConstraints.weighty = 1;
		add (imagePanel, gbagConstraints);
	
		//this.setLayout (new BorderLayout ());
		//add (nameLabel, BorderLayout.NORTH);
		//add (imagePanel, BorderLayout.CENTER);
		
		for (int n = 0; n < fonts.length; n++) {
			fonts [n] = mrmanfont.deriveFont ((float)(fontCutoff + n));
		}
	}
	
	@Override
	public Component getGraphCellRendererComponent (final JGraph graph, final Object value,
			final boolean isSelected, final boolean hasFocus) {
		this.setEnabled (isSelected);
		setImageAndNameLabel (value);
		this.setBorder (isSelected ? borderSelected : border);
		this.getInsets (insets);
		
		preferredSize.setSize (image.getWidth() + insets.left + insets.right, 
				image.getHeight() + insets.bottom + insets.top + 20);
		this.setPreferredSize (preferredSize);
		//this.validate();
		
		return this;
	}
	
	
	public void setImageAndNameLabel (final Object obj) {
		final MrMan mrman = (MrMan)obj;
		image = mrman.getImage();
		nameLabel.setText (mrman.getName());
	}
	
	
	@Override
	public void paintComponent (final Graphics gContext) {	
		final double scale = (double)getSize().height / (double)getPreferredSize().height;
		//System.out.println ("scale: "+scale);
		nameLabel.setSize (new Dimension (nameLabel.getSize().width, (int)Math.ceil(20.0 * scale)));
		int fontSize = (int)(nameLabel.getSize().getHeight() * 0.55);
		fontSize = Math.max (0, Math.min (fontSize, fonts.length - 1));
		nameLabel.setFont (fonts [fontSize]);
		
		//System.err.println ("Size: "+getSize());
	}
	
	class ImagePanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 708708980875613535L;

		@Override
		public void paintComponent (final Graphics gContext) {	
			super.paintComponent(gContext);
			getInsets (insets);
			MrManCellRenderer.this.drawImage (gContext, this);	
		}
	}
}

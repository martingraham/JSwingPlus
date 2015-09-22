package swingPlus.graph;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;


public class ForceAnalyser extends JPanel implements PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5929932113707593150L;
	
	JGraph graph;
	double maxEnergy = 20.0;
	boolean instantaneousScale = true;
	
	protected Font font = Font.decode ("Gill-Sans-MT-plain-10");
	



	public ForceAnalyser () {
		super (true);
	}
	
	public void setGraph (final JGraph graph) {
		if (this.graph != null) {
			this.graph.removePropertyChangeListener ("repaintLayoutThread", this);
		}
		this.graph = graph;
		if (graph != null) {
			graph.addPropertyChangeListener ("repaintLayoutThread", this);
		}	
	}
	
	public boolean isInstantaneousScale() {
		return instantaneousScale;
	}

	public void setInstantaneousScale (final boolean instantaneousScale) {
		this.instantaneousScale = instantaneousScale;
	}
	
	
	
	@Override
	public void paintComponent (final Graphics graphics) {
		 super.paintComponent (graphics); 

		graphics.setColor (new Color (255, 0, 0, 64));
		final ObjectPlacementMapping opMapping = graph.getObjectPlacementMapping ();
		
		final Set<Map.Entry<Object, ObjectPlacement>> placementEntries = opMapping.getAllEntries();
		final int size = placementEntries.size();
		final double heightPerPlacement = (double)this.getHeight() / size;
		final double widthScale = getWidth() / maxEnergy;
		
		if (isInstantaneousScale()) {
			maxEnergy = 0.0;
		}
		
		double y = 0;
		for (Map.Entry<Object, ObjectPlacement> entry : placementEntries) {
			final ObjectPlacement placement = entry.getValue();
			final double energy = (placement.getVelocityX() * placement.getVelocityX())
				+ (placement.getVelocityY() * placement.getVelocityY());
			final double scaledEnergy = energy * widthScale;
			if (heightPerPlacement <= 1.0) {
				graphics.drawLine (0, (int)y, (int)scaledEnergy, (int)y);
			} else {
				graphics.fillRect (0, (int)y, (int)scaledEnergy + 1, (int)(heightPerPlacement + 1));
			}
			if (heightPerPlacement > 10.0) {
				graphics.drawString (entry.getKey().toString(), 1, (int)(y + 10 + ((heightPerPlacement - 10) / 2)));
			}
			maxEnergy = Math.max (maxEnergy, energy);
			
			y += heightPerPlacement;
		}
	}

	@Override
	public void propertyChange (final PropertyChangeEvent evt) {
		if ("repaintLayoutThread".equals (evt.getPropertyName())) {
			repaint();
		}	
	}
}

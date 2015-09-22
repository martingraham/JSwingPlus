package util.anim;

import java.awt.Rectangle;

public interface Animatable {
	
	public boolean isNew ();

	public boolean isOld ();
	
	public Rectangle getBounds (final double percentage);
	
	public void setBounds (final double percentage);
	
	public void setFutureBounds (final Rectangle r);
	
	public void storeOldBounds ();
}

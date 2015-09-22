package ui;

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.ToolTipManager;
import javax.swing.plaf.ComponentUI;

import org.apache.log4j.Logger;

/**
 * Class that associates certain rendered swing components and selection models 
 * with ordered table columns depending on the data in that column
 * Currently a JSlider and BoundedRangeModel for continuous / large columns
 * and a JCheckBox set and a BitSet for highly discrete columns with few values
 * @author cs22
 *
 */
public class ParCoordMultiplexColumnTouchUI extends ParCoordMultiplexColumnUI {

    private static final Class<ParCoordMultiplexColumnTouchUI> CLASS_OBJ = ParCoordMultiplexColumnTouchUI.class;
    private static final Logger LOGGER = Logger.getLogger (CLASS_OBJ);

    static final int MIN_TOUCH_INTERACTOR_SIZE = 24;
    // Extended Handler - replaces all apart from listening to keypresses
    // and property change events
    protected class HandlerTouch extends Handler2 {
		
	    // MouseInputListener       
		@Override
		public void mouseEntered (final MouseEvent mEvent) {
			table.requestFocusInWindow();
			if (isTableActive() && !isDragging) {
				ToolTipManager.sharedInstance().setDismissDelay (5000);
	    		ToolTipManager.sharedInstance().setInitialDelay (0);
	    		//ToolTipManager.sharedInstance().setReshowDelay (0);
	    		prepareInteractorComponents (MIN_TOUCH_INTERACTOR_SIZE);
				startBrushing ();	
			}
		}
		

		@Override
        public void mouseMoved (final MouseEvent mEvent) {   	
	        if (isTableActive()) {   
				final Point mPoint = mEvent.getPoint ();
		        final int viewColumnIndex = table.columnAtPoint (mEvent.getPoint());
		        if (goneOutside) {
		        	goneOutside = false;
		        	prepareInteractorComponents (MIN_TOUCH_INTERACTOR_SIZE);
		        }
				calibrateColumnInteractor (viewColumnIndex);
				final AbstractColumnInteractor cInter = getAxisInteractor (viewColumnIndex);
				
				final boolean toolTipSet = cInter.setComponentSpecificToolTip (mEvent.getY(), viewColumnIndex, visibleRect, ParCoordMultiplexColumnTouchUI.this);
				if (!toolTipSet) {
					doBrushing (mPoint);
				}
	        }
		}
    }

//
//  Factory methods for the Listeners
//

    @Override
    protected Handler getHandler() {
        if (handler == null) {
            handler = new HandlerTouch();
        }
        return handler;
    }
    

    
//
//  The installation/uninstall procedures and support
//

    public static ComponentUI createUI (final JComponent comp) {
    	LOGGER.debug ("new ParCoordMultiplexColumnTouchUI instance created");
        return new ParCoordMultiplexColumnTouchUI();
    }
}
    
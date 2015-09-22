package swingPlus.graph;

import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.swing.JComponent;

import org.apache.log4j.Logger;

import swingPlus.shared.tooltip.AbstractCellToolTipMouseListener;



public class DefaultGraphCellToolTipListener extends AbstractCellToolTipMouseListener {

	private final static Logger LOGGER = Logger.getLogger (DefaultGraphCellToolTipListener.class);
	
	Object lastObj = null;
	
	@Override
	public void mouseMoved (final MouseEvent mEvent) {
		Object newObj = null;
		final JComponent jComp = (JComponent)mEvent.getComponent();
		
		if (jComp instanceof JGraph) {
			final JGraph jGraph = (JGraph)jComp;
			newObj = jGraph.getUI().getNodeAt (mEvent.getPoint ());	
		}
		
		if (newObj != lastObj && newObj != null) {
			lastObj = newObj;
			Object obj = null;
			
			if (newObj != null) {
				obj = newObj;
				
	        	//if (obj != null) {
	        		LOGGER.debug ("obj: "+obj+", "+obj.getClass());
	        		final GraphRendererToolTip rtt = (GraphRendererToolTip)jComp.createToolTip();
		        	if ((obj instanceof Collection && ((Collection)obj).isEmpty())) {
		        		jComp.setToolTipText ("");
		        		rtt.setToolTipObject (null);
		        	} else {
		        		jComp.setToolTipText (obj.toString());
		        		rtt.setToolTipObject (obj);
		        	}
		        /*	
	        	} else {
	        		final String toolTipText = jComp.getToolTipText (mEvent);
	    			jComp.setToolTipText (toolTipText);
	        	}
	        	*/
			}
		}
	}
	
	@Override
	public void mouseExited (final MouseEvent mEvent) {
		if (mEvent.getComponent() instanceof JComponent) {
			super.mouseExited (mEvent);
			lastObj = null;
		}		
	}
}

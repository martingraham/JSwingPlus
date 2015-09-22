package util.ui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;

import util.GraphicsUtil;
import util.Messages;


public final class NewMetalTheme extends DefaultMetalTheme {

       private final static Color basePrimaryColour = Color.decode (Messages.getString (GraphicsUtil.GRAPHICPROPS, "basePrimaryColour"));
       private final static Color baseSecondaryColour = Color.decode (Messages.getString (GraphicsUtil.GRAPHICPROPS, "baseSecondaryColour"));
       private final static ColorUIResource newPrimary2 = new ColorUIResource (basePrimaryColour);
       private final static ColorUIResource newPrimary1 = new ColorUIResource (basePrimaryColour.darker());
       private final static ColorUIResource newPrimary3 = new ColorUIResource (basePrimaryColour.brighter());
       private final static ColorUIResource newSecondary2 = new ColorUIResource (baseSecondaryColour);
       private final static ColorUIResource newSecondary1 = new ColorUIResource (baseSecondaryColour.darker());
       private final static ColorUIResource newSecondary3 = new ColorUIResource (baseSecondaryColour.brighter());
       private final static String description = "NewMetalTheme"; //$NON-NLS-1$
       private final static FontUIResource controlFont = new FontUIResource (Font.decode (Messages.getString (GraphicsUtil.GRAPHICPROPS, "controlFont")));
       private final static FontUIResource systemFont = new FontUIResource (Font.decode (Messages.getString (GraphicsUtil.GRAPHICPROPS, "systemFont")));
       private final static FontUIResource userFont = new FontUIResource (Font.decode (Messages.getString (GraphicsUtil.GRAPHICPROPS, "userFont")));
       private final static FontUIResource smallFont = new FontUIResource (Font.decode (Messages.getString (GraphicsUtil.GRAPHICPROPS, "smallFont")));
       private final static FontUIResource titleFont = new FontUIResource (Font.decode (Messages.getString (GraphicsUtil.GRAPHICPROPS, "titleFont")));


       @Override
	protected  ColorUIResource getPrimary1() { return newPrimary1; }
       @Override
	protected  ColorUIResource getPrimary2() { return newPrimary2; }
       @Override
	protected  ColorUIResource getPrimary3() { return newPrimary3; }
       @Override
	protected  ColorUIResource getSecondary1() { return newSecondary1; }
       @Override
	protected  ColorUIResource getSecondary2() { return newSecondary2; }
       @Override
	protected  ColorUIResource getSecondary3() { return newSecondary3; }
       @Override
	public ColorUIResource getDesktopColor() { return newSecondary3; }

       @Override
	public FontUIResource getControlTextFont () { return controlFont; }
       @Override
	public FontUIResource getMenuTextFont () { return controlFont; }
       @Override
	public FontUIResource getWindowTitleFont () { return titleFont; }
       @Override
	public FontUIResource getSystemTextFont () { return systemFont; }
       @Override
	public FontUIResource getUserTextFont () { return userFont; }
       @Override
	public FontUIResource getSubTextFont () { return smallFont; }

       @Override
	public String getName() { return description; }
}
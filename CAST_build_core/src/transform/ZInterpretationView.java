package transform;

import java.awt.*;
import java.util.*;

import dataView.*;


public class ZInterpretationView extends DataView {
	
	private String nameKey, zKey;
//	private String variableName;
	private Color zColor;
	private String messageString;
	
	public ZInterpretationView(DataSet theData, XApplet applet, String nameKey, String zKey,
																			String variableName, Color zColor, String messageString) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.nameKey = nameKey;
		this.zKey = zKey;
//		this.variableName = variableName;
		this.zColor = zColor;
		this.messageString = messageString;
	}
	
	public void setZKey(String zKey) {
		this.zKey = zKey;
	}
	
	public void paintView(Graphics g) {
		int selectedIndex = getSelection().findSingleSetFlag();
		
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
//		int descent = fm.getDescent();
		
		if (selectedIndex >= 0) {
			StringTokenizer st = new StringTokenizer(getApplet().translate("'s value is * st devns * the mean value"), "*");
			
			LabelVariable nameVar = (LabelVariable)getVariable(nameKey);
			Value name = nameVar.valueAt(selectedIndex);
			
			NumVariable zVar = (NumVariable)getVariable(zKey);
			NumValue z = (NumValue)zVar.valueAt(selectedIndex);
			String direction;
			if (z.toDouble() >= 0)
				direction = getApplet().translate("above");
			else {
				z.setValue(-z.toDouble());
				direction = getApplet().translate("below");
			}
			String s0 = name + st.nextToken();
			int s0Width = fm.stringWidth(s0);
			int zWidth = z.stringWidth(g);
			
			String s1 = st.nextToken();
			int s1Width = fm.stringWidth(s1);
			int directionWidth = fm.stringWidth(direction);
			
			String s2 = st.nextToken();
			int s2Width = fm.stringWidth(s2);
			
			int sWidth = s0Width + zWidth + s1Width + directionWidth + s2Width;
			
			int startHoriz = (getSize().width - sWidth) / 2;
			g.drawString(s0, startHoriz, ascent);
			startHoriz += s0Width;
			
			g.setColor(zColor);
			z.drawRight(g, startHoriz, ascent);
			startHoriz += zWidth;
			g.setColor(getForeground());
			
			g.drawString(s1, startHoriz, ascent);
			startHoriz += s1Width;
			
			g.setColor(zColor);
			g.drawString(direction, startHoriz, ascent);
			startHoriz += directionWidth;
			g.setColor(getForeground());
			
			g.drawString(s2, startHoriz, ascent);
		}
		else {
			int sWidth = fm.stringWidth(messageString);
			int startHoriz = (getSize().width - sWidth) / 2;
			g.setColor(Color.gray);
			g.drawString(messageString, startHoriz, ascent);
		}
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		FontMetrics fm = getGraphics().getFontMetrics();
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		
		return new Dimension(50, ascent + descent);		//	assumes its width is set by BorderLayout or similar
	}
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
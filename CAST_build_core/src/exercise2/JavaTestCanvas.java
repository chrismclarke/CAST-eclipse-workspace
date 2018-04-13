package exercise2;

import java.awt.*;

import dataView.*;

public class JavaTestCanvas extends BufferedCanvas {

	static final private Color kDarkGreen = new Color(0x009900);
	
	static final private String kErrorString1 = "Java";
	static final private String kErrorString2 = "update";
	static final private String kErrorString3 = "required.";
	
	public JavaTestCanvas(XApplet applet) {
		super(applet);
		lockBackground(new Color(0x0066FF));
	}
	
	public void corePaint(Graphics g) {
		boolean tooOld = false;
		if (isJavaUpToDate)
			g.setColor(kDarkGreen);
		else {
			g.setColor(new Color(0xFF9900));
			tooOld = true;
		}
		g.fillRect(0, 0, getSize().width, getSize().height);
		if (tooOld) {
			g.setColor(Color.black);
			FontMetrics fm = g.getFontMetrics();
			int ascent = fm.getAscent();
			int descent = fm.getDescent();
			int baseline = ascent + 3;
			int horizStart = (getSize().width - fm.stringWidth(kErrorString1)) / 2;
			g.drawString(kErrorString1, horizStart, baseline);
			baseline += ascent + descent + 2;
			horizStart = (getSize().width - fm.stringWidth(kErrorString2)) / 2;
			g.drawString(kErrorString2, horizStart, baseline);
			baseline += ascent + descent + 2;
			horizStart = (getSize().width - fm.stringWidth(kErrorString3)) / 2;
			g.drawString(kErrorString3, horizStart, baseline);
		}
	}

//-----------------------------------------------------------------------------------

	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		return false;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
	}
}
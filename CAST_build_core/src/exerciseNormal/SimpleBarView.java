package exerciseNormal;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import coreGraphics.*;


public class SimpleBarView extends DataView {
//	static public final String SIMPLE_BAR = "simpleBar";
	
	static final private int kBarTopBorder = 10;
	static final private int kMinBarWidth = 2;
	static final private int kMaxBarWidth = 8;
	
	static final private int kNoOfShades = AccurateDistnArtist.kNoOfShades;
	static final private Color kBarColor = Color.blue;
	
	private String distnKey;
	private HorizAxis valAxis;
	
	private Color barShade[] = new Color[kNoOfShades + 1];
	
	public SimpleBarView(DataSet theData, XApplet applet, String distnKey, HorizAxis valAxis) {
		super(theData, applet, new Insets(2, 5, 0, 5));
		this.distnKey = distnKey;
		this.valAxis = valAxis;
		
		AccurateDistnArtist.setShades(barShade, kBarColor);
	}
	
	public void changeDistnKey(String distnKey) {
		this.distnKey = distnKey;
	}
	
	private int getBarWidth() {
		int nCats = (int)Math.round(valAxis.maxOnAxis);
		return Math.min(kMaxBarWidth, Math.max(kMinBarWidth, getSize().width / (3 * nCats)));
	}
	
	public void paintView(Graphics g) {
		DiscreteDistnVariable distn = (DiscreteDistnVariable)getVariable(distnKey);
		int nCats = (int)Math.round(Math.floor(valAxis.maxOnAxis));
		
		Point p = null;
		int barWidth = getBarWidth();
		double maxScaledProb = distn.getMaxScaledProb();
		
		for (int i=0 ; i<=nCats ; i++) {
			double doubleHt = (getSize().height - kBarTopBorder) * distn.getScaledProb(i) / maxScaledProb;
			int pixHeight = (int)Math.floor(doubleHt);
			int topPixShade = (int)Math.round((doubleHt - pixHeight) * kNoOfShades);
			
			int xPos = valAxis.numValToRawPosition(i);			
			p = translateToScreen(xPos, pixHeight, p);
			
			g.setColor(kBarColor);
			g.fillRect(p.x - barWidth / 2, p.y + 1, barWidth, getSize().height - barWidth);
			
			g.setColor(barShade[topPixShade]);
			g.fillRect(p.x - barWidth / 2, p.y, barWidth, 1);
		}
	}
	
	protected void doChangeVariable(Graphics g, String key) {
		if (distnKey.equals(key))
			repaint();
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	

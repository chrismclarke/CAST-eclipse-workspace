package exerciseCateg;

import java.awt.*;

import dataView.*;
import axis.*;


public class BarCountView extends DataView {
//	static public final String BAR_COUNT_VIEW = "barCount";
	
	static final private int kCountGap = 5;
	static final private int kMinHalfBarWidth = 4;
	static final private int kMaxHalfBarWidth = 8;
	
	static final private Color kBarOutlineColour = Color.black;
	static final private Color kBarFillColour = new Color(0xAAAAAA);
	
	static final private Color kBarHiliteColour = Color.yellow;
	
	private String yKey;
	private HorizAxis valAxis;
	private VertAxis countAxis;
		
	private boolean[] selectedBars = null;
	
	public BarCountView(DataSet theData, XApplet applet,
												String yKey, HorizAxis valAxis, VertAxis countAxis) {
		super(theData, applet, new Insets(2, 5, 0, 5));
		this.yKey = yKey;
		this.valAxis = valAxis;
		this.countAxis = countAxis;
	}
	
	protected int[] getCounts() {
		CatVariable yVar = (CatVariable)getVariable(yKey);
		return yVar.getCounts();
	}
	
	public void setSelectedBars(boolean[] selectedBars) {
		this.selectedBars = selectedBars;
	}

//-------------------------------------------------------------------
	
	protected int getHalfBarWidth() {
		CatVariable yVar = (CatVariable)getVariable(yKey);
		int nCats = yVar.noOfCategories();
		return Math.min(kMaxHalfBarWidth, Math.max(kMinHalfBarWidth, getSize().width / (6 * nCats)));
	}
	
	private void drawBar(Graphics g, Point pTop, int halfWidth, int barIndex) {
		int width = 2 * halfWidth;
		int height = getSize().height - pTop.y;
		if (height > 1) {
			boolean selected = selectedBars != null && selectedBars[barIndex];
			g.setColor(selected ? kBarHiliteColour : kBarFillColour);
			g.fillRect(pTop.x - halfWidth, pTop.y, width, height);
			
			Color barOutlineColor = kBarOutlineColour;
			g.setColor(barOutlineColor);
			g.drawRect(pTop.x - halfWidth, pTop.y, width, height);
		}
		else if (selectedBars != null && selectedBars[barIndex]) {
			g.setColor(kBarHiliteColour);
			g.fillOval(pTop.x - 8, pTop.y - 22, 16, 16);
		}
	}
	
	public void paintView(Graphics g) {
		int count[] = getCounts();
		
		Point p = null;
		int halfBarWidth = getHalfBarWidth();
		
		NumValue countVal = new NumValue(0.0, 0);
//		int ascent = g.getFontMetrics().getAscent();
		
		for (int i=0 ; i<count.length ; i++) {
			int barHt = countAxis.numValToRawPosition(count[i]);
			int xPos = valAxis.catValToPosition(i);			
			p = translateToScreen(xPos, barHt, p);
			
			drawBar(g, p, halfBarWidth, i);
			
			if (count[i] > 0) {
				boolean selected = selectedBars != null && selectedBars[i];
				g.setColor(selected ? Color.red : Color.gray);
				
				countVal.setValue(Math.rint(count[i]));
				
				int baseline = p.y - kCountGap;
				countVal.drawCentred(g, p.x, baseline);
			}
		}
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(yKey)) {
			selectedBars = null;
			repaint();
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	

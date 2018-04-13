package cat;

import java.awt.*;

import dataView.*;
import axis.*;


public class CatBarView extends CatDataView {
//	static final public String BARCHART = "catBarChart";
	
	static final public Color kFreqColor = new Color(0xBB0000);
	static final public Color kPropnColor = new Color(0x0000BB);
	
	static final private int kHalfBarWidth = 4;
//	static final private int kHitSlop = 3;
	
	private HorizAxis catAxis;
	private VertAxis freqAxis;
	private boolean probabilityMode = false;		//		labels propn as "Probability"
	
	public CatBarView(DataSet theData, XApplet applet, String catKey, int dragType,
											HorizAxis catAxis, VertAxis freqAxis) {
		super(theData, applet, catKey, dragType);
		this.catAxis = catAxis;
		this.freqAxis = freqAxis;
		setFont(applet.getBigBoldFont());
	}
	
	public void setProbabilityMode(boolean probabilityMode) {
		this.probabilityMode = probabilityMode;
	}
	
	public void paintView(Graphics g) {
		CatVariable variable = (CatVariable)getVariable(catKey);
		initialise(variable, g);
		
		int halfBarWidth = Math.max(getSize().width / count.length / 5, kHalfBarWidth);
		
		boolean selectedCats[] = getSelectedCats();
		boolean noSelection = noSelectedCats(selectedCats);
		
		Point topLeft = null;
		for (int i=0 ; i<count.length ; i++) {
			g.setColor(getCatColor(i, noSelection || selectedCats[i]));
			
			int x = catAxis.catValToPosition(i);
			int y = freqAxis.numValToRawPosition(count[i]);
			topLeft = translateToScreen(x, y, topLeft);
			g.fillRect(topLeft.x - halfBarWidth, topLeft.y, 2 * halfBarWidth, getSize().height - topLeft.y);
			
			if (selectedCats[i]) {
				g.setColor(Color.black);
				g.drawRect(topLeft.x - halfBarWidth - 1, topLeft.y - 1, 2 * halfBarWidth + 1,
																					getSize().height - topLeft.y + 1);
				g.drawRect(topLeft.x - halfBarWidth, topLeft.y, 2 * halfBarWidth - 1,
																					getSize().height - topLeft.y - 1);
			}
		}
		
		if (dragType == SELECT_ONE)
			for (int i=0 ; i<count.length ; i++)
				if (selectedCats[i]) {
					int x = catAxis.catValToPosition(i);
					int y = freqAxis.numValToRawPosition(count[i]);
					topLeft = translateToScreen(x, y, topLeft);
					
					int freq = count[i];
					
					if (!probabilityMode) {
						g.setColor(kFreqColor);
						g.drawLine(0, topLeft.y, topLeft.x - halfBarWidth - 2, topLeft.y);
						g.drawLine(1, topLeft.y - 1, topLeft.x - halfBarWidth - 2, topLeft.y - 1);
						g.drawLine(1, topLeft.y + 1, topLeft.x - halfBarWidth - 2, topLeft.y + 1);
						g.drawLine(0, topLeft.y, 4, topLeft.y - 4);
						g.drawLine(0, topLeft.y, 4, topLeft.y + 4);
						
						NumValue freqVal = new NumValue(freq, 0);
						int freqLength = freqVal.stringWidthWithCommas(g);
						int freqLeft = Math.max(3, (topLeft.x - freqLength) / 2);
						int freqRight = freqLeft + freqLength;
						freqVal.drawWithCommas(g, freqRight, topLeft.y - 4);
					}
					
					g.setColor(kPropnColor);
					g.drawLine(topLeft.x + halfBarWidth + 1, topLeft.y, getSize().width - 1, topLeft.y);
					g.drawLine(topLeft.x + halfBarWidth + 1, topLeft.y - 1, getSize().width - 2, topLeft.y - 1);
					g.drawLine(topLeft.x + halfBarWidth + 1, topLeft.y + 1, getSize().width - 2, topLeft.y + 1);
					g.drawLine(getSize().width - 5, topLeft.y - 4, getSize().width - 1, topLeft.y);
					g.drawLine(getSize().width - 5, topLeft.y + 4, getSize().width - 1, topLeft.y);
					
					double propn = freq / (double)totalCount;
					Value propnVal = new NumValue(propn, 3);
					if (probabilityMode)
						propnVal = new LabelValue("Prob(" + variable.getLabel(i) + ") = " + propnVal.toString());
					int propnLength = propnVal.stringWidth(g);
					int propnRight = Math.max(3, (getSize().width - topLeft.x - 2 * halfBarWidth - propnLength) / 2);
					int propnLeft = getSize().width - propnLength - propnRight;
					propnVal.drawRight(g, propnLeft, topLeft.y - 4);
				}
		
		if (targetBefore != -1) {
			g.setColor(Color.red);
			int xBefore = (targetBefore == 0) ? 0 : catAxis.catValToPosition(targetBefore - 1);
			int xAfter = (targetBefore == count.length) ? catAxis.getAxisLength() : catAxis.catValToPosition(targetBefore);
			int x = (xBefore + xAfter) / 2;
			g.fillRect(x - 1, 0, 3, getSize().height);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (catKey.equals(key)) {
			CatVariable variable = (CatVariable)getVariable(catKey);
			catAxis.setCatLabels(variable);
			super.doChangeVariable(g, key);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < 0 || y < 0 || x >= getSize().width || y >= getSize().height)
			return null;
		
		Point hitPos = translateFromScreen(x, y, null);
		int nearestCat = catAxis.positionToCatVal(hitPos.x);
		int nearestX = catAxis.catValToPosition(nearestCat);
		
		return new CatPosInfo(nearestCat, hitPos.x >= nearestX);
	}
}
package structure;

import java.awt.*;

import dataView.*;
import valueList.*;
import images.*;


public class BodyDimensionsView extends DataView {
	
	static final private int kBodyWidth = 288;
	static final private int kBodyHeight = 289;
	static final private int kMaxWait = 30000;		//		30 seconds
	
	static final private int kWhiteBorder = 3;
	
	static final private Color kVarHiliteColor = new Color(0x66FFCC);		//		pale green
	
	private Image bodyPict;
	private ScrollValueList theList;
	
	private int selectedVarIndex = -1;
	
	private String bodyPictFile;
	
	private int fatXCentre, fatYTop;
	private int ageXLeft, ageYCentre;
	private int htXRight, htYCentre;
	private int chestXLeft, chestYTop;
	private int waistXLeft, waistYCentre;
	private int thighXLeft, thighYCentre;
	private int kneeXLeft, kneeYTop;
	
	private Rectangle hitRect[] = new Rectangle[7];
	private Rectangle highlightRect[] = new Rectangle[7];
	
	public BodyDimensionsView(DataSet theData, XApplet applet, ScrollValueList theList,
																																					int colour) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		
		this.theList = theList;
		
		boolean whiteNotBlack = (colour == PeopleView.WHITE);
		
		bodyPictFile = whiteNotBlack ? "vitruvian.gif" : "athlete.gif";
		
		fatXCentre =   whiteNotBlack ? 49 : 39;
		fatYTop =      whiteNotBlack ? 29 : 26;
		ageXLeft =     whiteNotBlack ? 210 : 190;
		ageYCentre =   whiteNotBlack ? 29 : 28;
		htXRight =     92;
		htYCentre =    whiteNotBlack ? 155 : 152;
		chestXLeft =   whiteNotBlack ? 175 : 176;
		chestYTop =    whiteNotBlack ? 113 : 112;
		waistXLeft =   whiteNotBlack ? 172 : 170;
		waistYCentre = whiteNotBlack ? 145 : 144;
		thighXLeft =   whiteNotBlack ? 177 : 173;
		thighYCentre = whiteNotBlack ? 186 : 184;
		kneeXLeft =    whiteNotBlack ? 171 : 164;
		kneeYTop =     whiteNotBlack ? 217 : 211;
	
		hitRect[0] = whiteNotBlack ? new Rectangle(10, 3, 79, 42)
										: new Rectangle(0, 0, 79, 42);			//		fat
		hitRect[1] = whiteNotBlack ? new Rectangle(173, 1, 79, 49)
										: new Rectangle(153, 0, 79, 49);		//		age
		hitRect[2] = whiteNotBlack ? new Rectangle(49, 46, 57, 243)
										: new Rectangle(49, 43, 57, 243);		//		ht
		hitRect[3] = whiteNotBlack ? new Rectangle(107, 96, 128, 31)
										: new Rectangle(108, 95, 128, 31);		//		chest
		hitRect[4] = whiteNotBlack ? new Rectangle(111, 131, 120, 32)
										: new Rectangle(109, 130, 120, 32);		//		waist
		hitRect[5] = whiteNotBlack ? new Rectangle(129, 170, 109, 30)
										: new Rectangle(125, 168, 109, 30);		//		thigh
		hitRect[6] = whiteNotBlack ? new Rectangle(134, 205, 100, 31)
										: new Rectangle(127, 199, 100, 31);		//		knee
		
		highlightRect[0] = whiteNotBlack ? new Rectangle(13, 6, 75, 26)
										: new Rectangle(3, 3, 75, 26);			//		fat
		highlightRect[1] = whiteNotBlack ? new Rectangle(175, 2, 35, 47)
										: new Rectangle(155, 1, 35, 47);		//		age
		highlightRect[2] = whiteNotBlack ? new Rectangle(89, 50, 14, 239)
										: new Rectangle(89, 47, 14, 239);		//		ht
		highlightRect[3] = whiteNotBlack ? new Rectangle(108, 100, 71, 24)
										: new Rectangle(109, 99, 71, 24);		//		chest
		highlightRect[4] = whiteNotBlack ? new Rectangle(114, 136, 61, 23)
										: new Rectangle(112, 135, 61, 23);		//		waist
		highlightRect[5] = whiteNotBlack ? new Rectangle(133, 174, 49, 22)
										: new Rectangle(129, 172, 49, 22);		//		thigh
		highlightRect[6] = whiteNotBlack ? new Rectangle(138, 209, 40, 24)
										: new Rectangle(131, 203, 40, 24);		//		knee
	
		
		MediaTracker tracker = new MediaTracker(applet);
		bodyPict = CoreImageReader.getImage(bodyPictFile);
		tracker.addImage(bodyPict, 0);
		try {
			tracker.waitForAll(kMaxWait);
		} catch (InterruptedException e) {
		}
		setFont(applet.getStandardBoldFont());
		setForeground(Color.red);
		lockBackground(Color.white);
	}
	
	private void drawValue(Graphics g, String key, int index, String units, int offset,
																		boolean selected, int x, int top) {
														//		offset = 0  -> left justified
														//		offset = 1  -> centred
														//		offset = 2  -> right justified
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		
		Value val = ((NumVariable)getVariable(key)).valueAt(index);
		String valString = val.toString() + units;
		int valWidth = fm.stringWidth(valString);
		g.setColor(selected ? kVarHiliteColor : Color.white);
		int valueStart = x - (valWidth * offset) / 2;
		g.fillRect(valueStart - kWhiteBorder, top - kWhiteBorder, valWidth + 2 * kWhiteBorder,
																								ascent + 2 * kWhiteBorder);
		g.setColor(getForeground());
		g.drawString(valString, valueStart, top + ascent);
	}
	
	public void paintView(Graphics g) {
		if (selectedVarIndex >= 0) {
			g.setColor(kVarHiliteColor);
			Rectangle r = highlightRect[selectedVarIndex];
			g.fillRect(r.x, r.y, r.width, r.height);
			g.setColor(getForeground());
		}
		
		g.drawImage(bodyPict, 0, 0, this);
		
		int selIndex = getSelection().findSingleSetFlag();
		if (selIndex >= 0) {
			FontMetrics fm = g.getFontMetrics();
			int ascent = fm.getAscent();
			
			drawValue(g, "fat", selIndex, "%", 1, (selectedVarIndex == 0), fatXCentre, fatYTop);
			drawValue(g, "age", selIndex, " yrs", 0, (selectedVarIndex == 1), ageXLeft, ageYCentre - ascent / 2);
			drawValue(g, "ht", selIndex, " ins", 2, (selectedVarIndex == 2), htXRight, htYCentre - ascent / 2);
			drawValue(g, "chest", selIndex, " cm", 0, (selectedVarIndex == 3), chestXLeft, chestYTop);
			drawValue(g, "waist", selIndex, " cm", 0, (selectedVarIndex == 4), waistXLeft, waistYCentre - ascent / 2);
			drawValue(g, "thigh", selIndex, " cm", 0, (selectedVarIndex == 5), thighXLeft, thighYCentre - ascent / 2);
			drawValue(g, "knee", selIndex, " cm", 0, (selectedVarIndex == 6), kneeXLeft, kneeYTop);
		}
	}
		
	public Dimension getMinimumSize() {
		return new Dimension(kBodyWidth, kBodyHeight);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		for (int i=0 ; i<hitRect.length ; i++)
			if (hitRect[i].contains(x, y))
				return new IndexPosInfo(i);
		
		return null;
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		doDrag(null, startInfo);
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		int hitVarIndex = (toPos == null) ? -1 : ((IndexPosInfo)toPos).itemIndex;
		selectedVarIndex = hitVarIndex;
		repaint();
		theList.setSelectedCols(hitVarIndex, -1);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
	}
}
	

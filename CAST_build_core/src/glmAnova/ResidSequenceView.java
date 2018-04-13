package glmAnova;

import java.awt.*;

import dataView.*;



public class ResidSequenceView extends DataView {
//	static public final String RESID_SEQ_VIEW = "ResidSeq";
	
	static public final int FITTED_VALUES = 0;
	static public final int RESIDUALS = 1;
	
	static final private Color kLightGreyBackground = new Color(0xDDDDDD);
	static final private Color kDimTextColor = new Color(0x999999);
	static final private Color kSelectedSsqColor = new Color(0xFFFF66);		//	pale yellow
	
	static final private LabelValue kSsqLabel = new LabelValue("Ssq");
	
	static final private int kMinusWidth = 10;
	
//	static final private LabelValue kFit0Label = new LabelValue("y\u0302(0) = y\u0304");
																																			//	yHat(0) = yBar
	
	static final private int kTopBorderGaps = 2;
	static final private int kLeftBorderGaps = 4;
	static final private int kBottomBorderGaps = 7;
	static final private int kRightBorderGaps = 6;
	
	static final private int kMaxRowGap = 4;
	static final private int kBestColumnGap = 6;
	
	private LabelValue kDifferenceLabel;
	
	private String fitKey[];
	private String residKey[];
	private String xKey[];
	
	private NumValue maxSsq;
	
	private int valueDisplayType = FITTED_VALUES;
	private int selectedCol = -1;
	
	private int ascent, descent;
	
	private int topBorder, bottomBorder, leftBorder, rightBorder;
	private int noOfRows, noOfFitCols;
	private int maxValueWidth, maxSsqWidth, maxColumnWidth;
	
	private boolean initialised = false;
	
	public ResidSequenceView(DataSet theData, XApplet applet, String[] fitKey,
																		String[] residKey, String[] xKey, NumValue maxSsq) {
		super(theData, applet, new Insets(0,0,0,0));
		this.fitKey = fitKey;
		this.residKey = residKey;
		this.xKey = xKey;
		this.maxSsq = maxSsq;
		kDifferenceLabel = new LabelValue(applet.translate("difference"));
	}
	
	public void setValueDisplayType(int valueDisplayType) {
		this.valueDisplayType = valueDisplayType;
	}
	
	public void setSelectedColumn(int selectedCol) {
		this.selectedCol = selectedCol;
	}
	
	private int getMaxHeadingWidth(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		int width = 0;
		for (int i=0 ; i<fitKey.length ; i++) {
			NumVariable fitVar = (NumVariable)getVariable(fitKey[i]);
			int fitNameLength = fm.stringWidth(fitVar.name);
			width = Math.max(width, fitNameLength);
			
			NumVariable residVar = (NumVariable)getVariable(residKey[i]);
			int residNameLength = fm.stringWidth(residVar.name);
			width = Math.max(width, residNameLength);
		}
		return width;
	}
	
	protected void doInitialisation(Graphics g) {
		ascent = g.getFontMetrics().getAscent();
		descent = g.getFontMetrics().getDescent();
		
		noOfRows = Integer.MAX_VALUE;
		maxValueWidth = 0;
		for (int i=0 ; i<fitKey.length ; i++) {
			NumVariable fitVar = (NumVariable)getVariable(fitKey[i]);
			noOfRows = Math.min(noOfRows, fitVar.noOfValues());
			maxValueWidth = Math.max(maxValueWidth, fitVar.getMaxWidth(g));
		}
		maxSsqWidth = maxSsq.stringWidth(g);
		
		noOfFitCols = fitKey.length;
		
		topBorder = 2 * kTopBorderGaps + ascent + descent;
		leftBorder = 2 * kLeftBorderGaps + kSsqLabel.stringWidth(g);
		bottomBorder = 2 * kBottomBorderGaps + ascent;
		rightBorder = 2 * kRightBorderGaps + Math.max(kDifferenceLabel.stringWidth(g),
																																			maxValueWidth);
		
		maxColumnWidth = Math.max(maxSsqWidth, Math.max(maxValueWidth, getMaxHeadingWidth(g)));
	}
	
	final protected boolean initialise(Graphics g) {
		if (initialised)
			return false;
		else {
			doInitialisation(g);
			initialised = true;
			return true;
		}
	}
	
	private int getRowGap(Graphics g) {
		Dimension bestDim = getMinimumSize(g);
		if (bestDim.height > getSize().height) {
			int difference = getSize().height - topBorder - bottomBorder - noOfRows * ascent;
			int bestRowGap = difference / (noOfRows + 1);
			if (difference < 0)
				bestRowGap --;
			
			return bestRowGap;
		}
		else
			return kMaxRowGap;
	}
	
	private int getColGap(Graphics g) {
		int difference = getSize().width - leftBorder - rightBorder - noOfFitCols * maxColumnWidth;
		int bestColGap = difference / (noOfFitCols + 1);
		if (difference < 0)
			bestColGap --;
		
		return bestColGap;
	}
	
	private void drawBackground(Graphics g, int selectedCol, int colGap, int rowGap) {
		int contentWidth = noOfFitCols * maxColumnWidth + (noOfFitCols + 1) * colGap;
		int contentHeight = noOfRows * ascent + (noOfRows + 1) * rowGap;
		
		g.setColor(selectedCol >= 0 ? kDimTextColor : getForeground());
		g.drawLine(leftBorder, topBorder, leftBorder + contentWidth, topBorder);
		g.drawLine(leftBorder, topBorder + contentHeight, leftBorder + contentWidth,
																															topBorder + contentHeight);
		
		Color mainBackColor = (selectedCol < 0) ? Color.white : kLightGreyBackground;
		g.setColor(mainBackColor);
		
		g.fillRect(leftBorder, topBorder + 1, contentWidth, contentHeight - 1);
		
		if (selectedCol >= 0) {
			int left = leftBorder + selectedCol * (maxColumnWidth + colGap);
			int width = 2 * maxColumnWidth + 3 * colGap;
			g.setColor(Color.white);
			g.fillRect(left, topBorder + 1, width, contentHeight - 1);
			g.setColor(Color.red);
			g.drawRect(left, topBorder, width, contentHeight);
		}
	}
	
	private void drawHeading(NumVariable theVar, int colCenter, int headingBaseline,
																											LabelValue tempLabel, Graphics g) {
		tempLabel.label = theVar.name;
		tempLabel.drawCentred(g, colCenter, headingBaseline);
	}
	
	private void drawColumnValues(NumVariable theVar, int valuesRight, int rowBaseline,
																													int rowGap, Graphics g) {
		for (int i=0 ; i<noOfRows ; i++) {
			theVar.valueAt(i).drawLeft(g, valuesRight, rowBaseline);
			rowBaseline += (ascent + rowGap);
		}
	}
	
	private void drawSsqBackground(int ssqRight, int ssqBaseline, Graphics g) {
		g.setColor(kSelectedSsqColor);
		g.fillRect(ssqRight - maxSsqWidth - 4, ssqBaseline - ascent - 2, maxSsqWidth + 8, ascent + 4);
		g.setColor(Color.red);
		g.drawRect(ssqRight - maxSsqWidth - 5, ssqBaseline - ascent - 3, maxSsqWidth + 9, ascent + 5);
	}
	
	private void drawSsq(NumVariable residVar, int ssqRight, int ssqBaseline, Graphics g) {
		ValueEnumeration re = residVar.values();
		double srr = 0.0;
		while (re.hasMoreValues()) {
			double r = re.nextDouble();
			srr += r * r;
		}
		NumValue srrValue = new NumValue(srr, maxSsq.decimals);
		srrValue.drawLeft(g, ssqRight, ssqBaseline);
	}
	
	private void drawDiffHeading(int diffCenter, int headingBaseline, Graphics g) {
		kDifferenceLabel.drawCentred(g, diffCenter, headingBaseline);
	}
	
	private int drawDiffBackground(int diffCenter, int colGap, int rowGap, Graphics g) {
//		Color mainBackColor = (selectedCol < 0) ? Color.white : kLightGreyBackground;
		g.setColor(Color.white);
		
		int diffWidth = (rightBorder + maxValueWidth) / 2;
		int diffLeft = diffCenter - diffWidth / 2;
		
		int contentHeight = noOfRows * ascent + (noOfRows + 1) * rowGap;
		
		g.fillRect(diffLeft, topBorder, diffWidth, contentHeight);
		
		g.setColor(Color.red);
		g.drawRect(diffLeft, topBorder, diffWidth, contentHeight);
		
		return diffLeft;
	}
	
	private void drawDiffValues(NumVariable var1, NumVariable var2, int diffValuesRight,
																						int rowBaseline, int rowGap, Graphics g) {
		NumValue tempVal = new NumValue(0.0, Math.max(var1.getMaxDecimals(), var2.getMaxDecimals()));
		for (int i=0 ; i<noOfRows ; i++) {
			tempVal.setValue(var1.doubleValueAt(i) - var2.doubleValueAt(i));
			tempVal.drawLeft(g, diffValuesRight, rowBaseline);
			rowBaseline += (ascent + rowGap);
		}
	}
	
	private void drawDiffSsq(NumVariable var1, NumVariable var2, int ssqRight, int ssqBaseline,
																																								Graphics g) {
		ValueEnumeration v1e = var1.values();
		ValueEnumeration v2e = var2.values();
		double sdd = 0.0;
		while (v1e.hasMoreValues() && v2e.hasMoreValues()) {
			double d = v1e.nextDouble() - v2e.nextDouble();
			sdd += d * d;
		}
		NumValue sddValue = new NumValue(sdd, maxSsq.decimals);
		sddValue.drawLeft(g, ssqRight, ssqBaseline);
	}
	
	private void drawMinusEquals(int vertCenter, int colGap, int diffLeft, Graphics g) {
		g.setColor(Color.red);
		int selectionLeft = leftBorder + selectedCol * (maxColumnWidth + colGap);
		int selectionRight = selectionLeft + 2 * maxColumnWidth + 3 * colGap;
		
		int equalsLeft = (selectionRight * 2 + diffLeft) / 3;
		int equalsRight = (selectionRight + diffLeft * 2) / 3;
//		int equalsCenter = (selectionRight + diffLeft) / 2;
		for (int j=-2 ; j<2 ; j+=3)
			for (int i=0 ; i<2 ; i++)
				g.drawLine(equalsLeft, vertCenter + j - i, equalsRight + kMinusWidth / 2, vertCenter + j - i);
			
		int minusCenter = (selectionLeft + selectionRight) / 2;
		for (int i=0 ; i<2 ; i++)
			g.drawLine(minusCenter - kMinusWidth / 2, vertCenter - i,
																						minusCenter + kMinusWidth / 2, vertCenter - i);
	}
	
	private void drawArrow(String plusXString, int betweenColPos, int vertCenter, Graphics g) {
		Font oldFont = g.getFont();
		g.setFont(new Font(oldFont.getName(), Font.BOLD, oldFont.getSize()));
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		
		int plusXWidth = fm.stringWidth(plusXString);
		
		int xCoord[] = new int[8];
		xCoord[0] = xCoord[6] = xCoord[7] = betweenColPos - plusXWidth / 2 - 6;
		xCoord[1] = xCoord[2] = xCoord[4] = xCoord[5] = betweenColPos + plusXWidth / 2 - 2;
		xCoord[3] = betweenColPos + plusXWidth / 2 + 6;
		
		int yCoord[] = new int[8];
		yCoord[0] = yCoord[1] = yCoord[7] = vertCenter - ascent / 2 - 3;
		yCoord[2] = vertCenter - ascent / 2 - 9;
		yCoord[3] = vertCenter;
		yCoord[4] = vertCenter + ascent / 2 + 9;
		yCoord[5] = yCoord[6] = vertCenter + ascent / 2 + 3;
		
		g.setColor(Color.yellow);
		g.fillPolygon(xCoord, yCoord, 8);
		
		g.setColor(Color.red);
		g.drawPolygon(xCoord, yCoord, 8);
		
		g.setColor(getForeground());
		g.drawString(plusXString, betweenColPos - plusXWidth / 2 - 2, vertCenter + ascent / 2);
		
		g.setFont(oldFont);
	}
	
	private NumVariable getColumnVariable(int colIndex) {
		String varKey = (valueDisplayType == FITTED_VALUES) ? fitKey[colIndex] : residKey[colIndex];
		NumVariable theVar = (NumVariable)getVariable(varKey);
		return theVar;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		int rowGap = getRowGap(g);
		int colGap = getColGap(g);
		
		drawBackground(g, selectedCol, colGap, rowGap);
		
		int headingBaseline = kTopBorderGaps + ascent;
		int ssqBaseline = topBorder + noOfRows * ascent + (noOfRows + 1) * rowGap
																														+ kBottomBorderGaps + ascent;
		int firstRowBaseline = topBorder + rowGap + ascent;
		
		if (valueDisplayType == RESIDUALS) {
			g.setColor(getForeground());
			kSsqLabel.drawRight(g, kLeftBorderGaps, ssqBaseline);
		}
		
		int colCenter = leftBorder + colGap + maxColumnWidth / 2;
		LabelValue tempLabel = new LabelValue("");
		
		for (int i=0 ; i<noOfFitCols ; i++) {
			boolean isDimColumn = (selectedCol >= 0) && (selectedCol != i) && (selectedCol != i-1);
			g.setColor(isDimColumn ? kDimTextColor : getForeground());
			
			NumVariable theVar = getColumnVariable(i);
			
			drawHeading(theVar, colCenter, headingBaseline, tempLabel, g);
			
			int valuesRight = colCenter + theVar.getMaxWidth(g) / 2;
			drawColumnValues(theVar, valuesRight, firstRowBaseline, rowGap, g);
			
			if (valueDisplayType == RESIDUALS) {
				int ssqRight = colCenter + maxSsqWidth / 2;
				
				if (selectedCol >= 0 && !isDimColumn)
					drawSsqBackground(ssqRight, ssqBaseline, g);
				
				drawSsq(theVar, ssqRight, ssqBaseline, g);
			}
			
			colCenter += (colGap + maxColumnWidth);
		}
		
		int contentHeight = noOfRows * ascent + (noOfRows + 1) * rowGap;
		int vertCenter = topBorder + contentHeight / 2;
		
		if (selectedCol >= 0) {
			g.setColor(getForeground());
			int diffCenter = leftBorder + (noOfFitCols + 1) * colGap + noOfFitCols * maxColumnWidth
																																							+ rightBorder / 2;
			drawDiffHeading(diffCenter, headingBaseline, g);
			int diffLeft = drawDiffBackground(diffCenter, colGap, rowGap, g);
			
			g.setColor(getForeground());
			NumVariable var1 = getColumnVariable(selectedCol);
			NumVariable var2 = getColumnVariable(selectedCol + 1);
			int diffValuesRight = diffCenter + maxValueWidth / 2;
			drawDiffValues(var1, var2, diffValuesRight, firstRowBaseline, rowGap, g);
		
			drawMinusEquals(vertCenter, colGap, diffLeft, g);
			
			int ssqRight = diffCenter + maxSsqWidth / 2;
			drawSsqBackground(ssqRight, ssqBaseline, g);
			drawDiffSsq(var1, var2, ssqRight, ssqBaseline, g);
			
			if (valueDisplayType == RESIDUALS) {
				int ssqVertCenter = ssqBaseline - ascent / 2;
				drawMinusEquals(ssqVertCenter, colGap, diffLeft, g);
			}
			else {
				LabelValue tempSsqLabel = new LabelValue(kSsqLabel.label + " =");
				int diffBoxWidth = (rightBorder + maxValueWidth) / 2;
				int diffBoxLeft = diffCenter - diffBoxWidth / 2;
				tempSsqLabel.drawLeft(g, diffBoxLeft - 4 , ssqBaseline);
			}
		}
		else {
			int betweenColPos = leftBorder + colGap + maxColumnWidth + colGap / 2;
			for (int i=0 ; i<xKey.length ; i++) {
				drawArrow("+" + xKey[i], betweenColPos, vertCenter, g);
				betweenColPos += (colGap + maxColumnWidth);
			}
			drawArrow("Y", betweenColPos, vertCenter, g);
		}
	}

//-----------------------------------------------------------------------------------
	
	private Dimension getMinimumSize(Graphics g) {
		initialise(g);
		
		int bestWidth = leftBorder + rightBorder + noOfFitCols * maxColumnWidth
																							+ (noOfFitCols + 1) * kBestColumnGap;
		int bestHeight = topBorder + bottomBorder + noOfRows * ascent
																							+ (noOfRows + 1) * kMaxRowGap;
		
		return new Dimension(bestWidth, bestHeight);
	}
	
	public Dimension getMinimumSize() {
		return getMinimumSize(getGraphics());
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

//-----------------------------------------------------------------------------------
	
//	private boolean doingDrag = false;
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		Graphics g = getGraphics();
		int rowGap = getRowGap(g);
		int colGap = getColGap(g);
		int contentHeight = noOfRows * ascent + (noOfRows + 1) * rowGap;
		if (y <= topBorder || y >= topBorder + contentHeight)
			return null;
		
		int left = leftBorder + colGap + maxColumnWidth / 2;
		int hitLeftIndex = (x - left) / (colGap + maxColumnWidth);
		if (hitLeftIndex >= noOfFitCols - 1)
			return null;
		else
			return new IndexPosInfo(hitLeftIndex);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		doingDrag = true;
		selectedCol = ((IndexPosInfo)startInfo).itemIndex;
		repaint();
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null)
			selectedCol = -1;
		else
			selectedCol = ((IndexPosInfo)toPos).itemIndex;
		repaint();
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		doingDrag = true;
		selectedCol = -1;
		repaint();
	}
}
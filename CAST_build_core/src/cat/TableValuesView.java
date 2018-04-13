package cat;

import java.awt.*;

import dataView.*;
import images.*;


public class TableValuesView extends DataView {
	
	static final public int UNITS = 0;
	static final public int THOUSANDS = 1;
	static final public int MILLIONS = 2;
	static final public int CUSTOM = 3;
	
	static final private int kNameTableHorizGap = 12;
	static final private int kHeadingTableGap = 4;
	static final private int kHeadingRowGap = 2;
	static final private int kTableVertBorder = 6;
	static final private int kTableHorizBorder = 10;
	static final protected int kRowSpacing = 6;
	static final private int kTotalGap = 5;
	static final private int kTableColGap = 20;
	
	static final private int kPlusMinusTableGap = 6;
	static final private int kPlusMinusWidth = 47;
	static final private int kPlusMinusHeight = 16;
	
	static final private LabelValue kThousands = new LabelValue("(thousands)");
	static final private LabelValue kMillions = new LabelValue("(millions)");
	
	static final private int kMaxWait = 30000;		//		30 seconds
	
	static private Image plusMinus[][] = new Image[3][3];
	static private boolean loadedImages = false;
	
	static private String[] stringToArray(String s) {
		String[] array = new String[1];
		array[0] = s;
		return array;
	}
	
	static private int[] intToArray(int i) {
		int[] array = new int[1];
		array[0] = i;
		return array;
	}
	
	private String labelKey;
	private String yKey[];
	private int yDisplayColumns;
	
	private boolean hasTotalRow = true;
	private LabelValue customUnits[];
	
	private int[] units;
	private int[] minShift, maxShift, currentShift;
	
	private boolean initialised = false;
	
	private int maxLabelWidth, labelNameWidth;
	private int[] yNameWidth, yColumnWidth;
	protected int nRows;
	private int boldAscent, boldDescent;
	
	private int tableTopBorder, tableLeftBorder, tableBottomBorder, tableHeight, tableWidth;
	
	private boolean hitMinusNotPlus;
	private int hitColumn = -1;
	private int selectedIndex = -1;
	
	public TableValuesView(DataSet theData, XApplet applet,
													String labelKey, String[] yKey, int[] minShift, int[] maxShift) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.labelKey = labelKey;
		this.yKey = yKey;
		yDisplayColumns = yKey.length;
		this.minShift = minShift;
		this.maxShift = maxShift;
		currentShift = (int[])minShift.clone();
		
		units = new int[yKey.length];
		for (int i=0 ; i<yKey.length ; i++)
			units[i] = UNITS;
		
		if (!loadedImages) {
			MediaTracker tracker = new MediaTracker(applet);
			plusMinus[0][1] = CoreImageReader.getImage("tableDigits/plusMinus01.gif");
			plusMinus[0][2] = CoreImageReader.getImage("tableDigits/plusMinus02.gif");
			plusMinus[1][0] = CoreImageReader.getImage("tableDigits/plusMinus10.gif");
			plusMinus[2][0] = CoreImageReader.getImage("tableDigits/plusMinus20.gif");
			plusMinus[1][1] = CoreImageReader.getImage("tableDigits/plusMinus11.gif");
			plusMinus[1][2] = CoreImageReader.getImage("tableDigits/plusMinus12.gif");
			plusMinus[2][1] = CoreImageReader.getImage("tableDigits/plusMinus21.gif");
			
			for (int i=0 ; i<3 ; i++)
				for (int j=0 ; j<3 ; j++)
					if (plusMinus[i][j] != null)
						tracker.addImage(plusMinus[i][j], 0);
			try {
				tracker.waitForAll(kMaxWait);
			} catch (InterruptedException e) {
			}
			loadedImages = true;
		}
	}
	
	public TableValuesView(DataSet theData, XApplet applet, String labelKey,
																			String yKey, int minShift, int maxShift) {
		this(theData, applet, labelKey, stringToArray(yKey), intToArray(minShift), intToArray(maxShift));
	}
	
	public void setYDisplayColumns(int yDisplayColumns) {
		this.yDisplayColumns = yDisplayColumns;
	}
	
	public void setHasTotalRow(boolean hasTotalRow) {
		this.hasTotalRow = hasTotalRow;
	}
	
	public void setCustomUnits(int varIndex, String unitsString) {
		if (customUnits == null)
			customUnits = new LabelValue[yKey.length];
		units[varIndex] = CUSTOM;
		customUnits[varIndex] = new LabelValue(unitsString);
	}
	
	protected void initialise(Graphics g) {
		if (!initialised) {
			doInitialisation(g);
			initialised = true;
		}
	}
	
	protected void doInitialisation(Graphics g) {
		Font oldFont = g.getFont();
		g.setFont(new Font(oldFont.getName(), Font.BOLD, oldFont.getSize()));
		
		FontMetrics fm = g.getFontMetrics();
		boldAscent = fm.getAscent();
		boldDescent = fm.getDescent();
		
		yNameWidth = new int[yKey.length];
		yColumnWidth = new int[yKey.length];
		for (int i=0 ; i<yKey.length ; i++) {
			Variable yVar = (Variable)getVariable(yKey[i]);
			yNameWidth[i] = fm.stringWidth(yVar.name);
			int maxValueWidth = yVar.getMaxWidth(g);
			yColumnWidth[i] = Math.max(yNameWidth[i], maxValueWidth);
		}
		
		Variable labelVar = (Variable)getVariable(labelKey);
		labelNameWidth = fm.stringWidth(labelVar.name);
		
		g.setFont(oldFont);
		
		maxLabelWidth = 0;
		ValueEnumeration le = labelVar.values();
		nRows = 0;
		while (le.hasMoreValues()) {
			maxLabelWidth = Math.max(maxLabelWidth, le.nextValue().stringWidth(g));
			nRows ++;
		}
		
		tableTopBorder = 2 * (boldAscent + boldDescent) + kHeadingRowGap + kHeadingTableGap;
		tableLeftBorder = Math.max(maxLabelWidth, labelNameWidth) + kNameTableHorizGap;
		tableBottomBorder = kPlusMinusTableGap + kPlusMinusHeight;
		
		tableHeight = nRows * (NumImageValue.kDigitAscent + NumImageValue.kDigitDescent)
													+ (nRows - 1) * kRowSpacing + 2 * kTableVertBorder;
		if (hasTotalRow)
			tableHeight += kTotalGap;
		tableWidth = 2 * kTableHorizBorder + (yKey.length - 1) * kTableColGap;
		for (int i=0 ; i<yKey.length ; i++)
			tableWidth += yColumnWidth[i];
	}
	
	protected void drawLabels(Graphics g, Variable labelVar, int topBaseline, Font boldFont) {
		int baseline = topBaseline;
		for (int row=0 ; row<nRows ; row++) {
			if (hasTotalRow && row == nRows - 1) {
				baseline += kTotalGap;
				g.setFont(boldFont);
				g.setColor(Color.black);
			}
			
//			int valIndex = (sortIndex == null) ? row : sortIndex[row];
			labelVar.valueAt(row).drawRight(g, 0, baseline);
			
			baseline += (NumImageValue.kDigitAscent + NumImageValue.kDigitDescent + kRowSpacing);
		}
	}
	
	private void drawLabelColumn(Graphics g, Variable labelVar, Font stdFont, Font boldFont) {
		g.setFont(boldFont);
		g.setColor(Color.black);
		int baseline = 2 * boldAscent + boldDescent + kHeadingRowGap;
		g.drawString(labelVar.name, 0, baseline);
		
		g.setFont(stdFont);
		g.setColor(getForeground());
		baseline = tableTopBorder + kTableVertBorder + NumImageValue.kDigitAscent;
		
		drawLabels(g, labelVar, baseline, boldFont);
		
		g.setFont(stdFont);
	}
	
	protected void drawValues(Graphics g, Variable yVar, int lineLeft, int lineRight, int topBaseline,
																																	int yValueRight, Font boldFont) {
		int baseline = topBaseline;
		for (int row=0 ; row<nRows ; row++) {
			if (hasTotalRow && row == nRows - 1) {
				baseline += kTotalGap;
				int lineVert = baseline - NumImageValue.kDigitAscent - (kRowSpacing + kTotalGap + NumImageValue.kDigitDescent) / 2;
				g.setColor(Color.lightGray);
				g.drawLine(lineLeft, lineVert, lineRight, lineVert);
//				g.drawLine(tableLeftBorder, lineVert, tableLeftBorder + tableWidth - 1, lineVert);
				g.setColor(Color.black);
				g.setFont(boldFont);
			}
			
//			int valIndex = (sortIndex == null) ? row : sortIndex[row];
			yVar.valueAt(row).drawLeft(g, yValueRight, baseline);
			
			baseline += (NumImageValue.kDigitAscent + NumImageValue.kDigitDescent + kRowSpacing);
		}
	}
	
	private void drawValueColumn(Graphics g, Variable yVar, int colLeft, int yIndex,
																												Font stdFont, Font boldFont) {
		g.setFont(boldFont);
		g.setColor(Color.black);
		int colCentre = colLeft + yColumnWidth[yIndex] / 2;
		int baseline = boldAscent;
		if (units[yIndex] != UNITS)
			g.drawString(yVar.name, colCentre - yNameWidth[yIndex] / 2, baseline);
		baseline += boldAscent + boldDescent + kHeadingRowGap;
		if (units[yIndex] == UNITS)
			g.drawString(yVar.name, colCentre - yNameWidth[yIndex] / 2, baseline);
		
		g.setFont(stdFont);
		
		if (units[yIndex] == THOUSANDS)
			kThousands.drawCentred(g, colCentre, baseline);
		else if (units[yIndex] == MILLIONS)
			kMillions.drawCentred(g, colCentre, baseline);
		else if (units[yIndex] == CUSTOM)
			customUnits[yIndex].drawCentred(g, colCentre, baseline);
		
		g.setColor(getForeground());
		int maxYWidth = yVar.getMaxWidth(g);
		
		baseline = tableTopBorder + kTableVertBorder + NumImageValue.kDigitAscent;
		int yValueRight = colLeft + yColumnWidth[yIndex] - (yColumnWidth[yIndex] - maxYWidth) / 2;
		
		int leftExtra = (yIndex == 0) ? kTableHorizBorder : kTableColGap;
		int rightExtra = (yIndex == yDisplayColumns - 1) ? kTableHorizBorder : kTableColGap;
		drawValues(g, yVar, colLeft - leftExtra, colLeft + yColumnWidth[yIndex] + rightExtra - 1,
																														baseline, yValueRight, boldFont);
		g.setFont(stdFont);
		
		if (minShift[yIndex] < maxShift[yIndex])  {
			int minusHiliteIndex = (currentShift[yIndex] == maxShift[yIndex]) ? 0 : 1;
			int plusHiliteIndex = (currentShift[yIndex] == minShift[yIndex]) ? 0 : 1;
			if (hitColumn == yIndex) {
				if (selectedIndex == 0)
					minusHiliteIndex = 2;
				else if (selectedIndex == 1)
					plusHiliteIndex = 2;
			}
			g.drawImage(plusMinus[minusHiliteIndex][plusHiliteIndex], colCentre - kPlusMinusWidth / 2,
																			tableTopBorder + tableHeight + kPlusMinusTableGap, this);
		}
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		g.setColor(Color.white);
		int usedTableWidth = 2 * kTableHorizBorder + (yDisplayColumns - 1) * kTableColGap;
		for (int i=0 ; i<yDisplayColumns ; i++)
			usedTableWidth += yColumnWidth[i];
		g.fillRect(tableLeftBorder, tableTopBorder, usedTableWidth, tableHeight);
		
		Font stdFont = g.getFont();
		Font boldFont = new Font(stdFont.getName(), Font.BOLD, stdFont.getSize());
		g.setColor(getForeground());
		
		Variable labelVar = (Variable)getVariable(labelKey);
		drawLabelColumn(g, labelVar, stdFont, boldFont);
		
		int colStart = tableLeftBorder + kTableHorizBorder;
		for (int i=0 ; i<yDisplayColumns ; i++) {
			Variable yVar = (Variable)getVariable(yKey[i]);
			drawValueColumn(g, yVar, colStart, i, stdFont, boldFont);
			colStart += kTableColGap + yColumnWidth[i];
		}
	}
	
	private void changeDecimals(int yIndex, boolean minusNotPlus) {
		if (minusNotPlus)
			currentShift[yIndex] ++;
		else
			currentShift[yIndex] --;
		ShiftedVariable yVar = (ShiftedVariable)getVariable(yKey[yIndex]);
		yVar.setHiddenDigits(currentShift[yIndex]);
		double displayFactor = yVar.getFactor();
		units[yIndex] = (displayFactor == 1.0) ? TableValuesView.UNITS
																		: (displayFactor > 0.00009) ? TableValuesView.THOUSANDS
																		: TableValuesView.MILLIONS;
	}
	
//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		
		return new Dimension(tableLeftBorder + tableWidth, tableTopBorder + tableHeight + tableBottomBorder);
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		y -= tableTopBorder + tableHeight + kPlusMinusTableGap;
		if (y < 0 || y > kPlusMinusHeight)
			return null;
		
		x -= tableLeftBorder + kTableHorizBorder;
		int colIndex = -1;
		boolean minusNotPlus = false;
		for (int i=0 ; i<yKey.length ; i++) {
			int xInButtons = x - (yColumnWidth[i] - kPlusMinusWidth) / 2;
			if (xInButtons >= -3 && xInButtons < kPlusMinusWidth + 3) {
				colIndex = i;
				minusNotPlus = xInButtons / (kPlusMinusWidth / 2) == 0;
			}
			x -= kTableColGap + yColumnWidth[i];
		}
		
		if (colIndex == -1)
			return null;
		
		if (minusNotPlus && (currentShift[colIndex] == maxShift[colIndex])
													|| !minusNotPlus && (currentShift[colIndex] == minShift[colIndex]))
			return null;
		
		return new CatPosInfo(colIndex, minusNotPlus);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		hitMinusNotPlus = ((CatPosInfo)startInfo).highNotLow;
		hitColumn = ((CatPosInfo)startInfo).catIndex;
		
		selectedIndex = hitMinusNotPlus ? 0 : 1;
		repaint();
		
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null)
			selectedIndex = -1;
		else {
			boolean minusNotPlus = ((CatPosInfo)toPos).highNotLow;
			int column = ((CatPosInfo)toPos).catIndex;
			if (minusNotPlus == hitMinusNotPlus && column == hitColumn)
				selectedIndex = hitMinusNotPlus ? 0 : 1;
			else
				selectedIndex = -1;
		}
		repaint();
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		if (endPos != null) {
			changeDecimals(hitColumn, hitMinusNotPlus);
			selectedIndex = -1;
			hitColumn = -1;
			repaint();
		}
	}
}
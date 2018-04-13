package cat;

import java.awt.*;

import dataView.*;


public class TableCombineView extends DataView {
//	static final public String TABLE_COMBINE_VIEW = "tableCombineView";
	
	static final public int NORMAL = 0;
	static final public int SORT = 1;
	static final public int COMBINE = 2;
	static final public int HIDE = 3;
	
	static final private int kNameTableHorizGap = 12;
	static final private int kHeadingTableGap = 4;
	static final private int kTableVertBorder = 6;
	static final private int kTableHorizBorder = 10;
	static final protected int kRowSpacing = 6;
	static final private int kTotalGap = 5;
	static final private int kTableColGap = 20;
	
	static final public Color kGroupColour[] = {new Color(0x0000AA), new Color(0xAA0000), new Color(0x006600)};
	
	public static final int kEndFrame = 40;
	
	static final private int kPercentDecimals = 1;
	
	private LabelValue kPercentHeading;
	
	private String labelKey;
	private String yKey, groupKey;
	
	private boolean initialised = false;
	
	private int maxLabelWidth, labelNameWidth;
	private int yNameWidth, yColumnWidth, percentColumnWidth;
	protected int nRows;
	private int boldAscent, boldDescent;
	
	private int tableTopBorder, tableLeftBorder, tableWidth;
	
	private int groupDisplay[];
	private int[] startBaseline, endBaseline;
	private int startTableHeight, endTableHeight;
	private boolean[] startGroupCombined, endGroupCombined;
	
	public TableCombineView(DataSet theData, XApplet applet,
													String labelKey, String yKey, String groupKey) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		kPercentHeading = new LabelValue(applet.translate("Percent"));
		this.labelKey = labelKey;
		this.yKey = yKey;
		this.groupKey = groupKey;
	}
	
	public void setGroupDisplay(int group, int displayType) {
		groupDisplay[group] = displayType;
		startBaseline = endBaseline;
		endBaseline = baselinesForDisplay(groupDisplay);
		startTableHeight = endTableHeight;
		endTableHeight = tableHeightForDisplay(groupDisplay);
		startGroupCombined = endGroupCombined;
		endGroupCombined = groupsCombined(groupDisplay);
		animateFrames(1, kEndFrame - 1, 20, null);
	}
	
	public int[] getGroupDisplays() {
		return groupDisplay;
	}
	
	protected void initialise(Graphics g) {
		if (!initialised) {
			doInitialisation(g);
			initialised = true;
		}
	}
	
	protected void doInitialisation(Graphics g) {
		if (!initialised) {
			Font oldFont = g.getFont();
			g.setFont(new Font(oldFont.getName(), Font.BOLD, oldFont.getSize()));
			
			FontMetrics fm = g.getFontMetrics();
			boldAscent = fm.getAscent();
			boldDescent = fm.getDescent();
			
			Variable yVar = (Variable)getVariable(yKey);
			yNameWidth = fm.stringWidth(yVar.name);
			int maxValueWidth = yVar.getMaxWidth(g);
			yColumnWidth = Math.max(yNameWidth, maxValueWidth);
			
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
			
			tableTopBorder = boldAscent + boldDescent + kHeadingTableGap;
			tableLeftBorder = Math.max(maxLabelWidth, labelNameWidth) + kNameTableHorizGap;
			
			percentColumnWidth = kPercentHeading.stringWidth(g);
			
			tableWidth = 2 * kTableHorizBorder + yColumnWidth + kTableColGap + percentColumnWidth;
			
			CatVariable groupVar = (CatVariable)getVariable(groupKey);
			int nGroups = groupVar.noOfCategories();
			groupDisplay = new int[nGroups];		//	default is NORMAL
			startBaseline = endBaseline = baselinesForDisplay(groupDisplay);
			startTableHeight = endTableHeight = tableHeightForDisplay(groupDisplay);
			startGroupCombined = endGroupCombined = new boolean[nGroups];				//	default is false
		}
	}
	
	private boolean[] groupsCombined(int[] groupDisplay) {
		boolean[] combined = new boolean[groupDisplay.length];
		for (int i=0 ; i<groupDisplay.length ; i++)
			combined[i] = groupDisplay[i] == COMBINE || groupDisplay[i] == HIDE;
		return combined;
	}
	
	private int tableHeightForDisplay(int[] groupDisplay) {
		int height = 2 * kTableVertBorder + kTotalGap + NumImageValue.kDigitAscent + NumImageValue.kDigitDescent;
		
		CatVariable groupVar = (CatVariable)getVariable(groupKey);
		int[] counts = groupVar.getCounts();
		for (int group=0 ; group<groupVar.noOfCategories() ; group++)
			if (groupDisplay[group] == NORMAL || groupDisplay[group] == SORT)
				height += counts[group] * (NumImageValue.kDigitAscent + NumImageValue.kDigitDescent + kRowSpacing);
			else if (groupDisplay[group] == COMBINE)
				height += NumImageValue.kDigitAscent + NumImageValue.kDigitDescent + kRowSpacing;
		
		return height;
	}
	
	private int getTableHeight() {
		int frame = getCurrentFrame();
		return (endTableHeight * frame + startTableHeight * (kEndFrame - frame)) / kEndFrame;
	}
	
	private int[] getBaselines() {
		int baseline[] = new int[startBaseline.length];
		int frame = getCurrentFrame();
		for (int i=0 ; i<startBaseline.length ; i++)
			baseline[i] = (endBaseline[i] * frame + startBaseline[i] * (kEndFrame - frame)) / kEndFrame;
		return baseline;
	}

	private int[] rowToValueIndices(NumVariable yVar, CatVariable groupVar, int[] groupDisplay) {
		int n = yVar.noOfValues();
		int[] rowToValueIndex = new int[n];
		for (int i=0 ; i<n ; i++)
			rowToValueIndex[i] = i;
			
		for (int i=0 ; i<n ; i++) {
			int group = groupVar.getItemCategory(rowToValueIndex[i]);
			if (groupDisplay[group] == SORT) {
				int maxIndex = i;
				double maxValue = yVar.doubleValueAt(rowToValueIndex[i]);
				
				for (int j=i+1 ; j<n ; j++)
					if (groupVar.getItemCategory(rowToValueIndex[j]) == group) {
						double yj = yVar.doubleValueAt(rowToValueIndex[j]);
						if (yj > maxValue) {
							maxIndex = j;
							maxValue = yj;
						}
					}
				if (maxIndex != i) {
					int tempIndex = rowToValueIndex[i];
					rowToValueIndex[i] = rowToValueIndex[maxIndex];
					rowToValueIndex[maxIndex] = tempIndex;
				}
			}
		}
		return rowToValueIndex;
	}
	
	private int[] baselinesForDisplay(int[] groupDisplay) {
		NumVariable yVar = (NumVariable)getVariable(yKey);
		CatVariable groupVar = (CatVariable)getVariable(groupKey);
		int baselines[] = new int[nRows + 1];		//	last for total
//		int tableHeight = getTableHeight();
		
		int[] rowValIndex = rowToValueIndices(yVar, groupVar, groupDisplay);
		
		int offTableBaseline = -NumImageValue.kDigitAscent - NumImageValue.kDigitDescent - kRowSpacing;
		
		int baseline = tableTopBorder + kTableVertBorder + NumImageValue.kDigitAscent;
		for (int group=0 ; group<groupDisplay.length ; group++) {
			Value targetCat = groupVar.getLabel(group);
			for (int i=0 ; i<nRows ; i++)
				if (groupVar.valueAt(i) == targetCat) {
					if (groupDisplay[group] == NORMAL || groupDisplay[group] == SORT) {
						baselines[rowValIndex[i]] = baseline;
						baseline += NumImageValue.kDigitAscent + NumImageValue.kDigitDescent + kRowSpacing;
					}
					else if (groupDisplay[group] == COMBINE)
						baselines[rowValIndex[i]] = baseline;
					else if (groupDisplay[group] == HIDE)
						baselines[rowValIndex[i]] = offTableBaseline;
				}
			if (groupDisplay[group] == COMBINE)
				baseline += NumImageValue.kDigitAscent + NumImageValue.kDigitDescent + kRowSpacing;
		}
		baseline += kTotalGap;
		baselines[nRows] = baseline;		//	for total
		
		return baselines;
	}
	
	private void drawLabels(Graphics g, Variable labelVar, int[] baselines,
																											Font stdFont, Font boldFont) {
		g.setFont(boldFont);
		int baseline = boldAscent;
		g.drawString(labelVar.name, 0, baseline);
		g.setFont(stdFont);
		
		CatVariable groupVar = (CatVariable)getVariable(groupKey);
		int[] lastInGroupIndex = new int[groupDisplay.length];
		
		for (int i=0 ; i<nRows ; i++) {
			int group = groupVar.getItemCategory(i);
			lastInGroupIndex[group] = i;
			g.setColor(kGroupColour[group]);
			boolean drawCombined = (getCurrentFrame() == kEndFrame && endGroupCombined[group])
																		|| startGroupCombined[group] && endGroupCombined[group];
			if (!drawCombined)
				labelVar.valueAt(i).drawRight(g, 0, baselines[i]);
		}
		
		for (int group=0 ; group<groupDisplay.length ; group++) {
			boolean drawCombined = (getCurrentFrame() == kEndFrame && endGroupCombined[group])
																		|| startGroupCombined[group] && endGroupCombined[group];
			if (drawCombined) {
				Value groupLabel = groupVar.getLabel(group);
				g.setFont(boldFont);
				g.setColor(kGroupColour[group]);
				groupLabel.drawRight(g, 0, baselines[lastInGroupIndex[group]]);
				g.setFont(stdFont);
			}
		}
		
		g.setFont(boldFont);
		g.setColor(getForeground());
		g.drawString(getApplet().translate("Total"), 0, baselines[nRows]);
		g.setFont(stdFont);
	}
	
	private void drawValueColumn(Graphics g, Variable yVar, int colLeft, int[] baselines,
																							boolean showPercent, Font stdFont, Font boldFont) {
		g.setFont(boldFont);
		int colCentre = colLeft + yColumnWidth / 2;
		int baseline = boldAscent;
		if (showPercent)
			kPercentHeading.drawCentred(g, colCentre, baseline);
		else
			g.drawString(yVar.name, colCentre - yNameWidth / 2, baseline);
		g.setFont(stdFont);
		
		int maxYWidth = yVar.getMaxWidth(g);
		int yValueRight = colLeft + yColumnWidth - (yColumnWidth - maxYWidth) / 2;
		
		CatVariable groupVar = (CatVariable)getVariable(groupKey);
		int[] lastInGroupIndex = new int[groupDisplay.length];
		double[] groupTotal = new double[groupDisplay.length];
		NumValue total = new NumValue(0.0, ((NumVariable)yVar).getMaxDecimals());
		
		for (int i=0 ; i<nRows ; i++) {
			NumValue y = (NumValue)yVar.valueAt(i);
			int group = groupVar.getItemCategory(i);
			groupTotal[group] += y.toDouble();
			if (groupDisplay[group] != HIDE)
				total.setValue(total.toDouble() + y.toDouble());
		}
		
		for (int i=0 ; i<nRows ; i++) {
			NumValue y = (NumValue)yVar.valueAt(i);
			int group = groupVar.getItemCategory(i);
			lastInGroupIndex[group] = i;
			g.setColor(kGroupColour[group]);
			boolean drawCombined = (getCurrentFrame() == kEndFrame && endGroupCombined[group])
																		|| startGroupCombined[group] && endGroupCombined[group];
			if (!drawCombined) {
				Value drawY = showPercent ? new NumValue(100 * y.toDouble() / total.toDouble(), kPercentDecimals) : y;
				drawY.drawLeft(g, yValueRight, baselines[i]);
			}
		}
		
		for (int group=0 ; group<groupDisplay.length ; group++) {
			boolean drawCombined = (getCurrentFrame() == kEndFrame && endGroupCombined[group])
																		|| startGroupCombined[group] && endGroupCombined[group];
			if (drawCombined) {
				Value drawY = showPercent ? new NumValue(100 * groupTotal[group] / total.toDouble(), kPercentDecimals)
																										: new NumValue(groupTotal[group], total.decimals);
				g.setFont(boldFont);
				g.setColor(kGroupColour[group]);
				drawY.drawLeft(g, yValueRight, baselines[lastInGroupIndex[group]]);
				g.setFont(stdFont);
			}
		}
		
		int lineVert = baselines[nRows] - NumImageValue.kDigitAscent - (kRowSpacing + kTotalGap + NumImageValue.kDigitDescent) / 2;
		g.setColor(Color.lightGray);
		g.drawLine(tableLeftBorder, lineVert, tableLeftBorder + tableWidth - 1, lineVert);
		g.setColor(getForeground());
		
		g.setFont(boldFont);
		if (showPercent)
			(new NumValue(100, kPercentDecimals)).drawLeft(g, yValueRight, baselines[nRows]);
		else
			total.drawLeft(g, yValueRight, baselines[nRows]);
		g.setFont(stdFont);
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		g.setColor(Color.white);
		int tableHeight = getTableHeight();
		g.fillRect(tableLeftBorder, tableTopBorder, tableWidth, tableHeight);
		
		Font stdFont = g.getFont();
		Font boldFont = new Font(stdFont.getName(), Font.BOLD, stdFont.getSize());
		g.setColor(getForeground());
		
		int[] baselines = getBaselines();
		
		Variable labelVar = (Variable)getVariable(labelKey);
		drawLabels(g, labelVar, baselines, stdFont, boldFont);
		
		int colStart = tableLeftBorder + kTableHorizBorder;
		drawValueColumn(g, (Variable)getVariable(yKey), colStart, baselines, false, stdFont, boldFont);
		
		colStart += yColumnWidth + kTableColGap;
		drawValueColumn(g, (Variable)getVariable(yKey), colStart, baselines, true, stdFont, boldFont);
	}
	
//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		
		int tableHeight = getTableHeight();
		return new Dimension(tableLeftBorder + tableWidth, tableTopBorder + tableHeight);
	}
	
	protected boolean canDrag() {
		return false;
	}
	
	protected boolean needsHitToDrag() {
		return true;
	}
}
package matrix;

import java.awt.*;

import dataView.*;



public class CatSelectorView extends DataView {
//	static public final String CAT_SELECTOR_VIEW = "catSelector";
	
	static final public Color kSelectedBackground = Color.yellow;
	
	static final private int kCellLeftRightBorder = 6;
	static final private int kCellTopBottomBorder = 10;
	static final private int kMinCellHeight = 30;
	static final private int kXCatNameInset = 10;
	static final private int kHeadingRowGap = 4;
	static final private int kXLabelCellGap = 6;
	
	static final private int kOutsideHorizBorder = 12;
	static final private int kOutsideVertBorder = 6;
	
	private String xKey, zKey;
	private Color xColor, zColor;
	
	private int nx = 1, nz;
	private int maxXCatWidth, maxZCatWidth;
	private int cellWidth, cellHeight, topBorder, leftBorder = 0;
	
	protected int ascent, descent, boldAscent, boldDescent;
	
	private boolean initialised = false;
	
	public CatSelectorView(DataSet theData, XApplet applet, String xKey, String zKey,
																						Color xColor, Color zColor) {
		super(theData, applet, new Insets(0,0,0,0));
		this.xKey = xKey;
		this.zKey = zKey;
		this.xColor = xColor;
		this.zColor = zColor;
	}
	
//----------------------------------
	
	protected void doInitialisation(Graphics g) {
		ascent = g.getFontMetrics().getAscent();
		descent = g.getFontMetrics().getDescent();
		
		Font normalFont = g.getFont();
		Font boldFont = new Font(normalFont.getName(), Font.BOLD, normalFont.getSize());
		g.setFont(boldFont);
		boldAscent = g.getFontMetrics().getAscent();
		boldDescent = g.getFontMetrics().getDescent();
		g.setFont(normalFont);
		
		CatVariable zVar = (CatVariable)getVariable(zKey);
		nz = zVar.noOfCategories();
		maxZCatWidth = 0;
		for (int j=0 ; j<nz ; j++)
			maxZCatWidth = Math.max(maxZCatWidth, zVar.getLabel(j).stringWidth(g));
		
		cellWidth = maxZCatWidth + 2 * kCellLeftRightBorder;
		cellHeight = Math.max(kMinCellHeight, ascent + descent + 2 * kCellTopBottomBorder);
		
		topBorder = ascent + descent + boldAscent + boldDescent + 2 * kHeadingRowGap;
		
		if (xKey != null) {
			CatVariable xVar = (CatVariable)getVariable(xKey);
			nx = xVar.noOfCategories();
			maxXCatWidth = 0;
			for (int i=0 ; i<nx ; i++)
				maxXCatWidth = Math.max(maxXCatWidth, xVar.getLabel(i).stringWidth(g));
			
			g.setFont(boldFont);
			leftBorder = Math.max(kXCatNameInset + maxXCatWidth, g.getFontMetrics().stringWidth(xVar.name))
																																				+ kXLabelCellGap;
			g.setFont(normalFont);
		}
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
	
//----------------------------------
	
	private int getSelectedCatIndex(CatVariable xVar) {
		Flags selection = getSelection();
		int row = selection.findFirstSetFlag();
		if (row == -1)
			return -1;
		
		Value selectedX = xVar.valueAt(row);
		
		ValueEnumeration xe = xVar.values();
		FlagEnumeration fe = selection.getEnumeration();
		while (xe.hasMoreValues() && fe.hasMoreFlags()) {
			Value x = xe.nextValue();
			boolean isSelected = fe.nextFlag();
			if (isSelected && x != selectedX) {
				selectedX = null;
				break;
			}
		}
		return (selectedX == null) ? -1 : xVar.getItemCategory(row);
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		CatVariable zVar = (CatVariable)getVariable(zKey);
		
		Font normalFont = g.getFont();
		Font boldFont = new Font(normalFont.getName(), Font.BOLD, normalFont.getSize());
		
		g.setColor(zColor);
		int headingCenter = kOutsideHorizBorder + leftBorder + (nz * cellWidth) / 2;
		int baseline = kOutsideVertBorder + boldAscent;
		g.setFont(boldFont);
		int varNameWidth = g.getFontMetrics().stringWidth(zVar.name);
		g.drawString(zVar.name, headingCenter - varNameWidth / 2, baseline);
		
		g.setFont(normalFont);
		baseline += boldDescent + kHeadingRowGap + ascent;
		int zCatCenter = kOutsideHorizBorder + leftBorder + cellWidth / 2;
		for (int j=0 ; j<nz ; j++) {
			zVar.getLabel(j).drawCentred(g, zCatCenter, baseline);
			zCatCenter += cellWidth;
		}
		
		int selectedZ = getSelectedCatIndex(zVar);
		int selectedX = 0;
		
		CatVariable xVar = null;
		if (xKey != null) {
			xVar = (CatVariable)getVariable(xKey);
			selectedX = getSelectedCatIndex(xVar);
			g.setColor(xColor);
			g.setFont(boldFont);
			g.drawString(xVar.name, kOutsideHorizBorder, baseline);
			g.setFont(normalFont);
		}
		
		int cellTop = baseline + descent + kHeadingRowGap;
		baseline = cellTop + (cellHeight + ascent - descent) / 2;
		
		for (int i=0 ; i<nx ; i++) {
			if (xKey != null) {
				g.setColor(xColor);
				xVar.getLabel(i).drawRight(g, kOutsideHorizBorder + kXCatNameInset, baseline);
			}
			
			int cellLeft = kOutsideHorizBorder + leftBorder;
			for (int j=0 ; j<nz ; j++) {
				g.setColor((i == selectedX && j == selectedZ) ? kSelectedBackground : Color.white);
				g.fillRect(cellLeft, cellTop, cellWidth, cellHeight);
				g.setColor(getForeground());
				g.drawRect(cellLeft, cellTop, cellWidth, cellHeight);
				cellLeft += cellWidth;
			}
			cellTop += cellHeight;
			baseline += cellHeight;
		}
	}

//-----------------------------------------------------------------------------------
	
	private Dimension getMinimumSize(Graphics g) {
		initialise(g);
		
		return new Dimension(2 * kOutsideHorizBorder + leftBorder + nz * cellWidth + 1,
											2 * kOutsideVertBorder + topBorder + nx * cellHeight + 1);
	}
	
	public Dimension getMinimumSize() {
		return getMinimumSize(getGraphics());
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

//-----------------------------------------------------------------------------------
		
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < leftBorder || x >= leftBorder + nz * cellWidth
																								|| y < topBorder || y >= topBorder + nx * cellHeight)
			return null;
		
		int zCat = (x - leftBorder) / cellWidth;
		int xCat = (y - topBorder) / cellHeight;
		
		return new DragPosInfo(xCat, zCat);
	}
	
	private void selectCell(int xCat, int zCat) {
		CatVariable zVar = (CatVariable)getVariable(zKey);
		Value zCatLabel = zVar.getLabel(zCat);
		int nVals = zVar.noOfValues();
		boolean selection[] = new boolean[nVals];
		
		ValueEnumeration xe = null;
		Value xCatLabel = null;
		if (xKey != null) {
			CatVariable xVar = (CatVariable)getVariable(xKey);
			xe = ((CatVariable)getVariable(xKey)).values();
			xCatLabel = xVar.getLabel(xCat);
		}
		
		ValueEnumeration ze = zVar.values();
		int index = 0;
		
		while (ze.hasMoreValues()) {
			Value z = ze.nextValue();
			Value x = (xe == null) ? null : xe.nextValue();
			selection[index ++] = (x == xCatLabel) && (z == zCatLabel);
		}
		getData().setSelection(selection);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		DragPosInfo posInfo = (DragPosInfo)startInfo;
		selectCell(posInfo.x, posInfo.y);
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null)
			getData().clearSelection();
		else
			startDrag(toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
	}
}
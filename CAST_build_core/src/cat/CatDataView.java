package cat;

import java.awt.*;

import dataView.*;


public abstract class CatDataView extends DataView {
	static final private Color catColor[][] = initialiseColors();
	
	static final public int SELECT_ONE = 0;
	static final public int DRAG_CUMULATIVE = 1;
	static final public int DRAG_REORDER = 2;
	static final public int NO_DRAG = 3;
	
	static private Color[][] initialiseColors() {
		Color c[][] = new Color[2][];
		c[0] = new Color[10];
		c[1] = new Color[10];
		
		c[0][0] = new Color(0x006600);
		c[0][1] = new Color(0xFF3333);
		c[0][2] = new Color(0x0066FF);
		c[0][3] = new Color(0xFF6600);
		c[0][4] = new Color(0xCC66FF);
		c[0][5] = new Color(0x990099);
		c[0][6] = new Color(0x009999);
		c[0][7] = new Color(0xFF9966);
		c[0][8] = new Color(0x66FF00);
		c[0][9] = new Color(0x666666);
		
		for (int i=0 ; i<c[0].length ; i++)
			c[1][i] = dimColor(c[0][i], 0.6);
	
		return c;
	}
	
	static public Color getColor(int catIndex, boolean boldNotDim) {
		return catColor[boldNotDim ? 0 : 1][catIndex];
	}
	
	protected String catKey;
	protected int count[] = null;
	protected int cumCount[] = null;
	protected Flags catFlags[] = null;
	protected int totalCount;
	
	protected int dragType;
	
	protected boolean initialised = false;
	
	public CatDataView(DataSet theData, XApplet applet, String catKey, int dragType) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.catKey = catKey;
		this.dragType = dragType;
	}
	
	protected boolean initialise(CatVariable variable, Graphics g) {
		if (!initialised) {
			count = variable.getCounts();
			
			cumCount = new int[count.length];
			cumCount[0] = count[0];
			for (int i=1 ; i<count.length ; i++)
				cumCount[i] = cumCount[i-1] + count[i];
			totalCount = cumCount[count.length - 1];
			
			catFlags = new Flags[count.length];
			for (int i=0 ; i<count.length ; i++)
				catFlags[i] = variable.getCatIndices(i);
			
			initialised = true;
			return true;
		}
		else
			return false;
	}
	
	protected boolean[] getSelectedCats() {
		boolean result[] = new boolean[catFlags.length];
		Flags selection = getSelection();
			
		for (int i=0 ; i<catFlags.length ; i++) {
			
			result[i] = selection.isSelected(catFlags[i]);
		}
		return result;
	}
	
	protected Color getCatColor(int catIndex, boolean boldNotDim) {
		CatVariable variable = (CatVariable)getVariable(catKey);
		return getColor(variable.originalLabelIndex(catIndex), boldNotDim);
	}
	
	protected boolean noSelectedCats(boolean[] selectedCats) {
		boolean noSelection = true;
		for (int i=0 ; i<selectedCats.length ; i++)
			if (selectedCats[i])
				noSelection = false;
		return noSelection;
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (catKey.equals(key)) {
			initialised = false;
			repaint();
		}
	}

//-----------------------------------------------------------------------------------
	
	private int startCat = -1;
	private int selectedCat = -1;
	protected int targetBefore = -1;
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return dragType != NO_DRAG;
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		CatPosInfo start = (CatPosInfo)startInfo;
		
		selectedCat = start.catIndex;
		startCat = selectedCat;
		getData().setSelection(catFlags[selectedCat]);
		
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null) {
			targetBefore = selectedCat = -1;
			getData().clearSelection();
		}
		else
			switch (dragType) {
				case SELECT_ONE:
					{
						int newSelCat = (toPos == null) ? -1 : ((CatPosInfo)toPos).catIndex;
						if (newSelCat != selectedCat) {
							if (newSelCat == -1)
								getData().clearSelection();
							else
								getData().setSelection(catFlags[newSelCat]);
							selectedCat = newSelCat;
						}
					}
					break;
				case DRAG_CUMULATIVE:
					{
						int newSelCat = ((CatPosInfo)toPos).catIndex;
						if (newSelCat != selectedCat) {
							selectedCat = newSelCat;
							int lowIndex = Math.min(startCat, newSelCat);
							int highIndex = Math.max(startCat, newSelCat);
							Flags newSelection = catFlags[lowIndex];
								
							for (int i=lowIndex+1 ; i<=highIndex ; i++)
								newSelection = newSelection.or(catFlags[i]);

							getData().setSelection(newSelection);
						}
					}
					break;
				case DRAG_REORDER:
					{
						if (selectedCat < 0) {
							selectedCat = startCat;
							getData().setSelection(catFlags[selectedCat]);
						}
						
						CatPosInfo target = (CatPosInfo)toPos;
						int newTargetBefore = (toPos == null) ? -1 : target.catIndex;
						if (toPos != null && target.highNotLow)
							newTargetBefore ++;
						if (newTargetBefore == startCat || newTargetBefore == startCat + 1)
							newTargetBefore = -1;
						if (newTargetBefore != targetBefore) {
							targetBefore = newTargetBefore;
							repaint();
						}
					}
			}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		if (selectedCat != -1 && !retainLastSelection) {
			if (dragType == DRAG_REORDER && targetBefore != -1) {
				CatVariable variable = (CatVariable)getVariable(catKey);
				if (targetBefore > startCat)
					targetBefore --;
				variable.changeLabelIndex(startCat, targetBefore);
				targetBefore = -1;
				getData().variableChanged(catKey);
			}
			
			selectedCat = -1;
			startCat = selectedCat;
			getData().clearSelection();
		}
	}
}
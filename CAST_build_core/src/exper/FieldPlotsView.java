package exper;

import java.awt.*;

import dataView.*;
import images.*;


public class FieldPlotsView extends DataView {
	
	static final public boolean CAN_EDIT = true;
	static final public boolean NO_EDIT = false;
	
	static final private int kFenceBorder = 6;
	static final private int kPlotCenter = 32;
	static final private int kPlotBorder = 1;
	
	static final public int kNoOfRows = 4;
	
	static final private Color kFieldColor = new Color(0x66FF33);
	
	private String treatmentKey;
	private Image fieldImage, treatmentImage;
	private boolean canEditTreatments;
	
	private boolean doShowPicture = false;
	private int hitField = -1;
	
	public FieldPlotsView(DataSet theData, XApplet applet,
						String fieldImageName, String treatmentImageName, boolean canEditTreatments,
						String treatmentKey, String plotEffectKey) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.treatmentKey = treatmentKey;
		this.canEditTreatments = canEditTreatments;
		
			fieldImage = CoreImageReader.getImage(fieldImageName);
			treatmentImage = CoreImageReader.getImage(treatmentImageName);
		MediaTracker mt = new MediaTracker(this);
		mt.addImage(fieldImage, 0);
		mt.addImage(treatmentImage, 0);
		try {
			mt.waitForAll();
		} catch (InterruptedException e) {
			System.err.println("could not load image: " + e);
		}
	}
	
	public void setShowPicture(boolean doShowPicture) {
		this.doShowPicture = doShowPicture;
		repaint();
	}
	
	public void paintView(Graphics g) {
		g.setColor(kFieldColor);
		int fieldWidth = kNoOfRows * kPlotCenter + (kNoOfRows + 1) * kPlotBorder;
		g.fillRect(kFenceBorder + kPlotBorder, kFenceBorder + kPlotBorder,
							fieldWidth - 2 * kPlotBorder, fieldWidth - 2 * kPlotBorder);
		
		if (doShowPicture)
			g.drawImage(fieldImage, 0, 0, this);
		
		g.setColor(Color.black);
		int pos = kFenceBorder;
		for (int i=0 ; i<= kNoOfRows ; i++) {
			g.drawLine(kFenceBorder, pos, kFenceBorder + fieldWidth - 1, pos);
			g.drawLine(pos, kFenceBorder, pos, kFenceBorder + fieldWidth - 1);
			pos += (kPlotCenter + kPlotBorder);
		}
		
		CatVariable v = (CatVariable)getVariable(treatmentKey);
		Value treatmentVal = v.getLabel(0);
		ValueEnumeration e = v.values();
		FlagEnumeration fe = getSelection().getEnumeration();
		
		int rowTop = kFenceBorder + kPlotBorder;
		for (int i=0 ; i<kNoOfRows ; i++) {
			int colLeft = kFenceBorder + kPlotBorder;
			for (int j=0 ; j<kNoOfRows ; j++) {
				LabelValue nextVal = (LabelValue)e.nextValue();
				boolean nextSel = fe.nextFlag();
				if (nextSel || hitField == i*kNoOfRows + j) {
					g.setColor(Color.red);
					g.drawRect(colLeft, rowTop, kPlotCenter - 1, kPlotCenter - 1);
					g.drawRect(colLeft + 1, rowTop + 1, kPlotCenter - 3, kPlotCenter - 3);
				}
				if (nextVal == treatmentVal)
					g.drawImage(treatmentImage, colLeft, rowTop, this);
				colLeft += (kPlotCenter + kPlotBorder);
			}
			rowTop += (kPlotCenter + kPlotBorder);
		}
	}
	
	public Dimension getMinimumSize() {
		int size = 2 * kFenceBorder + kNoOfRows * kPlotCenter + (kNoOfRows + 1) * kPlotBorder;
		return new Dimension(size, size);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(treatmentKey))
			repaint();
	}
	
//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return canEditTreatments;
	}
	
	private int initialRow, initialCol;
	
	protected int getCol(int x) {
		x -= (kFenceBorder + kPlotBorder);
		int col = x / (kPlotBorder + kPlotCenter);
		return (x < 0 || col >= kNoOfRows) ? -1 : col;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		initialCol = getCol(x);
		initialRow = getCol(y);
		if (initialRow < 0 || initialCol < 0)
			return null;
		else
			return new IndexPosInfo(initialCol + initialRow * kNoOfRows);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		int colIndex = getCol(x);
		int rowIndex = getCol(y);
		if (rowIndex != initialRow || colIndex != initialCol)
			return null;
		return new IndexPosInfo(colIndex + rowIndex * kNoOfRows);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		hitField = ((IndexPosInfo)startInfo).itemIndex;
		repaint();
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null)
			hitField = -1;
		else
			hitField = ((IndexPosInfo)toPos).itemIndex;
		repaint();
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		hitField = -1;
		repaint();
		if (endPos != null) {
			CatVariable v = (CatVariable)getVariable(treatmentKey);
			Value treatmentVal = v.getLabel(0);
			Value noTreatmentVal = v.getLabel(1);
			int hitIndex = ((IndexPosInfo)endPos).itemIndex;
			if (v.valueAt(hitIndex) == treatmentVal)
				v.setValueAt(noTreatmentVal, hitIndex);
			else
				v.setValueAt(treatmentVal, hitIndex);
			getData().variableChanged(treatmentKey);
		}
	}
}
	

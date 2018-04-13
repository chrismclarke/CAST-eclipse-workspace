package exper2;

import java.awt.*;

import dataView.*;


public class DataTableView extends DataView {
//	static final private int kTableLeftGap = 8;
//	static final private int kTableTopGap = 5;
	static final private int kCellVertBorder = 4;
	static final private int kCellHorizBorder = 10;
	static final private int kValueRowGap = 2;
	
	static final private Color kGridColor = new Color(0x999999);
	
	private String yKey, xKey, zKey;
	
	private int nx, nz;
	private int xLabelWidth;
//	private int zLabelWidth;
	private int ascent;
	private int maxReps, maxValueWidth;
	
	private boolean initialised = false;
	
	public DataTableView(DataSet theData, XApplet applet, String yKey, String xKey, String zKey) {
		super(theData, applet, null);
		this.yKey = yKey;
		this.xKey = xKey;
		this.zKey = zKey;
	}
	
	private int maxLabelWidth(Graphics g, CatVariable v) {
		int maxWidth = 0;
		for (int i=0 ; i<v.noOfCategories() ; i++)
			maxWidth = Math.max(maxWidth, v.getLabel(i).stringWidth(g));
		return maxWidth;
	}
	
	private int getMaxReps(CatVariable xVar, CatVariable zVar) {
		int reps[][] = new int[nx][nz];
		for (int i=0 ; i<xVar.noOfValues() ; i++)
			reps[xVar.getItemCategory(i)][zVar.getItemCategory(i)] ++;
		
		int maxReps = 0;
		for (int i=0 ; i<nx ; i++)
			for (int j=0 ; j<nz ; j++)
				maxReps = Math.max(maxReps, reps[i][j]);
		return maxReps;
	}
	
	private int getMaxWidth(Graphics g, NumVariable yVar) {
		int maxWidth = 0;
		for (int i=0 ; i<yVar.noOfValues() ; i++)
			maxWidth = Math.max(maxWidth, yVar.valueAt(i).stringWidth(g));
		return maxWidth;
	}
	
	private boolean initialise(Graphics g) {
		if (!initialised) {
			initialised = true;
			
			CatVariable xVar = (CatVariable)getVariable(xKey);
			nx = xVar.noOfCategories();
			
			CatVariable zVar = (CatVariable)getVariable(zKey);
			nz = zVar.noOfCategories();
			
			g.setFont(getFont());
			FontMetrics fm = g.getFontMetrics();
			ascent = fm.getAscent();
			
			xLabelWidth = maxLabelWidth(g, xVar);
//			zLabelWidth = maxLabelWidth(g, zVar);
			
			maxReps = getMaxReps(xVar, zVar);
			
			NumVariable yVar = (NumVariable)getVariable("y");
			maxValueWidth = getMaxWidth(g, yVar);
			
			return true;
		}
		else
			return false;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		int cellHeight = (getSize().height - 1) / nz;
		int tableHeight = nz * cellHeight + 1;
		int valueRowGap = (cellHeight - maxReps * ascent) / (maxReps + 1) * 2 / 3;
			
		int cellWidth = (getSize().width - 1) / nx;
		int tableWidth = nx * cellWidth + 1;
		
		g.setColor(Color.white);
		g.fillRect(0, 0, tableWidth, tableHeight);
		g.setColor(kGridColor);
		for (int i=0 ; i<=nx ; i++)
			g.drawLine(i * cellWidth, 0, i * cellWidth, tableHeight);
		for (int i=0 ; i<=nz ; i++)
			g.drawLine(0, i * cellHeight, tableWidth, i * cellHeight);
				
		g.setColor(getForeground());
		
		NumVariable yVar = (NumVariable)getVariable("y");
		CatVariable xVar = (CatVariable)getVariable(xKey);
		CatVariable zVar = (CatVariable)getVariable(zKey);
		int reps[][] = new int[nx][nz];
		for (int i=0 ; i<yVar.noOfValues() ; i++) {
			int x = xVar.getItemCategory(i);
			int z = zVar.getItemCategory(i);
			int horizCenter = x * cellWidth + cellWidth / 2;
			int baseline = z * cellHeight + (cellHeight - (maxReps - 2) * ascent
												- (maxReps - 1) * valueRowGap) / 2 + reps[x][z] * (ascent + valueRowGap);
			NumValue y = (NumValue)yVar.valueAt(i);
			if (!Double.isNaN(y.toDouble()))
				y.drawCentred(g, horizCenter, baseline);
			reps[x][z] ++;
		}
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		
		int cellHeight = ascent * maxReps + (maxReps - 1) * kValueRowGap + 2 * kCellVertBorder + 1;
		int tableHeight = nz * cellHeight + 1;
			
		int cellWidth = Math.max(xLabelWidth, maxValueWidth)  + 2 * kCellHorizBorder + 1;
		int tableWidth = nx * cellWidth + 1;
			
		return new Dimension(tableWidth, tableHeight);
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (yKey.equals(key)) {
//			initialised = false;		//	keeps the same grid layout
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
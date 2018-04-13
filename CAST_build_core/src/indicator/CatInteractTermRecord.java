package indicator;

import java.awt.*;

import dataView.*;


public class CatInteractTermRecord extends TermRecord {
	static final private int kColGap = 5;
	
	private CatVariable xVar;
	
	private int maxColWidth;
	
	public CatInteractTermRecord(CatVariable xVar, CatVariable zVar,
																							NumValue maxParam, NumValue maxPlusMinus) {
		super(zVar, maxParam, maxPlusMinus);
		this.xVar = xVar;
		
		heading2String = "";		//	to leave vertical space for z category labels
		setVarNameOverCats(true);
	}
	
	protected void doInitialisation(Graphics g) {
		super.doInitialisation(g);
		
		int nz = xVar.noOfCategories();
		maxColWidth = boxWidth;
		for (int i=0 ; i<nz ; i++)
			maxColWidth = Math.max(maxColWidth, xVar.getLabel(i).stringWidth(g));
		
		width += (maxColWidth - boxWidth) + (nz - 1) * (maxColWidth + kColGap);
	}
	
	public void drawParameters(Graphics g, int left, int top, NumValue singleParam,
												NumValue singlePlusMinus, NumValue[] catParams, NumValue[] catPlusMinus,
												Color c) {
		initialise(g);
		
		Font standardFont = g.getFont();
		g.setFont(new Font(standardFont.getName(), Font.BOLD, standardFont.getSize()));
		FontMetrics fm = g.getFontMetrics();
		g.setColor(c);
		
		g.drawString(heading1String, left, heading2Baseline);		//	z variable name
		
		int horizCentre = left + boxLeft + (width - boxLeft) / 2;
		String xVarName = xVar.name;														//	x variable name
		int zNameWidth = fm.stringWidth(xVarName);
		g.drawString(xVarName, horizCentre - zNameWidth / 2, top + heading1Baseline);
		
		g.setFont(standardFont);
		fm = g.getFontMetrics();
		
		drawCatNameColumn(g, left, top, c);											//	labels for z
		
		int nz = xVar.noOfCategories();
		left += boxLeft;
		
		int nx = ((CatVariable)var).noOfCategories();
		NumValue columnVals[] = new NumValue[nx];
		int paramIndex = 0;
		
		for (int i=0 ; i<nz ; i++) {
			int colCentre = left + maxColWidth / 2;
			g.setColor(c);
			xVar.getLabel(i).drawCentred(g, colCentre, heading2Baseline);		//	label for x column
			
			if (i > 0)
				for (int j=1 ; j<nx ; j++)
					columnVals[j] = catParams[paramIndex ++];
					
			drawCatValueColumn(g, left, top, columnVals, null, c);
			
			left += maxColWidth + kColGap;
		}
	}
}
	

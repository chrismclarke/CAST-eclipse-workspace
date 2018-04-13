package exper2;

import java.awt.*;

import dataView.*;
import models.*;


public class DiffTreatSeView extends DataView {
	static final private int kTreatHorizGap = 6;
	static final private int kTreatVertGap = 3;
	static final private int cellLeftRightBorder = 8;
	static final private int cellTopBottomBorder = 4;
	
	private String blockKey, treatKey, modelKey;
	private NumValue maxSe;
	private Color seColor, countColor;
	
	public DiffTreatSeView(DataSet theData, XApplet applet, String blockKey, String treatKey,
											String modelKey, NumValue maxSe, Color seColor, Color countColor) {
		super(theData, applet, null);
		this.blockKey = blockKey;
		this.treatKey = treatKey;
		this.modelKey = modelKey;
		this.maxSe = maxSe;
		this.seColor = seColor;
		this.countColor = countColor;
	}
	
	private double coeffCovar(double[] v, int i, int j, int nBlocks) {
		int iLow = Math.min(i, j);
		int iHigh = Math.max(i, j);
		if (iLow == 0)
			return 0.0;
		else {
			int rowStart = (nBlocks - 1 + iHigh) * (nBlocks + iHigh) / 2;
			return v[rowStart + nBlocks - 1 + iLow];
		}
	}
	
	public void drawSeDiffs(Graphics g) {
		CatVariable blockVar = (CatVariable)getVariable(blockKey);
		int nBlocks = blockVar.noOfCategories();
		CatVariable treatVar = (CatVariable)getVariable(treatKey);
		int nTreats = treatVar.noOfCategories();
		
		MultipleRegnModel model = (MultipleRegnModel)getVariable(modelKey);
		double sigma = model.evaluateSD().toDouble();
		double xxInv[] = model.getXXInv();
		double var[] = new double[nTreats];
		for (int i=0 ; i<nTreats ; i++)
			var[i] = coeffCovar(xxInv, i, i, nBlocks);
		NumValue tempVal = new NumValue(maxSe);
		
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		
		int maxTreatNameWidth = treatVar.getMaxWidth(g);
		int tableLeftRight = maxTreatNameWidth + kTreatHorizGap;
		int tableTopBottom = ascent + descent + kTreatVertGap;
		int cellWidth = Math.max(maxTreatNameWidth, maxSe.stringWidth(g)) + 2 * cellLeftRightBorder;
		int cellHeight = ascent + descent + 2 * cellTopBottomBorder;
		
		g.setColor(seColor);
		int bottomBaseline = getSize().height - descent;
		for (int i=0 ; i<nTreats-1 ; i++)
			treatVar.getLabel(i).drawCentred(g, tableLeftRight + cellWidth * i + cellWidth / 2,
																																							bottomBaseline);
		
		for (int i=1 ; i<nTreats ; i++) {
			int cellTop = getSize().height - tableTopBottom + cellHeight * (i - nTreats);
			int baseline = cellTop + (cellHeight + ascent - descent) / 2;
			treatVar.getLabel(i).drawRight(g, 0, baseline);
			g.setColor(Color.white);
			g.fillRect(tableLeftRight, cellTop, cellWidth * i, cellHeight);
			g.setColor(seColor);
			
			for (int j=0 ; j<i ; j++) {
				double covar = coeffCovar(xxInv, i, j, nBlocks);
				double diffSe = Math.sqrt(var[i] + var[j] - 2 * covar) * sigma;
				tempVal.setValue(diffSe);
				tempVal.drawCentred(g, tableLeftRight + cellWidth * j + cellWidth / 2, baseline);
			}
		}
	}
	
	public void drawBalance(Graphics g) {
		CatVariable blockVar = (CatVariable)getVariable(blockKey);
		CatVariable treatVar = (CatVariable)getVariable(treatKey);
		int nTreats = treatVar.noOfCategories();
		
		int counts[][] = blockVar.getCounts(treatVar);
		int togetherCount[][] = new int[nTreats][nTreats];
		for (int block=0 ; block<counts.length ; block++) {
			int blockCount[] = counts[block];
			for (int i=0 ; i<nTreats ; i++)
				for (int j=i+1 ; j<nTreats ; j++)
					if (blockCount[i] > 0 && blockCount[j] > 0)
						togetherCount[i][j] ++;
		}
		
		NumValue tempVal = new NumValue(1, 0);
		
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		
		int maxTreatNameWidth = treatVar.getMaxWidth(g);
		int tableLeftRight = maxTreatNameWidth + kTreatHorizGap;
		int tableTopBottom = ascent + descent + kTreatVertGap;
		int cellWidth = maxTreatNameWidth + 2 * cellLeftRightBorder;
		int cellHeight = ascent + descent + 2 * cellTopBottomBorder;
		
		int topBaseline = ascent;
		g.setColor(countColor);
		for (int i=1 ; i<nTreats ; i++)
			treatVar.getLabel(i).drawCentred(g, getSize().width - tableLeftRight + cellWidth * (i - nTreats) + cellWidth / 2, topBaseline);
		
		for (int i=0 ; i<nTreats-1 ; i++) {
			int cellTop = tableTopBottom + cellHeight * i;
			int baseline = cellTop + (cellHeight + ascent - descent) / 2;
			treatVar.getLabel(i).drawLeft(g, getSize().width, baseline);
			g.setColor(Color.white);
			g.fillRect(getSize().width - tableLeftRight - cellWidth * (nTreats - i - 1), cellTop, cellWidth * (nTreats - i - 1), cellHeight);
			g.setColor(countColor);
			
			for (int j=i+1 ; j<nTreats ; j++) {
				tempVal.setValue(togetherCount[i][j]);
				tempVal.drawCentred(g, getSize().width - tableLeftRight - cellWidth * (nTreats - j) + cellWidth / 2, baseline);
			}
		}
	}
	
	public void paintView(Graphics g) {
		drawSeDiffs(g);
		drawBalance(g);
	}

//-----------------------------------------------------------------------------------
	
	
	public Dimension getMinimumSize() {
		Graphics g = getGraphics();
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		
		CatVariable treatVar = (CatVariable)getVariable(treatKey);
		int nTreats = treatVar.noOfCategories();
		
		int maxTreatNameWidth = treatVar.getMaxWidth(g);
		int tableLeftRight = maxTreatNameWidth + kTreatHorizGap;
		int tableTopBottom = ascent + descent + kTreatVertGap;
		int seCellWidth = Math.max(maxTreatNameWidth, maxSe.stringWidth(g)) + 2 * cellLeftRightBorder;
		int balanceCellWidth = maxTreatNameWidth + 2 * cellLeftRightBorder;
		int cellHeight = ascent + descent + 2 * cellTopBottomBorder;
		
		int width = 2 * tableLeftRight + seCellWidth + (nTreats - 2) * Math.max(seCellWidth, balanceCellWidth) + balanceCellWidth;
		int height = 2 * tableTopBottom + nTreats * cellHeight;
		
		return new Dimension(width, height);
	}

	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
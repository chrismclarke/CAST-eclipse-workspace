package pairBlock;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;
import distn.*;



public class TreatBlockCIView extends BlockDotPlotView {
//	static public final String TREAT_BLOCK_CI = "treatBlockCi";
	
	static final public int NO_CIS = 0;
	static final public int MEAN_ONLY = 1;
	static final public int TREAT_CIS = 2;
	static final public int TREAT_IN_BLOCK_CIS = 3;
	
	static final private Color kMeanColor = new Color(0x3399FF);		//	mid blue
	static final private Color kCiColor = new Color(0x99CCFF);			//	pale blue
	
	private String lsXKey;
	
	private int ciType = NO_CIS;
	
	public TreatBlockCIView(DataSet theData, XApplet applet, String groupKey, String blockKey, String lsXKey,
								NumCatAxis numAxis, NumCatAxis groupAxis, double jitter) {
		super(theData, applet, groupKey, blockKey, numAxis, groupAxis, jitter);
		this.lsXKey = lsXKey;
	}
	
	public void setCIType(int ciType) {
		this.ciType = ciType;
	}
	
	public int getResidDf(boolean usingBlocks) {
		CoreComponentVariable residVar = (CoreComponentVariable)getVariable(
																			usingBlocks ? "residXZ" : "residX");
		return residVar.getDF();
	}
	
	public double getResidSsq(boolean usingBlocks) {
		CoreComponentVariable residVar = (CoreComponentVariable)getVariable(
																			usingBlocks ? "residXZ" : "residX");
		return residVar.getSsq();
	}
	
	protected void drawBackground(Graphics g, Value selectedBlock) {
		if (ciType != NO_CIS) {
			int residDf = getResidDf(ciType != TREAT_CIS);
			double residSsq = getResidSsq(ciType != TREAT_CIS);
			double residSd = Math.sqrt(residSsq / residDf);
			
			GroupsModelVariable lsX = (GroupsModelVariable)getVariable(lsXKey);
			
			int nTotal = groupVariable.noOfValues();
			int nX = groupVariable.noOfCategories();
			int nPerTreat = nTotal / nX;
			
			double se = residSd / Math.sqrt(nPerTreat);
			double t = TTable.quantile(0.975, residDf);
			
			Point p1 = null;
			Point p2 = null;
			int offset = (groupAxis.catValToPosition(1) - groupAxis.catValToPosition(0)) / 4;
			
			for (int i=0 ; i<nX ; i++) {
				double mean = lsX.evaluateMean(groupVariable.getLabel(i));
				int xPos = groupAxis.catValToPosition(i);
				
				if (ciType == TREAT_CIS || ciType == TREAT_IN_BLOCK_CIS) {
					int lowPos = axis.numValToRawPosition(mean - t * se);
					p1 = translateToScreen(lowPos, xPos - offset, p1);
					int highPos = axis.numValToRawPosition(mean + t * se);
					p2 = translateToScreen(highPos, xPos + offset, p2);
					g.setColor(kCiColor);
					fillRect(g, p1, p2);
				}
				
				int meanPos = axis.numValToRawPosition(mean);
				p1 = translateToScreen(meanPos, xPos - offset, p1);
				p2 = translateToScreen(meanPos + 1, xPos + offset, p2);
				g.setColor(kMeanColor);
				fillRect(g, p1, p2);
			}
		}
		
		super.drawBackground(g, selectedBlock);
	}
}
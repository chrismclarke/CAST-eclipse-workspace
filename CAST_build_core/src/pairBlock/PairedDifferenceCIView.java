package pairBlock;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;



public class PairedDifferenceCIView extends DataView {
//	static public final String PAIRED_DIFF_CI_VIEW = "pairedDiffCiView";
					//		Baseline treatment category is zero
	
	static final public int TWO_SAMPLE_CIS = 0;
	static final public int PAIRED_CIS = 1;
	
	static final private Color kIntervalColor = new Color(0xAAAAFF);
	static final private Color kNegativeShade = new Color(0xFFE6E6);
	
	static final private int kHalfCiWidth = 4;
	
	private int ciType;
	
	private NumCatAxis numAxis, catAxis;
	
	private String yKey, treatKey, blockKey;
	
	private int decimals;
	
	public PairedDifferenceCIView(DataSet theData, XApplet applet, String yKey, String treatKey, String blockKey,
								NumCatAxis numAxis, NumCatAxis catAxis, int decimals) {
		super(theData, applet, new Insets(0,0,0,0));
		this.numAxis = numAxis;
		this.catAxis = catAxis;
		this.yKey = yKey;
		this.treatKey = treatKey;
		this.blockKey = blockKey;
		this.decimals = decimals;
	}
	
	public void setCiType(int ciType) {
		this.ciType = ciType;
	}
	
	public void paintView(Graphics g) {
		int zeroPos = numAxis.numValToRawPosition(0.0);
		int zeroOnScreen = translateToScreen(zeroPos, 0, null).x;
		g.setColor(kNegativeShade);
		g.fillRect(0, 0, zeroOnScreen, getSize().height);
		
		CatVariable treatVar = (CatVariable)getVariable(treatKey);
		int nTreat = treatVar.noOfCategories();
		CatVariable blockVar = (CatVariable)getVariable(blockKey);
		int nBlock = blockVar.noOfCategories();
		NumVariable yVar = (NumVariable)getVariable(yKey);
		
		double meanDiff[] = new double[nTreat - 1];
		double seDiff[] = new double[nTreat - 1];
		double t = TTable.quantile(0.975, nBlock - 1);
		
		int[][] count = treatVar.getCounts(blockVar);
		boolean isOK = true;
		for (int i=0 ; i<count.length ; i++)
			for (int j=0 ; j<count[i].length ; j++)
				if (count[i][j] != 1)
					isOK = false;
		if (!isOK)
			throw new RuntimeException("Error: There should be exactly 1 value for each combination of block and treatment");
		
		if (ciType == TWO_SAMPLE_CIS) {
			double[] sy = new double[nTreat];
			double[] syy = new double[nTreat];
			
			ValueEnumeration ye = yVar.values();
			ValueEnumeration te = treatVar.values();
			while (ye.hasMoreValues() && te.hasMoreValues()) {
				double y = ye.nextDouble();
				int treat = treatVar.labelIndex(te.nextValue());
				sy[treat] += y;
				syy[treat] += y * y;
			}
			
			double y0Mean = sy[0] / nBlock;
			double y0Var = (syy[0] - y0Mean * sy[0]) / (nBlock - 1);
			
			for (int i=0 ; i<nTreat-1 ; i++) {
				double yiMean = sy[i + 1] / nBlock;
				double yiVar = (syy[i + 1] - yiMean * sy[i + 1]) / (nBlock);
				
				meanDiff[i] = yiMean - y0Mean;
				seDiff[i] = Math.sqrt(yiVar / nBlock + y0Var / nBlock);
			}
		}
		else {
			for (int i=0 ; i<nTreat-1 ; i++) {
				double diff[] = new double[nBlock];
				
				ValueEnumeration ye = yVar.values();
				ValueEnumeration te = treatVar.values();
				ValueEnumeration be = blockVar.values();
				while (ye.hasMoreValues() && te.hasMoreValues() && be.hasMoreValues()) {
					double y = ye.nextDouble();
					int treat = treatVar.labelIndex(te.nextValue());
					int block = blockVar.labelIndex(be.nextValue());
					if (treat == 0)
						diff[block] -= y;
					else if (treat == i + 1)
						diff[block] += y;
				}
				
				double sy = 0.0;
				double syy = 0.0;
				for (int j=0 ; j<nBlock ; j++) {
					double d = diff[j];
					sy += d;
					syy += d * d;
				}
				meanDiff[i] = sy / nBlock;
				seDiff[i] = Math.sqrt((syy - sy * sy / nBlock) / (nBlock - 1) / nBlock);
			}
		}
		
		Point pMean = null;
		Point pLow = null;
		Point pHigh = null;
		NumValue centerVal = new NumValue(0.0, decimals);
		NumValue plusMinusVal = new NumValue(0.0, decimals);
		LabelValue ci = new LabelValue("");
		for (int i=0  ; i<nTreat-1 ; i++) {
			int catPos = catAxis.catValToPosition(i);
			int meanPos = numAxis.numValToRawPosition(meanDiff[i]);
			pMean = translateToScreen(meanPos, catPos, pMean);
			
			int lowPos = numAxis.numValToRawPosition(meanDiff[i] - t * seDiff[i]);
			pLow = translateToScreen(lowPos, catPos, pLow);
			
			int highPos = numAxis.numValToRawPosition(meanDiff[i] + t * seDiff[i]);
			pHigh = translateToScreen(highPos, catPos, pHigh);
			
			g.setColor(kIntervalColor);
			g.fillRect(pLow.x, pLow.y - kHalfCiWidth, pHigh.x - pLow.x, 2 * kHalfCiWidth);
			
			g.setColor(getForeground());
			g.drawLine(pMean.x, pMean.y - kHalfCiWidth - 2, pMean.x, pMean.y + kHalfCiWidth + 2);
			
			centerVal.setValue(meanDiff[i]);
			plusMinusVal.setValue(t * seDiff[i]);
			
			int baseline = pMean.y - kHalfCiWidth - 4;
			ci.label = centerVal.toString() + " \u00b1 " + plusMinusVal.toString();
			ci.drawCentred(g, pMean.x, baseline);
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
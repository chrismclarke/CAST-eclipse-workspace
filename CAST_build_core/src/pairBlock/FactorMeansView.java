package pairBlock;

import java.awt.*;

import dataView.*;
import axis.*;



public class FactorMeansView extends MarginalDataView {
	static final private int kArrowHead = 4;
	
	private String responseKey, factorKey;
	
	public FactorMeansView(DataSet theData, XApplet applet, VertAxis responseAxis,
																								String responseKey, String factorKey) {
		super(theData, applet, new Insets(5,0,5,0), responseAxis);
		this.factorKey = factorKey;
		this.responseKey = responseKey;
	}
	
	public void paintView(Graphics g) {
		CatVariable factorVar = (CatVariable)getVariable(factorKey);
		int nCats = factorVar.noOfCategories();
		
		Color factorColor[] = new Color[nCats];
		double ySum[] = new double[nCats];
		int yN[] = new int[nCats];
		
		NumVariable yVar = (NumVariable)getVariable(responseKey);
		int nValues = yVar.noOfValues();
		for (int i=0 ; i<nValues ; i++) {
			int factor = factorVar.getItemCategory(i);
			double y = yVar.doubleValueAt(i);
			yN[factor] ++;
			ySum[factor] += y;
		}
		
		double factorMean[] = new double[nCats];
		for (int i=0 ; i<nCats ; i++) {
			factorMean[i] = ySum[i] / yN[i];
			factorColor[i] = getCrossColor(i);
		}
		
		Point p = null;
		for (int i=0 ; i<nCats ; i++) {
			g.setColor(factorColor[i]);
			int yPos = axis.numValToRawPosition(factorMean[i]);
			p = translateToScreen(yPos, 0, p);
			g.drawLine(p.x, p.y, getSize().width, p.y);
			g.drawLine(p.x, p.y, p.x + kArrowHead, p.y + kArrowHead);
			g.drawLine(p.x, p.y, p.x + kArrowHead, p.y - kArrowHead);
		}
	}
	
	public int minDisplayWidth() {
		return 20;
	}
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}

}
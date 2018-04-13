package exper2;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;


public class ShowContrastView extends CoreOneFactorView {
	static final private Color kPosBackground = new Color(0xEEEEFF);
	static final private Color kNegBackground = new Color(0xFFEEEE);
	static final private Color kPosMeanColor = new Color(0x0000FF);
	static final private Color kNegMeanColor = new Color(0xFF0000);
	static final private Color kArrowColor = new Color(0x009900);
	
	static final private int NEG = -1;
	static final private int ZERO = 0;
	static final private int POS = 1;
	
	static final private int kArrowLength = 5;
	
	private double[] contrast;
	
	public ShowContrastView(DataSet theData, XApplet applet, NumCatAxis xAxis, NumCatAxis yAxis,
								String xNumKey, String xCatKey, String yKey, String modelKey) {
		super(theData, applet, xAxis, yAxis, xCatKey, yKey, modelKey);
		setShowResiduals(false);
	}
	
	public void setContrast(double[] contrast) {
		this.contrast = contrast;
	}
	
	protected void drawBackground(Graphics g) {
		if (contrast != null) {
			CoreModelVariable model = (CoreModelVariable)getVariable(modelKey);
			CatVariable xVar = (CatVariable)getVariable(xKey);
			int nx = xVar.noOfCategories();
			
			int cSign[] = new int[nx];
			for (int i=0 ; i<nx ; i++)
				cSign[i] = (contrast[i] < 0) ? NEG : (contrast[i] > 0) ? POS : ZERO;
					
			
			double posSum = 0.0;
			double negSum = 0.0;
			double posWtSum = 0.0;
			double negWtSum = 0.0;
			for (int i=0 ; i<nx ; i++) {
				double mean = evaluateModelMean(model, i, xVar);
				if (cSign[i] == POS) {
					posSum += mean * contrast[i];
					posWtSum += contrast[i];
				}
				else if (cSign[i] == NEG) {
					negSum += mean * contrast[i];
					negWtSum += contrast[i];
				}
			}
			
			double posMean = posSum / posWtSum;
			double negMean = negSum / negWtSum;
			
			Point p0 = new Point(0, 0);
			Point p1 = new Point(0, 0);
			for (int i=0 ; i<nx ; i++) {
				int endPos = (i == nx - 1) ? (getSize().width + 100)			//	a bit beyond end of screen
															: (xAxis.catValToPosition(i) + xAxis.catValToPosition(i + 1)) / 2;
				p1 = getScreenPoint(cSign[i] == POS ? posMean : negMean, endPos, p1);
				
				if (cSign[i] != ZERO) {
					g.setColor(cSign[i] == POS ? kPosBackground : kNegBackground);
					g.fillRect(p0.x, 0, p1.x - p0.x, getSize().height);
					
					g.setColor(cSign[i] == POS ? kPosMeanColor : kNegMeanColor);
					g.drawLine(p0.x, p1.y, p1.x - 1, p1.y);
				}
				Point pTemp = p0;
				p0 = p1;
				p1 = pTemp;
			}
			
			for (int i=1 ; i<nx ; i++) {
				boolean change = cSign[i - 1] != cSign[i];
				boolean zerosBefore = true;
				for (int j=i-1 ; j>=0 ; j--)
					if (cSign[j] != ZERO)
						zerosBefore = false;
				
				boolean zerosAfter = true;
				for (int j=i ; j<nx ; j++)
					if (cSign[j] != ZERO)
						zerosAfter = false;
				
				if (change && !zerosBefore && !zerosAfter) {
					int midPos = (xAxis.catValToPosition(i - 1) + xAxis.catValToPosition(i)) / 2;
					p0 = getScreenPoint(negMean, midPos, p0);
					p1 = getScreenPoint(posMean, midPos, p1);
					g.setColor(kArrowColor);
					int aSign = (p1.y > p0.y) ? 1 : -1;
					
					for (int j=-1 ; j<=1 ; j++)
						g.drawLine(p0.x + j * aSign, p0.y, p1.x + j * aSign, p1.y - aSign);
					
					for (int j=0 ; j<2 ; j++) {
						g.drawLine(p1.x, p1.y - j * aSign, p1.x + kArrowLength, p1.y - aSign * (kArrowLength + j));
						g.drawLine(p1.x, p1.y - j * aSign, p1.x - kArrowLength, p1.y - aSign * (kArrowLength + j));
					}
				}
			}
		}
	}
	
	protected void drawFactorMeans(Graphics g, CoreModelVariable model, CatVariable xVar) {
	}
}
	

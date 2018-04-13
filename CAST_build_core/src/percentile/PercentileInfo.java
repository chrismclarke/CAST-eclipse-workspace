package percentile;

import java.awt.*;

import dataView.*;
import axis.*;

public class PercentileInfo {
	static final public int kBoxHeight = 20;
	static final private double kEps = 0.0001;
	
	static final public int STEP = 0;
	static final public int SMOOTH = 1;
	
	static public double evaluatePercentile(NumValue sortedVal[], double prob, int evaluateType) {
		if (evaluateType == STEP)
			return evaluateStepPercentile(sortedVal, prob);
		else
			return evaluateSmoothPercentile(sortedVal, prob);
	}
	
	static private double evaluateStepPercentile(NumValue sortedVal[], double prob) {
		int noOfVals = sortedVal.length;
		double targetCount = prob * noOfVals;
		double percentile;
		if (targetCount < 1.0 - kEps)
			percentile = sortedVal[0].toDouble();
		else if (targetCount > noOfVals - 1 + kEps)
			percentile = sortedVal[noOfVals - 1].toDouble();
		else if (Math.abs(targetCount - Math.rint(targetCount)) < kEps) {
			int index1 = (int)Math.round(targetCount);
			int index0 = index1 - 1;
			percentile = 0.5 * (sortedVal[index0].toDouble() + sortedVal[index1].toDouble());
		}
		else {
			int index = (int)Math.round(targetCount - 0.5);
			percentile = sortedVal[index].toDouble();
		}
		return percentile;
	}
	
	static private double evaluateSmoothPercentile(NumValue sortedVal[], double prob) {
		int noOfVals = sortedVal.length;
		double targetCount = prob * (noOfVals + 1);
		double percentile;
		if (targetCount < 1.0)
			percentile = sortedVal[0].toDouble();
		else if (targetCount > noOfVals)
			percentile = sortedVal[noOfVals - 1].toDouble();
		else {
			int index1 = (int)Math.round(targetCount - 0.5);
			int index0 = index1 - 1;
			double p = targetCount - index1;
			percentile = (1.0 - p) * sortedVal[index0].toDouble() + p * sortedVal[index1].toDouble();
		}
		return percentile;
	}
	
	public double prob[], percentile[];
	public int percentilePos[];
	
	public int boxBottom;
	public int vertMidLine;
	
	private Color fillColor[];
	
	public PercentileInfo(double[] prob) {
		this.prob = prob;
		int noOfPercentiles = prob.length;
		percentile = new double[noOfPercentiles];
		percentilePos = new int[noOfPercentiles];
		fillColor = new Color[noOfPercentiles - 1];
	}
	
	public void setFillColor(Color c, int i) {
		fillColor[i] = c;
	}
	
	public void initialisePercentiles(NumValue sortedVal[], NumCatAxis axis, int evaluateType) {
		for (int i=0 ; i<prob.length ; i++) {
			percentile[i] = evaluatePercentile(sortedVal, prob[i], evaluateType);
			percentilePos[i] = axis.numValToRawPosition(percentile[i]);
		}
	}
	
	public void drawPercentilePlot(Graphics g, DataView view, NumValue sortedVal[], NumCatAxis axis) {
		Point p1 = null;
		Point p2 = null;
		
		for (int i=0 ; i<percentile.length-1 ; i++) {
			p1 = view.translateToScreen(percentilePos[i + 1], boxBottom, p1);
			p2 = view.translateToScreen(percentilePos[i], boxBottom + kBoxHeight, p2);
			
			g.setColor((fillColor[i] == null) ? Color.white : fillColor[i]);
			if (p2.y > p1.y)
				g.fillRect(p1.x, p1.y, (p2.x - p1.x), (p2.y - p1.y));
			else
				g.fillRect(p2.x, p2.y, (p1.x - p2.x), (p1.y - p2.y));
			
			g.setColor(view.getForeground());
			g.drawLine(p2.x, p2.y, p2.x, p1.y);
			
			if (i == 0 || i == percentile.length - 2) {
				NumValue percent = new NumValue((prob[i + 1] - prob[i]) * 100, 0);
				LabelValue percentLabel = new LabelValue(percent + "%");
				int center = (p1.x + p2.x) / 2;
				int baseline = (p1.y + p2.y + g.getFontMetrics().getAscent()) / 2;
				percentLabel.drawCentred(g, center, baseline);
			}
		}
		
		g.setColor(view.getForeground());
		
		p1 = view.translateToScreen(percentilePos[prob.length - 1], boxBottom, p1);
		p2 = view.translateToScreen(percentilePos[0], boxBottom + kBoxHeight, p2);
		if (p2.y > p1.y)
			g.drawRect(p1.x, p1.y, (p2.x - p1.x), (p2.y - p1.y));
		else
			g.drawRect(p2.x, p2.y, (p1.x - p2.x), (p1.y - p2.y));
	}
}

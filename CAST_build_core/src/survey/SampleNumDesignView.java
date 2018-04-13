package survey;

import java.awt.*;

import dataView.*;


public class SampleNumDesignView extends SampleDesignView {

	private String kMeanString;
	
	private double nrBeta0, nrBeta1;				//		logistic params for p(response)
	
	private double iBeta0, iBeta1;				//		logistic params for p(change)
	private double changeMean, changeSD;
	
	
	public SampleNumDesignView(DataSet theData, XApplet applet, String yKey, int noCovered, int sampleSize,
						NumValue maxSummary, long randomSeed) {
		super(theData, applet, yKey, noCovered, sampleSize, maxSummary, randomSeed);
		kMeanString = applet.translate("Mean");
	}
	
	public void setResponseProbs(double y0, double p0, double y1, double p1) {
		if (p0 > 0.0 && p0 < 1.0 && p1 > 0.0 && p1 < 1.0) {
			double logit0 = Math.log((1 - p0) / p0);
			double logit1 = Math.log((1 - p1) / p1);
			nrBeta1 = (logit0 - logit1) / (y0 - y1);
			nrBeta0 = logit0 - nrBeta1 * logit0;
			nonResponseErrors = true;
		}
	}
	
	public void setChangeProbs(double y0, double p0, double y1, double p1,
																			double changeMean, double changeSD) {
		if (p0 > 0.0 && p0 < 1.0 && p1 > 0.0 && p1 < 1.0) {
			double logit0 = Math.log((1 - p0) / p0);
			double logit1 = Math.log((1 - p1) / p1);
			iBeta1 = (logit0 - logit1) / (y0 - y1);
			iBeta0 = logit0 - nrBeta1 * logit0;
			this.changeMean = changeMean;
			this.changeSD = changeSD;
			instrumentErrors = true;
		}
	}
	
	protected Dimension getMarkSize(Graphics g) {
		g.setFont(getApplet().getTinyBoldFont());
		FontMetrics fm = g.getFontMetrics();
		int valueAscent = fm.getAscent();
		NumVariable y = (NumVariable)getVariable(yKey);
		int maxValueWidth = y.getMaxWidth(g);
		return new Dimension(maxValueWidth, valueAscent);
	}
	
	protected Color drawMark(Graphics g, Value v, int x, int y, boolean changed) {
		Color markColor = changed ? Color.red : Color.blue;
		g.setColor(markColor);
		v.drawCentred(g, x, y + markSize.height / 2);
		return markColor;
	}
	
	protected void generateResponses(Value[] sample, Value[] responses) {
		for (int i=0 ; i<sample.length ; i++)
			if (sample[i] != null) {
				double rand = generator.nextDouble();
				double responseProb = 1.0 / (1.0 + Math.exp(nrBeta0
																		+ nrBeta1 * ((NumValue)sample[i]).toDouble()));
				if (rand <= responseProb)
					responses[i] = sample[i];
				else
					responses[i] = null;
			}
	}
	
	protected void generateMeasured(Value[] responses, Value[] measured) {
		for (int i=0 ; i<responses.length ; i++)
			if (responses[i] != null) {
				double rand = generator.nextDouble();
				double changeProb = 1.0 / (1.0 + Math.exp(iBeta0
																		+ iBeta1 * ((NumValue)responses[i]).toDouble()));
				if (rand <= changeProb) {
					NumValue nv = (NumValue)responses[i];
					double change = changeMean + changeSD * generator.nextGaussian();
					double factor = Math.pow(10.0, nv.decimals);
					change = Math.rint(change * factor) / factor;
					if (change == 0.0)
						measured[i] = responses[i];
					else
						measured[i] = new NumValue(nv.toDouble() + change, nv.decimals);
				}
				else
					measured[i] = responses[i];
			}
	}
	
	protected NumValue summarise(Value[] values) {
		int noOfVals = 0;
		double total = 0.0;
		for (int i=0 ; i<values.length ; i++)
			if (values[i] != null) {
				noOfVals ++;
				total += ((NumValue)values[i]).toDouble();
			}
		if (noOfVals == 0)
			return null;
		else
			return new NumValue(total / noOfVals, maxSummary.decimals);
	}
	
	protected LabelValue summaryName() {
		return new LabelValue(kMeanString + " (" + getVariable("y").name + ")");
	}
}
	

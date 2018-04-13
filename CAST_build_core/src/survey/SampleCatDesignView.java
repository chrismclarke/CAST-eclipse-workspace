package survey;

import java.awt.*;

import dataView.*;


public class SampleCatDesignView extends SampleDesignView {

	private String kPropnString;
	
	protected Value successVal, failureVal;
	
	private double successResponse, failureResponse;		//		response probs
	private double successChange, failureChange;				//		change probs
	
	
	public SampleCatDesignView(DataSet theData, XApplet applet, String yKey, int noCovered,
																				int sampleSize, NumValue maxSummary, long randomSeed) {
		super(theData, applet, yKey, noCovered, sampleSize, maxSummary, randomSeed);
		kPropnString = applet.translate("Proportion");
		CatVariable v = (CatVariable)getVariable(yKey);
		successVal = v.getLabel(0);
		failureVal = v.getLabel(1);
	}
	
	public void setResponseProbs(double successProb, double failureProb) {
		successResponse = successProb;
		failureResponse = failureProb;
		nonResponseErrors = true;
	}
	
	public void setChangeProbs(double successProb, double failureProb) {
		successChange = successProb;
		failureChange = failureProb;
		instrumentErrors = true;
	}

//----------------------------------------------------------------------
	
	protected Dimension getMarkSize(Graphics g) {
		return new Dimension(7, 7);
	}
	
	protected Color drawMark(Graphics g, Value v, int x, int y, boolean changed) {
		Color markColor = changed ? Color.red : (v == successVal) ? Color.blue : Color.black;
		g.setColor(markColor);
		if (v == successVal) {
			g.drawLine(x - 3, y - 3, x + 3, y + 3);
			g.drawLine(x - 3, y - 2, x + 2, y + 3);
			g.drawLine(x - 2, y - 3, x + 3, y + 2);
			g.drawLine(x - 3, y + 3, x + 3, y - 3);
			g.drawLine(x - 3, y + 2, x + 2, y - 3);
			g.drawLine(x - 2, y + 3, x + 3, y - 2);
		}
		else {
			g.drawOval(x - 3, y - 3, 7, 7);
			g.drawOval(x - 2, y - 2, 5, 5);
		}
		return markColor;
	}
	
	protected void generateResponses(Value[] sample, Value[] responses) {
		for (int i=0 ; i<sample.length ; i++)
			if (sample[i] == null)
				responses[i] = null;
			else {
				double rand = generator.nextDouble();
				if (sample[i] == successVal && rand <= successResponse
										|| sample[i] == failureVal && rand <= failureResponse)
					responses[i] = sample[i];
				else
					responses[i] = null;
			}
	}
	
	protected void generateMeasured(Value[] responses, Value[] measured) {
		for (int i=0 ; i<responses.length ; i++)
			if (responses[i] == null)
				measured[i] = null;
			else {
				double rand = generator.nextDouble();
				if (responses[i] == successVal)
					measured[i] = (rand <= successChange) ? failureVal : successVal;
				else if (responses[i] == failureVal)
					measured[i] = (rand <= failureChange) ? successVal : failureVal;
			}
	}
	
	protected NumValue summarise(Value[] values) {
		int noOfVals = 0;
		int noOfSuccess = 0;
		for (int i=0 ; i<values.length ; i++)
			if (values[i] != null) {
				noOfVals ++;
				if (values[i] == successVal)
					noOfSuccess ++;
			}
		if (noOfVals == 0)
			return null;
		else
			return new NumValue(noOfSuccess / (double)noOfVals, maxSummary.decimals);
	}
	
	protected LabelValue summaryName() {
		return new LabelValue(kPropnString + " (" + successVal.toString() + ")");
	}
}
	

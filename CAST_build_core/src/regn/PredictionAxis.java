package regn;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;


public class PredictionAxis extends VertAxis {
	static final private int kValueBorder = 2;
	
	private String predictionString;
	
	private DataSet data;
	private String lineKey;
	private DragExplanAxis xAxis;
	private int predictionDecimals;
	private int predictionAbove, predictionBelow, predictionWidth;
	
	public PredictionAxis(XApplet applet) {
		super(applet);
		predictionString = applet.translate("Prediction");
	}
	
	public void setModel(DataSet data, String yKey, String lineKey, DragExplanAxis xAxis,
					int predictionDecimals) {
		this.data = data;
		this.lineKey = lineKey;
		this.xAxis = xAxis;
		this.predictionDecimals = predictionDecimals;
	}
	
//-----------------------------------------------------------------------------------
	
	private NumValue prediction;
	
	private NumValue makePrediction() {
		if (xAxis ==  null)
			return new NumValue(0.0, 0);	//	Should never be called
		NumValue xValue = xAxis.getAxisVal();
		if (xValue == null)
			return null;
		
		CoreModelVariable theModel = (CoreModelVariable)data.getVariable(lineKey);
		double prediction = theModel.evaluateMean(xValue);
		
		return new NumValue(prediction, predictionDecimals);
	}
	
	public void checkPrediction() {
		prediction = makePrediction();
	}

//-----------------------------------------------------------------------------------
	
	protected int getMaxValueWidth(Graphics g) {
		return Math.max((new NumValue(maxOnAxis, predictionDecimals)).stringWidth(g),
								(new NumValue(minOnAxis, predictionDecimals)).stringWidth(g));
	}
	
	public void findAxisWidth() {
		super.findAxisWidth();
		Graphics g = getGraphics();
		FontMetrics fm = g.getFontMetrics();
		predictionWidth = Math.max(fm.stringWidth(predictionString),
							fm.stringWidth("= ") + getMaxValueWidth(g) + 2 * kValueBorder);
		axisWidth += predictionWidth;
		predictionAbove = fm.getHeight() + kValueBorder + (fm.getAscent() + fm.getDescent()) / 2;
		predictionBelow = kValueBorder + (fm.getAscent() + fm.getDescent() + 1) / 2;
	}
	
	public void corePaint(Graphics g) {
		if (prediction == null)
			checkPrediction();
		if (prediction != null)
			try {
				int markedPos = getSize().height - lowBorderUsed - numValToPosition(prediction.toDouble()) - 1;
				g.setColor(Color.blue);
				g.drawLine(predictionWidth, markedPos, getSize().width - 1, markedPos);
				
				if (markedPos < predictionAbove)
					markedPos = predictionAbove;
				if (markedPos >= getSize().height - predictionBelow)
					markedPos = getSize().height - predictionBelow;
				
				FontMetrics fm = g.getFontMetrics();
				int boxTop = markedPos - kValueBorder - (fm.getAscent() + fm.getDescent()) / 2;
				
				g.drawString(predictionString, 0, boxTop - (fm.getHeight() -fm.getAscent()));
				
				g.drawString("= ", 0, boxTop + kValueBorder + fm.getAscent());
				int kEqualsWidth = fm.stringWidth("= ");
				g.drawRect(kEqualsWidth, boxTop, predictionWidth - kEqualsWidth - 1,
													fm.getAscent() + fm.getDescent() + 2 * kValueBorder);
				g.setColor(Color.yellow);
				g.fillRect(kEqualsWidth + 1, boxTop + 1, predictionWidth - kEqualsWidth - 2,
												fm.getAscent() + fm.getDescent() + 2 * kValueBorder - 2);
				
				g.setColor(Color.blue);
				prediction.drawRight(g, kEqualsWidth + kValueBorder, boxTop + kValueBorder + fm.getAscent());
			} catch (AxisException e) {
			}
		g.setColor(getForeground());
		super.corePaint(g);
	}
}
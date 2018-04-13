package logistic;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;

//import cat.*;
import regn.*;


public class BarPredictionView extends DragLocationView {
//	static final public String BAR_PREDICT_PLOT = "barPredictPlot";
	
	static final private int kBarWidth = 5;
	static final private int kArrowSize = 3;
	static final private Color kSuccessColor = new Color(0x3399CC);
	static final private Color kFailureColor = new Color(0xFF66CC);
	
	protected String xKey, yKey, modelKey;
	protected VertAxis vertAxis;
	protected HorizAxis horizAxis;
	
	private boolean initialised = false;
	protected int [][] jointCounts;
	protected int [] xCounts, yCounts;
	
	public BarPredictionView(DataSet theData, XApplet applet,
								PredictionAxis vertAxis, DragExplanAxis horizAxis,
								String xKey, String yKey, String modelKey, int predictionDecimals) {
		super(theData, applet, horizAxis);
		this.xKey = xKey;
		this.yKey = yKey;
		this.modelKey = modelKey;
		this.vertAxis = vertAxis;
		this.horizAxis = horizAxis;
		vertAxis.setModel(theData, "y", modelKey, horizAxis, predictionDecimals);
	}
	
	public BarPredictionView(DataSet theData, XApplet applet,
								VertAxis vertAxis, HorizAxis horizAxis,
								String xKey, String yKey, String modelKey) {
		super(theData, applet, null);
		this.xKey = xKey;
		this.yKey = yKey;
		this.modelKey = modelKey;
		this.vertAxis = vertAxis;
		this.horizAxis = horizAxis;
	}
	
	protected boolean initialise(CatVariable x, Variable y) {
		if (!initialised) {
			jointCounts = x.getCounts(y);
			xCounts = x.getCounts();
			
			int noOfXCats = xCounts.length;
			int noOfYCats = jointCounts[0].length;
			yCounts = new int[noOfYCats];
			for (int i=0 ; i<noOfXCats ; i++)
				for (int j=0 ; j<noOfYCats ; j++)
					yCounts[j] += jointCounts[i][j];
			
			initialised = false;
			return true;
		}
		else
			return false;
	}
	
	protected boolean canShowPrediction() {
		return true;
	}
	
	protected double getPredictionX() {
		DragValAxis axis = (DragExplanAxis)horizAxis;
		return axis.getAxisVal().toDouble();
	}
	
	protected int [] getBarLeftPos(int noOfVals) {
		int [] result = new int[noOfVals];
		try {
			NumVariable y = (NumVariable)getVariable(yKey);
			ValueEnumeration e = y.values();
			for (int i=0 ; i<noOfVals ; i++)
				result[i] = horizAxis.numValToPosition(((NumValue)e.nextGroup().val).toDouble()) - kBarWidth / 2;
		} catch (AxisException e) {
		}
		return result;
	}
	
	protected void drawBars(Graphics g, int zeroVert, int oneVert) {
		if (xKey != null && yKey != null) {
			CatVariable x = (CatVariable)getVariable(xKey);
			NumVariable y = (NumVariable)getVariable(yKey);
			
			initialise(x, y);
			
			int noOfYCats = yCounts.length;
			
			int barLeftPos[] = getBarLeftPos(noOfYCats);
			
//			Point topLeft = null;
			Point midLeft = null;
//			Point bottomRight = null;
			for (int i=0 ; i<noOfYCats ; i++) {
				double proportion = ((double)jointCounts[0][i]) / yCounts[i];
				try {
					int propnPos = vertAxis.numValToPosition(proportion);
					midLeft = translateToScreen(barLeftPos[i], propnPos, midLeft);
					
					g.setColor(kFailureColor);
					g.fillRect(midLeft.x, oneVert, kBarWidth, midLeft.y - oneVert);
					g.setColor(kSuccessColor);
					g.fillRect(midLeft.x, midLeft.y, kBarWidth, zeroVert - midLeft.y);
				} catch (AxisException e) {
				}
			}
		}
	}
	
	protected void drawPrediction(Graphics g, LinearModel theModel, double xValue) {
		double prediction = theModel.evaluateMean(xValue);
		
		int vertPos = vertAxis.numValToRawPosition(prediction);
		int horizPos = horizAxis.numValToRawPosition(xValue);
		Point predictPos = translateToScreen(horizPos, vertPos, null);
		
		g.setColor(Color.red);
		g.drawLine(predictPos.x, getSize().height, predictPos.x, predictPos.y);
		g.drawLine(predictPos.x, predictPos.y, 0, predictPos.y);
		g.drawLine(0, predictPos.y, kArrowSize, predictPos.y + kArrowSize);
		g.drawLine(0, predictPos.y, kArrowSize, predictPos.y - kArrowSize);
	}
	
	protected void drawHandles(Graphics g, LinearModel theModel) {
	}
	
	public void paintView(Graphics g) {
		int pos0 = 0;
		try {
			pos0 = vertAxis.numValToPosition(0.0);
		} catch (AxisException e) {
		}
		int zeroVert = translateToScreen(0, pos0, null).y;
		
		int pos1 = 0;
		try {
			pos1 = vertAxis.numValToPosition(1.0);
		} catch (AxisException e) {
		}
		int oneVert = translateToScreen(0, pos1, null).y;
		
		drawBars(g, zeroVert, oneVert);
		
		g.setColor(Color.gray);
		g.drawLine(0, oneVert, getSize().width, oneVert);
		g.drawLine(0, zeroVert, getSize().width, zeroVert);
		
		if (modelKey != null) {
			LinearModel theModel = (LinearModel)getVariable(modelKey);
			if (canShowPrediction())
				drawPrediction(g, theModel, getPredictionX());
			
			drawHandles(g, theModel);
			
			g.setColor(Color.black);
			theModel.drawMean(g, this, horizAxis, vertAxis);
		}
		
		if (vertAxis instanceof PredictionAxis) {
			((PredictionAxis)vertAxis).checkPrediction();
			vertAxis.repaint();
		}
	}
	
	public void setModel(String newModelKey) {
		if (modelKey != newModelKey) {
			modelKey = newModelKey;
			repaint();
		}
	}
}
	

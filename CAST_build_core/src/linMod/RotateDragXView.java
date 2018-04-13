package linMod;

import java.awt.*;

import dataView.*;
import models.*;
import graphics3D.*;


public class RotateDragXView extends Rotate3DView {
//	static public final String ROTATE_DRAGX_PLOT = "rotateDragX";
	
	static final private Color kStandardBandGray = new Color(0x999999);
	static final private Color kPaleGray = new Color(0xCCCCCC);
	static final private Color kMidGray = new Color(0x666666);
	static final private Color kPink = new Color(0xFF99FF);
	
	static final public int DRAW_MEAN_PDF = 0;
	static final public int DRAW_BAND_PDF = 1;
	static final public int DRAW_SIMPLE_BAND_PDF = 2;
	
	static final public int DRAW_CROSSES = 0;
	static final public int DRAW_LS_CROSSES = 1;
	
	
	private String yDataKey;
	
	protected Normal3DArtist normalArtist;
	
	private boolean drawModel = true;
	private boolean drawData = false;
	
	private int modelDrawType = DRAW_MEAN_PDF;
	private int dataDrawType = DRAW_CROSSES;
	protected NumValue pdfX;
	
	public RotateDragXView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis, String yKey,
						String xKey, String dataKey, NumValue pdfX) {
		super(theData, applet, yAxis, zAxis, xAxis, xKey, yKey, null);
		
		normalArtist = new Normal3DArtist(this, yKey, xKey, theData, yAxis, xAxis, false);
		this.yDataKey = dataKey;
		this.pdfX = pdfX;
	}
	
	public void setXKey(String newXKey) {
		xKey = newXKey;
	}
	
	public void setPDFDrawX(NumValue pdfX) {
		this.pdfX = pdfX;
	}
	
	public void setModelDrawType(int modelDrawType) {
		this.modelDrawType = modelDrawType;
	}
	
	public void setDataDrawType(int dataDrawType) {
		this.dataDrawType = dataDrawType;
	}
	
	public void setShowData(boolean drawData) {
		this.drawData = drawData;
	}
	
	private void drawModelLine(Graphics g, double offsetSD, Color drawColor) {
		Color oldColor = g.getColor();
		g.setColor(drawColor);
		
		double lowX = zAxis.getMinOnAxis();
		double highX = zAxis.getMaxOnAxis();
		
		CoreModelVariable distn = (CoreModelVariable)getData().getVariable(yKey);
		
		double yOffset = offsetSD * distn.evaluateSD().toDouble();
		double lowY = distn.evaluateMean(new NumValue(lowX)) + yOffset;
		double highY = distn.evaluateMean(new NumValue(highX)) + yOffset;
		
		Point startPos = getScreenPoint(lowY, 0.0, lowX, null);
		Point endPos = getScreenPoint(highY, 0.0, highX, null);
		
		g.drawLine(startPos.x, startPos.y, endPos.x, endPos.y);
		g.setColor(oldColor);
	}
	
	private void fillModel(Graphics g, double offsetSD, Color fillColor) {
		Color oldColor = g.getColor();
		g.setColor(fillColor);
		
		double lowX = zAxis.getMinOnAxis();
		double highX = zAxis.getMaxOnAxis();
		
		CoreModelVariable distn = (CoreModelVariable)getData().getVariable(yKey);
		
		double yOffset = offsetSD * distn.evaluateSD().toDouble();
		
		double lowY1 = distn.evaluateMean(new NumValue(lowX)) + yOffset;
		double highY1 = distn.evaluateMean(new NumValue(highX)) + yOffset;
		
		Point startPos1 = getScreenPoint(lowY1, 0.0, lowX, null);
		Point endPos1 = getScreenPoint(highY1, 0.0, highX, null);
		
		double lowY2 = distn.evaluateMean(new NumValue(lowX)) - yOffset;
		double highY2 = distn.evaluateMean(new NumValue(highX)) - yOffset;
		
		Point startPos2 = getScreenPoint(lowY2, 0.0, lowX, null);
		Point endPos2 = getScreenPoint(highY2, 0.0, highX, null);
		
		int x[] = {startPos1.x, endPos1.x, endPos2.x, startPos2.x};
		int y[] = {startPos1.y, endPos1.y, endPos2.y, startPos2.y};
		
		g.fillPolygon(x, y, 4);
		g.drawLine(startPos1.x, startPos1.y, endPos1.x, endPos1.y);
		g.drawLine(startPos2.x, startPos2.y, endPos2.x, endPos2.y);
		g.setColor(oldColor);
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		if (drawModel)
			switch (modelDrawType) {
				case DRAW_MEAN_PDF:
					drawModelLine(g, 0.0, Color.green);
					normalArtist.paintDistn(g, pdfX);
					break;
				case DRAW_BAND_PDF:
					{
						fillModel(g, 2.0, kStandardBandGray);
						drawModelLine(g, 0.0, Color.blue);
						
						CoreModelVariable distn = (CoreModelVariable)getData().getVariable(yKey);
						double yMean = distn.evaluateMean(pdfX);
						double ySD = distn.evaluateSD().toDouble();
						normalArtist.paintDistn(g, pdfX, yMean - 2.0 * ySD, yMean + 2.0 * ySD);
					}
					break;
				case DRAW_SIMPLE_BAND_PDF:
					{
						fillModel(g, 2.0, kPaleGray);
						drawModelLine(g, 0.0, kMidGray);
						
//						CoreModelVariable distn = (CoreModelVariable)getData().getVariable(yKey);
//						double yMean = distn.evaluateMean(pdfX);
//						double ySD = distn.evaluateSD().toDouble();
						normalArtist.setFillColor(kPink);
						normalArtist.paintDistn(g, pdfX);
					}
					break;
			}
		if (drawData)
			switch (dataDrawType) {
				case DRAW_CROSSES:
					drawCrosses(g);
					break;
				case DRAW_LS_CROSSES:
					drawLSLine(g, Color.blue);
					drawCrosses(g);
					break;
			}
	}
	
	private void drawLSLine(Graphics g, Color drawColor) {
		Color oldColor = g.getColor();
		g.setColor(drawColor);
		
		double lowX = zAxis.getMinOnAxis();
		double highX = zAxis.getMaxOnAxis();
		
		LSEstimate lsEvaluator = new LSEstimate(getData(), xKey, yDataKey);
		double b0 = lsEvaluator.getIntercept();
		double b1 = lsEvaluator.getSlope();
		
		double lowY = b0 + b1 * lowX;
		double highY = b0 + b1 * highX;
		
		Point startPos = getScreenPoint(lowY, 0.0, lowX, null);
		Point endPos = getScreenPoint(highY, 0.0, highX, null);
		
		g.drawLine(startPos.x, startPos.y, endPos.x, endPos.y);
		g.setColor(oldColor);
	}
	
	private void drawCrosses(Graphics g) {
		NumVariable xVariable = (NumVariable)getVariable(xKey);
		NumVariable yVariable = (NumVariable)getVariable(yDataKey);
		ValueEnumeration xe = xVariable.values();
		ValueEnumeration ye = yVariable.values();
		Point crossPos = null;
		while (xe.hasMoreValues() && ye.hasMoreValues()) {
			crossPos = getScreenPoint(ye.nextDouble(), 0.0, xe.nextDouble(), crossPos);
			if (crossPos != null)
				drawCross(g, crossPos);
		}
	}
}
	

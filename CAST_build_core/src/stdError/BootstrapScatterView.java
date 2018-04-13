package stdError;

import java.awt.*;

import dataView.*;
import axis.*;


public class BootstrapScatterView extends DataView {
	static final private Color kSampColor = Color.blue;
	static final private Color kPopnColor = new Color(0xBBBBBB);
	static final private Color kPopnLabelColor = Color.gray;
	static final private Color kErrorColor = Color.red;
	
	static final private String digitString[] = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "+"};
	
	static final private int kTopBorder = 4;
	static final private int kLeftBorder = 4;
	
	private String kPopnCorrString, kSampCorrString, kErrorString;
	
	private HorizAxis xAxis;
	private VertAxis yAxis;
	private String xKey, yKey, bootstrapKey;
	
	private SummaryDataSet summaryData;
	private String corrKey;
	private NumValue popnCorr;
	
	public BootstrapScatterView(DataSet theData, XApplet applet, HorizAxis xAxis,
											VertAxis yAxis, String xKey, String yKey, String bootstrapKey,
											SummaryDataSet summaryData, String corrKey, NumValue popnCorr) {
		super(theData, applet, new Insets(5, 5, 5, 5));
		this.xKey = xKey;
		this.yKey = yKey;
		this.bootstrapKey = bootstrapKey;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		this.summaryData = summaryData;
		this.corrKey = corrKey;
		this.popnCorr = popnCorr;
		
		kPopnCorrString = "r " + applet.translate("from data") + " = ";
		kSampCorrString = "r " + applet.translate("from bootstrap") + " = ";
		kErrorString = applet.translate("Error") + " = ";
	}
	
	private Point getScreenPoint(double x, double y, Point thePoint) {
		int vertPos = yAxis.numValToRawPosition(y);
		int horizPos = xAxis.numValToRawPosition(x);
		
		return translateToScreen(horizPos, vertPos, thePoint);
	}
	
	private void paintBackground(Graphics g) {
		NumVariable corrVar = (NumVariable)summaryData.getVariable(corrKey);
		int selectedSumIndex = summaryData.getSelection().findSingleSetFlag();
		if (selectedSumIndex < 0 || selectedSumIndex >= corrVar.noOfValues())
			selectedSumIndex = corrVar.noOfValues() - 1;
		NumValue corr = (NumValue)corrVar.valueAt(selectedSumIndex);
		
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int baseline = ascent + kTopBorder;
		
		g.setColor(kPopnLabelColor);
		g.drawString(kPopnCorrString + popnCorr, kLeftBorder, baseline);
		
		baseline += ascent + kTopBorder;
		g.setColor(kSampColor);
		g.drawString(kSampCorrString + corr, kLeftBorder, baseline);
		
		baseline += ascent + kTopBorder;
		g.setColor(kErrorColor);
		NumValue error = new NumValue(corr.toDouble() - popnCorr.toDouble(), Math.max(corr.decimals, popnCorr.decimals));
		g.drawString(kErrorString + error, kLeftBorder, baseline);
	}
	
	public void paintView(Graphics g) {
		paintBackground(g);
		
		NumVariable xVar = (NumVariable)getVariable(xKey);
		NumVariable yVar = (NumVariable)getVariable(yKey);
		BootstrapNumVariable bootVar = (BootstrapNumVariable)getVariable(bootstrapKey);
		int count[] = bootVar.getUsageCounts();
		Point p = null;
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		
		for (int i=0 ; i<xVar.noOfValues() ; i++) {
			double x = xVar.doubleValueAt(i);
			double y = yVar.doubleValueAt(i);
			p = getScreenPoint(x, y, p);
			if (count[i] > 0) {
				g.setColor(kSampColor);
				if (count[i] == 1)
					drawCross(g, p);
				else {
					String s = digitString[Math.min(count[i], 10)];
					int sWidth = fm.stringWidth(s);
					g.drawString(s, p.x - sWidth / 2, p.y + ascent / 2);
				}
			}
			else {
				g.setColor(kPopnColor);
				drawCross(g, p);
			}
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
	

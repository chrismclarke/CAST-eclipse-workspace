package stdError;

import java.awt.*;

import dataView.*;
import axis.*;


public class PropnBigErrorView extends ParamAndStatView {
//	static public final String PROPN_BIG_ERROR_VIEW = "propnBiggerView";
	
	static final private Color kSmallSampleColor = new Color(0x009900);
	static final private Color kBigBackgroundColor = new Color(0xDBE9FF);
	
	private double cutoff;
	
	public PropnBigErrorView(DataSet theData, XApplet applet,
								NumCatAxis theAxis, String normalKey, String bootstrapKey, SummaryDataSet summaryData,
								String statKey, String errorKey, NumValue paramValue, String paramName,
								String statisticName, double cutoff) {
		super(theData, applet, theAxis, normalKey, bootstrapKey, summaryData,
													statKey, errorKey, paramValue, paramName, statisticName);
		this.cutoff = cutoff;
	}
	
	protected void paintBackground(Graphics g) {
		NumVariable statVar = (NumVariable)summaryData.getVariable(statKey);
		int selectedSumIndex = summaryData.getSelection().findSingleSetFlag();
		if (selectedSumIndex < 0 || selectedSumIndex >= statVar.noOfValues())
			selectedSumIndex = statVar.noOfValues() - 1;
		
		FontMetrics fm = g.getFontMetrics();
		int popnBaseline = fm.getAscent() + 3;
		int sampBaseline = popnBaseline + fm.getAscent() + fm.getDescent() + 5;
		int errorBaseline = sampBaseline + fm.getAscent() + fm.getDescent() + 5;

		g.setColor(kBigBackgroundColor);
		int cutoffPos = translateToScreen(axis.numValToRawPosition(cutoff), 0, null).x;
		g.fillRect(cutoffPos, 0, getSize().width - cutoffPos, getSize().height);
		
		g.setColor(kPopnLabelColor);
		String paramString = paramName + " = " + paramValue.toString();
		int paramWidth = fm.stringWidth(paramString);
		int paramStart = getSize().width - paramWidth - 2;
		g.drawString(paramString, paramStart, popnBaseline);
		
		NumValue stat = (NumValue)statVar.valueAt(selectedSumIndex);
		g.setColor(kSampColor);
		String statString = statisticName + " = " + stat.toString();
		int statWidth = fm.stringWidth(statString);
		int statStart = getSize().width - statWidth - 2;
		g.drawString(statString, statStart, sampBaseline);
		
		NumVariable errorVar = (NumVariable)summaryData.getVariable(errorKey);
		NumValue error = (NumValue)errorVar.valueAt(selectedSumIndex);
		String errorString = "Error = " + error.toString();
		Font stdFont = g.getFont();
		Font boldFont = new Font(stdFont.getName(), Font.BOLD, stdFont.getSize());
		g.setFont(boldFont);
		g.setColor(Color.red);
		int errorWidth = g.getFontMetrics().stringWidth(errorString);
		int errorStart = getSize().width - errorWidth - 2;
		g.drawString(errorString, errorStart, errorBaseline);
		g.setFont(stdFont);
	}
	
	protected void setBootstrapColor(Graphics g, NumValue theVal) {
		g.setColor(theVal.toDouble() > cutoff ? kSampColor : kSmallSampleColor);
	}
}
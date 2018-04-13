package stdError;

import java.awt.*;

import dataView.*;
import axis.*;
import imageGroups.*;


public class MeanBootstrapErrorView extends ParamAndStatView {
	static final private int kValueGap = 4;
	
	
	public MeanBootstrapErrorView(DataSet theData, XApplet applet,
								NumCatAxis theAxis, String normalKey, String bootstrapKey, SummaryDataSet summaryData,
								String statKey, String errorKey, NumValue paramValue) {
		super(theData, applet, theAxis, normalKey, bootstrapKey, summaryData,
													statKey, errorKey, paramValue, null, null);
		MeanSDImages.loadMeanSD(applet);
	}
	
	protected void paintBackground(Graphics g) {
		drawNormalBackground(g);
		
		NumVariable statVar = (NumVariable)summaryData.getVariable(statKey);
		int selectedSumIndex = summaryData.getSelection().findSingleSetFlag();
		if (selectedSumIndex < 0 || selectedSumIndex >= statVar.noOfValues())
			selectedSumIndex = statVar.noOfValues() - 1;
		
		FontMetrics fm = g.getFontMetrics();
		int paramStatAscent = Math.max(fm.getAscent(), MeanSDImages.kParamAscent);
		int paramStatDescent = Math.max(fm.getDescent(), MeanSDImages.kParamDescent);
		
		int popnBaseline = paramStatAscent + 3;
		int sampBaseline = popnBaseline + paramStatAscent + paramStatDescent + 5;
		int errorBaseline = sampBaseline + paramStatDescent + fm.getAscent() + 5;

		g.setColor(kPopnLabelColor);
		int paramPos = translateToScreen(axis.numValToRawPosition(paramValue.toDouble()), 0, null).x;
		int paramWidth = paramValue.stringWidth(g);
		int paramStart = paramPos - (paramWidth + MeanSDImages.kParamWidth) / 2 + kValueGap;
		g.drawImage(MeanSDImages.popnMean2, paramStart, popnBaseline - MeanSDImages.kParamAscent,
											MeanSDImages.kParamWidth, MeanSDImages.kParamHeight, this);
		paramStart += MeanSDImages.kParamWidth + kValueGap;
		paramValue.drawRight(g, paramStart, popnBaseline);
		
		g.drawLine(paramPos, popnBaseline + 3, paramPos, getSize().height);
		
		
		NumValue stat = (NumValue)statVar.valueAt(selectedSumIndex);
		g.setColor(kSampColor);
		int statPos = translateToScreen(axis.numValToRawPosition(stat.toDouble()), 0, null).x;
		int statWidth = stat.stringWidth(g);
		int statStart = statPos - (statWidth + MeanSDImages.kParamWidth) / 2 + kValueGap;
		g.drawImage(MeanSDImages.sampMean, statStart, sampBaseline - MeanSDImages.kParamAscent,
											MeanSDImages.kParamWidth, MeanSDImages.kParamHeight, this);
		statStart += MeanSDImages.kParamWidth + kValueGap;
		stat.drawRight(g, statStart, sampBaseline);
		
		g.drawLine(statPos, sampBaseline + 3, statPos, getSize().height);
		
		
		NumVariable errorVar = (NumVariable)summaryData.getVariable(errorKey);
		NumValue error = (NumValue)errorVar.valueAt(selectedSumIndex);
		int arrowVertPos = errorBaseline - fm.getAscent() / 2;
		int direction = (statPos > paramPos) ? 1 : -1;
		g.setColor(Color.red);
		for (int i=-1 ; i<=1 ; i++)
			g.drawLine(paramPos + direction, arrowVertPos + i, statPos - direction, arrowVertPos + i);
		
		g.drawLine(statPos, arrowVertPos - direction, statPos - direction * kArrowHead, arrowVertPos - direction * kArrowHead - direction);
		g.drawLine(statPos, arrowVertPos + direction, statPos - direction * kArrowHead, arrowVertPos + direction * kArrowHead + direction);
		
		String errorString = "Error = " + error.toString();
		Font stdFont = g.getFont();
		Font boldFont = new Font(stdFont.getName(), Font.BOLD, stdFont.getSize());
		g.setFont(boldFont);
		int errorWidth = g.getFontMetrics().stringWidth(errorString);
		int errorStart = getSize().width - errorWidth - 2;
		g.drawString(errorString, errorStart, errorBaseline);
		g.setFont(stdFont);
	}
}
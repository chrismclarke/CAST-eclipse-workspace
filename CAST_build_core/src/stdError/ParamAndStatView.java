package stdError;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class ParamAndStatView extends StackedPlusNormalView {
	static final private Color kPopnColor = new Color(0xBBBBBB);
	static final protected Color kPopnLabelColor = Color.gray;
	static final protected Color kSampColor = Color.blue;
	
	static final private int kTopBorder = 40;
	static final protected int kArrowHead = 3;
	
	static final private String digitString[] = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "+"};
	
	private String bootstrapKey;
	protected SummaryDataSet summaryData;
	protected String statKey, errorKey;
	protected NumValue paramValue;
	protected String paramName, statisticName;
	
	public ParamAndStatView(DataSet theData, XApplet applet,
								NumCatAxis theAxis, String normalKey, String bootstrapKey, SummaryDataSet summaryData,
								String statKey, String errorKey, NumValue paramValue, String paramName,
								String statisticName) {
		super(theData, applet, theAxis, normalKey);
		this.bootstrapKey = bootstrapKey;
		this.summaryData = summaryData;
		this.statKey = statKey;
		this.errorKey = errorKey;
		this.paramValue = paramValue;
		this.paramName = paramName;
		this.statisticName = statisticName;
		getViewBorder().top = kTopBorder;
		
		setForeground(bootstrapKey == null ? kSampColor : kPopnColor);
	}
	
	public void setTargetValue(NumValue paramValue) {
		this.paramValue = paramValue;
	}
	
	protected void drawNormalBackground(Graphics g) {
		super.paintBackground(g);
	}
	
	protected void paintBackground(Graphics g) {
		NumVariable statVar = (NumVariable)summaryData.getVariable(statKey);
		int selectedSumIndex = summaryData.getSelection().findSingleSetFlag();
		if (selectedSumIndex < 0 || selectedSumIndex >= statVar.noOfValues())
			selectedSumIndex = statVar.noOfValues() - 1;
		
		FontMetrics fm = g.getFontMetrics();
		int baseline = 0;
		int paramPos = 0;
		
		if (paramValue != null) {
			drawNormalBackground(g);
			
			g.setColor(kPopnLabelColor);
			paramPos = translateToScreen(axis.numValToRawPosition(paramValue.toDouble()), 0, null).x;
			baseline += fm.getAscent() + 3;
			String paramString = paramName + " = " + paramValue.toString();
			int paramWidth = fm.stringWidth(paramString);
			int paramStart = Math.max(2, Math.min(getSize().width - paramWidth - 2, paramPos - paramWidth / 2));
			g.drawString(paramString, paramStart, baseline);
			
			g.drawLine(paramPos, baseline + 3, paramPos, getSize().height);
		}
		
		NumValue stat = (NumValue)statVar.valueAt(selectedSumIndex);
		g.setColor(kSampColor);
		int statPos = translateToScreen(axis.numValToRawPosition(stat.toDouble()), 0, null).x;
		baseline += fm.getAscent() + fm.getDescent() + 5;
		String statString = statisticName + " = " + stat.toString();
		int statWidth = fm.stringWidth(statString);
		int statStart = statPos - statWidth / 2;
		if (statPos > paramPos && statStart < paramPos)
			statStart = paramPos + 3;
		else if (statPos < paramPos && statStart + statWidth > paramPos)
			statStart = paramPos - 3 - statWidth;
		statStart = Math.max(2, Math.min(getSize().width - statWidth - 2, statStart));
		g.drawString(statString, statStart, baseline);
		
		g.drawLine(statPos, baseline + 3, statPos, getSize().height);
		
		if (paramValue != null) {
			NumVariable errorVar = (NumVariable)summaryData.getVariable(errorKey);
			NumValue error = (NumValue)errorVar.valueAt(selectedSumIndex);
			baseline += fm.getAscent() + fm.getDescent() + 5;
			int arrowVertPos = baseline - fm.getAscent() / 2;
			int direction = (statPos > paramPos) ? 1 : -1;
			g.setColor(Color.red);
			
			int lowX = Math.min(paramPos, statPos);
			int highX = Math.max(paramPos, statPos);
			g.fillRect(lowX + 1, arrowVertPos - 1, highX - lowX - 1, 3);
			
//			for (int i=-1 ; i<=1 ; i++)
//				g.drawLine(paramPos + direction, arrowVertPos + i, statPos - direction, arrowVertPos + i);
//			
//			g.drawLine(statPos, arrowVertPos - direction, statPos - direction * kArrowHead, arrowVertPos - direction * kArrowHead - direction);
//			g.drawLine(statPos, arrowVertPos + direction, statPos - direction * kArrowHead, arrowVertPos + direction * kArrowHead + direction);
			
			if (highX - lowX >= kArrowHead + 2) {
				g.drawLine(statPos - direction, arrowVertPos - 1, statPos - direction* (1 + kArrowHead), arrowVertPos - 1 - kArrowHead);
				g.drawLine(statPos - direction, arrowVertPos + 1, statPos - direction* (1 + kArrowHead), arrowVertPos + 1 + kArrowHead);
			}
			
			String errorString = getApplet().translate("Error") + " = " + error.toString();
			Font stdFont = g.getFont();
			Font boldFont = new Font(stdFont.getName(), Font.BOLD, stdFont.getSize());
			g.setFont(boldFont);
//			int errorWidth = g.getFontMetrics().stringWidth(errorString);
			int errorStart = Math.max(paramPos, statPos) + 4;
//			int errorStart = (paramPos < getSize().width / 2) ? getSize().width - errorWidth - 2 : 2;
			g.drawString(errorString, errorStart, baseline);
			g.setFont(stdFont);
		}
	}
	
	protected void setBootstrapColor(Graphics g, NumValue theVal) {
	}
	
	public void paintView(Graphics g) {
		super.paintView(g);
		
		if (bootstrapKey != null) {
			BootstrapNumVariable sample = (BootstrapNumVariable)getVariable(bootstrapKey);
			int usageCounts[] = sample.getUsageCounts();
			
			NumVariable popn = getNumVariable();
			Point thePoint = null;
			
			Font stdFont = g.getFont();
			g.setFont(getApplet().getSmallBoldFont());
			FontMetrics fm = g.getFontMetrics();
			int halfAscent = fm.getAscent() / 2;
			int halfDigitWidth = fm.stringWidth("0") / 2;
			g.setColor(kSampColor);
			ValueEnumeration e = popn.values();
			int index = 0;
			while (e.hasMoreValues()) {
				NumValue nextVal = (NumValue)e.nextValue();
				thePoint = getScreenPoint(index, nextVal, thePoint);
				setBootstrapColor(g, nextVal);
				if (thePoint != null) {
					if (usageCounts[index] == 1)
						drawMark(g, thePoint, 0);
					else if (usageCounts[index] > 1)
						g.drawString(digitString[Math.min(usageCounts[index], 10)], thePoint.x - halfDigitWidth,
																												thePoint.y + halfAscent);
				}
				index++;
			}
			g.setFont(stdFont);
		}
	}
}
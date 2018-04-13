package multiRegn;

import java.awt.*;

import dataView.*;
import models.*;


public class MultiAnovaTableView extends DataView {
	static final private String kMaxDFString = "999";
	
	static final private int kLeftRightBorder = 2;
	static final private int kConditWidth = 15;
	static final private int kBracketGap = 2;
	static final private int kColumnGap = 12;
	static final private int kLineSpace = 3;
	static final private int kLineGap = 2 * kLineSpace + 1;
	static final private int kMinSourceWidth = 90;
	
	private String kResidualString, kTotalString;
	
	static final private Value kSourceString = new LabelValue("Source");
	static final private Value kSSString = new LabelValue("Ssq");
	static final private Value kDFString = new LabelValue("Df");
	
	private String modelKey, yKey;
	private String[] xKey;
	private String[] xName;
	private NumValue maxSsq;
	
	private Font boldFont;
	
	private int currentDragIndex = Model3DragView.B0_DRAG;
	
	private boolean initialised = false;
	
	private int ascent, descent, tableHeight, tableWidth;
	private int ssqColWidth, dfColWidth, sourceColWidth;
	private int explainedHeight, xNamesPerRow;
	
	private SSComponent[] bestSsq;
	
	public MultiAnovaTableView(DataSet theData, XApplet applet, String modelKey, String yKey, String[] xKey,
							String[] xName, NumValue maxSsq) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		kResidualString = applet.translate("Residual");
		kTotalString = "Total";
		MultiRegnImages.loadMultiRegn(applet);
		this.modelKey = modelKey;
		this.yKey = yKey;
		this.xKey = xKey;
		this.xName = xName;
		this.maxSsq = maxSsq;
		boldFont = applet.getStandardBoldFont();
	}
	
	public void setDragIndex(int newDragIndex) {
		if (currentDragIndex != newDragIndex) {
			currentDragIndex = newDragIndex;
			repaint();
		}
	}
	
	protected boolean initialise(Graphics g) {
		if (!initialised) {
			FontMetrics fm = g.getFontMetrics();
			ascent = fm.getAscent();
			descent = fm.getDescent();
			
			Font standardFont = g.getFont();
			g.setFont(boldFont);
			fm = g.getFontMetrics();
			
			ssqColWidth = Math.max(kSSString.stringWidth(g), maxSsq.stringWidth(g));
			dfColWidth = Math.max(kDFString.stringWidth(g), fm.stringWidth(kMaxDFString));
			sourceColWidth = Math.max(kSourceString.stringWidth(g),
							Math.max(fm.stringWidth(kResidualString), 
							Math.max(fm.stringWidth(kTotalString), kMinSourceWidth)));
			
			int maxExplanWidth = 0;
			g.setFont(standardFont);
			fm = g.getFontMetrics();
			for (int i=0 ; i<xName.length ; i++)
				maxExplanWidth = Math.max(maxExplanWidth, fm.stringWidth(xName[i]));
			sourceColWidth = Math.max(sourceColWidth, kConditWidth + maxExplanWidth);
			xNamesPerRow = sourceColWidth / (maxExplanWidth + kConditWidth);
			
			tableWidth = 2 * kLeftRightBorder + MultiRegnImages.kExplainedWidth
													+ MultiRegnImages.kBracketWidth + 2 * kBracketGap;
			tableWidth += sourceColWidth + ssqColWidth + dfColWidth + 2 *kColumnGap;
			
			tableHeight = 3 * (ascent + descent) + 2 * kLineGap;
			explainedHeight = 0;
			for (int i=0 ; i<xName.length ; i++)
				explainedHeight += (1 + i / xNamesPerRow) * (ascent + descent) + kLineSpace;
			explainedHeight -= kLineSpace;
			tableHeight += explainedHeight + kLineSpace;
			
			MultipleRegnModel model = (MultipleRegnModel)getVariable(modelKey);
			bestSsq = model.getBestSsqComponents(yKey);
			
			initialised = true;
			return true;
		}
		else
			return false;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		int headingBaseline = ascent;
		int totalBaseline = tableHeight - descent;
		int residualBaseline = totalBaseline - ascent - descent - kLineGap;
		
		int sourceLeft = kLeftRightBorder + MultiRegnImages.kExplainedWidth
													+ MultiRegnImages.kBracketWidth + 2 * kBracketGap;
		int ssqRight = sourceLeft + sourceColWidth + kColumnGap + ssqColWidth;
		int ssqCenter = ssqRight - ssqColWidth / 2;
		int dfRight = ssqRight + kColumnGap + dfColWidth;
		int dfCenter = dfRight - dfColWidth / 2;
		
		int explainedCenter = headingBaseline + descent + kLineSpace + explainedHeight / 2;
		int explainedBaseline = headingBaseline + descent + kLineGap + ascent;
		int explainedTop = headingBaseline + descent + kLineGap - kLineSpace + 2;
		int explainedBottom = residualBaseline - ascent - 2;
		
		Font standardFont = g.getFont();
		g.setFont(boldFont);
		
		kSourceString.drawRight(g, sourceLeft, headingBaseline);
		kSSString.drawCentred(g, ssqCenter, headingBaseline);
		kDFString.drawCentred(g, dfCenter, headingBaseline);
		g.drawLine(sourceLeft - 4, headingBaseline + descent + kLineSpace, tableWidth,
																	headingBaseline + descent + kLineSpace);
		
		g.drawString(kTotalString, sourceLeft, totalBaseline);
		g.drawLine(sourceLeft - 4, residualBaseline + descent + kLineSpace, tableWidth,
																	residualBaseline + descent + kLineSpace);
		g.setColor(Color.red);
		g.drawString(kResidualString, sourceLeft, residualBaseline);
		g.setColor(getForeground());
		
		g.setFont(standardFont);
		
		g.drawImage(MultiRegnImages.explained, kLeftRightBorder,
									explainedCenter - MultiRegnImages.kExplainedHeight / 2, this);
		int bracketLeft = kLeftRightBorder + MultiRegnImages.kExplainedWidth + kBracketGap;
		int bracketCenterTop = explainedCenter - MultiRegnImages.kBracketCenterHt / 2;
		g.drawLine(bracketLeft + 3, explainedTop + MultiRegnImages.kBracketEndHt,
															bracketLeft + 3, bracketCenterTop - 1);
		g.drawLine(bracketLeft + 3, bracketCenterTop + MultiRegnImages.kBracketCenterHt,
								bracketLeft + 3, explainedBottom - MultiRegnImages.kBracketEndHt - 1);
		g.drawImage(MultiRegnImages.bracketTop, bracketLeft, explainedTop, this);
		g.drawImage(MultiRegnImages.bracketBottom, bracketLeft,
											explainedBottom - MultiRegnImages.kBracketEndHt, this);
		g.drawImage(MultiRegnImages.bracketCenter, bracketLeft,
									explainedCenter - MultiRegnImages.kBracketCenterHt / 2, this);
		
		NumValue ssq = new NumValue(0.0, maxSsq.decimals);
		NumValue df = new NumValue(0, 0);
		
		double totalSsq = bestSsq[0].ssq;
		int totalDF = bestSsq[0].df;
		for (int i=0 ; i<xName.length ; i++) {
			totalSsq += bestSsq[i+2].ssq;
			totalDF += bestSsq[i+2].df;
		}
		if (currentDragIndex > 0) {
			ssq.setValue(totalSsq);
			ssq.drawLeft(g, ssqRight, totalBaseline);
			df.setValue(totalDF);
			df.drawLeft(g, dfRight, totalBaseline);
		}
		
		g.setColor(Color.red);
		double rssq = residSsq();
		ssq.setValue(rssq);
		ssq.drawLeft(g, ssqRight, residualBaseline);
		df.setValue(totalDF - currentDragIndex);
		df.drawLeft(g, dfRight, residualBaseline);
		g.setColor(getForeground());
		
		double explainedSsq = 0.0;
		for (int i=0 ; i<xName.length ; i++) {
			g.drawString(xName[i], sourceLeft, explainedBaseline);
			
			if (i > 0) {
				int conditBaseline = explainedBaseline;
				FontMetrics fm = g.getFontMetrics();
				int startHoriz = sourceLeft + fm.stringWidth(xName[i]);
				boolean drawConditLine = true;
				for (int j=0 ; j<i ; j++) {
					if ((j+1) % xNamesPerRow == 0) {
						conditBaseline += (ascent + descent);
						startHoriz = sourceLeft;
						drawConditLine = true;
					}
					if (drawConditLine) {
						g.drawLine(startHoriz + kConditWidth / 2, conditBaseline - ascent,
												startHoriz + kConditWidth / 2, conditBaseline + descent);
						startHoriz += kConditWidth;
						drawConditLine = false;
					}
					g.drawString(xName[j], startHoriz, conditBaseline);
					int nameWidth = fm.stringWidth(xName[j]);
					startHoriz += nameWidth;
					if (j < (i-1)) {
						g.drawString(", ", startHoriz, conditBaseline);
						startHoriz += fm.stringWidth(", ");
					}
				}
			}
			if (i <= currentDragIndex-1) {
				if (i == currentDragIndex-1)
					ssq.setValue(totalSsq - rssq - explainedSsq);
				else {
					ssq.setValue(bestSsq[i+2].ssq);
					explainedSsq += ssq.toDouble();
				}
				ssq.drawLeft(g, ssqRight, explainedBaseline);
				
				df.setValue(1);
				df.drawLeft(g, dfRight, explainedBaseline);
			}
			explainedBaseline += (1 + i / xNamesPerRow) * (ascent + descent) + kLineSpace;
		}
	}
	
	private double residSsq() {
		NumVariable y = (NumVariable)getVariable(yKey);
		MultipleRegnModel model = (MultipleRegnModel)getVariable(modelKey);
		ValueEnumeration xe[] = new ValueEnumeration[xKey.length];
		for (int i=0 ; i<xe.length ; i++)
			xe[i] = ((NumVariable)getVariable(xKey[i])).values();
		ValueEnumeration ye = y.values();
		double rss = 0.0;
		double xi[] = new double[xe.length];
		while (ye.hasMoreValues()) {
			for (int i=0 ; i<xe.length ; i++)
				xi[i] = xe[i].nextDouble();
			double yVal = ye.nextDouble();
			double resid = yVal - model.evaluateMean(xi);
			rss += resid * resid;
		}
		return rss;
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		return new Dimension(tableWidth, tableHeight);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
}
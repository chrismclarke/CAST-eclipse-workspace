package variance;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import coreGraphics.*;
import images.*;


public class ScaledChi2View extends StackedPlusNormalView {
	
	static final private Color kLabelColor = new Color(0x999999);
	
	static final private String sigma2File = "anova/sigma2Blue.gif";
	static final private String chi2File = "anova/chi2Grey.gif";
	static final private int kMaxWait = 30000;		//		30 seconds
	static final private int kChi2Ascent = 15;
	static final private int kChi2Width = 16;
	static final private int kTimesSize = 5;
	
	private String chi2Key;
	private NumValue variance, df;
	
	private Image sigma2Image, chi2Image;
	
	private boolean isMeanSumOfSquares = true;
	
	public ScaledChi2View(DataSet theData, XApplet applet, NumCatAxis theAxis, String chi2Key,
									int varianceDecimals) {
		super(theData, applet, theAxis, chi2Key);
		Insets border = getViewBorder();
		border.right = 0;
		
		this.chi2Key = chi2Key;
		variance = new NumValue(1.0, varianceDecimals);
		df = new NumValue(1.0, 0);
		
		MediaTracker tracker = new MediaTracker(this);
			sigma2Image = CoreImageReader.getImage(sigma2File);
			chi2Image = CoreImageReader.getImage(chi2File);
		tracker.addImage(sigma2Image, 0);
		tracker.addImage(chi2Image, 0);
		try {
			tracker.waitForAll(kMaxWait);
		} catch (InterruptedException e) {
		}
	}
	
	public void setIsMeanSumOfSquares(boolean isMeanSumOfSquares) {
		this.isMeanSumOfSquares = isMeanSumOfSquares;
	}
	
	public void paintView(Graphics g) {
		GammaDistnVariable distn = (GammaDistnVariable)getVariable(chi2Key);
		df.setValue(distn.getShape().toDouble() * 2.0);
		double chi2Mean = distn.getScale().toDouble() * distn.getShape().toDouble();
		variance.setValue(distn.getScale().toDouble() * 0.5);
		
		
		if (isMeanSumOfSquares)
			try {
				int meanPos = axis.numValToPosition(chi2Mean);
				int meanHoriz = translateToScreen(meanPos, 0, null).x;
				g.setColor(Color.blue);
				g.drawLine(meanHoriz, 0, meanHoriz, getSize().height);
				g.setColor(getForeground());
				
				g.drawImage(sigma2Image, meanHoriz + 3, 3, this);
			} catch (AxisException e) {
			}
		
		super.paintView(g);
		
		g.setColor(kLabelColor);
		
		String dfString = "(" + df.toString() + " df)";
		FontMetrics fm = g.getFontMetrics();
		int dfStringWidth = fm.stringWidth(dfString);
		int baseline = 3 + kChi2Ascent;
		int startHoriz = getSize().width - 3 - dfStringWidth;
		g.drawString(dfString, startHoriz, baseline);
		
		startHoriz -= (kChi2Width + 2);
		g.drawImage(chi2Image, startHoriz, 3, this);
		
		startHoriz -= (3 + kTimesSize);
		g.drawLine(startHoriz, baseline - 1, startHoriz + kTimesSize, baseline - 1 - kTimesSize);
		g.drawLine(startHoriz, baseline - 1 - kTimesSize, startHoriz + kTimesSize, baseline - 1);
		
		startHoriz -= 3;
		
		if (isMeanSumOfSquares) {
			int scaleWidth = Math.max(variance.stringWidth(g), df.stringWidth(g)) + 2;
			
			int lineHt = baseline - (kTimesSize + 1) / 2;
			g.drawLine(startHoriz - scaleWidth, lineHt, startHoriz, lineHt);
			int fractionCenter = startHoriz - scaleWidth / 2;
			variance.setValue(variance.toDouble() * df.toDouble());
			variance.drawCentred(g, fractionCenter, lineHt - 2);
			df.drawCentred(g, fractionCenter, lineHt + 2 + fm.getAscent());
		}
		else
			variance.drawLeft(g, startHoriz, baseline);
	}
}
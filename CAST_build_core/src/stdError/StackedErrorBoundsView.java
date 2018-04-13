package stdError;

import java.awt.*;

import dataView.*;
import axis.*;


public class StackedErrorBoundsView extends Stacked2SdBoundsView {
	static final private Color kExtremeShade = new Color(0xFFDDDD);
	
	private String estimateKey;
	private double target;
	
	public StackedErrorBoundsView(DataSet theData, XApplet applet, NumCatAxis theAxis, String estimateKey, double target) {
		super(theData, applet, theAxis);
		this.estimateKey = estimateKey;
		this.target = target;
	}
	
	public double findError95() {
		NumValue sortedEst[] = ((NumVariable)getVariable(estimateKey)).getSortedData();
		int n = sortedEst.length;
		double extremeTarget = 0.05 * (n + 1);
		int low = 0;
		int high = n - 1;
		
		double lastError = 0.0;
		while (extremeTarget >= 1.0) {
			if (target - sortedEst[low].toDouble() > sortedEst[high].toDouble() - target) {
				lastError = target - sortedEst[low].toDouble();
				low ++;
			}
			else {
				lastError = sortedEst[high].toDouble() - target;
				high --;
			}
			extremeTarget -= 1.0;
		}
		double nextError = Math.max(target - sortedEst[low].toDouble(), sortedEst[high].toDouble() - target);
		return lastError * (1.0 - extremeTarget) + nextError * extremeTarget;
	}
	
	protected void paintBackground(Graphics g) {
		if (showBounds) {
			double error95 = findError95();
			int lowPos = axis.numValToRawPosition(-error95);
			int highPos = axis.numValToRawPosition(error95);
			int lowScreen = translateToScreen(lowPos, 0, null).x;
			int highScreen = translateToScreen(highPos, 0, null).x;
			
			g.setColor(kExtremeShade);
			g.fillRect(0, 0, lowScreen, getSize().height);
			g.fillRect(highScreen, 0, getSize().width - highScreen, getSize().height);
			
			g.setColor(getForeground());
		}
	}
	
	protected void fiddleColor(Graphics g, int index) {
		if (showBounds) {
			double error95 = findError95();
			double y = getNumVariable().doubleValueAt(index);
			if (y < -error95 || y > error95)
				g.setColor(Color.black);
			else
				g.setColor(getForeground());
		}
	}
}
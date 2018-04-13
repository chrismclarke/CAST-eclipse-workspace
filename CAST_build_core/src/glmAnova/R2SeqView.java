package glmAnova;

import java.awt.*;

import dataView.*;
import valueList.*;
import models.*;


public class R2SeqView extends ValueView implements SetLastExplanInterface {
//	static public final String R2_SEQ_VIEW = "r2SeqValue";
	static private final NumValue kMaxR2 = new NumValue(1.0, 3);
	
	protected String componentKey[];
	private String labelString = "R\u00B2 =";
	
	private int lastSeparateX;
	
	public R2SeqView(DataSet theData, String[] componentKey, XApplet applet) {
		super(theData, applet);
		this.componentKey = componentKey;
		
		lastSeparateX = componentKey.length - 3;		//	-1 for no explan
	}
	
	public void setLastExplanatory(int lastSeparateX) {
		this.lastSeparateX = lastSeparateX;
		redrawValue();
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(labelString);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return kMaxR2.stringWidth(g);
	}
	
	protected String getValueString() {
		CoreComponentVariable totalComp = (CoreComponentVariable)getVariable(componentKey[0]);
		double totalSsq = totalComp.getSsq();
		
		double explainedSsq = 0.0;
		for (int i=1 ; i<=lastSeparateX+1 ; i++) {
			CoreComponentVariable explainedComp = (CoreComponentVariable)getVariable(componentKey[i]);
			explainedSsq += explainedComp.getSsq();
		}
		
		NumValue r2 = new NumValue(explainedSsq / totalSsq, kMaxR2.decimals);
		return r2.toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		g.drawString(labelString, startHoriz, baseLine);
	}
	
	protected boolean highlightValue() {
		return false;
	}
}

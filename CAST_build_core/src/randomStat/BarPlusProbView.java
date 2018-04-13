package randomStat;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class BarPlusProbView extends DataView implements DataPlusDistnInterface {
//	static final public String BAR_BINOMIAL = "barBinomial";
	
	static final private int kHalfDataBarWidth = 5;
	static final private int kHalfTheoryBarWidth = 2;
	
	private String dataKey, distnKey;
	private HorizAxis catAxis;
	private VertAxis probAxis;
	
	private int densityType = DISCRETE_DISTN;
	private Color distnColor = Color.lightGray;
	
	public BarPlusProbView(DataSet theData, XApplet applet, String dataKey,
												String distnKey, HorizAxis catAxis, VertAxis probAxis) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.dataKey = dataKey;
		this.distnKey = distnKey;
		this.catAxis = catAxis;
		this.probAxis = probAxis;
	}
	
	public void setShowDensity (int densityType) {
		this.densityType = densityType;
		repaint();
	}
	
	public void setDensityColor(Color c) {
		distnColor = c;
	}
	
	public void paintView(Graphics g) {
		Point topLeft = null;
		if (densityType == DISCRETE_DISTN) {
			CatDistnVariable distn = (CatDistnVariable)getVariable(distnKey);
			double p[] = distn.getProbs();
			
			g.setColor(distnColor);
			
			for (int i=0 ; i<p.length ; i++) {
				int x = catAxis.catValToPosition(i);
				int y = probAxis.numValToRawPosition(p[i]);
				topLeft = translateToScreen(x, y, topLeft);
				g.fillRect(topLeft.x - kHalfTheoryBarWidth, topLeft.y, 2 * kHalfTheoryBarWidth + 1,
																							getSize().height - topLeft.y);
			}
		}
		
		g.setColor(Color.black);
		CatVariable samp = (CatVariable)getVariable(dataKey);
		if (samp != null) {
			int count[] = samp.getCounts();
			int total = 0;
			for (int i=0 ; i<count.length ; i++)
				total += count[i];
			if (total == 0)
				return;
			double p[] = new double[count.length];
			for (int i=0 ; i<count.length ; i++)
				p[i] = count[i] / (double)total;
			
			for (int i=0 ; i<p.length ; i++) {
				int x = catAxis.catValToPosition(i);
				int y = probAxis.numValToRawPosition(p[i]);
				topLeft = translateToScreen(x, y, topLeft);
				g.drawRect(topLeft.x - kHalfDataBarWidth, topLeft.y, 2 * kHalfDataBarWidth,
																					getSize().height - topLeft.y);
			}
		}
	}
	
	public void setDistnLabel(LabelValue label, Color labelColor) {
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
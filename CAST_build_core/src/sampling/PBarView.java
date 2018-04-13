package sampling;

import java.awt.*;

import dataView.*;
import axis.*;
import imageGroups.TickCrossImages;


public class PBarView extends DataView {
	static final private int kHalfBarWidth = 4;
	
	private String catKey;
	private HorizAxis catAxis;
	private VertAxis probAxis;
	
	public PBarView(DataSet theData, XApplet applet, String catKey,
											HorizAxis catAxis, VertAxis probAxis) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.catKey = catKey;
		this.catAxis = catAxis;
		this.probAxis = probAxis;
	}
	
	public void paintView(Graphics g) {
		CoreVariable variable = getVariable(catKey);
		if (variable == null) {
			Point questionCentre = translateToScreen(getSize().width / 2,
																					getSize().height / 2, null);
			
			g.drawImage(TickCrossImages.question,
									questionCentre.x - TickCrossImages.question.getWidth(this) / 2,
									questionCentre.y - TickCrossImages.question.getHeight(this) / 2, this);
		}
		else {
			double p[];
			if (variable instanceof CatVariable) {
				CatVariable v = (CatVariable)variable;
				int count[] = v.getCounts();
				int total = 0;
				for (int i=0 ; i<count.length ; i++)
					total += count[i];
				if (total == 0)
					return;
				p = new double[count.length];
				for (int i=0 ; i<count.length ; i++)
					p[i] = count[i] / (double)total;
			}
			else {
				CatDistnVariable v = (CatDistnVariable)variable;
				p = v.getProbs();
			}
			
			Point topLeft = null;
			for (int i=0 ; i<p.length ; i++) {
				g.setColor(Color.lightGray);
				
				int x = catAxis.catValToPosition(i);
				int y = probAxis.numValToRawPosition(p[i]);
				topLeft = translateToScreen(x, y, topLeft);
				g.fillRect(topLeft.x - kHalfBarWidth, topLeft.y, 2 * kHalfBarWidth, getSize().height - topLeft.y);
				
				g.setColor(getForeground());
				g.drawRect(topLeft.x - kHalfBarWidth - 1, topLeft.y - 1, 2 * kHalfBarWidth + 1,
																						getSize().height - topLeft.y + 1);
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
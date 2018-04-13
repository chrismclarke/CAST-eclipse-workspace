package cat;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;


public class SortBarView extends CatDataView {
	static final private int kHalfBarWidth = 4;
	static final private int kEndFrame = 100;
	static final private int kFramesPerSec = 40;
	
	static final public int TO_SORTED = 0;
	static final public int TO_ALPHABETIC = 1;
	
	private HorizAxis catAxis;
	private VertAxis freqAxis;
	
	private int rank[];
	private AxisLabel axisLabels[];
	
	public SortBarView(DataSet theData, XApplet applet, String catKey,
																				HorizAxis catAxis, VertAxis freqAxis) {
		super(theData, applet, catKey, NO_DRAG);
		this.catAxis = catAxis;
		this.freqAxis = freqAxis;
		
		CatVariable variable = (CatVariable)getVariable(catKey);
		int counts[] = variable.getCounts();
		int invRank[] = new int[counts.length];
		for (int i=0 ; i<invRank.length ; i++)
			invRank[i] = i;
		
		for (int i=1 ; i<counts.length ; i++)
			for (int j=i ; j>0 ; j--)
				if (counts[invRank[j]] > counts[invRank[j - 1]]) {
					int temp = invRank[j];
					invRank[j] = invRank[j - 1];
					invRank[j - 1] = temp;
				}
				else
					break;
		
		rank = new int[counts.length];
		for (int i=0 ; i<rank.length ; i++)
			rank[invRank[i]] = i;
		
		Vector labels = catAxis.getLabels();
		axisLabels = new AxisLabel[counts.length];
		for (int i=0 ; i<counts.length ; i++)
			axisLabels[i] = (AxisLabel)labels.elementAt(i);
	}
	
	public void paintView(Graphics g) {
		CatVariable variable = (CatVariable)getVariable(catKey);
		initialise(variable, g);
		
		for (int i=0 ; i<count.length ; i++) {
			double index = i + (rank[i] - i) * getCurrentFrame() / (double)kEndFrame;
			axisLabels[i].position = (index + 0.5) / count.length;
		}
		
		AxisLabel sortedLabels[] = new AxisLabel[axisLabels.length];
		for (int i=0 ; i<axisLabels.length ; i++) {
			sortedLabels[i] = axisLabels[i];
			for (int j=i ; j>0 ; j--)
				if (sortedLabels[j].position < sortedLabels[j - 1].position) {
					AxisLabel temp = sortedLabels[j];
					sortedLabels[j] = sortedLabels[j - 1];
					sortedLabels[j - 1] = temp;
				}
				else
					break;
		}
		
		Vector labels = catAxis.getLabels();
		labels.removeAllElements();
		for (int i=0 ; i<sortedLabels.length ; i++)
			labels.add(sortedLabels[i]);
		
		catAxis.repaint();
		
		int halfBarWidth = Math.max(getSize().width / count.length / 5, kHalfBarWidth);
		
		Point topLeft = null;
		for (int i=0 ; i<count.length ; i++) {
			g.setColor(getCatColor(i, true));
			
			int x0 = catAxis.catValToPosition(i);
			int x1 = catAxis.catValToPosition(rank[i]);
			
			int x = x0 + getCurrentFrame() * (x1 - x0) / kEndFrame;
			
			int y = freqAxis.numValToRawPosition(count[i]);
			topLeft = translateToScreen(x, y, topLeft);
			g.fillRect(topLeft.x - halfBarWidth, topLeft.y, 2 * halfBarWidth, getSize().height - topLeft.y);
		}
	}

//-----------------------------------------------------------------------------------

	public void doAnimation(int animationType) {
		if (animationType == TO_SORTED)
			animateFrames(0, kEndFrame, kFramesPerSec, null);
		else
			animateFrames(kEndFrame, -kEndFrame, kFramesPerSec, null);
	}
	
/*
	public void run() {
		catAxis.show(false);
		
		super.run();
		
		CatVariable yVar = (CatVariable)getVariable(catKey);
		if (getCurrentFrame() == 0)
			catAxis.setCatLabels(yVar);
		else {
			Value labels[] = new Value[count.length];
			for (int i=0 ; i<count.length ; i++)
				labels[rank[i]] = yVar.getLabel(i);
			
			CatVariable sortedVar = new CatVariable("");
			sortedVar.setLabels(labels);
			catAxis.setCatLabels(sortedVar);
		}
		
		catAxis.show(true);
	}
*/
}
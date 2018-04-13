package sampling;

import java.awt.*;

import dataView.*;

import survey.SamplePictView;


public class TreatmentPictView extends SamplePictView {
	
	private CatVariable treatVar;
	private String treatKey;
	private Value noTreatLabel, nextTreatLabel;
	
	public TreatmentPictView(DataSet theData, XApplet applet,
						int sampleSize, long popnRandomSeed, long randomSeed, int rows, int cols,
						int rowCycle, int maxHorizOffset, int maxVertOffset, String treatKey) {
		super(theData, applet, sampleSize, popnRandomSeed, randomSeed, rows, cols, rowCycle,
																														maxHorizOffset, maxVertOffset);
		this.treatKey = treatKey;
		treatVar = (CatVariable)theData.getVariable(treatKey);
		noTreatLabel = treatVar.getLabel(0);
		setNextTreatmentLabel(1);
	}
	
	public void doInitialisation(XApplet applet) {
		super.doInitialisation(applet);
		
		TreatmentImages.loadTreatments(applet);
	}
	
	public void setNextTreatmentLabel(int newTreatIndex) {
		nextTreatLabel = treatVar.getLabel(newTreatIndex);
	}
	
	public void clearSample() {
		for (int i=0 ; i<treatVar.noOfValues() ; i++)
			treatVar.setValueAt(noTreatLabel, i);
		highlightIndex = -1;
		nextTreatLabel = treatVar.getLabel(1);
		getData().variableChanged(treatKey);
	}
	
	public boolean addToSample (int index) {
		LabelValue oldTreat = (LabelValue)treatVar.valueAt(index);
		if (oldTreat != noTreatLabel)
			return false;
		
		highlightIndex = index;
		treatVar.setValueAt(nextTreatLabel, index);
		
		getData().variableChanged(treatKey);
		
		return true;
	}
	
	protected int drawHeading(Graphics g) {
		return 0;
	}
	
	protected void drawPicture(Graphics g, int x, int y, int valueIndex, boolean dimmed,
																								boolean isSuccess) {
		int treatIndex = treatVar.getItemCategory(valueIndex);
//		super.drawPicture(g, x, y, valueIndex, (treatIndex > 0), isSuccess);
		super.drawPicture(g, x, y, valueIndex, (treatIndex == 0), isSuccess);
		
		if (treatIndex == 0)
			return;
		
		int left = x - TreatmentImages.kWidth / 2 + horizOffset[valueIndex];
		int top = y - TreatmentImages.kHeight / 2 + vertOffset[valueIndex] + 8;
		g.drawImage(TreatmentImages.treatImage[treatIndex - 1], left, top, this);
	}
}
	

package boxPlotProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import boxPlot.*;


public class DragBoxHistoDotApplet extends DragBoxHistoApplet {
	static final private String QUARTER_SAMPLE_SIZE_PARAM = "quarterSampleSize";
	
	private int quarterSampleSize[];
	
	private XChoice histoDotChoice, sampleSizeChoice;
	private int currentHistoDot, currentSampleSizeIndex;
	
	public void setupApplet() {
		StringTokenizer st = new StringTokenizer(getParameter(QUARTER_SAMPLE_SIZE_PARAM));
		quarterSampleSize = new int[st.countTokens()];
		for (int i=0 ; i<quarterSampleSize.length ; i++) {
			String sizeString = st.nextToken();
			if (sizeString.charAt(0) == '*') {
				currentSampleSizeIndex = i;
				sizeString = sizeString.substring(1);
			}
			quarterSampleSize[i] = Integer.parseInt(sizeString);
		}
		
		super.setupApplet();
	}
	
	protected XPanel viewPanel(DataSet data) {
		XPanel thePanel = super.viewPanel(data);
		theView.setDisplayType(DragDistnShapeView.DOT_PLOT, quarterSampleSize[currentSampleSizeIndex]);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 5));
		
		thePanel.add(super.controlPanel(data));
		
			XPanel dotPanel = new XPanel();
			dotPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
			
				histoDotChoice = new XChoice(translate("Display"), XChoice.HORIZONTAL, this);
				histoDotChoice.addItem(translate("Dot plot"));
				histoDotChoice.addItem(translate("Histogram"));
				histoDotChoice.select(0);
				currentHistoDot = 0;
			dotPanel.add(histoDotChoice);
			
				sampleSizeChoice = new XChoice("n =", XChoice.HORIZONTAL, this);
				for (int i=0 ; i<quarterSampleSize.length ; i++)
					sampleSizeChoice.addItem(String.valueOf(quarterSampleSize[i] * 4));
				sampleSizeChoice.select(currentSampleSizeIndex);
			dotPanel.add(sampleSizeChoice);
			
		thePanel.add(dotPanel);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == histoDotChoice) {
			if (histoDotChoice.getSelectedIndex() != currentHistoDot) {
				currentHistoDot = histoDotChoice.getSelectedIndex();
				if (currentHistoDot == 0) {
					theView.setDisplayType(DragDistnShapeView.DOT_PLOT, quarterSampleSize[currentSampleSizeIndex]);
					sampleSizeChoice.setEnabled(true);
				}
				else {
					theView.setDisplayType(DragDistnShapeView.HISTOGRAM, quarterSampleSize[currentSampleSizeIndex]);
					sampleSizeChoice.setEnabled(false);
				}
			}
			return true;
		}
		else if (target == sampleSizeChoice) {
			if (sampleSizeChoice.getSelectedIndex() != currentSampleSizeIndex) {
				currentSampleSizeIndex = sampleSizeChoice.getSelectedIndex();
				theView.setDisplayType(DragDistnShapeView.DOT_PLOT, quarterSampleSize[currentSampleSizeIndex]);
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
	
}
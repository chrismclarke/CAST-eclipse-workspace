package exper2Prog;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;

import exper2.*;

public class AdjustedMeansApplet extends RawMeansApplet {
	
	private AdjustedOneFactorView treatDataView;
	private AdjustedMeanView treatMeanView[];
	
	private XChoice blockChoice;
	private int currentBlockIndex;
	
	protected XPanel meansPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			XPanel innerPanel = new InsetPanel(8, 4);
			innerPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																													VerticalLayout.VERT_CENTER, 3));
			
				XLabel label = new XLabel(translate("Adjusted means"), XLabel.CENTER, this);
				label.setFont(getStandardBoldFont());
			innerPanel.add(label);
			
				XPanel meansPanel = new XPanel();
				meansPanel.setLayout(new VerticalLayout(VerticalLayout.RIGHT,
																														VerticalLayout.VERT_CENTER, 3));
				
				CatVariable treatVar = (CatVariable)data.getVariable("treat");
				int nTreat = treatVar.noOfCategories();
				treatMeanView = new AdjustedMeanView[nTreat];
				for (int i=0 ; i<nTreat ; i++) {
					treatMeanView[i] = new AdjustedMeanView(data, this, "treat", i,
																										"block", 0, "lsAll", maxMean);
					meansPanel.add(treatMeanView[i]);
				}
			innerPanel.add(meansPanel);
			innerPanel.lockBackground(kParamBackgroundColor);
		
		thePanel.add(innerPanel);
			
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.6, 6));
		
			CatVariable dummyBlockVar = new CatVariable("");
			dummyBlockVar.readLabels(getParameter(SHORT_BLOCK_LABELS_PARAM));
		thePanel.add(ProportionLayout.LEFT, meansDisplayPanel(data, "block", "lsBlock",
																dummyBlockVar, "Left", XLabel.LEFT, translate("Block means")));
			
			CatVariable treatVar = (CatVariable)data.getVariable("treat");
		thePanel.add(ProportionLayout.RIGHT, meansDisplayPanel(data, "treat", "lsAll",
															treatVar, "Right", XLabel.RIGHT, translate("Adjusted treatment means")));
		return thePanel;
	}
	
	protected CoreOneFactorView getView(DataSet data, HorizAxis treatAxis, VertAxis yAxis,
																			String xKey, String yKey, String lsKey, int labelSide) {
		if (labelSide == XLabel.LEFT)
			return new CoreOneFactorView(data, this, treatAxis, yAxis, xKey, "y", lsKey);
		else {
			treatDataView = new AdjustedOneFactorView(data, this, treatAxis, yAxis, xKey, "y",
																																				"block", 0, lsKey);
			return treatDataView;
		}
	}
	
	protected XPanel sliderPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(50, 20, 50, 0);
		thePanel.setLayout(new BorderLayout(20, 0));
		
			XPanel sliderPanel = new XPanel();
			sliderPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
				maxExtra = Double.parseDouble(getParameter(BLOCK_N_MAX_EXTRA_PARAM));
				blockNSlider = new XNoValueSlider("0", String.valueOf(maxExtra),
																			translate("Addition to last block"), 0, kSliderMax, 0, this);
		
			sliderPanel.add(blockNSlider);
		
		thePanel.add("Center", sliderPanel);
		
			XPanel choicePanel = new XPanel();
			choicePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
				
				blockChoice = new XChoice(translate("Adjusted for block") + ":", XChoice.VERTICAL_CENTER, this);
				CatVariable blockVar = (CatVariable)data.getVariable("block");
				for (int i=0 ; i<blockVar.noOfCategories() ; i++)
					blockChoice.addItem(blockVar.getLabel(i).toString());
				blockChoice.addItem("Average");
			
			choicePanel.add(blockChoice);
		
		thePanel.add("East", choicePanel);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == blockChoice) {
			int newChoice = blockChoice.getSelectedIndex();
			if (newChoice != currentBlockIndex) {
				currentBlockIndex = newChoice;
				
				CatVariable blockVar = (CatVariable)data.getVariable("block");
				int nBlocks = blockVar.noOfCategories();
				int adjustBlock = newChoice < nBlocks ? newChoice : -1;
				treatDataView.setBlockIndex(adjustBlock);
				
				for (int i=0 ; i<treatMeanView.length ; i++)
					treatMeanView[i].setBlockIndex(adjustBlock);
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
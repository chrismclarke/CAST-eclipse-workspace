package controlProg;

import java.awt.*;

import dataView.*;
import utils.*;


public class Fixed2ChartApplet extends FixedChartApplet {
	static private final String MESSSAGE_WIDTH_PARAM = "messageWidth";
	static private final String MESSSAGE_PARAM = "message";
	
	protected void doDataSelection() {
		data.setSelection(data.getNumVariable().noOfValues() - 1);
	}
	
	protected XPanel createProblemView(DataSet data) {
		int noOfOptions = dataSetChoice.countItems();
		String messageText[] = new String[noOfOptions];
		for (int i=0 ; i<noOfOptions ; i++)
			messageText[i] = getParameter(MESSSAGE_PARAM + (i+1));
		int messageWidth = Integer.parseInt(getParameter(MESSSAGE_WIDTH_PARAM));
		
		XPanel messagePanel = new XPanel();
		messagePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		dataDescriptions = new XTextArea(messageText, 0, messageWidth, this);
		messagePanel.add(dataDescriptions);
		dataDescriptions.setFont(getStandardFont());
		dataDescriptions.lockBackground(Color.white);
		dataDescriptions.setForeground(Color.red);
		
		theView.setFrame(data.getNumVariable().noOfValues());
		doDataSelection();
		
		return messagePanel;
	}
}
package dotPlotProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import valueList.*;

import dotPlot.*;


public class SortListApplet extends XApplet {
	private final static String INIT_MESSAGE_PARAM = "initMessage";
	private final static String SORTED_MESSAGE_PARAM = "sortedMessage";
	private final static String ANSWER_MESSAGE_PARAM = "answerMessage";
	private final static String MESSSAGE_WIDTH_PARAM = "messageWidth";
	private final static String HILITE_INDEX_PARAM = "highlight";
	
	static final private int kInitMessage = 0;
	static final private int kSortedMessage = 1;
	static final private int kAnswerMessage = 2;
	
	private DataSet data;
	
	private XNoValueSlider sortSlider;
	private XButton answerButton;
	private XTextArea message;
	private boolean isSorted = false;
	
	private SortScrollContent theListContent;
	
	public void setupApplet() {
		
		data = getData();
		
		setLayout(new ProportionLayout(0.3, 10, ProportionLayout.HORIZONTAL,
																															ProportionLayout.TOTAL));
		
		SortScrollList theList = new SortScrollList(data, this, ScrollValueList.NO_HEADING);
		theList.addVariableToList("y", ScrollValueList.RAW_VALUE);
		theList.sortByVariable("y", ScrollValueList.SMALL_LAST);
		theListContent = (SortScrollContent)theList.getSortContent();
		theListContent.setRetainLastSelection(true);
		add(ProportionLayout.LEFT, theList);
		
		add(ProportionLayout.RIGHT, controlPanel());
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		return data;
	}
	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 20));
		
			sortSlider = new XNoValueSlider(translate("raw"), translate("sorted"), null, 0,
																														SortScrollContent.kSortedIndex, 0, this);
//			sortSlider.setSnapToExtremes();			//		Currently works on PC but not Mac
			
		thePanel.add(sortSlider);
		
				String messageText[] = new String[3];
				messageText[0] = getParameter(INIT_MESSAGE_PARAM);
				messageText[1] = getParameter(SORTED_MESSAGE_PARAM);
				messageText[2] = getParameter(ANSWER_MESSAGE_PARAM);
				int messageWidth = Integer.parseInt(getParameter(MESSSAGE_WIDTH_PARAM));
				
				message = new XTextArea(messageText, 0, messageWidth, this);
				message.setFont(getStandardFont());
				message.lockBackground(Color.white);
				message.setForeground(Color.red);
		
		thePanel.add(message);
		
			XPanel buttonPanel = new XPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				answerButton = new XButton(translate("Give answer"), this);
				answerButton.disable();
			buttonPanel.add(answerButton);
			
		thePanel.add(buttonPanel);
		
		return thePanel;
	}
	
	public void hiliteValues() {
		String highlightString = getParameter(HILITE_INDEX_PARAM);
		if (highlightString != null && data != null) {
			NumVariable y = data.getNumVariable();
			int noOfValues = y.noOfValues();
			boolean selected[] = new boolean[noOfValues];
			
			int sortedIndex[] = y.getSortedIndex();
			
			StringTokenizer theHighlights = new StringTokenizer(highlightString);
			while (theHighlights.hasMoreTokens()) {
				String nextString = theHighlights.nextToken();
				int colonPos = nextString.indexOf('-');
				if (colonPos >= 0) {
					String startString = nextString.substring(0, colonPos);
					String endString = nextString.substring(colonPos+1, nextString.length());
					int hilite1 = Integer.parseInt(startString);
					int hilite2 = Integer.parseInt(endString);
					for (int i=hilite1 ; i<=hilite2 ; i++)
						selected[sortedIndex[i]] = true;
				}
				else
					selected[sortedIndex[Integer.parseInt(nextString)]] = true;
			}
			data.setSelection(selected);
		}
	}
	
	private boolean localAction(Object target) {
		if (target == answerButton) {
			hiliteValues();
//			answerButton.disable();
			message.setText(kAnswerMessage);
			message.repaint();
			return true;
		}
		else if (target == sortSlider) {
			theListContent.setFrame(sortSlider.getValue());
			boolean sorted = (sortSlider.getValue() == sortSlider.getMaxValue());
			if (sorted != isSorted) {
				isSorted = sorted;
				if (isSorted) {
					answerButton.enable();
					message.setText(kSortedMessage);
				}
				else {
					answerButton.disable();
					message.setText(kInitMessage);
					data.clearSelection();
				}
				message.repaint();
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}
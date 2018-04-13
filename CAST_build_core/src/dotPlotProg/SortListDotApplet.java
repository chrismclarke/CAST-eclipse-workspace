package dotPlotProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import valueList.*;
import coreGraphics.*;

import dotPlot.*;


public class SortListDotApplet extends XApplet {
	static final private String INIT_MESSAGE_PARAM = "initMessage";
	static final private String SORTED_MESSAGE_PARAM = "sortedMessage";
	static final private String ANSWER_MESSAGE_PARAM = "answerMessage";
	static final private String MESSSAGE_WIDTH_PARAM = "messageWidth";
	static final private String HILITE_INDEX_PARAM = "highlight";
	static final private String AXIS_INFO_PARAM = "vertAxis";
	
	static final private int kInitMessage = 0;
	static final private int kSortedMessage = 1;
	static final private int kAnswerMessage = 2;
	
	private DataSet data;
	
	private XNoValueSlider sortSlider;
	private boolean isSorted = false;
	private XButton answerButton;
	private XCheckbox dotPlotCheck;
	
	private XTextArea message;
	
	private SortScrollContent theListContent;
	
	private XPanel dotPanel;
	private CardLayout dotPanelLayout;
	
	public void setupApplet() {
		
		data = getData();
		
		setLayout(new BorderLayout(20, 0));
		add("West", listPanel(data));
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new BorderLayout(20, 0));
			
			rightPanel.add("Center", controlPanel());
			rightPanel.add("East", dotPanel(data));
		add("Center", rightPanel);
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		return data;
	}
	
	private XPanel listPanel(DataSet data) {
		SortScrollList theList = new SortScrollList(data, this, ScrollValueList.HEADING);
		theList.addVariableToList("y", ScrollValueList.RAW_VALUE);
		theList.sortByVariable("y", ScrollValueList.SMALL_LAST);
		theListContent = (SortScrollContent)theList.getSortContent();
		theListContent.setRetainLastSelection(true);
		return theList;
	}
	
	private XPanel dotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FixedSizeLayout(100, 50));
		
			dotPanel = new XPanel();
			dotPanelLayout = new CardLayout();
			dotPanel.setLayout(dotPanelLayout);
			
			dotPanel.add("blank", new XPanel());
			
				XPanel innerPanel = new XPanel();
				innerPanel.setLayout(new BorderLayout());
				
				innerPanel.add("North", new XLabel(getParameter(VAR_NAME_PARAM), XLabel.LEFT, this));
				
				XPanel plotPanel = new XPanel();
					plotPanel.setLayout(new AxisLayout());
					
					VertAxis theVertAxis = new VertAxis(this);
						String labelInfo = getParameter(AXIS_INFO_PARAM);
						theVertAxis.readNumLabels(labelInfo);
					plotPanel.add("Left", theVertAxis);
					
						DotPlotView theDotPlot = new DotPlotView(data, this, theVertAxis);
						theDotPlot.lockBackground(Color.white);
						theDotPlot.setViewBorder(new Insets(5, 15, 5, 15));
						theDotPlot.setCrossSize(DataView.LARGE_CROSS);
						theDotPlot.setRetainLastSelection(true);
					plotPanel.add("Center", theDotPlot);
				innerPanel.add("Center", plotPanel);
				
			dotPanel.add("dotPlot", innerPanel);
			dotPanelLayout.show(dotPanel, "blank");
			
		thePanel.add(dotPanel);
		
		return thePanel;
	}
	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.VERTICAL));
		
		thePanel.add(ProportionLayout.TOP, sliderPanel());
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new BorderLayout(0, 20));
			bottomPanel.add("Center", messagePanel());
			bottomPanel.add("South", buttonPanel());
			
		thePanel.add(ProportionLayout.BOTTOM, bottomPanel);
		
		return thePanel;
	}
	
	private XPanel sliderPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new CenterFillLayout(CenterFillLayout.FILL_VERT));
		
			sortSlider = new XNoValueSlider(translate("Raw order"), translate("Sorted"), null, 0,
																			SortScrollContent.kSortedIndex, 0, XSlider.VERTICAL_INVERSE, this);
//		sortSlider.setSnapToExtremes();			//		Currently works on PC but not Mac
		
		thePanel.add(sortSlider);
		return thePanel;
	}
	
	private XPanel buttonPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 20));
		
			answerButton = new XButton(translate("Answer"), this);
			answerButton.disable();
		thePanel.add(answerButton);
		
			dotPlotCheck = new XCheckbox(translate("Show dot plot"), this);
			dotPlotCheck.disable();

		thePanel.add(dotPlotCheck);
		return thePanel;
	}
	
	private XPanel messagePanel() {
		String messageText[] = new String[3];
		messageText[0] = getParameter(INIT_MESSAGE_PARAM);
		messageText[1] = getParameter(SORTED_MESSAGE_PARAM);
		messageText[2] = getParameter(ANSWER_MESSAGE_PARAM);
		int messageWidth = Integer.parseInt(getParameter(MESSSAGE_WIDTH_PARAM));
		
		message = new XTextArea(messageText, 0, messageWidth, this);
		message.setFont(getStandardFont());
		message.lockBackground(Color.white);
		message.setForeground(Color.red);
		
		return message;
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
					dotPlotCheck.enable();
				}
				else {
					answerButton.disable();
					message.setText(kInitMessage);
					data.clearSelection();
					dotPlotCheck.setState(false);
					dotPanelLayout.show(dotPanel, "blank");
					dotPlotCheck.disable();
				}
				message.repaint();
			}
			return true;
		}
		else if (target == dotPlotCheck) {
			boolean showDotPlot = dotPlotCheck.getState();
			dotPanelLayout.show(dotPanel, showDotPlot ? "dotPlot" : "blank");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}
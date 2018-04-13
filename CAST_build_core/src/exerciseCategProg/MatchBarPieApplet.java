package exerciseCategProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import exercise2.*;

import exerciseCateg.*;


public class MatchBarPieApplet extends CoreMatchApplet {
	static final private String[] kYKeys = {"y0", "y1", "y2", "y3"};
	static final private int kNDisplayedDistns = kYKeys.length;
	
	private HorizAxis catAxis;
	private PieDrawer pieDrawer = new PieDrawer();
	private MultipleBarPieView barCharts, pieCharts;
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
			XPanel localWorkingPanel = getWorkingPanels(data);		//	CoreMatchApplet has variable workingPanel
		add("Center", localWorkingPanel);
				
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 4));
			
			bottomPanel.add(createMarkingPanel(NO_HINTS));
			
				XPanel messagePanel = new XPanel();
				messagePanel.setLayout(new FixedSizeLayout(100, getMessageHeight()));
					
					message = new ExerciseMessagePanel(this);
				messagePanel.add(message);
			bottomPanel.add(messagePanel);
		
		add("South", bottomPanel);
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
		registerParameter("categories", "string");
		registerParameter("counts", "string");
	}
	
	private Value[] getCategories() {
		StringTokenizer st = new StringTokenizer(getStringParam("categories"));
		Value label[] = new Value[st.countTokens()];
		for (int i=0 ; i<label.length ; i++) {
			String labelString = st.nextToken().replace('_', ' ');
			label[i] = new LabelValue(labelString);
		}
		
		return label;
	}
	
	private int[][] getCounts() {
		StringTokenizer st = new StringTokenizer(getStringParam("counts"));
		int counts[][] = new int[st.countTokens()][];
		
		for (int i=0 ; i<counts.length ; i++) {
			StringTokenizer st2 = new StringTokenizer(st.nextToken(), "_");
			counts[i] = new int[st2.countTokens()];
			for (int j=0 ; j<counts[i].length ; j++)
				counts[i][j] = Integer.parseInt(st2.nextToken());
		}
		
		return counts;
	}
	
	
//-----------------------------------------------------------
	
	protected int noOfItems() {
		return kNDisplayedDistns;
	}
	
	protected boolean retainFirstItems() {
		return false;
	}
	
	protected int getDragMatchHeight() {
		return barCharts.getSize().height;
	}
	
	protected void setWorkingPanelLayout(XPanel thePanel) {
		thePanel.setLayout(new ProportionLayout(0.75, 20));
	}
	
	protected XPanel addLeftItems(XPanel thePanel, int[] leftOrder) {
			XPanel barPanel = new XPanel();
			barPanel.setLayout(new AxisLayout());
			
				catAxis = new HorizAxis(this);
			barPanel.add("Bottom", catAxis);
			
				barCharts = new MultipleBarPieView(data, this, catAxis, kYKeys, leftOrder,
																								MultipleBarPieView.BAR_CHART, pieDrawer);
				barCharts.lockBackground(Color.white);
				registerStatusItem("barPerm", barCharts);
			barPanel.add("Center", barCharts);
			
		thePanel.add(ProportionLayout.LEFT, barPanel);
		
		return barPanel;
	}
	
	protected XPanel addRightItems(XPanel thePanel, int[] rightOrder) {
			XPanel piePanel = new XPanel();
			piePanel.setLayout(new BorderLayout(0, 0));
			
				pieCharts = new MultipleBarPieView(data, this, null, kYKeys, rightOrder,
																								MultipleBarPieView.PIE_CHART, pieDrawer);
				registerStatusItem("piePerm", pieCharts);
			piePanel.add("Center", pieCharts);
			
			piePanel.add("South", new Separator(0.0, 7));		//	height =  2 * 7 + 2 = 16
			
		thePanel.add(ProportionLayout.RIGHT, piePanel);
		
		return piePanel;
	}
	
	
//-----------------------------------------------------------
	
	protected void setDisplayForQuestion() {
		super.setDisplayForQuestion();
		
		catAxis.setCatLabels((CatVariable)data.getVariable(kYKeys[0]));
		catAxis.invalidate();
		
		barCharts.repaint();
		pieCharts.repaint();
	}
	
	protected void setDataForQuestion() {
		permute(pieDrawer.getColorPerm());
		
		int[][] counts = getCounts();
		int[] perm = createPermutation(counts.length);
		
		Value labels[] = getCategories();
		for (int i=0 ; i<kYKeys.length ; i++) {
			CatVariable yVar = (CatVariable)data.getVariable(kYKeys[i]);
			yVar.setLabels(labels);
			yVar.setCounts(counts[perm[i]]);
		}
		
		super.setDataForQuestion();
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Reorder the pie charts (or bar charts) by dragging so that each pie chart describes the same data set as the bar chart on its left.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("The correct matching of bar and pie charts is shown. The proportion of any colour of ink is the same in each bar chart and the matching pie chart.");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have correctly matched up the bar and pie charts of the four data sets. The proportion of any colour of ink is the same in each bar chart and the matching pie chart.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertText("The red arrows indicate pie charts that describe different data sets from the bar charts on their left. A bar chart and a pie chart of any data set should have the different colours shown in the same proportions.");
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 120;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		for (int i=0 ; i<kYKeys.length ; i++) {
			CatVariable yVar = new CatVariable("", CatVariable.USES_REPEATS);
			data.addVariable(kYKeys[i], yVar);
		}
		
		return data;
	}
	
//-----------------------------------------------------------
	
	protected void showCorrectWorking() {
		super.showCorrectWorking();
		
		pieCharts.repaint();
	}
	
}
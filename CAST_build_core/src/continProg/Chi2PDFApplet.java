package continProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import distn.*;
import utils.*;
import imageUtils.*;

import contin.*;
import variance.Chi2View;


public class Chi2PDFApplet extends XApplet {
	static final private String AXIS_PARAM = "horizAxis";
	static final private String CHI2_NAME_PARAM = "chi2Name";
	static final private String N_ROW_PARAM = "nRows";
	static final private String N_COL_PARAM = "nCols";
	
	static final private Color kGray = new Color(0x555555);
	static final private Color kRowLabelColor = new Color(0x009900);	// dark green
	static final private Color kColLabelColor = new Color(0x0000CC);	// dark blue
	
	private int nRows[];
	private int nCols[];
	private int r, c;
	
	private DataSet data;
	private BlankTableView table;
	
	private XChoice nRowChoice, nColChoice;
	
	public void setupApplet() {
		readRowCols();
		data = readData();
		
		setLayout(new BorderLayout(0, 10));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout(20, 10));
			
			topPanel.add("West", choicePanel());
			
				table = new BlankTableView(this, nRows[nRows.length - 1], nCols[nCols.length - 1],
																nRows[r], nCols[c], "R", "C", kRowLabelColor, kColLabelColor);
				table.setForeground(kGray);
			topPanel.add("Center", table);
			topPanel.add("South", new ImageCanvas("contin/chi2Distn.png", this));
		
		add("North", topPanel);
		add("Center", displayPanel(data));
	}
	
	private int[] readIntArray(String param) {
		StringTokenizer st = new StringTokenizer(param, " *");
		int nVals = st.countTokens();
		int counts[] = new int[nVals];
		for (int i=0 ; i<nVals ; i++)
			counts[i] = Integer.parseInt(st.nextToken());
		return counts;
	}
	
	private int readSelected(String param) {
		StringTokenizer st = new StringTokenizer(param, " ");
		int selected = 0;
		while (st.hasMoreTokens()) {
			if (st.nextToken().charAt(0) == '*')
				return selected;
			selected ++;
		}
		return 0;
	}
	
	private void readRowCols() {
		String rowParam = getParameter(N_ROW_PARAM);
		nRows = readIntArray(rowParam);
		r = readSelected(rowParam);
		
		String colParam = getParameter(N_COL_PARAM);
		nCols = readIntArray(colParam);
		c = readSelected(colParam);
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		
		Chi2DistnVariable chi2Distn = new Chi2DistnVariable("Chi2 distn");
		chi2Distn.setDF((nRows[r] - 1) * (nCols[c] - 1));
		data.addVariable("chi2", chi2Distn);
		
		return data;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
			theHorizAxis.readNumLabels(getParameter(AXIS_PARAM));
			thePanel.add("Bottom", theHorizAxis);
		
			Chi2View theView = new Chi2View(data, this, theHorizAxis, "chi2");
			theView.setDistnLabel(new LabelValue(getParameter(CHI2_NAME_PARAM)), kGray);
			theView.setAreaProportion(0.05);
			theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	private XPanel countChoicePanel(int[] array, int selection, String label, Color c) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 0));
		XLabel l = new XLabel(label, XLabel.LEFT, this);
		l.setForeground(c);
		l.setFont(getStandardBoldFont());
		thePanel.add(l);
		
			XChoice tempChoice = new XChoice(this);
			for (int i=0 ; i<array.length ; i++)
				tempChoice.addItem(String.valueOf(array[i]));
			tempChoice.select(selection);
		tempChoice.setForeground(c);
		thePanel.add(tempChoice);
		
		if (nRowChoice == null)
			nRowChoice = tempChoice;
		else
			nColChoice = tempChoice;
		
		return thePanel;
	}
	
	private XPanel choicePanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.RIGHT, VerticalLayout.VERT_CENTER, 7));
		
		thePanel.add(countChoicePanel(nRows, r, translate("No of rows") + ":", kRowLabelColor));
		thePanel.add(countChoicePanel(nCols, c, translate("No of cols") + ":", kColLabelColor));
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == nRowChoice || target == nColChoice) {
			int oldDF = (nRows[r] - 1) * (nCols[c] - 1);
			int newRIndex = nRowChoice.getSelectedIndex();
			int newR = nRows[newRIndex];
			int newCIndex = nColChoice.getSelectedIndex();
			int newC = nCols[newCIndex];
			int newDF = (newR - 1) * (newC - 1);
			if (oldDF != newDF) {
				r = newRIndex;
				c = newCIndex;
				Chi2DistnVariable chi2Distn = (Chi2DistnVariable)data.getVariable("chi2");
				chi2Distn.setDF(newDF);
				data.variableChanged("chi2");
				
				table.setTableSize(nRows[r], nCols[c]);
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
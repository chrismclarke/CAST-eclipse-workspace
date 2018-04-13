package inference;

import java.awt.*;

import dataView.*;
import utils.*;


public class DFEditPanel extends XPanel {
	private TTableDataSet tData;
	private XNumberEditPanel dfEdit;
	
	public DFEditPanel(TTableDataSet tData, XApplet applet) {
		setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		
		dfEdit = new XNumberEditPanel(tData.getVariable("df").name, 5, applet);
		dfEdit.setIntegerType(1, Integer.MAX_VALUE);
		
		add(dfEdit);
		
		setFont(applet.getStandardFont());
		
		this.tData = tData;
	}
	
	public void setForeground(Color c) {
		super.setForeground(c);
		if (dfEdit != null)
			dfEdit.setForeground(c);
	}
	
	public void setFont(Font f) {
		super.setFont(f);
		if (dfEdit != null)
			dfEdit.setFont(f);
	}
	
	private boolean localAction(Object target) {
		if (target == dfEdit) {
			int newDF = dfEdit.getIntValue();
			tData.selectDF(newDF);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}
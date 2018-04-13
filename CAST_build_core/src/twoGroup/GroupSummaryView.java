package twoGroup;

import java.awt.*;

import dataView.*;
import valueList.ValueView;
import models.*;


public class GroupSummaryView extends ValueView {
	static final public int MU_HAT = 0;
	static final public int SIGMA_HAT = 1;
	static final public int N = 2;
	static final public int X_BAR = 3;
	static final public int S = 4;
	static final public int P = 5;
	
	private String maxValueString;
	private int decimals;
	private int summaryType;
	private int targetGroup;
	
	public GroupSummaryView(DataSet theData, XApplet applet, int summaryType,
									int targetGroup, String maxValueString, int decimals) {
		super(theData, applet);
		this.summaryType = summaryType;
		this.targetGroup = targetGroup;
		this.maxValueString = maxValueString;
		this.decimals = decimals;
		GroupsImages.loadGroups(applet);
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return (summaryType <= SIGMA_HAT) ? GroupsImages.kMuHatWidth : GroupsImages.kXBarWidth;
	}
	
	protected int getLabelAscent(Graphics g) {
		return (summaryType <= SIGMA_HAT) ? GroupsImages.kMuHatAscent : GroupsImages.kXBarAscent;
	}
	
	protected int getLabelDescent(Graphics g) {
		return (summaryType <= SIGMA_HAT) ? GroupsImages.kMuHatDescent : GroupsImages.kXBarDescent;
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		int imageAscent = getLabelAscent(g);
		Image summaryImage;
		switch (summaryType) {
			case MU_HAT:
				summaryImage = GroupsImages.muHat[targetGroup];
				break;
			case SIGMA_HAT:
				summaryImage = GroupsImages.sigmaHat[targetGroup];
				break;
			case X_BAR:
				summaryImage = GroupsImages.xBar[targetGroup];
				break;
			case S:
				summaryImage = GroupsImages.s[targetGroup];
				break;
			case P:
				summaryImage = GroupsImages.p[targetGroup];
				break;
			default:
			case N:
				summaryImage = GroupsImages.n[targetGroup];
				break;
		}
		g.drawImage(summaryImage, startHoriz, baseLine - imageAscent, this);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(maxValueString);
	}
	
	public String getValueString() {
		NumValue result;
		
		if (summaryType == N) {
			if (getData() instanceof GroupsDataSet) {
				GroupsDataSet data = (GroupsDataSet)getData();
				result = new NumValue(data.getN(targetGroup), 0);
			}
			else {
				ContinTableDataSet data = (ContinTableDataSet)getData();
				result = new NumValue(data.getN(targetGroup), 0);
			}
		}
		else if (summaryType == P) {
			ContinTableDataSet data = (ContinTableDataSet)getData();
			result = new NumValue(data.getPropn(targetGroup), decimals);
		}
		else {
			GroupsDataSet data = (GroupsDataSet)getData();
			switch (summaryType) {
				case MU_HAT:
				case X_BAR:
					result = new NumValue(data.getMean(targetGroup), decimals);
					break;
				case SIGMA_HAT:
				case S:
				default:
					result = new NumValue(data.getSD(targetGroup), decimals);
					break;
			}
		}
		return result.toString();
	}
	
	public void setDecimals(int decimals) {
		this.decimals = decimals;
		resetSize();
	}
	
	protected boolean highlightValue() {
		return false;
	}
}

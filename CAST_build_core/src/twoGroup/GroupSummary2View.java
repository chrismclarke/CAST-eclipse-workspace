package twoGroup;

import java.awt.*;

import dataView.*;
import valueList.ValueView;
import models.*;


public class GroupSummary2View extends ValueView {
	static final public int MU_DIFF_HAT = 0;
	static final public int X_BAR_DIFF = 1;
	static final public int SD_DIFF_HAT = 2;
	static final public int PI_DIFF_HAT = 3;
	static final public int P_DIFF = 4;
	static final public int SD_PDIFF_HAT = 5;
	static final public int SD_POOLED_DIFF_HAT = 6;
	
	private String maxValueString;
	private int decimals;
	private int summaryType;
	
	public GroupSummary2View(DataSet theData, XApplet applet, int summaryType,
									String maxValueString, int decimals) {
		super(theData, applet);
		this.summaryType = summaryType;
		this.maxValueString = maxValueString;
		this.decimals = decimals;
		GroupsImages.loadGroups(applet);
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return (summaryType == MU_DIFF_HAT) ? GroupsImages.kMuDiffHatWidth :
								(summaryType == X_BAR_DIFF || summaryType == P_DIFF) ? GroupsImages.kXBarDiffWidth :
								(summaryType == SD_DIFF_HAT) ? GroupsImages.kSDDiffHatWidth :
								(summaryType == SD_POOLED_DIFF_HAT) ? GroupsImages.kSDPooledDiffWidth :
								(summaryType == PI_DIFF_HAT) ? GroupsImages.kPiDiffHatWidth :
								GroupsImages.kSDPDiffHatWidth;
	}
	
	protected int getLabelAscent(Graphics g) {
		return (summaryType == MU_DIFF_HAT || summaryType == PI_DIFF_HAT) ? GroupsImages.kMuDiffHatAscent : 
								(summaryType == X_BAR_DIFF || summaryType == P_DIFF) ? GroupsImages.kXBarDiffAscent :
								(summaryType == SD_POOLED_DIFF_HAT) ? GroupsImages.kSDPooledDiffAscent :
								GroupsImages.kSDDiffHatAscent;
	}
	
	protected int getLabelDescent(Graphics g) {
		return (summaryType == MU_DIFF_HAT || summaryType == PI_DIFF_HAT) ? GroupsImages.kMuDiffHatDescent : 
								(summaryType == X_BAR_DIFF || summaryType == P_DIFF) ? GroupsImages.kXBarDiffDescent :
								(summaryType == SD_POOLED_DIFF_HAT) ? GroupsImages.kSDPooledDiffDescent :
								GroupsImages.kSDDiffHatDescent;
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		int imageAscent = getLabelAscent(g);
		Image summaryImage;
		switch (summaryType) {
			case MU_DIFF_HAT:
				summaryImage = GroupsImages.muDiffHat;
				break;
			case X_BAR_DIFF:
				summaryImage = GroupsImages.xBarDiff;
				break;
			case SD_DIFF_HAT:
				summaryImage = GroupsImages.sdDiffHat;
				break;
			case SD_POOLED_DIFF_HAT:
				summaryImage = GroupsImages.sdDiffHatPooled;
				break;
			case PI_DIFF_HAT:
				summaryImage = GroupsImages.piDiffHat;
				break;
			case P_DIFF:
				summaryImage = GroupsImages.pDiff;
				break;
			default:
			case SD_PDIFF_HAT:
				summaryImage = GroupsImages.sdPDiffHat;
				break;
		}
		g.drawImage(summaryImage, startHoriz, baseLine - imageAscent, this);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(maxValueString);
	}
	
	protected String getValueString() {
		double result;
		switch (summaryType) {
			case MU_DIFF_HAT:
			case X_BAR_DIFF:
				{
					GroupsDataSet data = (GroupsDataSet)getData();
					result = data.getMean(1) - data.getMean(0);
				}
				break;
			case SD_DIFF_HAT:
				{
					GroupsDataSet data = (GroupsDataSet)getData();
					double s1 = data.getSD(0);
					double s2 = data.getSD(1);
					result = Math.sqrt(s1 * s1 / data.getN(0) + s2 * s2 / data.getN(1));
				}
				break;
			case SD_POOLED_DIFF_HAT:
				{
					GroupsDataSet data = (GroupsDataSet)getData();
					double s1 = data.getSD(0);
					double s2 = data.getSD(1);
					int n1 = data.getN(0);
					int n2 = data.getN(1);
					double pooledVar = (s1 * s1 * (n1 - 1) + s2 * s2 * (n2 - 1)) / (n1 + n2 - 2);
					result = Math.sqrt((1.0 / n1 + 1.0 / n2) * pooledVar);
				}
				break;
			case PI_DIFF_HAT:
			case P_DIFF:
				{
					ContinTableDataSet data = (ContinTableDataSet)getData();
					result = data.getPropn(1) - data.getPropn(0);
				}
				break;
			default:
			case SD_PDIFF_HAT:
				{
					ContinTableDataSet data = (ContinTableDataSet)getData();
					double p1 = data.getPropn(0);
					double p2 = data.getPropn(1);
					result = Math.sqrt(p1 * (1 - p1) / data.getN(0) + p2 * (1 - p2) / data.getN(1));
				}
				break;
		}
		return new NumValue(result, decimals).toString();
	}
	
	protected boolean highlightValue() {
		return false;
	}
}

package ssq;


public class AnovaLayoutInfo {
	public int headingBaseline, componentStep, nComp, firstComponentBaseline,
																															residBaseline, totalBaseline;
	public int tableWidth, tableLeft, residWidth;
	private int firstComponentTop;
	private int leftMargin, localSsqRight, localDfRight, localMsqRight, localFRight, localPValRight,
																												localTotalWidth;
	
	public AnovaLayoutInfo(int nComp, int headingBaseline, int firstComponentBaseline, int firstComponentTop,
													int componentStep, int residBaseline, int totalBaseline,
													int leftMargin, int tableWidth, int tableLeft, int localSsqRight, int localDfRight,
													int localMsqRight, int localFRight, int localPValRight, int localTotalWidth,
													int residWidth) {
		this.nComp = nComp;
		this.headingBaseline = headingBaseline;
		this.firstComponentBaseline = firstComponentBaseline;
		this.firstComponentTop = firstComponentTop;
		this.componentStep = componentStep;
		this.residBaseline = residBaseline;
		this.totalBaseline = totalBaseline;
		this.leftMargin = leftMargin;
		this.tableWidth = tableWidth;
		this.tableLeft = tableLeft;
		this.localSsqRight = localSsqRight;
		this.localDfRight = localDfRight;
		this.localMsqRight = localMsqRight;
		this.localFRight = localFRight;
		this.localPValRight = localPValRight;
		this.localTotalWidth = localTotalWidth;
		this.residWidth = residWidth;
	}
	
	public int tableRight() {
		return tableLeft + tableWidth;
	}
	
	public int residRight() {
		return tableLeft + residWidth;
	}
	
	public int totalRight() {
		return tableLeft + localTotalWidth;
	}
	
	public int ssqRight() {
		return tableLeft + localSsqRight;
	}
	
	public int dfRight() {
		return tableLeft + localDfRight;
	}
	
	public int msqRight() {
		return tableLeft + localMsqRight;
	}
	
	public int fRight() {
		return tableLeft + localFRight;
	}
	
	public int pValueRight() {
		return tableLeft + localPValRight;
	}
	
	public int hitComponentInMargin(int x, int y) {
		if (x < tableLeft - leftMargin || x > tableLeft)
			return -1;
		if (y < firstComponentTop)
			return -1;
		int hitItem = (y - firstComponentTop) / componentStep;
		if (hitItem < nComp)
			return hitItem;
		else
			return -1;
	}
	
	public int componentTop(int componentIndex) {
		return firstComponentTop + componentIndex * componentStep;
	}
}


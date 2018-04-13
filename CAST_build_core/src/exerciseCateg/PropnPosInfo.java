package exerciseCateg;

import dataView.*;

public class PropnPosInfo implements PositionInfo {
	public double propn;		//	really a propn between 0 and 1
	public int catIndex;
	public double propnOffset;
	
	public PropnPosInfo(int catIndex, double propnOffset) {
		this.catIndex = catIndex;
		this.propnOffset = propnOffset;
	}
	
	public PropnPosInfo(double propn) {
		this.propn = propn;
	}
	
	public boolean equals(PositionInfo otherPos) {
		if (otherPos == null || !(otherPos instanceof PropnPosInfo))
			return false;
		PropnPosInfo other = (PropnPosInfo)otherPos;
		return (propn == other.propn);
	}
}
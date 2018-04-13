package exper2;

import java.awt.*;


public class MultiRowLayout extends FlowLayout {
	private int nRows;
	
	public MultiRowLayout(int nRows, int hgap, int vgap) {
		super(FlowLayout.CENTER, hgap, vgap);
		this.nRows = nRows;
	}
	
	public Dimension preferredLayoutSize(Container parent) {
		Dimension dim = super.preferredLayoutSize(parent);
		dim.height = nRows * dim.height - (nRows - 1) * 2 * getVgap();
		return dim;
	}
	
	public Dimension minimumLayoutSize(Container parent) {
		Dimension dim = super.minimumLayoutSize(parent);
		dim.height = nRows * dim.height - (nRows - 1) * 2 * getVgap();
		return dim;
	}
}
package cast.utils;

import java.awt.*;


public class FixedSizeLayout implements LayoutManager {
			//		If this layout is used for the outer components of a BorderLayout,
			//		it restricts the size of the component. (If the outer component contains
			//		an AxisLayout, it is otherwise sized to  its preferred size, 300x300)
	private int preferredWidth, preferredHeight;
	
	public FixedSizeLayout(int preferredWidth, int preferredHeight) {
		this.preferredWidth = preferredWidth;
		this.preferredHeight = preferredHeight;
	}
	
	public void addLayoutComponent(String name, Component comp) {
	}
	
	public void removeLayoutComponent(Component comp) {
		comp = null;
	}
	
	public Dimension preferredLayoutSize(Container parent) {
		return new Dimension(preferredWidth, preferredHeight);
	}
	
	public Dimension minimumLayoutSize(Container parent) {
		return new Dimension(preferredWidth, preferredHeight);
	}
	
	public void layoutContainer(Container parent) {
		Insets insets = parent.getInsets();
		int innerWidth = parent.getSize().width - (insets.left + insets.right);
		int innerHeight = parent.getSize().height - (insets.top + insets.bottom);
		
		int noOfComponents = parent.getComponentCount();
		if (noOfComponents > 0) {
			Component comp = parent.getComponent(0);
			comp.setBounds(insets.left, insets.top, innerWidth, innerHeight);
		}
	}
	
	public String toString() {
		return getClass().getName();
	}
}
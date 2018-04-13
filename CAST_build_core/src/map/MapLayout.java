package map;

import java.awt.*;


public class MapLayout implements LayoutManager {
			//		Keeps the proportions of the single component to those of its preferred size
			//		It is only likely to be of any use inside the Center component of a BorderLayout
	private Component comp;
	
	public MapLayout() {
	}
	
	public void addLayoutComponent(String name, Component comp) {
//		System.out.println("Adding map component");
		if (this.comp == null)
			this.comp = comp;
		else
			throw new RuntimeException("Error!! You can only add a single component to a MapLayout.");
	}
	
	public void removeLayoutComponent(Component comp) {
		if (comp == this.comp)
			this.comp = null;
	}
	
	public Dimension preferredLayoutSize(Container parent) {
		return minimumLayoutSize(parent);
	}
	
	public Dimension minimumLayoutSize(Container parent) {
		return  comp.getPreferredSize();
	}
	
	public void layoutContainer(Container parent) {
		Insets insets = parent.getInsets();
		int innerWidth = parent.getSize().width - (insets.left + insets.right);
		int innerHeight = parent.getSize().height - (insets.top + insets.bottom);
		
		Dimension preferred = comp.getPreferredSize();
		int left = insets.left;
		int top = insets.top;
		int width = innerWidth;
		int height = innerHeight;
		
		if (innerWidth * preferred.height > innerHeight * preferred.width) {
			width = innerHeight * preferred.width / preferred.height;
			left += (innerWidth - width) / 2;
		}
		else {
			height = innerWidth * preferred.height / preferred.width;
			top += (innerHeight - height) / 2;
		}
		
		comp.setBounds(left, top, width, height);
	}
	
	public String toString() {
		return getClass().getName();
	}
}
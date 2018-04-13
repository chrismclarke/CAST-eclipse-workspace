package utils;

import java.awt.*;


public class EqualSpacingLayout extends GridBagLayout {
	static public final int HORIZONTAL = 0;
	static public final int VERTICAL = 1;
	
	
	private int orientation;
	private int gap = 0;
	
	private GridBagConstraints constraints;
	
	private boolean firstComponent = true;
	
	public EqualSpacingLayout() {
		this(HORIZONTAL, 0);
	}
	
	public EqualSpacingLayout(int orientation) {
		this(orientation, 0);
	}
	
	public EqualSpacingLayout(int orientation, int gap) {
		this.orientation = orientation;
		this.gap = gap;
		
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.insets = new Insets(0,0,0,0);
		constraints.gridheight = constraints.gridwidth = 1;
		constraints.ipadx = constraints.ipady = 0;
		constraints.weightx = constraints.weighty = 1.0;
		
		constraints.fill = GridBagConstraints.BOTH;
		
		constraints.anchor = (orientation == HORIZONTAL) ? GridBagConstraints.EAST : GridBagConstraints.SOUTH;
	}

	public void addLayoutComponent(Component comp, Object dummy) {
		if (orientation == HORIZONTAL)
			constraints.insets.left = firstComponent ? 0 : gap;
		else
			constraints.insets.top = firstComponent ? 0 : gap;
		
		super.addLayoutComponent(comp, constraints);
		
		if (orientation == HORIZONTAL)
			constraints.gridx ++;
		else
			constraints.gridy ++;
		
		firstComponent = false;
	}

	public void addLayoutComponent(String name, Component comp) {
		addLayoutComponent(comp, null);
	}
  public void layoutContainer(Container parent) {
    super.layoutContainer(parent);
					//	there is often a 1-pixel gap at the bottom so we may need to move last component
		
		if (orientation == VERTICAL) {
			Component components[] = parent.getComponents();
			Component lastComponent = components[components.length - 1];
			Rectangle lastBounds = lastComponent.getBounds();
			
			int parentHeight = parent.getHeight();
			int lastBottom = lastBounds.y + lastBounds.height;
			if (lastBottom != parentHeight) {
				lastBounds.y ++;
				lastComponent.setBounds(lastBounds);
			}
		}
  }
}
package utils;

import java.awt.*;
import java.io.Serializable;


public class ProportionLayout implements LayoutManager, Serializable {
	static public final String LEFT = "Left";
	static public final String RIGHT = "Right";
	static public final String TOP = "Top";
	static public final String BOTTOM = "Bottom";
	
	static public final int HORIZONTAL = 0;
	static public final int VERTICAL = 1;
	
	static public final int TOTAL = 0;
	static public final int REMAINDER = 1;
	
	private int orientation;
	private int totalOrRemainder;
	private double leftPropn;
	private int horizGap;
	private Component left, right;
	
	public ProportionLayout(double leftPropn, int horizGap, int orientation, int totalOrRemainder) {
		this.leftPropn = leftPropn;
		this.horizGap = horizGap;
		this.orientation = orientation;
		this.totalOrRemainder = totalOrRemainder;
	}
	
	public ProportionLayout(double leftPropn, int horizGap, int orientation) {
		this(leftPropn, horizGap, orientation, TOTAL);
	}
	
	public ProportionLayout(double leftPropn, int horizGap) {
		this(leftPropn, horizGap, HORIZONTAL);
	}
	
	public void addLayoutComponent(String name, Component comp) {
		if (name.equals(LEFT) || name.equals(TOP))
			left = comp;
		else
			right = comp;
	}
	
	public void removeLayoutComponent(Component comp) {
	}
	
	public Dimension preferredLayoutSize(Container parent) {
		int width = 0;
		int height = 0;
		if (left != null) {
			Dimension d = left.getPreferredSize();
			width = d.width;
			height = d.height;
		}
		if (right != null) {
			Dimension d = right.getPreferredSize();
			if (orientation == HORIZONTAL) {
				if (totalOrRemainder == TOTAL)
					width = (int)Math.max(width / leftPropn, d.width / (1.0 - leftPropn)) + horizGap;
				else
					width = width + d.width + horizGap;
				height = Math.max(height, d.height);
			}
			else {
				if (totalOrRemainder == TOTAL)
					height = (int)Math.max(height / leftPropn, d.height / (1.0 - leftPropn)) + horizGap;
				else
					height = height + d.height + horizGap;
				width = Math.max(width, d.width);
			}
		}
//		System.out.println("Left wid = " + left.getPreferredSize().width + ", ht = " + left.getPreferredSize().height);
//		System.out.println("Right wid = " + right.getPreferredSize().width + ", ht = " + right.getPreferredSize().height);

		Insets insets = parent.getInsets();
		width += insets.left + insets.right;
		height += insets.top + insets.bottom;
		
		return new Dimension(width, height);
	}
	
	public Dimension minimumLayoutSize(Container parent) {
		int width = 0;
		int height = 0;
		if (left != null) {
			Dimension d = left.getMinimumSize();
			width = d.width;
			height = d.height;
		}
		if (right != null) {
			Dimension d = right.getMinimumSize();
			if (orientation == HORIZONTAL) {
				if (totalOrRemainder == TOTAL)
					width = (int)Math.max(width / leftPropn, d.width / (1.0 - leftPropn)) + horizGap;
				else
					width = width + d.width + horizGap;
				height = Math.max(height, d.height);
			}
			else {
				if (totalOrRemainder == TOTAL)
					height = (int)Math.max(height / leftPropn, d.height / (1.0 - leftPropn)) + horizGap;
				else
					height = height + d.height + horizGap;
				width = Math.max(width, d.width);
			}
		}

		Insets insets = parent.getInsets();
		width += insets.left + insets.right;
		height += insets.top + insets.bottom;
		
		return new Dimension(width, height);
	}
	
	public void layoutContainer(Container parent) {
		Insets insets = parent.getInsets();
		int availableWidth = parent.getSize().width - (insets.left + insets.right);
		int availableHeight = parent.getSize().height - (insets.top + insets.bottom);
//		int tempTop = insets.top;
//		int tempLeft = insets.left;
		
		if (left == null) {
			if (right != null) {
				int leftAdjust = (orientation == HORIZONTAL)
																? (int)Math.round(availableWidth * leftPropn) : 0;
				int topAdjust = (orientation == VERTICAL)
																? (int)Math.round(availableHeight * leftPropn) : 0;
				right.setBounds(insets.left + leftAdjust, insets.top + topAdjust,
													availableWidth - leftAdjust, availableHeight - topAdjust);
			}
		}
		else if (right == null) {
			int leftWidth = (orientation == HORIZONTAL)
															? (int)Math.round(availableWidth * leftPropn) : availableWidth;
			int topHeight = (orientation == VERTICAL)
														? (int)Math.round(availableHeight * leftPropn) : availableHeight;
			left.setBounds(insets.left, insets.top, leftWidth, topHeight);
		}
		else if (orientation == HORIZONTAL) {
			int leftSize, rightSize;
			if (totalOrRemainder == TOTAL) {
				leftSize = (int)((availableWidth - horizGap) * leftPropn);
				rightSize = availableWidth - horizGap - leftSize;
			}
			else {
				leftSize = left.getMinimumSize().width;
				rightSize = right.getMinimumSize().width;
				leftSize += (int)((availableWidth - leftSize - rightSize - horizGap) * leftPropn);
				rightSize = availableWidth - leftSize - horizGap;
			}
			left.setBounds(insets.left, insets.top, leftSize, availableHeight);
			right.setBounds(insets.left + leftSize + horizGap, insets.top, rightSize, availableHeight);
		}
		else {
			int topSize, bottomSize;
			if (totalOrRemainder == TOTAL) {
				topSize = (int)((availableHeight - horizGap) * leftPropn);
				bottomSize = availableHeight - horizGap - topSize;
			}
			else {
				topSize = left.getMinimumSize().height;
				bottomSize = right.getMinimumSize().height;
				topSize += (int)((availableHeight - topSize - bottomSize - horizGap) * leftPropn);
				bottomSize = availableHeight - topSize - horizGap;
			}
			left.setBounds(insets.left, insets.top, availableWidth, topSize);
			right.setBounds(insets.left, insets.top + topSize + horizGap, availableWidth, bottomSize);
		}
	}
	
	public String toString() {
		return getClass().getName();
	}
}
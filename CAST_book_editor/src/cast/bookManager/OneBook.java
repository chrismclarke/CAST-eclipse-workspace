package cast.bookManager;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import cast.bookEditor.*;
import cast.utils.*;
import cast.index.*;


public class OneBook extends JPanel {
	static final private Color kLookOnlyColor = new Color(0x333399);
	static final private Color kEditBackground = new Color(0xFFFFFF);
	static final private Color kShowBackground = new Color(0xDDDDEE);
	
	private JButton editButton, buildButton, moreButton;
//	private CastEbook castEbook;
	
	private BookBuildFrame buildFrame = null;
	
	public OneBook(final CastEbook castEbook) {
//		this.castEbook = castEbook;
		boolean canEdit = castEbook.canEditBook();
		
		setLayout(new GridLayout(1, 2));
		Color bookBackground = canEdit ? kEditBackground : kShowBackground;
		setBackground(bookBackground);
		
		JLabel title = new JLabel(castEbook.getShortBookName());
		
		title.setFont(new Font("SansSerif", Font.BOLD, 24));
		if (!canEdit)
			title.setForeground(kLookOnlyColor);
		add(title);
		
			JPanel rightPanel = new JPanel();
			rightPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			rightPanel.setBackground(bookBackground);
			
				JPanel buttonPanel = new JPanel();
				buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 14, 0));
				buttonPanel.setBackground(bookBackground);
				
				if (!castEbook.canOnlyTranslate() || canEdit) {
					editButton = new JButton(canEdit ? "Edit" : "Look");
					editButton.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										BookEditor editFrame = new BookEditor(castEbook, OneBook.this);
										editFrame.pack();
										editFrame.setVisible(true);
										editFrame.toFront();
										
										editButton.setEnabled(false);
									}
								});
					buttonPanel.add(editButton);
				}

				if (canEdit) {
					buildButton = new JButton("Build...");
					buildButton.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										buildFrame = new BookBuildFrame(castEbook, OneBook.this);
										buildFrame.pack();
										buildFrame.setVisible(true);
										buildFrame.toFront();
										
										buildButton.setEnabled(false);
									}
								});
					buttonPanel.add(buildButton);
				}
			rightPanel.add(buttonPanel);
			
		add(rightPanel);
	}
	
	public Dimension getPreferredSize() {
		Dimension ps = super.getPreferredSize();
		if (ps.width < 400)
			ps.width = 400;
		return ps;
	}
	

	public void enableBuildButton() {
		buildButton.setEnabled(true);
		buildFrame = null;
	}
	
	public void enableEditButton() {
		editButton.setEnabled(true);
	}
	
	public void enableMoreButton() {
		moreButton.setEnabled(true);
	}
}

package cast.bookEditor;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import cast.bookManager.*;


public class PartTitle extends ElementTitle {
	
//	static final private Color kSelectedBackground = new Color(0xBBBBFF);
	static final public Color kDarkRed = new Color(0x660000);
	static final public Color kLightRed = new Color(0xAA6666);
	
//	private JButton changeButton;
//	private boolean showingButtons = false;
	
	public PartTitle(final DomPart domPart, final CastEbook castEbook) {
		super(domPart);
		
		setLayout(new BorderLayout(0, 0));
		setOpaque(false);
		
		title = new JLabel("");
		setTitle(domPart);
		title.setFont(new Font("SansSerif", Font.BOLD, 14));
		add("Center", title);
		
		menu = new JPopupMenu();
		
		JMenuItem changeItem = new JMenuItem("Change Part Title...");
		menu.add(changeItem);
		changeItem.addActionListener(new ActionListener() {
														public void actionPerformed(ActionEvent e) {
															if (domPart.createCopyInEbook(PartTitle.this))	//	queries for new part title
																setTitle(domPart);
//															String newName = (String)JOptionPane.showInputDialog(PartTitle.this, "Type the Part name:", "Part name",
//																									JOptionPane.QUESTION_MESSAGE, null, null, domPart.getPartName());
//															if (newName != null && newName.length() > 0) {
//																domPart.setPartName(newName);
//																setTitle(domPart);
//															}
														}
										});
		
		JMenuItem deleteItem = new JMenuItem("Delete");
		menu.add(deleteItem);
		deleteItem.addActionListener(new ActionListener() {
														public void actionPerformed(ActionEvent e) {
															int result = JOptionPane.showConfirmDialog(PartTitle.this, "Are you sure that you want to delete this part?",
																					"Delete Part?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
															
															if (result == JOptionPane.OK_OPTION)
																deleteSelf(castEbook);
														}
										});
		
		setMenuDragMouseListener(castEbook);
		setTitleForeground(kDarkRed, kLightRed, castEbook);
	}
	
	private void setTitle(DomPart domPart) {
		String decodedTitle = domPart.getPartName();
		title.setText(decodedTitle);
	}
	
	public Insets getInsets() {
		return new Insets(10, 0, 0, 0);
	}
}

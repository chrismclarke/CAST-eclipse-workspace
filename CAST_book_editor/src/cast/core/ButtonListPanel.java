package cast.core;

import java.awt.*;

import javax.swing.*;


public class ButtonListPanel extends JPanel {
	private GridBagConstraints buttonConstraint, labelConstraint, infoConstraint;
	
	public ButtonListPanel() {
		setOpaque(false);
		
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		
		buttonConstraint = new GridBagConstraints();
			buttonConstraint.anchor = GridBagConstraints.CENTER;
			buttonConstraint.fill = GridBagConstraints.NONE;
			buttonConstraint.gridheight = buttonConstraint.gridwidth = 1;
			buttonConstraint.gridx = buttonConstraint.gridy = 0;
			buttonConstraint.insets = new Insets(15, 10, 15, 10);
			buttonConstraint.ipadx = 0;
			buttonConstraint.ipady = 0;
			buttonConstraint.weightx = buttonConstraint.weighty = 0.0;
		
		labelConstraint = new GridBagConstraints();
			labelConstraint.anchor = GridBagConstraints.WEST;
			labelConstraint.fill = GridBagConstraints.VERTICAL;
			labelConstraint.gridheight = labelConstraint.gridwidth = 1;
			labelConstraint.gridx = 1;
			labelConstraint.gridy = 0;
			labelConstraint.insets = new Insets(15, 10, 15, 10);
			labelConstraint.ipadx = 0;
			labelConstraint.ipady = 0;
			labelConstraint.weightx = 1.0;
			labelConstraint.weighty = 0.0;
		
		infoConstraint = new GridBagConstraints();
			infoConstraint.anchor = GridBagConstraints.CENTER;
			infoConstraint.fill = GridBagConstraints.NONE;
			infoConstraint.gridheight = infoConstraint.gridwidth = 1;
			infoConstraint.gridx = 2;
			infoConstraint.gridy = 0;
			infoConstraint.insets = new Insets(15, 10, 15, 10);
			infoConstraint.ipadx = 0;
			infoConstraint.ipady = 0;
			infoConstraint.weightx = infoConstraint.weighty = 0.0;
	}
	
	public JButton addRow(JButton actionButton, JLabel infoLabel) {
		add(actionButton, buttonConstraint);
		buttonConstraint.gridy ++;
		
		add(infoLabel, labelConstraint);
		labelConstraint.gridy ++;
		
		JButton theInfoButton = null;
		if (Desktop.isDesktopSupported()) {
			theInfoButton = new JButton("Info");
			add(theInfoButton, infoConstraint);
		}
		infoConstraint.gridy ++;
		
		return theInfoButton;
	}
	
}

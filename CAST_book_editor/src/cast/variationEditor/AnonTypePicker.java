package cast.variationEditor;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


public class AnonTypePicker extends JDialog {
	static final private String kDefaultIntValue = "int(10:20)";
	static final private String kDefaultDoubleValue = "double(0.0:1.0)";
	static final private String kDefaultStringValue = "string[](text1*text2)";
	
	static public String pickType(Frame parent) {
		AnonTypePicker dialog = new AnonTypePicker(parent);

		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);

		dialog.dispose();

		if (dialog.ok)
			return dialog.currentValue;
		else
			return null;
	}
	
	private String currentValue = kDefaultStringValue;
	private boolean ok = false;
	
	public AnonTypePicker(Frame parent) {
		super(parent, "Select type for anonymous variable", true);
		
		setLayout(new BorderLayout(0, 0));
		
			JPanel mainPanel = new JPanel() {
											public Insets getInsets() {
												return new Insets(10, 20, 10, 20);
											}
										};
		
			mainPanel.setLayout(new BorderLayout(0, 10));
			
				JLabel title = new JLabel("Type of variable:", JLabel.LEFT);
				title.setFont(new Font("SansSerif", Font.BOLD, 14));
			mainPanel.add("North", title);
			
				JPanel typePanel = new JPanel();
				typePanel.setLayout(new GridLayout(0, 1));
				
					JRadioButton intOption = new JRadioButton("Integer", false);
					intOption.addActionListener(new ActionListener() {
																					public void actionPerformed(ActionEvent e) {
																						currentValue = kDefaultIntValue;
																					}
																			});
					JRadioButton doubleOption = new JRadioButton("Double", false);
					doubleOption.addActionListener(new ActionListener() {
																					public void actionPerformed(ActionEvent e) {
																						currentValue = kDefaultDoubleValue;
																					}
																			});
					JRadioButton stringOption = new JRadioButton("String", true);
					stringOption.addActionListener(new ActionListener() {
																					public void actionPerformed(ActionEvent e) {
																						currentValue = kDefaultStringValue;
																					}
																			});

					ButtonGroup bgroup = new ButtonGroup();
					bgroup.add(intOption);
					bgroup.add(doubleOption);
					bgroup.add(stringOption);
				
				typePanel.add(intOption);
				typePanel.add(doubleOption);
				typePanel.add(stringOption);
				
			mainPanel.add("Center", typePanel);
			
			
				JPanel bottomPanel = new JPanel();
				bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
				
					JButton saveButton = new JButton("OK");
					saveButton.addActionListener(new ActionListener() {
																					public void actionPerformed(ActionEvent e) {
																						ok = true;
																						setVisible(false);
																					}
																			});
				bottomPanel.add(saveButton);
				
					JButton cancelButton = new JButton("Cancel");
					cancelButton.addActionListener(new ActionListener() {
																					public void actionPerformed(ActionEvent e) {
																						ok = false;
																						setVisible(false);
																					}
																			});
				bottomPanel.add(cancelButton);
				
			mainPanel.add("South", bottomPanel);
			
		add("Center", mainPanel);
		
		pack();
		setResizable(false);
	}
	
}

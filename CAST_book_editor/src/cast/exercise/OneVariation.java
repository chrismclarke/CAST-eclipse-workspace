package cast.exercise;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import cast.core.*;
import cast.utils.*;
import cast.variationEditor.*;

public class OneVariation extends OneCoreVariation {
	public OneVariation(final DomTopic topic, final DomExercise exercise,
																				final DomVariation variation, final ExerciseListFrame mainFrame) {
																		//	CoreVariation sets up the textArea for the variation description
		super(variation, mainFrame);
		setBackground(Color.white);
		
		textArea.setText(getLongVariationName());
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		buttonPanel.setOpaque(false);
		
		if (Options.isMasterCast) {
					final JPopupMenu popup = new JPopupMenu();
					
						JMenuItem menuItem = new JMenuItem("Edit");
						menuItem.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									VariationEditor variationFrame = new VariationEditor(variation, OneVariation.this);
									variationFrame.pack();
									variationFrame.setVisible(true);
									variationFrame.toFront();
								}
							});
					popup.add(menuItem);
					
						menuItem = new JMenuItem("Delete...");
						menuItem.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											Object[] options = {"Delete", "Cancel"};
											int result = JOptionPane.showOptionDialog(OneVariation.this,
																	"Are you sure that you want to delete core variation \"" + getLongVariationName() + "\"?"
																	+ "\nYou will not be able to undo this action.", "Delete?",
																	JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);

											switch (result) {
												case JOptionPane.YES_OPTION:
													exercise.deleteVariation(variation);
													topic.saveDom();
													mainFrame.updateVariationList();
													break;
												case JOptionPane.NO_OPTION:
												default:
													break;
											}
										}
									});
					popup.add(menuItem);
					
						menuItem = new JMenuItem("Duplicate...");
						menuItem.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											doDuplicate(mainFrame);
										}
									});
					popup.add(menuItem);
					
						JLabel clickLabel = new JLabel("(right-click for menu)", JLabel.RIGHT);
						Border spacingBorder = BorderFactory.createEmptyBorder(5, 0, 5, 0);
						clickLabel.setBorder(spacingBorder);
						clickLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
					buttonPanel.add(clickLabel);
					
					MouseListener popupListener = new MouseAdapter() {
															public void mousePressed(MouseEvent e) {
																maybeShowPopup(e);
															}

															public void mouseReleased(MouseEvent e) {
																maybeShowPopup(e);
															}

															private void maybeShowPopup(MouseEvent e) {
																if (e.isPopupTrigger()) {
																	popup.show(e.getComponent(), e.getX(), e.getY());
																}
															}
														};
					
					buttonPanel.addMouseListener(popupListener);
					textArea.addMouseListener(popupListener);
		}
		else {
			JPanel innerPanel = new JPanel();
			innerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
			
				JLabel clickLabel = new JLabel("(core)", JLabel.RIGHT);
				Border spacingBorder = BorderFactory.createEmptyBorder(5, 0, 5, 0);
				clickLabel.setBorder(spacingBorder);
				clickLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
				buttonPanel.add(clickLabel);
			
			innerPanel.add(buttonPanel);
				
					JButton duplicateButton = new JButton("Duplicate");
					duplicateButton.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										doDuplicate(mainFrame);
									}
								});
					innerPanel.add(duplicateButton);
		}
			
		add("East", buttonPanel);
	}
	
	private void doDuplicate(ExerciseListFrame mainFrame) {
		DomCustomVariations customVariations = mainFrame.getCustomVariations();
		if (customVariations == null)
			JOptionPane.showMessageDialog(OneVariation.this, "Error! No custom variations file has been selected.",
																									"Error!", JOptionPane.ERROR_MESSAGE);
		else {
			customVariations.duplicateVariation(variation);
			mainFrame.updateVariationList();
		}
	}
	
	protected String getLongVariationName() {
		return variation.getLongName();
	}
	
	protected Color getLineColor() {
		return Color.black;
	}
}

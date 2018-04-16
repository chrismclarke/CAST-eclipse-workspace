package cast.exercise;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import org.w3c.dom.*;

import cast.utils.*;
import cast.core.*;
import cast.variationEditor.*;

public class OneCustomVariation extends OneCoreVariation {
	static final private Color kCustomBackground = new Color(0xFFDDDD);
	static final private Color kDarkRed = new Color(0x660000);
	
	private DomCustomVariations customVariations;
	private DomExercise exercise;
	
	public OneCustomVariation(final DomExercise exercise , final DomVariation variation,
															final DomCustomVariations customVariations, final ExerciseListFrame mainFrame) {
		super(variation, mainFrame);
		this.customVariations = customVariations;
		this.exercise = exercise;
		
		setBackground(kCustomBackground);
		
		textArea.setText(getLongVariationName());
		
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			buttonPanel.setOpaque(false);
			
				JPanel innerPanel = new JPanel();
				innerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
				innerPanel.setOpaque(false);
				
				if (Options.isMasterCast) {
					final JPopupMenu popup = new JPopupMenu();
					
						JMenuItem menuItem = new JMenuItem("Edit");
						menuItem.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											showEditWindow();
										}
									});
					popup.add(menuItem);
					
						menuItem = new JMenuItem("Delete");
						menuItem.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											queryDeleteVariation();
										}
									});
					popup.add(menuItem);
					
						menuItem = new JMenuItem("Move to core exercises");
						menuItem.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											queryMoveVariationToCore();
										}
									});
					popup.add(menuItem);
					
						JLabel clickLabel = new JLabel("(right-click for menu)", JLabel.RIGHT);
						Border spacingBorder = BorderFactory.createEmptyBorder(5, 0, 5, 0);
						clickLabel.setBorder(spacingBorder);
						clickLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
					innerPanel.add(clickLabel);
					
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
					
					innerPanel.addMouseListener(popupListener);
					textArea.addMouseListener(popupListener);
				}
				else {
					editButton = new JButton("Edit");
					editButton.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										showEditWindow();
									}
								});
					innerPanel.add(editButton);
				
					JButton deleteButton = new JButton("Delete");
					deleteButton.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										queryDeleteVariation();
									}
								});
					innerPanel.add(deleteButton);
				}
				
			buttonPanel.add(innerPanel);
			
		add("East", buttonPanel);
		
		if (variation.neverSaved())
			showEditWindow();
	}
	
	private void showEditWindow() {
		VariationEditor variationFrame = new VariationEditor(variation, this);
		variationFrame.pack();
		variationFrame.setVisible(true);
		variationFrame.toFront();
		
		if (editButton != null)
			editButton.setEnabled(false);
	}
	
	private void queryDeleteVariation() {
		Object[] options = {"Delete", "Cancel"};
		int result = JOptionPane.showOptionDialog(this,
								"Are you sure that you want to delete variation \"" + getLongVariationName() + "\"?"
								+ "\nYou will not be able to undo this action.", "Delete?",
								JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);

		switch (result) {
			case JOptionPane.YES_OPTION:
				deleteVariation();
				break;
			case JOptionPane.NO_OPTION:
			default:
				break;
		}
	}
	
	private void queryMoveVariationToCore() {
		Object[] options = {"Move it", "Cancel"};
		int result = JOptionPane.showOptionDialog(this,
								"Are you sure that you want to move variation \"" + getLongVariationName() + "\" into the set of core variations?"
								+ "\nYou will not be able to undo this action.", "Move variation to core?",
								JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);

		switch (result) {
			case JOptionPane.YES_OPTION:
				DomTopic topic = exercise.getTopic();
				Document topicDocument = topic.getDocument();
				DomVariation newVariation = new DomVariation(variation, topicDocument);			//	creates copy belonging to topic document
				
				exercise.addVariation(newVariation);
				topic.saveDom();
				
				deleteVariation();
				
				break;
			case JOptionPane.NO_OPTION:
			default:
				break;
		}
	}
	
	protected String getLongVariationName() {
		return variation.getLongName();
	}
	
	protected Color getLineColor() {
		return kDarkRed;
	}
	
	public void deleteVariation() {
		customVariations.deleteVariation(variation, exercise);
		if (!variation.neverSaved())
			saveDom();
		
		mainFrame.updateVariationList();
	}
	
	public void saveDom() {
		customVariations.saveDom();
		variation.clearNeverSaved();
		variation.clearDomChanged();
	}
}

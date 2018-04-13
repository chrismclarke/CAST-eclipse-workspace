package cast.pageEditor;

import java.awt.*;

import javax.swing.*;


public class HtmlImageElement extends CoreHtmlElement {
	static final private Color kImageBackground = new Color(0xAAAAAA);
	
	static final private int kImageWidth = 100;
	static final private int kImageHeight = 50;
	
	private String imageString;
	
	public HtmlImageElement(String imageString, JPanel parent) {
		this.imageString = imageString;
		
		JPanel thePanel = new JPanel();
		thePanel.setOpaque(false);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			JPanel imageCanvas = new JPanel() {
															public void paintComponent(Graphics g) {
																g.setColor(kImageBackground);
																g.fillRect(0, 0, kImageWidth, kImageHeight);
																g.setColor(Color.black);
																g.drawRect(0, 0, kImageWidth - 1, kImageHeight - 1);
																
																int textWidth = g.getFontMetrics().stringWidth("Image");
																g.drawString("Image", (kImageWidth - textWidth) / 2, kImageHeight / 2 + 5);
															}
															
															public Dimension getPreferredSize() {
																return new Dimension(kImageWidth, kImageHeight);
															}
														};
		
		thePanel.add(imageCanvas);
		
		parent.add(thePanel);
	}
	
	public String getHtml() {
		return imageString;
	}
}

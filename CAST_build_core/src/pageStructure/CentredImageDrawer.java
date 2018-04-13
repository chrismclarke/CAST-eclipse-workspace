package pageStructure;

import java.awt.*;
import java.util.regex.*;
import java.io.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.html.*;

import javafx.application.*;
import javafx.embed.swing.*;
import javafx.scene.*;
import javafx.scene.web.*;

import ebook.*;


public class CentredImageDrawer extends CoreDrawer {
	static final private String kSrcPattern = "src=\"(.*?)\"";
	static final private String kWidthPattern = "width=\"(.*?)\"";
	static final private String kHeightPattern = "height=\"(.*?)\"";
	static final private String kClassPattern = "class=\"(.*?)\"";
	
	private String bgColorString;
	private File imgFile = null, svgFile = null;
	private int width, height;
	private UiImage theImage;
	private JFXPanel htmlPanel;
	private boolean isSummaryPict = false;
	
	public CentredImageDrawer(String divString, String dirString, StyleSheet theStyleSheet,
																																				BookFrame theBookFrame) {
		bgColorString = getBackgroundColorString(theStyleSheet);
		Pattern srcPattern = Pattern.compile(kSrcPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher srcMatcher = srcPattern.matcher(divString);
		Pattern widthPattern = Pattern.compile(kWidthPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher widthMatcher = widthPattern.matcher(divString);
		Pattern heightPattern = Pattern.compile(kHeightPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher heightMatcher = heightPattern.matcher(divString);
		if (srcMatcher.find() && widthMatcher.find() && heightMatcher.find()) {
			String src = srcMatcher.group(1);
			width = Integer.parseInt(widthMatcher.group(1));
			height = Integer.parseInt(heightMatcher.group(1));
			File coreDir = theBookFrame.getEbook().getCoreDir();
			File pageDir = new File(coreDir, dirString);
			imgFile = new File(pageDir, src);
			
			Pattern classPattern = Pattern.compile(kClassPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			Matcher classMatcher = classPattern.matcher(divString);
			if (classMatcher.find()) {
				String imgClass = " " + classMatcher.group(1) + " ";
				if ((imgClass.contains(" gif ") || imgClass.contains(" svgImage ")) && (src.endsWith(".gif") || src.endsWith(".png"))) {
					String svgSrc = src.substring(0, src.length() - 4) + ".svg";
					svgFile = new File(pageDir, svgSrc);
				}
				isSummaryPict = imgClass.contains(" summaryPict ");
			}
		}
	}
	
	
	public JPanel createPanel() {
		JPanel thePanel	= new JPanel();
		thePanel.setOpaque(false);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
//		thePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		
		if (svgFile == null) {
			if (isSummaryPict) {
				Border outerSpacing = BorderFactory.createEmptyBorder(0, 30, 0, 30);
				Border lineBorder = BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(0x999999));
				Border innerSpacing = BorderFactory.createEmptyBorder(8, 3, 8, 3);
				thePanel.setBorder(BorderFactory.createCompoundBorder(outerSpacing, BorderFactory.createCompoundBorder(lineBorder, innerSpacing)));
			}
			
			theImage = new UiImage(imgFile, width, height);
			theImage.setPageScaling(true);
			
			thePanel.add(theImage);
		}
		else {
			thePanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
			htmlPanel = new JFXPanel() {
									public Dimension getPreferredSize() {
										return new Dimension(CoreDrawer.scaledSize(width), CoreDrawer.scaledSize(height));
									}
									public Dimension getMinimumSize() {
										return getPreferredSize();
									}
			};

			Platform.runLater(new Runnable() {	//	run on the JavaFX thread
															public void run() {
																initFX(htmlPanel);
															}
			});
			Platform.setImplicitExit(false);
			
			thePanel.add(htmlPanel);
		}
		
		return thePanel;
	}

	private void initFX(JFXPanel htmlPanel) {
		// This method is invoked on the JavaFX thread
		WebView webView = new WebView();
		htmlPanel.setScene(new Scene(webView));
		try {
//			String imgString = "<img src='file://" + svgFile.getCanonicalPath() + "' width='100%' height='100%'"
//																	+ " style='margin:0px; padding:0px'>";
//			String svgUrlString = svgFile.getCanonicalPath();
			String svgUrlString = new File(svgFile.getCanonicalPath()).toURI().toURL().toString();
			String imgString = "<object data='" + svgUrlString + "' type='image/svg+xml' width='100%' height='100%'"
																	+ " style='margin:0px; padding:0px'>";
			String htmlString = "<html><body style='margin:0px; padding:0px' bgcolor='#" + bgColorString + "'>\n" + imgString + "\n</body></html>";
			
			webView.getEngine().loadContent(htmlString);
		} catch (IOException e) {
			System.out.println("Error! Could not get canonical path for SVG file: " + svgFile.toString());
			e.printStackTrace(System.out);
		}
	}
	
	
	public int getMinimumWidth() {
		if (svgFile == null)
			return theImage.getPreferredSize().width;
		else
			return htmlPanel.getPreferredSize().width;
	}
}

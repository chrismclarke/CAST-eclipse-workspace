package pageStructure;

import java.awt.*;
import java.util.regex.*;
import java.io.*;

import javax.swing.*;

import javafx.application.*;
import javafx.embed.swing.*;
import javafx.scene.*;
import javafx.scene.web.*;

import ebook.*;
import ebookStructure.*;


public class Html5Drawer extends CoreDrawer {
	static final public String kStartHtmlString = "<!DOCTYPE HTML>\n<html>\n<head>\n"
																+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
																+ "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n"
																+ "<head><body>";
																
	static final public String kEndHtmlString = "\n</body>\n</html>";
	
	static final private String kDimensionPattern = "<!-- width=(\\d*)\\s+height=(\\d*)-->";
	
	private JFXPanel htmlPanel = null;
	private String pageHtml = null;
	private int width = 650, height = 550;
	
	public Html5Drawer(String htmlString, BookFrame theBookFrame) {
		CastEbook theEbook = theBookFrame.getEbook();
		File coreDir = theEbook.getCoreDir();
		try {
			String coreUrl = new File(coreDir.getCanonicalPath()).toURI().toURL().toString();
//			String path = coreDir.getCanonicalPath();
//			String coreUrl = "file:///" + path;
			
			pageHtml = kStartHtmlString + "\n\n" + htmlString + "\n\n" + kEndHtmlString;
			pageHtml = pageHtml.replaceAll("../../", coreUrl + "/");
			
			Pattern dimensionPattern = Pattern.compile(kDimensionPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			Matcher dimensionMatcher = dimensionPattern.matcher(htmlString);
			if (dimensionMatcher.find()) {
				width = Integer.parseInt(dimensionMatcher.group(1));
				height = Integer.parseInt(dimensionMatcher.group(2));
			}
		} catch (IOException e) {
		}
	}
	
	
	public JPanel createPanel() {
		JPanel thePanel = new JPanel();
		thePanel.setOpaque(false);
		
		if (pageHtml != null) {
			thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
			htmlPanel = new JFXPanel() {
											public Dimension getMinimumSize() {
												return new Dimension(width, height);
											}
											
											public Dimension getPreferredSize() {
												return new Dimension(width, height);
											}
										};
			htmlPanel.setOpaque(false);
			thePanel.add(htmlPanel);
			
			Platform.runLater(new Runnable() {
				public void run() {
					initFX(htmlPanel);
				}
			});
			Platform.setImplicitExit(false);
		}
		else {
			thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			thePanel.setBackground(Color.red);
			thePanel.add(new JLabel("Error: Cannot find CSS file!", JLabel.LEFT));
		}
		
		return thePanel;
	}
	
	
//**************************************
//******	method invoked on JavaFX thread
//**************************************

	private void initFX(JFXPanel htmlPanel) {
							// Fill the JavaFX panel
		WebView view = new WebView();
		WebEngine engine = view.getEngine();
		engine.loadContent(pageHtml);
		
    Scene scene = new Scene(view, width, height);
    htmlPanel.setScene(scene);
	}
}

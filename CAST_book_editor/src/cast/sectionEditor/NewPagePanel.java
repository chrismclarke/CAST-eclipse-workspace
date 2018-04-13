package cast.sectionEditor;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;

import cast.bookManager.*;

public class NewPagePanel extends CorePagePanel {
	static final private Color kNewBackground = new Color(0xFFFFCC);
	static final private Color kSelectedBackground = new Color(0xBBBBFF);
	
	static public Dom2Page createNewPage() {
		try {
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			Document localDoc = docBuilder.newDocument();
			
			Element root = localDoc.createElement("root");
			localDoc.appendChild(root);
			
			Element pageElement = localDoc.createElement("page");
			pageElement.setAttribute("dir", "bk/general");
			pageElement.setAttribute("filePrefix", "newPage");
			
			pageElement.appendChild(localDoc.createTextNode("Description of page"));
			
			root.appendChild(pageElement);
			return new Dom2Page(pageElement);
		}
		catch (Exception e) {
			System.err.println("NewTitle: could not create new DOM elements");
			return null;
		}
	}
	
	private JPanel titlePanel;
	
	public NewPagePanel() {
		super(createNewPage(), null, 0);
		
		setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		setOpaque(false);
		
		titlePanel = new JPanel();
		titlePanel.setBackground(kNewBackground);
		titlePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
			Border raisedetched = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
			Border spaced = BorderFactory.createEmptyBorder(3, 10, 3, 10);
			Border compound = BorderFactory.createCompoundBorder(raisedetched, spaced);
			titlePanel.setBorder(compound);
		
				JLabel title = new JLabel("New page");
				title.setFont(new Font("SansSerif", Font.BOLD, 14));
			
			titlePanel.add(title);
		
		add(titlePanel);
		
		addMouseListener(new MouseDragListener());
	}
	
	public void updatePageDom() {
	}
	
	public void doHighlight(boolean selected) {
		titlePanel.setBackground(selected ? kSelectedBackground : kNewBackground);
	}
	
}

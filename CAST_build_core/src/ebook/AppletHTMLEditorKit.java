package ebook;

import java.awt.*;
import java.net.*;
import java.io.*;

import javax.swing.text.*;
import javax.swing.text.html.*;


public class AppletHTMLEditorKit extends HTMLEditorKit {
						//		based on http://java-sl.com/custom_tag_html_kit.html
	
	private Color backgroundColor, foregroundColor;
	
	public AppletHTMLEditorKit(Color backgroundColor, Color foregroundColor) {
		this.backgroundColor = backgroundColor;
		this.foregroundColor = foregroundColor;
	}
	
	public ViewFactory getViewFactory() {
		return new MyHTMLFactory();
	}

	public Document createDefaultDocument() {
		StyleSheet styles = getStyleSheet();
		StyleSheet ss = new StyleSheet();

		ss.addStyleSheet(styles);

		MyHTMLDocument doc = new MyHTMLDocument(ss);
		doc.setParser(getParser());
		doc.setAsynchronousLoadPriority(4);
		doc.setTokenThreshold(100);
		
		File currentDir = new File(System.getProperty("user.dir"));
		try {
			doc.setBase(currentDir.toURI().toURL());
		} catch (MalformedURLException e) {
		}
		return doc;
	}
	
//------------------------------------------------------------------
	
	class MyHTMLFactory extends HTMLFactory {
		
		public View create(final Element element) {
			AttributeSet attrs = element.getAttributes();
			HTML.Tag kind = (HTML.Tag) (attrs.getAttribute(javax.swing.text.StyleConstants.NameAttribute));
			
			if (kind == HTML.Tag.IMG) {
				final String latexString = (String) attrs.getAttribute(HTML.Attribute.SRC);
				if (latexString.indexOf("\\(") == 0)
					return new ComponentView(element) {
								protected Component createComponent() {
									LatexView theView = new LatexView(latexString.substring(2, latexString.length()-2));
									theView.setForeground(foregroundColor);
									theView.setBackground(backgroundColor);
									return theView;
								}
							};
				else
					return new ImageView(element);
			}
			else
				return super.create(element);
		}
	}
	
//------------------------------------------------------------------
	
	class MyHTMLDocument extends HTMLDocument {
    public MyHTMLDocument(StyleSheet styles) {
			super(styles);
    }

    public HTMLEditorKit.ParserCallback getReader(int pos) {
			Object desc = getProperty(Document.StreamDescriptionProperty);
			if (desc instanceof URL) {
				setBase((URL)desc);
			}
			return new MyHTMLReader(pos);
    }

    class MyHTMLReader extends HTMLDocument.HTMLReader {
			public MyHTMLReader(int offset) {
				super(offset);
			}
			public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
				super.handleStartTag(t, a, pos);
			}
    }
	}
	
/*
//------------------------------------------------------------------
	

	class MyParserDelegator extends ParserDelegator {
		public MyParserDelegator() {
        try {
            //for JDK version later than 1.6_26
            Field f=javax.swing.text.html.parser.ParserDelegator.class.getDeclaredField("DTD_KEY");
            AppContext appContext = AppContext.getAppContext();
            f.setAccessible(true);
            Object dtd_key = f.get(null);
 
            DTD dtd = (DTD) appContext.get(dtd_key);
 
//            javax.swing.text.html.parser.Element div = dtd.getElement("div");
//            dtd.defineElement("castapplet", div.getType(), true, true, div.getContent(), null, null, div.getAttributes());
//            dtd.defineElement("displaytex", div.getType(), true, true, div.getContent(), null, null, div.getAttributes());
						
//						dtd.defineElement("inlinetex", div.getType(), true, true, div.getContent(), null, null, div.getAttributes());
            javax.swing.text.html.parser.Element img = dtd.getElement("img");
						dtd.defineElement("inlinetex", img.getType(), true, true, img.getContent(), null, null, img.getAttributes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	}
*/
}
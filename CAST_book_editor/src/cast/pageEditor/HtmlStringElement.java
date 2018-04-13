package cast.pageEditor;


public class HtmlStringElement extends CoreHtmlElement {
	private String htmlString;
	
	public HtmlStringElement(String htmlString) {
//		System.out.println("String element: " + htmlString);
		
		this.htmlString = htmlString;
	}
	
	public String getHtml() {
		return htmlString;
	}
}

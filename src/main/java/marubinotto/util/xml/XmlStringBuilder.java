package marubinotto.util.xml;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import marubinotto.util.Assert;

public class XmlStringBuilder {

	private StringBuilder buffer = new StringBuilder();
	
	private static class Element {
		public String name;
		
		public Element(String name) {
			this.name = name;
		}
	}
	
	private Stack<Element> elementStack = new Stack<Element>();
	private Map<Element, Boolean> hasChildren = new HashMap<Element, Boolean>();
	
	public static XmlStringBuilder create(String elementName) {
		XmlStringBuilder builder = new XmlStringBuilder();
		return builder.element(elementName);
	}
	
	public XmlStringBuilder element(String name) {
		Assert.Arg.notNull(name, "name");
		
		onChildAdded();
		
		this.buffer.append("<");
		this.buffer.append(name);
		
		this.elementStack.push(new Element(name));
		return this;
	}
	
	public XmlStringBuilder attribute(String name, Object value) {
		Assert.Arg.notNull(name, "name");
		Assert.Arg.notNull(value, "value");
		
		this.buffer.append(" ");
		this.buffer.append(name);
		this.buffer.append("=\"");
		this.buffer.append(value.toString());
        this.buffer.append("\"");		
		return this;
	}
	
	public XmlStringBuilder text(String text) {
		Assert.Arg.notNull(text, "text");
		
		onChildAdded();
		
		this.buffer.append(text);
		
		return this;
	}
	
	public XmlStringBuilder end() {
		Element element = this.elementStack.pop();
		if (hasChildren(element)) {
			this.buffer.append("</");
			this.buffer.append(element.name);
			this.buffer.append(">");
		}
		else {
			this.buffer.append("/>");
		}
		return this;
	}
	
	public XmlStringBuilder endAll() {
		while (!elementStack.isEmpty()) {
			end();
		}
		return this;
	}
	
	@Override
	public String toString() {
		endAll();
		return this.buffer.toString();
	}
	
// Internal
	
	private boolean hasChildren(Element element) {
		Boolean flag = this.hasChildren.get(element);
		return flag != null && flag.equals(Boolean.TRUE);
	}
	
	private void onChildAdded() {
		if (this.elementStack.isEmpty()) return;
		
		Element parent = this.elementStack.peek();
		if (!hasChildren(parent)) {
			this.buffer.append(">");
			this.hasChildren.put(parent, Boolean.TRUE);
		}
	}
}

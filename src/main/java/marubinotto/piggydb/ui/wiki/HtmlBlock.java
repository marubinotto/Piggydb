package marubinotto.piggydb.ui.wiki;

public class HtmlBlock extends org.apache.commons.lang.enums.Enum {
	
	public static final HtmlBlock PARAGRAPH = new Paragraph();
	public static final HtmlBlock PREFORMATTED_TEXT = new HtmlBlock("pre", 2);
	public static final HtmlBlock BLOCKQUOTE = new HtmlBlock("blockquote", 3);
	public static final HtmlBlock UNORDERED_LIST = new UnorderedList();
	public static final HtmlBlock ORDERED_LIST = new OrderedList();
	public static final HtmlBlock DEFINITION_LIST = new HtmlBlock("dl", 5);
	public static final HtmlBlock TABLE = new HtmlBlock("table", 6);
	
	private int typeId;
	
    private HtmlBlock(String name, int typeId) {
    	super(name);
        this.typeId = typeId;
    }
    
    public static HtmlBlock getEnum(String name) {
        return (HtmlBlock)getEnum(HtmlBlock.class, name);
    }
    
    public boolean isSameTypeTo(HtmlBlock blockType) {
    	return blockType.typeId == this.typeId;
    }
    
    public void open(ParseContext context, int level) {
    	context.println("<" + getName() +  ">");
    }
    
    public void readyToAppend(ParseContext context) { 	
    }
    
    public void close(ParseContext context) {
    	context.println("</" + getName() + ">");
    }
    
    public static class Paragraph extends HtmlBlock {
    	public static final String OPEN = "<div class=\"paragraph\">";
    	public static final String CLOSE = "</div>";
    	
    	public Paragraph() {
    		super("p", 1);
    	}
    	
    	@Override
    	public void open(ParseContext context, int level) {
        	context.println(OPEN);
        }
    	
    	@Override
        public void close(ParseContext context) {
        	context.println(CLOSE);
        }
    }
        
    public static class UnorderedList extends HtmlBlock {
    	private UnorderedList() {
    		super("ul", 4);
    	}
    	
    	@Override
    	public void open(ParseContext context, int level) {
        	context.print("<ul class=\"level" + level + "\"><li>");
        }
        
    	@Override
        public void readyToAppend(ParseContext context) {
    		context.println("</li>");
    		context.print("<li>");
        }
        
    	@Override
        public void close(ParseContext context) {
        	context.println("</li></ul>");
        }
    }
    
    public static class OrderedList extends HtmlBlock {
    	private OrderedList() {
    		super("ol", 4);
    	}
    	
    	@Override
    	public void open(ParseContext context, int level) {
        	context.print("<ol class=\"level" + level + "\"><li>");
        }
        
    	@Override
        public void readyToAppend(ParseContext context) {
    		context.println("</li>");
    		context.print("<li>");
        }
        
    	@Override
        public void close(ParseContext context) {
        	context.println("</li></ol>");
        }
    }
}
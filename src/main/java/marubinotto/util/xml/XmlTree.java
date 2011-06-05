package marubinotto.util.xml;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * <p>A XmlTree is a model that represents an XML document or fragment
 * as a tree of nodes. This interface provides convenient function
 * to manipulate the tree.
 * What is the difference from {@link org.w3c.dom.Document} is that
 * XmlTree can be based on an arbitrary node as a root of tree.</p>
 *
 * <p>XmlTree also provides dynamic attribute mapping using XPath,
 * which hides XML structure behind object interface.</p>
 *
 * @version $Id: XmlTree.java 1286 2008-03-20 15:39:37Z morita $
 */
public interface XmlTree extends XmlSerializable {

    /**
     * Return the root node of this tree.
     */
    public Node getRoot();

    /**
     * Set the given node as the root of this tree.
     */
    public void setRoot(Node rootNode) throws Exception;

    /**
     * Return the Document object associated with this tree.
     */
    public Document getOwnerDocument();

// XPath

    /**
     * Set the mapping the given prefix to the given namespace URI.
     * The mapping is used to evaluate a XPath.
     */
    public void setNamespacePrefix(String prefix, String namespaceUri);

    /**
     * Select only the first node that is selectable by the given
     * XPath expression using the root node as a context.
     * If multiple nodes match, the first node node will be returned.
     */
    public Node selectSingleNode(String xpath) throws Exception;

    /**
     * <p>Select only the first node that is selectable by the given
     * XPath expression and locale using the root node as a context.</p>
     *
     * <p>The locale will be matched to <code>xml:lang</code> attribute of
     * the selected nodes or its ancestors. If such nodes are not found,
     * the first node of those which do not have an "xml:lang" attribute
     * will be returned as a default.</p>
     */
    public Node selectSingleNode(String xpath, Locale locale)
    throws Exception;

    /**
     * Selects all nodes that are selectable by the given XPath expression
     * using the root node as a context.
     *
     * @return The node(org.w3c.dom.Node) set of all items
     *   selected by the given XPath expression.
     */
    public List<Node> selectNodes(String xpath) throws Exception;

    /**
     * Selects all subtree that are selectable by the given
     * XPath expression using the root node as a context.
     *
     * @return The XmlTree set of all items selected by the given XPath expression.
     */
    public List<XmlTree> selectSubtrees(String xpath) throws Exception;

    /**
     * <p>Selects all values that are selectable by the given
     * XPath expression using the root node as a context.</p>
     *
     * <p>All selected nodes will be evaluated as a String value.
     * The detail spec of evaluation is the same as that of
     * {@link #stringValueOf(String)}.</p>
     *
     * @return The value of all nodes selected by the given XPath expression.
     */
    public List<String> selectValues(String xpath) throws Exception;

    /**
     * <p>Selects only the first node that is selectable
     * by the given XPath expression using the root node as a context,
     * and evaluates the node as a string.</p>
     *
     * <p>The result of evaluation will be slightly different from
     * that of the <code>string()</code> XPath core function (see
     * <a href="http://www.w3.org/TR/xpath#section-String-Functions">
     * http://www.w3.org/TR/xpath#section-String-Functions</a>).
     * The difference is element evaluation. If the node is an element,
     * returns the textual content directly held under this element as a string.
     * This includes all text within this single element,
     * including whitespace and CDATA sections if they exist.
     * The call does not recurse into child elements.
     * If no textual value exists for the element, a null value is returned.</p>
     */
    public String stringValueOf(String xpath) throws Exception;

    /**
     * <p>Selects only the first node that is selectable by the given
     * XPath expression and locale using the root node as a context,
     * and evaluates the node as a string.</p>
     *
     * <p>The nodes selected by this method are the same as
     * those of {@link #selectSingleNode(String, Locale)}.</p>
     *
     * <p>The detail spec of evaluation is described in the document
     * of {@link #stringValueOf(String)}.</p>
     */
    public String stringValueOf(String xpath, Locale locale)
    throws Exception;

    /**
     * <p>Selects the nodes that are selectable by the given
     * XPath expression using the root node as a context,
     * and evaluates the nodes as a boolean.<p>
     *
     * <p>The detail spec of evaluation is the same as that of
     * the <code>boolean()</code> XPath core function (see
     * <a href="http://www.w3.org/TR/xpath#section-Boolean-Functions">
     * http://www.w3.org/TR/xpath#section-Boolean-Functions</a>).
     * This means that an expression that selects zero nodes will return false,
     * while an expression that selects one-or-more nodes will return true.</p>
     */
    public boolean booleanValueOf(String xpath) throws Exception;

    /**
     * <p>Selects the nodes that are selectable by the given
     * XPath expression using the root node as a context,
     * and evaluates the nodes as a number.<p>
     *
     * <p>The detail spec of evaluation is the same as that of
     * the <code>number()</code> XPath core function (see
     * <a href="http://www.w3.org/TR/xpath#section-Number-Functions">
     * http://www.w3.org/TR/xpath#section-Number-Functions</a>).</p>
     */
    public Number numberValueOf(String xpath) throws Exception;

    /**
     * Register Name-XPath mappings to this tree.
     * The purpose of this mappings is to expose XPath definition.
     * You can override those XPaths that this tree uses.
     * The default XPaths are generally provided by XmlTree implementation.
     */
    public void setXpaths(Map<String, String> xpaths);

    /**
     * Return the Name-XPath mappings.
     */
    public Map<String, String> getXpaths();

    /**
     * Return the xpath specified by the given name.
     *
     * @exception IllegalArgumentException the specified mapping doesn't exist.
     */
    public String getXpath(String name);

    /**
     * Get a node value by the given XPath name.
     * The value returned by this method is internally calculated
     * by {@link #stringValueOf(String)} .
     *
     * @see #setXpaths
     */
    public String get(String name);

    /**
     * Get a node value by the given XPath name and locale.
     * The value returned by this method is internally calculated
     * by {@link #stringValueOf(String, Locale)}.
     *
     * @see #setXpaths
     */
    public String get(String name, Locale locale);

    /**
     * Set a value to the single node specified by the given XPath name.
     * The target node to modify will be the same as those selected by
     * {@link #selectSingleNode(String)}.
     * If the specified node is not found, this method will do nothing
     * and return false.
     *
     * @return true if and only if the value is successfully set;
     *  false otherwise because of missing the node.
     * @see #setXpaths
     */
    public boolean set(String name, String value);

    /**
     * Set a value to the single node specified by the given XPath name and locale.
     * The target node to modify will be the same as those selected by
     * {@link #selectSingleNode(String, Locale)}.
     * If the specified node is not found, this method will do nothing
     * and return false.
     *
     * @return true if and only if the value is successfully set;
     *  false otherwise because of missing the node.
     * @see #setXpaths
     */
    public boolean set(String name, Locale locale, String value);

    /**
     * Exception thrown when invalid root node is given.
     */
    public static class InvalidRootException extends RuntimeException {
        public InvalidRootException(String message, Node invalidRoot) {
            super(message + " (actual: " + DomUtils.toString(invalidRoot) + ")");
        }
    }
}

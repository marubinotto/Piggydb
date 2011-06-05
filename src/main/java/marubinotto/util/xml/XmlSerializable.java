package marubinotto.util.xml;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.w3c.dom.Node;

/**
 * XmlSerializable interface represents XML Serializability.
 * If a class implements this interface, the instance of the class
 * can be serialized as XML format.
 *
 * @version $Id: XmlSerializable.java 1286 2008-03-20 15:39:37Z morita $
 */
public interface XmlSerializable {

    /**
     * Deserialize this object from the given XML input stream.
     */
    public void readFrom(InputStream xmlInput) throws Exception;

    /**
     * Deserialize this object from the given XML file.
     */
    public void readFrom(File xmlFile) throws Exception;

    /**
     * Deserialize this object from the given XML string.
     */
    public void fromXml(String xml) throws Exception;

    /**
     * Deserialize this object from the given DOM tree.
     */
    public void fromDom(Node node) throws Exception;

    /**
     * Serialize this object to the given output stream.
     */
    public void writeTo(OutputStream xmlOut) throws Exception;

    /**
     * Serialize this object to the given output stream
     * with the given encoding.
     */
    public void writeTo(OutputStream xmlOut, String encoding) throws Exception;

    /**
     * Serialize this object to the given file.
     */
    public void writeTo(File file) throws Exception;

    /**
     * Serialize this object to the given file with the given encoding.
     */
    public void writeTo(File file, String encoding) throws Exception;

    /**
     * Serialize this object to a XML string.
     */
    public String toXml();

    /**
     * Serialize this object to a DOM tree.
     */
    public Node toDom();
}

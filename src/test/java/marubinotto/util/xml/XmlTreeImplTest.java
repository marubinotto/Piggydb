package marubinotto.util.xml;

import static org.junit.Assert.*;

import org.junit.Test;
import org.w3c.dom.Document;

/**
 * marubinotto.util.xml.XmlTreeImplTest
 */
public class XmlTreeImplTest extends XmlTreeTestBase {

    protected XmlTree createXmlTree() throws Exception {
        return new XmlTreeImpl();
    }

    @Test
    public void shouldSetRootViaConstructor() throws Exception {
        Document document = createDocument(null, "root");

        XmlTreeImpl tree = new XmlTreeImpl(document);
        assertSame(document, tree.getRoot());
    }
}

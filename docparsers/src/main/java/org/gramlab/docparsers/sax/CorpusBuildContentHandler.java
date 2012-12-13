package org.gramlab.docparsers.sax;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.apache.tika.sax.WriteOutContentHandler;
import org.gramlab.docparsers.util.StringUtils;
import org.gramlab.docparsers.util.TEIConstants;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.Attributes2Impl;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.XMLConstants;
import java.io.OutputStream;
import java.io.Writer;

/**
 * User: freddy
 * Date: 18/05/11
 * Time: 17:46
 */

/**
 * This ContentHandler allows to merge several TEI file in a TEI Corpus. In order to do this, one must specify a header at
 * construction then before parsing each sub TEI file, {@link #startCorpus} must be call once and {@link #closeCorpus} should be called
 * at end of the process.
 */
public class CorpusBuildContentHandler extends WriteOutContentHandler {

    private static final String DEFAULT_ENCODING = "utf-8";
    private String encoding = DEFAULT_ENCODING;
    private BidiMap prefixMapping = new DualHashBidiMap();
    private static final Attributes EMPTY_ATTRIBUTES = new AttributesImpl();
    private TEIHeader header;

    /**
     * The XHTML namespace URI
     */
    private static final String XHTML_NS = "http://www.w3.org/1999/xhtml";

    public CorpusBuildContentHandler(final Writer writer, final TEIHeader header) {
        super(writer);
        this.header = header;
    }

    public CorpusBuildContentHandler(final Writer writer, final TEIHeader header, final String encoding) {
        super(writer);
        this.encoding = encoding;
        this.header = header;
    }

    public CorpusBuildContentHandler(final OutputStream stream, final TEIHeader header) {
        super(stream);
        this.header = header;
    }

    public CorpusBuildContentHandler(final OutputStream stream, final TEIHeader header, final String encoding) {
        super(stream);
        this.encoding = encoding;
        this.header = header;
    }

    public CorpusBuildContentHandler(final int writeLimit, final TEIHeader header) {
        super(writeLimit);
        this.header = header;
    }

    /**
     * This method must be called before any attempt to merge TEI file.
     *
     * @throws SAXException
     */
    public void startCorpus() throws SAXException {
        final String encodingHeader = "<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>" + System.getProperty("line.separator");
        charactersWithNoEncode(encodingHeader.toCharArray(), 0, encodingHeader.length());
        super.startDocument();
        startPrefixMapping(XMLConstants.DEFAULT_NS_PREFIX, TEIConstants.TEI_NS);
        startPrefixMapping("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
        Attributes2Impl att = new Attributes2Impl();
        att.addAttribute(
                XMLConstants.DEFAULT_NS_PREFIX,
                "xmlns", // localname
                "xmlns", // qualified name
                "CDATA", // type
                TEIConstants.TEI_NS);
        att.addAttribute(
                XMLConstants.DEFAULT_NS_PREFIX,
                "xmlns:xsi",
                "xmlns:xsi",
                "CDATA",
                XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
        att.addAttribute(
                XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
                "xsi:schemaLocation",
                "xsi:schemaLocation",
                "CDATA",
                TEIConstants.TEI_NS + " http://www.tei-c.org/release/xml/tei/custom/schema/xsd/teilite.xsd");
        startElement(TEIConstants.TEI_NS, "teiCorpus", "teiCorpus", att);
        writeHeader();
    }

    /**
     * This method must be called to close the corpus. If this method is not called at end of the merging process, the corpus
     * will not be valid.
     *
     * @throws SAXException
     */
    public void closeCorpus() throws SAXException {
        endElement(TEIConstants.TEI_NS, "teiCorpus", "teiCorpus");
        endPrefixMapping(XMLConstants.DEFAULT_NS_PREFIX);
        super.endDocument();
    }

    private void writeHeader() throws SAXException {
        startElement(TEIConstants.TEI_NS, "teiHeader", "teiHeader", EMPTY_ATTRIBUTES);
        startElement(TEIConstants.TEI_NS, "fileDesc", "fileDesc", EMPTY_ATTRIBUTES);

        // Title statement
        startElement(TEIConstants.TEI_NS, "titleStmt", "titleStmt", EMPTY_ATTRIBUTES);
        // title field
        startElement(XHTML_NS, "title", "title", EMPTY_ATTRIBUTES);
        if (header.getTitle() != null) {
            final String element = StringUtils.escapeXML(header.getTitle().trim());
            characters(element.toCharArray(), 0, element.length());
        }
        endElement(XHTML_NS, "title", "title");
        // author field
        if (header.getAuthor() != null) {
            startElement(TEIConstants.TEI_NS, "author", "author", EMPTY_ATTRIBUTES);
            final String element = StringUtils.escapeXML(header.getAuthor().trim());
            characters(element.toCharArray(), 0, element.length());
            endElement(TEIConstants.TEI_NS, "author", "author");
        }
        endElement(TEIConstants.TEI_NS, "titleStmt", "titleStmt");

        // Edition statement
        startElement(TEIConstants.TEI_NS, "editionStmt", "editionStmt", EMPTY_ATTRIBUTES);
        startElement(XHTML_NS, "p", "p", EMPTY_ATTRIBUTES);
        if (header.getRevision() != null) {
            final String editionStr = "Edition rev. " + header.getRevision();
            final String element = StringUtils.escapeXML(editionStr);
            characters(element.toCharArray(), 0, element.length());
        }
        if (header.getLastModificationDate() != null) {
            if (header.getRevision() != null) {
                final String sep = " - ";
                final String element = StringUtils.escapeXML(sep);
                characters(element.toCharArray(), 0, element.length());
            }
            startElement(TEIConstants.TEI_NS, "date", "date", EMPTY_ATTRIBUTES);
            final String element = StringUtils.escapeXML(header.getLastModificationDate().trim());
            characters(element.toCharArray(), 0, element.length());
            endElement(TEIConstants.TEI_NS, "date", "date");

        }
        endElement(XHTML_NS, "p", "p");
        endElement(TEIConstants.TEI_NS, "editionStmt", "editionStmt");

        // Publication Statement
        startElement(TEIConstants.TEI_NS, "publicationStmt", "publicationStmt", EMPTY_ATTRIBUTES);
        startElement(TEIConstants.TEI_NS, "publisher", "publisher", EMPTY_ATTRIBUTES);
        if (header.getCompany() != null) {
            final String element = StringUtils.escapeXML(header.getCompany().trim());
            characters(element.toCharArray(), 0, element.length());
        }
        endElement(TEIConstants.TEI_NS, "publisher", "publisher");

        if (header.getCreationDate() != null) {
            startElement(TEIConstants.TEI_NS, "date", "date", EMPTY_ATTRIBUTES);
            final String element = StringUtils.escapeXML(header.getCreationDate().trim());
            characters(element.toCharArray(), 0, element.length());
            endElement(TEIConstants.TEI_NS, "date", "date");
        }
        endElement(TEIConstants.TEI_NS, "publicationStmt", "publicationStmt");

        // source description statement (mandatory)
        startElement(TEIConstants.TEI_NS, "sourceDesc", "sourceDesc", EMPTY_ATTRIBUTES);
        startElement(XHTML_NS, "p", "p", EMPTY_ATTRIBUTES);
        if (header.getResourceName() != null) {
            final String element = StringUtils.escapeXML(header.getResourceName().trim());
            characters(element.toCharArray(), 0, element.length());
        }
        endElement(XHTML_NS, "p", "p");
        endElement(TEIConstants.TEI_NS, "sourceDesc", "sourceDesc");

        endElement(TEIConstants.TEI_NS, "fileDesc", "fileDesc");
        endElement(TEIConstants.TEI_NS, "teiHeader", "teiHeader");
    }

    @Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		String prefixStr = "";
		Object prefix = prefixMapping.getKey(uri);
		if (prefix != null) {
			prefixStr = (String)prefix;
			if (prefixStr.length() > 0) prefixStr = prefix + ":";
		}
		final StringBuilder sb = new StringBuilder();
		final int attCount = attributes.getLength();
		sb.append('<').append(prefixStr).append(qName);
		for (int i = 0; i < attCount; i++) {
            // in a corpus, the namespaces and schema locations are set in the overall enclosing tag (aka teiCorpus)
            if (!localName.equals("TEI")) {
			    sb.append(' ').append(attributes.getQName(i)).append("=\'").append(StringUtils.escapeXML(attributes.getValue(i))).append("\'");
            }
		}
		sb.append('>');
		final String element = sb.toString().trim();
		charactersWithNoEncode(element.toCharArray(), 0, element.length());
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		String prefixStr = "";
		Object prefix = prefixMapping.getKey(uri);
		if (prefix != null) {
			prefixStr = (String)prefix;
			if (prefixStr.length() > 0) prefixStr = prefix + ":";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("</").append(prefixStr).append(qName).append('>');
		final String element = sb.toString().trim();
		charactersWithNoEncode(element.toCharArray(), 0, element.length());
	}

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        prefixMapping.put(prefix, uri);
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        prefixMapping.remove(prefix);
    }

    public void charactersWithNoEncode(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        final String encoded = StringUtils.escapeXML(String.copyValueOf(ch, start, length));
        super.characters(encoded.toCharArray(), 0, encoded.length());
    }
}

 package org.gramlab.docparsers.sax;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.sax.ElementMappingContentHandler;
import org.apache.tika.sax.SafeContentHandler;
import org.gramlab.docparsers.util.StringUtils;
import org.gramlab.docparsers.util.TEIConstants;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.Attributes2Impl;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.util.HashMap;

/**
 * User: freddy
 * Date: 18/05/11
 * Time: 16:09
 */

/**
 * <p>A content handler that produces TEI LITE output. See <a href="http://www.tei-c.org/Guidelines/Customization/Lite/">
 * <code>http://www.tei-c.org/Guidelines/Customization/Lite/</code></a> for further information about this XML format.</p>
 * <p>This handler tries to get information for parsers provided by tika in a best effort way. For example, some of them do not produce metadata
 * information in their output so tei header is quite empty.</p>
 * <p>Do not use the same TEIContentHandler for several parsing. It is not re entrant because it maintains an internal node. Use one instance per thread,
 * it is not thread-safe.</p>
 * @author Freddy RABILLER
 * @since 1.0.0
 */
public class TEIContentHandler extends SafeContentHandler {

	/**
	 * The XHTML namespace URI
	 */
	private static final String XHTML_NS = "http://www.w3.org/1999/xhtml";

	/**
	 * Mappings between ODF tag names and XHTML tag names (including attributes). All other tag names/attributes are ignored
	 * and left out from event stream.
	 */
    private static final HashMap<QName, ElementMappingContentHandler.TargetElement> MAPPINGS =
        new HashMap<QName, ElementMappingContentHandler.TargetElement>();

    static {
		MAPPINGS.put(
				new QName(XHTML_NS, "body"),
				new ElementMappingContentHandler.TargetElement(TEIConstants.TEI_NS, "body"));
		MAPPINGS.put(
                new QName(XHTML_NS, "p"),
                new ElementMappingContentHandler.TargetElement(TEIConstants.TEI_NS, "p"));
        MAPPINGS.put(
                new QName(XHTML_NS, "br"),
                new ElementMappingContentHandler.TargetElement(TEIConstants.TEI_NS, "lb"));
        MAPPINGS.put(
                new QName(XHTML_NS, "em"),
                new ElementMappingContentHandler.TargetElement(TEIConstants.TEI_NS, "emph"));
        MAPPINGS.put(
                new QName(XHTML_NS, "ul"),
                new ElementMappingContentHandler.TargetElement(TEIConstants.TEI_NS, "list"));
        MAPPINGS.put(
                new QName(XHTML_NS, "li"),
                new ElementMappingContentHandler.TargetElement(TEIConstants.TEI_NS, "item"));
        // create HTML tables from table:-tags
		MAPPINGS.put(
                new QName(XHTML_NS, "table"),
                new ElementMappingContentHandler.TargetElement(TEIConstants.TEI_NS, "table"));
		// repeating of rows is ignored; for columns, see below!
		MAPPINGS.put(
                new QName(XHTML_NS, "tr"),
                new ElementMappingContentHandler.TargetElement(TEIConstants.TEI_NS, "row"));
		MAPPINGS.put(
                new QName(XHTML_NS, "td"),
                new ElementMappingContentHandler.TargetElement(TEIConstants.TEI_NS, "cell"));

		final HashMap<QName, QName> teiAttsMapping = new HashMap<QName, QName>();
		teiAttsMapping.put(new QName(XMLConstants.DEFAULT_NS_PREFIX, XMLConstants.XMLNS_ATTRIBUTE),
				new QName(XMLConstants.DEFAULT_NS_PREFIX, XMLConstants.XMLNS_ATTRIBUTE));
		teiAttsMapping.put(new QName(XMLConstants.DEFAULT_NS_PREFIX, "xmlns:xsi"),
				new QName(XMLConstants.DEFAULT_NS_PREFIX, "xmlns:xsi"));
		teiAttsMapping.put(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation", "xsi"),
				new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation", "xsi"));
		MAPPINGS.put(
				new QName(TEIConstants.TEI_NS, "TEI"),
				new ElementMappingContentHandler.TargetElement(TEIConstants.TEI_NS, "TEI", teiAttsMapping));
		MAPPINGS.put(
				new QName(TEIConstants.TEI_NS, "teiHeader"),
				new ElementMappingContentHandler.TargetElement(TEIConstants.TEI_NS, "teiHeader"));
		MAPPINGS.put(
				new QName(TEIConstants.TEI_NS, "fileDesc"),
				new ElementMappingContentHandler.TargetElement(TEIConstants.TEI_NS, "fileDesc"));
		MAPPINGS.put(
				new QName(TEIConstants.TEI_NS, "titleStmt"),
				new ElementMappingContentHandler.TargetElement(TEIConstants.TEI_NS, "titleStmt"));
		MAPPINGS.put(
				new QName(XHTML_NS, "title"),
				new ElementMappingContentHandler.TargetElement(TEIConstants.TEI_NS, "title"));
		MAPPINGS.put(
				new QName(TEIConstants.TEI_NS, "author"),
				new ElementMappingContentHandler.TargetElement(TEIConstants.TEI_NS, "author"));
		MAPPINGS.put(
				new QName(TEIConstants.TEI_NS, "editionStmt"),
				new ElementMappingContentHandler.TargetElement(TEIConstants.TEI_NS, "editionStmt"));

		final HashMap<QName, QName> editionAttsMapping = new HashMap<QName, QName>();
		editionAttsMapping.put(new QName(TEIConstants.TEI_NS, "n"), new QName(TEIConstants.TEI_NS, "n"));
		MAPPINGS.put(
				new QName(TEIConstants.TEI_NS, "edition"),
				new ElementMappingContentHandler.TargetElement(TEIConstants.TEI_NS, "edition", editionAttsMapping));
		MAPPINGS.put(
				new QName(TEIConstants.TEI_NS, "date"),
				new ElementMappingContentHandler.TargetElement(TEIConstants.TEI_NS, "date"));
		MAPPINGS.put(
				new QName(TEIConstants.TEI_NS, "publicationStmt"),
				new ElementMappingContentHandler.TargetElement(TEIConstants.TEI_NS, "publicationStmt"));
		MAPPINGS.put(
				new QName(TEIConstants.TEI_NS, "publisher"),
				new ElementMappingContentHandler.TargetElement(TEIConstants.TEI_NS, "publisher"));
		MAPPINGS.put(
				new QName(TEIConstants.TEI_NS, "sourceDesc"),
				new ElementMappingContentHandler.TargetElement(TEIConstants.TEI_NS, "sourceDesc"));
		MAPPINGS.put(
				new QName(TEIConstants.TEI_NS, "bibl"),
				new ElementMappingContentHandler.TargetElement(TEIConstants.TEI_NS, "bibl"));
		MAPPINGS.put(
				new QName(TEIConstants.TEI_NS, "text"),
				new ElementMappingContentHandler.TargetElement(TEIConstants.TEI_NS, "text"));
		MAPPINGS.put(
				new QName(TEIConstants.TEI_NS, "front"),
				new ElementMappingContentHandler.TargetElement(TEIConstants.TEI_NS, "front"));
		MAPPINGS.put(
				new QName(TEIConstants.TEI_NS, "back"),
				new ElementMappingContentHandler.TargetElement(TEIConstants.TEI_NS, "back"));
    }

	private static final Attributes EMPTY_ATTRIBUTES = new AttributesImpl();

	private TEIHeader header;
	private TreePath currentPath;


	public TEIContentHandler(ContentHandler handler, Metadata meta) {
		super(new ElementMappingContentHandler(handler, MAPPINGS));
		this.header = new TEIHeader();
		final String resourceName = meta.get(Metadata.RESOURCE_NAME_KEY);
		if (resourceName != null) this.header.setResourceName(resourceName);
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		this.currentPath = new TreePath(TreeNode.root, null);

		// using startPrefixMapping, it is easy to set a prefix to each TEI schema-element
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
				"schemaLocation",
				"schemaLocation",
				"CDATA",
				TEIConstants.TEI_NS + " http://www.tei-c.org/release/xml/tei/custom/schema/xsd/teilite.xsd");
		super.startElement(TEIConstants.TEI_NS, "TEI", "TEI", att);
	}

	@Override
	public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
		final TreeNode elementNode = TreeNode.getNodeTypeByName(name);
		if (elementNode != TreeNode.unknown) currentPath = new TreePath(elementNode, currentPath);

		// in head, collect meta information in header object
		if (currentPath.match(TreeNode.head)) {
			if (currentPath.node == TreeNode.meta) {
				String attName = atts.getValue("name");
				String attValue = atts.getValue("content");
				if ("author".equalsIgnoreCase(attName)) {
					header.setAuthor(attValue);
				} else if ("revision-number".equalsIgnoreCase(attName)) {
					header.setRevision(attValue);
				} else if ("last-save-date".equalsIgnoreCase(attName)) {
					header.setLastModificationDate(attValue);
				} else if ("creation-date".equalsIgnoreCase(attName)) {
					header.setCreationDate(attValue);
				} else if ("company".equalsIgnoreCase(attName)) {
					header.setCompany(attValue);
				}
			}
		// p tags should not be output in cells
		} else if (currentPath.match(TreeNode.td)) {
			if (currentPath.node != TreeNode.p) super.startElement(uri, localName, name, atts);
		} else {
			switch (elementNode) {
				// title headings could not contains p tags
				case h1:
				case h2:
				case h3:
				case h4:
				case h5:
				case h6:
				case h7:
				case h8:
				case h9:
					super.startElement(XHTML_NS, "p", "p", EMPTY_ATTRIBUTES);
					break;
				case p:
					if (!currentPath.match(TreeNode.h1, TreeNode.h2, TreeNode.h3, TreeNode.h4, TreeNode.h5,
						TreeNode.h6, TreeNode.h7, TreeNode.h8, TreeNode.h9)) {
						super.startElement(uri, localName, name, atts);
					}
					break;
				default:
					super.startElement(uri, localName, name, atts);
					break;
			}
		}
	}


	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		String chars = new String(ch, start, length);
		// title should be stored in header object until lazyEndHeader is called
		if (currentPath.match(TreeNode.title)) {
			header.setTitle(chars);
		// no characters run should be output at the body level, so embed it in paragraph tags
		} else if (currentPath.node == TreeNode.body ||
				(currentPath.node == TreeNode.unknown && currentPath.match(TreeNode.body))){
			super.startElement(XHTML_NS, "p", "p", EMPTY_ATTRIBUTES);
			emphasize(chars);
			super.endElement(XHTML_NS, "p", "p");
		} else {
			emphasize(chars);
		}
	}

	/**
	 *	Embbed in a emph tag if required, i.e if in a title heading tag
	 * @param charSequence
	 * @throws SAXException
	 */
	private void emphasize(final String charSequence) throws SAXException {
		final String chars = StringUtils.escapeXML(charSequence);
		if (currentPath.match(TreeNode.h1, TreeNode.h2, TreeNode.h3, TreeNode.h4, TreeNode.h5,
						TreeNode.h6, TreeNode.h7, TreeNode.h8, TreeNode.h9)) {
			super.startElement(XHTML_NS, "em", "em", EMPTY_ATTRIBUTES);
			super.characters(chars.toCharArray(), 0, chars.length());
			super.endElement(XHTML_NS, "em", "em");
		} else {
			super.characters(chars.toCharArray(), 0, chars.length());
		}
	}

	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		TreeNode elementNode = TreeNode.getNodeTypeByName(name);

		switch (elementNode) {
			case head:
				lazyEndHeader();
				super.endElement(uri, localName, name);
				currentPath = currentPath.parent;
				break;
			case title:
				currentPath = currentPath.parent;
				break;
			case td:
				currentPath = currentPath.parent;
				super.endElement(uri, localName, name);
				break;
			case p:
				if (currentPath.match(TreeNode.td)
						|| currentPath.match(TreeNode.h1, TreeNode.h2, TreeNode.h3, TreeNode.h4, TreeNode.h5,
						TreeNode.h6, TreeNode.h7, TreeNode.h8, TreeNode.h9)) {
					super.startElement(XHTML_NS, "br", "br", EMPTY_ATTRIBUTES);
					super.endElement(XHTML_NS, "br", "br");
					currentPath = currentPath.parent;
				} else {
					currentPath = currentPath.parent;
					super.endElement(uri, localName, name);
				}
				break;
			// title headings could not contains p tags
			case h1:
			case h2:
			case h3:
			case h4:
			case h5:
			case h6:
			case h7:
			case h8:
			case h9:
				currentPath = currentPath.parent;
				super.endElement(XHTML_NS, "p", "p");
				break;
			case body:
			case ul:
			case li:
			case meta:
				currentPath = currentPath.parent;
			default:
				super.endElement(uri, localName, name);
		}
	}

	private void lazyEndHeader() throws SAXException {
		super.startElement(TEIConstants.TEI_NS, "teiHeader", "teiHeader", EMPTY_ATTRIBUTES);
		super.startElement(TEIConstants.TEI_NS, "fileDesc", "fileDesc", EMPTY_ATTRIBUTES);

		// Title statement
		super.startElement(TEIConstants.TEI_NS, "titleStmt", "titleStmt", EMPTY_ATTRIBUTES);
		// title field
		super.startElement(XHTML_NS, "title", "title", EMPTY_ATTRIBUTES);
		if (header.getTitle() != null) {
			characters(header.getTitle().toCharArray(), 0, header.getTitle().length());
		}
		super.endElement(XHTML_NS, "title", "title");
		// author field
		if (header.getAuthor() != null) {
			super.startElement(TEIConstants.TEI_NS, "author", "author", EMPTY_ATTRIBUTES);
			characters(header.getAuthor().toCharArray(), 0, header.getAuthor().length());
			super.endElement(TEIConstants.TEI_NS, "author", "author");
		}
		super.endElement(TEIConstants.TEI_NS, "titleStmt", "titleStmt");

		// Edition statement
		super.startElement(TEIConstants.TEI_NS, "editionStmt", "editionStmt", EMPTY_ATTRIBUTES);
		super.startElement(XHTML_NS, "p", "p", EMPTY_ATTRIBUTES);
		if (header.getRevision() != null) {
			final String editionStr = "Edition rev. "+header.getRevision();
			characters(editionStr.toCharArray(), 0, editionStr.length());
		}
		if (header.getLastModificationDate() != null) {
			if (header.getRevision() != null) {
				final String sep = " - ";
				characters(sep.toCharArray(), 0, sep.length());
			}
			super.startElement(TEIConstants.TEI_NS, "date", "date", EMPTY_ATTRIBUTES);
			characters(header.getLastModificationDate().toCharArray(), 0, header.getLastModificationDate().length());
			super.endElement(TEIConstants.TEI_NS, "date", "date");

		}
		super.endElement(XHTML_NS, "p", "p");
		super.endElement(TEIConstants.TEI_NS, "editionStmt", "editionStmt");

		// Publication Statement
		super.startElement(TEIConstants.TEI_NS, "publicationStmt", "publicationStmt", EMPTY_ATTRIBUTES);
		super.startElement(TEIConstants.TEI_NS, "publisher", "publisher", EMPTY_ATTRIBUTES);
		if (header.getCompany() != null) {
			characters(header.getCompany().toCharArray(), 0, header.getCompany().length());
		}
		super.endElement(TEIConstants.TEI_NS, "publisher", "publisher");

		if (header.getCreationDate() != null) {
			super.startElement(TEIConstants.TEI_NS, "date", "date", EMPTY_ATTRIBUTES);
			characters(header.getCreationDate().toCharArray(), 0, header.getCreationDate().length());
			super.endElement(TEIConstants.TEI_NS, "date", "date");
		}
		super.endElement(TEIConstants.TEI_NS, "publicationStmt", "publicationStmt");

		// source description statement (mandatory)
		super.startElement(TEIConstants.TEI_NS, "sourceDesc", "sourceDesc", EMPTY_ATTRIBUTES);
		super.startElement(XHTML_NS, "p", "p", EMPTY_ATTRIBUTES);
		if (header.getResourceName() != null) {
			characters(header.getResourceName().toCharArray(), 0, header.getResourceName().length());
		}
		super.endElement(XHTML_NS, "p", "p");
		super.endElement(TEIConstants.TEI_NS, "sourceDesc", "sourceDesc");

		super.endElement(TEIConstants.TEI_NS, "fileDesc", "fileDesc");
		super.endElement(TEIConstants.TEI_NS, "teiHeader", "teiHeader");
		super.startElement(TEIConstants.TEI_NS, "text", "text", EMPTY_ATTRIBUTES);
		super.startElement(TEIConstants.TEI_NS, "front", "front", EMPTY_ATTRIBUTES);
		super.endElement(TEIConstants.TEI_NS, "front", "front");
	}

	@Override
	public void endDocument() throws SAXException {
		super.endElement(TEIConstants.TEI_NS, "body", "body");
		super.startElement(TEIConstants.TEI_NS, "back", "back", EMPTY_ATTRIBUTES);
		super.endElement(TEIConstants.TEI_NS, "back", "back");
		super.endElement(TEIConstants.TEI_NS, "text", "text");
		super.endElement(TEIConstants.TEI_NS, "TEI", "TEI");
		endPrefixMapping(XMLConstants.DEFAULT_NS_PREFIX);
		currentPath = currentPath.parent;
		super.endDocument();
	}

    private static enum TreeNode {
		root, head, body, title, td, p, ul, li, meta, h1, h2, h3, h4, h5, h6, h7, h8, h9,unknown;

		private static TreeNode getNodeTypeByName(final String name) {
			TreeNode elementNode;
			try {
				elementNode = TreeNode.valueOf(name);
			} catch (IllegalArgumentException e) {
				return unknown;
			}
			return elementNode;
		}
	}

	private static class TreePath {
		TreeNode node;
		TreePath parent;

		private TreePath(final TreeNode aNode, final TreePath parent) {
			this.node = aNode;
			this.parent = parent;
		}

		private boolean matchOne(final TreeNode query) {
			return node == query || (parent != null && parent.match(query));
		}

		private boolean match(final TreeNode... queries) {
			boolean match = false;
			for (TreeNode query : queries) {
				if (matchOne(query)) {
					match = true;
					break;
				}
			}
			return match;
		}
	}
}

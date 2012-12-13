package org.gramlab.docparsers.sax;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.apache.tika.sax.WriteOutContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.OutputStream;
import java.io.Writer;

/**
 * User: freddy
 * Date: 18/05/11
 * Time: 17:46
 */

/**
 * Writes to the underlying output stream character content as well as the elements as XML. If the encoding argument is used for construction
 * then it is used for the XML encoding header.
 */
public class FullWriteOutContentHandler extends WriteOutContentHandler {

	private static final String DEFAULT_ENCODING = "utf-8";
	private String encoding = DEFAULT_ENCODING;
	private BidiMap prefixMapping = new DualHashBidiMap();


	public FullWriteOutContentHandler(final Writer writer) {
		super(writer);
	}

	public FullWriteOutContentHandler(final Writer writer, final String encoding) {
		super(writer);
		this.encoding = encoding;
	}

	public FullWriteOutContentHandler(final OutputStream stream) {
		super(stream);
	}

	public FullWriteOutContentHandler(final OutputStream stream, final String encoding) {
		super(stream);
		this.encoding = encoding;
	}

	public FullWriteOutContentHandler(final int writeLimit) {
		super(writeLimit);
	}

	@Override
	public void startDocument() throws SAXException {
		final String encodingHeader = "<?xml version=\"1.0\" encoding=\""+encoding+"\"?>"+System.getProperty("line.separator");
		characters(encodingHeader.toCharArray(), 0, encodingHeader.length());
		super.startDocument();
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
			sb.append(' ').append(attributes.getQName(i)).append("=\'").append(attributes.getValue(i)).append("\'");
		}
		sb.append('>');
		final String element = sb.toString().trim();
		characters(element.toCharArray(), 0, element.length());
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
		characters(element.toCharArray(), 0, element.length());
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		prefixMapping.put(prefix, uri);
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		prefixMapping.remove(prefix);
	}
}

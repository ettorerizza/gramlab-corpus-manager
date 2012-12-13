package org.gramlab.docparsers.util;

import java.text.StringCharacterIterator;
import java.util.HashSet;
import java.util.Set;

/**
 * User: freddy
 * Date: 24/05/11
 * Time: 14:32
 */

/**
 *
 */
public abstract class StringUtils {

	private static final String XML_ENCODING_PREFIX = "&#";
	private static final char XML_ENCODING_SUFFIX = ';';

	private static Set<Character> xmlContentToEscapeSet;
	private static Set<Character> xmlAttributeToEscapeSet;

	static {
		xmlContentToEscapeSet = new HashSet<Character>(3);
		xmlContentToEscapeSet.add('&');
		xmlContentToEscapeSet.add('<');
		xmlContentToEscapeSet.add('>');
	}

	static {
		xmlAttributeToEscapeSet = new HashSet<Character>(4);
		xmlAttributeToEscapeSet.add('\n');
		xmlAttributeToEscapeSet.add('\t');
		xmlAttributeToEscapeSet.add('\r');
		xmlAttributeToEscapeSet.add('"');
	}

	/**
	 * Encodes an input string (not null) to XML format.<br/>
	 * Convert the XML reserved characters ('<', '>' and '&') to their corresponding code.
	 *
	 * @param anInputString string to escape
	 * @return the input string without XML reserved characters.
	 * @throws IllegalArgumentException if the input string is null.
	 */
	public static String escapeXML(final String anInputString) {
		if (anInputString == null) return null;
		final StringCharacterIterator iterator = new StringCharacterIterator(anInputString);
		char character = iterator.current();
		final StringBuilder buffer = new StringBuilder(anInputString.length() << 1);
		while (character != StringCharacterIterator.DONE) {
			buffer.append(escapeXML(character));
			character = iterator.next();
		}
		return buffer.toString();
	}

	/**
	 * Encodes an input character to a valid character.
	 * Convert the XML reserved characters ('<', '>' and '&') to their corresponding code.
	 *
	 * @param cCharacter character to escape to xml
	 * @return the input character  escaped to a valid XML character
	 */
	public static String escapeXML(final char cCharacter) {
		final String encodedText;

		if (xmlContentToEscapeSet.contains(cCharacter))
			encodedText = new StringBuilder(7)
					.append(XML_ENCODING_PREFIX).append((int) cCharacter).append(XML_ENCODING_SUFFIX).toString();
		else
			encodedText = transformToValidXMLChar(cCharacter);
		return encodedText;
	}

	private static String transformToValidXMLChar(final char cCharacter) {
		String result;
		switch (cCharacter) {
			case '\t': // \u0009
			case '\n': // \u000A
			case '\r': // \u000D
				result = String.valueOf(cCharacter);
				break;
			case '\u000B': //LINE TABULATION but replaced by \n by default for normalization
				result = "\n";
				break;
			case '\u0000': //NULL character
			case '\u0001': //START OF HEADING
			case '\u0002': //START OF TEXT
			case '\u0003': //END OF TEXT
			case '\u0004': //END OF TRANSMISSION
			case '\u0005': //ENQUIRY
			case '\u0006': //ACKNOWLEDGE
			case '\u0007': //BELL
			case '\u0008': //BACKSPACE
			case '\u000C': //FORM FEED (FF)
			case '\u000E': //SHIFT OUT
			case '\u000F': //SHIFT IN
			case '\u0010': //DATA LINK ESCAPE
			case '\u0011': //DEVICE CONTROL ONE
			case '\u0012': //DEVICE CONTROL TWO
			case '\u0013': //DEVICE CONTROL THREE
			case '\u0014': //DEVICE CONTROL FOUR
			case '\u0015': //NEGATIVE ACKNOWLEDGE
			case '\u0016': //SYNCHRONOUS IDLE
			case '\u0017': //END OF TRANSMISSION BLOCK
			case '\u0018': //CANCEL
			case '\u0019': //END OF MEDIUM
			case '\u001A': //SUBSTITUTE
			case '\u001B': //ESCAPE
			case '\u001C': //INFORMATION SEPARATOR FOUR
			case '\u001D': //INFORMATION SEPARATOR THREE
			case '\u001E': //INFORMATION SEPARATOR TWO
			case '\u001F': //INFORMATION SEPARATOR ONE
			case '\u007F': //DELETE
				result = "";
				break;
			default:
				if (cCharacter < ' ') {
					result = XML_ENCODING_PREFIX + ((int) cCharacter) + XML_ENCODING_SUFFIX;
				} else {
					result = String.valueOf(cCharacter);
				}
				break;
		}
		return result;
	}

	/**
	 * Encodes an input string (not null) to XML attributes format.<br/>
	 * Call the <tt>encodeXML</tt>( anInputString ) method and convert the following characters :
	 * '\n', '\r', '\t' and '\"' to their corresponding code.
	 *
	 * @param anInputString string to escape
	 * @return the input string without XML reserved characters.
	 * @throws IllegalArgumentException if the input string is null.
	 * @see #escapeXML(String)
	 */
	public static String escapeXMLAttribute(final String anInputString) {
		if (anInputString == null)
			throw new IllegalArgumentException("The input string must be not null");
		final StringCharacterIterator iterator = new StringCharacterIterator(anInputString);
		char character = iterator.current();
		final StringBuilder buffer = new StringBuilder(anInputString.length() << 1);
		while (character != StringCharacterIterator.DONE) {
			buffer.append(escapeXMLAttribute(character));
			character = iterator.next();
		}
		return buffer.toString();
	}

	/**
	 * Encodes an input character (not null) to XML attributes format.<br/>
	 * Call the <tt>encodeXML</tt>( string ) method and convert the following characters :
	 * '\n', '\r', '\t' and '\"' to their corresponding code.
	 *
	 * @param cCharacter character to escape to XML
	 * @return a valid  XML attribute
	 */
	public static String escapeXMLAttribute(final char cCharacter) {
		final String encodedText;
		if (xmlAttributeToEscapeSet.contains(cCharacter) || xmlContentToEscapeSet.contains(cCharacter))
			encodedText = new StringBuilder(7)
					.append(XML_ENCODING_PREFIX).append((int) cCharacter).append(XML_ENCODING_SUFFIX).toString();
		else
			encodedText = transformToValidXMLChar(cCharacter);
		return encodedText;
	}
}

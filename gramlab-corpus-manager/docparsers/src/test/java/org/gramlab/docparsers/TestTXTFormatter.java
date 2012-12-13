package org.gramlab.docparsers;

import org.apache.log4j.Logger;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.txt.TXTParser;
import org.gramlab.docparsers.sax.FullWriteOutContentHandler;
import org.gramlab.docparsers.sax.TEIContentHandler;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * User: freddy
 * Date: 19/05/11
 * Time: 15:37
 */
public class TestTXTFormatter {
	private static final Logger logger = Logger.getLogger(TestTXTFormatter.class);

	private static final File[] SAMPLES = new File[] {
			new File("src/test/resources/samples/samples01.txt")
	};

	@Test
	public void testFormat() throws SAXException, TikaException {
		TXTParser parser = new TXTParser();
		for (File sample : SAMPLES) {
			InputStream tis = null;
			try {
				tis = TikaInputStream.get(sample);
				Metadata meta = new Metadata();
				meta.add(Metadata.RESOURCE_NAME_KEY, sample.getName());
				ParseContext context = new ParseContext();
				parser.parse(tis, new TEIContentHandler(new FullWriteOutContentHandler(System.out), meta), meta, context);
			} catch (TikaException e) {
				logger.error(e);
				throw e;
			} catch (IOException e) {
				logger.error(e);
			} finally {
				if (tis != null) {
					try {
						tis.close();
					} catch (IOException e) {/*do nothing*/}
				}
			}

		}
	}
}

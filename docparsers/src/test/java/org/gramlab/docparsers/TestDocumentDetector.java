package org.gramlab.docparsers;

import org.apache.log4j.Logger;
import org.apache.tika.detect.ContainerAwareDetector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypes;
import org.junit.Test;
import static org.junit.Assert.assertNotSame;

import java.io.*;
import java.text.MessageFormat;

/**
 * User: freddy
 * Date: 13/05/11
 * Time: 16:20
 */
public class TestDocumentDetector {

	private static final Logger logger = Logger.getLogger(TestDocumentDetector.class);

	private static final File SAMPLES_DIR = new File("src/test/resources/samples");

	@Test
	public void testDetection() throws IOException {
		File[] files = SAMPLES_DIR.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return !pathname.isDirectory();
			}
		});
		ContainerAwareDetector detector = new ContainerAwareDetector(new ContainerAwareDetector(MimeTypes.getDefaultMimeTypes()));
		for (File file : files) {
			InputStream tis = null;
			try {
				tis = TikaInputStream.get(new FileInputStream(file));
				Metadata meta = new Metadata();
				meta.add(Metadata.RESOURCE_NAME_KEY, file.getName());
				MediaType type = detector.detect(tis, meta);
				assertNotSame(MediaType.OCTET_STREAM, type);
				logger.info(MessageFormat.format("File {0} : {1}", file.getName(), type));
			} catch (IOException e) {
				logger.error(e);
				throw e;
			} finally {
				if (tis != null) {
					try {
						tis.close();
					} catch (IOException e) {/* do nothing */}
				}
			}

		}

	}
}

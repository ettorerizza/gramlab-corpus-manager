package org.gramlab.docparsers;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * User: freddy
 * Date: 29/03/12
 * Time: 15:26
 */
public class TestEntryPoint {
    private static final Logger logger = Logger.getLogger(TestDocumentDetector.class);

	private static final File SAMPLES_DIR = new File("src/test/resources/samples");
	private static final File OUTPUT_DIR = new File("target");

    @Before
    public void setUp() {
        if (!OUTPUT_DIR.exists()) OUTPUT_DIR.mkdirs();
    }

	@Test
	public void testDetection() throws IOException {
        final String[] params = {
                "-i",
                SAMPLES_DIR.getAbsolutePath(),
                "-o",
                OUTPUT_DIR.getAbsolutePath()
        };
        org.gramlab.docparsers.cli.EntryPoint.main(params);
	}
}

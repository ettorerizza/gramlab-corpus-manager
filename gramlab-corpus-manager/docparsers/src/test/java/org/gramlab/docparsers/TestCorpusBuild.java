package org.gramlab.docparsers;

import org.apache.log4j.Logger;
import org.gramlab.docparsers.sax.CorpusBuildContentHandler;
import org.gramlab.docparsers.sax.TEIHeader;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.*;

import static org.junit.Assert.assertNotSame;

/**
 */
public class TestCorpusBuild {
    private static final Logger logger = Logger.getLogger(TestCorpusBuild.class);

    private static final File SAMPLES_DIR = new File("src/test/resources/tei");

    @Test
    public void testCorpusBuild() throws SAXException, IOException {
        InputStream is = null;
        File[] files = SAMPLES_DIR.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return !pathname.isDirectory();
            }
        });

        Writer fosw = null;
        try {
            fosw = new OutputStreamWriter(new FileOutputStream(new File("target/corpus.xml")), "utf-8");
            TEIHeader header = new TEIHeader();
            header.setAuthor("foo");
            header.setTitle("test TEI corpus title");
            CorpusBuildContentHandler handler = new CorpusBuildContentHandler(fosw, header);
            handler.startCorpus();
            for (File file : files) {
                // multiple inputStream parsing
                is = new FileInputStream(file);
                XMLReader myReader = XMLReaderFactory.createXMLReader();
                myReader.setContentHandler(handler);
                myReader.parse(new InputSource(new InputStreamReader(is, "utf-8")));
            }
            handler.closeCorpus();
        } catch (IOException e) {
            logger.error(e);
            throw e;
        } finally {
            if (fosw != null) {
                try {
                    fosw.close();
                } catch (IOException e) {/* Nothing to do */}
            }

        }
    }

}

package org.gramlab.docparsers.cli;

import org.apache.log4j.Logger;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.gramlab.docparsers.sax.FullWriteOutContentHandler;
import org.gramlab.docparsers.sax.TEIContentHandler;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.xml.sax.SAXException;

import java.io.*;
import java.text.MessageFormat;

/**
 * User: freddy
 * Date: 25/05/11
 * Time: 16:19
 */

/**
 * A very simple command line utility for converting documents to TEI LITE format.
 */
public class EntryPoint {
	private static final Logger theLogger = Logger.getLogger(EntryPoint.class);

	public static void main(String[] args) {
		final CommandLineOptions options = new CommandLineOptions();
		final CmdLineParser cmdLineParser = new CmdLineParser(options);
		try {
			cmdLineParser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("java -cp gramlab-docparsers.jar:... " + EntryPoint.class.getName() + " [options...] arguments...");
			cmdLineParser.printUsage(System.err);
			return;
		}

		try {
			EntryPoint cli = new EntryPoint();
			cli.processFile(new File(options.input), new File(options.outputDir));
		} catch (Exception e) {
			theLogger.error("IO error occurred. " + e.getMessage());
		}
	}

	public void processFile(final File anInputFile, final File anOutputDir) throws IOException, SAXException, TikaException {
		if( anInputFile.isDirectory() ) {
			processDirectory( anInputFile, anOutputDir, anInputFile );
		} else {
			processSingleFile( anInputFile, anOutputDir, "");
		}
	}

	private void processSingleFile(final File anInputFile, final File anOutputDir, final String aRelativePath) throws IOException, SAXException, TikaException {
		long start = System.currentTimeMillis();
		theLogger.info("--------------------------");
		theLogger.info("Processing file "+anInputFile.getAbsolutePath());
		AutoDetectParser parser = new AutoDetectParser();

		InputStream tis = TikaInputStream.get(anInputFile);
		Metadata meta = new Metadata();
		meta.add(Metadata.RESOURCE_NAME_KEY, anInputFile.getName());
		ParseContext context = new ParseContext();

        File myCanonicalOutputDir = new File(anOutputDir.getCanonicalPath()+aRelativePath);
        if (!myCanonicalOutputDir.exists()) myCanonicalOutputDir.mkdirs();
        File anOutputFile = new File( myCanonicalOutputDir, anInputFile.getName()+".xml" );
		BufferedWriter bWriter = new BufferedWriter( new OutputStreamWriter( new FileOutputStream(anOutputFile), "utf-8" ) );
		parser.parse(tis, new TEIContentHandler(new FullWriteOutContentHandler(bWriter), meta), meta, context);
		bWriter.close();
		theLogger.info("Done in "+(System.currentTimeMillis()-start)+" ms.");
		theLogger.info("--------------------------");
	}

	private void processDirectory(File anInputDir, File aBaseOutputDir, File aBaseInputDir) throws IOException {
		long start = System.currentTimeMillis();
        if (".svn".equals(anInputDir.getName())) {
            theLogger.warn(MessageFormat.format("Ignoring {0} directory.", anInputDir.getAbsolutePath()));
        } else {
            theLogger.info("Processing directory " + anInputDir.getAbsolutePath());
            File[] dirFiles = anInputDir.listFiles();
            for (File myFile : dirFiles) {
                if (myFile.isDirectory()) {
                    try {
                        processDirectory(myFile, aBaseOutputDir, aBaseInputDir);
                    } catch (IOException e) {
                        theLogger.error("Process failed locally.", e);
                    }
                } else {
                    try {
                        processSingleFile(myFile, aBaseOutputDir, getRelativePath(aBaseInputDir, myFile.getParentFile()));
                    } catch (Exception e) {
                        theLogger.error("Process failed locally.", e);
                    }
                }
            }
        }
		theLogger.info("Directory processing done in "+(System.currentTimeMillis()-start)+" ms.");
	}

	/**
	 * Returns wether the first argument is an ancestor directory of the second argument.
	 * Both files must exist.
	 *
	 * @param aWorkingDir
	 * @param aFile
	 * @return true if anAncestorCandidate is an ancestor of aDescendentCandidate. false if any of the two files
	 *         does not exist.
	 */
	public static boolean isAncestor(final File aWorkingDir, final File aFile) {
		if (aWorkingDir == null || aFile == null) return false;
		if (!aFile.exists() || !aWorkingDir.isDirectory()) return false;
		if (aFile.isDirectory()) {
			if (aFile.equals(aWorkingDir)) return true;
		}
		File parent = aFile.getParentFile();
		while (parent != null) {
			if (parent.equals(aWorkingDir)) return true;
			parent = parent.getParentFile();
		}
		return false;
	}

	public static String getRelativePath(final File aRootFolder, final File aDescendent) {
		if (!isAncestor(aRootFolder, aDescendent)) return null;
		final String rootPath = aRootFolder.getAbsolutePath();
		final String descendentPath = aDescendent.getAbsolutePath();
		return getRelativePath(rootPath, descendentPath);
	}

	public static String getRelativePath(final String aRootFolderPath, final String aDescendentPath) {
		final String relativePath;
		if (!aDescendentPath.startsWith(aRootFolderPath))
			relativePath = null;
		else
			relativePath = aDescendentPath.substring(aRootFolderPath.length());
		return relativePath;
	}

	private static class CommandLineOptions {
		@Option(name = "--in", aliases = "-i", usage = "Input file or directory (required)", required = true)
		private String input;

		@Option(name = "--out", aliases = "-o", usage = "Output directory (required)", required = true)
		private String outputDir;
	}
}

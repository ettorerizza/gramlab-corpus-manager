package servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.gramlab.corpus.Corpus;

// TODO: Auto-generated Javadoc
/**
 * The Class UrlUploadServlet get website urls providing by the user and crawl
 * them. Crawling urls are indexed in GRAMLAB solr instance, saved in local
 * directory and in a TEI format.
 *
 * @author Aurelien Saint Requier
 */
public class UrlUploadServlet extends HttpServlet {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant NUTCH_HOME. */
	private static final String NUTCH_HOME = "nutchHome";

	/** The nutch home. */
	private String nutchHome;

	/** The corpus. */
	private Corpus corpus = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		corpus = (Corpus) getServletContext().getAttribute("corpus");
		if (corpus == null) {
			System.out.println(" Corpus pas trouv\u00E9 dans le context on va recreer");
			corpus = new Corpus(getServletContext().getRealPath("/"));
		} else {
			System.out.println(" Corpus trouv\u00E9 dans le context ");
		}

		nutchHome = config.getInitParameter(NUTCH_HOME);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/html");
		log("doPost()");

		Enumeration<?> input_names = request.getParameterNames();

		log("UrlUploadServlet - doPost()" + input_names.toString());

		/**
		 * Get website urls
		 */
		ArrayList<String> url_to_crawl = new ArrayList<String>();
		while (input_names.hasMoreElements()) {
			String input_name = (String) input_names.nextElement();
			String input_value = request.getParameter(input_name);
			log("url to crawl " + input_value);
			url_to_crawl.add(input_value);
		}

		/**
		 * Crawl website urls
		 */
		if (!url_to_crawl.isEmpty()) {
			String urls_filePath = nutchHome + "urls/seed" + System.nanoTime() + ".txt";
			PrintWriter url_file = new PrintWriter(new FileWriter(urls_filePath));
			for (String s : url_to_crawl) {
				url_file.println(s);
			}
			url_file.close();
			/**
			 * Create a temp crawl directory
			 */
			String crawl_directory = "gramlabcrawl" + System.nanoTime();
			/**
			 * Create crawl system command
			 */
			String[] nutch_crawl = { "bin/nutch", "crawl", urls_filePath, "-dir", crawl_directory, "-depth", "2",
					"-topN", "10000" };
			Runtime runtime = Runtime.getRuntime();
			/**
			 * Lauch nutch crawl
			 */
			log("Launch nutch crawl");
			Process pcrawl = runtime.exec(nutch_crawl, null, new File(nutchHome));
			BufferedReader crawlReader = new BufferedReader(new InputStreamReader(pcrawl.getInputStream()));
			String line = "";
			final List<String> urls = new ArrayList<String>();
			try {
				while ((line = crawlReader.readLine()) != null) {
					log(line);
					if (line.contains("fetching")) {
						String[] lines = line.split(" ");
						urls.add(lines[1]);
					}
				}
			} finally {
				crawlReader.close();
			}
			log("Crawl end");
			/**
			 * Add crawled urls to Gramlab corpus
			 */
			for (String s : urls) {
				try {
					/**
					 *Create MD5 filename from url
					 */
					MessageDigest md = MessageDigest.getInstance("MD5");
					URL url = new URL(s);
					md.update(s.getBytes(), 0, s.length());
					String fileName = new BigInteger(1, md.digest()).toString(16);
					/**
					 * Get url content
					 */
					URLConnection conn = url.openConnection();
					InputStream is = conn.getInputStream();
					/**
					 * Add content url to the Gramlab corpus
					 */
					corpus.addFile(fileName, is);
				} catch (Exception e) {
					log("ERROR - CANNOT PROCESS URL :" + s);
				}
			}
			/**
			 * Delete temp crawl directory
			 */
			FileUtils.deleteDirectory(new File(nutchHome + crawl_directory));
		}
		response.sendRedirect("index.html");

	}

}

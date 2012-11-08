package org.gramlab.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class SimplePostTool
{
  public static final String DEFAULT_POST_URL = "http://localhost:8180/solr330/gramlab/update";
  public static final String DEFAULT_CORE = "gramlab";
  public static final String POST_ENCODING = "UTF-8";
  public static final String VERSION_OF_THIS_TOOL = "1.2";
  private static final String SOLR_OK_RESPONSE_EXCERPT = "<int name=\"status\">0</int>";
  private static final String DEFAULT_COMMIT = "yes";
  private static final String DATA_MODE_FILES = "files";
  private static final String DATA_MODE_ARGS = "args";
  private static final String DATA_MODE_STDIN = "stdin";
  private static final String DEFAULT_DATA_MODE = "files";
  private static final Set<String> DATA_MODES = new HashSet();
  protected URL solrUrl;



  int postFiles(String[] args, int startIndexInArgs) throws IOException
  {
    int filesPosted = 0;
    for (int j = startIndexInArgs; j < args.length; j++) {
      File srcFile = new File(args[j]);
      StringWriter sw = new StringWriter();

      if (srcFile.canRead()) {
        info("POSTing file " + srcFile.getName());
        postFile(srcFile, sw);
        filesPosted++;
        warnIfNotExpectedResponse(sw.toString(), "<int name=\"status\">0</int>");
      } else {
        warn("Cannot read input file: " + srcFile);
      }
    }
    return filesPosted;
  }

  static void warnIfNotExpectedResponse(String actual, String expected)
  {
    if (actual.indexOf(expected) < 0)
      warn("Unexpected response from Solr: '" + actual + "' does not contain '" + expected + "'");
  }

  static void warn(String msg)
  {
    System.err.println("SimplePostTool: WARNING: " + msg);
  }

  static void info(String msg) {
    System.out.println("SimplePostTool: " + msg);
  }

  static void fatal(String msg) {
    System.err.println("SimplePostTool: FATAL: " + msg);
    System.exit(1);
  }

  public SimplePostTool(URL solrUrl)
  {
    this.solrUrl = solrUrl;
    warn("Make sure your XML documents are encoded in UTF-8, other encodings are not currently supported");
  }

  public void commit(Writer output)
    throws IOException
  {
    postData(new StringReader("<commit/>"), output);
  }

  public void postFile(File file, Writer output) throws FileNotFoundException, UnsupportedEncodingException{
    Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
    try {
      postData(reader, output);
    } finally {
      try {
        if (reader != null) reader.close(); 
      }
      catch (IOException e) {
        throw new PostException("IOException while closing file", e);
      }
    }
  }

  public void postData(Reader data, Writer output) 
  {
    HttpURLConnection urlc = null;
    try {
      
    	urlc = (HttpURLConnection)this.solrUrl.openConnection();
    	urlc.setConnectTimeout(1000); 
    	
      try {
        urlc.setRequestMethod("POST");
      } catch (ProtocolException e) {
        throw new PostException("Shouldn't happen: HttpURLConnection doesn't support POST??", e);
      }
      urlc.setDoOutput(true);
      urlc.setDoInput(true);
      urlc.setUseCaches(false);
      urlc.setAllowUserInteraction(false);
      urlc.setRequestProperty("Content-type", "text/xml; charset=UTF-8");

      OutputStream out = urlc.getOutputStream();
      try
      {
        Writer writer = new OutputStreamWriter(out, "UTF-8");
        pipe(data, writer);
        writer.close();
      } catch (IOException e) {
        throw new PostException("IOException while posting data", e);
      } finally {
        if (out != null) out.close();
      }

      InputStream in = urlc.getInputStream();
      try {
        Object reader = new InputStreamReader(in);
        pipe((Reader)reader, output);
        ((Reader)reader).close();
      } catch (IOException e) {
        throw new PostException("IOException while reading response", e);
      } finally {
        if (in != null) in.close(); 
      }
    }
    catch (IOException e)
    {
      try {
        fatal("Solr returned an error: " + urlc.getResponseMessage()); } catch (IOException f) {
      }
      fatal("Connection error (is Solr running at " + this.solrUrl + " ?): " + e);
    } finally {
      if (urlc != null) urlc.disconnect();
    }
  }

  private static void pipe(Reader reader, Writer writer)
    throws IOException
  {
    char[] buf = new char[1024];
    int read = 0;
    while ((read = reader.read(buf)) >= 0) {
      writer.write(buf, 0, read);
    }
    writer.flush();
  }

  static
  {
    DATA_MODES.add("files");
    DATA_MODES.add("args");
    DATA_MODES.add("stdin");
  }

  private class PostException extends RuntimeException
  {
    PostException(String reason, Throwable cause)
    {
      super(cause);
    }
  }
  
  public static void main(String[] args){
	  URL u = null;
	  try {
	      u = new URL("http://localhost:8180/solr330/generic/update");
	    } catch (MalformedURLException e) {
	      fatal("System Property 'url' is not a valid URL: " + u);
	    }
	    SimplePostTool t = new SimplePostTool(u);
	    StringWriter sw = new StringWriter();
	    try{
	    t.postFile(new File("C:\\clients\\gramlab\\samples\\samples01.xml"), sw);
	    warnIfNotExpectedResponse(sw.toString(), "<int name=\"status\">0</int>");
	    if ("yes".equals(System.getProperty("commit", "yes"))) {
	        info("COMMITting Solr index changes..");
	        StringWriter sw2 = new StringWriter();
	        t.commit(sw2);
	        warnIfNotExpectedResponse(sw2.toString(), "<int name=\"status\">0</int>");
	      }
	    }catch(Exception e){
	    	System.out.println("ERROR: "+e.getMessage());
	    	e.printStackTrace();
	    }
	    System.out.println("END sw "+sw.toString());
	    System.out.println("Finished");
  }
  
}

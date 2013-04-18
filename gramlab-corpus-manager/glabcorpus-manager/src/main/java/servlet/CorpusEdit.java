package servlet;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.gramlab.corpus.Corpus;
import org.gramlab.docparsers.sax.CorpusBuildContentHandler;
import org.gramlab.docparsers.sax.TEIHeader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


public class CorpusEdit extends HttpServlet{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(CorpusEdit.class);
	private Corpus corpus = null;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		corpus  = (Corpus)getServletContext().getAttribute("corpus");
		if(corpus==null){
			System.out.println(" Corpus pas trouv\u00E9 dans le context on va recreer");
			corpus = new Corpus(getServletContext().getRealPath("/"));
		}else{
			System.out.println(" Corpus trouv\u00E9 dans le context ");
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//request.getParameter("delete");
		String title = "Reading Three Request Parameters";

		String ids = request.getParameter("delete");
		System.out.println("Delete files  "+request.getParameter("delete"));
		List<String> idList = new ArrayList<String>();
		if(ids!=null){
			idList = Arrays.asList(ids.split(";"));
			System.out.println("Delete detecte et corpus "+corpus);
			corpus.removeEntry(idList);
		}

		if(request.getParameter("tei_show")!=null){
			System.out.println("Show TEI file  "+request.getParameter("tei_show"));
			PrintWriter out = response.getWriter();
			response.setContentType("text/xml");
			response.setCharacterEncoding("utf-8");
			response.addHeader("Content-Type" , "text/xml;charset=utf-8");
			String name = request.getParameter("tei_show")+".xml";
			File show = new File(corpus.teiDir,FilenameUtils.removeExtension(request.getParameter("tei_show"))+".xml");
			System.out.println("Constructed TEI file  "+show.getAbsolutePath());
			if(show.exists()){
				BufferedInputStream in = new BufferedInputStream(new FileInputStream(show) );
				int ch;
				while ((ch = in.read()) !=-1) {
					out.print((char)ch);
				}
				String s;
			}else{
				File essai = new File(corpus.teiDir,name);
				System.out.println("Constructed second TEI file  "+show.getAbsolutePath());
				if(essai.exists()){
					BufferedInputStream in = new BufferedInputStream(new FileInputStream(essai) );
					int ch;
					while ((ch = in.read()) !=-1) {
						out.print((char)ch);
					}
				}else{
					out.print("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
					out.print("<Error>File "+ show.getAbsolutePath()+" not found</Error>");
				}
			}
		}

		if(request.getParameter("file_get")!=null){
			System.out.println("File get  "+request.getParameter("file_get"));
			String files = request.getParameter("file_get");
			if(files.contains(";")){
				String[] file = files.split(";");
				File arch = new File(corpus.archiveZipDir,"archive_"+System.currentTimeMillis()+".zip");
				FileOutputStream fout = new FileOutputStream(arch);
				ZipOutputStream zout = new ZipOutputStream(fout);
				byte[] buffer = new byte[1024];
				for(String curr:file){
					File curr_file = new File(corpus.pjDir,curr);
					if (curr_file.exists()) {
						FileInputStream fin = new FileInputStream(curr_file);
						zout.putNextEntry(new ZipEntry(curr_file.getName()));
						int length;
						while ((length = fin.read(buffer)) > 0) {
							zout.write(buffer, 0, length);
						}
						zout.closeEntry();
						fin.close();
					} else {
						log.error(" File "+curr_file.getAbsolutePath()+" not found ignoring got tei corpus extract");
					}
				}
				zout.close();
				sendFile(arch, response);
			}else{
				File get = new File(corpus.pjDir,files);
				sendFile(get, response);
			}
		}

		if(request.getParameter("tei_get")!=null){
			System.out.println("Tei Get  "+request.getParameter("tei_get"));
			String files = request.getParameter("tei_get");
			if(files.contains(";")){
				String titles = request.getParameter("titles");
				String authors = request.getParameter("authors");

				String[] file = files.split(";");
				//File arch = new File(corpus.archiveZipDir,"archive_"+System.currentTimeMillis()+".zip");
				File arch = new File(corpus.archiveZipDir,"tei_corpus_"+System.currentTimeMillis()+".xml");
				//FileOutputStream fout = new FileOutputStream(arch);
				Writer fosw = new OutputStreamWriter(new FileOutputStream(new File(corpus.archiveZipDir,"tei_corpus_"+System.currentTimeMillis()+".xml")), "utf-8");
				//ZipOutputStream zout = new ZipOutputStream(fout);
				byte[] buffer = new byte[1024];
				TEIHeader header = new TEIHeader();
				if(request.getParameter("titles")!=null){
					header.setTitle(request.getParameter("titles"));
				}else{
					header.setTitle("");
				}
				if(request.getParameter("authors")!=null){
					header.setAuthor(request.getParameter("authors"));
				}else{
					header.setAuthor("");
				}
				StringBuilder sb = new StringBuilder();
				int titleCount = 3;
				CorpusBuildContentHandler handler = new CorpusBuildContentHandler(fosw, header);
				try {
					handler.startCorpus();
					for (String curr : file) {
						File curr_file = new File(corpus.teiDir, FilenameUtils.removeExtension(curr)+".xml");
						if(!curr_file.exists()){
							curr_file = new File(corpus.teiDir, curr+".xml");
						}
						if (curr_file.exists()) {
							try {
								//InputStream is = new InputStreamReader(new FileInputStream(curr_file),"utf-8");
								InputStreamReader isr = new InputStreamReader(new FileInputStream(curr_file),"utf-8");
								XMLReader myReader = XMLReaderFactory.createXMLReader();
								myReader.setContentHandler(handler);
								myReader.parse(new InputSource(isr));
							} catch (SAXException e) {
								log.error(e.getMessage());
								e.printStackTrace();
							}
						}else{
							log.error(" File "+curr_file.getAbsolutePath()+" not found ignoring got tei corpus extract");
						}
					}

					handler.closeCorpus();
				} catch (SAXException e) {
					log.error(e.getMessage());
					e.printStackTrace();
				}
				// zout.close();
				sendFile(arch, response);
			}else{
				File get = new File(corpus.teiDir,FilenameUtils.removeExtension(files)+".xml");
				if (get.exists()) {
					sendFile(get, response);
				}else{
					get = new File(corpus.teiDir,files+".xml");
					if(get.exists()){
						sendFile(get, response);
					}else{
						log.error(" File "+get.getAbsolutePath()+" not found ignoring got tei corpus extract");
					}

				}
			}
		}
	}

	public void sendFile(File get,HttpServletResponse response)throws ServletException, IOException{
		ServletOutputStream op       = response.getOutputStream();
		response.setContentType("application/octet-stream" );
		response.setContentLength( (int)get.length() );
		response.setHeader( "Content-Disposition", "attachment; filename=\"" + get.getName() + "\"" );
		//
		//  Stream to the requester.
		//
		int length=0;
		byte[] bbuf = new byte[1024];
		DataInputStream in = new DataInputStream(new FileInputStream(get));
		while (in != null && (length = in.read(bbuf)) != -1)
		{
			op.write(bbuf,0,length);
		}
		in.close();
		op.flush();
		op.close();
	}

	@Override
	public void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}

package org.gramlab.corpus;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Properties;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.language.LanguageIdentifier;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.SafeContentHandler;
import org.gramlab.docparsers.sax.FullWriteOutContentHandler;
import org.gramlab.docparsers.sax.TEIContentHandler;
import org.gramlab.gateway.Gateway;
import org.gramlab.gateway.solrGateway;
import org.gramlab.util.GenericUtils;
import org.xml.sax.helpers.DefaultHandler;


public class Corpus {
	private static final Logger theLogger = Logger.getLogger(Corpus.class);

	public File outputDir;
	public File pjDir;
	public File teiDir;
	public File archiveZipDir;
	public File home;
	public String solr_server="";
	public String solr_core="";
	public String propFileName = "glabcorpus.properties";
	public Gateway sg;

	public Corpus(String props_path){
		try {
			theLogger.trace("project real path "+props_path);
			// Check if there is an environment variable.
			String env = System.getenv("GLABCORPUS_HOME");
			FileInputStream fis = null;
			Properties props = new Properties();
			File prop = null;
			File prop_out = null;
			if(env==null){
				System.out.println("GCH not exist");
				/*GLABCORPUS_HOME does not exist*/
				prop = new File(props_path,"conf"+System.getProperty("file.separator")+propFileName);
				fis = new FileInputStream(prop);
				props.load(fis);
				/*create corpus home*/
				home = new File(props_path,"corpus_home");
				if(!home.exists()){
					home.mkdirs();
				}
				props.setProperty("corpus_home",home.getAbsolutePath());
				props.setProperty("data_home",new File(home,"data").getAbsolutePath());
				prop_out = prop;
			}else{
				/*GLABCORPUS_HOME does exist */
				System.out.println("GCH exist");
				File home_dir = new File(env);
				if(home_dir.exists()){
					System.out.println("GCH env exist & file exist");
					/*pre existing home directory*/
					if(new File(home_dir,propFileName).exists()){
						prop = new File(home_dir,propFileName);
					}else if (new File(new File(home_dir,"conf"),propFileName).exists()){
						prop = new File(new File(home_dir,"conf"),propFileName);
					}
					if(prop!=null){
						fis = new FileInputStream(prop);
						props.load(fis);
						prop_out = prop;
					}else{
						prop = new File(props_path,"conf"+System.getProperty("file.separator")+propFileName);
						prop_out = new File(new File(env,"conf"),propFileName);
						prop_out.getParentFile().mkdirs();
						fis = new FileInputStream(prop);
						props.load(fis);
						home = new File(home_dir,"corpus_home");
						if(!home.exists()){
							home.mkdirs();
						}
						props.setProperty("corpus_home",home.getAbsolutePath());
						props.setProperty("data_home",new File(home,"data").getAbsolutePath());
					}
				}else{
					/*GLABCORPUS_HOME variable does exist but not the file*/
					System.out.println("GCH env exist & file not exist");
					home_dir.mkdirs();
					prop = new File(props_path,"conf"+System.getProperty("file.separator")+propFileName);
					prop_out = new File(new File(home_dir,"conf"),propFileName);
					prop_out.mkdirs();
					fis = new FileInputStream(prop);
					props.load(fis);
					home = new File(home_dir,"corpus_home");
					if(!home.exists()){
						home.mkdirs();
					}
					props.setProperty("corpus_home",home.getAbsolutePath());
					props.setProperty("data_home",new File(home,"data").getAbsolutePath());
				}
			}
			System.out.println(" writing to propout "+prop_out.getAbsolutePath());
			System.out.println(" writing to propout core "+props.getProperty("solr_server"));
			System.out.println(" writing to propout home "+props.getProperty("corpus_home"));
			System.out.println(" writing to propout conf "+props.getProperty("data_home"));
			if(prop_out!=null){
				props.store(new FileOutputStream(prop_out),null);
			}
			fis = new FileInputStream(prop_out);
			props.load(fis);

			if(props.getProperty("data_home")!=null){
				home = new File(props.getProperty("data_home"));
			}else{
				home= new File(props_path);
			}
			System.out.println("data_home "+home);
			if(!home.exists()){
				home.mkdirs();
			}
			outputDir = new File(home,"data");
			if(!outputDir.exists()){
				outputDir.mkdirs();
			}
			pjDir = new File(home,"files");
			if(!pjDir.exists()){
				pjDir.mkdirs();
			}
			teiDir = new File(home,"tei");
			if(!teiDir.exists()){
				teiDir.mkdirs();
			}
			archiveZipDir = new File(home,"archiveZip");
			if(!archiveZipDir.exists()){
				archiveZipDir.mkdirs();
			}

			solr_server=props.getProperty("solr_server");
			solr_core=props.getProperty("solr_core");
			System.out.println("corpus solr server "+solr_server+"/"+solr_core);

			sg = new solrGateway(solr_server+"/"+solr_core,"");

			theLogger.trace("home "+home);
			theLogger.trace("directory files "+pjDir);
			theLogger.trace("directory tei "+teiDir);
			theLogger.trace("directory output "+outputDir);
			theLogger.trace("directory archive "+archiveZipDir);

		}catch(Exception e){
			System.out.println(" Error loading properties file "+e.getMessage());
			e.printStackTrace();
			theLogger.error("Error loading properties file "+e.getMessage());
		}
	}

	public void addFile(FileItem fit)throws MalformedURLException,Exception{
		File dir = new File(pjDir,GenericUtils.getDate());
		if(!dir.exists()){dir.mkdirs();}
		File file = new File(dir,fit.getName());
		try{
			fit.write(file);
		}catch(Exception e){
			theLogger.error("Adding file "+e.getMessage());
		}
		File2TEI(fit);
		sg.addEntry(File2Notice(file));
	}

	public void addFile(String name,InputStream is)throws MalformedURLException,Exception{
		File dir = new File(pjDir,GenericUtils.getDate());
		if(!dir.exists()){
			if(dir.mkdirs()){
				System.out.println(" Directory for pj files "+ dir.getAbsolutePath() + " is created");
			}else{
				System.out.println(" Directory for pj files "+ dir.getAbsolutePath() + " could not create");
			}
		}else{
			System.out.println(" Directory for for pj files "+ dir.getAbsolutePath() + " already exists ");
		}
		File file = new File(dir,name);
		OutputStream out=new FileOutputStream(file);
		byte buf[] = new byte[1024];
		int len;
		while ((len = is.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		out.close();
		is.close();

		File2TEI(file);
		System.out.println(" solr gateway "+sg);
		System.out.println(" addFile name "+file.getAbsolutePath());
		sg.addEntry(File2Notice(file));
	}

	public void File2TEI(FileItem fit){
		try{
			File dir = new File(teiDir,GenericUtils.getDate());
			if(!dir.exists()){
				if(dir.mkdirs()){
					System.out.println(" Directory for tei files "+ dir.getAbsolutePath() + " is created");
				}else{
					System.out.println(" Directory for tei files "+ dir.getAbsolutePath() + " could not create");
				}
			}else{
				System.out.println(" Directory for for tei files "+ dir.getAbsolutePath() + " already exists ");
			}

			AutoDetectParser parser = new AutoDetectParser();
			Metadata meta = new Metadata();
			meta.add(Metadata.RESOURCE_NAME_KEY, fit.getName());
			ParseContext context = new ParseContext();
			File tei_file = new File(dir,FilenameUtils.removeExtension(fit.getName())+".xml");
			System.out.println(" Debug : tei_file to create "+tei_file.getAbsolutePath());
			BufferedWriter bWriter = new BufferedWriter( new OutputStreamWriter( new FileOutputStream(tei_file), "utf-8" ) );
			parser.parse(fit.getInputStream(), new TEIContentHandler(new FullWriteOutContentHandler(bWriter), meta), meta, context);
			bWriter.close();
		}catch(Exception e){
			theLogger.error("TEI notice "+e.getMessage());
		}
	}

	public void File2TEI(File f){
		try{
			File dir = new File(teiDir,GenericUtils.getDate());
			if(!dir.exists()){dir.mkdirs();}
			AutoDetectParser parser = new AutoDetectParser();
			Metadata meta = new Metadata();
			meta.add(Metadata.RESOURCE_NAME_KEY, f.getName());
			ParseContext context = new ParseContext();
			BufferedWriter bWriter = new BufferedWriter( new OutputStreamWriter( new FileOutputStream(new File(dir,FilenameUtils.removeExtension(f.getName())+".xml")), "utf-8" ) );
			FileInputStream fis = new FileInputStream(f);
			parser.parse(fis, new TEIContentHandler(new FullWriteOutContentHandler(bWriter), meta), meta, context);
			bWriter.close();
		}catch(Exception e){
			theLogger.error("TEI notice "+e.getMessage());
		}
	}

	private void File2TEI(String name,OutputStream stream,InputStream fis){
		try{
			AutoDetectParser parser = new AutoDetectParser();
			Metadata meta = new Metadata();
			meta.add(Metadata.RESOURCE_NAME_KEY, name);
			ParseContext context = new ParseContext();
			BufferedWriter bWriter = new BufferedWriter( new OutputStreamWriter(stream, "utf-8" ) );
			parser.parse(fis, new TEIContentHandler(new FullWriteOutContentHandler(bWriter), meta), meta, context);
			bWriter.close();
		}catch(Exception e){
			theLogger.error("TEI notice "+e.getMessage());
		}
	}


	public File File2Notice(File f){
		try{
			System.out.println(" File - > 2 Solr Notice "+f.getAbsolutePath());
			if(f!=null){
				System.out.println(" FILE abs "+f.getAbsolutePath());
				File dir = new File(outputDir,GenericUtils.getDate());
				if (!dir.exists()) {
					if (dir.mkdirs()) {
						System.out.println(" Directory for solr data "+ dir.getAbsolutePath() + " is created");
					}else{
						System.out.println(" Directory for solr data "+ dir.getAbsolutePath() + " could not create");
					}
				}else{
					System.out.println(" Directory for solr data "+ dir.getAbsolutePath() + " already exists ");
				}
			}
			AutoDetectParser parser = new AutoDetectParser();
			InputStream tis = TikaInputStream.get(f);
			Metadata meta = new Metadata();
			meta.add(Metadata.RESOURCE_NAME_KEY, f.getName());
			ParseContext context = new ParseContext();
			DefaultHandler handler = new SafeContentHandler(new BodyContentHandler(-1));
			parser.parse(tis, handler, meta, context);
			meta.add(Metadata.LANGUAGE, getLanguage(handler.toString()));


			File out = new File(new File(outputDir,GenericUtils.getDate()),FilenameUtils.removeExtension(f.getName())+".xml");
			System.out.println(" outputDir is created "+outputDir.getAbsolutePath());
			System.out.println(" out is created "+out.getAbsolutePath());
			BufferedWriter bWriter = new BufferedWriter( new OutputStreamWriter( new FileOutputStream(out), "utf-8" ) );
			GenericUtils.QESNotice(handler, meta, bWriter);
			return out;

		} catch (Exception e) {
			System.out.println("Notice " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}


	public void removeEntry(List<String> ids){
		for(String i:ids){
			this.removeEntry(i);
		}
	}

	public void removeEntry(String uniqueId){
		System.out.println("Removing a file "+uniqueId);

		File file = new File(pjDir,uniqueId);
		System.out.println("PJ "+file.getAbsolutePath()+" "+file.exists());
		File parent = null;
		if(file.exists()){
			parent = file.getParentFile();
			if(file.delete()){
				System.out.println("PJ "+file.getAbsolutePath()+" deleted ");
			}
			if(parent.isDirectory()&& parent.list().length==0){
				System.out.println("PJ "+parent.getAbsolutePath()+" is empty also deleting ");
				parent.delete();
			}
		}
		file = new File(teiDir,FilenameUtils.removeExtension(uniqueId)+".xml");
		System.out.println("tei "+file.getAbsolutePath()+" "+file.exists());
		if(file.exists()){
			parent = file.getParentFile();
			if(file.delete()){
				System.out.println("TEI "+file.getAbsolutePath()+" deleted ");
			}
			if(parent.isDirectory()&& parent.list().length==0){
				System.out.println("TEI "+parent.getAbsolutePath()+" is empty also deleting ");
				parent.delete();
			}
		}
		file = new File(outputDir,FilenameUtils.removeExtension(uniqueId)+".xml");
		System.out.println("OUTPUT "+file.getAbsolutePath()+" "+file.exists());
		if(file.exists()){
			parent = file.getParentFile();
			if(file.delete()){
				System.out.println("Native file "+file.getAbsolutePath()+" deleted ");
			}
			if(parent.isDirectory()&& parent.list().length==0){
				System.out.println("Native directory "+parent.getAbsolutePath()+" is empty also deleting ");
				parent.delete();
			}
		}
		sg.deleteEntry(uniqueId);
	}

	private String getLanguage(String content){
		LanguageIdentifier li = new LanguageIdentifier(content);
		String language = li.getLanguage();
		return language;
	}

	public static void main(String[] args) {

	}
}
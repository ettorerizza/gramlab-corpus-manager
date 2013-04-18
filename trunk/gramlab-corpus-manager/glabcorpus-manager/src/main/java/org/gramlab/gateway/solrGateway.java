package org.gramlab.gateway;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.util.SimplePostTool;


public class solrGateway implements Gateway{
	private static final Logger log = Logger.getLogger(solrGateway.class);
	
	private String serverUrl;
	private String solr_home;
	private SimplePostTool spt;
	
	/*public solrGateway() throws MalformedURLException{
		spt = new SimplePostTool(new URL(serverUrl+"/update"));
	}*/
	
	public solrGateway(String solr_server,String solr_home) throws MalformedURLException{
		serverUrl = solr_server;
		this.solr_home= solr_home;
		System.out.println("solt url "+serverUrl);
		spt = new SimplePostTool(new URL(serverUrl+"/update"));
	}
  
  public  void addEntry (File fileName){
	  System.out.println(" filename "+fileName);
	 if(fileName.isDirectory()){
		for(File currFile:fileName.listFiles()){
			indexFile(currFile);
		}
	 }else{
		 indexFile(fileName);
	 }
  }
  
  
  public void deleteEntries(List<String> ids){
	  try{
		  SolrServer server = new CommonsHttpSolrServer(serverUrl);
		  for(String id:ids){
			  server.deleteById(id);
		  }
		 server.commit(); 
	  }catch(Exception e){
			log.error("delete entries "+e.getMessage()); 
		 }
  }
  
  public void deleteEntry(String id){
	  try{
		  SolrServer server = new CommonsHttpSolrServer(serverUrl);
		  server.deleteById(id);
		  server.commit();
	  }catch(Exception e){
		  log.error("delete entr    y "+e.getMessage());
		 }
  }
  
  private  void indexFile(File file){
	 this.postIndexFile(file);
  }
  
  
  
  public void postIndexFile(File topost){
	  OutputStream os = new ByteArrayOutputStream();
		try {
			spt.postFile(topost, os);
			if ("yes".equals(System.getProperty("commit", "yes"))) {
				OutputStream os2 = new ByteArrayOutputStream();
				spt.commit(os2);	
			}
		} catch (Exception e) {
			log.error("post index "+e.getMessage()); 
		}
	  
  }
  
  public void postIndexFile(StringBuffer topost){
	  
  }
  
  public static void main(String[] args) {
	  }
}

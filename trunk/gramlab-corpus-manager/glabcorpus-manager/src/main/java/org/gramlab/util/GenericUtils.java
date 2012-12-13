package org.gramlab.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.sax.TextContentHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.helpers.DefaultHandler;

import servlet.CorpusEdit;

public class GenericUtils {
	private static final Logger log = Logger.getLogger(GenericUtils.class);
	private static TransformerFactory factory = TransformerFactory.newInstance();
	private static Transformer transformer = null;
	
	static HashMap<Object,String> metamap = new HashMap<Object,String>();
	 static {
		 metamap.put(Metadata.AUTHOR, "AUTHOR");
		 metamap.put(Metadata.CREATOR, "CREATOR");
		 metamap.put(Metadata.LAST_AUTHOR, "AUTHOR");
		 metamap.put(Metadata.PUBLISHER, "PUBLISHER");
		 metamap.put(Metadata.CATEGORY, "CATEGORY");
		 metamap.put(Metadata.DESCRIPTION, "DESCRIPTION");
		 metamap.put(Metadata.LANGUAGE, "LANGUAGE");
		 metamap.put(Metadata.COMMENTS, "COMMENTS");
		 metamap.put(Metadata.COMMENT, "COMMENTS");
		 metamap.put(Metadata.CONTENT_TYPE, "CONTENT_TYPE");
		 metamap.put(Metadata.DATE, "PUBLISH_DATE");
		 metamap.put(Metadata.CREATION_DATE, "PUBLISH_DATE");
		 metamap.put("Creation-Date", "PUBLISH_DATE");
		 metamap.put("Last-Modified","DATE_MODIF");
		 metamap.put(Metadata.LAST_MODIFIED, "DATE_MODIF");
		 metamap.put("xmpTPg:NPages","PAGE_COUNT");
		 metamap.put(Metadata.PAGE_COUNT, "PAGE_COUNT");
		 metamap.put(Metadata.RESOURCE_NAME_KEY, "FILE_NAME");
		 metamap.put(Metadata.TITLE, "TITLE");
	 }
	
	public static String getUTCDate(){
		return new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime())+"T"+new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime())+"Z";
	}
	public static String getDate(){
		return new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
	}
	
//	public static void QESNotice(DefaultHandler handler,Metadata meta,File outputDir){
//		File output = new File(outputDir,GenericUtils.getDate());
//		if(!output.exists()){
//			output.mkdirs();
//		}
//		try{
//			  // Create file 
//			//String nom = "output.xml";
//			  BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(output,meta.get(Metadata.RESOURCE_NAME_KEY)+".xml")),"UTF-8"));
//			  out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
//			  out.write("<DOCUMENT>\n");
//			  out.write("<DATE_COLLECT>"+GenericUtils.getUTCDate()+"</DATE_COLLECT>\n");
//			  for(String curr_name : meta.names()){
//				  if(metamap.containsKey(curr_name)){
//					  out.write("<"+metamap.get(curr_name)+">"+meta.get(curr_name)+"</"+metamap.get(curr_name)+">\n");
//				  }else{
//					  out.write("<NOT_"+curr_name+">"+meta.get(curr_name)+"<NOT_"+curr_name+">\n");
//				  }
//			  }
//			  out.write("<Fichier_joint>"+new File(output,meta.get(Metadata.RESOURCE_NAME_KEY)).getAbsolutePath()+"</Fichier_joint>\n");
//			  out.write("<TEXT>"+handler.toString()+"</TEXT>\n");
//			  out.write("</DOCUMENT>\n");
//			  out.close();
//		}catch (Exception e){//Catch exception if any
//			log.error("QESNotice "+e.getMessage());
//	    }	  
//	}
	
//	public static void QESNotice(DefaultHandler handler,Metadata meta,File outputDir){
//		File output = new File(outputDir,GenericUtils.getDate());
//		if(!output.exists()){
//			output.mkdirs();
//		}
//		try{
//			// Create file
//			String id = GenericUtils.getDate()+ System.getProperty("file.separator")+ meta.get(Metadata.RESOURCE_NAME_KEY);
//			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(output,meta.get(Metadata.RESOURCE_NAME_KEY) + ".xml")),"UTF-8"));
//			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
//			Element root = document.createElement("add");
//			root.appendChild(document.createElement("doc"));
//			Element date = document.createElement("DATE_COLLECT");
//			date.setTextContent(GenericUtils.getUTCDate());
//			for (String curr_name : meta.names()) {
//				if (metamap.containsKey(curr_name)) {
//					Element current = document.createElement(curr_name);
//					current.setTextContent(meta.get(curr_name));
//					root.appendChild(current);
//				} else {
//					Element current = document.createElement(curr_name);
//					current.setTextContent(meta.get(curr_name));
//					root.appendChild(current);
//					out.write("<NOT_" + curr_name + ">" + meta.get(curr_name)
//							+ "<NOT_" + curr_name + ">\n");
//				}
//			}
//			Element fj = document.createElement("Fichier_joint");
//			fj.setTextContent((new File(output, meta
//					.get(Metadata.RESOURCE_NAME_KEY)).getAbsolutePath()));
//			root.appendChild(fj);
//			Element text = document.createElement("TEXT");
//			text.setTextContent(handler.toString());
//			root.appendChild(date);
//			root.appendChild(text);
//		} catch (Exception e) {// Catch exception if any
//			log.error("QESNotice " + e.getMessage());
//		}
//	}
	
	public static void  QESNotice(DefaultHandler handler,Metadata meta,Writer writer){
		try{
			// Create file
			String id = GenericUtils.getDate()+ System.getProperty("file.separator")+ meta.get(Metadata.RESOURCE_NAME_KEY);
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			
			Element parent = document.createElement("add");
			document.appendChild(parent);
			Element root = document.createElement("doc");
			parent.appendChild(root);
			
			Element idelt = document.createElement("field");
			idelt.setAttribute("name", "ID");
			idelt.setTextContent(id);
			root.appendChild(idelt);
			
			Element date = document.createElement("field");
			date.setAttribute("name", "DATE_COLLECT");
			date.setTextContent(GenericUtils.getUTCDate());
			root.appendChild(date);

			for (String curr_name : meta.names()) {
				Element current = null;
				if (metamap.containsKey(curr_name)) {
					current = document.createElement("field");
					current.setAttribute("name", metamap.get(curr_name));
					//meta.CONTENT_ENCODING,
					//current.setTextContent(new String(meta.get(curr_name).getBytes(System.getProperty("file.encoding")), "UTF-8"));
					current.setTextContent(meta.get(curr_name));
					
					root.appendChild(current);
				} else {
					current = document.createElement("field");
					current.setAttribute("name", "NOT_"+curr_name);
					current.setTextContent(new String(meta.get(curr_name).getBytes(System.getProperty("file.encoding")), "UTF-8"));
				}
			}
			
			if(meta.get(Metadata.TITLE)==null){
				Element titre = document.createElement("field");
				titre.setAttribute("name", "TITLE");
				titre.setTextContent(meta.get(Metadata.RESOURCE_NAME_KEY));
				root.appendChild(titre);
			  }
			
			
			Element pj = document.createElement("field");
			pj.setAttribute("name","PJ");
			pj.setTextContent(id);
			root.appendChild(pj);
			
			Element content = document.createElement("field");
			content.setAttribute("name","CONTENT");
			content.setTextContent(StringEscapeUtils.escapeXml(handler.toString()));
			root.appendChild(content);
			
			Element tei = document.createElement("field");
			tei.setAttribute("name","TEI_FILE");
			tei.setTextContent(StringEscapeUtils.escapeXml(handler.toString()));
			root.appendChild(tei);
			
			transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.transform(new DOMSource(document), new StreamResult(writer));
			
		} catch (Exception e) {// Catch exception if any
			log.error("QESNotice " + e.getMessage());
			e.printStackTrace();
		}
		
	}
	
	
//	public static StringBuffer Notice(DefaultHandler handler,Metadata meta){
//		String id = GenericUtils.getDate()+System.getProperty("file.separator")+meta.get(Metadata.RESOURCE_NAME_KEY);
//		StringBuffer sb = new StringBuffer();
//			sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
//			sb.append("<add>\n");
//			sb.append("<doc>\n");
//			sb.append("<field name=\"ID\">"+id+"</field>\n");
//			sb.append("<field name=\"DATE_COLLECT\">"+GenericUtils.getUTCDate()+"</field>\n");
//			  for(String curr_name : meta.names()){
//				  if(metamap.containsKey(curr_name)){
//					  sb.append("<field name=\""+metamap.get(curr_name)+"\">"+meta.get(curr_name)+"</field>\n");
//				  }
//			  }
//			  if(meta.get(Metadata.TITLE)==null){
//				  sb.append("<field name=\"TITLE\">"+meta.get(Metadata.RESOURCE_NAME_KEY)+"</field>\n");
//			  }
//			  sb.append("<field name=\"PJ\">"+id+"</field>\n");
//			  sb.append("<field name=\"TEI_FILE\">"+id+".xml"+"</field>\n");
//			  sb.append("<field name=\"CONTENT\">"+StringEscapeUtils.escapeXml(handler.toString())+"</field>\n");
//			  sb.append("</doc>\n");
//			  sb.append("</add>\n");
//			  return sb;
//	}
	
	public static void write2File(StringBuffer sb,File file){
		try{
			if(!file.exists()){
				file.getParentFile().mkdirs();
			}
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF-8"));
			out.write(sb.toString());
			out.close();
		}catch (Exception e){//Catch exception if any
			System.out.println("to file "+e.getMessage());
			e.printStackTrace();
	    }	  	 
	}
	public static void main(String[] args) {
		//Corpus corpus = new Corpus();
		//QESNotice(new File("C:\\opensource\\gramlab tika\\trunk\\src\\test\\resources\\samples\\samples01.pdf"));
	}
	
	
}

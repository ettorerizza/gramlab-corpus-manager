package servlet;

import java.io.File;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.gramlab.corpus.Corpus;


public class InitCore extends HttpServlet{
	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		Corpus corpus = new Corpus(getServletContext().getRealPath("/"));
		System.out.println("######  init core servlet for glab");
		getServletContext().setAttribute("corpus", corpus);
		if(this.getServletContext().getAttribute("corpus")!=null){
			System.out.println("######  corpus set en context");
		}else{
			System.out.println("######  corpus setted but not found in context");
		}
		
	}
}
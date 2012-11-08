package servlet;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.gramlab.corpus.Corpus;

 
 
public class CommonsFileUploadServlet extends HttpServlet {
	private File tmpDir;
	private static final String DESTINATION_DIR_PATH ="/";
	private File destinationDir;
	private Corpus corpus = null;
 
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		corpus  = (Corpus)this.getServletContext().getAttribute("corpus");
		if(corpus==null){
			System.out.println(" Corpus pas trouvé dans le context on va recreer");
			corpus = new Corpus(getServletContext().getRealPath("/"));
		}else{
			System.out.println(" Corpus rouvé dans le context ");
		}
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 "
				+ "Transitional//EN\">\n" + "<HTML>\n"
				+ "<HEAD><TITLE>Hello WWW</TITLE></HEAD>\n" + "<BODY>\n"
				+ "<H1>Hello WWW</H1>\n" + "</BODY></HTML>");
	}

 
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    PrintWriter writer = null;
	    InputStream is = null;
        FileOutputStream fos = null;
	    response.setContentType("text/plain");
	   
	    System.out.println(" Got a do post Request");
	    try {
            writer = response.getWriter();
            writer.println("{success:true}");
        } catch (IOException ex) {
            log("Thrown an exception: " + ex.getMessage());
        }
 
        boolean isMultiPart = ServletFileUpload.isMultipartContent(request);
        System.out.println(" Got a do post Request is multipart "+isMultiPart);
        if (isMultiPart) {
            log("Content-Type: " + request.getContentType());
            // Create a factory for disk-based file items
            DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
 
            /*
             * Set the file size limit in bytes. This should be set as an
             * initialization parameter
             */
            diskFileItemFactory.setSizeThreshold(1024 * 1024 * 100); //100MB.
            diskFileItemFactory.setRepository(tmpDir);
            
            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload(diskFileItemFactory);
 
            List items = null;
 
            try {
                items = upload.parseRequest(request);
            }catch(FileUploadException ex) {
    			log("Error encountered while parsing the request",ex);
    		} catch(Exception ex) {
    			log("Error encountered while uploading file",ex);
    			response.sendError(500,ex.getMessage());
    		}
 
            ListIterator li = items.listIterator();
            while (li.hasNext()) {
                FileItem item = (FileItem) li.next();
                try {
					corpus.addFile(item);
				} catch (Exception e) {
					log("Error corpus adding file"+e.getMessage());
					e.printStackTrace();
				}	
				writer.close();
            }
        }else {
        	try {
				corpus.addFile(request.getParameter("qqfile"), request.getInputStream());
			} catch (Exception e) {
				System.out.println(" Erreur dans la gestion uniq file "+e.getMessage());
				e.printStackTrace();
			}
        }

        	
        /*if ("application/octet-stream".equals(request.getContentType())) {
            log("Content-Type: " + request.getContentType());
            String filename = request.getHeader("X-File-Name");
 
            try {
                is = request.getInputStream();
                fos = new FileOutputStream(new File(realPath + filename));
                IOUtils.copy(is, fos);
                response.setStatus(HttpServletResponse.SC_OK);
                writer.print("{success: true}");
            } catch (FileNotFoundException ex) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writer.print("{success: false}");
                log(MultiContentServlet.class.getName() + "has thrown an exception: " + ex.getMessage());
            } catch (IOException ex) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writer.print("{success: false}");
                log(MultiContentServlet.class.getName() + "has thrown an exception: " + ex.getMessage());
            } finally {
                try {
                    fos.close();
                    is.close();
                } catch (IOException ignored) {
                }
            }
            writer.flush();
            writer.close();
        }*/
    
        
/*		DiskFileItemFactory  fileItemFactory = new DiskFileItemFactory ();
		fileItemFactory.setSizeThreshold(1*1024*1024); //1 MB
		fileItemFactory.setRepository(tmpDir);
 
		ServletFileUpload uploadHandler = new ServletFileUpload(fileItemFactory);
		try {
			List items = uploadHandler.parseRequest(request);
			Iterator itr = items.iterator();
			while(itr.hasNext()) {
				FileItem item = (FileItem) itr.next();
				corpus.addFile(item);	
				out.close();
			}
		}catch(MalformedURLException ex) {
			log("Solr ulr not valid",ex);
		}catch(FileUploadException ex) {
			log("Error encountered while parsing the request",ex);
		} catch(Exception ex) {
			log("Error encountered while uploading file",ex);
			response.sendError(500,ex.getMessage());
		}*/
 
	}
	
	 private void processFormField(FileItem item) {
	        // Process a regular form field
	        if (item.isFormField()) {
	            String name = item.getFieldName();
	            String value = item.getString();
	            log("name: " + name + " value: " + value);
	        }
	    }
	 
	/*    private String processUploadedFile(FileItem item) {
	        // Process a file upload
	        if (!item.isFormField()) {
	            try {
	                item.write(new File(realPath + item.getName()));
	                return "{success:true}";
	            } catch (Exception ex) {
	                log(FileUploadServlet.class.getName() + " has thrown an exception: " + ex.getMessage());
	            }
	        }
	        return "{success:false}";
	    }*/
 
}
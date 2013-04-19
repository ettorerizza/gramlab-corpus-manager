(function ($) {

AjaxSolr.theme.prototype.result = function (doc, snippet) {
  var output = '<div class="document"><div class="selection"><input type="checkbox"></div><div class="res_content"><div class="top_res">';
  if(doc.CONTENT_TYPE=="application/pdf"){
	 output +=  '<span class="picto"><img src="img/picto/pdf.png" alt="PDF" /></span>';
  }
  if(doc.CONTENT_TYPE=="application/msword"){
	 output +=  '<span class="picto"><img src="img/picto/word.gif" alt="word" /></span>';
  }
  if(doc.CONTENT_TYPE=="text/plain"){
	 output +=  '<span class="picto"><img src="img/picto/txt.gif" alt="rtf" /></span>';
  }
  if(doc.CONTENT_TYPE=="application/rtf"){
	 output +=  '<span class="picto"><img src="img/picto/rtf.gif" alt="rtf" /></span>';
  }
   if(doc.CONTENT_TYPE=="application/vnd.oasis.opendocument.text"){
	 output +=  '<span class="picto"><img src="img/picto/odt.gif" alt="rtf" /></span><';
  }
   if(doc.CONTENT_TYPE=="application/zip"){
	 output +=  '<span class="picto"><img src="img/picto/zip.gif" alt="zip" /></span>';
  }
  if(doc.CONTENT_TYPE=="text/html; charset=iso-8859-1" | doc.CONTENT_TYPE=="text/html" | doc.CONTENT_TYPE=="text/html; charset=UTF-8" | doc.CONTENT_TYPE=="text/html; charset=utf-8")
  {
		 output +=  '<span class="picto"><img src="img/picto/application-xhtml+xml.png" alt="html" /></span>';
	}
  if(doc.CONTENT_TYPE=="application/rss+xml"){
		 output +=  '<span class="picto"><img src="img/picto/rss.png" alt="rss" /></span>';
	  }
  
  if(doc.LANGUAGE!==undefined){
	  if(doc.LANGUAGE=="fr" | doc.LANGUAGE=="fra"){
		  output += '<span class="picto"><img src="img/flags/fr.png" alt="fr"/></span>';  
	  }
	  if(doc.LANGUAGE=="en"){
		  output += '<span class="picto"><img src="img/flags/en.png" alt="en"/><span>';  
	  }
	  if(doc.LANGUAGE=="de"){
		  output += '<span class="picto"><img src="img/flags/de.png" alt="de"/></span>';  
	  }
	  if(doc.LANGUAGE=="es"){
		  output += '<span class="picto"><img src="img/flags/es.png" alt="es"/></span>';  
	  }
	  if(doc.LANGUAGE=="it"){
		  output += '<span class="picto"><img src="img/flags/it.png" alt="it"/></span>';  
	  }
	  if(doc.LANGUAGE=="pt"){
		  output += '<span class="picto"><img src="img/flags/pt.png" alt="pt"/></span>';  
	  }
	  if(doc.LANGUAGE=="no"){
		  output += '<span class="picto"><img src="img/flags/no.png" alt="no"/></span>';  
	  }
	  if(doc.LANGUAGE=="hu"){
		  output += '<span class="picto"><img src="img/flags/hu.png" alt="hu"/></span>';  
	  }
	  if(doc.LANGUAGE=="ru"){
		  output += '<span class="picto"><img src="img/flags/ru.png" alt="ru"/></span>';  
	  }  
  }
   
   
  output +=  '<span class="title" idgram="'+doc.ID+'"><h2>' + doc.TITLE + '</h2></span>';
  output +=  "<a href=\"CorpusEdit?tei_show="+encodeURIComponent(doc.ID)+"\" target=\"_blank\"><span class=\"tei_picto\"><img src=\"img/picto/tei.png\" alt=\"tei\" title=\"see as TEI\"/></span></a>";  
  output +=  "<a target=\"_blank\" href=\"CorpusEdit?file_get="+encodeURIComponent(doc.ID)+"\"><span class=\"download_picto\"><img src=\"img/picto/download.gif\" alt=\"download\" title=\"download\"/></span></a>";  
  
  output +=  '</div>';
  output += '<div class="content_res"><span class="date" class="date_collect">' + doc.DATE_COLLECT + '</span>';
  output += '<p id="links_' + doc.ID + '" class="links" val="'+doc.CONTENT[0].length+'"></p>';
  output += '<p>' + snippet + '</p></div>';
  output += '<div class="sugar"><span class="IDQES no_view">'+doc.ID+'</span>';
  output += '<div class="author_container">';
  if(doc.AUTHOR!==undefined){
	output += '<img class="img_author" src="img/picto/author.png" alt="author"/><span class="author">'+doc.AUTHOR+'</span><br/>';
  }
 
  output += '</div>';
  output += '<img class="delete" src="img/picto/delete.gif" alt="delete" title="delete"/></div></div></div>';
  return output;
};

AjaxSolr.theme.prototype.snippet = function (doc) {
  var output = '';
  if (doc.CONTENT[0].length > 350) {
    output += doc.CONTENT[0].substring(0, 350);
  }
  else {
    output += doc.CONTENT;
  }
  return output;
};

AjaxSolr.theme.prototype.tag = function (value, weight, handler) {
  return $('<a href="#" class="tagcloud_item"/>').text(value).addClass('tagcloud_size_' + weight).click(handler);
};

AjaxSolr.theme.prototype.facet_link = function (value, handler) {
  return $('<a href="#"/>').text(value).click(handler);
};

AjaxSolr.theme.prototype.no_items_found = function () {
  return 'no items found in current selection';
};

})(jQuery);

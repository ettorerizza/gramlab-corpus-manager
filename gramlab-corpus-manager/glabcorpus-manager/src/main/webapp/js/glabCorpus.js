var Manager;

(function ($) {

$(document).ready(function() { 
            // bind 'myForm' and provide a simple callback function 
            $('#uploadform').ajaxForm(function() { 
                $('input:file').MultiFile('reset'); 
            });
			$(".title").live("click",function(){selectDocs(this)});
			$(".selection input").live("change",function(){selectDocs(this)});
			$("#selector input").live("change",function(){selectAllDocs(this)});
			$(".author").hide();
			$(".delete").live("click",function(){delEntry($(this).parent().find(".IDQES").text())});
			$("#delete_batch_picto").live("click",function(){delEntries()});
			$(".img_author").live("click",function(){showHideAuthor(this)});
			$(".more_content_link").live("click",function(){showMore(this)});
			$(".less_content_link").live("click",function(){showLess(this)});
			$("#tei_batch_picto").live("click",function(){getTEIFormat(this)});
			$("#download_batch_picto").live("click",function(){getFileFormat(this)});
        });
		
		function selectDocs(elt){
			//$(elt).toggleClass("doc_selected");
			if(getDoc(elt).hasClass("doc_selected")){
				$("#selector input").attr('checked',false);
			}
			getDoc(elt).toggleClass("doc_selected");
			var list =  $(".doc_selected").length;
			if(list){
				$('#batch_action').animate({"right":"-1px"},200);
				$('#selected-header').text(" Selected "+list+" documents");
			}else{
				$('#batch_action').animate({"right":"-60px"},200);
				$('#selected-header').text("");
			}
		}
		
		function selectAllDocs(elt){
			if($(elt).is(':checked')){
				$(".selection input").attr('checked',true);
				$(".document").addClass("doc_selected");
				var list =  $(".doc_selected").length;
				if(list){
					$('#batch_action').animate({"right":"-1px"},200);
					$('#selected-header').text(" Selected "+list+" documents");
				}
			}else{
			    $(".selection input").attr('checked',false);
				$(".document").removeClass("doc_selected");
				$('#batch_action').animate({"right":"-60px"},200);
				$('#selected-header').text("");
			}
		}
		
		function getDoc(elt){
			return $($(elt).parents(".document")[0]);
		}

function getTEIFormat(){
	var list =  $(".doc_selected");
	var p = [];
	var title =[];
	var authors = [];
	
	for (i=0; i <list.length;i++){
		el= jQuery(list[i]);
		p.unshift(encodeURIComponent(el.find(".IDQES").text()));
		if(i<3){
			title.unshift(el.find(".title").text());
			authors.unshift(el.find(".author").text());
		}
	}
	params=p.join(";");
	ptitle = title.join(",");
	pauthor = 	authors.join(",");
	if(list.length>3){
		other = list.length - 3;
		if(other>1){
			ptitle +=" and "+other+" others";
			pauthor +=" and "+other+" others";
		}else{
			ptitle +=" and "+other+" other";
			pauthor +=" and "+other+" other";
		}
	}
	window.open("CorpusEdit?tei_get="+params+"&authors="+pauthor+"&titles="+ptitle);
}

function getFileFormat(){
	var list =  $(".doc_selected");
	var p = [];
	for (i=0; i <list.length;i++){
		el= jQuery(list[i]);
		p.unshift(encodeURIComponent(el.find(".IDQES").text()));
	}
	params=p.join(";");
	window.open("CorpusEdit?file_get="+params);
}
		
function showMore(e){
				el= jQuery(e);
				el.toggle();
				el.parent().find(".more_content").toggle("slow");
			}
			function showMore(e){
				el= jQuery(e);
				el.toggle();
				el.parent().find(".more_content").toggle();
				el.parent().find(".more_content_link").toggle();
			}
			
			function showHideAuthor(e){
					el= jQuery(e);
					//el.toggle();
					el.parent().find(".author").toggle();
			}
			function selectDoc(e){
				el= jQuery(e);
				el.toggleClass("doc_selected");
			}			

function delEntry(value){
	params={"delete":value};
	$.ajax({
	url: "CorpusEdit",
	data:params,
	context: document.body,
	success: function(){
		Manager.widgets.text.manager.doRequest(0);
	 }
	});
}

function delEntries(){
	var list =  $(".doc_selected");
	var p = [];
	for (i=0; i <list.length;i++){
		el= jQuery(list[i]);
		p.unshift(el.find(".IDQES").text());
	}
	params={"delete":p.join(";")};
	$.ajax({
	url: "CorpusEdit",
	data:params,
	context: document.body,
	success: function(){
		Manager.widgets.text.manager.doRequest(0);
	 }
	});
}

	
function getFiles(value){
	params={"file_get":value};
	$.ajax({
	url: "CorpusEdit",
	data:params,
	context: document.body,
	success: function(){
	 }
	});

}


$(function () {
    Manager = new AjaxSolr.Manager({
      solrUrl: 'http://localhost:8080/GramlabSolr/gramlab/'
    });
    Manager.addWidget(new AjaxSolr.ResultWidget({
      id: 'result',
      target: '#docs'
    }));
	
	Manager.addWidget(new AjaxSolr.PagerWidget({
		id: 'pager',
		target: '#pager',
		prevLabel: '&lt;',
		nextLabel: '&gt;',
		innerWindow: 1,
		renderHeader: function (perPage, offset, total) {
			$('#pager-header').html($('<span/>').text('displaying ' + Math.min(total, offset + 1) + ' to ' + Math.min(total, offset + perPage) + ' of ' + total));
		}
	}));
	
	Manager.addWidget(new AjaxSolr.TextWidget({
		id: 'text',
		target: '#search',
		field: 'text'
	}));
	
		
	Manager.addWidget(new AjaxSolr.CurrentSearchWidget({
		id: 'currentsearch',
		target: '#selection'
	}));
	
	Manager.addWidget(new AjaxSolr.CalendarWidget({
		  id: 'calendar',
		  target: '#calendar',
		  field: 'DATE_COLLECT'
		}));
	
	var fields = [ 'LANGUAGE', 'PUBLISHER', 'AUTHOR', 'CONTENT_TYPE' ];
    for (var i = 0, l = fields.length; i < l; i++) {
      Manager.addWidget(new AjaxSolr.TagcloudWidget({
        id: fields[i],
        target: '#' + fields[i],
        field: fields[i]
      }));
    }
    
    Manager.init();
    Manager.store.addByValue('q', '*:*');
	Manager.store.addByValue('rows', '50');
	var params = {
			facet: true,
			'facet.field': [ 'LANGUAGE', 'PUBLISHER', 'AUTHOR', 'CONTENT_TYPE'],
			'facet.limit': 20,
			'facet.mincount': 1,
			'json.nl': 'map',
			'facet.date': 'DATE_COLLECT',
			'facet.date.start': '2013-04-01T00:00:00.000Z/DAY',
			'facet.date.end': '2030-12-31T00:00:00.000Z/DAY+1DAY',
			'facet.date.gap': '+1DAY',
	};
		    for (var name in params) {
		      Manager.store.addByValue(name, params[name]);
		    }
    Manager.doRequest();
  });

})(jQuery);

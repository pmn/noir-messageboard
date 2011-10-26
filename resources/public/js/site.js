function togglesticky(postid){
    posturl = "/togglesticky";
    $.post(posturl, { postid: postid }, function(data){
	if (data == "true"){
	    $("#togglesticky").text(" [Make Sticky]");
	} else {
	    $("#togglesticky").text(" [Unsticky]");
	}
    });

}

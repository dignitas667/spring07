<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>

<div class="uploadDiv">
	<input type="file" name="uploadFile" multiple="multiple" />
</div>

<button id="uploadBtn">Upload</button>
<script type="text/javascript" src="<c:url value="/webjars/jquery/3.6.0/dist/jquery.js" />"></script>
<script type="text/javascript">
$(document).ready(function(){
	$("#uploadBtn").on("click", function(e){
		let formData = new FormData();
		let inputFile = $("input[name='uploadFile']");
		let files = inputFile[0].files;
		console.log(files);
		
		for(let i=0; i < files.length; i++) {
			formData.append("uploadFile", files[i]);	
		}		
		
		$.ajax({
			url : 'uploadAjaxAction',
			processData: false,
			contentType: false,
			data : formData, 
			type : 'POST',
			success:function(result) {
				alert("Uploaded");
			}
		});
	});
});
</script>
</body>
</html>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="app" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<style>
.uploadResult {
	width: 100%;
	background-color: gray;
}

.uploadResult ul {
	display: flex;
	flex-flow: row;
	justify-content: center;
	align-items: center;
}

.uploadResult ul li {
	list-style: none;
	padding: 10px;
}

.uploadResult ul li img {
	width: 20px;
}
</style>

</head>
<body>

<div class="uploadDiv">
<input type="file" name="uploadFile" multiple="multiple">
</div>
<div class="uploadResult">
    <ul>
    
    </ul>
</div>

<button id="uploadBtn">Upload</button>

<script type="text/javascript" src="<c:url value="/webjars/jquery/3.6.0/dist/jquery.js" />"></script>
<script type="text/javascript">
$(document).ready(function(){
	let regex = new RegExp("(.*?)\.(exe|sh|zip|alz)");
	let maxSize = 41943040 ; 

	function checkExtension(fileName, fileSize) {
		if(fileSize >= maxSize){
			alert("파일 사이즈 초과");
			return false;
		}//end if

		if(regex.test(fileName)){
			alert("해당 종류 파일 업로드 불가");
			return false;
		}//end if
		return true;
	}
	
	let cloneObj =$(".uploadDiv").clone();

	$("#uploadBtn").on("click", function(e){
		let formData = new FormData();
		let inputFile = $("input[name='uploadFile']");
		let files = inputFile[0].files;
		console.log(files);
		
		for(let i=0; i < files.length; i++) {
			//파일 종류 및 크기 체크
			if( !checkExtension(files[i].name, files[i].size ) ){
				return false;
			}

			formData.append("uploadFile", files[i]);	
		}		
		
		$.ajax({
			url : 'uploadAjaxAction',
			processData: false,
			contentType: false,
			data : formData, 
			type : 'POST',
			dataType : 'json',
			success:function(result) {
				//alert("Uploaded");
				console.log(result);
				showUploadedFile(result);
				$(".uploadDiv").html(cloneObj.html());
			}
		});
	});
	let uploadResult = $(".uploadResult ul");
	function showUploadedFile(uploadResultArr) {
	    var str="";
	    $(uploadResultArr).each(function (i,obj) {
            if( !obj.image){ 
            	let fileCallpath= obj.uploadPath+"/"+obj.uuid+"_"+obj.fileName;
            
           		str += "<li><a href='${app}/download?fileName=" + fileCallpath + "'>"  +
            		"<img src='${app}/resources/img/attach.png'>" + obj.fileName+"</li>";
            }else{
            	let fileCallpath=encodeURIComponent(obj.uploadPath+"/s_"+obj.uuid+"_"+obj.fileName);
            	str += "<li><img src='/display?fileName=" + fileCallpath+
          			"'></li>";
            }            
        });    
	    console.log(str);
	    uploadResult.append(str);

	}

});



</script>
</body>
</html>
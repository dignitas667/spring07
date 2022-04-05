package net.developia.spring07.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.log4j.Log4j;
import net.coobird.thumbnailator.Thumbnailator;
import net.developia.spring07.domain.AttachFileDTO;

@Controller
@Log4j
public class UploadController {

	@Value("${uploadFolder}")
	private String uploadFolder;
	
	@GetMapping("uploadForm")
	public void uploadForm() {
		log.info("upload form");
	}
	
	@PostMapping("uploadFormAction")
	public void uploadFormAction(MultipartFile[] uploadFile, Model model) {
		for(MultipartFile multipartFile: uploadFile) {
			log.info("-----------------------------------------");
			log.info("Upload File Name : " + multipartFile.getOriginalFilename());
			log.info("Upload File Size : " + multipartFile.getSize());
			
			File saveFile = new File(uploadFolder, multipartFile.getOriginalFilename());
			
			try {
				multipartFile.transferTo(saveFile);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
	}
	
	@GetMapping("uploadAjax")
	public void uploadAjax() {
		log.info("upload Ajax");
	}

	
	//폴더 이름 처리
	private String getFolder() {   //날짜 구분자 대소문자 구별
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		String str = sdf.format(date);
		return str.replace("-", File.separator);//OS구별없이 구분자로 대체		
	}//end getFolder()

	//이미지 파일 검사
	private boolean checkImageType(File file) {
		try { //파일 타입 체크
			String contenType = Files.probeContentType(file.toPath());
			log.info(contenType);
			return contenType.startsWith("image");			
		}catch (Exception e) {
			e.printStackTrace();		
		}//end try
		
		return false;		
	}//end check...

	
	@PostMapping(value = "uploadAjaxAction", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<AttachFileDTO>> uploadAjaxAction(MultipartFile[] uploadFile, Model model) {
		log.info("upload ajax post....");
		
		List<AttachFileDTO> list = new ArrayList<>();
		String uploadFolderPath = getFolder();

		File uploadPath = new File(uploadFolder, uploadFolderPath);
		log.info("uploadPath" + uploadPath);
		if(!uploadPath.exists()) {
			uploadPath.mkdirs();
		}
		
		for(MultipartFile multipartFile: uploadFile) {
			AttachFileDTO attachDTO = new AttachFileDTO();

			
			log.info("-----------------------------------------");
			log.info("Upload File Name : " + multipartFile.getOriginalFilename());
			log.info("Upload File Size : " + multipartFile.getSize());
			
			String uploadFileName = multipartFile.getOriginalFilename();
			log.info("path+file name : " + uploadFileName);
			
			// IE
			uploadFileName = uploadFileName.substring(uploadFileName.lastIndexOf("\\") + 1);
			log.info("only file name : " + uploadFileName);
			
			attachDTO.setFileName(uploadFileName); 
			
			UUID uuid = UUID.randomUUID();
			uploadFileName = uuid.toString() + "_" + uploadFileName;
			attachDTO.setUuid(uuid.toString()); 
			attachDTO.setUploadPath(uploadFolderPath); 
			
			// File saveFile = new File(uploadFolder, uploadFileName);
			File saveFile = new File(uploadPath, uploadFileName);
			
			try {
				multipartFile.transferTo(saveFile);
				log.info("contentType : " + Files.probeContentType(saveFile.toPath()));

				if( checkImageType(saveFile)) {
					attachDTO.setImage(true);
					FileOutputStream thumnail =  //파일생성
							new FileOutputStream(new File(uploadPath,"s_"+uploadFileName));
					Thumbnailator.createThumbnail(
							multipartFile.getInputStream(),thumnail, 100, 100);
					thumnail.close(); //파일 닫기					
				}//end if
				list.add(attachDTO);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
		return new ResponseEntity<>(list, HttpStatus.OK);
	}
	
	
	@GetMapping("/display")
	@ResponseBody         
	public ResponseEntity<byte[]> getFile(String fileName){
		log.info("filename : " + fileName );
		
		File file = new File("c:\\upload\\" + fileName);

		log.info("※※※※※ file : " + file);
		
		ResponseEntity<byte[]> result =null;		
		try {
			HttpHeaders headers = new HttpHeaders();			
			
			headers.add("Content-Type", Files.probeContentType(file.toPath()));
			
			result = new ResponseEntity<>(FileCopyUtils.copyToByteArray(file)
					,headers,HttpStatus.OK);			
		}catch (Exception e) {
			e.printStackTrace();			
		} 	
		return result;		
	}
	
/*
//	다운로드는 잘 되지만 IE에서는 한글 파일명에서 문제 발생	
	@GetMapping(value = "/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@ResponseBody
	public ResponseEntity<Resource> downloadFile(String fileName){
		log.info("download file: " + fileName);
		
		//리소스 객체에 파일 담기
		Resource resource = new FileSystemResource("c:\\upload\\" + fileName);
		log.info("resource: " + resource);
		//파일 이름 저장
		String resourceName = resource.getFilename();
		
		//헤더에 파일이름 저장
		HttpHeaders headers = new HttpHeaders();
		try{
			headers.add("Content-Disposition", 
					"attachment; filename=" + 
							new String(resourceName.getBytes("UTF-8") ,	"ISO-8859-1")
					);			
		}catch (Exception e) {
			e.printStackTrace();
		}//end try
		
		return new ResponseEntity<Resource>(resource,headers,HttpStatus.OK);
	
	} 
*/
	// IE 한글 문제 처리
	@GetMapping(value = "/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@ResponseBody
	public ResponseEntity<Resource> downloadFile(
			@RequestHeader("User-Agent") String userAgent,
			String fileName){
		
		log.info("download file: " + fileName);
		
		Resource resource = new FileSystemResource("c:\\upload\\" + fileName);
		log.info("resource: " + resource);
		String resourceName = resource.getFilename();
		HttpHeaders headers = new HttpHeaders();
		
		try{
			String downloadName = null;
			
			if (userAgent.contains("Trident")) {
				log.info("IE browser");
				downloadName = URLEncoder.encode(resourceName, "UTF-8").replaceAll("\\+", " ");
			} else if (userAgent.contains("Edge")) {
				log.info("Edge browser");
				downloadName = URLEncoder.encode(resourceName, "UTF-8");
				log.info("Edge name : " + downloadName);
			} else {
				log.info("Chrome browser");
				downloadName = new String(resourceName.getBytes("UTF-8"), "ISO-8859-1");
			}
			headers.add("Content-Disposition", 
					"attachment; filename=" + downloadName); 
		} catch (Exception e) {
			e.printStackTrace();
		} //end try
		
		return new ResponseEntity<Resource>(resource, headers, HttpStatus.OK);
	} 
}

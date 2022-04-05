package net.developia.spring07.controller;

import java.io.File;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.log4j.Log4j;

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

	@PostMapping("uploadAjaxAction")
	public void uploadAjaxAction(MultipartFile[] uploadFile, Model model) {
		log.info("upload ajax post....");
		for(MultipartFile multipartFile: uploadFile) {
			log.info("-----------------------------------------");
			log.info("Upload File Name : " + multipartFile.getOriginalFilename());
			log.info("Upload File Size : " + multipartFile.getSize());
			
			String uploadFileName = multipartFile.getOriginalFilename();
			log.info("path+file name : " + uploadFileName);
			
			// IE
			uploadFileName = uploadFileName.substring(uploadFileName.lastIndexOf("\\") + 1);
			log.info("only file name : " + uploadFileName);
			
			File saveFile = new File(uploadFolder, uploadFileName);
			
			try {
				multipartFile.transferTo(saveFile);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
	}

}

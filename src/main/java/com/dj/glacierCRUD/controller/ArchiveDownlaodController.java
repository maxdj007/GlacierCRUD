package com.dj.glacierCRUD.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.services.glacier.model.ResourceNotFoundException;
import com.dj.glacierCRUD.payload.ArchiveRetrieveResponseTwo;
import com.dj.glacierCRUD.payload.GetArchiveByIdRequest;
import com.dj.glacierCRUD.service.ArchiveDownloadService;
import com.dj.glacierCRUD.service.FileStorageService;
import com.dj.glacierCRUD.util.Constants;

@RestController
public class ArchiveDownlaodController {
	
	private static final Logger logger = LoggerFactory.getLogger(ArchiveDownlaodController.class);


	@Autowired
	private FileStorageService fileStorageService;
	
	@Autowired
	private ArchiveDownloadService archiveDownloadService;
	
	@PostMapping("/getArchive")
	public ArchiveRetrieveResponseTwo getArchive(@RequestBody GetArchiveByIdRequest getArchiveRequest){
		
		System.out.println("vault: "+getArchiveRequest.getVaultName()+" aID: "+ getArchiveRequest.getArchiveId()+" Access Key :"+getArchiveRequest.getAccesskey()+" SecretKey : "+getArchiveRequest.getSecretKey());
		try{
			String jobId = archiveDownloadService.ArchiveRetrieval(getArchiveRequest);
			return new ArchiveRetrieveResponseTwo(Constants.SUCCESS_FLAG, Constants.SUCCESS_RETRIEVAL_MESSAGE, false, jobId, "");
		} catch(ResourceNotFoundException ex){
			return new ArchiveRetrieveResponseTwo(Constants.FAILURE_FLAG, Constants.FAILURE_RETRIEVAL_MESSAGE, true, "", ex.getMessage());
		} catch(RuntimeException en){
			return new ArchiveRetrieveResponseTwo(Constants.ERROR_FLAG, Constants.ERROR_RETRIEVAL_MESSAGE, true, "", en.getMessage());
		}
		
	}
	
	
	@GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
	
}

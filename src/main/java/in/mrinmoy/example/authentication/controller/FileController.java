package in.mrinmoy.example.authentication.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import in.mrinmoy.example.authentication.exception.CustomException;
import in.mrinmoy.example.authentication.model.MultipleFileResponse;
import in.mrinmoy.example.authentication.model.UploadFileResponse;
import in.mrinmoy.example.authentication.service.FileService;
import in.mrinmoy.example.authentication.util.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = { RequestMethod.DELETE, RequestMethod.GET, RequestMethod.HEAD, RequestMethod.OPTIONS,
        RequestMethod.PATCH, RequestMethod.POST, RequestMethod.PUT })
public class FileController {
    @Autowired
    private FileService fileService;

    @PostMapping("/uploadFile")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            UploadFileResponse uploadFileResponse = fileService.uploadFile(file);
            return ResponseEntity.ok(uploadFileResponse);
        } catch (CustomException e) {
            e.printStackTrace();
            return ExceptionUtil.getExceptionResponse(e);
        }
    }

    @PostMapping("/uploadMultipleFiles")
    public ResponseEntity<?> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        List<String> failedFiles = new ArrayList<>();
        List<UploadFileResponse> uploadFileResponses = Arrays.asList(files)
            .stream()
            .map(file -> {
                try {
                    return fileService.uploadFile(file);
                } catch (CustomException e) {
                    failedFiles.add(file.getName());
                }
                return null;
            }).filter(Objects::nonNull)
            .collect(Collectors.toList());
        if (failedFiles.isEmpty())
            return ResponseEntity.ok(uploadFileResponses);
        else
            return new ResponseEntity<>(MultipleFileResponse.builder().failedFiles(failedFiles).uploadFileResponses(uploadFileResponses).build(),
                HttpStatus.MULTI_STATUS);
    }

    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<?> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = null;
        try {
            resource = fileService.loadFileAsResource(fileName);
            // Try to determine file's content type
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                log.info("Could not determine file type.");
            }

            // Fallback to the default content type if type could not be determined
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
        } catch (CustomException e) {
            e.printStackTrace();
            return ExceptionUtil.getExceptionResponse(e);
        }
    }
}

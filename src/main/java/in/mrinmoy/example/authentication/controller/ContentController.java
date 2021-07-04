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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import in.mrinmoy.example.authentication.exception.CustomException;
import in.mrinmoy.example.authentication.model.ImageResponse;
import in.mrinmoy.example.authentication.model.MultiImageUploadResponse;
import in.mrinmoy.example.authentication.model.StatusResponse;
import in.mrinmoy.example.authentication.service.ContentService;
import in.mrinmoy.example.authentication.service.FileService;
import in.mrinmoy.example.authentication.util.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = { RequestMethod.DELETE, RequestMethod.GET, RequestMethod.HEAD, RequestMethod.OPTIONS,
        RequestMethod.PATCH, RequestMethod.POST, RequestMethod.PUT })
@RequestMapping(value = "/content")
public class ContentController {
    @Autowired
    private FileService fileService;
    @Autowired
    private ContentService contentService;

    @PostMapping("/uploadFile")
    public ResponseEntity<?> uploadFile(HttpServletRequest request, @RequestParam("file") MultipartFile file, @RequestParam("subType") String subType) {
        try {
            return ResponseEntity.ok(contentService.upload(request, file, subType));
        } catch (CustomException e) {
            e.printStackTrace();
            return ExceptionUtil.getExceptionResponse(e);
        }
    }

    @PostMapping("/uploadMultipleFiles")
    public ResponseEntity<?> uploadMultipleFiles(HttpServletRequest request, @RequestParam("files") MultipartFile[] files, @RequestParam("subType") String subType) {
        List<String> failedFiles = new ArrayList<>();
        List<ImageResponse> uploadFileResponses = Arrays.stream(files)
            .map(file -> {
                try {
                    return contentService.upload(request, file, subType);
                } catch (CustomException e) {
                    failedFiles.add(file.getName());
                }
                return null;
            }).filter(Objects::nonNull)
            .collect(Collectors.toList());
        return ResponseEntity.ok(MultiImageUploadResponse.builder().failedResources(failedFiles).successfulResources(uploadFileResponses).build());
    }

    @GetMapping(value = "/{referralCode}/{imageId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody
    byte[] downloadImage(HttpServletRequest request, @PathVariable("referralCode") String referralCode, @PathVariable("imageId") String imageId) throws CustomException {
        try {
            return contentService.retrieveContent(request, referralCode, imageId);
        } catch (CustomException e) {
            log.error("Exception occur while fetching content for outside users: ", e);
            throw new CustomException("Not found");
        }
    }

    @GetMapping(value = "/{imageId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody
    byte[] downloadImage(@PathVariable("imageId") String imageId) throws CustomException {
        try {
            return contentService.retrieveContent(imageId);
        } catch (CustomException e) {
            log.error("Exception occur while fetching content for outside users: ", e);
            throw new CustomException("Not found");
        }
    }

    @DeleteMapping(value = "/{imageId}")
    public ResponseEntity<?> deleteContent(HttpServletRequest request, @PathVariable("imageId") String imageId) throws CustomException {
        try {
            contentService.deleteContent(request, imageId);
            return ResponseEntity.ok(StatusResponse.builder().status("Content with id : "+imageId+" successfully deleted").build());
        } catch (CustomException e) {
            log.error("Exception occur while fetching content for outside users: ", e);
            return ExceptionUtil.getExceptionResponse(e);
        }
    }

    @GetMapping(value = "/getAll")
    public ResponseEntity<?> downloadImage(@RequestParam(name = "page", defaultValue = "0") int page,
                                           @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(contentService.retrieveAllContents(page, size));
    }

    @PostMapping("/registerDownload/{imageId}")
    public ResponseEntity<?> downloadContent(HttpServletRequest request, @PathVariable("imageId") String imageId) {
        try {
            return ResponseEntity.ok(contentService.downloadContent(request, imageId));
        } catch (CustomException e) {
            e.printStackTrace();
            return ExceptionUtil.getExceptionResponse(e);
        }
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

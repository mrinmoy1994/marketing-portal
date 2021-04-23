package in.mrinmoy.example.authentication.service;

import in.mrinmoy.example.authentication.exception.CustomException;
import in.mrinmoy.example.authentication.model.UploadFileResponse;
import in.mrinmoy.example.authentication.util.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@Slf4j
public class FileService {

    private Path fileStorageLocation;

    @Autowired
    public void FileStorageService() throws CustomException {
        try {
            File fileDirectory = new File("Uploaded Files");
            if (fileDirectory.exists() || fileDirectory.mkdir())
                this.fileStorageLocation = Paths.get(fileDirectory.getAbsolutePath());
        } catch (Exception ex) {
            log.error("Could not create the directory where the uploaded files will be stored.", ex);
            //throw ExceptionUtil.getException("Could not create the directory where the uploaded files will be stored.","Check logs for more details", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public UploadFileResponse uploadFile(MultipartFile file) throws CustomException {
        String fileName = storeFile(file);
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();

        return new UploadFileResponse(fileName, fileDownloadUri, file.getContentType(), file.getSize());
    }

    public String storeFile(MultipartFile file) throws CustomException {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        // Check if the file's name contains invalid characters
        if (fileName.contains("..")) {
            log.error("Sorry! Filename contains invalid path sequence " + fileName);
            throw ExceptionUtil.getException("Sorry! Filename contains invalid path sequence " + fileName, "Provide valid file path", HttpStatus.BAD_REQUEST);
        }

        // Copy file to the target location (Replacing existing file with the same name)
        Path targetLocation = this.fileStorageLocation.resolve(fileName);
        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Internal exception occurred", e);
            throw ExceptionUtil.getException("Internal exception occurred", "Check logs for more details", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return fileName;

    }

    public Resource loadFileAsResource(String fileName) throws CustomException {
        Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
        Resource resource = null;
        try {
            resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                log.error("File not found " + fileName);
                throw ExceptionUtil.getException("File not found " + fileName, null, HttpStatus.NOT_FOUND);
            }
        } catch (MalformedURLException e) {
            log.error("File not found " + fileName, e);
            throw ExceptionUtil.getException("File not found " + fileName, null, HttpStatus.NOT_FOUND);
        }
    }
}

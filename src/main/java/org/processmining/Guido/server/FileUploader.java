package org.processmining.Guido.server;

import org.processmining.Guido.ConformanceChecker;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*")
@Controller
public class FileUploader {

    private final Path modelLocation = Paths.get("./data");
    private final Path customElementsLocation = Paths.get("./data");
    private final Path logLocation = Paths.get("./data");

    ConformanceChecker cc = Database.getConformanceChecker();

    @PostMapping("/uploadModel")
    public ResponseEntity<String> uploadModel(@RequestParam("file") MultipartFile file) {
        try {
            try {
                Files.copy(file.getInputStream(), this.modelLocation.resolve(file.getOriginalFilename()), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                throw new RuntimeException("FAIL!");
            }
            File model = new File(String.valueOf(this.modelLocation.resolve(file.getOriginalFilename())));

            cc.setModelBpmn(model);

            return ResponseEntity.status(HttpStatus.OK).body("Successfully uploaded!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Failed to upload!");
        }
    }

    @PostMapping("/uploadCustomElements")
    public ResponseEntity<String> uploadCustomElements(@RequestParam("file") MultipartFile file) {
        try {
            try {
                Files.copy(file.getInputStream(), this.customElementsLocation.resolve(file.getOriginalFilename()), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                throw new RuntimeException("FAIL!");
            }
            File customElements = new File(String.valueOf(this.customElementsLocation.resolve(file.getOriginalFilename())));
            cc.setCustomElements(customElements);

            return ResponseEntity.status(HttpStatus.OK).body("Successfully uploaded!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Failed to upload!");
        }
    }

    @PostMapping("/uploadLog")
    public ResponseEntity<String> uploadLog(@RequestParam("file") MultipartFile file) {
        try {
            try {
                Files.copy(file.getInputStream(), this.logLocation.resolve(file.getOriginalFilename()), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                throw new RuntimeException("FAIL!");
            }
            File log = new File(String.valueOf(this.logLocation.resolve(file.getOriginalFilename())));
            cc.setLog(log);

            return ResponseEntity.status(HttpStatus.OK).body("Successfully uploaded!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Failed to upload!");
        }
    }
}
package com.nhathuy.docker.deployspring.controller;

import com.nhathuy.docker.deployspring.model.Product;
import com.nhathuy.docker.deployspring.model.ProductDTO;
import com.nhathuy.docker.deployspring.repository.ProductRepository;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/products")
public class ProductRestController {

    @Autowired
    private ProductRepository repo;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
        return ResponseEntity.ok(products);
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductDTO productDTO){
        if (productDTO.getImageFile() == null || productDTO.getImageFile().isEmpty()) {
            return ResponseEntity.badRequest().body("The image file is required");
        }

        String storageFileName = saveImage(productDTO.getImageFile());
        if (storageFileName == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save image");
        }

        Product product = new Product();
        product.setName(productDTO.getName());
        product.setBrand(productDTO.getBrand());
        product.setCategory(productDTO.getCategory());
        product.setPrice(productDTO.getPrice());
        product.setDescription(productDTO.getDescription());
        product.setImageFileName(storageFileName);

        Product savedProduct = repo.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    private String saveImage(MultipartFile image) {
        UUID uuid = UUID.randomUUID();
        String storageFileName = uuid + "_" + image.getOriginalFilename();

        try {
            String uploadDir = "public/images/";
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            try (InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
            }
            return storageFileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id){
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDTO productDTO) {
        return repo.findById(id)
                .map(product -> {
                    product.setName(productDTO.getName());
                    product.setBrand(productDTO.getBrand());
                    product.setCategory(productDTO.getCategory());
                    product.setPrice(productDTO.getPrice());
                    product.setDescription(productDTO.getDescription());

                    if (productDTO.getImageFile() != null && !productDTO.getImageFile().isEmpty()) {
                        String storageFileName = saveImage(productDTO.getImageFile());
                        if (storageFileName != null) {
                            product.setImageFileName(storageFileName);
                        }
                    }

                    Product updatedProduct = repo.save(product);
                    return ResponseEntity.ok(updatedProduct);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id){
        return repo.findById(id)
                .map(product -> {
                    deleteImage(product.getImageFileName());
                    repo.delete(product);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private void deleteImage(String fileName) {
        try {
            String imagePath = "public/images/" + fileName;
            Files.deleteIfExists(Paths.get(imagePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

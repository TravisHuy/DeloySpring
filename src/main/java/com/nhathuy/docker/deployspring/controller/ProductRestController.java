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
    public ResponseEntity<?> createProduct(@RequestBody Product product){
        if (product.getImageFileName() == null || product.getImageFileName().isEmpty()) {
            return ResponseEntity.badRequest().body("The image file is required");
        }
        Product savedProduct = repo.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id){
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id,@RequestBody Product product) {
        return repo.findById(id)
                .map(p -> {
                    p.setName(product.getName());
                    p.setBrand(product.getBrand());
                    p.setCategory(product.getCategory());
                    p.setPrice(product.getPrice());
                    p.setDescription(product.getDescription());

                    if(product.getImageFileName()!=null && !product.getImageFileName().isEmpty()){
                        p.setImageFileName(product.getImageFileName());
                    }
                    Product updatedProduct = repo.save(p);
                    return ResponseEntity.ok(updatedProduct);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id){
        return repo.findById(id)
                .map(product -> {
                    repo.delete(product);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}

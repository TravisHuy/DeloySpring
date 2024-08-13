package com.nhathuy.docker.deployspring.controller;

import com.nhathuy.docker.deployspring.model.Product;
import com.nhathuy.docker.deployspring.model.ProductDTO;
import com.nhathuy.docker.deployspring.repository.ProductRepository;
import com.nhathuy.docker.deployspring.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductRepository repo;
    @Autowired
    private ProductRepository productRepository;

    @GetMapping({"","/"})
    public String showProductList(Model model) {
        List<Product> products = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("products", products);
        return "products/index";
    }
    @GetMapping("/create")
    public String showCreatPage(Model model) {
        ProductDTO productDto= new ProductDTO();
        model.addAttribute("productDto", productDto);
        return "products/createProduct";
    }

    @PostMapping("/create")
    public String createProduct(@Valid @ModelAttribute("productDto") ProductDTO productDTO,
                                BindingResult result,
                                Model model) {
        if (productDTO.getImageFile().isEmpty()) {
            result.rejectValue("imageFile", "error.imageFile", "The image file is required");
        }

        if (result.hasErrors()) {
            return "products/createProduct";
        }

        MultipartFile image=productDTO.getImageFile();
        UUID uuid= UUID.randomUUID();
        String storageFileName= uuid+ "_"+image.getOriginalFilename();

        try {
            String uploadDir="public/images/";
            Path uploadPath= Paths.get(uploadDir);

            if(!Files.exists(uploadPath)){
                Files.createDirectories(uploadPath);
            }

            try(InputStream inputStream=image.getInputStream()){
                Files.copy(inputStream,Paths.get(uploadDir+storageFileName), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (Exception e){
            System.out.println("Exception: "+e.getMessage());
        }

        Product product = new Product();
        product.setName(productDTO.getName());
        product.setBrand(productDTO.getBrand());
        product.setCategory(productDTO.getCategory());
        product.setPrice(productDTO.getPrice());
        product.setDescription(productDTO.getDescription());

        product.setImageFileName(storageFileName);
        // Assuming you have a method in ProductService to handle file upload and saving
        repo.save(product);

        return "redirect:/products";
    }
    @GetMapping("/edit")
    public String showEditPage(Model model, @RequestParam("id") Long id) {
        try {
            Product product = repo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            model.addAttribute("product", product);

            ProductDTO productDTO = new ProductDTO();

            productDTO.setName(product.getName());
            productDTO.setBrand(product.getBrand());
            productDTO.setCategory(product.getCategory());
            productDTO.setPrice(product.getPrice());
            productDTO.setDescription(product.getDescription());

            model.addAttribute("productDto", productDTO);

            return "products/editProduct";
        } catch (Exception e) {
            // Log the exception
            e.printStackTrace();
            // Add an error message to the model
            model.addAttribute("errorMessage", "An error occurred while fetching the product.");
            return "error"; // Create an error.html template to display errors
        }
    }

}
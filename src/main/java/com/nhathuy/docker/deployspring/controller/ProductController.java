package com.nhathuy.docker.deployspring.controller;


import com.nhathuy.docker.deployspring.model.Product;
import com.nhathuy.docker.deployspring.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Controller
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @Value("${product.image.upload.dir}")
    private String uploadDir;

    @GetMapping
    public String listProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "products/list";
    }
    

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        return "products/add";
    }

    @PostMapping("/add")
    public String addProduct(@ModelAttribute Product product,@RequestParam("imageFile")MultipartFile multipartFile) {
        saveImage(product,multipartFile);
        productService.saveProduct(product);
        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "products/edit";
    }

    @PostMapping("/edit")
    public String editProduct(@ModelAttribute Product product, @RequestParam("imageFile")MultipartFile multipartFile) {
        saveImage(product,multipartFile);
        productService.saveProduct(product);
        return "redirect:/products";
    }
    @DeleteMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/products";
    }

    private void saveImage(Product product, MultipartFile imageFile) {
        if(!imageFile.isEmpty()){
            try{
                String fileName= imageFile.getOriginalFilename();
                Path path= Paths.get(uploadDir+fileName);
                Files.copy(imageFile.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
                product.setImage(fileName);
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
    }

}
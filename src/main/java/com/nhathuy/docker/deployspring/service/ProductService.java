package com.nhathuy.docker.deployspring.service;

import com.nhathuy.docker.deployspring.model.Product;
import com.nhathuy.docker.deployspring.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    @Autowired
    ProductRepository repository;

    public List<Product> getAllProduct(){
        return repository.findAll();
    }
}

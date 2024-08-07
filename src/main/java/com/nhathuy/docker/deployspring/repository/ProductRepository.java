package com.nhathuy.docker.deployspring.repository;

import com.nhathuy.docker.deployspring.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {

}

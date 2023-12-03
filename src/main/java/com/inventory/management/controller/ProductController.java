package com.inventory.management.controller;

import com.inventory.management.entity.Product;
import com.inventory.management.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/products")
@Slf4j
public class ProductController {
    @Autowired
    private ProductService service;

    @PostMapping(value = "/addProduct" ,produces =APPLICATION_JSON_VALUE)
    public ResponseEntity<Product> addProduct(Product product){
        return service.addProduct(product);
    }

    @PostMapping(value = "/deleteProduct" ,produces =APPLICATION_JSON_VALUE)
    public ResponseEntity<String> deleteProduct(Product product){
        return service.deleteProduct(product.getProductId());
    }

  //  @PostMapping(value = "/updateProduct" ,produces =APPLICATION_JSON_VALUE)
  //  public ResponseEntity<String> updateProduct(Product product){
   //     return service.updateProduct(product);
   // }

    /*@GetMapping(value = "/products" ,produces =APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Product>> getAllProducts(){
        List<Product> products = service.getAllProducts();
        return ResponseEntity.ok(products);
    }*/
}
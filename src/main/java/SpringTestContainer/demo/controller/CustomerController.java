package SpringTestContainer.demo.controller;


import SpringTestContainer.demo.entity.Customer;
import SpringTestContainer.demo.repository.CustomerRepository;
import SpringTestContainer.demo.service.customerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CustomerController {
    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    customerService customerService;

    @GetMapping("/customers")
    List<Customer> getAll() {
        return customerRepository.findAll();
    }


    @GetMapping("/customers/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        Customer customer = customerService.getCustomerById(id);
        if (customer != null) {
            return ResponseEntity.ok(customer);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}

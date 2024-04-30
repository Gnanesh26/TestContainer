package SpringTestContainer.demo.service;

import SpringTestContainer.demo.entity.Customer;
import SpringTestContainer.demo.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.beans.JavaBean;

@Service
public class customerService {

    @Autowired
    CustomerRepository customerRepository ;

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id).orElse(null);
    }

}

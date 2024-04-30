package SpringTestContainer.demo.service;

import SpringTestContainer.demo.entity.Customer;
import SpringTestContainer.demo.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.beans.JavaBean;
import java.util.Optional;

@Service
public class customerService {

    @Autowired
    CustomerRepository customerRepository ;

//    public Customer addCustomer(Customer customer) {
//        return customerRepository.save(customer);
//    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id).orElse(null);
    }

//    public Customer updateCustomer(Long id, Customer customer) {
//        if (customerRepository.existsById(id)) {
//            customer.setId(id);
//            return customerRepository.save(customer);
//        }
//        return null;
//    }

//    public void deleteCustomer(Long id) {
//        customerRepository.deleteById(id);
//    }

    public Customer addCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Long id, Customer customer) {
        Optional<Customer> optionalCustomer = customerRepository.findById(id);
        if (optionalCustomer.isPresent()) {
            Customer existingCustomer = optionalCustomer.get();
            existingCustomer.setName(customer.getName());
            existingCustomer.setEmail(customer.getEmail());
            return customerRepository.save(existingCustomer);
        } else {
            return null;
        }
    }

    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }
}

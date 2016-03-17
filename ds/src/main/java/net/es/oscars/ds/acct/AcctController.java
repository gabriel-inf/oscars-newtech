package net.es.oscars.ds.acct;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.ds.acct.ent.ECustomer;
import net.es.oscars.ds.acct.svc.CustService;
import net.es.oscars.dto.acct.Customer;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Controller
public class AcctController {

    @Autowired
    private CustService custService;

    @Autowired
    private ModelMapper modelMapper;

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        // LOG.warn("user requested a strResource which didn't exist", ex);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public void handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        // LOG.warn("user requested a strResource which didn't exist", ex);
    }

    @RequestMapping(value = "/acct/customers/", method = RequestMethod.GET)
    @ResponseBody
    public List<Customer> listCustomers() {
        log.info("listing all customers");
        List<Customer> customers = new ArrayList<>();

        for (ECustomer eCustomer : custService.findAll()) {
            Customer customer = convertToDto(eCustomer);
            customers.add(customer);
        }
        return customers;
    }

    @RequestMapping(value = "/acct/customers/{name}", method = RequestMethod.GET)
    @ResponseBody
    public Customer custByName(@PathVariable("name") String name) {
        log.info("getting customer " + name);

        return convertToDto(custService.findByName(name).orElseThrow(NoSuchElementException::new));
    }

    @RequestMapping(value = "/acct/customers/update", method = RequestMethod.POST)
    @ResponseBody
    public Customer update(@RequestBody Customer dtoCustomer) {
        ECustomer eCustomer = custService.findByName(dtoCustomer.getName()).orElseThrow(NoSuchElementException::new);

        Long id = eCustomer.getId();
        eCustomer = convertToEnt(dtoCustomer);
        eCustomer.setId(id);

        custService.save(eCustomer);
        return dtoCustomer;
    }

    @RequestMapping(value = "/acct/customers/delete/{username}", method = RequestMethod.GET)
    @ResponseBody
    public String delete(@PathVariable("name") String name) {

        custService.delete(custService.findByName(name).orElseThrow(NoSuchElementException::new));
        return "User deleted.";
    }

    private ECustomer convertToEnt(Customer dtoCustomer) {
        return modelMapper.map(dtoCustomer, ECustomer.class);
    }

    private Customer convertToDto(ECustomer eCustomer) {
        return modelMapper.map(eCustomer, Customer.class);
    }

}
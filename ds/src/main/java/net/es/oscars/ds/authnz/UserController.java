package net.es.oscars.ds.authnz;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.ds.authnz.dao.UserRepository;
import net.es.oscars.ds.authnz.ent.EUser;
import net.es.oscars.dto.auth.Permissions;
import net.es.oscars.dto.auth.User;
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
public class UserController {
    @Autowired
    private UserRepository repository;

    @Autowired
    private ModelMapper modelMapper;


    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex)
    {
        // LOG.warn("user requested a strResource which didn't exist", ex);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public void handleDataIntegrityViolationException(DataIntegrityViolationException  ex)
    {
        // LOG.warn("user requested a strResource which didn't exist", ex);
    }

    @RequestMapping(value = "/users/add", method = RequestMethod.POST)
    @ResponseBody
    public User add(@RequestBody User dtoUser) {
        if (repository.findByUsername(dtoUser.getUsername()).isPresent()) {
            throw new DataIntegrityViolationException("User already exists.");
        }

        EUser eUser = convertToEnt(dtoUser);
        repository.save(eUser);
        return dtoUser;
    }


    @RequestMapping(value = "/users/update", method = RequestMethod.POST)
    @ResponseBody
    public User update(@RequestBody User dtoUser) {
        EUser eUser = repository.findByUsername(dtoUser.getUsername()).orElseThrow(NoSuchElementException::new);

        // save the internal id
        Long id = eUser.getId();
        eUser = convertToEnt(dtoUser);
        eUser.setId(id);

        repository.save(eUser);
        return dtoUser;
    }

    @RequestMapping(value = "/users/delete/{username}", method = RequestMethod.GET)
    @ResponseBody
    public String delete(@PathVariable("username") String username) {

        repository.delete(repository.findByUsername(username).orElseThrow(NoSuchElementException::new));
        return "User deleted.";
    }


    @RequestMapping(value = "/users/", method = RequestMethod.GET)
    @ResponseBody
    public List<User> getAll() {
        List<EUser> eUsers = repository.findAll();
        List<User> result = new ArrayList<>();

        for (EUser eUser : eUsers) {
            User dtoUser = convertToDto(eUser);
            result.add(dtoUser);
        }
        return result;
    }


    @RequestMapping(value = "/users/institutions", method = RequestMethod.GET)
    @ResponseBody
    public List<String> getInstitutions() {
        List<EUser> eUsers = repository.findAll();
        List<String> result = new ArrayList<>();

        for (EUser eUserUser : eUsers) {
            String inst = eUserUser.getInstitution();
            if (inst != null && ! inst.equals("")) {
                result.add(inst);
            }
        }
        return result;
    }

    @RequestMapping(value = "/users/byCertSubject", method = RequestMethod.POST)
    @ResponseBody
    public User byCertSubject(@RequestBody String certSubject) {

        return convertToDto(repository.findByCertSubject(certSubject).orElseThrow(NoSuchElementException::new));

    }


    @RequestMapping(value = "/users/get/{username}", method = RequestMethod.GET)
    @ResponseBody
    public User byUsername(@PathVariable("username") String username) {
        return convertToDto(repository.findByUsername(username).orElseThrow(NoSuchElementException::new));
    }

    private EUser convertToEnt(User dtoUser) {
        if (dtoUser.getPermissions() == null) {
            dtoUser.setPermissions(new Permissions());
        }
        EUser EUser = modelMapper.map(dtoUser, EUser.class);
        return EUser;
    }

    private User convertToDto(EUser eUser) {
        User dtoUser = modelMapper.map(eUser, User.class);
        return dtoUser;
    }
}
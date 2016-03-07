package net.es.oscars.ds.resv;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.ds.resv.dao.ReservedStrRepository;
import net.es.oscars.ds.resv.ent.EConnection;
import net.es.oscars.ds.resv.ent.EReservedString;
import net.es.oscars.ds.resv.svc.ResvService;
import net.es.oscars.dto.resv.Connection;
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
public class ResvController {
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ResvService service;

    @Autowired
    private ReservedStrRepository strRepo;

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        // LOG.warn("user requested a resource which didn't exist", ex);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public void handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        // LOG.warn("user requested a resource which didn't exist", ex);
    }



    @RequestMapping(value = "/resvs/gri/{gri}", method = RequestMethod.GET)
    @ResponseBody
    public Connection getResv(@PathVariable("gri") String gri) {
        log.info("retrieving " + gri);

        return convertToDto(service.findByGri(gri).orElseThrow(NoSuchElementException::new));

    }

    @RequestMapping(value = "/resvs", method = RequestMethod.GET)
    @ResponseBody
    public List<Connection> listResvs() {

        log.info("listing all resvs");
        List<Connection> dtoItems = new ArrayList<>();

        for (EConnection eItem : service.findAll()) {
            Connection dtoItem = convertToDto(eItem);
            dtoItems.add(dtoItem);
        }
        return dtoItems;

    }




    @RequestMapping(value = "/resvs/urn/", method = RequestMethod.GET)
    @ResponseBody
    public List<EReservedString> urnReserved() {

        log.info("returning all reserved items on all urns");
        //TODO : fix
        List<EReservedString> dtoItems = strRepo.findAll();
        if (dtoItems.isEmpty()) {
            log.info("empty list!");
        }
        for (EReservedString urnReserved : dtoItems) {
            log.info(urnReserved.toString());
        }
        return dtoItems;

    }


    private EConnection convertToEnt(Connection dtoResv) {
        return modelMapper.map(dtoResv, EConnection.class);
    }

    private Connection convertToDto(EConnection eResv) {
        return modelMapper.map(eResv, Connection.class);
    }

}
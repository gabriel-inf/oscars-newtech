package net.es.oscars.ds.resv;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.ds.resv.ent.EReservation;
import net.es.oscars.ds.resv.svc.ResvService;
import net.es.oscars.dto.resv.Reservation;
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
    public Reservation getResv(@PathVariable("gri") String gri) {
        log.info("retrieving " + gri);

        return convertToDto(service.findByGri(gri).orElseThrow(NoSuchElementException::new));

    }

    @RequestMapping(value = "/resvs", method = RequestMethod.GET)
    @ResponseBody
    public List<Reservation> listResvs() {

        log.info("listing all resvs");
        List<Reservation> dtoItems = new ArrayList<>();

        for (EReservation eItem : service.findAll()) {
            Reservation dtoItem = convertToDto(eItem);
            dtoItems.add(dtoItem);
        }
        return dtoItems;

    }


    @RequestMapping(value = "/resvs/r_state/{r_state}", method = RequestMethod.GET)
    @ResponseBody
    public List<Reservation> r_stateResvs(@PathVariable("r_state") String r_state) {

        log.info("listing resvs with r_state " + r_state);
        List<Reservation> dtoItems = new ArrayList<>();

        for (EReservation eItem : service.byResvState(r_state)) {
            Reservation dtoItem = convertToDto(eItem);
            dtoItems.add(dtoItem);
        }
        return dtoItems;

    }

    private EReservation convertToEnt(Reservation dtoResv) {
        return modelMapper.map(dtoResv, EReservation.class);
    }

    private Reservation convertToDto(EReservation eResv) {
        return modelMapper.map(eResv, Reservation.class);
    }

}
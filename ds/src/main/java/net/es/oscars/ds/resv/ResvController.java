package net.es.oscars.ds.resv;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.common.resv.ResourceType;
import net.es.oscars.ds.resv.dao.ReservedResourceRepository;
import net.es.oscars.ds.resv.ent.EConnection;
import net.es.oscars.ds.resv.ent.EReservedResource;
import net.es.oscars.ds.resv.svc.ResvService;
import net.es.oscars.ds.topo.dao.DeviceRepository;
import net.es.oscars.ds.topo.ent.EDevice;
import net.es.oscars.dto.resv.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class ResvController {
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ResvService service;

    @Autowired
    private ReservedResourceRepository resRepo;

    @Autowired
    private DeviceRepository devRepo;


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

    // TODO: make better
    @RequestMapping(value = "/resvs/gri/{gri}", method = RequestMethod.GET)
    @ResponseBody
    public Connection getResv(@PathVariable("gri") String gri) {
        log.info("retrieving " + gri);

        return convertConnToDto(service.findByGri(gri).orElseThrow(NoSuchElementException::new));

    }

    // TODO: make better
    @RequestMapping(value = "/resvs", method = RequestMethod.GET)
    @ResponseBody
    public List<Connection> listResvs() {

        log.info("listing all resvs");
        List<Connection> dtoItems = new ArrayList<>();

        for (EConnection eItem : service.findAll()) {
            Connection dtoItem = convertConnToDto(eItem);
            dtoItems.add(dtoItem);
        }
        return dtoItems;

    }


    @RequestMapping(
            value = "/queryReserved", method = RequestMethod.GET,
            params = { "beginning", "ending" }
    )
    @ResponseBody
    public ReservedResponse reservedResources(@RequestParam Instant beginning, @RequestParam Instant ending) {
        log.info("reserved resources start");

        List<EReservedResource> eReservedResources = resRepo.findOverlappingInterval(beginning, ending).orElse(new ArrayList<>());

        List<ReservedResource> rsrcs = eReservedResources.stream().map(this::convertRStoDTO).collect(Collectors.toList());

        return ReservedResponse.builder().reservedResources(rsrcs).build();
    }


    public List<String> findAllUrns() {
        List<String> urns = new ArrayList<>();
        List<EDevice> devices = devRepo.findAll();

        devices.stream()
                .forEach(d -> {
                    urns.add(d.getUrn());
                    d.getIfces().stream()
                            .forEach(i -> urns.add(i.getUrn()));
                });


        return urns;
    }

    private ReservedResource convertRStoDTO(EReservedResource eRs) {
        return modelMapper.map(eRs, ReservedResource.class);
    }


    private Connection convertConnToDto(EConnection eResv) {
        return modelMapper.map(eResv, Connection.class);
    }

}
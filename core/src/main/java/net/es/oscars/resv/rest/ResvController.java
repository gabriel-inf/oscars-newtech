package net.es.oscars.resv.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.EthPipeType;
import net.es.oscars.dto.resv.Connection;
import net.es.oscars.dto.resv.ConnectionFilter;
import net.es.oscars.dto.spec.PalindromicType;
import net.es.oscars.dto.spec.RequestedVlanFlow;
import net.es.oscars.dto.spec.RequestedVlanPipe;
import net.es.oscars.dto.spec.SurvivabilityType;
import net.es.oscars.pce.exc.DuplicateConnectionIdException;
import net.es.oscars.pce.exc.InvalidUrnException;
import net.es.oscars.pce.exc.PCEException;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.ent.ConnectionE;
import net.es.oscars.resv.svc.ResvService;
import net.es.oscars.st.resv.ResvState;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@Controller
public class ResvController {
    private ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public ResvController(ResvService resvService) {
        this.resvService = resvService;
    }


    private ResvService resvService;


    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        // LOG.warn("user requested a strResource which didn't exist", ex);
    }

    @ExceptionHandler(DuplicateConnectionIdException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT ,reason="Duplicate connection id")
    @ResponseBody
    public Map<String,Object> handleDuplicateConnId(DuplicateConnectionIdException ex) {
        HashMap<String, Object> result = new HashMap<>();
        result.put("error", true);
        result.put("error_message", ex.getMessage());
        log.error(ex.getMessage());
        return result;
    }

    @ExceptionHandler(InvalidUrnException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    public Map<String,Object> handleInvalidUrn(InvalidUrnException ex) {
        String message = "One or more requested URNs not found: "+ StringUtils.join(ex.getBadUrns(), ",");

        HashMap<String, Object> result = new HashMap<>();
        result.put("error", true);
        result.put("error_message", message);
        log.error(message);
        return result;
    }



    @RequestMapping(value = "/resv/get/{connectionId}", method = RequestMethod.GET)
    @ResponseBody
    public Connection getResv(@PathVariable("connectionId") String connectionId) {
        log.info("retrieving " + connectionId);

        return convertConnToDto(resvService.findByConnectionId(connectionId).orElseThrow(NoSuchElementException::new));

    }

    // TODO: make better
    @RequestMapping(value = "/resv/all", method = RequestMethod.GET)
    @ResponseBody
    public List<Connection> allResvs() {

        log.info("listing all resvs");
        List<Connection> dtoItems = new ArrayList<>();

        for (ConnectionE eItem : resvService.findAll()) {
            Connection dtoItem = convertConnToDto(eItem);
            dtoItems.add(dtoItem);
        }
        return dtoItems;

    }


    @RequestMapping(value = "/resv/filter", method = RequestMethod.POST)
    @ResponseBody
    public Set<Connection> resvFilter(@RequestBody ConnectionFilter filter)
    {
        Set<Connection> result = new HashSet<>();
        if (filter.getConnectionId() != null)
        {

            Optional<ConnectionE> c = resvService.findByConnectionId(filter.getConnectionId());
            if (c.isPresent()) {
                result.add(this.convertConnToDto(c.get()));
            }
        }
        else if (filter.getResvStates() != null)
        {
            filter.getResvStates().forEach(st -> {
                resvService.ofResvState(st).forEach(ce -> {
                    Connection c = this.convertConnToDto(ce);
                    result.add(c);
                });

            });

        }
        else
        {
            for (ConnectionE eItem : resvService.findAll())
            {
                Connection dtoItem = convertConnToDto(eItem);
                result.add(dtoItem);
            }
        }

        return result;
    }

    // Default endpoint for holding a new connection
    @RequestMapping(value = "/resv/connection/add", method = RequestMethod.POST)
    @ResponseBody
    public Connection submitConnection(@RequestBody Connection connection) throws PSSException, PCEException {
        log.info("Submitting a new complicated connection request");
        log.info(connection.toString());

        return holdConnection(connection);
    }

    // Endpoint for pre-check on a connection
    @RequestMapping(value = "/resv/connection/precheck", method = RequestMethod.POST)
    @ResponseBody
    public Connection preCheck(@RequestBody Connection connection) throws PSSException, PCEException
    {
        log.info("Pre-check initialized for ConnectionID: " + connection.getConnectionId());

        return preCheckConnection(connection); // may be null
    }


    @RequestMapping(value = "/resv/commit/{connectionId}", method = RequestMethod.GET)
    @ResponseBody
    public Connection commit(@PathVariable("connectionId") String connectionId) {
        log.info("attempting to commit " + connectionId);
        ConnectionE connE = resvService.findByConnectionId(connectionId).orElseThrow(NoSuchElementException::new);
        if (connE.getStates().getResv().equals(ResvState.HELD)) {
            connE.getStates().setResv(ResvState.COMMITTING);
            resvService.save(connE);
        }

        return this.convertConnToDto(connE);
    }


    @RequestMapping(value = "/resv/abort/{connectionId}", method = RequestMethod.GET)
    @ResponseBody
    public Connection abort(@PathVariable("connectionId") String connectionId) {
        log.info("attempting to commit " + connectionId);
        ConnectionE connE = resvService.findByConnectionId(connectionId).orElseThrow(NoSuchElementException::new);
        if (connE.getStates().getResv().equals(ResvState.HELD)) {
            connE.getStates().setResv(ResvState.ABORTING);
            resvService.save(connE);
        }

        return this.convertConnToDto(connE);

    }

    private Connection holdConnection(Connection connection) throws PCEException, PSSException {
        connection = defineDefaults(connection);
        ConnectionE connE = modelMapper.map(connection, ConnectionE.class);

        resvService.hold(connE);

        log.info("saved connection, connectionId " + connection.getConnectionId());
        log.info(connE.toString());



        Connection conn = modelMapper.map(connE, Connection.class);
        log.info(conn.toString());


        return conn;

    }

    private Connection defineDefaults(Connection connection) {
        RequestedVlanFlow flow = connection.getSpecification().getRequested().getVlanFlow();
        if(flow.getMinPipes() == null){flow.setMinPipes(flow.getPipes().size());}
        if(flow.getMaxPipes() == null){flow.setMaxPipes(flow.getPipes().size());}

        Set<RequestedVlanPipe> pipes = flow.getPipes();
        for(RequestedVlanPipe pipe : pipes){
            if(pipe.getAzMbps() == null){pipe.setAzMbps(0);}
            if(pipe.getZaMbps() == null){pipe.setZaMbps(0);}
            if(pipe.getAzERO() == null){pipe.setAzERO(new ArrayList<>());}
            if(pipe.getZaERO() == null){pipe.setZaERO(new ArrayList<>());}
            if(pipe.getUrnBlacklist() == null){pipe.setUrnBlacklist(new HashSet<>());}
            if(pipe.getPipeType() == null){pipe.setPipeType(EthPipeType.REQUESTED);}
            if(pipe.getEroPalindromic() == null){pipe.setEroPalindromic(PalindromicType.PALINDROME);}
            if(pipe.getEroSurvivability() == null){pipe.setEroSurvivability(SurvivabilityType.SURVIVABILITY_NONE);}
            if(pipe.getNumPaths() == null){pipe.setNumPaths(1);}
            if(pipe.getPriority() == null){pipe.setPriority(Integer.MAX_VALUE);}
        }
        flow.setPipes(pipes);
        connection.getSpecification().getRequested().setVlanFlow(flow);
        return connection;
    }

    private Connection preCheckConnection(Connection connection) throws PCEException, PSSException
    {
        log.info("Pre-checking ConnectionID: " + connection.getConnectionId());
        connection = defineDefaults(connection);
        ConnectionE connE = modelMapper.map(connection, ConnectionE.class);

        Boolean successful = resvService.preCheck(connE);

        Connection conn = modelMapper.map(connE, Connection.class);

        if(successful)
        {
            log.info("Pre-check result: SUCCESS");
            return conn;
        }
        else
        {
            log.info("Pre-check result: UNSUCCESSFUL");
            return null;
        }
    }

    private Connection convertConnToDto(ConnectionE connectionE) {
        return modelMapper.map(connectionE, Connection.class);
    }

}
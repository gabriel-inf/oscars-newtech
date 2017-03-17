package net.es.oscars.pce.helpers;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.enums.Layer;
import net.es.oscars.dto.topo.enums.PortLayer;
import net.es.oscars.dto.topo.enums.VertexType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by Jeremy on 7/8/2016.
 */
@Slf4j
@Component
public class TopologyBuilder
{
    @Autowired
    private RepoEntityBuilder testBuilder;

    public void buildTopo1()
    {
        log.info("Building Test Topology 1");

        TopoVertex nodeK = new TopoVertex("nodeK", VertexType.SWITCH);
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();
        portDeviceMap.put(portA, nodeK);
        portDeviceMap.put(portZ, nodeK);

        //Internal Links
        TopoEdge edgeInt_A_K = new TopoEdge(portA, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_K = new TopoEdge(portZ, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_A = new TopoEdge(nodeK, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_Z = new TopoEdge(nodeK, portZ, 0L, Layer.INTERNAL);

        List<TopoVertex> topoNodes = new ArrayList<>();
        topoNodes.add(nodeK);
        topoNodes.add(portA);
        topoNodes.add(portZ);

        List<TopoEdge> topoLinks = new ArrayList<>();
        topoLinks.add(edgeInt_A_K);
        topoLinks.add(edgeInt_Z_K);
        topoLinks.add(edgeInt_K_A);
        topoLinks.add(edgeInt_K_Z);


        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap);
    }

    public void buildTopo2()
    {
        log.info("Building Test Topology 2");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();

        // Devices //
        TopoVertex nodeL = new TopoVertex("nodeL", VertexType.SWITCH);
        TopoVertex nodeM = new TopoVertex("nodeM", VertexType.SWITCH);
        TopoVertex nodeN = new TopoVertex("nodeN", VertexType.SWITCH);
        TopoVertex nodeP = new TopoVertex("nodeP", VertexType.ROUTER);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portL1 = new TopoVertex("nodeL:1", VertexType.PORT);
        TopoVertex portL2 = new TopoVertex("nodeL:2", VertexType.PORT);
        TopoVertex portL3 = new TopoVertex("nodeL:3", VertexType.PORT);
        TopoVertex portM1 = new TopoVertex("nodeM:1", VertexType.PORT);
        TopoVertex portM2 = new TopoVertex("nodeM:2", VertexType.PORT);
        TopoVertex portN1 = new TopoVertex("nodeN:1", VertexType.PORT);
        TopoVertex portN2 = new TopoVertex("nodeN:2", VertexType.PORT);
        TopoVertex portP1 = new TopoVertex("nodeP:1", VertexType.PORT);

        // End-Port Links //
        TopoEdge edgeInt_A_P = new TopoEdge(portA, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_M = new TopoEdge(portZ, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_A = new TopoEdge(nodeP, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_Z = new TopoEdge(nodeM, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_L1_L = new TopoEdge(portL1, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L2_L = new TopoEdge(portL2, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L3_L = new TopoEdge(portL3, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M1_M = new TopoEdge(portM1, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M2_M = new TopoEdge(portM2, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N1_N = new TopoEdge(portN1, nodeN, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N2_N = new TopoEdge(portN2, nodeN, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P1_P = new TopoEdge(portP1, nodeP, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_L_L1 = new TopoEdge(nodeL, portL1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L2 = new TopoEdge(nodeL, portL2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L3 = new TopoEdge(nodeL, portL3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M1 = new TopoEdge(nodeM, portM1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M2 = new TopoEdge(nodeM, portM2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N_N1 = new TopoEdge(nodeN, portN1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N_N2 = new TopoEdge(nodeN, portN2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P1 = new TopoEdge(nodeP, portP1, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeEth_L1_P1 = new TopoEdge(portL1, portP1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L2_M1 = new TopoEdge(portL2, portM1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L3_N1 = new TopoEdge(portL3, portN1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M1_L2 = new TopoEdge(portM1, portL2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M2_N2 = new TopoEdge(portM2, portN2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_N1_L3 = new TopoEdge(portN1, portL3, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_N2_M2 = new TopoEdge(portN2, portM2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_P1_L1 = new TopoEdge(portP1, portL1, 100L, Layer.ETHERNET);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();

        topoNodes.add(nodeL);
        topoNodes.add(nodeM);
        topoNodes.add(nodeN);
        topoNodes.add(nodeP);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portL1);
        topoNodes.add(portL2);
        topoNodes.add(portL3);
        topoNodes.add(portM1);
        topoNodes.add(portM2);
        topoNodes.add(portN1);
        topoNodes.add(portN2);
        topoNodes.add(portP1);

        topoLinks.add(edgeInt_A_P);
        topoLinks.add(edgeInt_Z_M);
        topoLinks.add(edgeInt_L1_L);
        topoLinks.add(edgeInt_L2_L);
        topoLinks.add(edgeInt_L3_L);
        topoLinks.add(edgeInt_M1_M);
        topoLinks.add(edgeInt_M2_M);
        topoLinks.add(edgeInt_N1_N);
        topoLinks.add(edgeInt_N2_N);
        topoLinks.add(edgeInt_P1_P);

        topoLinks.add(edgeInt_P_A);
        topoLinks.add(edgeInt_M_Z);
        topoLinks.add(edgeInt_L_L1);
        topoLinks.add(edgeInt_L_L2);
        topoLinks.add(edgeInt_L_L3);
        topoLinks.add(edgeInt_M_M1);
        topoLinks.add(edgeInt_M_M2);
        topoLinks.add(edgeInt_N_N1);
        topoLinks.add(edgeInt_N_N2);
        topoLinks.add(edgeInt_P_P1);

        topoLinks.add(edgeEth_L1_P1);
        topoLinks.add(edgeEth_L2_M1);
        topoLinks.add(edgeEth_L3_N1);
        topoLinks.add(edgeEth_M1_L2);
        topoLinks.add(edgeEth_M2_N2);
        topoLinks.add(edgeEth_N1_L3);
        topoLinks.add(edgeEth_N2_M2);
        topoLinks.add(edgeEth_P1_L1);

        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap);
    }

    public void buildTopo3()
    {
        log.info("Building Test Topology 3");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();

        // Devices //
        TopoVertex nodeK = new TopoVertex("nodeK", VertexType.SWITCH);
        TopoVertex nodeP = new TopoVertex("nodeP", VertexType.ROUTER);
        TopoVertex nodeQ = new TopoVertex("nodeQ", VertexType.ROUTER);
        TopoVertex nodeR = new TopoVertex("nodeR", VertexType.ROUTER);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portK1 = new TopoVertex("nodeK:1", VertexType.PORT);
        TopoVertex portP1 = new TopoVertex("nodeP:1", VertexType.PORT);
        TopoVertex portP2 = new TopoVertex("nodeP:2", VertexType.PORT);
        TopoVertex portP3 = new TopoVertex("nodeP:3", VertexType.PORT);
        TopoVertex portQ1 = new TopoVertex("nodeQ:1", VertexType.PORT);
        TopoVertex portQ2 = new TopoVertex("nodeQ:2", VertexType.PORT);
        TopoVertex portR1 = new TopoVertex("nodeR:1", VertexType.PORT);
        TopoVertex portR2 = new TopoVertex("nodeR:2", VertexType.PORT);

        // End-Port Links //
        TopoEdge edgeInt_A_K = new TopoEdge(portA, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_Q = new TopoEdge(portZ, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_A = new TopoEdge(nodeK, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Z = new TopoEdge(nodeQ, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_K1_K = new TopoEdge(portK1, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P1_P = new TopoEdge(portP1, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P2_P = new TopoEdge(portP2, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P3_P = new TopoEdge(portP3, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q1_Q = new TopoEdge(portQ1, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q2_Q = new TopoEdge(portQ2, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R1_R = new TopoEdge(portR1, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R2_R = new TopoEdge(portR2, nodeR, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_K_K1 = new TopoEdge(nodeK, portK1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P1 = new TopoEdge(nodeP, portP1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P2 = new TopoEdge(nodeP, portP2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P3 = new TopoEdge(nodeP, portP3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q1 = new TopoEdge(nodeQ, portQ1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q2 = new TopoEdge(nodeQ, portQ2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R1 = new TopoEdge(nodeR, portR1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R2 = new TopoEdge(nodeR, portR2, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeEth_K1_P1 = new TopoEdge(portK1, portP1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_P1_K1 = new TopoEdge(portP1, portK1, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_P2_Q1 = new TopoEdge(portP2, portQ1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_P3_R1 = new TopoEdge(portP3, portR1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q1_P2 = new TopoEdge(portQ1, portP2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q2_R2 = new TopoEdge(portQ2, portR2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_R1_P3 = new TopoEdge(portR1, portP3, 100L, Layer.MPLS);
        TopoEdge edgeMpls_R2_Q2 = new TopoEdge(portR2, portQ2, 100L, Layer.MPLS);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();

        topoNodes.add(nodeK);
        topoNodes.add(nodeP);
        topoNodes.add(nodeQ);
        topoNodes.add(nodeR);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portK1);
        topoNodes.add(portP1);
        topoNodes.add(portP2);
        topoNodes.add(portP3);
        topoNodes.add(portQ1);
        topoNodes.add(portQ2);
        topoNodes.add(portR1);
        topoNodes.add(portR2);

        topoLinks.add(edgeInt_A_K);
        topoLinks.add(edgeInt_Z_Q);
        topoLinks.add(edgeInt_K1_K);
        topoLinks.add(edgeInt_P1_P);
        topoLinks.add(edgeInt_P2_P);
        topoLinks.add(edgeInt_P3_P);
        topoLinks.add(edgeInt_Q1_Q);
        topoLinks.add(edgeInt_Q2_Q);
        topoLinks.add(edgeInt_R1_R);
        topoLinks.add(edgeInt_R2_R);

        topoLinks.add(edgeInt_K_A);
        topoLinks.add(edgeInt_Q_Z);
        topoLinks.add(edgeInt_K_K1);
        topoLinks.add(edgeInt_P_P1);
        topoLinks.add(edgeInt_P_P2);
        topoLinks.add(edgeInt_P_P3);
        topoLinks.add(edgeInt_Q_Q1);
        topoLinks.add(edgeInt_Q_Q2);
        topoLinks.add(edgeInt_R_R1);
        topoLinks.add(edgeInt_R_R2);

        topoLinks.add(edgeEth_K1_P1);
        topoLinks.add(edgeEth_P1_K1);
        topoLinks.add(edgeMpls_P2_Q1);
        topoLinks.add(edgeMpls_P3_R1);
        topoLinks.add(edgeMpls_Q1_P2);
        topoLinks.add(edgeMpls_Q2_R2);
        topoLinks.add(edgeMpls_R1_P3);
        topoLinks.add(edgeMpls_R2_Q2);

        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap);
    }

    public void buildTopo4()
    {
        log.info("Building Test Topology 4");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();

        // Devices //
        TopoVertex nodeK = new TopoVertex("nodeK", VertexType.SWITCH);
        TopoVertex nodeL = new TopoVertex("nodeL", VertexType.SWITCH);
        TopoVertex nodeM = new TopoVertex("nodeM", VertexType.SWITCH);
        TopoVertex nodeP = new TopoVertex("nodeP", VertexType.ROUTER);
        TopoVertex nodeQ = new TopoVertex("nodeQ", VertexType.ROUTER);
        TopoVertex nodeR = new TopoVertex("nodeR", VertexType.ROUTER);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portK1 = new TopoVertex("nodeK:1", VertexType.PORT);
        TopoVertex portK2 = new TopoVertex("nodeK:2", VertexType.PORT);
        TopoVertex portL1 = new TopoVertex("nodeL:1", VertexType.PORT);
        TopoVertex portL2 = new TopoVertex("nodeL:2", VertexType.PORT);
        TopoVertex portL3 = new TopoVertex("nodeL:3", VertexType.PORT);
        TopoVertex portM1 = new TopoVertex("nodeM:1", VertexType.PORT);
        TopoVertex portM2 = new TopoVertex("nodeM:2", VertexType.PORT);
        TopoVertex portM3 = new TopoVertex("nodeM:3", VertexType.PORT);
        TopoVertex portP1 = new TopoVertex("nodeP:1", VertexType.PORT);
        TopoVertex portP2 = new TopoVertex("nodeP:2", VertexType.PORT);
        TopoVertex portP3 = new TopoVertex("nodeP:3", VertexType.PORT);
        TopoVertex portQ1 = new TopoVertex("nodeQ:1", VertexType.PORT);
        TopoVertex portQ2 = new TopoVertex("nodeQ:2", VertexType.PORT);
        TopoVertex portR1 = new TopoVertex("nodeR:1", VertexType.PORT);
        TopoVertex portR2 = new TopoVertex("nodeR:2", VertexType.PORT);
        TopoVertex portR3 = new TopoVertex("nodeR:3", VertexType.PORT);

        // End-Port Links //
        TopoEdge edgeInt_A_K = new TopoEdge(portA, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_Q = new TopoEdge(portZ, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_A = new TopoEdge(nodeK, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Z = new TopoEdge(nodeQ, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_K1_K = new TopoEdge(portK1, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K2_K = new TopoEdge(portK2, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L1_L = new TopoEdge(portL1, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L2_L = new TopoEdge(portL2, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L3_L = new TopoEdge(portL3, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M1_M = new TopoEdge(portM1, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M2_M = new TopoEdge(portM2, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M3_M = new TopoEdge(portM3, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P1_P = new TopoEdge(portP1, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P2_P = new TopoEdge(portP2, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P3_P = new TopoEdge(portP3, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q1_Q = new TopoEdge(portQ1, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q2_Q = new TopoEdge(portQ2, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R1_R = new TopoEdge(portR1, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R2_R = new TopoEdge(portR2, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R3_R = new TopoEdge(portR3, nodeR, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_K_K1 = new TopoEdge(nodeK, portK1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_K2 = new TopoEdge(nodeK, portK2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L1 = new TopoEdge(nodeL, portL1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L2 = new TopoEdge(nodeL, portL2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L3 = new TopoEdge(nodeL, portL3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M1 = new TopoEdge(nodeM, portM1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M2 = new TopoEdge(nodeM, portM2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M3 = new TopoEdge(nodeM, portM3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P1 = new TopoEdge(nodeP, portP1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P2 = new TopoEdge(nodeP, portP2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P3 = new TopoEdge(nodeP, portP3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q1 = new TopoEdge(nodeQ, portQ1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q2 = new TopoEdge(nodeQ, portQ2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R1 = new TopoEdge(nodeR, portR1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R2 = new TopoEdge(nodeR, portR2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R3 = new TopoEdge(nodeR, portR3, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeEth_K1_L1 = new TopoEdge(portK1, portL1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_K2_M1 = new TopoEdge(portK2, portM1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L1_K1 = new TopoEdge(portL1, portK1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L2_M2 = new TopoEdge(portL2, portM2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L3_P1 = new TopoEdge(portL3, portP1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M1_K2 = new TopoEdge(portM1, portK2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M2_L2 = new TopoEdge(portM2, portL2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M3_R1 = new TopoEdge(portM3, portR1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_P1_L3 = new TopoEdge(portP1, portL3, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_P2_Q1 = new TopoEdge(portP2, portQ1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_P3_R2 = new TopoEdge(portP3, portR2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q1_P2 = new TopoEdge(portQ1, portP2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q2_R3 = new TopoEdge(portQ2, portR3, 100L, Layer.MPLS);
        TopoEdge edgeEth_R1_M3 = new TopoEdge(portR1, portM3, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_R2_P3 = new TopoEdge(portR2, portP3, 100L, Layer.MPLS);
        TopoEdge edgeMpls_R3_Q2 = new TopoEdge(portR3, portQ2, 100L, Layer.MPLS);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();

        topoNodes.add(nodeK);
        topoNodes.add(nodeL);
        topoNodes.add(nodeM);
        topoNodes.add(nodeP);
        topoNodes.add(nodeQ);
        topoNodes.add(nodeR);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portK1);
        topoNodes.add(portK2);
        topoNodes.add(portL1);
        topoNodes.add(portL2);
        topoNodes.add(portL3);
        topoNodes.add(portM1);
        topoNodes.add(portM2);
        topoNodes.add(portM3);
        topoNodes.add(portP1);
        topoNodes.add(portP2);
        topoNodes.add(portP3);
        topoNodes.add(portQ1);
        topoNodes.add(portQ2);
        topoNodes.add(portR1);
        topoNodes.add(portR2);
        topoNodes.add(portR3);

        topoLinks.add(edgeInt_A_K);
        topoLinks.add(edgeInt_Z_Q);
        topoLinks.add(edgeInt_K1_K);
        topoLinks.add(edgeInt_K2_K);
        topoLinks.add(edgeInt_L1_L);
        topoLinks.add(edgeInt_L2_L);
        topoLinks.add(edgeInt_L3_L);
        topoLinks.add(edgeInt_M1_M);
        topoLinks.add(edgeInt_M2_M);
        topoLinks.add(edgeInt_M3_M);
        topoLinks.add(edgeInt_P1_P);
        topoLinks.add(edgeInt_P2_P);
        topoLinks.add(edgeInt_P3_P);
        topoLinks.add(edgeInt_Q1_Q);
        topoLinks.add(edgeInt_Q2_Q);
        topoLinks.add(edgeInt_R1_R);
        topoLinks.add(edgeInt_R2_R);
        topoLinks.add(edgeInt_R3_R);

        topoLinks.add(edgeInt_K_A);
        topoLinks.add(edgeInt_Q_Z);
        topoLinks.add(edgeInt_K_K1);
        topoLinks.add(edgeInt_K_K2);
        topoLinks.add(edgeInt_L_L1);
        topoLinks.add(edgeInt_L_L2);
        topoLinks.add(edgeInt_L_L3);
        topoLinks.add(edgeInt_M_M1);
        topoLinks.add(edgeInt_M_M2);
        topoLinks.add(edgeInt_M_M3);
        topoLinks.add(edgeInt_P_P1);
        topoLinks.add(edgeInt_P_P2);
        topoLinks.add(edgeInt_P_P3);
        topoLinks.add(edgeInt_Q_Q1);
        topoLinks.add(edgeInt_Q_Q2);
        topoLinks.add(edgeInt_R_R1);
        topoLinks.add(edgeInt_R_R2);
        topoLinks.add(edgeInt_R_R3);

        topoLinks.add(edgeEth_K1_L1);
        topoLinks.add(edgeEth_K2_M1);
        topoLinks.add(edgeEth_L1_K1);
        topoLinks.add(edgeEth_L2_M2);
        topoLinks.add(edgeEth_L3_P1);
        topoLinks.add(edgeEth_M1_K2);
        topoLinks.add(edgeEth_M2_L2);
        topoLinks.add(edgeEth_M3_R1);
        topoLinks.add(edgeEth_P1_L3);
        topoLinks.add(edgeMpls_P2_Q1);
        topoLinks.add(edgeMpls_P3_R2);
        topoLinks.add(edgeMpls_Q1_P2);
        topoLinks.add(edgeMpls_Q2_R3);
        topoLinks.add(edgeEth_R1_M3);
        topoLinks.add(edgeMpls_R2_P3);
        topoLinks.add(edgeMpls_R3_Q2);

        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap);
    }

    // Same as buildTopo4, except all devices/ports are MPLS
    public void buildTopo4_2()
    {
        log.info("Building Test Topology 4.2");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();

        // Devices //
        TopoVertex nodeK = new TopoVertex("nodeK", VertexType.ROUTER);
        TopoVertex nodeL = new TopoVertex("nodeL", VertexType.ROUTER);
        TopoVertex nodeM = new TopoVertex("nodeM", VertexType.ROUTER);
        TopoVertex nodeP = new TopoVertex("nodeP", VertexType.ROUTER);
        TopoVertex nodeQ = new TopoVertex("nodeQ", VertexType.ROUTER);
        TopoVertex nodeR = new TopoVertex("nodeR", VertexType.ROUTER);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portK1 = new TopoVertex("nodeK:1", VertexType.PORT);
        TopoVertex portK2 = new TopoVertex("nodeK:2", VertexType.PORT);
        TopoVertex portL1 = new TopoVertex("nodeL:1", VertexType.PORT);
        TopoVertex portL2 = new TopoVertex("nodeL:2", VertexType.PORT);
        TopoVertex portL3 = new TopoVertex("nodeL:3", VertexType.PORT);
        TopoVertex portM1 = new TopoVertex("nodeM:1", VertexType.PORT);
        TopoVertex portM2 = new TopoVertex("nodeM:2", VertexType.PORT);
        TopoVertex portM3 = new TopoVertex("nodeM:3", VertexType.PORT);
        TopoVertex portP1 = new TopoVertex("nodeP:1", VertexType.PORT);
        TopoVertex portP2 = new TopoVertex("nodeP:2", VertexType.PORT);
        TopoVertex portP3 = new TopoVertex("nodeP:3", VertexType.PORT);
        TopoVertex portQ1 = new TopoVertex("nodeQ:1", VertexType.PORT);
        TopoVertex portQ2 = new TopoVertex("nodeQ:2", VertexType.PORT);
        TopoVertex portR1 = new TopoVertex("nodeR:1", VertexType.PORT);
        TopoVertex portR2 = new TopoVertex("nodeR:2", VertexType.PORT);
        TopoVertex portR3 = new TopoVertex("nodeR:3", VertexType.PORT);

        // End-Port Links //
        TopoEdge edgeInt_A_K = new TopoEdge(portA, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_Q = new TopoEdge(portZ, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_A = new TopoEdge(nodeK, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Z = new TopoEdge(nodeQ, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_K1_K = new TopoEdge(portK1, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K2_K = new TopoEdge(portK2, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L1_L = new TopoEdge(portL1, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L2_L = new TopoEdge(portL2, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L3_L = new TopoEdge(portL3, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M1_M = new TopoEdge(portM1, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M2_M = new TopoEdge(portM2, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M3_M = new TopoEdge(portM3, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P1_P = new TopoEdge(portP1, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P2_P = new TopoEdge(portP2, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P3_P = new TopoEdge(portP3, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q1_Q = new TopoEdge(portQ1, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q2_Q = new TopoEdge(portQ2, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R1_R = new TopoEdge(portR1, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R2_R = new TopoEdge(portR2, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R3_R = new TopoEdge(portR3, nodeR, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_K_K1 = new TopoEdge(nodeK, portK1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_K2 = new TopoEdge(nodeK, portK2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L1 = new TopoEdge(nodeL, portL1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L2 = new TopoEdge(nodeL, portL2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L3 = new TopoEdge(nodeL, portL3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M1 = new TopoEdge(nodeM, portM1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M2 = new TopoEdge(nodeM, portM2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M3 = new TopoEdge(nodeM, portM3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P1 = new TopoEdge(nodeP, portP1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P2 = new TopoEdge(nodeP, portP2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P3 = new TopoEdge(nodeP, portP3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q1 = new TopoEdge(nodeQ, portQ1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q2 = new TopoEdge(nodeQ, portQ2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R1 = new TopoEdge(nodeR, portR1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R2 = new TopoEdge(nodeR, portR2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R3 = new TopoEdge(nodeR, portR3, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeMpls_K1_L1 = new TopoEdge(portK1, portL1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_K2_M1 = new TopoEdge(portK2, portM1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_L1_K1 = new TopoEdge(portL1, portK1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_L2_M2 = new TopoEdge(portL2, portM2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_L3_P1 = new TopoEdge(portL3, portP1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_M1_K2 = new TopoEdge(portM1, portK2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_M2_L2 = new TopoEdge(portM2, portL2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_M3_R1 = new TopoEdge(portM3, portR1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_P1_L3 = new TopoEdge(portP1, portL3, 100L, Layer.MPLS);
        TopoEdge edgeMpls_P2_Q1 = new TopoEdge(portP2, portQ1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_P3_R2 = new TopoEdge(portP3, portR2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q1_P2 = new TopoEdge(portQ1, portP2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q2_R3 = new TopoEdge(portQ2, portR3, 100L, Layer.MPLS);
        TopoEdge edgeMpls_R1_M3 = new TopoEdge(portR1, portM3, 100L, Layer.MPLS);
        TopoEdge edgeMpls_R2_P3 = new TopoEdge(portR2, portP3, 100L, Layer.MPLS);
        TopoEdge edgeMpls_R3_Q2 = new TopoEdge(portR3, portQ2, 100L, Layer.MPLS);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();

        topoNodes.add(nodeK);
        topoNodes.add(nodeL);
        topoNodes.add(nodeM);
        topoNodes.add(nodeP);
        topoNodes.add(nodeQ);
        topoNodes.add(nodeR);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portK1);
        topoNodes.add(portK2);
        topoNodes.add(portL1);
        topoNodes.add(portL2);
        topoNodes.add(portL3);
        topoNodes.add(portM1);
        topoNodes.add(portM2);
        topoNodes.add(portM3);
        topoNodes.add(portP1);
        topoNodes.add(portP2);
        topoNodes.add(portP3);
        topoNodes.add(portQ1);
        topoNodes.add(portQ2);
        topoNodes.add(portR1);
        topoNodes.add(portR2);
        topoNodes.add(portR3);

        topoLinks.add(edgeInt_A_K);
        topoLinks.add(edgeInt_Z_Q);
        topoLinks.add(edgeInt_K1_K);
        topoLinks.add(edgeInt_K2_K);
        topoLinks.add(edgeInt_L1_L);
        topoLinks.add(edgeInt_L2_L);
        topoLinks.add(edgeInt_L3_L);
        topoLinks.add(edgeInt_M1_M);
        topoLinks.add(edgeInt_M2_M);
        topoLinks.add(edgeInt_M3_M);
        topoLinks.add(edgeInt_P1_P);
        topoLinks.add(edgeInt_P2_P);
        topoLinks.add(edgeInt_P3_P);
        topoLinks.add(edgeInt_Q1_Q);
        topoLinks.add(edgeInt_Q2_Q);
        topoLinks.add(edgeInt_R1_R);
        topoLinks.add(edgeInt_R2_R);
        topoLinks.add(edgeInt_R3_R);

        topoLinks.add(edgeInt_K_A);
        topoLinks.add(edgeInt_Q_Z);
        topoLinks.add(edgeInt_K_K1);
        topoLinks.add(edgeInt_K_K2);
        topoLinks.add(edgeInt_L_L1);
        topoLinks.add(edgeInt_L_L2);
        topoLinks.add(edgeInt_L_L3);
        topoLinks.add(edgeInt_M_M1);
        topoLinks.add(edgeInt_M_M2);
        topoLinks.add(edgeInt_M_M3);
        topoLinks.add(edgeInt_P_P1);
        topoLinks.add(edgeInt_P_P2);
        topoLinks.add(edgeInt_P_P3);
        topoLinks.add(edgeInt_Q_Q1);
        topoLinks.add(edgeInt_Q_Q2);
        topoLinks.add(edgeInt_R_R1);
        topoLinks.add(edgeInt_R_R2);
        topoLinks.add(edgeInt_R_R3);

        topoLinks.add(edgeMpls_K1_L1);
        topoLinks.add(edgeMpls_K2_M1);
        topoLinks.add(edgeMpls_L1_K1);
        topoLinks.add(edgeMpls_L2_M2);
        topoLinks.add(edgeMpls_L3_P1);
        topoLinks.add(edgeMpls_M1_K2);
        topoLinks.add(edgeMpls_M2_L2);
        topoLinks.add(edgeMpls_M3_R1);
        topoLinks.add(edgeMpls_P1_L3);
        topoLinks.add(edgeMpls_P2_Q1);
        topoLinks.add(edgeMpls_P3_R2);
        topoLinks.add(edgeMpls_Q1_P2);
        topoLinks.add(edgeMpls_Q2_R3);
        topoLinks.add(edgeMpls_R1_M3);
        topoLinks.add(edgeMpls_R2_P3);
        topoLinks.add(edgeMpls_R3_Q2);

        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap);
    }
    
    public void buildTopo5()
    {
        log.info("Building Test Topology 5");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();

        // Devices //
        TopoVertex nodeK = new TopoVertex("nodeK", VertexType.SWITCH);
        TopoVertex nodeM = new TopoVertex("nodeM", VertexType.SWITCH);
        TopoVertex nodeP = new TopoVertex("nodeP", VertexType.ROUTER);
        TopoVertex nodeQ = new TopoVertex("nodeQ", VertexType.ROUTER);
        TopoVertex nodeR = new TopoVertex("nodeR", VertexType.ROUTER);
        TopoVertex nodeS = new TopoVertex("nodeS", VertexType.SWITCH);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portK1 = new TopoVertex("nodeK:1", VertexType.PORT);
        TopoVertex portK2 = new TopoVertex("nodeK:2", VertexType.PORT);
        TopoVertex portM1 = new TopoVertex("nodeM:1", VertexType.PORT);
        TopoVertex portM2 = new TopoVertex("nodeM:2", VertexType.PORT);
        TopoVertex portM3 = new TopoVertex("nodeM:3", VertexType.PORT);
        TopoVertex portP1 = new TopoVertex("nodeP:1", VertexType.PORT);
        TopoVertex portP2 = new TopoVertex("nodeP:2", VertexType.PORT);
        TopoVertex portP3 = new TopoVertex("nodeP:3", VertexType.PORT);
        TopoVertex portP4 = new TopoVertex("nodeP:4", VertexType.PORT);
        TopoVertex portQ1 = new TopoVertex("nodeQ:1", VertexType.PORT);
        TopoVertex portQ2 = new TopoVertex("nodeQ:2", VertexType.PORT);
        TopoVertex portQ3 = new TopoVertex("nodeQ:3", VertexType.PORT);
        TopoVertex portR1 = new TopoVertex("nodeR:1", VertexType.PORT);
        TopoVertex portR2 = new TopoVertex("nodeR:2", VertexType.PORT);
        TopoVertex portR3 = new TopoVertex("nodeR:3", VertexType.PORT);
        TopoVertex portS1 = new TopoVertex("nodeS:1", VertexType.PORT);

        // End-Port Links //
        TopoEdge edgeInt_A_K = new TopoEdge(portA, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_S = new TopoEdge(portZ, nodeS, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_A = new TopoEdge(nodeK, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S_Z = new TopoEdge(nodeS, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_K1_K = new TopoEdge(portK1, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K2_K = new TopoEdge(portK2, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M1_M = new TopoEdge(portM1, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M2_M = new TopoEdge(portM2, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M3_M = new TopoEdge(portM3, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P1_P = new TopoEdge(portP1, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P2_P = new TopoEdge(portP2, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P3_P = new TopoEdge(portP3, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P4_P = new TopoEdge(portP4, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q1_Q = new TopoEdge(portQ1, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q2_Q = new TopoEdge(portQ2, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q3_Q = new TopoEdge(portQ3, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R1_R = new TopoEdge(portR1, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R2_R = new TopoEdge(portR2, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R3_R = new TopoEdge(portR3, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S1_S = new TopoEdge(portS1, nodeS, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_K_K1 = new TopoEdge(nodeK, portK1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_K2 = new TopoEdge(nodeK, portK2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M1 = new TopoEdge(nodeM, portM1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M2 = new TopoEdge(nodeM, portM2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M3 = new TopoEdge(nodeM, portM3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P1 = new TopoEdge(nodeP, portP1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P2 = new TopoEdge(nodeP, portP2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P3 = new TopoEdge(nodeP, portP3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P4 = new TopoEdge(nodeP, portP4, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q1 = new TopoEdge(nodeQ, portQ1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q2 = new TopoEdge(nodeQ, portQ2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q3 = new TopoEdge(nodeQ, portQ3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R1 = new TopoEdge(nodeR, portR1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R2 = new TopoEdge(nodeR, portR2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R3 = new TopoEdge(nodeR, portR3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S_S1 = new TopoEdge(nodeS, portS1, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeEth_K1_M1 = new TopoEdge(portK1, portM1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_K2_P1 = new TopoEdge(portK2, portP1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M1_K1 = new TopoEdge(portM1, portK1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M2_P2 = new TopoEdge(portM2, portP2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M3_R1 = new TopoEdge(portM3, portR1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_P1_K2 = new TopoEdge(portP1, portK2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_P2_M2 = new TopoEdge(portP2, portM2, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_P3_Q1 = new TopoEdge(portP3, portQ1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_P4_R2 = new TopoEdge(portP4, portR2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q1_P3 = new TopoEdge(portQ1, portP3, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q2_R3 = new TopoEdge(portQ2, portR3, 100L, Layer.MPLS);
        TopoEdge edgeEth_Q3_S1 = new TopoEdge(portQ3, portS1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_R1_M3 = new TopoEdge(portR1, portM3, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_R2_P4 = new TopoEdge(portR2, portP4, 100L, Layer.MPLS);
        TopoEdge edgeMpls_R3_Q2 = new TopoEdge(portR3, portQ2, 100L, Layer.MPLS);
        TopoEdge edgeEth_S1_Q3 = new TopoEdge(portS1, portQ3, 100L, Layer.ETHERNET);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();

        topoNodes.add(nodeK);
        topoNodes.add(nodeM);
        topoNodes.add(nodeP);
        topoNodes.add(nodeQ);
        topoNodes.add(nodeR);
        topoNodes.add(nodeR);
        topoNodes.add(nodeS);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portK1);
        topoNodes.add(portK2);
        topoNodes.add(portM1);
        topoNodes.add(portM2);
        topoNodes.add(portM3);
        topoNodes.add(portP1);
        topoNodes.add(portP2);
        topoNodes.add(portP3);
        topoNodes.add(portP4);
        topoNodes.add(portQ1);
        topoNodes.add(portQ2);
        topoNodes.add(portQ3);
        topoNodes.add(portR1);
        topoNodes.add(portR2);
        topoNodes.add(portR3);
        topoNodes.add(portS1);

        topoLinks.add(edgeInt_A_K);
        topoLinks.add(edgeInt_Z_S);
        topoLinks.add(edgeInt_K1_K);
        topoLinks.add(edgeInt_K2_K);
        topoLinks.add(edgeInt_M1_M);
        topoLinks.add(edgeInt_M2_M);
        topoLinks.add(edgeInt_M3_M);
        topoLinks.add(edgeInt_P1_P);
        topoLinks.add(edgeInt_P2_P);
        topoLinks.add(edgeInt_P3_P);
        topoLinks.add(edgeInt_P4_P);
        topoLinks.add(edgeInt_Q1_Q);
        topoLinks.add(edgeInt_Q2_Q);
        topoLinks.add(edgeInt_Q3_Q);
        topoLinks.add(edgeInt_R1_R);
        topoLinks.add(edgeInt_R2_R);
        topoLinks.add(edgeInt_R3_R);
        topoLinks.add(edgeInt_S1_S);

        topoLinks.add(edgeInt_K_A);
        topoLinks.add(edgeInt_S_Z);
        topoLinks.add(edgeInt_K_K1);
        topoLinks.add(edgeInt_K_K2);
        topoLinks.add(edgeInt_M_M1);
        topoLinks.add(edgeInt_M_M2);
        topoLinks.add(edgeInt_M_M3);
        topoLinks.add(edgeInt_P_P1);
        topoLinks.add(edgeInt_P_P2);
        topoLinks.add(edgeInt_P_P3);
        topoLinks.add(edgeInt_P_P4);
        topoLinks.add(edgeInt_Q_Q1);
        topoLinks.add(edgeInt_Q_Q2);
        topoLinks.add(edgeInt_Q_Q3);
        topoLinks.add(edgeInt_R_R1);
        topoLinks.add(edgeInt_R_R2);
        topoLinks.add(edgeInt_R_R3);
        topoLinks.add(edgeInt_S_S1);

        topoLinks.add(edgeEth_K1_M1);
        topoLinks.add(edgeEth_K2_P1);
        topoLinks.add(edgeEth_M1_K1);
        topoLinks.add(edgeEth_M2_P2);
        topoLinks.add(edgeEth_M3_R1);
        topoLinks.add(edgeEth_P1_K2);
        topoLinks.add(edgeEth_P2_M2);
        topoLinks.add(edgeMpls_P3_Q1);
        topoLinks.add(edgeMpls_P4_R2);
        topoLinks.add(edgeMpls_Q1_P3);
        topoLinks.add(edgeMpls_Q2_R3);
        topoLinks.add(edgeEth_Q3_S1);
        topoLinks.add(edgeEth_R1_M3);
        topoLinks.add(edgeMpls_R2_P4);
        topoLinks.add(edgeMpls_R3_Q2);
        topoLinks.add(edgeEth_S1_Q3);

        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap);
    }


    public void buildTopo6()
    {
        log.info("Building Test Topology 6");

        TopoVertex nodeP = new TopoVertex("nodeP", VertexType.ROUTER);
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();
        portDeviceMap.put(portA, nodeP);
        portDeviceMap.put(portZ, nodeP);

        //Internal Links
        TopoEdge edgeInt_A_P = new TopoEdge(portA, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_P = new TopoEdge(portZ, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_A = new TopoEdge(nodeP, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_Z = new TopoEdge(nodeP, portZ, 0L, Layer.INTERNAL);

        List<TopoVertex> topoNodes = new ArrayList<>();
        topoNodes.add(nodeP);
        topoNodes.add(portA);
        topoNodes.add(portZ);

        List<TopoEdge> topoLinks = new ArrayList<>();
        topoLinks.add(edgeInt_A_P);
        topoLinks.add(edgeInt_Z_P);
        topoLinks.add(edgeInt_P_A);
        topoLinks.add(edgeInt_P_Z);

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap);
    }

    public void buildTopo7()
    {
        log.info("Building Test Topology 7");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();
        Map<TopoVertex, List<Integer>> floorMap = new HashMap<>();
        Map<TopoVertex, List<Integer>> ceilingMap = new HashMap<>();
        // Devices //
        TopoVertex nodeK = new TopoVertex("nodeK", VertexType.SWITCH);
        TopoVertex nodeL = new TopoVertex("nodeL", VertexType.SWITCH);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portK1 = new TopoVertex("nodeK:1", VertexType.PORT);
        TopoVertex portL1 = new TopoVertex("nodeL:1", VertexType.PORT);

        // End-Port Links //
        TopoEdge edgeInt_A_K = new TopoEdge(portA, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_L = new TopoEdge(portZ, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_A = new TopoEdge(nodeK, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_Z = new TopoEdge(nodeL, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_K1_K = new TopoEdge(portK1, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L1_L = new TopoEdge(portL1, nodeL, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_K_K1 = new TopoEdge(nodeK, portK1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L1 = new TopoEdge(nodeL, portL1, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeEth_K1_L1 = new TopoEdge(portK1, portL1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L1_K1 = new TopoEdge(portL1, portK1, 100L, Layer.ETHERNET);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();
        List<Integer> floors = Arrays.asList(1);
        List<Integer> ceilings = Arrays.asList(5);

        topoNodes.add(nodeK);
        topoNodes.add(nodeL);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portK1);
        topoNodes.add(portL1);

        topoLinks.add(edgeInt_A_K);
        topoLinks.add(edgeInt_Z_L);
        topoLinks.add(edgeInt_K1_K);
        topoLinks.add(edgeInt_L1_L);

        topoLinks.add(edgeInt_K_A);
        topoLinks.add(edgeInt_L_Z);
        topoLinks.add(edgeInt_K_K1);
        topoLinks.add(edgeInt_L_L1);

        topoLinks.add(edgeEth_K1_L1);
        topoLinks.add(edgeEth_L1_K1);


        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        for(TopoVertex oneVert : topoNodes)
        {
            if(oneVert.getVertexType().equals(VertexType.SWITCH))
            {
                floorMap.put(oneVert, floors);
                ceilingMap.put(oneVert, ceilings);
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap, floorMap, ceilingMap);
    }

    public void buildTopo8()
    {
        log.info("Building Test Topology 8");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();
        Map<TopoVertex, List<Integer>> floorMap = new HashMap<>();
        Map<TopoVertex, List<Integer>> ceilingMap = new HashMap<>();


        // Devices //
        TopoVertex nodeP = new TopoVertex("nodeP", VertexType.ROUTER);
        TopoVertex nodeQ = new TopoVertex("nodeQ", VertexType.ROUTER);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portP1 = new TopoVertex("nodeP:1", VertexType.PORT);
        TopoVertex portQ1 = new TopoVertex("nodeQ:1", VertexType.PORT);

        // End-Port Links //
        TopoEdge edgeInt_A_P = new TopoEdge(portA, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_Q = new TopoEdge(portZ, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_A = new TopoEdge(nodeP, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Z = new TopoEdge(nodeQ, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_P1_P = new TopoEdge(portP1, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q1_Q = new TopoEdge(portQ1, nodeQ, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_P_P1 = new TopoEdge(nodeP, portP1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q1 = new TopoEdge(nodeQ, portQ1, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeMpls_P1_Q1 = new TopoEdge(portP1, portQ1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q1_P1 = new TopoEdge(portQ1, portP1, 100L, Layer.MPLS);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();
        List<Integer> floors = Arrays.asList(1);
        List<Integer> ceilings = Arrays.asList(5);

        topoNodes.add(nodeP);
        topoNodes.add(nodeQ);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portP1);
        topoNodes.add(portQ1);

        topoLinks.add(edgeInt_A_P);
        topoLinks.add(edgeInt_Z_Q);
        topoLinks.add(edgeInt_P1_P);
        topoLinks.add(edgeInt_Q1_Q);

        topoLinks.add(edgeInt_P_A);
        topoLinks.add(edgeInt_Q_Z);
        topoLinks.add(edgeInt_P_P1);
        topoLinks.add(edgeInt_Q_Q1);

        topoLinks.add(edgeMpls_P1_Q1);
        topoLinks.add(edgeMpls_Q1_P1);


        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        for(TopoVertex oneVert : topoNodes)
        {
            if(oneVert.getVertexType().equals(VertexType.PORT))
            {
                floorMap.put(oneVert, floors);
                ceilingMap.put(oneVert, ceilings);
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap, floorMap, ceilingMap);
    }

    public void buildTopo9()
    {
        log.info("Building Test Topology 9");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();

        // Devices //
        TopoVertex nodeK = new TopoVertex("nodeK", VertexType.SWITCH);
        TopoVertex nodeP = new TopoVertex("nodeP", VertexType.ROUTER);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portK1 = new TopoVertex("nodeK:1", VertexType.PORT);
        TopoVertex portP1 = new TopoVertex("nodeP:1", VertexType.PORT);

        // End-Port Links //
        TopoEdge edgeInt_A_K = new TopoEdge(portA, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_P = new TopoEdge(portZ, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_A = new TopoEdge(nodeK, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_Z = new TopoEdge(nodeP, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_K1_K = new TopoEdge(portK1, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P1_P = new TopoEdge(portP1, nodeP, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_K_K1 = new TopoEdge(nodeK, portK1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P1 = new TopoEdge(nodeP, portP1, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeEth_K1_P1 = new TopoEdge(portK1, portP1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_P1_K1 = new TopoEdge(portP1, portK1, 100L, Layer.ETHERNET);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();

        topoNodes.add(nodeK);
        topoNodes.add(nodeP);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portK1);
        topoNodes.add(portP1);

        topoLinks.add(edgeInt_A_K);
        topoLinks.add(edgeInt_Z_P);
        topoLinks.add(edgeInt_K1_K);
        topoLinks.add(edgeInt_P1_P);

        topoLinks.add(edgeInt_K_A);
        topoLinks.add(edgeInt_P_Z);
        topoLinks.add(edgeInt_K_K1);
        topoLinks.add(edgeInt_P_P1);

        topoLinks.add(edgeEth_K1_P1);
        topoLinks.add(edgeEth_P1_K1);


        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap);
    }

    public void buildTopo10()
    {
        log.info("Building Test Topology 10");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();

        // Devices //
        TopoVertex nodeK = new TopoVertex("nodeK", VertexType.SWITCH);
        TopoVertex nodeL = new TopoVertex("nodeL", VertexType.SWITCH);
        TopoVertex nodeM = new TopoVertex("nodeM", VertexType.SWITCH);
        TopoVertex nodeN = new TopoVertex("nodeN", VertexType.SWITCH);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portK1 = new TopoVertex("nodeK:1", VertexType.PORT);
        TopoVertex portL1 = new TopoVertex("nodeL:1", VertexType.PORT);
        TopoVertex portL2 = new TopoVertex("nodeL:2", VertexType.PORT);
        TopoVertex portL3 = new TopoVertex("nodeL:3", VertexType.PORT);
        TopoVertex portM1 = new TopoVertex("nodeM:1", VertexType.PORT);
        TopoVertex portM2 = new TopoVertex("nodeM:2", VertexType.PORT);
        TopoVertex portN1 = new TopoVertex("nodeN:1", VertexType.PORT);
        TopoVertex portN2 = new TopoVertex("nodeN:2", VertexType.PORT);

        // End-Port Links //
        TopoEdge edgeInt_A_K = new TopoEdge(portA, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_M = new TopoEdge(portZ, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_A = new TopoEdge(nodeK, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_Z = new TopoEdge(nodeM, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_K1_K = new TopoEdge(portK1, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L1_L = new TopoEdge(portL1, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L2_L = new TopoEdge(portL2, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L3_L = new TopoEdge(portL3, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M1_M = new TopoEdge(portM1, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M2_M = new TopoEdge(portM2, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N1_N = new TopoEdge(portN1, nodeN, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N2_N = new TopoEdge(portN2, nodeN, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_K_K1 = new TopoEdge(nodeK, portK1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L1 = new TopoEdge(nodeL, portL1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L2 = new TopoEdge(nodeL, portL2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L3 = new TopoEdge(nodeL, portL3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M1 = new TopoEdge(nodeM, portM1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M2 = new TopoEdge(nodeM, portM2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N_N1 = new TopoEdge(nodeN, portN1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N_N2 = new TopoEdge(nodeN, portN2, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeEth_K1_L1 = new TopoEdge(portK1, portL1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L1_K1 = new TopoEdge(portL1, portK1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L2_M1 = new TopoEdge(portL2, portM1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L3_N1 = new TopoEdge(portL3, portN1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M1_L2 = new TopoEdge(portM1, portL2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M2_N2 = new TopoEdge(portM2, portN2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_N1_L3 = new TopoEdge(portN1, portL3, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_N2_M2 = new TopoEdge(portN2, portM2, 100L, Layer.ETHERNET);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();

        topoNodes.add(nodeK);
        topoNodes.add(nodeL);
        topoNodes.add(nodeM);
        topoNodes.add(nodeN);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portK1);
        topoNodes.add(portL1);
        topoNodes.add(portL2);
        topoNodes.add(portL3);
        topoNodes.add(portM1);
        topoNodes.add(portM2);
        topoNodes.add(portN1);
        topoNodes.add(portN2);

        topoLinks.add(edgeInt_A_K);
        topoLinks.add(edgeInt_Z_M);
        topoLinks.add(edgeInt_K1_K);
        topoLinks.add(edgeInt_L1_L);
        topoLinks.add(edgeInt_L2_L);
        topoLinks.add(edgeInt_L3_L);
        topoLinks.add(edgeInt_M1_M);
        topoLinks.add(edgeInt_M2_M);
        topoLinks.add(edgeInt_N1_N);
        topoLinks.add(edgeInt_N2_N);

        topoLinks.add(edgeInt_K_A);
        topoLinks.add(edgeInt_M_Z);
        topoLinks.add(edgeInt_K_K1);
        topoLinks.add(edgeInt_L_L1);
        topoLinks.add(edgeInt_L_L2);
        topoLinks.add(edgeInt_L_L3);
        topoLinks.add(edgeInt_M_M1);
        topoLinks.add(edgeInt_M_M2);
        topoLinks.add(edgeInt_N_N1);
        topoLinks.add(edgeInt_N_N2);

        topoLinks.add(edgeEth_K1_L1);
        topoLinks.add(edgeEth_L1_K1);
        topoLinks.add(edgeEth_L2_M1);
        topoLinks.add(edgeEth_L3_N1);
        topoLinks.add(edgeEth_M1_L2);
        topoLinks.add(edgeEth_M2_N2);
        topoLinks.add(edgeEth_N1_L3);
        topoLinks.add(edgeEth_N2_M2);


        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap);
    }

    public void buildTopo11()
    {
        log.info("Building Test Topology 11");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();

        // Devices //
        TopoVertex nodeP = new TopoVertex("nodeP", VertexType.ROUTER);
        TopoVertex nodeQ = new TopoVertex("nodeQ", VertexType.ROUTER);
        TopoVertex nodeR = new TopoVertex("nodeR", VertexType.ROUTER);
        TopoVertex nodeS = new TopoVertex("nodeS", VertexType.ROUTER);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portP1 = new TopoVertex("nodeP:1", VertexType.PORT);
        TopoVertex portQ1 = new TopoVertex("nodeQ:1", VertexType.PORT);
        TopoVertex portQ2 = new TopoVertex("nodeQ:2", VertexType.PORT);
        TopoVertex portQ3 = new TopoVertex("nodeQ:3", VertexType.PORT);
        TopoVertex portR1 = new TopoVertex("nodeR:1", VertexType.PORT);
        TopoVertex portR2 = new TopoVertex("nodeR:2", VertexType.PORT);
        TopoVertex portS1 = new TopoVertex("nodeS:1", VertexType.PORT);
        TopoVertex portS2 = new TopoVertex("nodeS:2", VertexType.PORT);

        // End-Port Links //
        TopoEdge edgeInt_A_P = new TopoEdge(portA, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_R = new TopoEdge(portZ, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_A = new TopoEdge(nodeP, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_Z = new TopoEdge(nodeR, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_P1_P = new TopoEdge(portP1, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q1_Q = new TopoEdge(portQ1, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q2_Q = new TopoEdge(portQ2, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q3_Q = new TopoEdge(portQ3, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R1_R = new TopoEdge(portR1, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R2_R = new TopoEdge(portR2, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S1_S = new TopoEdge(portS1, nodeS, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S2_S = new TopoEdge(portS2, nodeS, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_P_P1 = new TopoEdge(nodeP, portP1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q1 = new TopoEdge(nodeQ, portQ1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q2 = new TopoEdge(nodeQ, portQ2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q3 = new TopoEdge(nodeQ, portQ3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R1 = new TopoEdge(nodeR, portR1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R2 = new TopoEdge(nodeR, portR2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S_S1 = new TopoEdge(nodeS, portS1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S_S2 = new TopoEdge(nodeS, portS2, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeMpls_P1_Q1 = new TopoEdge(portP1, portQ1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q1_P1 = new TopoEdge(portQ1, portP1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q2_R1 = new TopoEdge(portQ2, portR1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q3_S1 = new TopoEdge(portQ3, portS1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_R1_Q2 = new TopoEdge(portR1, portQ2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_R2_S2 = new TopoEdge(portR2, portS2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_S1_Q3 = new TopoEdge(portS1, portQ3, 100L, Layer.MPLS);
        TopoEdge edgeMpls_S2_R2 = new TopoEdge(portS2, portR2, 100L, Layer.MPLS);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();

        topoNodes.add(nodeP);
        topoNodes.add(nodeQ);
        topoNodes.add(nodeR);
        topoNodes.add(nodeS);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portP1);
        topoNodes.add(portQ1);
        topoNodes.add(portQ2);
        topoNodes.add(portQ3);
        topoNodes.add(portR1);
        topoNodes.add(portR2);
        topoNodes.add(portS1);
        topoNodes.add(portS2);

        topoLinks.add(edgeInt_A_P);
        topoLinks.add(edgeInt_Z_R);
        topoLinks.add(edgeInt_P1_P);
        topoLinks.add(edgeInt_Q1_Q);
        topoLinks.add(edgeInt_Q2_Q);
        topoLinks.add(edgeInt_Q3_Q);
        topoLinks.add(edgeInt_R1_R);
        topoLinks.add(edgeInt_R2_R);
        topoLinks.add(edgeInt_S1_S);
        topoLinks.add(edgeInt_S2_S);

        topoLinks.add(edgeInt_P_A);
        topoLinks.add(edgeInt_R_Z);
        topoLinks.add(edgeInt_P_P1);
        topoLinks.add(edgeInt_Q_Q1);
        topoLinks.add(edgeInt_Q_Q2);
        topoLinks.add(edgeInt_Q_Q3);
        topoLinks.add(edgeInt_R_R1);
        topoLinks.add(edgeInt_R_R2);
        topoLinks.add(edgeInt_S_S1);
        topoLinks.add(edgeInt_S_S2);

        topoLinks.add(edgeMpls_P1_Q1);
        topoLinks.add(edgeMpls_Q1_P1);
        topoLinks.add(edgeMpls_Q2_R1);
        topoLinks.add(edgeMpls_Q3_S1);
        topoLinks.add(edgeMpls_R1_Q2);
        topoLinks.add(edgeMpls_R2_S2);
        topoLinks.add(edgeMpls_S1_Q3);
        topoLinks.add(edgeMpls_S2_R2);


        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap);
    }

    public void buildTopo12()
    {
        log.info("Building Test Topology 12");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();

        // Devices //
        TopoVertex nodeK = new TopoVertex("nodeK", VertexType.SWITCH);
        TopoVertex nodeL = new TopoVertex("nodeL", VertexType.SWITCH);
        TopoVertex nodeM = new TopoVertex("nodeM", VertexType.SWITCH);
        TopoVertex nodeP = new TopoVertex("nodeP", VertexType.ROUTER);
        TopoVertex nodeQ = new TopoVertex("nodeQ", VertexType.ROUTER);


        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portK1 = new TopoVertex("nodeK:1", VertexType.PORT);
        TopoVertex portK2 = new TopoVertex("nodeK:2", VertexType.PORT);
        TopoVertex portL1 = new TopoVertex("nodeL:1", VertexType.PORT);
        TopoVertex portL2 = new TopoVertex("nodeL:2", VertexType.PORT);
        TopoVertex portM1 = new TopoVertex("nodeM:1", VertexType.PORT);
        TopoVertex portM2 = new TopoVertex("nodeM:2", VertexType.PORT);
        TopoVertex portP1 = new TopoVertex("nodeP:1", VertexType.PORT);
        TopoVertex portP2 = new TopoVertex("nodeP:2", VertexType.PORT);
        TopoVertex portQ1 = new TopoVertex("nodeQ:1", VertexType.PORT);
        TopoVertex portQ2 = new TopoVertex("nodeQ:2", VertexType.PORT);

        // End-Port Links //
        TopoEdge edgeInt_A_K = new TopoEdge(portA, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_Q = new TopoEdge(portZ, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_A = new TopoEdge(nodeK, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Z = new TopoEdge(nodeQ, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_K1_K = new TopoEdge(portK1, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K2_K = new TopoEdge(portK2, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L1_L = new TopoEdge(portL1, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L2_L = new TopoEdge(portL2, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M1_M = new TopoEdge(portM1, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M2_M = new TopoEdge(portM2, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P1_P = new TopoEdge(portP1, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P2_P = new TopoEdge(portP2, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q1_Q = new TopoEdge(portQ1, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q2_Q = new TopoEdge(portQ2, nodeQ, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_K_K1 = new TopoEdge(nodeK, portK1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_K2 = new TopoEdge(nodeK, portK2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L1 = new TopoEdge(nodeL, portL1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L2 = new TopoEdge(nodeL, portL2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M1 = new TopoEdge(nodeM, portM1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M2 = new TopoEdge(nodeM, portM2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P1 = new TopoEdge(nodeP, portP1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P2 = new TopoEdge(nodeP, portP2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q1 = new TopoEdge(nodeQ, portQ1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q2 = new TopoEdge(nodeQ, portQ2, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeEth_K1_L1 = new TopoEdge(portK1, portL1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_K2_M1 = new TopoEdge(portK2, portM1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L1_K1 = new TopoEdge(portL1, portK1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L2_P1 = new TopoEdge(portL2, portP1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M1_K2 = new TopoEdge(portM1, portK2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M2_Q2 = new TopoEdge(portM2, portQ2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_P1_L2 = new TopoEdge(portP1, portL2, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_P2_Q1 = new TopoEdge(portP2, portQ1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q1_P2 = new TopoEdge(portQ1, portP2, 100L, Layer.MPLS);
        TopoEdge edgeEth_Q2_M2 = new TopoEdge(portQ2, portM2, 100L, Layer.ETHERNET);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();

        topoNodes.add(nodeK);
        topoNodes.add(nodeL);
        topoNodes.add(nodeM);
        topoNodes.add(nodeP);
        topoNodes.add(nodeQ);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portK1);
        topoNodes.add(portK2);
        topoNodes.add(portL1);
        topoNodes.add(portL2);
        topoNodes.add(portM1);
        topoNodes.add(portM2);
        topoNodes.add(portP1);
        topoNodes.add(portP2);
        topoNodes.add(portQ1);
        topoNodes.add(portQ2);

        topoLinks.add(edgeInt_A_K);
        topoLinks.add(edgeInt_Z_Q);
        topoLinks.add(edgeInt_K1_K);
        topoLinks.add(edgeInt_K2_K);
        topoLinks.add(edgeInt_L1_L);
        topoLinks.add(edgeInt_L2_L);
        topoLinks.add(edgeInt_M1_M);
        topoLinks.add(edgeInt_M2_M);
        topoLinks.add(edgeInt_P1_P);
        topoLinks.add(edgeInt_P2_P);
        topoLinks.add(edgeInt_Q1_Q);
        topoLinks.add(edgeInt_Q2_Q);

        topoLinks.add(edgeInt_K_A);
        topoLinks.add(edgeInt_Q_Z);
        topoLinks.add(edgeInt_K_K1);
        topoLinks.add(edgeInt_K_K2);
        topoLinks.add(edgeInt_L_L1);
        topoLinks.add(edgeInt_L_L2);
        topoLinks.add(edgeInt_M_M1);
        topoLinks.add(edgeInt_M_M2);
        topoLinks.add(edgeInt_P_P1);
        topoLinks.add(edgeInt_P_P2);
        topoLinks.add(edgeInt_Q_Q1);
        topoLinks.add(edgeInt_Q_Q2);

        topoLinks.add(edgeEth_K1_L1);
        topoLinks.add(edgeEth_K2_M1);
        topoLinks.add(edgeEth_L1_K1);
        topoLinks.add(edgeEth_L2_P1);
        topoLinks.add(edgeEth_M1_K2);
        topoLinks.add(edgeEth_M2_Q2);
        topoLinks.add(edgeEth_P1_L2);
        topoLinks.add(edgeMpls_P2_Q1);
        topoLinks.add(edgeMpls_Q1_P2);
        topoLinks.add(edgeEth_Q2_M2);


        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap);
    }


    public void buildSharedLinkTopo1()
    {
        log.info("Building Shared-Link Test Topology 1");

        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();
        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();

        VertexType kType = VertexType.SWITCH;
        VertexType lType = VertexType.SWITCH;
        VertexType mType = VertexType.SWITCH;
        VertexType nType = VertexType.SWITCH;

        buildSharedLinkTopology(topoNodes, topoLinks, portDeviceMap, kType, lType, mType, nType);

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap);
    }

    public void buildSharedLinkTopo2()
    {
        log.info("Building Shared-Link Test Topology 2");

        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();
        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();

        VertexType kType = VertexType.ROUTER;
        VertexType lType = VertexType.ROUTER;
        VertexType mType = VertexType.ROUTER;
        VertexType nType = VertexType.ROUTER;

        buildSharedLinkTopology(topoNodes, topoLinks, portDeviceMap, kType, lType, mType, nType);

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap);
    }

    public void buildSharedLinkTopo3()
    {
        log.info("Building Shared-Link Test Topology 3");

        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();
        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();

        VertexType kType = VertexType.SWITCH;
        VertexType lType = VertexType.ROUTER;
        VertexType mType = VertexType.ROUTER;
        VertexType nType = VertexType.SWITCH;

        buildSharedLinkTopology(topoNodes, topoLinks, portDeviceMap, kType, lType, mType, nType);

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap);
    }

    public void buildSharedLinkTopo4()
    {
        log.info("Building Shared-Link Test Topology 4");

        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();
        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();

        VertexType kType = VertexType.ROUTER;
        VertexType lType = VertexType.SWITCH;
        VertexType mType = VertexType.SWITCH;
        VertexType nType = VertexType.ROUTER;

        buildSharedLinkTopology(topoNodes, topoLinks, portDeviceMap, kType, lType, mType, nType);

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap);
    }

    private void buildSharedLinkTopology(List<TopoVertex> topoNodes, List<TopoEdge> topoLinks, Map<TopoVertex,TopoVertex> portDeviceMap, VertexType typeK, VertexType typeL, VertexType typeM, VertexType typeN)
    {
        class VertTypeTuple<V, T>
        {
            public final V v;
            public final T t;

            public VertTypeTuple(V vSpec, T tSpec)
            {
                v = vSpec;
                t = tSpec;
            }
        }

        List<VertTypeTuple<TopoVertex, VertexType>> networkPorts = new ArrayList<>();

        // Devices //
        TopoVertex nodeK = new TopoVertex("nodeK", typeK);
        TopoVertex nodeL = new TopoVertex("nodeL", typeL);
        TopoVertex nodeM = new TopoVertex("nodeM", typeM);
        TopoVertex nodeN = new TopoVertex("nodeN", typeN);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portK1 = new TopoVertex("nodeK:1", VertexType.PORT);
        TopoVertex portK2 = new TopoVertex("nodeK:2", VertexType.PORT);
        TopoVertex portL1 = new TopoVertex("nodeL:1", VertexType.PORT);
        TopoVertex portL2 = new TopoVertex("nodeL:2", VertexType.PORT);
        TopoVertex portL3 = new TopoVertex("nodeL:3", VertexType.PORT);
        TopoVertex portM1 = new TopoVertex("nodeM:1", VertexType.PORT);
        TopoVertex portM2 = new TopoVertex("nodeM:2", VertexType.PORT);
        TopoVertex portM3 = new TopoVertex("nodeM:3", VertexType.PORT);
        TopoVertex portN1 = new TopoVertex("nodeN:1", VertexType.PORT);
        TopoVertex portN2 = new TopoVertex("nodeN:2", VertexType.PORT);

        networkPorts.add(new VertTypeTuple<>(portK1, nodeK.getVertexType()));
        networkPorts.add(new VertTypeTuple<>(portK2, nodeK.getVertexType()));
        networkPorts.add(new VertTypeTuple<>(portL1, nodeL.getVertexType()));
        networkPorts.add(new VertTypeTuple<>(portL2, nodeL.getVertexType()));
        networkPorts.add(new VertTypeTuple<>(portL3, nodeL.getVertexType()));
        networkPorts.add(new VertTypeTuple<>(portM1, nodeM.getVertexType()));
        networkPorts.add(new VertTypeTuple<>(portM2, nodeM.getVertexType()));
        networkPorts.add(new VertTypeTuple<>(portM3, nodeM.getVertexType()));
        networkPorts.add(new VertTypeTuple<>(portN1, nodeN.getVertexType()));
        networkPorts.add(new VertTypeTuple<>(portN2, nodeN.getVertexType()));

        // End-Port Links //
        TopoEdge edgeInt_A_K = new TopoEdge(portA, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_N = new TopoEdge(portZ, nodeN, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_A = new TopoEdge(nodeK, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N_Z = new TopoEdge(nodeN, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_K1_K = new TopoEdge(portK1, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K2_K = new TopoEdge(portK2, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L1_L = new TopoEdge(portL1, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L2_L = new TopoEdge(portL2, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L3_L = new TopoEdge(portL3, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M1_M = new TopoEdge(portM1, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M2_M = new TopoEdge(portM2, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M3_M = new TopoEdge(portM3, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N1_N = new TopoEdge(portN1, nodeN, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N2_N = new TopoEdge(portN2, nodeN, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_K_K1 = new TopoEdge(nodeK, portK1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_K2 = new TopoEdge(nodeK, portK2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L1 = new TopoEdge(nodeL, portL1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L2 = new TopoEdge(nodeL, portL2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L3 = new TopoEdge(nodeL, portL3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M1 = new TopoEdge(nodeM, portM1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M2 = new TopoEdge(nodeM, portM2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M3 = new TopoEdge(nodeM, portM3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N_N1 = new TopoEdge(nodeN, portN1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N_N2 = new TopoEdge(nodeN, portN2, 0L, Layer.INTERNAL);


        // Network Links //
        for(VertTypeTuple onePort : networkPorts)
        {
            TopoVertex oneVertex = (TopoVertex)onePort.v;
            VertexType oneType = (VertexType)onePort.t;

            for(VertTypeTuple anotherPort : networkPorts)
            {
                TopoVertex anotherVertex = (TopoVertex)anotherPort.v;
                VertexType anotherType = (VertexType)anotherPort.t;

                TopoEdge edgeNet;
                Layer linkLayer = Layer.ETHERNET;
                Long linkMetric;
                if(oneVertex.getUrn().equals("nodeK:1") && anotherVertex.getUrn().equals("nodeL:1"))
                    linkMetric = 50L;
                else if(oneVertex.getUrn().equals("nodeK:2") && anotherVertex.getUrn().equals("nodeM:1"))
                    linkMetric = 300L;
                else if(oneVertex.getUrn().equals("nodeL:1") && anotherVertex.getUrn().equals("nodeK:1"))
                    linkMetric = 200L;
                else if(oneVertex.getUrn().equals("nodeL:2") && anotherVertex.getUrn().equals("nodeN:1"))
                    linkMetric = 200L;
                else if(oneVertex.getUrn().equals("nodeL:3") && anotherVertex.getUrn().equals("nodeM:2"))
                    linkMetric = 50L;
                else if(oneVertex.getUrn().equals("nodeM:1") && anotherVertex.getUrn().equals("nodeK:2"))
                    linkMetric = 50L;
                else if(oneVertex.getUrn().equals("nodeM:2") && anotherVertex.getUrn().equals("nodeL:3"))
                    linkMetric = 100L;
                else if(oneVertex.getUrn().equals("nodeM:3") && anotherVertex.getUrn().equals("nodeN:2"))
                    linkMetric = 50L;
                else if(oneVertex.getUrn().equals("nodeN:1") && anotherVertex.getUrn().equals("nodeL:2"))
                    linkMetric = 50L;
                else if(oneVertex.getUrn().equals("nodeN:2") && anotherVertex.getUrn().equals("nodeM:3"))
                    linkMetric = 300L;
                else
                    continue;


                if(oneType.equals(VertexType.ROUTER) && anotherType.equals(VertexType.ROUTER))
                {
                    linkLayer = Layer.MPLS;
                }

                edgeNet = new TopoEdge(oneVertex, anotherVertex, linkMetric, linkLayer);
                topoLinks.add(edgeNet);
            }
        }

        topoNodes.add(nodeK);
        topoNodes.add(nodeL);
        topoNodes.add(nodeM);
        topoNodes.add(nodeN);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portK1);
        topoNodes.add(portK2);
        topoNodes.add(portL1);
        topoNodes.add(portL2);
        topoNodes.add(portL3);
        topoNodes.add(portM1);
        topoNodes.add(portM2);
        topoNodes.add(portM3);
        topoNodes.add(portN1);
        topoNodes.add(portN2);

        topoLinks.add(edgeInt_A_K);
        topoLinks.add(edgeInt_Z_N);
        topoLinks.add(edgeInt_K1_K);
        topoLinks.add(edgeInt_K2_K);
        topoLinks.add(edgeInt_L1_L);
        topoLinks.add(edgeInt_L2_L);
        topoLinks.add(edgeInt_L3_L);
        topoLinks.add(edgeInt_M1_M);
        topoLinks.add(edgeInt_M2_M);
        topoLinks.add(edgeInt_M3_M);
        topoLinks.add(edgeInt_N1_N);
        topoLinks.add(edgeInt_N2_N);

        topoLinks.add(edgeInt_K_A);
        topoLinks.add(edgeInt_N_Z);
        topoLinks.add(edgeInt_K_K1);
        topoLinks.add(edgeInt_K_K2);
        topoLinks.add(edgeInt_L_L1);
        topoLinks.add(edgeInt_L_L2);
        topoLinks.add(edgeInt_L_L3);
        topoLinks.add(edgeInt_M_M1);
        topoLinks.add(edgeInt_M_M2);
        topoLinks.add(edgeInt_M_M3);
        topoLinks.add(edgeInt_N_N1);
        topoLinks.add(edgeInt_N_N2);

        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }
    }

    public void buildTopo7MultiFix()
    {
        log.info("Building Test Topology 7 with multiple src/dest fixtures");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();

        // Devices //
        TopoVertex nodeK = new TopoVertex("nodeK", VertexType.SWITCH);
        TopoVertex nodeL = new TopoVertex("nodeL", VertexType.SWITCH);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portB = new TopoVertex("portB", VertexType.PORT);
        TopoVertex portC = new TopoVertex("portC", VertexType.PORT);
        TopoVertex portX = new TopoVertex("portX", VertexType.PORT);
        TopoVertex portY = new TopoVertex("portY", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portK1 = new TopoVertex("nodeK:1", VertexType.PORT);
        TopoVertex portL1 = new TopoVertex("nodeL:1", VertexType.PORT);

        // End-Port Links //
        TopoEdge edgeInt_A_K = new TopoEdge(portA, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B_K = new TopoEdge(portB, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_C_K = new TopoEdge(portC, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_X_L = new TopoEdge(portX, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Y_L = new TopoEdge(portY, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_L = new TopoEdge(portZ, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_A = new TopoEdge(nodeK, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_B = new TopoEdge(nodeK, portB, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_C = new TopoEdge(nodeK, portC, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_X = new TopoEdge(nodeL, portX, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_Y = new TopoEdge(nodeL, portY, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_Z = new TopoEdge(nodeL, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_K1_K = new TopoEdge(portK1, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L1_L = new TopoEdge(portL1, nodeL, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_K_K1 = new TopoEdge(nodeK, portK1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L1 = new TopoEdge(nodeL, portL1, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeEth_K1_L1 = new TopoEdge(portK1, portL1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L1_K1 = new TopoEdge(portL1, portK1, 100L, Layer.ETHERNET);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();

        topoNodes.add(nodeK);
        topoNodes.add(nodeL);

        topoNodes.add(portA);
        topoNodes.add(portB);
        topoNodes.add(portC);
        topoNodes.add(portX);
        topoNodes.add(portY);
        topoNodes.add(portZ);
        topoNodes.add(portK1);
        topoNodes.add(portL1);

        topoLinks.add(edgeInt_A_K);
        topoLinks.add(edgeInt_B_K);
        topoLinks.add(edgeInt_C_K);
        topoLinks.add(edgeInt_X_L);
        topoLinks.add(edgeInt_Y_L);
        topoLinks.add(edgeInt_Z_L);
        topoLinks.add(edgeInt_K1_K);
        topoLinks.add(edgeInt_L1_L);

        topoLinks.add(edgeInt_K_A);
        topoLinks.add(edgeInt_K_B);
        topoLinks.add(edgeInt_K_C);
        topoLinks.add(edgeInt_L_X);
        topoLinks.add(edgeInt_L_Y);
        topoLinks.add(edgeInt_L_Z);
        topoLinks.add(edgeInt_K_K1);
        topoLinks.add(edgeInt_L_L1);

        topoLinks.add(edgeEth_K1_L1);
        topoLinks.add(edgeEth_L1_K1);


        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap);
    }


    public void buildMultiMplsTopo1()
    {
        log.info("Building Multi-MPLS Segment Test Topology 1");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();

        // Devices //
        TopoVertex nodeK = new TopoVertex("nodeK", VertexType.ROUTER);
        TopoVertex nodeM = new TopoVertex("nodeM", VertexType.ROUTER);
        TopoVertex nodeP = new TopoVertex("nodeP", VertexType.SWITCH);
        TopoVertex nodeQ = new TopoVertex("nodeQ", VertexType.SWITCH);
        TopoVertex nodeR = new TopoVertex("nodeR", VertexType.SWITCH);
        TopoVertex nodeS = new TopoVertex("nodeS", VertexType.ROUTER);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portK1 = new TopoVertex("nodeK:1", VertexType.PORT);
        TopoVertex portK2 = new TopoVertex("nodeK:2", VertexType.PORT);
        TopoVertex portM1 = new TopoVertex("nodeM:1", VertexType.PORT);
        TopoVertex portM2 = new TopoVertex("nodeM:2", VertexType.PORT);
        TopoVertex portM3 = new TopoVertex("nodeM:3", VertexType.PORT);
        TopoVertex portP1 = new TopoVertex("nodeP:1", VertexType.PORT);
        TopoVertex portP2 = new TopoVertex("nodeP:2", VertexType.PORT);
        TopoVertex portP3 = new TopoVertex("nodeP:3", VertexType.PORT);
        TopoVertex portP4 = new TopoVertex("nodeP:4", VertexType.PORT);
        TopoVertex portQ1 = new TopoVertex("nodeQ:1", VertexType.PORT);
        TopoVertex portQ2 = new TopoVertex("nodeQ:2", VertexType.PORT);
        TopoVertex portQ3 = new TopoVertex("nodeQ:3", VertexType.PORT);
        TopoVertex portR1 = new TopoVertex("nodeR:1", VertexType.PORT);
        TopoVertex portR2 = new TopoVertex("nodeR:2", VertexType.PORT);
        TopoVertex portR3 = new TopoVertex("nodeR:3", VertexType.PORT);
        TopoVertex portS1 = new TopoVertex("nodeS:1", VertexType.PORT);

        // End-Port Links //
        TopoEdge edgeInt_A_K = new TopoEdge(portA, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_S = new TopoEdge(portZ, nodeS, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_A = new TopoEdge(nodeK, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S_Z = new TopoEdge(nodeS, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_K1_K = new TopoEdge(portK1, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K2_K = new TopoEdge(portK2, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M1_M = new TopoEdge(portM1, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M2_M = new TopoEdge(portM2, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M3_M = new TopoEdge(portM3, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P1_P = new TopoEdge(portP1, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P2_P = new TopoEdge(portP2, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P3_P = new TopoEdge(portP3, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P4_P = new TopoEdge(portP4, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q1_Q = new TopoEdge(portQ1, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q2_Q = new TopoEdge(portQ2, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q3_Q = new TopoEdge(portQ3, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R1_R = new TopoEdge(portR1, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R2_R = new TopoEdge(portR2, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R3_R = new TopoEdge(portR3, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S1_S = new TopoEdge(portS1, nodeS, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_K_K1 = new TopoEdge(nodeK, portK1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_K2 = new TopoEdge(nodeK, portK2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M1 = new TopoEdge(nodeM, portM1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M2 = new TopoEdge(nodeM, portM2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M3 = new TopoEdge(nodeM, portM3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P1 = new TopoEdge(nodeP, portP1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P2 = new TopoEdge(nodeP, portP2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P3 = new TopoEdge(nodeP, portP3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P4 = new TopoEdge(nodeP, portP4, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q1 = new TopoEdge(nodeQ, portQ1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q2 = new TopoEdge(nodeQ, portQ2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q3 = new TopoEdge(nodeQ, portQ3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R1 = new TopoEdge(nodeR, portR1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R2 = new TopoEdge(nodeR, portR2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R3 = new TopoEdge(nodeR, portR3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S_S1 = new TopoEdge(nodeS, portS1, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeMpls_K1_M1 = new TopoEdge(portK1, portM1, 100L, Layer.MPLS);
        TopoEdge edgeEth_K2_P1 = new TopoEdge(portK2, portP1, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_M1_K1 = new TopoEdge(portM1, portK1, 100L, Layer.MPLS);
        TopoEdge edgeEth_M2_P2 = new TopoEdge(portM2, portP2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M3_R1 = new TopoEdge(portM3, portR1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_P1_K2 = new TopoEdge(portP1, portK2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_P2_M2 = new TopoEdge(portP2, portM2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_P3_Q1 = new TopoEdge(portP3, portQ1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_P4_R2 = new TopoEdge(portP4, portR2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_Q1_P3 = new TopoEdge(portQ1, portP3, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_Q2_R3 = new TopoEdge(portQ2, portR3, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_Q3_S1 = new TopoEdge(portQ3, portS1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_R1_M3 = new TopoEdge(portR1, portM3, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_R2_P4 = new TopoEdge(portR2, portP4, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_R3_Q2 = new TopoEdge(portR3, portQ2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_S1_Q3 = new TopoEdge(portS1, portQ3, 100L, Layer.ETHERNET);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();

        topoNodes.add(nodeK);
        topoNodes.add(nodeM);
        topoNodes.add(nodeP);
        topoNodes.add(nodeQ);
        topoNodes.add(nodeR);
        topoNodes.add(nodeR);
        topoNodes.add(nodeS);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portK1);
        topoNodes.add(portK2);
        topoNodes.add(portM1);
        topoNodes.add(portM2);
        topoNodes.add(portM3);
        topoNodes.add(portP1);
        topoNodes.add(portP2);
        topoNodes.add(portP3);
        topoNodes.add(portP4);
        topoNodes.add(portQ1);
        topoNodes.add(portQ2);
        topoNodes.add(portQ3);
        topoNodes.add(portR1);
        topoNodes.add(portR2);
        topoNodes.add(portR3);
        topoNodes.add(portS1);

        topoLinks.add(edgeInt_A_K);
        topoLinks.add(edgeInt_Z_S);
        topoLinks.add(edgeInt_K1_K);
        topoLinks.add(edgeInt_K2_K);
        topoLinks.add(edgeInt_M1_M);
        topoLinks.add(edgeInt_M2_M);
        topoLinks.add(edgeInt_M3_M);
        topoLinks.add(edgeInt_P1_P);
        topoLinks.add(edgeInt_P2_P);
        topoLinks.add(edgeInt_P3_P);
        topoLinks.add(edgeInt_P4_P);
        topoLinks.add(edgeInt_Q1_Q);
        topoLinks.add(edgeInt_Q2_Q);
        topoLinks.add(edgeInt_Q3_Q);
        topoLinks.add(edgeInt_R1_R);
        topoLinks.add(edgeInt_R2_R);
        topoLinks.add(edgeInt_R3_R);
        topoLinks.add(edgeInt_S1_S);

        topoLinks.add(edgeInt_K_A);
        topoLinks.add(edgeInt_S_Z);
        topoLinks.add(edgeInt_K_K1);
        topoLinks.add(edgeInt_K_K2);
        topoLinks.add(edgeInt_M_M1);
        topoLinks.add(edgeInt_M_M2);
        topoLinks.add(edgeInt_M_M3);
        topoLinks.add(edgeInt_P_P1);
        topoLinks.add(edgeInt_P_P2);
        topoLinks.add(edgeInt_P_P3);
        topoLinks.add(edgeInt_P_P4);
        topoLinks.add(edgeInt_Q_Q1);
        topoLinks.add(edgeInt_Q_Q2);
        topoLinks.add(edgeInt_Q_Q3);
        topoLinks.add(edgeInt_R_R1);
        topoLinks.add(edgeInt_R_R2);
        topoLinks.add(edgeInt_R_R3);
        topoLinks.add(edgeInt_S_S1);

        topoLinks.add(edgeMpls_K1_M1);
        topoLinks.add(edgeEth_K2_P1);
        topoLinks.add(edgeMpls_M1_K1);
        topoLinks.add(edgeEth_M2_P2);
        topoLinks.add(edgeEth_M3_R1);
        topoLinks.add(edgeEth_P1_K2);
        topoLinks.add(edgeEth_P2_M2);
        topoLinks.add(edgeEth_P3_Q1);
        topoLinks.add(edgeEth_P4_R2);
        topoLinks.add(edgeEth_Q1_P3);
        topoLinks.add(edgeEth_Q2_R3);
        topoLinks.add(edgeEth_Q3_S1);
        topoLinks.add(edgeEth_R1_M3);
        topoLinks.add(edgeEth_R2_P4);
        topoLinks.add(edgeEth_R3_Q2);
        topoLinks.add(edgeEth_S1_Q3);

        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap);
    }

    public void buildMultiMplsTopo2()
    {
        log.info("Building Multi-MPLS Segment Test Topology 2");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();

        // Devices //
        TopoVertex nodeK = new TopoVertex("nodeK", VertexType.ROUTER);
        TopoVertex nodeL = new TopoVertex("nodeL", VertexType.ROUTER);
        TopoVertex nodeM = new TopoVertex("nodeM", VertexType.ROUTER);
        TopoVertex nodeP = new TopoVertex("nodeP", VertexType.SWITCH);
        TopoVertex nodeQ = new TopoVertex("nodeQ", VertexType.SWITCH);
        TopoVertex nodeR = new TopoVertex("nodeR", VertexType.SWITCH);
        TopoVertex nodeS = new TopoVertex("nodeS", VertexType.ROUTER);
        TopoVertex nodeT = new TopoVertex("nodeT", VertexType.ROUTER);
        TopoVertex nodeU = new TopoVertex("nodeU", VertexType.ROUTER);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portK1 = new TopoVertex("nodeK:1", VertexType.PORT);
        TopoVertex portK2 = new TopoVertex("nodeK:2", VertexType.PORT);
        TopoVertex portL1 = new TopoVertex("nodeL:1", VertexType.PORT);
        TopoVertex portL2 = new TopoVertex("nodeL:2", VertexType.PORT);
        TopoVertex portL3 = new TopoVertex("nodeL:3", VertexType.PORT);
        TopoVertex portM1 = new TopoVertex("nodeM:1", VertexType.PORT);
        TopoVertex portM2 = new TopoVertex("nodeM:2", VertexType.PORT);
        TopoVertex portP1 = new TopoVertex("nodeP:1", VertexType.PORT);
        TopoVertex portP2 = new TopoVertex("nodeP:2", VertexType.PORT);
        TopoVertex portP3 = new TopoVertex("nodeP:3", VertexType.PORT);
        TopoVertex portQ1 = new TopoVertex("nodeQ:1", VertexType.PORT);
        TopoVertex portQ2 = new TopoVertex("nodeQ:2", VertexType.PORT);
        TopoVertex portQ3 = new TopoVertex("nodeQ:3", VertexType.PORT);
        TopoVertex portR1 = new TopoVertex("nodeR:1", VertexType.PORT);
        TopoVertex portR2 = new TopoVertex("nodeR:2", VertexType.PORT);
        TopoVertex portS1 = new TopoVertex("nodeS:1", VertexType.PORT);
        TopoVertex portS2 = new TopoVertex("nodeS:2", VertexType.PORT);
        TopoVertex portS3 = new TopoVertex("nodeS:3", VertexType.PORT);
        TopoVertex portT1 = new TopoVertex("nodeT:1", VertexType.PORT);
        TopoVertex portT2 = new TopoVertex("nodeT:2", VertexType.PORT);
        TopoVertex portU1 = new TopoVertex("nodeU:1", VertexType.PORT);
        TopoVertex portU2 = new TopoVertex("nodeU:2", VertexType.PORT);

        // End-Port Links //
        TopoEdge edgeInt_A_K = new TopoEdge(portA, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_T = new TopoEdge(portZ, nodeT, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_A = new TopoEdge(nodeK, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_T_Z = new TopoEdge(nodeT, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_K1_K = new TopoEdge(portK1, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K2_K = new TopoEdge(portK2, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L1_L = new TopoEdge(portL1, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L2_L = new TopoEdge(portL2, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L3_L = new TopoEdge(portL3, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M1_M = new TopoEdge(portM1, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M2_M = new TopoEdge(portM2, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P1_P = new TopoEdge(portP1, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P2_P = new TopoEdge(portP2, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P3_P = new TopoEdge(portP3, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q1_Q = new TopoEdge(portQ1, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q2_Q = new TopoEdge(portQ2, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q3_Q = new TopoEdge(portQ3, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R1_R = new TopoEdge(portR1, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R2_R = new TopoEdge(portR2, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S1_S = new TopoEdge(portS1, nodeS, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S2_S = new TopoEdge(portS2, nodeS, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S3_S = new TopoEdge(portS3, nodeS, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_T1_T = new TopoEdge(portT1, nodeT, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_T2_T = new TopoEdge(portT2, nodeT, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_U1_U = new TopoEdge(portU1, nodeU, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_U2_U = new TopoEdge(portU2, nodeU, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_K_K1 = new TopoEdge(nodeK, portK1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_K2 = new TopoEdge(nodeK, portK2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L1 = new TopoEdge(nodeL, portL1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L2 = new TopoEdge(nodeL, portL2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L3 = new TopoEdge(nodeL, portL3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M1 = new TopoEdge(nodeM, portM1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M2 = new TopoEdge(nodeM, portM2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P1 = new TopoEdge(nodeP, portP1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P2 = new TopoEdge(nodeP, portP2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P3 = new TopoEdge(nodeP, portP3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q1 = new TopoEdge(nodeQ, portQ1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q2 = new TopoEdge(nodeQ, portQ2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q3 = new TopoEdge(nodeQ, portQ3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R1 = new TopoEdge(nodeR, portR1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R2 = new TopoEdge(nodeR, portR2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S_S1 = new TopoEdge(nodeS, portS1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S_S2 = new TopoEdge(nodeS, portS2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S_S3 = new TopoEdge(nodeS, portS3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_T_T1 = new TopoEdge(nodeT, portT1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_T_T2 = new TopoEdge(nodeT, portT2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_U_U1 = new TopoEdge(nodeU, portU1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_U_U2 = new TopoEdge(nodeU, portU2, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeMpls_K1_L1 = new TopoEdge(portK1, portL1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_K2_M1 = new TopoEdge(portK2, portM1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_L1_K1 = new TopoEdge(portL1, portK1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_L2_M2 = new TopoEdge(portL2, portM2, 100L, Layer.MPLS);
        TopoEdge edgeEth_L3_P1 = new TopoEdge(portL3, portP1, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_M1_K2 = new TopoEdge(portM1, portK2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_M2_L2 = new TopoEdge(portM2, portL2, 100L, Layer.MPLS);
        TopoEdge edgeEth_P1_L3 = new TopoEdge(portP1, portL3, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_P2_Q1 = new TopoEdge(portP2, portQ1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_P3_R1 = new TopoEdge(portP3, portR1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_Q1_P2 = new TopoEdge(portQ1, portP2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_Q2_R2 = new TopoEdge(portQ2, portR2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_Q3_S1 = new TopoEdge(portQ3, portS1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_R1_P3 = new TopoEdge(portR1, portP3, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_R2_Q2 = new TopoEdge(portR2, portQ2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_S1_Q3 = new TopoEdge(portS1, portQ3, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_S2_T1 = new TopoEdge(portS2, portT1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_S3_U1 = new TopoEdge(portS3, portU1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_T1_S2 = new TopoEdge(portT1, portS2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_T2_U2 = new TopoEdge(portT2, portU2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_U1_S3 = new TopoEdge(portU1, portS3, 100L, Layer.MPLS);
        TopoEdge edgeMpls_U2_T2 = new TopoEdge(portU2, portT2, 100L, Layer.MPLS);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();

        topoNodes.add(nodeK);
        topoNodes.add(nodeL);
        topoNodes.add(nodeM);
        topoNodes.add(nodeP);
        topoNodes.add(nodeQ);
        topoNodes.add(nodeR);
        topoNodes.add(nodeS);
        topoNodes.add(nodeT);
        topoNodes.add(nodeU);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portK1);
        topoNodes.add(portK2);
        topoNodes.add(portL1);
        topoNodes.add(portL2);
        topoNodes.add(portL3);
        topoNodes.add(portM1);
        topoNodes.add(portM2);
        topoNodes.add(portP1);
        topoNodes.add(portP2);
        topoNodes.add(portP3);
        topoNodes.add(portQ1);
        topoNodes.add(portQ2);
        topoNodes.add(portQ3);
        topoNodes.add(portR1);
        topoNodes.add(portR2);
        topoNodes.add(portS1);
        topoNodes.add(portS2);
        topoNodes.add(portS3);
        topoNodes.add(portT1);
        topoNodes.add(portT2);
        topoNodes.add(portU1);
        topoNodes.add(portU2);

        topoLinks.add(edgeInt_A_K);
        topoLinks.add(edgeInt_Z_T);
        topoLinks.add(edgeInt_K1_K);
        topoLinks.add(edgeInt_K2_K);
        topoLinks.add(edgeInt_L1_L);
        topoLinks.add(edgeInt_L2_L);
        topoLinks.add(edgeInt_L3_L);
        topoLinks.add(edgeInt_M1_M);
        topoLinks.add(edgeInt_M2_M);
        topoLinks.add(edgeInt_P1_P);
        topoLinks.add(edgeInt_P2_P);
        topoLinks.add(edgeInt_P3_P);
        topoLinks.add(edgeInt_Q1_Q);
        topoLinks.add(edgeInt_Q2_Q);
        topoLinks.add(edgeInt_Q3_Q);
        topoLinks.add(edgeInt_R1_R);
        topoLinks.add(edgeInt_R2_R);
        topoLinks.add(edgeInt_S1_S);
        topoLinks.add(edgeInt_S2_S);
        topoLinks.add(edgeInt_S3_S);
        topoLinks.add(edgeInt_T1_T);
        topoLinks.add(edgeInt_T2_T);
        topoLinks.add(edgeInt_U1_U);
        topoLinks.add(edgeInt_U2_U);

        topoLinks.add(edgeInt_K_A);
        topoLinks.add(edgeInt_T_Z);
        topoLinks.add(edgeInt_K_K1);
        topoLinks.add(edgeInt_K_K2);
        topoLinks.add(edgeInt_L_L1);
        topoLinks.add(edgeInt_L_L2);
        topoLinks.add(edgeInt_L_L3);
        topoLinks.add(edgeInt_M_M1);
        topoLinks.add(edgeInt_M_M2);
        topoLinks.add(edgeInt_P_P1);
        topoLinks.add(edgeInt_P_P2);
        topoLinks.add(edgeInt_P_P3);
        topoLinks.add(edgeInt_Q_Q1);
        topoLinks.add(edgeInt_Q_Q2);
        topoLinks.add(edgeInt_Q_Q3);
        topoLinks.add(edgeInt_R_R1);
        topoLinks.add(edgeInt_R_R2);
        topoLinks.add(edgeInt_S_S1);
        topoLinks.add(edgeInt_S_S2);
        topoLinks.add(edgeInt_S_S3);
        topoLinks.add(edgeInt_T_T1);
        topoLinks.add(edgeInt_T_T2);
        topoLinks.add(edgeInt_U_U1);
        topoLinks.add(edgeInt_U_U2);

        topoLinks.add(edgeMpls_K1_L1);
        topoLinks.add(edgeMpls_K2_M1);
        topoLinks.add(edgeMpls_L1_K1);
        topoLinks.add(edgeMpls_L2_M2);
        topoLinks.add(edgeEth_L3_P1);
        topoLinks.add(edgeMpls_M1_K2);
        topoLinks.add(edgeMpls_M2_L2);
        topoLinks.add(edgeEth_P1_L3);
        topoLinks.add(edgeEth_P2_Q1);
        topoLinks.add(edgeEth_P3_R1);
        topoLinks.add(edgeEth_Q1_P2);
        topoLinks.add(edgeEth_Q2_R2);
        topoLinks.add(edgeEth_Q3_S1);
        topoLinks.add(edgeEth_R1_P3);
        topoLinks.add(edgeEth_R2_Q2);
        topoLinks.add(edgeEth_S1_Q3);
        topoLinks.add(edgeMpls_S2_T1);
        topoLinks.add(edgeMpls_S3_U1);
        topoLinks.add(edgeMpls_T1_S2);
        topoLinks.add(edgeMpls_T2_U2);
        topoLinks.add(edgeMpls_U1_S3);
        topoLinks.add(edgeMpls_U2_T2);

        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap);
    }

    public void buildTopoFourPaths()
    {
        log.info("Building Test Topology Four Paths");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();

        // Devices //
        TopoVertex nodeK = new TopoVertex("nodeK", VertexType.SWITCH);
        TopoVertex nodeL = new TopoVertex("nodeL", VertexType.SWITCH);
        TopoVertex nodeM = new TopoVertex("nodeM", VertexType.SWITCH);
        TopoVertex nodeP = new TopoVertex("nodeP", VertexType.ROUTER);
        TopoVertex nodeQ = new TopoVertex("nodeQ", VertexType.ROUTER);
        TopoVertex nodeR = new TopoVertex("nodeR", VertexType.ROUTER);

        TopoVertex nodeW = new TopoVertex("nodeW", VertexType.ROUTER);
        TopoVertex nodeX = new TopoVertex("nodeX", VertexType.ROUTER);
        TopoVertex nodeY = new TopoVertex("nodeY", VertexType.SWITCH);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portK1 = new TopoVertex("nodeK:1", VertexType.PORT);
        TopoVertex portK2 = new TopoVertex("nodeK:2", VertexType.PORT);
        TopoVertex portL1 = new TopoVertex("nodeL:1", VertexType.PORT);
        TopoVertex portL2 = new TopoVertex("nodeL:2", VertexType.PORT);
        TopoVertex portL3 = new TopoVertex("nodeL:3", VertexType.PORT);
        TopoVertex portM1 = new TopoVertex("nodeM:1", VertexType.PORT);
        TopoVertex portM2 = new TopoVertex("nodeM:2", VertexType.PORT);
        TopoVertex portM3 = new TopoVertex("nodeM:3", VertexType.PORT);
        TopoVertex portP1 = new TopoVertex("nodeP:1", VertexType.PORT);
        TopoVertex portP2 = new TopoVertex("nodeP:2", VertexType.PORT);
        TopoVertex portP3 = new TopoVertex("nodeP:3", VertexType.PORT);
        TopoVertex portQ1 = new TopoVertex("nodeQ:1", VertexType.PORT);
        TopoVertex portQ2 = new TopoVertex("nodeQ:2", VertexType.PORT);
        TopoVertex portR1 = new TopoVertex("nodeR:1", VertexType.PORT);
        TopoVertex portR2 = new TopoVertex("nodeR:2", VertexType.PORT);
        TopoVertex portR3 = new TopoVertex("nodeR:3", VertexType.PORT);

        TopoVertex portW1 = new TopoVertex("nodeW:1", VertexType.PORT);
        TopoVertex portW2 = new TopoVertex("nodeW:2", VertexType.PORT);
        TopoVertex portX1 = new TopoVertex("nodeX:1", VertexType.PORT);
        TopoVertex portX2 = new TopoVertex("nodeX:2", VertexType.PORT);
        TopoVertex portY1 = new TopoVertex("nodeY:1", VertexType.PORT);
        TopoVertex portY2 = new TopoVertex("nodeY:2", VertexType.PORT);

        TopoVertex portK3 = new TopoVertex("nodeK:3", VertexType.PORT);
        TopoVertex portK4 = new TopoVertex("nodeK:4", VertexType.PORT);

        TopoVertex portQ3 = new TopoVertex("nodeQ:3", VertexType.PORT);
        TopoVertex portQ4 = new TopoVertex("nodeQ:4", VertexType.PORT);

        // End-Port Links //
        TopoEdge edgeInt_A_K = new TopoEdge(portA, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_Q = new TopoEdge(portZ, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_A = new TopoEdge(nodeK, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Z = new TopoEdge(nodeQ, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_K1_K = new TopoEdge(portK1, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K2_K = new TopoEdge(portK2, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L1_L = new TopoEdge(portL1, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L2_L = new TopoEdge(portL2, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L3_L = new TopoEdge(portL3, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M1_M = new TopoEdge(portM1, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M2_M = new TopoEdge(portM2, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M3_M = new TopoEdge(portM3, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P1_P = new TopoEdge(portP1, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P2_P = new TopoEdge(portP2, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P3_P = new TopoEdge(portP3, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q1_Q = new TopoEdge(portQ1, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q2_Q = new TopoEdge(portQ2, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R1_R = new TopoEdge(portR1, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R2_R = new TopoEdge(portR2, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R3_R = new TopoEdge(portR3, nodeR, 0L, Layer.INTERNAL);

        TopoEdge edgeInt_W1_W = new TopoEdge(portW1, nodeW, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_W2_W = new TopoEdge(portW2, nodeW, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_X1_X = new TopoEdge(portX1, nodeX, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_X2_X = new TopoEdge(portX2, nodeX, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Y1_Y = new TopoEdge(portY1, nodeY, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Y2_Y = new TopoEdge(portY2, nodeY, 0L, Layer.INTERNAL);

        TopoEdge edgeInt_K3_K = new TopoEdge(portK3, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K4_K = new TopoEdge(portK4, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q3_Q = new TopoEdge(portQ3, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q4_Q = new TopoEdge(portQ4, nodeQ, 0L, Layer.INTERNAL);


        // Internal-Reverse Links //
        TopoEdge edgeInt_K_K1 = new TopoEdge(nodeK, portK1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_K2 = new TopoEdge(nodeK, portK2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L1 = new TopoEdge(nodeL, portL1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L2 = new TopoEdge(nodeL, portL2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L3 = new TopoEdge(nodeL, portL3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M1 = new TopoEdge(nodeM, portM1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M2 = new TopoEdge(nodeM, portM2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M3 = new TopoEdge(nodeM, portM3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P1 = new TopoEdge(nodeP, portP1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P2 = new TopoEdge(nodeP, portP2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P3 = new TopoEdge(nodeP, portP3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q1 = new TopoEdge(nodeQ, portQ1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q2 = new TopoEdge(nodeQ, portQ2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R1 = new TopoEdge(nodeR, portR1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R2 = new TopoEdge(nodeR, portR2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R3 = new TopoEdge(nodeR, portR3, 0L, Layer.INTERNAL);

        TopoEdge edgeInt_W_W1 = new TopoEdge(nodeW, portW1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_W_W2 = new TopoEdge(nodeW, portW2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_X_X1 = new TopoEdge(nodeX, portX1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_X_X2 = new TopoEdge(nodeX, portX2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Y_Y1 = new TopoEdge(nodeY, portY1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Y_Y2 = new TopoEdge(nodeY, portY2, 0L, Layer.INTERNAL);

        TopoEdge edgeInt_K_K3 = new TopoEdge(nodeK, portK3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_K4 = new TopoEdge(nodeK, portK4, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q3 = new TopoEdge(nodeQ, portQ3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q4 = new TopoEdge(nodeQ, portQ4, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeEth_K1_L1 = new TopoEdge(portK1, portL1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_K2_M1 = new TopoEdge(portK2, portM1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L1_K1 = new TopoEdge(portL1, portK1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L2_M2 = new TopoEdge(portL2, portM2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L3_P1 = new TopoEdge(portL3, portP1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M1_K2 = new TopoEdge(portM1, portK2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M2_L2 = new TopoEdge(portM2, portL2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M3_R1 = new TopoEdge(portM3, portR1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_P1_L3 = new TopoEdge(portP1, portL3, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_P2_Q1 = new TopoEdge(portP2, portQ1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_P3_R2 = new TopoEdge(portP3, portR2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q1_P2 = new TopoEdge(portQ1, portP2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q2_R3 = new TopoEdge(portQ2, portR3, 100L, Layer.MPLS);
        TopoEdge edgeEth_R1_M3 = new TopoEdge(portR1, portM3, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_R2_P3 = new TopoEdge(portR2, portP3, 100L, Layer.MPLS);
        TopoEdge edgeMpls_R3_Q2 = new TopoEdge(portR3, portQ2, 100L, Layer.MPLS);

        TopoEdge edgeEth_K3_W1 = new TopoEdge(portK3, portW1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_K4_X1 = new TopoEdge(portK4, portX1, 100L, Layer.ETHERNET);
        TopoEdge edgeEthW1_K3 = new TopoEdge(portW1, portK3, 100L, Layer.ETHERNET);
        TopoEdge edgeEthX1_K4 = new TopoEdge(portX1, portK4, 100L, Layer.ETHERNET);

        TopoEdge edgeEthX2_Y1 = new TopoEdge(portX2, portY1, 100L, Layer.ETHERNET);
        TopoEdge edgeEthY1_X2 = new TopoEdge(portY1, portX2, 100L, Layer.ETHERNET);

        TopoEdge edgeEthW2_Q3 = new TopoEdge(portW2, portQ3, 100L, Layer.ETHERNET);
        TopoEdge edgeEthY2_Q4 = new TopoEdge(portY2, portQ4, 100L, Layer.ETHERNET);
        TopoEdge edgeEthQ3_W2 = new TopoEdge(portQ3, portW2, 100L, Layer.ETHERNET);
        TopoEdge edgeEthQ4_Y2 = new TopoEdge(portQ4, portY2, 100L, Layer.ETHERNET);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();

        topoNodes.add(nodeK);
        topoNodes.add(nodeL);
        topoNodes.add(nodeM);
        topoNodes.add(nodeP);
        topoNodes.add(nodeQ);
        topoNodes.add(nodeR);
        topoNodes.add(nodeW);
        topoNodes.add(nodeX);
        topoNodes.add(nodeY);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portK1);
        topoNodes.add(portK2);
        topoNodes.add(portL1);
        topoNodes.add(portL2);
        topoNodes.add(portL3);
        topoNodes.add(portM1);
        topoNodes.add(portM2);
        topoNodes.add(portM3);
        topoNodes.add(portP1);
        topoNodes.add(portP2);
        topoNodes.add(portP3);
        topoNodes.add(portQ1);
        topoNodes.add(portQ2);
        topoNodes.add(portR1);
        topoNodes.add(portR2);
        topoNodes.add(portR3);
        topoNodes.add(portW1);
        topoNodes.add(portW2);
        topoNodes.add(portX1);
        topoNodes.add(portX2);
        topoNodes.add(portY1);
        topoNodes.add(portY2);

        topoLinks.add(edgeInt_A_K);
        topoLinks.add(edgeInt_Z_Q);
        topoLinks.add(edgeInt_K1_K);
        topoLinks.add(edgeInt_K2_K);
        topoLinks.add(edgeInt_L1_L);
        topoLinks.add(edgeInt_L2_L);
        topoLinks.add(edgeInt_L3_L);
        topoLinks.add(edgeInt_M1_M);
        topoLinks.add(edgeInt_M2_M);
        topoLinks.add(edgeInt_M3_M);
        topoLinks.add(edgeInt_P1_P);
        topoLinks.add(edgeInt_P2_P);
        topoLinks.add(edgeInt_P3_P);
        topoLinks.add(edgeInt_Q1_Q);
        topoLinks.add(edgeInt_Q2_Q);
        topoLinks.add(edgeInt_R1_R);
        topoLinks.add(edgeInt_R2_R);
        topoLinks.add(edgeInt_R3_R);

        topoLinks.add(edgeInt_K3_K);
        topoLinks.add(edgeInt_K4_K);
        topoLinks.add(edgeInt_Q3_Q);
        topoLinks.add(edgeInt_Q4_Q);
        topoLinks.add(edgeInt_W1_W);
        topoLinks.add(edgeInt_W2_W);
        topoLinks.add(edgeInt_X1_X);
        topoLinks.add(edgeInt_X2_X);
        topoLinks.add(edgeInt_Y1_Y);
        topoLinks.add(edgeInt_Y2_Y);

        topoLinks.add(edgeInt_K_A);
        topoLinks.add(edgeInt_Q_Z);
        topoLinks.add(edgeInt_K_K1);
        topoLinks.add(edgeInt_K_K2);
        topoLinks.add(edgeInt_L_L1);
        topoLinks.add(edgeInt_L_L2);
        topoLinks.add(edgeInt_L_L3);
        topoLinks.add(edgeInt_M_M1);
        topoLinks.add(edgeInt_M_M2);
        topoLinks.add(edgeInt_M_M3);
        topoLinks.add(edgeInt_P_P1);
        topoLinks.add(edgeInt_P_P2);
        topoLinks.add(edgeInt_P_P3);
        topoLinks.add(edgeInt_Q_Q1);
        topoLinks.add(edgeInt_Q_Q2);
        topoLinks.add(edgeInt_R_R1);
        topoLinks.add(edgeInt_R_R2);
        topoLinks.add(edgeInt_R_R3);

        topoLinks.add(edgeInt_K_K3);
        topoLinks.add(edgeInt_K_K4);
        topoLinks.add(edgeInt_Q_Q3);
        topoLinks.add(edgeInt_Q_Q4);
        topoLinks.add(edgeInt_W_W1);
        topoLinks.add(edgeInt_W_W2);
        topoLinks.add(edgeInt_X_X1);
        topoLinks.add(edgeInt_X_X2);
        topoLinks.add(edgeInt_Y_Y1);
        topoLinks.add(edgeInt_Y_Y2);

        topoLinks.add(edgeEth_K1_L1);
        topoLinks.add(edgeEth_K2_M1);
        topoLinks.add(edgeEth_L1_K1);
        topoLinks.add(edgeEth_L2_M2);
        topoLinks.add(edgeEth_L3_P1);
        topoLinks.add(edgeEth_M1_K2);
        topoLinks.add(edgeEth_M2_L2);
        topoLinks.add(edgeEth_M3_R1);
        topoLinks.add(edgeEth_P1_L3);
        topoLinks.add(edgeMpls_P2_Q1);
        topoLinks.add(edgeMpls_P3_R2);
        topoLinks.add(edgeMpls_Q1_P2);
        topoLinks.add(edgeMpls_Q2_R3);
        topoLinks.add(edgeEth_R1_M3);
        topoLinks.add(edgeMpls_R2_P3);
        topoLinks.add(edgeMpls_R3_Q2);

        topoLinks.add(edgeEth_K3_W1);
        topoLinks.add(edgeEth_K4_X1);
        topoLinks.add(edgeEthW1_K3);
        topoLinks.add(edgeEthX1_K4);
        topoLinks.add(edgeEthX2_Y1);
        topoLinks.add(edgeEthY1_X2);
        topoLinks.add(edgeEthW2_Q3);
        topoLinks.add(edgeEthY2_Q4);
        topoLinks.add(edgeEthQ3_W2);
        topoLinks.add(edgeEthQ4_Y2);

        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap);
    }

    public void buildTopoMultipleDisjointPaths() {
        log.info("Building Test Topology Multiple Disjoint Paths");
        Map<TopoVertex, TopoVertex> portToDeviceMap = new HashMap<>();

        TopoVertex nodeOne = new TopoVertex("1", VertexType.ROUTER);
        TopoVertex nodeTwo = new TopoVertex("2", VertexType.ROUTER);
        TopoVertex nodeThree = new TopoVertex("3", VertexType.ROUTER);
        TopoVertex nodeFour = new TopoVertex("4", VertexType.ROUTER);
        TopoVertex nodeFive = new TopoVertex("5", VertexType.ROUTER);
        TopoVertex nodeSix = new TopoVertex("6", VertexType.ROUTER);
        TopoVertex nodeSeven = new TopoVertex("7", VertexType.ROUTER);
        TopoVertex nodeEight = new TopoVertex("8", VertexType.ROUTER);
        TopoVertex nodeNine = new TopoVertex("9", VertexType.ROUTER);

        Set<TopoVertex> topoNodes = new HashSet<>(Arrays.asList(nodeOne, nodeTwo, nodeThree, nodeFour, nodeFive,
                nodeSix, nodeSeven, nodeEight, nodeNine));

        Set<TopoEdge> topoLinks = new HashSet<>();

        topoLinks.add(new TopoEdge(nodeOne, nodeTwo, 100L, Layer.MPLS));
        topoLinks.add(new TopoEdge(nodeOne, nodeFour, 100L, Layer.MPLS));

        topoLinks.add(new TopoEdge(nodeTwo, nodeOne, 100L, Layer.MPLS));
        topoLinks.add(new TopoEdge(nodeTwo, nodeThree, 100L, Layer.MPLS));
        topoLinks.add(new TopoEdge(nodeTwo, nodeFour, 100L, Layer.MPLS));
        topoLinks.add(new TopoEdge(nodeTwo, nodeFive, 100L, Layer.MPLS));

        topoLinks.add(new TopoEdge(nodeThree, nodeTwo, 100L, Layer.MPLS));
        topoLinks.add(new TopoEdge(nodeThree, nodeFive, 100L, Layer.MPLS));
        topoLinks.add(new TopoEdge(nodeThree, nodeSix, 100L, Layer.MPLS));

        topoLinks.add(new TopoEdge(nodeFour, nodeOne, 100L, Layer.MPLS));
        topoLinks.add(new TopoEdge(nodeFour, nodeTwo, 100L, Layer.MPLS));
        topoLinks.add(new TopoEdge(nodeFour, nodeFive, 100L, Layer.MPLS));
        topoLinks.add(new TopoEdge(nodeFour, nodeSeven, 100L, Layer.MPLS));

        topoLinks.add(new TopoEdge(nodeFive, nodeTwo, 100L, Layer.MPLS));
        topoLinks.add(new TopoEdge(nodeFive, nodeThree, 100L, Layer.MPLS));
        topoLinks.add(new TopoEdge(nodeFive, nodeFour, 100L, Layer.MPLS));
        topoLinks.add(new TopoEdge(nodeFive, nodeSix, 100L, Layer.MPLS));
        topoLinks.add(new TopoEdge(nodeFive, nodeSeven, 100L, Layer.MPLS));
        topoLinks.add(new TopoEdge(nodeFive, nodeEight, 100L, Layer.MPLS));

        topoLinks.add(new TopoEdge(nodeSix, nodeThree, 100L, Layer.MPLS));
        topoLinks.add(new TopoEdge(nodeSix, nodeFive, 100L, Layer.MPLS));
        topoLinks.add(new TopoEdge(nodeSix, nodeEight, 100L, Layer.MPLS));
        topoLinks.add(new TopoEdge(nodeSix, nodeNine, 100L, Layer.MPLS));

        topoLinks.add(new TopoEdge(nodeSeven, nodeFour, 100L, Layer.MPLS));
        topoLinks.add(new TopoEdge(nodeSeven, nodeFive, 100L, Layer.MPLS));
        topoLinks.add(new TopoEdge(nodeSeven, nodeEight, 100L, Layer.MPLS));

        topoLinks.add(new TopoEdge(nodeEight, nodeFive, 100L, Layer.MPLS));
        topoLinks.add(new TopoEdge(nodeEight, nodeSix, 100L, Layer.MPLS));
        topoLinks.add(new TopoEdge(nodeEight, nodeSeven, 100L, Layer.MPLS));
        topoLinks.add(new TopoEdge(nodeEight, nodeNine, 100L, Layer.MPLS));

        topoLinks.add(new TopoEdge(nodeNine, nodeSix, 100L, Layer.MPLS));
        topoLinks.add(new TopoEdge(nodeNine, nodeEight, 100L, Layer.MPLS));


        testBuilder.populateRepos(topoNodes, topoLinks, portToDeviceMap);
    }


    public void buildTopoWithNonUniformPorts()
    {
        log.info("Building Test Topology with Dual-Layer Devices");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();
        Map<TopoVertex, List<Integer>> floorMap = new HashMap<>();
        Map<TopoVertex, List<Integer>> ceilingMap = new HashMap<>();

        // Devices //
        TopoVertex nodeP = new TopoVertex("nodeP", VertexType.ROUTER);
        TopoVertex nodeQ = new TopoVertex("nodeQ", VertexType.ROUTER);

        // Ports //
        TopoVertex portP1 = new TopoVertex("portP:1", VertexType.PORT, PortLayer.ETHERNET);
        TopoVertex portP2 = new TopoVertex("portP:2", VertexType.PORT, PortLayer.MPLS);
        TopoVertex portP3 = new TopoVertex("portP:3", VertexType.PORT, PortLayer.ETHERNET);
        TopoVertex portP4 = new TopoVertex("portP:4", VertexType.PORT, PortLayer.MPLS);
        TopoVertex portP5 = new TopoVertex("portP:5", VertexType.PORT, PortLayer.ETHERNET);

        TopoVertex portQ1 = new TopoVertex("portQ:1", VertexType.PORT, PortLayer.ETHERNET);
        TopoVertex portQ2 = new TopoVertex("portQ:2", VertexType.PORT, PortLayer.MPLS);
        TopoVertex portQ3 = new TopoVertex("portQ:3", VertexType.PORT, PortLayer.MPLS);
        TopoVertex portQ4 = new TopoVertex("portQ:4", VertexType.PORT, PortLayer.ETHERNET);
        TopoVertex portQ5 = new TopoVertex("portQ:5", VertexType.PORT, PortLayer.ETHERNET);

        // Internal Links //
        TopoEdge edgeInt_P1_P = new TopoEdge(portP1, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P2_P = new TopoEdge(portP2, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P3_P = new TopoEdge(portP3, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P4_P = new TopoEdge(portP4, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P5_P = new TopoEdge(portP5, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q1_Q = new TopoEdge(portQ1, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q2_Q = new TopoEdge(portQ2, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q3_Q = new TopoEdge(portQ3, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q4_Q = new TopoEdge(portQ4, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q5_Q = new TopoEdge(portQ5, nodeQ, 0L, Layer.INTERNAL);

        TopoEdge edgeInt_P_P1 = new TopoEdge(nodeP, portP1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P2 = new TopoEdge(nodeP, portP2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P3 = new TopoEdge(nodeP, portP3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P4 = new TopoEdge(nodeP, portP4, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P5 = new TopoEdge(nodeP, portP5, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q1 = new TopoEdge(nodeQ, portQ1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q2 = new TopoEdge(nodeQ, portQ2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q3 = new TopoEdge(nodeQ, portQ3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q4 = new TopoEdge(nodeQ, portQ4, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q5 = new TopoEdge(nodeQ, portQ5, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeMpls_P2_Q2 = new TopoEdge(portP2, portQ2, 100L, Layer.MPLS);
        TopoEdge edgeEth_P3_Q3 = new TopoEdge(portP3, portQ3, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_P4_Q4 = new TopoEdge(portP4, portQ4, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_P5_Q5 = new TopoEdge(portP5, portQ5, 100L, Layer.ETHERNET);

        TopoEdge edgeMpls_Q2_P2 = new TopoEdge(portQ2, portP2, 100L, Layer.MPLS);
        TopoEdge edgeEth_Q3_P3 = new TopoEdge(portQ3, portP3, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_Q4_P4 = new TopoEdge(portQ4, portP4, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_Q5_P5 = new TopoEdge(portQ5, portP5, 100L, Layer.ETHERNET);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();
        List<Integer> floors = Arrays.asList(1);
        List<Integer> ceilings = Arrays.asList(5);

        topoNodes.add(nodeP);
        topoNodes.add(nodeQ);

        topoNodes.add(portP1);
        topoNodes.add(portP2);
        topoNodes.add(portP3);
        topoNodes.add(portP4);
        topoNodes.add(portP5);
        topoNodes.add(portQ1);
        topoNodes.add(portQ2);
        topoNodes.add(portQ3);
        topoNodes.add(portQ4);
        topoNodes.add(portQ5);

        topoLinks.add(edgeInt_P1_P);
        topoLinks.add(edgeInt_P2_P);
        topoLinks.add(edgeInt_P3_P);
        topoLinks.add(edgeInt_P4_P);
        topoLinks.add(edgeInt_P5_P);
        topoLinks.add(edgeInt_Q1_Q);
        topoLinks.add(edgeInt_Q2_Q);
        topoLinks.add(edgeInt_Q3_Q);
        topoLinks.add(edgeInt_Q4_Q);
        topoLinks.add(edgeInt_Q5_Q);

        topoLinks.add(edgeInt_P_P1);
        topoLinks.add(edgeInt_P_P2);
        topoLinks.add(edgeInt_P_P3);
        topoLinks.add(edgeInt_P_P4);
        topoLinks.add(edgeInt_P_P5);
        topoLinks.add(edgeInt_Q_Q1);
        topoLinks.add(edgeInt_Q_Q2);
        topoLinks.add(edgeInt_Q_Q3);
        topoLinks.add(edgeInt_Q_Q4);
        topoLinks.add(edgeInt_Q_Q5);

        topoLinks.add(edgeMpls_P2_Q2);
        topoLinks.add(edgeEth_P3_Q3);
        topoLinks.add(edgeEth_P4_Q4);
        topoLinks.add(edgeEth_P5_Q5);

        topoLinks.add(edgeMpls_Q2_P2);
        topoLinks.add(edgeEth_Q3_P3);
        topoLinks.add(edgeEth_Q4_P4);
        topoLinks.add(edgeEth_Q5_P5);

        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        for(TopoVertex oneVert : topoNodes)
        {
            floorMap.put(oneVert, floors);
            ceilingMap.put(oneVert, ceilings);
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap, floorMap, ceilingMap);
    }

    public void buildTopoEsnet() {
        testBuilder.importEsnet();
    }
}

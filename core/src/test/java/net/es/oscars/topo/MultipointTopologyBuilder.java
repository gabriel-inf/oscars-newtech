package net.es.oscars.topo;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pce.RepoEntityBuilder;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.enums.Layer;
import net.es.oscars.dto.topo.enums.VertexType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by Jeremy on 7/18/2016.
 */
@Slf4j
@Component
public class MultipointTopologyBuilder
{
    @Autowired
    private RepoEntityBuilder testBuilder;

    public void buildMultipointTopo1()
    {
        log.info("Building Multipoint Test Topology 1");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();

        // Devices //
        TopoVertex nodeL = new TopoVertex("nodeL", VertexType.SWITCH);
        TopoVertex nodeM = new TopoVertex("nodeM", VertexType.SWITCH);
        TopoVertex nodeN = new TopoVertex("nodeN", VertexType.SWITCH);
        TopoVertex nodeP = new TopoVertex("nodeP", VertexType.ROUTER);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portB = new TopoVertex("portB", VertexType.PORT);
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
        TopoEdge edgeInt_B_N = new TopoEdge(portB, nodeN, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_M = new TopoEdge(portZ, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_A = new TopoEdge(nodeP, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N_B = new TopoEdge(nodeN, portB, 0L, Layer.INTERNAL);
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
        topoNodes.add(portB);
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
        topoLinks.add(edgeInt_B_N);
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
        topoLinks.add(edgeInt_N_B);
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

    public void buildMultipointTopo2()
    {
        log.info("Building Multipoint Test Topology 2");

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
        TopoVertex portB = new TopoVertex("portB", VertexType.PORT);
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
        TopoEdge edgeInt_B_M = new TopoEdge(portB, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_Q = new TopoEdge(portZ, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_A = new TopoEdge(nodeK, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_B = new TopoEdge(nodeM, portB, 0L, Layer.INTERNAL);
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
        topoNodes.add(portB);
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
        topoLinks.add(edgeInt_B_M);
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
        topoLinks.add(edgeInt_M_B);
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

    public void buildMultipointTopo3()
    {
        log.info("Building Multipoint Test Topology 3");

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
        TopoVertex portB = new TopoVertex("portB", VertexType.PORT);
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
        TopoEdge edgeInt_B_M = new TopoEdge(portB, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_Q = new TopoEdge(portZ, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_A = new TopoEdge(nodeK, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_B = new TopoEdge(nodeM, portB, 0L, Layer.INTERNAL);
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
        topoNodes.add(portB);
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
        topoLinks.add(edgeInt_B_M);
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
        topoLinks.add(edgeInt_M_B);
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

    public void buildMultipointTopo4()
    {
        log.info("Building Multipoint Test Topology 4");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();

        // Devices //
        TopoVertex nodeL = new TopoVertex("nodeL", VertexType.ROUTER);
        TopoVertex nodeM = new TopoVertex("nodeM", VertexType.SWITCH);
        TopoVertex nodeN = new TopoVertex("nodeN", VertexType.SWITCH);
        TopoVertex nodeP = new TopoVertex("nodeP", VertexType.ROUTER);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portB = new TopoVertex("portB", VertexType.PORT);
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
        TopoEdge edgeInt_B_N = new TopoEdge(portB, nodeN, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_M = new TopoEdge(portZ, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_A = new TopoEdge(nodeP, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N_B = new TopoEdge(nodeN, portB, 0L, Layer.INTERNAL);
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
        TopoEdge edgeMpls_L1_P1 = new TopoEdge(portL1, portP1, 100L, Layer.MPLS);
        TopoEdge edgeEth_L2_M1 = new TopoEdge(portL2, portM1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L3_N1 = new TopoEdge(portL3, portN1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M1_L2 = new TopoEdge(portM1, portL2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M2_N2 = new TopoEdge(portM2, portN2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_N1_L3 = new TopoEdge(portN1, portL3, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_N2_M2 = new TopoEdge(portN2, portM2, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_P1_L1 = new TopoEdge(portP1, portL1, 100L, Layer.MPLS);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();

        topoNodes.add(nodeL);
        topoNodes.add(nodeM);
        topoNodes.add(nodeN);
        topoNodes.add(nodeP);

        topoNodes.add(portA);
        topoNodes.add(portB);
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
        topoLinks.add(edgeInt_B_N);
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
        topoLinks.add(edgeInt_N_B);
        topoLinks.add(edgeInt_M_Z);
        topoLinks.add(edgeInt_L_L1);
        topoLinks.add(edgeInt_L_L2);
        topoLinks.add(edgeInt_L_L3);
        topoLinks.add(edgeInt_M_M1);
        topoLinks.add(edgeInt_M_M2);
        topoLinks.add(edgeInt_N_N1);
        topoLinks.add(edgeInt_N_N2);
        topoLinks.add(edgeInt_P_P1);

        topoLinks.add(edgeMpls_L1_P1);
        topoLinks.add(edgeEth_L2_M1);
        topoLinks.add(edgeEth_L3_N1);
        topoLinks.add(edgeEth_M1_L2);
        topoLinks.add(edgeEth_M2_N2);
        topoLinks.add(edgeEth_N1_L3);
        topoLinks.add(edgeEth_N2_M2);
        topoLinks.add(edgeMpls_P1_L1);

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

    public void buildComplexMultipointTopo()
    {
        log.info("Building Complex Multipoint Test Topology");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();
        Map<TopoVertex, List<Integer>> portBWs = new HashMap<>();

        // Devices //
        TopoVertex nodeL = new TopoVertex("nodeL", VertexType.ROUTER);
        TopoVertex nodeM = new TopoVertex("nodeM", VertexType.ROUTER);
        TopoVertex nodeN = new TopoVertex("nodeN", VertexType.ROUTER);
        TopoVertex nodeP = new TopoVertex("nodeP", VertexType.ROUTER);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portB = new TopoVertex("portB", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portL1 = new TopoVertex("nodeL:1", VertexType.PORT);
        TopoVertex portL2 = new TopoVertex("nodeL:2", VertexType.PORT);
        TopoVertex portL3 = new TopoVertex("nodeL:3", VertexType.PORT);
        TopoVertex portM1 = new TopoVertex("nodeM:1", VertexType.PORT);
        TopoVertex portM2 = new TopoVertex("nodeM:2", VertexType.PORT);
        TopoVertex portN1 = new TopoVertex("nodeN:1", VertexType.PORT);
        TopoVertex portN2 = new TopoVertex("nodeN:2", VertexType.PORT);
        TopoVertex portN3 = new TopoVertex("nodeN:3", VertexType.PORT);
        TopoVertex portP1 = new TopoVertex("nodeP:1", VertexType.PORT);
        TopoVertex portP2 = new TopoVertex("nodeP:2", VertexType.PORT);

        // Asymmetric Bandwidth Capacity  (Ingress - Egress) //
        List<Integer> bwPortA = Arrays.asList(500, 500);
        List<Integer> bwPortB = Arrays.asList(500, 500);
        List<Integer> bwPortZ = Arrays.asList(500, 500);
        List<Integer> bwPortL1 = Arrays.asList(100, 200);
        List<Integer> bwPortL2 = Arrays.asList(100, 100);
        List<Integer> bwPortL3 = Arrays.asList(200, 100);
        List<Integer> bwPortM1 = Arrays.asList(100, 100);
        List<Integer> bwPortM2 = Arrays.asList(500, 500);
        List<Integer> bwPortN1 = Arrays.asList(100, 200);
        List<Integer> bwPortN2 = Arrays.asList(500, 500);
        List<Integer> bwPortN3 = Arrays.asList(100, 0);
        List<Integer> bwPortP1 = Arrays.asList(200, 100);
        List<Integer> bwPortP2 = Arrays.asList(0, 100);
        portBWs.put(portA, bwPortA);
        portBWs.put(portB, bwPortB);
        portBWs.put(portZ, bwPortZ);
        portBWs.put(portL1, bwPortL1);
        portBWs.put(portL2, bwPortL2);
        portBWs.put(portL3, bwPortL3);
        portBWs.put(portM1, bwPortM1);
        portBWs.put(portM2, bwPortM2);
        portBWs.put(portN1, bwPortN1);
        portBWs.put(portN2, bwPortN2);
        portBWs.put(portN3, bwPortN3);
        portBWs.put(portP1, bwPortP1);
        portBWs.put(portP2, bwPortP2);

        // End-Port Links //
        TopoEdge edgeInt_A_P = new TopoEdge(portA, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B_N = new TopoEdge(portB, nodeN, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_M = new TopoEdge(portZ, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_A = new TopoEdge(nodeP, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N_B = new TopoEdge(nodeN, portB, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_Z = new TopoEdge(nodeM, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_L1_L = new TopoEdge(portL1, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L2_L = new TopoEdge(portL2, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L3_L = new TopoEdge(portL3, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M1_M = new TopoEdge(portM1, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M2_M = new TopoEdge(portM2, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N1_N = new TopoEdge(portN1, nodeN, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N2_N = new TopoEdge(portN2, nodeN, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N3_N = new TopoEdge(portN3, nodeN, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P1_P = new TopoEdge(portP1, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P2_P = new TopoEdge(portP2, nodeP, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_L_L1 = new TopoEdge(nodeL, portL1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L2 = new TopoEdge(nodeL, portL2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L3 = new TopoEdge(nodeL, portL3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M1 = new TopoEdge(nodeM, portM1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M2 = new TopoEdge(nodeM, portM2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N_N1 = new TopoEdge(nodeN, portN1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N_N2 = new TopoEdge(nodeN, portN2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N_N3 = new TopoEdge(nodeN, portN3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P1 = new TopoEdge(nodeP, portP1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P2 = new TopoEdge(nodeP, portP2, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeMpls_L1_P1 = new TopoEdge(portL1, portP1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_L2_M1 = new TopoEdge(portL2, portM1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_L3_N1 = new TopoEdge(portL3, portN1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_M1_L2 = new TopoEdge(portM1, portL2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_M2_N2 = new TopoEdge(portM2, portN2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_N1_L3 = new TopoEdge(portN1, portL3, 100L, Layer.MPLS);
        TopoEdge edgeMpls_N2_M2 = new TopoEdge(portN2, portM2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_N3_P2 = new TopoEdge(portN3, portP2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_P1_L1 = new TopoEdge(portP1, portL1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_P2_N3 = new TopoEdge(portP2, portN3, 100L, Layer.MPLS);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();

        topoNodes.add(nodeL);
        topoNodes.add(nodeM);
        topoNodes.add(nodeN);
        topoNodes.add(nodeP);

        topoNodes.add(portA);
        topoNodes.add(portB);
        topoNodes.add(portZ);
        topoNodes.add(portL1);
        topoNodes.add(portL2);
        topoNodes.add(portL3);
        topoNodes.add(portM1);
        topoNodes.add(portM2);
        topoNodes.add(portN1);
        topoNodes.add(portN2);
        topoNodes.add(portN3);
        topoNodes.add(portP1);
        topoNodes.add(portP2);

        topoLinks.add(edgeInt_A_P);
        topoLinks.add(edgeInt_B_N);
        topoLinks.add(edgeInt_Z_M);
        topoLinks.add(edgeInt_L1_L);
        topoLinks.add(edgeInt_L2_L);
        topoLinks.add(edgeInt_L3_L);
        topoLinks.add(edgeInt_M1_M);
        topoLinks.add(edgeInt_M2_M);
        topoLinks.add(edgeInt_N1_N);
        topoLinks.add(edgeInt_N2_N);
        topoLinks.add(edgeInt_N3_N);
        topoLinks.add(edgeInt_P1_P);
        topoLinks.add(edgeInt_P2_P);

        topoLinks.add(edgeInt_P_A);
        topoLinks.add(edgeInt_N_B);
        topoLinks.add(edgeInt_M_Z);
        topoLinks.add(edgeInt_L_L1);
        topoLinks.add(edgeInt_L_L2);
        topoLinks.add(edgeInt_L_L3);
        topoLinks.add(edgeInt_M_M1);
        topoLinks.add(edgeInt_M_M2);
        topoLinks.add(edgeInt_N_N1);
        topoLinks.add(edgeInt_N_N2);
        topoLinks.add(edgeInt_N_N3);
        topoLinks.add(edgeInt_P_P1);
        topoLinks.add(edgeInt_P_P2);

        topoLinks.add(edgeMpls_L1_P1);
        topoLinks.add(edgeMpls_L2_M1);
        topoLinks.add(edgeMpls_L3_N1);
        topoLinks.add(edgeMpls_M1_L2);
        topoLinks.add(edgeMpls_M2_N2);
        topoLinks.add(edgeMpls_N1_L3);
        topoLinks.add(edgeMpls_N2_M2);
        topoLinks.add(edgeMpls_N3_P2);
        topoLinks.add(edgeMpls_P1_L1);
        topoLinks.add(edgeMpls_P2_N3);

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


        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap, portBWs);
    }

}

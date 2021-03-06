<#-- @ftlvariable name="vpls" type="net.es.oscars.dto.pss.params.alu.AluVpls" -->
<#-- @ftlvariable name="sap" type="net.es.oscars.dto.pss.params.alu.AluSap" -->
<#-- @ftlvariable name="sdpToVcId" type="net.es.oscars.dto.pss.params.alu.AluSdpToVcId" -->

<#assign endpointSnippet = "">
<#if vpls.endpointName??>
    <#assign endpointName = vpls.endpointName>
    <#assign endpointSnippet = "endpoint "+endpointName>
</#if>

<#assign svcId = vpls.svcId>

/configure service vpls ${svcId} customer 1 create
exit
/configure service vpls ${svcId} shutdown
/configure service vpls ${svcId} description "${vpls.description}"
/configure service vpls ${svcId} service-name "${vpls.serviceName}"
/configure service vpls ${svcId} service-mtu 9114
/configure service vpls ${svcId} fdb-table-size 4096
/configure service vpls ${svcId} stp shutdown


<#if vpls.endpointName??>
<#assign endpointName = vpls.endpointName>
/configure service vpls ${svcId} endpoint "${endpointName}" create
exit
/configure service vpls ${svcId} endpoint "${endpointName}" revert-time 1
/configure service vpls ${svcId} endpoint "${endpointName}" restrict-protected-src discard-frame
/configure service vpls ${svcId} endpoint "${endpointName}" no suppress-standby-signaling
</#if>


<#list vpls.saps as sap>
<#assign sapId = sap.port+":"+sap.vlan>
/configure service vpls ${svcId} sap ${sapId} create
exit
/configure service vpls ${svcId} sap ${sapId} auto-learn-mac-protect
/configure service vpls ${svcId} sap ${sapId} restrict-protected-src discard-frame
/configure service vpls ${svcId} sap ${sapId} description "${sap.description}"
/configure service vpls ${svcId} sap ${sapId} ingress qos ${sap.ingressQosId}
/configure service vpls ${svcId} sap ${sapId} egress qos ${sap.egressQosId}
/configure service vpls ${svcId} sap ${sapId} no shutdown
</#list>

<#if vpls.sdpToVcIds??>
<#list vpls.sdpToVcIds as sdpToVcId>

<#assign sdpId = sdpToVcId.sdpId>
<#assign vcId = sdpToVcId.vcId>

/configure service vpls ${svcId} spoke-sdp ${sdpId}:${vcId} vc-type vlan ${endpointSnippet} create
exit
/configure service vpls ${svcId} spoke-sdp ${sdpId}:${vcId} restrict-protected-src discard-frame
/configure service vpls ${svcId} spoke-sdp ${sdpId}:${vcId} precedence primary
/configure service vpls ${svcId} spoke-sdp ${sdpId}:${vcId} no shutdown

</#list>
</#if>

/configure service vpls ${svcId} no shutdown

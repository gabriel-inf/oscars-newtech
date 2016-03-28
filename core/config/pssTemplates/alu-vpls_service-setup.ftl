<#-- @ftlvariable name="vpls" type="net.es.oscars.common.pss.AluVpls" -->
<#-- @ftlvariable name="sap" type="net.es.oscars.common.pss.AluSap" -->
<#-- @ftlvariable name="sdp" type="java.util.Optional" -->

<#assign endpointSnippet = "">
<#if vpls.endpointName.isPresent()>
    <#assign endpointName = vpls.endpointName.get()>
    <#assign endpointSnippet = "endpoint "+endpointName>
</#if>

<#assign vcId = vpls.vcId >
/configure service vpls ${vcId} customer 1 create
/configure service vpls ${vcId} shutdown
/configure service vpls ${vcId} description "${vpls.description}"
/configure service vpls ${vcId} service-name "${vpls.serviceName}"
/configure service vpls ${vcId} service-mtu 9114
/configure service vpls ${vcId} fdb-table-size 4096
/configure service vpls ${vcId} stp shutdown


<#if vpls.endpointName.isPresent()>
<#assign endpointName = vpls.endpointName.get()>
/configure service vpls ${vcId} endpoint "${endpointName}" create
/configure service vpls ${vcId} endpoint "${endpointName}" revert-time 1
/configure service vpls ${vcId} endpoint "${endpointName}" restrict-protected-src discard-frame
/configure service vpls ${vcId} endpoint "${endpointName}" no suppress-standby-signaling
</#if>


<#list vpls.saps as sap>
<#assign sapId = sap.port+":"+sap.vlan>
/configure service vpls ${vcId} sap ${sapId} create
/configure service vpls ${vcId} sap ${sapId} auto-learn-mac-protect
/configure service vpls ${vcId} sap ${sapId} restrict-protected-src discard-frame
/configure service vpls ${vcId} sap ${sapId} description "${sap.description}"
/configure service vpls ${vcId} sap ${sapId} ingress qos ${sap.ingressQosId}
/configure service vpls ${vcId} sap ${sapId} egress qos ${sap.egressQosId}
/configure service vpls ${vcId} sap ${sapId} no shutdown
</#list>

<#if vpls.sdp.isPresent()>
<#assign sdp = vpls.sdp.get()>
/configure service vpls ${vcId} spoke-sdp ${sdp.sdpId}:${vcId} vc-type vlan ${endpointSnippet} create
/configure service vpls ${vcId} spoke-sdp ${sdp.sdpId}:${vcId} restrict-protected-src discard-frame
/configure service vpls ${vcId} spoke-sdp ${sdp.sdpId}:${vcId} precedence primary
/configure service vpls ${vcId} spoke-sdp ${sdp.sdpId}:${vcId} no shutdown
</#if>


<#if vpls.protectSdp.isPresent()>
<#assign sdp = vpls.protectSdp.get()>
<#assign protectVcId = vpls.protectVcId.get()>
/configure service vpls ${vcId} spoke-sdp ${sdp.sdpId}:${protectVcId} vc-type vlan ${endpointSnippet} create
/configure service vpls ${vcId} spoke-sdp ${sdp.sdpId}:${protectVcId} egress qos 3 port-redirect-group "best-effort-vc" instance 1
/configure service vpls ${vcId} spoke-sdp ${sdp.sdpId}:${protectVcId} restrict-protected-src discard-frame
/configure service vpls ${vcId} spoke-sdp ${sdp.sdpId}:${protectVcId} no shutdown
</#if>

/configure service vpls ${vcId} no shutdown

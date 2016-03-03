<#-- @ftlvariable name="vpls" type="net.es.oscars.core.pss.ftl.AluVpls" -->
<#-- @ftlvariable name="sap" type="net.es.oscars.core.pss.ftl.AluSap" -->
<#-- @ftlvariable name="sdp" type="net.es.oscars.core.pss.ftl.AluSdp" -->

<#assign vcId = vpls.vcId >
/configure service vpls ${vcId} customer 1 create
/configure service vpls ${vcId} shutdown
/configure service vpls ${vcId} description "${vpls.description}"
/configure service vpls ${vcId} service-name "${vpls.serviceName}"
/configure service vpls ${vcId} service-mtu 9114
/configure service vpls ${vcId} fdb-table-size 4096
/configure service vpls ${vcId} stp shutdown


<#if vpls.endpoint>
/configure service vpls ${vcId} endpoint "${vpls.endpointName}" create
/configure service vpls ${vcId} endpoint "${vpls.endpointName}" revert-time 1
/configure service vpls ${vcId} endpoint "${vpls.endpointName}" restrict-protected-src discard-frame
/configure service vpls ${vcId} endpoint "${vpls.endpointName}" no suppress-standby-signaling
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

<#if vpls.sdp??>
<#assign sdp = vpls.sdp>
<#if vpls.endpoint>
/configure service vpls ${vcId} spoke-sdp ${sdp.sdpId}:${vcId} vc-type vlan endpoint "${vpls.endpointName}" create
<#else>
/configure service vpls ${vcId} spoke-sdp ${sdp.sdpId}:${vcId} vc-type vlan create
</#if>
/configure service vpls ${vcId} spoke-sdp ${sdp.sdpId}:${vcId} restrict-protected-src discard-frame
/configure service vpls ${vcId} spoke-sdp ${sdp.sdpId}:${vcId} precedence primary
/configure service vpls ${vcId} spoke-sdp ${sdp.sdpId}:${vcId} no shutdown
</#if>


<#if vpls.hasProtect>
<#assign sdp = vpls.protectSdp>
<#if vpls.endpoint>
/configure service vpls ${vcId} spoke-sdp ${sdp.sdpId}:${vpls.protectVcId} vc-type vlan endpoint "${vpls.endpointName}" create
<#else>
/configure service vpls ${vcId} spoke-sdp ${sdp.sdpId}:${vpls.protectVcId} vc-type vlan create
</#if>
/configure service vpls ${vcId} spoke-sdp ${sdp.sdpId}:${vpls.protectVcId} egress qos 3 port-redirect-group "best-effort-vc" instance 1
/configure service vpls ${vcId} spoke-sdp ${sdp.sdpId}:${vpls.protectVcId} restrict-protected-src discard-frame
/configure service vpls ${vcId} spoke-sdp ${sdp.sdpId}:${vpls.protectVcId} no shutdown
</#if>

/configure service vpls ${vcId} no shutdown

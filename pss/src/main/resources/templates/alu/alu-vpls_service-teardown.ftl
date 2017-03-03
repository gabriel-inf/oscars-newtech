<#-- @ftlvariable name="vpls" type="net.es.oscars.pss.cmd.AluVpls" -->
<#-- @ftlvariable name="sap" type="net.es.oscars.pss.cmd.AluSap" -->
<#-- @ftlvariable name="sdp" type="net.es.oscars.pss.cmd.AluSdp" -->

<#assign vcId = vpls.vcId >
/configure service vpls ${vcId} shutdown

<#if vpls.sdp??>
<#assign sdp = vpls.sdp>
/configure service vpls ${vcId} spoke-sdp ${sdp.sdpId}:${vcId} shutdown
/configure service vpls ${vcId} no spoke-sdp ${sdp.sdpId}:${vcId}
</#if>

<#if vpls.hasProtect>
<#assign sdp = vpls.protectSdp>
/configure service vpls ${vcId} spoke-sdp ${sdp.sdpId}:${vpls.protectVcId} shutdown
/configure service vpls ${vcId} no spoke-sdp ${sdp.sdpId}:${vpls.protectVcId}
</#if>

<#list vpls.saps as sap>
<#assign sapId = sap.port+":"+sap.vlan>
configure service vpls ${vcId} sap ${sapId} shutdown
configure service vpls ${vcId} no sap ${sapId}
</#list>

configure service no vpls ${vcId}




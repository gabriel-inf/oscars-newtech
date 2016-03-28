<#-- @ftlvariable name="sdps" type="java.util.List" -->
<#-- @ftlvariable name="sdp" type="net.es.oscars.common.pss.AluSdp" -->
<#-- @ftlvariable name="protect" type="java.lang.Boolean" -->

<#list sdps as sdp>
<#assign sdpId = sdp.sdpId>

# service distribution point - forwards packets to the MPLS tunnel
/configure service sdp ${sdpId} shutdown
/configure service no sdp ${sdpId}
</#list>


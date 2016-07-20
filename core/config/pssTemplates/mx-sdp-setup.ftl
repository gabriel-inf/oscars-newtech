<#-- @ftlvariable name="lsps" type="java.util.List<net.es.oscars.pss.cmd.Lsp>" -->
<#-- @ftlvariable name="lsp" type="net.es.oscars.pss.cmd.Lsp" -->
<#-- @ftlvariable name="vpls" type="net.es.oscars.pss.cmd.MxVpls" -->

<#assign communityMembers = "65000:672277:"+vpls.vcId>

set policy-options community ${vpls.communityName} members ${communityMembers}

top
edit policy-options policy-statement ${vpls.policyName} term oscars
set from community ${vpls.communityName}
<#list lsps as lsp>
set then install-nexthop lsp ${lsp.name}
</#list>
set then accept
top


edit routing-options
set forwarding-table export [ ${vpls.policyName} ]
top


<#assign mesh_group = "sdp-"+vpls.vcId >
<#list lsps as lsp>
<#assign lsp_neighbor = vpls.getLspNeighbors().get(lsp.to)>
edit routing-instances ${vpls.serviceName} protocols vpls mesh-group ${mesh_group}
set vpls-id ${vpls.vcId}
edit neighbor ${lsp_neighbor}
set psn-tunnel-endpoint ${lsp.to}
set community ${vpls.communityName}
set encapsulation-type ethernet-vlan
</#list>



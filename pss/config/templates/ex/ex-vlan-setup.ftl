<#-- @ftlvariable name="ifces" type="java.util.List<net.es.oscars.pss.cmd.ExIfce>" -->
<#-- @ftlvariable name="vlan" type="net.es.oscars.pss.cmd.ExVlan" -->


edit vlans ${vlan.name}
<#if vlan.description??>
set description "${vlan.description}"
</#if>
set vlan-id ${vlan.vlanId}
top

<#list ifces as ifce>
edit interfaces ${ifce.port} unit 0 family ethernet-switching vlan
set members vlan_name
</#list>

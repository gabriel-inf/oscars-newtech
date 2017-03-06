<#-- @ftlvariable name="ifces" type="java.util.List<net.es.oscars.pss.cmd.MxIfce>" -->
<#-- @ftlvariable name="filter" type="net.es.oscars.pss.cmd.MxFilter" -->

<#list ifces as ifce>
delete interfaces ${ifce.port} unit ${ifce.vlan}
</#list>
delete firewall family vpls filter ${filter.name}


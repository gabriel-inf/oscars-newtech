<#-- @ftlvariable name="ifces" type="java.util.List<net.es.oscars.pss.cmd.MxIfce>" -->
<#-- @ftlvariable name="filter" type="net.es.oscars.pss.cmd.MxFilter" -->

edit firewall family vpls filter ${filter.name}
set interface-specific
set term oscars then count oscars_counter
set term oscars then accept
top

<#list ifces as ifce>
edit interfaces ${ifce.port}
edit unit ${ifce.vlan}
set description ${ifce.description}
set encapsulation vlan-vpls
set vlan-id ${ifce.vlan}
set output-vlan-map swap
set family vpls filter input ${filter.name}
set family vpls filter output ${filter.name}
top
</#list>

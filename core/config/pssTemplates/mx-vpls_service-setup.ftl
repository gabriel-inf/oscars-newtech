<#-- @ftlvariable name="vpls" type="net.es.oscars.pss.cmd.MxVpls" -->
<#-- @ftlvariable name="ifce" type="net.es.oscars.pss.cmd.MxIfce" -->


edit routing-instances ${vpls.serviceName}
set instance-type vpls
<#list vpls.ifces as ifce>
set interface ${ifce.port}.${ifce.vlan}
</#list>
edit protocols vpls
set no-tunnel-services
set mtu 9100
edit site CE
<#list vpls.ifces as ifce>
set interface ${ifce.port}.${ifce.vlan}
</#list>


<#if vpls.loopback??>
top
edit interfaces lo0 unit 0 family inet
set address ${vpls.loopback}
top
</#if>
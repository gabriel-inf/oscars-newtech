<#-- @ftlvariable name="vpls" type="net.es.oscars.pss.cmd.MxVpls" -->
<#-- @ftlvariable name="ifce" type="net.es.oscars.pss.cmd.MxIfce" -->

delete routing-instances ${vpls.serviceName}


<#if vpls.loopback??>
top
edit interfaces lo0 unit 0 family inet
delete address ${vpls.loopback}
top
</#if>
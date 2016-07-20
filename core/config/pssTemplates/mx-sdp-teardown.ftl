<#-- @ftlvariable name="lsps" type="java.util.List<net.es.oscars.pss.cmd.Lsp>" -->
<#-- @ftlvariable name="lsp" type="net.es.oscars.pss.cmd.Lsp" -->
<#-- @ftlvariable name="vpls" type="net.es.oscars.pss.cmd.MxVpls" -->


delete policy-options community ${vpls.communityName}
delete policy-options policy-statement  ${vpls.policyName}
delete routing-options forwarding-table export  ${vpls.policyName}


delete routing-instances ${vpls.serviceName}

<#-- @ftlvariable name="filter" type="net.es.oscars.pss.cmd.MxFilter" -->
<#-- @ftlvariable name="policer" type="net.es.oscars.pss.cmd.MxPolicer" -->
<#-- @ftlvariable name="applyqos" type="boolean" -->


delete firewall family any filter ${filter.name}

<#if applyqos>
delete firewall policer ${policer.name}
</#if>

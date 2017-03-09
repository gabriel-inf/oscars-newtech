<#-- @ftlvariable name="filter" type="net.es.oscars.pss.cmd.MxFilter" -->
<#-- @ftlvariable name="policer" type="net.es.oscars.pss.cmd.MxPolicer" -->
<#-- @ftlvariable name="applyqos" type="boolean" -->


edit firewall family any filter ${filter.name} term oscars then

<#if applyqos>
set forwarding-class expedited-forwarding-vc
set loss-priority low
<#else>
set forwarding-class best-effort-vc
set loss-priority high
</#if>
set count oscars
set accept
top



<#if applyqos>


edit firewall policer ${policer.name}

<#-- may need some massaging of values? -->
<#if policer.mbps gt 0>
<#assign bw_limit = qos.mbps+"000000" >
<#assign burst_limit = qos.mbps+"00000" >
set if-exceeding bandwidth-limit ${bw_limit}
set if-exceeding burst-size-limit ${burst_limit}
</#if>

<#if policer.policing == "SOFT">
set then loss-priority high
<#else>
set then discard
</#if>
top

edit firewall family any filter ${filter.name} term oscars then
set policer ${policer.name}
top

</#if>

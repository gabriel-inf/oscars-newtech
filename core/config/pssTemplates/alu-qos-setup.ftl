<#-- @ftlvariable name="qosList" type="java.util.List" -->
<#-- @ftlvariable name="qos" type="net.es.oscars.pss.cmd.AluQos" -->
<#-- @ftlvariable name="protect" type="java.lang.Boolean" -->
<#-- @ftlvariable name="apply" type="java.lang.Boolean" -->

<#list qosList as qos>

<#assign qosId = qos.policyId >
<#assign sapType = "sap-egress" >
<#if qos.type == "SAP_INGRESS">
    <#assign sapType = "sap-ingress" >
</#if>

<#if qos.mbps gt 0>
    <#assign bps = qos.mbps+"000" >
<#else>
    <#assign bps = "0" >
</#if>

<#assign max = bps >
<#if qos.policing == "SOFT" >
    <#assign max = "max" >
</#if>

<#-- shared for ingress and egress -->
/configure qos ${sapType} ${qosId} create
/configure qos ${sapType} ${qosId} description "${qos.description}"
/configure qos ${sapType} ${qosId} policy-name "${qos.policyName}"
/configure qos ${sapType} ${qosId} queue 1 create
/configure qos ${sapType} ${qosId} fc "ef" create
/configure qos ${sapType} ${qosId} fc "ef" queue 2
/configure qos ${sapType} ${qosId} fc "l1" create
/configure qos ${sapType} ${qosId} fc "l1" queue 3


<#-- ingress only -->
<#if qos.type == "SAP_INGRESS">
/configure qos ${sapType} ${qosId} queue 2 create
/configure qos ${sapType} ${qosId} queue 3 create
/configure qos ${sapType} ${qosId} queue 11 multipoint create

<#-- ingress, only when we apply QoS-->
<#if apply>
/configure qos ${sapType} ${qosId} default-fc "ef"
/configure qos ${sapType} ${qosId} queue 2 ${max} max cir ${bps}
<#else>
/configure qos ${sapType} ${qosId} default-fc "l1"
/configure qos ${sapType} ${qosId} queue 3 rate max cir 0
</#if>


<#-- egress only -->
<#else>
/configure qos ${sapType} ${qosId} queue 2 expedite create
/configure qos ${sapType} ${qosId} queue 3 best-effort create

<#-- egress only,when protect is set TODO: check w chin -->
<#if protect>
/configure qos ${sapType} ${qosId} queue 3 rate max cir 1000
</#if>

<#-- egress only, depending on whether we apply QoS -->
<#if apply>
/configure qos ${sapType} ${qosId} queue 2 ${max} max cir ${bps}
<#else>
/configure qos ${sapType} ${qosId} queue 3 rate max cir 0
</#if>

</#if>

</#list>


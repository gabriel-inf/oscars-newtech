<#-- @ftlvariable name="paths" type="java.util.List" -->
<#-- @ftlvariable name="path" type="net.es.oscars.pss.cmd.MplsPath" -->

<#list paths as path>
edit protocols mpls path ${path.name}
    <#list path.hops as hop>
    set ${hop} strict
    </#list>
top
</#list>

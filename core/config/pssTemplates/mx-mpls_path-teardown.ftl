<#-- @ftlvariable name="paths" type="java.util.List" -->
<#-- @ftlvariable name="path" type="net.es.oscars.pss.cmd.MplsPath" -->
<#-- @ftlvariable name="protect" type="java.lang.Boolean" -->

<#list paths as path>
delete protocols mpls path ${path.name}
</#list>

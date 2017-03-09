<#-- @ftlvariable name="lsps" type="java.util.List" -->
<#-- @ftlvariable name="lsp" type="net.es.oscars.pss.cmd.Lsp" -->
<#-- @ftlvariable name="filter" type="net.es.oscars.pss.cmd.MxFilter" -->


<#list lsps as lsp>
top
edit protocols mpls label-switched-path ${lsp.name}
set to ${lsp.to}
set metric 65000
set no-cspf
set priority 4 4
set primary ${lsp.pathName}
set policing filter ${filter.name}
</#list>
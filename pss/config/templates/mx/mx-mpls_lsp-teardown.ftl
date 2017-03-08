<#-- @ftlvariable name="lsps" type="java.util.List" -->
<#-- @ftlvariable name="lsp" type="net.es.oscars.pss.cmd.Lsp" -->
<#-- @ftlvariable name="filter" type="net.es.oscars.pss.cmd.MxFilter" -->


<#list lsps as lsp>
delete protocols mpls label-switched-path ${lsp.name}
</#list>



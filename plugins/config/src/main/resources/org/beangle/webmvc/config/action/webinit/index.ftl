[@b.head/]
[#include "../nav.ftl"/]
<div class="container-fluid">
<h4>Initializers Support By BootstrapListener</h4>
<table class="table  table-striped">
  <thead>
    <th>Initializer</th>
    <th>URL</th>
  </thead>
  <tbody>
    [#list initializers?keys as init]
    <tr>
       <td>${init}</td>
       <td>${initializers[init]}</td>
    </tr>
    [/#list]
  </tbody>
</table>

[#if webxml??]
<h4>web.xml</h4>
<pre class="code">
${webxml?html}
</pre>
[#else]
<h4>No web.xml</h4>
[/#if]
</div>
[@b.foot/]
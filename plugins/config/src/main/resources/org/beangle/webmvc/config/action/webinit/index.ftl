[@b.head/]
[#include "../nav.ftl"/]
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

<h4>web.xml</h4>
<pre class="code">
${webxml?html}
</pre>

[@b.foot/]
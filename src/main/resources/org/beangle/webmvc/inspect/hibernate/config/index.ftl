[@b.head/]
[#include "../nav.ftl"/]

<h4> Hibernate.cfg.xml Locations</h4>
<ul>
  [#list factory.configLocations as ml]
    <li>${ml.URL}</li>
  [/#list]
</ul>

<h4> Beangle Orm Locations</h4>
<ul>
  [#list factory.ormLocations  as ml ]
    <li>${ml.URL}</li>
  [/#list]
</ul>

<h4>Configurations & Settings</h4>
<table class="table table-striped">
  <thead>
    <th>Object</th>
    <th>Value</th>
  </thead>
  <tbody>
    [#list factory.properties?keys as s]
    [#if s?starts_with("hibernate")]
    <tr>
       <td>${s}</td>
       <td [#if s=='hibernate.show_sql' && factory.properties[s]=='true'] style="color:red"[/#if]>${factory.properties[s]}</td>
    </tr>
    [/#if]
    [/#list]
  </tbody>
</table>
[@b.foot/]

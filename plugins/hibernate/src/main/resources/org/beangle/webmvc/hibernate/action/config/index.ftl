[@b.head/]
[#include "../nav.ftl"/]

[#list factory.configLocations]
<h4> Hibernate.cfg.xml Locations</h4>
<ul>
    [#items as  as ml]
    <li>${ml.URL}</li>
    [/#items]
  [/#list]
</ul>

<h4> Beangle Orm Locations</h4>
<ul>
  [#list factory.ormLocations  as ml ]
    <li>${ml.URL}</li>
  [/#list]
</ul>

<h4> Beangle Namings</h4>
<ul>
  [#list factory.namingStrategy.policy.configLocations  as ml ]
    <li>${ml}
    <pre class="code">${action.getURLString(ml)?html}</pre>
    </li>
  [/#list]
</ul>

<h4>Configurations & Settings</h4>
<table class="table table-striped">
  <thead>
    <th>Object</th>
    <th>Value</th>
  </thead>
  <tbody>
    [#list factory.configuration.properties?keys as s]
    [#if s?starts_with("hibernate")]
    <tr>
       <td>${s}</td>
       <td [#if s=='hibernate.show_sql' && factory.configuration.properties[s]=='true'] style="color:red"[/#if]>${factory.configuration.properties[s]}</td>
    </tr>
    [/#if]
    [/#list]
  </tbody>
</table>
[@b.foot/]
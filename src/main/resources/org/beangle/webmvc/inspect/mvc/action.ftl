[@b.head/]
<h4>Action information - <a href="${request.contextPath}${Parameters['name']}" target="_blank">${Parameters['name']}</a></h4>

<table>
  <tr><td>Action class:</td><td> ${mapping.clazz.name}</td></tr>
  <tr><td>Namespace:</td><td>${mapping.namespace}</td></tr>
  <tr><td>Profile:</td><td>${mapping.profile.name}(${mapping.profile.pattern})</td></tr>
</table>

[@b.tabs]
  [@b.tab label="Mappings"]
    <table width="100%" class="table table-sm">
      <thead>
        <tr>
          <th width="10%">Method</th>
          <th width="37%">Mapping</th>
          <th width="38%">Action Method</th>
          <th width="15%">Default View</th>
        </tr>
      </thead>
      <tbody>
        [#list mappings?values as route]
        <tr>
          <td>${route.httpMethod!"*"}</td>
          <td>${route.url}</td>
          <td>${route.method.name}([#list route.arguments as p]${p}[#if p_has_next],[/#if][/#list])</td>
          <td>${route.defaultView!}</td>
        </tr>
      [/#list]
      </tbody>
    </table>
  [/@]
  [@b.tab label="Views"]
      <table width="100%" class="table table-sm">
      <tr><th>Name</th><th>Type</th><th>location</th></tr>
      [#list mapping.views?keys as viewName]
        <tr>
         <td>${viewName}</td>
         <td>${mapping.views[viewName].className}</td>
         <td>${mapping.views[viewName].location}</td>
        </tr>
      [/#list]
    </table>
  [/@]
  [@b.tab label="Properties"]
  <table width="100%" class="table table-sm">
        <thead>
        <tr><th>Name</th><th>Type</th></tr>
        </thead>
        <tbody>
        [#list properties as prop]
         <tr>
          <td>${prop.getter.name}</td>
          <td>${prop.clazz.name}</td>
         </tr>
        [/#list]
      </tbody>
    </table>
  [/@]
[/@]
[@b.foot/]

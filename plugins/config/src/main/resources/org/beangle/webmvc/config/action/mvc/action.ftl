[@b.head/]
<h4>Action information - <a href="${request.contextPath}${Parameters['name']}" target="_blank">${Parameters['name']}</a></h4>

<table>
  <tr><td>Action class:</td><td> ${config.clazz.name}</td></tr>
  <tr><td>Namespace:</td><td>${config.namespace}</td></tr>
  <tr><td>Profile:</td><td>${config.profile.name}(${config.profile.pattern})</td></tr>
</table>

[@b.tabs]
  [@b.tab label="Mappings"]
    <table width="100%" class="table">
      <thead>
        <tr>
          <th width="10%">Method</th>
          <th width="37%">Mapping</th>
          <th width="38%">Action Method</th>
          <th width="15%">Default View</th>
        </tr>
      </thead>
      <tbody>
        [#list config.mappings?values as mapping]
        <tr>
          <td>${mapping.httpMethod!"*"}</td>
          <td>[#if mapping.name?length>0]${Parameters['name']}/${mapping.name}[#else]${Parameters['name']}[/#if]</td>
          <td>${mapping.method.name}([#list mapping.arguments as p]${p.toString()}[#if p_has_next],[/#if][/#list])</td>
          <td>${mapping.defaultView!}</td>
        </tr>
      [/#list]
      </tbody>
    </table>
  [/@]
  [@b.tab label="Views"]
      <table width="100%" class="table">
      <tr><th>Name</th><th>Type</th><th>location</th></tr>
      [#list  config.views?keys as viewName]
        <tr>
         <td>${viewName}</td>
         <td>${config.views[viewName].class.name}</td>
         <td>${config.views[viewName].location}</td>
        </tr>
      [/#list]
    </table>
  [/@]
  [@b.tab label="Properties"]
  <table width="100%" class="table">
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
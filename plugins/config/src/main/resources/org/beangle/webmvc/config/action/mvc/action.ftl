[@b.head/]
<h4>Action information - ${Parameters['name']}</h4>

<table>
  <tr><td>Action class:</td><td> ${config.clazz.name}</td></tr>
  <tr><td>Profile:</td><td>${config.profile.name}(${config.profile.actionPattern})</td></tr>
</table>

[@b.tabs]
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
  [@b.tab label="Mappings"]
    <table width="100%" class="table">
      <thead>
        <tr>
          <th width="10%">Method</th>
          <th width="45%">Mapping</th>
          <th width="45%">Action Method</th>
        </tr>
      </thead>
      <tbody>
        [#list config.mappings?values as mapping]
        <tr>
          <td>${mapping.httpMethod!"*"}</td>
          <td>${Parameters['name']}/${mapping.name}</td>
          <td>${mapping.method.name}([#list mapping.params as p]${p}[#if p_has_next],[/#if][/#list])</td>
        </tr>
      [/#list]
      </tbody>
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
          <td>${prop.method.name}</td>
          <td>${prop.method.returnType.name}</td>
         </tr>
        [/#list]
      </tbody>
    </table>
  [/@]
[/@]
[@b.foot/]
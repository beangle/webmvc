[@b.head/]
[#include "../nav.ftl"/]
<div class="container-fluid">
<h4> Config Locations</h4>
<ul>
  [#list configLocations as url]
    <li>${url}</li>
  [/#list]
</ul>
<h4>Configurations & Settings</h4>
<table class="table  table-striped">
  <thead>
    <th>Object</th>
    <th>Value</th>
  </thead>
  <tbody>
    <tr>
       <td>Object Wrapper</td>
       <td>${config.objectWrapper.class.name}</td>
    </tr>
    <tr>
       <td>Template Loader</td>
       <td>${config.templateLoader.class.name}(${templatePath})</td>
    </tr>
    <tr>
       <td>Cache Storage</td>
       <td>
       ${config.cacheStorage.class.name}[#if properties?contains("cache_storage")](${properties["cache_storage"]})[/#if]
       </td>
    </tr>
    <tr>
       <td>Shared Variable Names</td>
       <td>[#list config.sharedVariableNames as name]${name}[#if name_has_next],[/#if][/#list]</td>
    </tr>
    <tr>
       <td>Incompatible Improvement Version</td>
       <td>${config.incompatibleImprovements}</td>
    </tr>
    <tr>
       <td>Localized Lookup</td>
       <td>${config.localizedLookup?string("True","False")}</td>
    </tr>
    [#if properties?contains("template_update_delay")]
    <tr>
       <td>Template Update Delay</td>
       <td [#if properties["template_update_delay"]=="0"]style="color:red"[/#if]>${properties["template_update_delay"]} seconds</td>
    </tr>
    [/#if]
    <tr>
       <td>Auto Import</td>
       <td>${properties["auto_import"]!}</td>
    </tr>
    <tr>
       <td>Auto Include</td>
       <td>${properties["auto_include"]!}</td>
    </tr>
    <tr>
       <td>Tag Syntax</td>
       <td>[#if config.tagSyntax==0]AUTO[#elseif config.tagSyntax==1]&lt;&gt;[#else][][/#if]</td>
    </tr>
    <tr>
       <td>Whitespace Stripping</td>
       <td>${config.whitespaceStripping?string("True","False")}</td>
    </tr>
    <tr> <td colspan="2"><hr></td></tr>
    [#list config.settings?keys as s]
    [#if s!="object_wrapper"]
    <tr>
       <td>${s}</td>
       <td>${config.getSetting(s)}</td>
    </tr>
    [/#if]
    [/#list]
  </tbody>
</table>
</div>
[@b.foot/]
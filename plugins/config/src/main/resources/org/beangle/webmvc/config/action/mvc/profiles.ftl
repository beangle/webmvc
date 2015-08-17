[#assign profilexml]
<?xml version="1.0"?>
<mvc>

[#list profiles as profile]
  <!--${profile.source!}-->
  <profile name="${profile.name}" package="${profile.pattern}">
    <action suffix="${profile.actionSuffix}" defaultMethod="${profile.defaultMethod}"/>
    <view path="${profile.viewPath}" style="${profile.viewPathStyle}" type="${profile.viewType}" suffix="${profile.viewSuffix}" />
    <url path="${profile.urlPath}" suffix="${profile.urlSuffix}"  style="${profile.urlStyle}" />
    <interceptors>
      [#list profile.interceptorNames as interceptor]
      <interceptor name="${interceptor}"/>
      [/#list]
    </interceptors>
  </profile>
  
[/#list]
</mvc>
[/#assign]
<pre class="code">
${profilexml?html}
</pre>

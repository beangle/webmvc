[#ftl/]
[@b.head/]
[#include "nav.ftl"/]

[#assign profilexml]
<profiles>

[#list profiles as profile]
  <!--${profile.source!}-->
  <profile name="${profile.name}" pattern="${profile.actionPattern}">
    <action suffix="${profile.actionSuffix}" defaultMethod="${profile.defaultMethod}"/>
    <view path="${profile.viewPath}" style="${profile.viewPathStyle}" type="${profile.viewType}" suffix="${profile.viewSuffix}" />
    <uri path="${profile.uriPath}" suffix="${profile.uriSuffix}"  style="${profile.uriStyle}" />
    <interceptors>
      [#list profile.interceptors as interceptor]
      <interceptor class="${interceptor.class.name}"/>
      [/#list]
    </interceptors>
  </profile>
  
[/#list]
</profiles>
[/#assign]
<pre class="code">
${profilexml?html}
</pre>
[@b.foot/]
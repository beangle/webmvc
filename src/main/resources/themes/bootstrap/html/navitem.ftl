[#ftl]
<li class="nav-item">
[#if tag.href??]
 [#if tag.onclick??]
   [#assign onclick=tag.onclick]
 [#else]
   [#if tag.target??]
     [#assign onclick="jQuery(this).parent().siblings().each(function (i,li){jQuery(li).children('a').removeClass('active')});jQuery(this).addClass('active');return bg.Go(this,'${tag.target}')"]
   [#else]
     [#assign onclick="return bg.Go(this,null)"]
   [/#if]
 [/#if]
<a href="${tag.href}" class="nav-link [#if tag.active]active[/#if]" [#if onclick??]onclick="${onclick}"[/#if] ${tag.parameterString}>${tag.body!}</a>
[#else]
  ${tag.body!}
[/#if]
</li>

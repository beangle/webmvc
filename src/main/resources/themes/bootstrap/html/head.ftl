[#ftl]
[#if !b.ajax]
<!DOCTYPE html>
<html lang="zh_CN">
  <head>
    <title>[#if tag.parameters['title']??]${tag.parameters['title']}[/#if]</title>
    <meta http-equiv="content-type" content="text/html;charset=utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
    <meta http-equiv="pragma" content="no-cache"/>
    <meta http-equiv="cache-control" content="no-cache"/>
    <meta http-equiv="expires" content="0"/>
    <meta http-equiv="content-style-type" content="text/css"/>
    <meta http-equiv="content-script-type" content="text/javascript"/>
    [#if tag.loadui]
    <link rel="icon" href="data:;base64,=">
    <base href="${b.base}/"/>
    ${b.static.load(["requirejs","jquery","beangle","bui"])}
    <script type="text/javascript">
    beangle.register("${b.static_base}/",{
      [#assign contents = b.static.module_contents/]
      [#list contents?keys?sort as k]
        "${k}":${contents[k]}[#if k_has_next],[/#if]
      [/#list]
    });
    bg.load(["jquery-form","bootstrap","font-awesome","adminlte"])
    </script>
    [#else]
    ${b.static.load(["jquery","beangle"])}
    [/#if]
  [@include_optional path="head_ext.ftl"/]
  ${tag.body}
 </head>
 <body[#if tag.smallText] class="text-sm"[/#if]>
[/#if]

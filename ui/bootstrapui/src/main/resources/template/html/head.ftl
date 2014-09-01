[#ftl]
[#if !(request.getHeader('x-requested-with')??) && !Parameters['x-requested-with']??]
<!DOCTYPE html>
<html lang="zh_CN">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta http-equiv="pragma" content="no-cache"/>
    <meta http-equiv="cache-control" content="no-cache"/>
    <meta http-equiv="content-style-type" content="text/css"/>
    <meta http-equiv="content-script-type" content="text/javascript"/>
    <meta http-equiv="expires" content="0"/>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>[#if tag.parameters['title']??]${tag.parameters['title']}[/#if]</title>
    [#--[@b.agent/]--]
    [#if tag.loadui]
    [@beangle_js_head/][#--[@beangle_uijs_head/]--]
    [#else]
    [@beangle_js_head/]
    [/#if]
    ${tag.body}
  </head>
  <body class="container">
[/#if]


[#macro beangle_uijs_head]
  <script type="text/javascript" src="${base}/static/scripts/jquery/jquery.min,jquery-ui.min.js?bg=3.4.3"></script>
  <script type="text/javascript" src="${base}/static/scripts/common/require,bootstrap.min.js"></script>
[#if tag.compressed]
  <script type="text/javascript" src="${base}/static/scripts/plugins/jquery-form,jquery-history,jquery-colorbox,jquery-chosen.js?bg=3.4.3"></script>
  <script type="text/javascript" src="${base}/static/scripts/beangle/beangle,beangle-ui.js?bg=3.4.3"></script>
[#else]
  <script type="text/javascript" src="${base}/static/scripts/plugins/jquery-form.js?bg=3.4.3&compress=no"></script>
  <script type="text/javascript" src="${base}/static/scripts/plugins/jquery-history.js?bg=3.4.3&compress=no"></script>
  <script type="text/javascript" src="${base}/static/scripts/plugins/jquery-colorbox.js?bg=3.4.3&compress=no"></script>
  <script type="text/javascript" src="${base}/static/scripts/plugins/jquery-chosen.js?bg=3.4.3&compress=no"></script>
  <script type="text/javascript" src="${base}/static/scripts/beangle/beangle.js?bg=3.4.3&compress=no"></script>
  <script type="text/javascript" src="${base}/static/scripts/beangle/beangle-ui.js?bg=3.4.3&compress=no"></script>
[/#if]
[/#macro]

[#macro beangle_js_head]
[#assign themeName = Parameters['ui.theme']!"default"]
[#if tag.compressed]
  <script type="text/javascript" src="${base}/static/scripts/jquery/jquery,/static/scripts/beangle/beangle.js"></script>
[#else]
  <script type="text/javascript" src="${base}/static/scripts/jquery/jquery,/scripts/beangle/beangle.js?compress=no"></script>
[/#if]
  <link rel="stylesheet" href="${base}/static/themes/default/bootstrap.min.css">
  <script src="${base}/static/scripts/bootstrap/bootstrap.min.js"></script>
  <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
  <!--[if lt IE 9]>
  <script src="http://cdn.bootcss.com/html5shiv/3.7.2/html5shiv.min.js"></script>
  <script src="http://cdn.bootcss.com/respond.js/1.4.2/respond.min.js"></script>
  <![endif]-->
[/#macro]

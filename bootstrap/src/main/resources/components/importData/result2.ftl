[#ftl/]
[@b.head/]
<div class="container">
  [@b.toolbar title="数据导入结果"]
      bar.addClose();
  [/@]
  <div class="alert [#if importer.fail>0]alert-warning[#else]alert-success[/#if]" role="alert">
  成功导入：${(importer.success)!}[#if importer.fail>0]&nbsp;失败：${importer.fail}[/#if]
  </div>
  [#if (importResult.errs)?? && importResult.errs?size > 0]
  [@b.grid items=importResult.errs var="message" sortable="false" style="border:0.5px solid #006CB2"]
      [@b.row]
          [@b.col title="错误序号" width="10%"]${message_index + 1}[/@]
          [@b.col title="行号" property="index" width="10%"]${(message.location)!}[/@]
          [@b.col title="错误内容" width="40%"][#if message.message?starts_with("error")]${b.text(message.message)}[#else]${message.message}[/#if][/@]
          [@b.col title="错误值"][#list message.values as value]${value?default("")}[/#list][/@]
      [/@]
  [/@]
  [/#if]
  <br>
  [#if (importResult.msgs)?? && importResult.msgs?size > 0]
  [@b.grid items=importResult.msgs var="message" sortable="false"]
      [@b.row]
          [@b.col title="序号" width="10%"]${message_index + 1}[/@]
          [@b.col title="行号" property="index" width="10%"]${(message.location)!}[/@]
          [@b.col title="提示内容" width="40%"][#if message.message?starts_with("error")]${b.text(message.message)}[#else]${message.message}[/#if][/@]
          [@b.col title="提示值"][#list message.values as value]${value?default("")}[/#list][/@]
      [/@]
  [/@]
  [/#if]
</div>
[@b.foot/]

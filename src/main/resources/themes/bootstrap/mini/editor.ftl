[#ftl]
<li [#if tag.hidden]style="display:none"[/#if]>[#if tag.label??]<label for="${tag.id}" class="title">[#if (tag.required!"")=="true"]<em class="required">*</em>[/#if]${tag.label}:</label>[/#if]<textarea id="${tag.id}" [#if tag.title??]title="${tag.title}"[/#if] name="${tag.name}" [#if tag.rows??] rows="${tag.rows}"[/#if][#if tag.cols??] cols="${tag.cols}"[/#if] ${tag.parameterString}>${tag.value}</textarea>[#if tag.comment??]<label class="comment">${tag.comment}</label>[/#if]
<script>
beangle.load(["kindeditor"],function(){
   var editor = null;
   editor = KindEditor.create('#${tag.id}', {
    resizeType : 1,
    allowPreviewEmoticons : false,
    [#if tag.uploadJson??]
    allowImageUpload : true,
    allowFileUpload:true,
    [#else]
    allowImageUpload : false,
    allowFileUpload:false,
    allowFileManager:false,
    [/#if]
    allowFlashUpload:false,
    allowMediaUpload:false,
    loadStyleMode:false,
    afterBlur : function() {
      var originHtml = editor.html();
      var html = originHtml;
      html = html.replace(/(<script[^>]*>)([\s\S]*?)(<\/script>)/ig, '');
      html = html.replace(/(<style[^>]*>)([\s\S]*?)(<\/style>)/ig, '');
      html = KindEditor.formatHtml(html, {
        table : ['border'],
        'td,th' : ['rowspan', 'colspan'],
        'tr,tbody,thead':[],
        p:['align'],
        div:[],
        a:[],
        img:['src','width','height',''],
        'strong,em,i,u,del':[],
        'blockquote':[],
        'hr':[],
        'br':[],
        'li,ul,ol':[],
        'sup,sub':[]
      });
      $('#${tag.id}').val(html);
      if(originHtml!=html){
        KindEditor.html('#${tag.id}',html);
      }
    },
    [#if tag.uploadJson??]
    uploadJson:'${tag.uploadJson}',
    items:['source','preview', 'wordpaste', 'indent', 'outdent', 'bold', 'italic', 'underline', 'removeformat', '|', 'image','table','fullscreen']
    [#else]
    items:['source','preview', 'wordpaste', 'indent', 'outdent', 'bold', 'italic', 'underline', 'removeformat', '|','table','fullscreen']
    [/#if]
  });
});
</script>
</li>

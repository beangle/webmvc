[#ftl]
<li [#if tag.hidden]style="display:none"[/#if]>[#if tag.label??]<label for="${tag.id}" class="title">[#if (tag.required!"")=="true"]<em class="required">*</em>[/#if]${tag.label}:</label>[/#if]<textarea id="${tag.id}" [#if tag.title??]title="${tag.title}"[/#if] name="${tag.name}" [#if tag.rows??] rows="${tag.rows}"[/#if][#if tag.cols??] cols="${tag.cols}"[/#if] ${tag.parameterString}>${tag.value}</textarea>[#if tag.comment??]<label class="comment">${tag.comment}</label>[/#if]
<script>
beangle.load(["kindeditor"],function(){
   var editor = null;
   editor = KindEditor.create('#${tag.id}', {
    resizeType : 1,
    allowPreviewEmoticons : false,
    [#if tag.allowImageUpload=="true"]
    allowImageUpload : true,
    [#else]
    allowImageUpload : false,
    [/#if]
    allowFlashUpload:false,
    allowMediaUpload:false,
    allowFileUpload:false,
    allowFileManager:true,
    loadStyleMode:false,
    afterBlur : function() {
      var html = editor.html();
      html = html.replace(/(<script[^>]*>)([\s\S]*?)(<\/script>)/ig, '');
      html = html.replace(/(<style[^>]*>)([\s\S]*?)(<\/style>)/ig, '');
      html = KindEditor.formatHtml(html, {
        table : ['border'],
        'td,th' : ['rowspan', 'colspan'],
        'tr,tbody,thead':[],
        p:['align'],
        div:[],
        a:[],
        img:[],
        'strong,em,i,u':[],
        'blockquote':[],
        'hr':[]
      });
      $('#${tag.id}').val(html);
      KindEditor.html('#${tag.id}',html);
    },
    [#if tag.allowImageUpload=="true"]
    //afterUpload:function(){this.sync();},
    uploadJson:'${b.url("!uploadImage")}',
    items:['source','preview', 'wordpaste', 'indent', 'outdent', 'bold', 'italic', 'underline', 'removeformat', '|', 'image','table','fullscreen']
    [#else]
    items:['source','preview', 'wordpaste', 'indent', 'outdent', 'bold', 'italic', 'underline', 'removeformat', '|','table','fullscreen']
    [/#if]
  });
});
</script>
</li>

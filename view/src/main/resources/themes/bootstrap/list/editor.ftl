[#ftl]
<li>[#if tag.label??]<label for="${tag.id}" class="title">[#if (tag.required!"")=="true"]<em class="required">*</em>[/#if]${tag.label}:</label>[/#if]<textarea id="${tag.id}" [#if tag.title??]title="${tag.title}"[/#if] name="${tag.name}" [#if tag.rows??] rows="${tag.rows}"[/#if][#if tag.cols??] cols="${tag.cols}"[/#if] ${tag.parameterString}>${tag.value}</textarea>[#if tag.comment??]<label class="comment">${tag.comment}</label>[/#if]
<script>
beangle.load(["kindeditor"],function(){
   var editor = null;
   editor = KindEditor.create('#${tag.id}', {
    resizeType : 1,
    allowPreviewEmoticons : false,
    allowImageUpload : false,
    allowFlashUpload:false,
    allowMediaUpload:false,
    allowFileUpload:false,
    allowFileManager:false,
    loadStyleMode:false,
    afterBlur : function() {$('#${tag.id}').val(editor.html());},
    items:[ 'source', '|', 'undo', 'redo', '|', 'preview', 'code', 'cut', 'copy', 'paste', 'plainpaste', 'wordpaste', '|', 'justifyleft', 'justifycenter', 'justifyright', 'justifyfull', 'insertorderedlist', 'insertunorderedlist', 'indent', 'outdent', 'subscript', 'superscript', 'clearhtml', 'quickformat', 'selectall', '|', 'fullscreen', '/', 'formatblock', 'fontname', 'fontsize', '|', 'forecolor', 'hilitecolor', 'bold', 'italic', 'underline', 'strikethrough', 'lineheight', 'removeformat', '|', 'image', 'multiimage', 'flash', 'media', 'insertfile', 'table', 'hr',  'pagebreak', 'anchor', 'link', 'unlink']
  });
});
</script>
</li>

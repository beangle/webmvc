<div [#if tag.id??]id="${tag.id}"[/#if] class="${tag.cssClass}" ${tag.parameterString}>
[#if tag.title??]
    <h3 class="card-title">${tag.title}</h3>
    [#if tag.minimal=="true" || tag.closeable=="true"]
    <div class="card-tools">
      [#if tag.minimal == "true"]
      <button type="button" class="btn btn-tool" data-card-widget="collapse">
        <i class="fas fa-minus"></i>
      </button>
      [/#if]
      [#if tag.closeable == "true"]
      <button type="button" class="btn btn-tool" data-card-widget="remove">
        <i class="fas fa-times"></i>
      </button>
      [/#if]
    </div>
    [/#if]
 [/#if]
  ${tag.body}
</div>

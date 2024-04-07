<nav [#if tag.id??] id="${tag.id}"[/#if] class="navbar[#if tag.cssClass??] ${tag.cssClass}[#else] navbar-expand-lg navbar-light bg-light[/#if]" ${tag.parameterString}>
   [#if tag.brand??]
      [#if tag.brand?contains("<")]
      ${tag.brand}
      [#else]
      <a class="navbar-brand" href="#">${tag.brand}</a>
      [/#if]
   [/#if]
   ${tag.body!}
</nav>

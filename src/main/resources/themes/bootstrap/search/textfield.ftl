[#ftl]
<div class="search-item"><label style="font-weight:inherit" for="${tag.id}">${tag.label!}:</label><input type="text" id="${tag.id}" name="${tag.name}" maxlength="${tag.maxlength}" [#if !tag.parameters['value']??]value="${Parameters[tag.name]!?html}"[/#if]${tag.parameterString}/></div>

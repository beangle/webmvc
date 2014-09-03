[#list factories?keys as k]
[@b.a href="config/${k?replace('.','_')?replace('#','_')}/index"]${k}[/@]
[/#list]
{
 [#assign nonevoid = (method.returnType.name != "void")]
 [#if method.parameterTypes?size==0]
   [#if nonevoid]return action.${method.name}();[#else]action.${method.name}();return null;[/#if]
 [#else]
   org.beangle.webmvc.api.context.ActionContext context = org.beangle.webmvc.api.context.ContextHolder$.MODULE$.context();
   [#assign needconverter=false/]
   [#list method.parameterTypes as pt]
     [#if pt.name!="javax.servlet.http.HttpServletRequest" && pt.name!="javax.servlet.http.HttpServletResponse"]
     [#assign needconverter=true/]
     [#break/]
     [/#if]
   [/#list]
   [#if needconverter]
   scala.collection.immutable.Map params = context.params();
   org.beangle.commons.collection.MapConverter converter = org.beangle.webmvc.api.context.Params$.MODULE$.converter();
   String[] paramNames = $1.params();
   [/#if]
   [#assign paramNameMap = []/]
   [#list method.parameterTypes as pt]
     [#if pt.name =="javax.servlet.http.HttpServletRequest"]
       [#assign paramNameMap = paramNameMap + ["context.request()"]/]
     [#elseif pt.name =="javax.servlet.http.HttpServletResponse"]
       [#assign paramNameMap = paramNameMap + ["context.response()"]/]
     [#else]
       scala.Option vp = params.get(paramNames[${pt_index}]);
       Object v${pt_index} = null;
       if(vp.isEmpty()){throw new IllegalArgumentException("Cannot bind parameter "+ paramNames[${pt_index}]+" for ${actionClass.name}.${method.name}");
       }else{v${pt_index} =vp.get();}
       
       [#if !pt.isArray()]
       if(v${pt_index}.getClass().isArray()) v${pt_index}= ((Object[])v${pt_index})[0];
       [/#if]
       
       [#assign paramNameMap = paramNameMap + ["vp${pt_index}"]/]
       [#if !pt.primitive]
         [#if pt.name=="java.lang.String"]
         ${pt.name} vp${pt_index} = (${pt.name})v${pt_index};
         [#else]
         ${pt.name} vp${pt_index} = (${pt.name})converter.convert(v${pt_index}, ${pt.name}.class);
         [/#if]
         if(null== vp${pt_index}) throw new IllegalArgumentException("Cannot bind parameter "+ paramNames[${pt_index}]+" for ${actionClass.name}.${method.name}");
       [#else]
         Object vWrapper${pt_index} = converter.convert(v${pt_index}, ${Primitives.wrap(pt).name}.class);
         if(null== vWrapper${pt_index}) throw new IllegalArgumentException("Cannot bind parameter "+ paramNames[${pt_index}]+" for ${actionClass.name}.${method.name}");
         ${pt.name} vp${pt_index} = ((${Primitives.wrap(pt).name})vWrapper${pt_index}).${pt.name}Value();
       [/#if]
     [/#if]
   [/#list]
   [#if nonevoid]return action.${method.name}([#list paramNameMap as pn]${pn}[#if pn_has_next],[/#if][/#list]);
   [#else]action.${method.name}([#list paramNameMap as pn]${pn}[#if pn_has_next],[/#if][/#list]);return null;
   [/#if]
 [/#if]
}
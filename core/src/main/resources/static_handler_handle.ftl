{
 [#assign nonevoid = (method.returnType.name != "void")]
 [#if method.parameterTypes?size==0]
   [#if nonevoid]return action.${method.name}();[#else]action.${method.name}();return null;[/#if]
 [#else]
   [#assign needRequest=false/]
   [#assign needParam=false/]
   [#assign needConverter=false/]
   [#assign arguments = mapping.arguments()/]
   [#assign parameterTypes =method.parameterTypes/]
   [#list arguments as argu]
      [#if argu.class.name=="org.beangle.webmvc.context.impl.HeaderArgument" || argu.class.name=="org.beangle.webmvc.context.impl.CookieArgument"]
        [#assign needRequest=true/]
        [#if parameterTypes[argu_index].name!="java.lang.String"]
        [#assign needConverter=true/]
        [/#if]
      [/#if]
      [#if argu.class.name=="org.beangle.webmvc.context.impl.ParamArgument"]
        [#assign needParam=true/]
        [#assign needConverter=true/]
      [/#if]
   [/#list]

   org.beangle.webmvc.api.context.ActionContext context = org.beangle.webmvc.api.context.ContextHolder$.MODULE$.context();
   [#if needConverter]
   org.beangle.commons.collection.MapConverter converter = org.beangle.webmvc.api.context.Params$.MODULE$.converter();
   [/#if]
   [#if needRequest]
   javax.servlet.http.HttpServletRequest request = context.request();
   [/#if]
   [#if needParam]
   scala.collection.immutable.Map params = context.params();
   [/#if]
   [#assign paramMap = []/]
   [#list method.parameterTypes as pt]
     [#if pt.name =="javax.servlet.http.HttpServletRequest"]
       [#if needRequest][#assign paramMap = paramMap + ["request"]/][#else][#assign paramMap = paramMap + ["context.request()"]/][/#if]
     [#elseif pt.name =="javax.servlet.http.HttpServletResponse"]
       [#assign paramMap = paramMap + ["context.response()"]/]
     [#else]
       [#assign argument= arguments[pt_index]/]
       [#assign paramAsString= false/]
       
       [#if argument.class.name=="org.beangle.webmvc.context.impl.ParamArgument"]
       scala.Option vp${pt_index} = params.get("${argument.name()}");
       Object v${pt_index} = null;
       if(vp${pt_index}.isEmpty()){[@handleNone argument , pt_index /]
       }else{
         v${pt_index} =vp${pt_index}.get();
         [#if !pt.isArray()]
         if(v${pt_index}.getClass().isArray()) v${pt_index}= ((Object[])v${pt_index})[0];
         [/#if]
       }
       [#elseif argument.class.name="org.beangle.webmvc.context.impl.CookieArgument"]
       [#assign paramAsString= true/]
       String v${pt_index} = org.beangle.commons.web.util.CookieUtils.getCookieValue(request,"${argument.name()}");
       if(null==v${pt_index}){[@handleNone argument , pt_index /]}

       [#elseif argument.class.name="org.beangle.webmvc.context.impl.HeaderArgument"]
       [#assign paramAsString= true/]
       String v${pt_index} = request.getHeader("${argument.name()}");
       if(null==v${pt_index}){[@handleNone argument,pt_index /]}
       [/#if]
       
       [#if paramAsString && pt.name=="java.lang.String"]
       [#assign paramMap = paramMap + ["v${pt_index}"]/]
       [#else]
       [#assign paramMap = paramMap + ["vp${pt_index}"]/]
       [/#if]

       [#if !pt.primitive]
         [#if !(paramAsString && pt.name=="java.lang.String") ]
         ${pt.name} vp${pt_index} = (${pt.name})converter.convert(v${pt_index}, ${pt.name}.class);
           [#if argument.required()]
           if(null == vp${pt_index}) throw new IllegalArgumentException("Cannot bind parameter ${argument.toString()} for ${actionClass.name}.${method.name}");
           [/#if]
         [/#if]
       [#else]
         Object vWrapper${pt_index} = converter.convert(v${pt_index}, ${Primitives.wrap(pt).name}.class);
         if(null== vWrapper${pt_index}) throw new IllegalArgumentException("Cannot bind parameter ${argument.toString()} for ${actionClass.name}.${method.name}");
         ${pt.name} vp${pt_index} = ((${Primitives.wrap(pt).name})vWrapper${pt_index}).${pt.name}Value();
       [/#if]
     [/#if]
   [/#list]
   [#if nonevoid]return action.${method.name}([#list paramMap as pn]${pn}[#if pn_has_next],[/#if][/#list]);
   [#else]action.${method.name}([#list paramMap as pn]${pn}[#if pn_has_next],[/#if][/#list]);return null;
   [/#if]
 [/#if]
}
[#macro handleNone(argument,idx)]
[#if argument.required()]
   [#if argument.defaultValue()==DefaultNone]
     throw new IllegalArgumentException("Cannot bind parameter ${argument.toString()} for ${actionClass.name}.${method.name}");
   [#else]
     v${idx}="${argument.defaultValue()!}";
   [/#if]
 [#else]
   [#if argument.defaultValue() != DefaultNone]
     v${idx}="${argument.defaultValue()}";
   [/#if]
 [/#if]
[/#macro]

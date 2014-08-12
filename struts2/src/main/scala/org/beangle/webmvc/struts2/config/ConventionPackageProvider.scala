package org.beangle.webmvc.struts2.config

import java.lang.reflect.Method

import scala.collection.JavaConversions.seqAsJavaList

import org.apache.struts2.views.freemarker.FreemarkerManager
import org.beangle.commons.inject.{ Container, ContainerAware }
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.{ ClassLoaders, Objects, Strings }
import org.beangle.commons.lang.reflect.ClassInfo
import org.beangle.commons.lang.time.Stopwatch
import org.beangle.commons.logging.Logging
import org.beangle.commons.text.i18n.spi.TextBundleRegistry
import org.beangle.commons.web.context.ServletContextHolder
import org.beangle.webmvc.annotation.{ action, ignore, result, results }
import org.beangle.webmvc.route.{ Action, ActionFinder, ActionMapping, ContainerActionFinder, RequestMapper, RouteService }
import org.beangle.webmvc.route.impl.{ DefaultViewMapper, HierarchicalUrlMapper, RequestMappingBuilder }
import org.beangle.webmvc.view.freemarker.{ TemplateFinder, TemplateFinderByLoader }

import com.opensymphony.xwork2.config.{ Configuration, ConfigurationException, PackageProvider }
import com.opensymphony.xwork2.config.entities.{ ActionConfig, PackageConfig, ResultConfig }
import com.opensymphony.xwork2.factory.ActionFactory
import com.opensymphony.xwork2.inject.Inject
import com.opensymphony.xwork2.util.classloader.ReloadingClassLoader
import com.opensymphony.xwork2.util.finder.{ ClassLoaderInterface, ClassLoaderInterfaceDelegate }

/**
 * This class is a configuration provider for the XWork configuration system. This is really the
 * only way to truly handle loading of the packages, actions and results correctly.
 */
class ConventionPackageProvider(val configuration: Configuration, val actionFinder: ActionFinder) extends PackageProvider with Logging {

  private var actionPackages = new collection.mutable.ListBuffer[String]

  @Inject("beangle.convention.action.suffix")
  var actionSuffix: String = "Action"

  @Inject("struts.devMode")
  var devMode = "false"

  @Inject
  var routeService: RouteService = _

  @Inject
  var registry: TextBundleRegistry = _

  @Inject("beangle.i18n.resources")
  var defaultBundleNames: String = _

  @Inject("beangle.i18n.reload")
  var reloadBundles: String = "false"

  @Inject("beangle.convention.preloadftl")
  var preloadftl: String = "true"

  @Inject
  var mapper: RequestMapper = _

  @Inject
  var freemarkerManager: FreemarkerManager = _

  private var reloadingClassLoader: ReloadingClassLoader = _

  private var defaultParentPackage: String = "beangle"

  private var templateFinder: TemplateFinder = _

  private var objectContainer: Container = _
  /** [url- > classname] */
  private var primaryMappings: Map[String, String] = Map.empty

  @Inject
  def this(configuration: Configuration, actionFactory: ActionFactory) {
    this(configuration, new ContainerActionFinder(actionFactory.asInstanceOf[ContainerAware].container))
    objectContainer = actionFactory.asInstanceOf[ContainerAware].container
  }

  protected def initReloadClassLoader() {
    if (isReloadEnabled() && reloadingClassLoader == null) reloadingClassLoader = new ReloadingClassLoader(getClassLoader())
  }

  protected def getClassLoader(): ClassLoader = Thread.currentThread().getContextClassLoader()

  @throws(classOf[ConfigurationException])
  def init(configuration: Configuration) = {
    registry.addDefaults(defaultBundleNames.split(","): _*)
    registry.reloadable = java.lang.Boolean.parseBoolean(reloadBundles)
    val sc = ServletContextHolder.context
    templateFinder = new TemplateFinderByLoader(freemarkerManager.getConfiguration(sc).getTemplateLoader(), routeService.viewMapper)

    val url = ClassLoaders.getResource("struts.properties")
    if (null != url) {
      val pmappings = new collection.mutable.HashMap[String, String]
      IOs.readJavaProperties(url) foreach {
        case (k, v) =>
          if (k.startsWith("url.")) pmappings.put(Strings.substringAfter(k, "url."), v)
      }
      primaryMappings = pmappings.toMap
    }
  }

  @throws(classOf[ConfigurationException])
  def loadPackages() {
    var watch = new Stopwatch(true)
    routeService.profiles foreach { profile =>
      if (profile.actionScan) actionPackages += profile.actionPattern
    }
    if (actionPackages.isEmpty) { return }

    initReloadClassLoader()
    val packageConfigs = new collection.mutable.HashMap[String, PackageConfig.Builder]()
    var newActions, overrideActions = 0
    val actionTypes = actionFinder.getActions(new ActionFinder.Test(actionSuffix, actionPackages))

    val name2Clazz = new collection.mutable.HashMap[String, Class[_]]
    val name2Packages = new collection.mutable.HashMap[String, PackageConfig.Builder]

    for ((actionClass, beanName) <- actionTypes) {
      var profile = routeService.getProfile(actionClass.getName())
      val action = routeService.buildAction(actionClass)
      val key =
        if (action.namespace.equals("/")) action.namespace + action.name
        else action.namespace + "/" + action.name

      val primaryClassName = primaryMappings.get(key).orNull
      if (null == primaryClassName || primaryClassName.equals(actionClass.getName())) {
        val exist = name2Clazz.get(key).orNull
        val pcb =
          if (null == exist) buildPackageConfig(actionClass, action, packageConfigs)
          else name2Packages.get(key).orNull

        var created = false
        //subclass or nonrelated class
        if (null == exist || !actionClass.isAssignableFrom(exist)) {
          created = createActionConfig(pcb, action, actionClass, beanName)
          if (created) {
            if (null == exist) newActions += 1 else overrideActions += 1
          }
        }
        if (created) {
          name2Clazz.put(key, actionClass);
          name2Packages.put(key, pcb)
          // build all action to action mappings
          val classInfo = ClassInfo.get(actionClass)
          routeService.buildMappings(actionClass) foreach {
            case (action, method) =>
              addAction2Mapper(action, beanName, method)
              if (method.getName == "index" && method.getParameterTypes.length == 0
                && action.httpMethod == null && action.url.endsWith("/index")) {
                val indexUrl = Strings.substringBeforeLast(action.url, "/index")
                addAction2Mapper(new ActionMapping(action.httpMethod, indexUrl, action.clazz, action.method,
                  action.paramNames, action.urlParamNames, action.namespace, action.name), beanName, method)
              }
          }
        }
      }
    }
    newActions += buildIndexActions(packageConfigs)
    // Add the new actions to the configuration(remove duplicated package builder for key != builder.name)
    val processedPackages = new collection.mutable.HashSet[String]
    for (builder <- packageConfigs.values) {
      val packageName = builder.getName
      if (!processedPackages.contains(packageName)) {
        configuration.removePackageConfig(packageName)
        configuration.addPackageConfig(packageName, packageConfigs(packageName).build())
        processedPackages += packageName
      }
    }

    info(s"Action scan completed,create ${newActions} action(override ${overrideActions}) in ${watch}.")
    templateFinder = null
  }

  private def addAction2Mapper(action: ActionMapping, beanName: String, method: Method): Unit = {
    val bean: Object = objectContainer.getBean(beanName).get
    mapper.asInstanceOf[HierarchicalUrlMapper].add(RequestMappingBuilder.build(action, bean, method))
  }

  protected def getClassLoaderInterface(): ClassLoaderInterface = {
    if (isReloadEnabled()) return new ClassLoaderInterfaceDelegate(reloadingClassLoader)
    else {
      var classLoaderInterface: ClassLoaderInterface = null
      var ctx = com.opensymphony.xwork2.ActionContext.getContext()
      if (ctx != null)
        classLoaderInterface = ctx.get(ClassLoaderInterface.CLASS_LOADER_INTERFACE).asInstanceOf[ClassLoaderInterface]
      Objects.defaultIfNull(classLoaderInterface, new ClassLoaderInterfaceDelegate(getClassLoader()))
    }
  }

  protected def isReloadEnabled(): Boolean = devMode == "true"

  protected def createActionConfig(pkgCfg: PackageConfig.Builder, action: Action, actionClass: Class[_], beanName: String): Boolean = {
    var actionConfig = new ActionConfig.Builder(pkgCfg.getName(), action.name, beanName)
    actionConfig.methodName(action.method)
    var actionName = action.name
    // check action exists on that package (from XML config probably)
    var existedPkg = configuration.getPackageConfig(pkgCfg.getName())
    var create: Boolean = true
    if (existedPkg != null) {
      var existed = existedPkg.getActionConfigs().get(actionName)
      create = (null == existed)
    }
    if (create) {
      import scala.collection.JavaConversions._
      actionConfig.addResultConfigs(buildResultConfigs(actionClass, pkgCfg))
      pkgCfg.addActionConfig(actionName, actionConfig.build())
      debug(s"Add ${pkgCfg.getNamespace()}/${actionName} for ${actionClass.getName()} in ${pkgCfg.getName()}")
    }
    create
  }

  protected def shouldGenerateResult(m: Method): Boolean = {
    if (classOf[String].equals(m.getReturnType()) && m.getParameterTypes.length == 0
      && null == m.getAnnotation(classOf[ignore])) {
      var name = m.getName().toLowerCase()
      !(name.startsWith("save") || name.startsWith("remove")
        || name.startsWith("export") || name.startsWith("import")
        || name.startsWith("get") || Strings.contains(name, "$"))
    } else {
      false
    }
  }

  protected def buildResultConfigs(clazz: Class[_], pcb: PackageConfig.Builder): Seq[ResultConfig] = {
    var configs = new collection.mutable.ListBuffer[ResultConfig]
    // load annotation results
    var results = new Array[result](0)
    val rs = clazz.getAnnotation(classOf[results])
    if (null == rs) {
      val an = clazz.getAnnotation(classOf[action])
      if (null != an) results = an.results()
    } else {
      results = rs.value()
    }
    val annotationResults = new collection.mutable.HashSet[String]
    if (null != results) {
      for (result <- results) {
        var resultType = result.`type`()
        if (Strings.isEmpty(resultType)) resultType = "dispatcher"
        val rtc = pcb.getResultType(resultType)
        val rcb = new ResultConfig.Builder(result.name(), rtc.getClassName())
        if (null != rtc.getDefaultResultParam()) rcb.addParam(rtc.getDefaultResultParam(), result.location())
        configs += rcb.build()
        annotationResults.add(result.name())
      }
    }
    // load ftl convension results
    var suffix = routeService.getProfile(clazz.getName).viewSuffix
    if (preloadftl == "true" && suffix.endsWith(".ftl")) {
      var resultTypeConfig = configuration.getPackageConfig("struts-default").getAllResultTypeConfigs().get("freemarker")
      ClassInfo.get(clazz).getMethods foreach { mi =>
        val m = mi.method
        var name = m.getName()
        if (!annotationResults.contains(name) && shouldGenerateResult(m)) {
          val path = templateFinder.find(clazz, DefaultViewMapper.defaultView(name, name), suffix)
          if (null != path) {
            configs += new ResultConfig.Builder(name, resultTypeConfig.getClassName()).addParam(
              resultTypeConfig.getDefaultResultParam(), path).build()
          }
        }
      }
    }
    configs
  }

  protected def buildPackageConfig(actionClass: Class[_], action: Action, packageConfigs: collection.mutable.Map[String, PackageConfig.Builder]): PackageConfig.Builder = {
    // 循环查找父包
    var actionPkg = actionClass.getPackage.getName
    var parentPkg: PackageConfig = null
    var ifContinue = true
    while (Strings.contains(actionPkg, '.') && ifContinue) {
      parentPkg = configuration.getPackageConfig(actionPkg)
      if (null != parentPkg) ifContinue = false else actionPkg = Strings.substringBeforeLast(actionPkg, ".")
    }
    if (null == parentPkg) {
      actionPkg = defaultParentPackage
      parentPkg = configuration.getPackageConfig(actionPkg)
    }
    if (parentPkg == null) {
      throw new ConfigurationException(s"Unable to locate parent package [${actionClass.getPackage.getName}]")
    }
    var actionPackage = actionClass.getPackage().getName
    var pkgConfig: PackageConfig.Builder = packageConfigs.get(actionPackage).orNull
    if (pkgConfig == null) {
      var myPkg = configuration.getPackageConfig(actionPackage)
      pkgConfig = if (null != myPkg) new PackageConfig.Builder(myPkg) else new PackageConfig.Builder(actionPackage).namespace(action.namespace).addParent(parentPkg)
      packageConfigs.put(actionPackage, pkgConfig)
    }
    pkgConfig
  }

  /**
   * Determine all the index handling actions and results based on this logic:
   *
   * - Loop over all the namespaces such as /foo and see if it has an action named index
   * - If an action doesn't exists in the parent namespace of the same name, create an action in the parent namespace of the same name as the namespace that points to the index action in the namespace. e.g. /foo -> /foo/index
   * - 3. Create the action in the namespace for empty string if it doesn't exist. e.g. /foo/ the action is "" and the namespace is /foo
   *
   */
  protected def buildIndexActions(packageConfigs: collection.mutable.Map[String, PackageConfig.Builder]): Int = {
    var createCount = 0
    var byNamespace = new collection.mutable.HashMap[String, PackageConfig.Builder]
    for (packageConfig <- packageConfigs.values) {
      byNamespace.put(packageConfig.getNamespace(), packageConfig)
    }
    var namespaces = byNamespace.keySet
    for (namespace <- namespaces if (byNamespace(namespace).build().getAllActionConfigs().get("index") != null)) {
      // First see if the namespace has an index action
      var pkgConfig = byNamespace(namespace)
      var indexActionConfig = pkgConfig.build().getAllActionConfigs().get("index")
      if (pkgConfig.build().getAllActionConfigs().get("") == null) {
        debug(s"Creating index actionconfig for ${indexActionConfig.getClassName}")
        pkgConfig.addActionConfig("", indexActionConfig)
        createCount += 1
      }
    }
    return createCount
  }

  def needsReload(): Boolean = devMode == "true"

}

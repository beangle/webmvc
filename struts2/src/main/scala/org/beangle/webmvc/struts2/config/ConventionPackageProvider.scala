package org.beangle.webmvc.struts2.config

import java.lang.reflect.{ Method, Modifier }

import scala.collection.JavaConversions.seqAsJavaList

import org.beangle.commons.inject.ContainerAware
import org.beangle.commons.lang.{ Objects, Strings }
import org.beangle.commons.lang.time.Stopwatch
import org.beangle.commons.logging.Logging
import org.beangle.commons.text.i18n.spi.TextBundleRegistry
import org.beangle.webmvc.annotation.{ action, noaction, result, results }
import org.beangle.webmvc.route.{ Action, ActionFinder, ContainerActionFinder, Profile, RouteService }

import org.apache.struts2.StrutsConstants

import com.opensymphony.xwork2.{ ActionContext, ObjectFactory }
import com.opensymphony.xwork2.config.{ Configuration, ConfigurationException, PackageProvider }
import com.opensymphony.xwork2.config.entities.{ ActionConfig, PackageConfig, ResultConfig }
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

  @Inject(StrutsConstants.STRUTS_DEVMODE)
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

  private var reloadingClassLoader: ReloadingClassLoader = _

  private var defaultParentPackage: String = "beangle"

  @Inject
  def this(configuration: Configuration, objectFactory: ObjectFactory) {
    this(configuration, new ContainerActionFinder(objectFactory.asInstanceOf[ContainerAware].container))
  }

  protected def initReloadClassLoader() {
    if (isReloadEnabled() && reloadingClassLoader == null) reloadingClassLoader = new ReloadingClassLoader(getClassLoader())
  }

  protected def getClassLoader(): ClassLoader = Thread.currentThread().getContextClassLoader()

  @throws(classOf[ConfigurationException])
  def init(configuration: Configuration) = {}

  @throws(classOf[ConfigurationException])
  def loadPackages() {
    registry.addDefaults(defaultBundleNames.split(","): _*)
    registry.reloadable = java.lang.Boolean.parseBoolean(reloadBundles)
    var watch = new Stopwatch(true)
    routeService.profiles foreach { profile =>
      if (profile.actionScan) actionPackages += profile.actionPattern
    }
    if (actionPackages.isEmpty) { return }

    initReloadClassLoader()
    var packageConfigs = new collection.mutable.HashMap[String, PackageConfig.Builder]()
    var newActions: Int = 0
    var actionTypes = actionFinder.getActions(new ActionFinder.Test(actionSuffix, actionPackages))
    for ((actionClass, value) <- actionTypes) {
      var profile = routeService.getProfile(actionClass.getName())
      var action = routeService.buildAction(actionClass.getName())
      var packageConfig = getPackageConfig(profile, packageConfigs, action, actionClass)
      if (createActionConfig(packageConfig, action, actionClass, value)) newActions += 1
    }
    newActions += buildIndexActions(packageConfigs)
    // Add the new actions to the configuration
    var packageNames = packageConfigs.keySet
    for (packageName <- packageNames) {
      configuration.removePackageConfig(packageName)
      configuration.addPackageConfig(packageName, packageConfigs(packageName).build())
    }
    info(s"Action scan completed,create ${newActions} action in ${watch}.")
  }

  protected def getClassLoaderInterface(): ClassLoaderInterface = {
    if (isReloadEnabled()) return new ClassLoaderInterfaceDelegate(reloadingClassLoader)
    else {
      var classLoaderInterface: ClassLoaderInterface = null
      var ctx = ActionContext.getContext()
      if (ctx != null)
        classLoaderInterface = ctx.get(ClassLoaderInterface.CLASS_LOADER_INTERFACE).asInstanceOf[ClassLoaderInterface]
      Objects.defaultIfNull(classLoaderInterface, new ClassLoaderInterfaceDelegate(getClassLoader()))
    }
  }

  protected def isReloadEnabled(): Boolean = devMode == "true"

  protected def createActionConfig(pkgCfg: PackageConfig.Builder, action: Action, actionClass: Class[_],
    beanName: String): Boolean = {
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
      && Modifier.isPublic(m.getModifiers) && !Modifier.isStatic(m.getModifiers) && null == m.getAnnotation(classOf[noaction])) {
      var name = m.getName().toLowerCase()
      !(name.startsWith("save") || name.startsWith("remove")
        || name.startsWith("export") || name.startsWith("import")
        || name.startsWith("execute") || name.startsWith("tostring") || name.startsWith("get") || Strings.contains(name, "$"))
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
        configs.add(rcb.build())
        annotationResults.add(result.name())
      }
    }
    // load ftl convension results
    var extention = routeService.getProfile(clazz.getName).viewExtension
    if (preloadftl == "true" && extention.endsWith("ftl")) {
      var resultTypeConfig = configuration.getPackageConfig("struts-default")
        .getAllResultTypeConfigs().get("freemarker")
      for (m <- clazz.getMethods) {
        var name = m.getName()
        if (shouldGenerateResult(m)) {
          var buf = new StringBuilder()
          buf.append(routeService.mapView(clazz.getName(), name, name))
          buf.append('.')
          buf.append(extention)
          configs += new ResultConfig.Builder(name, resultTypeConfig.getClassName()).addParam(
            resultTypeConfig.getDefaultResultParam(), buf.toString()).build()
        }
      }
    }
    configs
  }

  protected def getPackageConfig(profile: Profile, packageConfigs: collection.mutable.Map[String, PackageConfig.Builder], action: Action, actionClass: Class[_]): PackageConfig.Builder = {
    // 循环查找父包
    var actionPkg = actionClass.getPackage().getName()
    var parentPkg: PackageConfig = null
    var ifContinue = true
    while (Strings.contains(actionPkg, '.') && ifContinue) {
      parentPkg = configuration.getPackageConfig(actionPkg)
      if (null != parentPkg) {
        ifContinue = false
      } else {
        actionPkg = Strings.substringBeforeLast(actionPkg, ".")
      }
    }
    if (null == parentPkg) {
      actionPkg = defaultParentPackage
      parentPkg = configuration.getPackageConfig(actionPkg)
    }
    if (parentPkg == null) {
      throw new ConfigurationException("Unable to locate parent package ["
        + actionClass.getPackage().getName() + "]")
    }
    var actionPackage = actionClass.getPackage().getName()
    var pkgConfig: PackageConfig.Builder = packageConfigs.get(actionPackage).orNull
    if (pkgConfig == null) {
      var myPkg = configuration.getPackageConfig(actionPackage)
      if (null != myPkg) {
        pkgConfig = new PackageConfig.Builder(myPkg)
      } else {
        pkgConfig = new PackageConfig.Builder(actionPackage).namespace(action.namespace).addParent(
          parentPkg)
        debug(s"Created package config named ${actionPackage} with a namespace ${action.namespace}")
      }
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

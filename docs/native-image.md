# webmvc 的 GraalVM native-image 适配清单（交接文档）

> 2026-08 侦察结果：beangle 生态（commons/data/build/fork）的 native 机制已打通并沉淀文档，
> 本文件是 webmvc 接入的起点清单。方法沿用 data 仓库的 **native-image-agent 证据驱动审计**
> （见 data 仓库 `docs/native-image-reflection-audit.md`），不要手写宽口径反射清单。

> 2026-08-29 适配进展：§2 版本升级、§5.2 WebmvcAotHints、§5.3 锚定文件已完成；
> 本地 native 冒烟（最小 action 应用的 ActionMapping 构建 + DynaMethodInvoker 反射调用）
> 构建并运行通过。剩余：§5.4 agent 证据采集（含 freemarker 面）、§5.5 应用 sample、§5.6 CI 冒烟。

## 1. 现状盘点

- 单模块 sbt 工程 `org.beangle.webmvc` 0.15.2-SNAPSHOT，91 个 scala 源文件。
- 依赖：`beangle-commons 6.3.0-SNAPSHOT`、`beangle-web 0.7.8`、`beangle-template 0.2.10-SNAPSHOT`、freemarker optional；
  测试：slf4j/logback/scalatest/mockito。
- 插件：`sbt-beangle-parent 0.16.1`、`sbt-beangle-build 0.1.4-SNAPSHOT`（sbt 2.0.8）。
- 资源：仅 `src/main/resources/beangle.xml`（cdi 模块 + mvc profile 同文件）。
- 已内嵌 `META-INF/native-image/beangle`（reflect/resource-config）与 `META-INF/beangle/beanmeta.idx`。

## 2. 前置：版本升级（阻塞，先做）

| 构件 | 现版本 | 目标 | 原因 |
|---|---|---|---|
| `beangle-commons` | 6.2.1 | 6.3.0-SNAPSHOT | AOT 机制主体：`AotHintRegistrar` 自注册、枚举伴生自动注册、`LogbackAotHints`/`MetaAotHints`、`BeanInfos.get` 静态化路径 |
| `sbt-beangle-build` | 0.1.3 | 0.1.4-SNAPSHOT | Aot/Meta/Proxy 插件自动启用 + 锚定文件 + 生成器 retry |
| `sbt-beangle-parent` | 0.16.0 | 0.16.1 | 与 data 一致 |

升级后先跑全量测试回归（当前 9 个测试套件），再开始适配。

## 3. 反射/动态行为热点（按严重度排序）

1. **CDI 容器 bean 实例化**：`beangle.xml` 声明的 `BindModule`（`DefaultModule`/`ViewModule`/`DevModule` +
   应用模块）经 commons cdi 实例化，`bind` 注册的 bean 类按名 `newInstance`——
   **模块类与全部 bound 类需要注册构造器**。
2. **Action 扫描与映射**：`DefaultConfigurator.build` + `ActionMappingBuilder`——对 action 类做
   `getMethods`/`getDeclaredMethods` + `getAnnotation`（`mapping/action/cache/response/ignore/views/param/
   cookie/header/body/view` 等）→ **action 类（allPublicMethods + allPublicConstructors）+ webmvc 注解类**。
3. **方法调用**：`DynaMethodInvoker` 的 `method.invoke(action)` → action 方法必须可反射调用。
4. **参数绑定**：`Params.converter`（commons converters；枚举面已由 commons AOT 处理）+ 方法签名
   `TypeInfo`/`ClassTag`。
5. **描述注解**：commons cdi `Binder` 内 `getAnnotation(classOf[description])` → `description` 注解类。
6. **XML 配置**：`XmlProfileProvider` 读 `classpath*:beangle.xml`（commons `XmlConfigs`/`Element`/`Node`）→
   `beangle.xml` 资源注册 + commons xml 解析反射面。
7. **模板（最大不确定项）**：beangle-template freemarker 侧 65 处反射命中（`TagModel.newInstance`、
   `SimpleMethodModel` 的 `Array.newInstance`、freemarker 内部反射）→ freemarker tag/指令类注册 + `*.ftl` 资源。
8. **静态资源**：`StaticResourceRouteProvider`/`StaticFactory`（classpath 资源查找）。

## 4. 资源与 SPI

- `beangle.xml`（`classpath*:`，XmlProfileProvider）；
- i18n message bundle（`*.properties` / `.*\.zh_CN`，commons `Messages`）；
- freemarker 模板（`*.ftl`）与 tag 库资源；
- 静态资源目录（应用面）；
- `META-INF/services/.*`（如有 SPI 文件）。

## 5. 适配步骤（按序）

1. 升级依赖/插件（§2），回归测试。**✅ 已完成**：commons 6.3.0-SNAPSHOT 的 `BeanInfo` API 变化
   （`methods` 移除，改 `meta`+`properties`）导致 `ActionMapping.scala` 两处编译错误，已改为
   `clazz.getMethods` 分组并保留旧过滤语义；commons `BindModule` 采用 data `MappingModule` 同款
   **buildTime 开关**（`registering()` 置位，宏展开按 `buildTime` 分支：构建期只注册 BeanMeta 并
   返回空 BatchBinder，链式 `.onMissing`/`.property`/实例绑定安全跳过），bind 调用方零改动；
   测试 7 套件全绿。
2. 库侧 `AotHintRegistrar` 子类（如 `org.beangle.webmvc.aot.WebmvcAotHints`）：
   - registerType：webmvc 注解族、`BindModule` 族（`DefaultModule`/`ViewModule`/`DevModule`）、
     cdi 容器/分派/模板解析器等被按名实例化的类、`description` 注解；
   - registerPattern：`beangle\.xml`、`.*\.ftl`、`.*\.zh_CN`、`META-INF/services/.*`（按证据取舍）。
   **✅ 已完成**：`WebmvcAotHints` 注册 11 个注解；`description`/`beangle.xml`/`META-INF/services`/
   `*.zh_CN`/mime 类型表归 commons `BuiltinAotHints`，`*.ftl` 归 template `TemplateAotHints`
   （见 §8 职责边界）；`BindModule` 族与绑定类由
   `MetaPlugin`/`AotPlugin` 依 `beangle.xml` 自动注册（已验证生成物）。
3. 锚定文件 `src/main/resources/META-INF/beangle/aot-registrars.txt` 登记该 registrar；
   `MetaPlugin` 依据 `beangle.xml` 的 cdi 模块自动生成 `beanmeta.idx`。
   **✅ 已完成**：`META-INF/beangle/beanmeta.idx` 由 `DefaultModule`/`ViewModule`/`DevModule` 生成并随 jar 内嵌。
4. `native-image-agent` 采集（以 webmvc 测试 + 一个最小 servlet 应用跑一遍），对照注册面收紧
   （无证据的删除、按证据缩小范围，方法与 data 一致）。**⏳ 待做**：库侧已按文档证据注册
   （`*.ftl`/`*.zh_CN`/`META-INF/services` 已按库归属注册）；最小本地冒烟已通过，agent 全量审计未跑。
5. 建应用 sample（参照 `beangle/sample`）：`build.sbt` + `build-native.sh` +
   `resource-config.json`（仅应用面无库侧归属的项）。**⏳ 待做**（独立仓库 beangle/sample，
   同 data 模式；本地验证用临时工程 `/tmp/webmvc-native-smoke` 已完成构建+运行）。
6. CI 冒烟（native 构建 + 运行）。**⏳ 待做**。

## 6. 职责边界

- **webmvc 库侧**：注册库自身反射面与资源（§3/§4），随 `beangle-webmvc.jar` 内嵌
  `META-INF/native-image` 发布。
- **应用侧**：action 类属于应用面——应用定义自己的 `AotHintRegistrar` 或由 `beangle.xml`
  jpa/cdi 扫描带出（机制同 data 的 `sample`）。
- **第三方**：freemarker/servlet 容器的内部反射由对应库/使用方承担，webmvc 不背；
  模板若反射面过大，优先用 agent 证据决定是否按需收紧。

## 7. 新任务开始时先核对

- commons `BindModule` 与 `MetaRegistrar` 的关系：`MetaPlugin` 能否直接把 cdi 模块当
  `MetaRegistrar` 消费（data 侧结论是"cdi 声明的模块也是 metagenerator 的对象"，需在 6.3.0-SNAPSHOT 上确认）；
  **✅ 已确认**：`MetaPlugin` 直接以 `beangle.xml` 的 `<cdi><module>` 为清单调用 `MetaGenerator`，
  生成 `beanmeta.idx` 成功（`DefaultModule`/`ViewModule`/`DevModule` 均被消费）。
- beangle-web `BootstrapInitializer` 的容器实现：bean 实例化走 `Reflections.getInstance` 的精确位置
  （决定注册面是"构造器"还是"方法+构造器"）；
  **✅ 已确认**：`BootstrapInitializer.onStartup` 对 `<web><initializer>` 类走
  `Reflections.newInstance`（构造器路径）；`AotHintGenerator` 对 registrar 自注册
  `allDeclaredConstructors`（object 补 `allPublicFields`）已覆盖该面。initializer 类属应用面，由应用注册。
- freemarker 版本及其反射面清单（用 agent 采集，别照搬网上配置）。

## 8. 参考

- data 仓库 `docs/native-image.md`（总纲，§6 使用方清单）
- data 仓库 `docs/native-image-reflection-audit.md`（agent 审计方法）
- commons 仓库 `docs/aot-usage.md`（AotHintRegistrar/MetaRegistrar 写法）
- `beangle/sample` 工程（native 构建模板：build.sbt + build-native.sh + resource-config）

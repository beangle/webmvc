# Action URL 与 Profile 设计说明

本文档说明 Beangle WebMVC 如何根据 `Profile` 配置，为 Action 类生成 URL 前缀（`actionName` / `namespace`），以及不同 `urlStyle` 下的差异。

实现入口：

- `ActionNameBuilder.build` — 根据 Profile 与 `@action` 注解生成 `(actionName, namespace)`
- `DefaultActionMappingBuilder.build` — 在 `actionName` 上拼接方法名或 `@mapping`，得到最终路由 URL

---

## 1. Profile 中与 URL 相关的配置

| 配置项 | XML 属性 | 默认值 | 说明 |
|--------|----------|--------|------|
| `pattern` | `@package` | `*` | Action 类的包名匹配模式，支持 `*` 通配 |
| `actionSuffix` | `<action suffix="...">` | `Action` | 类名后缀，参与匹配与截断 |
| `urlPath` | `<url path="...">` | `/` | URL 前缀，**必须以 `/` 结尾**（XML 配置会自动补 `/`） |
| `urlStyle` | `<url style="...">` | `seo` | URL 命名风格，见下文四种取值 |
| `urlSuffix` | `<url suffix="...">` | 空 | 预留字段，当前 `ActionNameBuilder` 未使用 |

`urlStyle` 合法取值（定义于 `Profile` 伴生对象）：

| 常量 | 配置值 | 含义 |
|------|--------|------|
| `SHORT_URI` | `short` | 仅用类简单名（去掉 `actionSuffix`） |
| `SIMPLE_URI` | `simple` | 使用 `getMatched` 原始路径，保留 camelCase |
| `SEO_URI` | `seo` | 对 `getMatched` 结果做 `unCamel`（kebab-case） |
| `PLUR_SEO_URI` | `plur-seo` | 在 SEO 基础上，对**最后一段**做英文复数 |

---

## 2. 生成流程概览

```
Action 类 + Profile
        │
        ▼
  profile.urlPath          （例如 "/" 或 "/app/"）
        +
  无 @action：按 urlStyle 处理 getMatched(className)
  有 @action：按 urlStyle 与注解 value 组合
        │
        ▼
  (actionName, namespace)  ──►  ActionMapping
        │
        ▼
  方法 URL = actionName + "/" + methodName
           或 actionName + "/" + @mapping("...")
```

约定（见 `ActionNameBuilder` 注释）：

- `namespace` 以 `/` 开头，除根路径 `/` 外不以 `/` 结尾
- `actionName` 包含 namespace 语义上的完整 Action 路径，同样不以 `/` 结尾（根 Action 为 `/`）

---

## 3. `getMatched`：从类名到路径片段

在调用 `ActionNameBuilder` 之前，须先对类名执行 `profile.matches(className)`。匹配成功后，`getMatched(className)` 返回**不含前导 `/` 的路径字符串**。

算法要点（`Profile.matches` / `getInfix`）：

1. 类全名须以 `actionSuffix` 结尾（默认 `Action`）。
2. `pattern` 按 `*` 拆成多段，在全类名中顺序查找；`*` 之间的包名片段进入 `reserved`。
3. 去掉前后缀后，将类简单名（首字母小写）与 `reserved`、中间包名片段拼合，**把 `.` 替换为 `/`**。

示例（`pattern = org.beangle.webmvc.test`，`actionSuffix = Action`）：

| 类全名 | getMatched |
|--------|------------|
| `org.beangle.webmvc.test.ShowcaseAction` | `showcase` |
| `org.beangle.webmvc.test.IndexAction` | `index` |

当 `pattern` 含通配且类位于子包时，`getMatched` 会带上子包路径（如 `admin/userProfile`）。具体结果依赖 `pattern` 与包结构是否对齐；**生产环境应使用明确的 `pattern`，避免单独使用 `*`**。

---

## 4. 无 `@action` 注解时：四种 `urlStyle`

设 `urlPath = /`，`pattern = org.beangle.webmvc.test`，类为 `ShowcaseAction`（`getMatched = showcase`）。

| urlStyle | 计算方式 | actionName | namespace |
|----------|----------|------------|-----------|
| `short` | `uncapitalize(简单类名去掉 Action 后缀)` | `/showcase` | （空） |
| `simple` | `urlPath + getMatched` | `/showcase` | （空） |
| `seo` | `urlPath + unCamel(getMatched)` | `/showcase` | （空） |
| `plur-seo` | 对 getMatched 最后一段 `EnNounPluralizer.pluralize` 后再 `unCamel` | `/showcases` | （空） |

说明：

- **flat 包**（类直接在 `pattern` 对应包下）时，`short` / `simple` / `seo` 对单词类名结果相同；`plur-seo` 会复数化最后一段。
- **子路径**（`getMatched` 含 `/`，如 `admin/userProfile`）时差异明显：

| urlStyle | actionName（示意） |
|----------|-------------------|
| `short` | `/userProfile`（忽略包路径，仅简单类名） |
| `simple` | `/admin/userProfile` |
| `seo` | `/admin/user-profile` |
| `plur-seo` | `/admin/user-profiles` |

`plur-seo` 对含 `/` 的 matched 名：前缀段只做 `unCamel`，**最后一段**先复数再 `unCamel`：

```scala
// Profile.PLUR_SEO_URI 分支（概念）
prefix/unCamel(last)  →  prefix/unCamel(pluralize(last))
```

`namespace` 在无 `@action` 时取 `actionName` 最后一个 `/` 之前的部分；若 actionName 为 `/showcase`，则 namespace 为空字符串。

---

## 5. 有 `@action` 注解时

类上的 `@action("...")` 会覆盖或补充默认类名推导。

### 5.1 通用规则

- 注解 value **不以 `/` 开头**：拼在 `urlPath` 之后（`seo` / `plur-seo` 另有分支，见下）。
- 注解 value **以 `/` 开头**：去掉首 `/` 后拼在 `urlPath` 之后（`urlPath` 为 `/` 时即绝对路径段）。

### 5.2 `short` / `simple`

直接将注解 value 接到 `urlPath` 后，**不使用** `getMatched` 的类名路径。

| 类 | @action | actionName |
|----|---------|------------|
| `IndexAction` | `""` | `/` |
| `SettingsAction` | `"settings"` | `/settings` |
| `AdminUsersAction` | `"/admin/users"` | `/admin/users` |
| `HrEmployeesAction` | `"hr/employees"` | `/hr/employees` |

此两种 style 下行为一致。

### 5.3 `seo` / `plur-seo`

1. `namespace` 初始为 `urlPath`。
2. 若 `getMatched` 中含 `/`（`lastSlashIdx > 0`），则把 matched 的**目录部分**经 `unCamel` 后写入 `nameBuilder` 与 `namespace`。
3. 若注解 value 非空：  
   - matched 有目录部分：`nameBuilder += "/" + value`  
   - 否则：`nameBuilder += value`（value 可自带 `/`，如 `hr/employees`）

| 类 | @action | 典型 actionName |
|----|---------|-----------------|
| `IndexAction` | `""` | `/` |
| `SettingsAction` | `"settings"` | `/settings`（matched 无 `/` 时，**不**使用 matched 的 `index`） |
| `AdminUsersAction` | `"/admin/users"` | `/admin/users` |
| `HrEmployeesAction` | `"hr/employees"` | `/hr/employees` |

---

## 6. 从 actionName 到方法 URL

`DefaultActionMappingBuilder` 在得到 `actionName` 后，为每个 Action 方法生成 URL：

```text
url = actionName + "/" + segment
```

其中 `segment` 为：

- 方法名（如 `string` → `/showcase/string`）
- 或 `@mapping("path/{id}")` 的 value（去掉Leading `/` 后拼接）

示例（`ShowcaseAction`，`urlStyle = seo`，无类级 `@action`）：

| 方法 | 映射 | 最终 URL |
|------|------|----------|
| `string` | （默认方法名） | `/showcase/string` |
| `path` | `@mapping("path/{id}")` | `/showcase/path/{id}` |
| `echofloat` | `@mapping("echofloat/{num}")` | `/showcase/echofloat/{num}` |

默认 HTTP 方法为 `GET`；带 `@mapping(method = "...")` 时可指定其它动词。

---

## 7. 配置示例（beangle.xml）

```xml
<mvc>
  <profile package="com.example.web.action">
    <action suffix="Action" defaultMethod="index"/>
    <url path="/" style="seo"/>
  </profile>
</mvc>
```

- `com.example.web.action.user.UserAction` → actionName `/user`（seo）
- `UserAction` 的 `index` 方法 → `/user/index`

若需 REST 风格复数资源路径，可将 `style` 设为 `plur-seo`：

- `UserAction` → `/users`
- `index` → `/users/index`

---

## 8. 选型建议

| 场景 | 推荐 urlStyle |
|------|----------------|
| 最短 URL、不关心包结构 | `short` |
| 保留 Java camelCase 路径 | `simple` |
| 可读、SEO 友好（默认） | `seo` |
| REST 风格复数资源名 | `plur-seo` |
| 完全自定义路径 | 类上 `@action`（可与任意 style 组合；`short`/`simple` 最直接） |

---

## 9. 相关源码

| 文件 | 职责 |
|------|------|
| `config/ActionNameBuilder.scala` | URL 前缀与 namespace 生成 |
| `config/Profile.scala` | `urlStyle` 常量、`matches` / `getMatched` |
| `config/ProfileProvider.scala` | XML 默认值与 `<url style="...">` 读取 |
| `config/ActionMapping.scala` | 方法级 URL 拼接 |
| `annotation/action.java` | 类级 URL 覆盖 |

---

## 10. 验证用例

仓库内现有测试可作为回归参考：

- `ActionNameBuilderTest` — `@action("")` 的 `IndexAction` → `("/", "/")`
- `DefaultActionMappingBuilderTest` — `ShowcaseAction` + 默认 seo → `/showcase/path/{id}` 等

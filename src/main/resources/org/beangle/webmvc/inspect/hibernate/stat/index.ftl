[#ftl]
[@b.head/]
[#include "../nav.ftl"/]
  [@b.a href="!index"]Reload[/@] |
  [@b.a href="!index?do=${active?string('deactivate','activate')}"]${active?string('Deactivate','Activate')}[/@] | [@b.a href="!index?do=clear"]CLEAR[/@]
  Last update: ${lastUpdate?string("MM-dd HH:mm:ss")}<br/>
  Activation: ${(activation?string("MM-dd HH:mm:ss"))!}<br/>
  Deactivation: ${(deactivation?string("dd.MM.yy HH:mm:ss"))!}<br/>
  Active duration:[#if duration??] ${((duration/1000)?floor)!}s[/#if]
  [#if generalStatistics?size>0]
  <table class="table">
    <tr>
      <th class="c bd1 bg1">Connects:</th>
      <td>${generalStatistics[0]}</td>
      <th class="c bd1 bg1">Flushes</th>
      <td>${generalStatistics[1]}</td>
      <th class="c bd1 bg1">Prepare statements</th>
      <td>${generalStatistics[2]}</td>
    </tr>
    <tr>
      <th class="c bd1 bg1">Close statements</th>
      <td>${generalStatistics[3]}</td>
      <th class="c bd1 bg1">Session opens</th>
      <td>${generalStatistics[5]}</td>
      <th class="c bd1 bg1">Session closes</th>
      <td>${generalStatistics[4]}</td>
    </tr>
    <tr>
      <th class="c bd1 bg1">Total Transactions</th>
      <td>${generalStatistics[6]}</td>
      <th class="c bd1 bg1">Successfull Transactions</th>
      <td>${generalStatistics[7]}</td>
      <th class="c bd1 bg1">Optimistic failures</th>
      <td colspan="3">${generalStatistics[8]}</td>
    </tr>
  </table>
<div class="container">
  <div class="row">
   <div class="col-md-2">
     [@b.navlist class="nav-pills nav-stacked"]
       [@b.navitem href="!entity" active=true target="action_content"]实体类<span class="badge">${statistics.entityNames?size}</span>[/@]
       [@b.navitem href="!collection" target="action_content"]集合<span class="badge">${statistics.collectionRoleNames?size}</span>[/@]
       [@b.navitem href="!cache" target="action_content"]二级缓存<span class="badge">${statistics.secondLevelCacheRegionNames?size}</span>[/@]
       [@b.navitem href="!query" target="action_content"]查询缓存<span class="badge">${statistics.queries?size}</span>[/@]
     [/@]
   </div>
   [@b.div id="action_content" class="col-md-10" href="!entity"/]
  </div>
</div>
[/#if]
[@b.foot/]

[#ftl]
[@b.head/]
  <table class="table table-striped">
    <thead>
    <tr>
      <th width="30%">缓存区域</th>
      <th width="10%">放置</th>
      <th width="10%">命中</th>
      <th width="10%">未命中</th>
      <th width="10%">内存对象数量</th>
      <th width="10%">磁盘对象数量</th>
      <th width="10%">内存大小</th>
      <th width="10%">命中率</th>
    </tr>
    </thead>
    <tbody>
    [#list statistics.secondLevelCacheRegionNames?sort as cacheRegion]
    <tr>
      [#assign regionStat=statistics.getSecondLevelCacheStatistics(cacheRegion)/]
      <td>${cacheRegion}</td>
      <td>${regionStat.putCount}</td>
      <td>${regionStat.hitCount}</td>
      <td>${regionStat.missCount}</td>
      <td>${regionStat.elementCountInMemory}</td>
      <td>${regionStat.elementCountOnDisk}</td>
      <td>${regionStat.sizeInMemory}</td>
      <td>${regionStat.hitCount/(0.01+regionStat.hitCount+regionStat.missCount)}</td>
    </tr>
    [/#list]
    </tbody>
  </table>
[@b.foot/]

[#ftl]
[@b.head/]
  <table class="table table-striped">
    <thead>
    <tr>
      <th width="5%">Index</th>
      <th width="50%">HQL Query</th>
      <th width="5%">Calls</th>
      <th width="5%">Total rowcount</th>
      <th width="5%">Max dur.</th>
      <th width="5%">Min dur.</th>
      <th width="5%">Avg dur.</th>
      <th width="5%">Total dur.</th>
      <th width="5%">Cache hits</th>
      <th width="5%">Cache miss</th>
      <th width="5%">Cache put</th>
    </tr>
    </thead>
    <tbody>
    [#list statistics.queries?sort as query]
    <tr>
      [#assign queryStats=statistics.getQueryStatistics(query)/]
      <td>${query_index+1}</td>
      <td>${query}</td>
      <td>${queryStats.executionCount}</td>
      <td>${queryStats.executionRowCount}</td>
      <td>${queryStats.executionMaxTime}</td>
      <td>${queryStats.executionMinTime}</td>
      <td>${queryStats.executionAvgTime}</td>
      <td>
        ${queryStats.executionAvgTime*queryStats.executionCount}
      </td>
      <td>${queryStats.cacheHitCount}</td>
      <td>${queryStats.cacheMissCount}</td>
      <td>${queryStats.cachePutCount}</td>
    </tr>
    [/#list]
    </tbody>
  </table>
  <br/>
[@b.foot/]

[#ftl]
[@b.head/]
  <table class="table table-striped">
    <thead>
    <tr>
      <th width="5%">Index</th>
      <th width="53%">Entity</th>
      <th width="7%">Loads</th>
      <th width="7%">Fetches</th>
      <th width="7%">Inserts</th>
      <th width="7%">Updates</th>
      <th width="7%">Deletes</th>
      <th width="7%">Optimistic failures</th>
    </tr>
    </thead>
    <tbody>
    [#list statistics.entityNames?sort as entity]
    [#assign entityStats=statistics.getEntityStatistics(entity)/]
    <tr>
      <td>${entity_index+1}</td>
      <td>${entity}</td>
      <td>${entityStats.loadCount}</td>
      <td>${entityStats.fetchCount}</td>
      <td>${entityStats.insertCount}</td>
      <td>${entityStats.updateCount}</td>
      <td>${entityStats.deleteCount}</td>
      <td>${entityStats.optimisticFailureCount}</td>
    </tr>
    [/#list]
    </tbody>
  </table>
  <br/>
[@b.foot/]

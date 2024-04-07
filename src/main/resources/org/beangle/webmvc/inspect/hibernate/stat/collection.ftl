[#ftl]
[@b.head/]
  <table class="table table-striped">
    <thead>
     <tr>
      <th width="5%">Index</th>
      <th width="53%">Role</th>
      <th width="8%">Loads</th>
      <th width="8%">Fetches</th>
      <th width="8%">Updates</th>
      <th width="8%">Recreates</th>
      <th width="8%">Remove</th>
     </tr>
    </thead>
    <tbody>
    [#list statistics.collectionRoleNames?sort as collection]
    [#assign collectionStats=statistics.getCollectionStatistics(collection)/]
    <tr>
      <td>${collection_index+1}</td>
      <td>${collection}</td>
      <td>${collectionStats.loadCount}</td>
      <td>${collectionStats.fetchCount}</td>
      <td>${collectionStats.updateCount}</td>
      <td>${collectionStats.recreateCount}</td>
      <td>${collectionStats.removeCount}</td>
    </tr>
    [/#list]
    </tbody>
  </table>
  <br/>
[@b.foot/]

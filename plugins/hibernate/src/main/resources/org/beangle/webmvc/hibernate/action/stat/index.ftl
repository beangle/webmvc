[#ftl]
[@b.head/]
[#include "nav.ftl"/]
  <p/>
  [@b.a href="!index"]Reload[/@] |
  [@b.a href="!index?do=${active?string('deactivate','activate')}"]${active?string('Deactivate','Activate')}[/@] | [@b.a href="!index?do=clear"]CLEAR[/@]
  <p/>
  Last update: ${lastUpdate?string("MM-dd HH:mm:ss")}<br/>
  Activation: ${(activation?string("MM-dd HH:mm:ss"))!}<br/>
  Deactivation: ${(deactivation?string("dd.MM.yy HH:mm:ss"))!}<br/>
  Active duration:[#if duration??] ${((duration/1000)?floor)!}s[/#if]
  <p/>
  [#if generalStatistics?size>0]
  <table>
    <tr>
      <th class="c bd1 bg1">Connects</th>
      <td>${generalStatistics[0]}</td>
    </tr>
    <tr>
      <th class="c bd1 bg1">Flushes</th>
      <td>${generalStatistics[1]}</td>
    </tr>
    <tr>
      <th class="c bd1 bg1">Prepare statements</th>
      <td>${generalStatistics[2]}</td>
    </tr>
    <tr>
      <th class="c bd1 bg1">Close statements</th>
      <td>${generalStatistics[3]}</td>
    </tr>
    <tr>
      <th class="c bd1 bg1">Session opens</th>
      <td>${generalStatistics[5]}</td>
    </tr>
    <tr>
      <th class="c bd1 bg1">Session closes</th>
      <td>${generalStatistics[4]}</td>
    </tr>
    <tr>
      <th class="c bd1 bg1">Total Transactions</th>
      <td>${generalStatistics[6]}</td>
    </tr>
    <tr>
      <th class="c bd1 bg1">Successfull Transactions</th>
      <td>${generalStatistics[7]}</td>
    </tr>
      <th class="c bd1 bg1">Optimistic failures</th>
      <td>${generalStatistics[8]}</td>
    </tr>
  </table>
[/#if]
[@b.foot/]
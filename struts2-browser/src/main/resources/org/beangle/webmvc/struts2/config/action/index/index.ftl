[#ftl]
[@b.head/]
<style>
.bs-sidebar .nav > .active > ul {
    display: block;
    margin-bottom: 8px;
}
</style>
<nav class="navbar navbar-default" role="navigation">
   <div class="navbar-header">
      <a class="navbar-brand" href="#">Struts Configuration Browser</a>
   </div>
   <div>
     <ul class="nav navbar-nav">
         <li>[@b.a href="!index"  class="" target="content"]Actions[/@]</li>
         <li>[@b.a href="!consts" target="content"]Constants[/@]</li>
         <li>[@b.a href="!beans" target="content"]Beans[/@]</li>
         <li>[@b.a href="!jars"  target="content"]Jars[/@]</li>
      </ul>
   </div>
</nav>
<script>
$('.nav li a').click(function(e) {
  var $this = $(this);
  if (!$this.hasClass('active')) {
    $this.addClass('active');
  }
  e.preventDefault();
});
</script>
[@b.div id="content" href="!actions"/]
[@b.foot/]
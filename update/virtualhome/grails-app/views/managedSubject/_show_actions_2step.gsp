<g:if test="${managedSubjectInstance.isUsingTwoStepLogin()}">
  <li>
    <a href="#" onclick="$(this).next('form').submit();"><g:message encodeAs='HTML' code="views.aaf.vhr.managedsubject.show.twostep.reset"/></a>
    <g:form action="resettwosteplogin" method="post">
      <g:hiddenField name="id" value="${managedSubjectInstance.id}" />
    </g:form>
  <li>
</g:if>
<li>
  <g:if test="${!managedSubjectInstance.totpForce}">
    <a href="#" onclick="$(this).next('form').submit();"><g:message encodeAs='HTML' code="views.aaf.vhr.managedsubject.show.twostep.enforce"/></a>
    <g:form action="enforcetwosteplogin" method="post">
      <g:hiddenField name="id" value="${managedSubjectInstance.id}" />
      <g:hiddenField name="enforce" value="true" />
    </g:form>
  </g:if>
  <g:else>
    <a href="#" onclick="$(this).next('form').submit();"><g:message encodeAs='HTML' code="views.aaf.vhr.managedsubject.show.twostep.unenforce"/></a>
    <g:form action="enforcetwosteplogin" method="post">
      <g:hiddenField name="id" value="${managedSubjectInstance.id}" />
      <g:hiddenField name="enforce" value="false" />
    </g:form>
  </g:else>
</li>

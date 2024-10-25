Hello ${managedSubject.cn.encodeAsHTML()},<br><br>

You are attempting to recover a lost password.<br><br>

However, you do not have a mobile number configured for this account.<br>

A mobile number is required for SMS confirmation of your identity.<br>

This requires contacting one of the administators of your account.<br><br>

<g:if test="${groupAdminRole.subjects?.size() > 0}">
  <h5>Primary Administrators</h5>
  <ul>
    <g:each in="${groupAdminRole.subjects?.sort{it.cn}}" var="subject">
      <li><g:fieldValue bean="${subject}" field="cn"/> - <a href="mailto:${subject.email}"><g:fieldValue bean="${subject}" field="email"/></a></li>
    </g:each>
  </ul>
</g:if>

<g:if test="${organizationAdminRole.subjects?.size() > 0}">
<h5>Secondary Administrators</h5>
  <ul>
    <g:each in="${organizationAdminRole.subjects?.sort{it.cn}}" var="subject">
      <li><g:fieldValue bean="${subject}" field="cn"/> - <a href="mailto:${subject.email}"><g:fieldValue bean="${subject}" field="email"/></a></li>
    </g:each>
  </ul>
</g:if>

In no adminstrators are available for this account, please email <a href="mailto:tuakiri@reannz.co.nz">Tuakiri support at tuakiri@reannz.co.nz</a>
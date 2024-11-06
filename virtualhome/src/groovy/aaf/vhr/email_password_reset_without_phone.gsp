Hello ${managedSubject.cn.encodeAsHTML()},<br><br>

You are attempting to recover a lost password.<br><br>

Please note that you do not have a mobile number configured for this account.<br>

A mobile number is required for SMS confirmation of your identity.<br>

<strong>Please contact one of your listed administrators to obtain the required SMS code!</strong><br><br>

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

Once you have obtained the SMS code from your administrator, <a href="${emailURL}">click the link</a> to proceed with your password reset.<br><br>

<g:if test="${groupAdminRole.subjects?.size() > 0 || organizationAdminRole.subjects?.size() > 0}">
If you are unable to contact the above administrators, please email <a href="mailto:tuakiri@reannz.co.nz">Tuakiri support at tuakiri@reannz.co.nz</a>
</g:if>
<g:else>
As your organisation and group does not have any administrators registered, please email <a href="mailto:tuakiri@reannz.co.nz">Tuakiri support at tuakiri@reannz.co.nz</a>
</g:else>


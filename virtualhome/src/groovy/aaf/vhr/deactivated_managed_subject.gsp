Hello ${managedSubject.cn.encodeAsHTML()},<br><br>

Your AAF Virtual Home account has been temporarily deactivated. This is an automatic security measure when the wrong password is entered for your account a number of times.<br><br>

If you believe that you did not enter the incorrect password for your account then <strong>please advise your support contact, as shown below, when getting assistance</strong>.<br><br>

<h4>Correcting the problem</h4>
Your username for this account is: <em>${managedSubject.login.encodeAsHTML()}</em><br><br>

To have your account checked and re-enabled please contact one of the administrators shown below.<br><br>

<h5>Primary Administrators</h5>
<g:if test="${groupRole.subjects?.size() > 0}">
  <ul>
    <g:each in="${groupRole.subjects?.sort{it.cn}}" var="subject">
      <li><g:fieldValue bean="${subject}" field="cn"/> - <a href="mailto:${subject.email}"><g:fieldValue bean="${subject}" field="email"/></a></li>
    </g:each>
  </ul>
</g:if>
<g:else>
  Unfortunately there are no primary administrators available for your account.
</g:else>
<br><br>

<h5>Secondary Administrators</h5>
<g:if test="${organizationRole.subjects?.size() > 0}">
  <ul>
    <g:each in="${organizationRole.subjects?.sort{it.cn}}" var="subject">
      <li><g:fieldValue bean="${subject}" field="cn"/> - <a href="mailto:${subject.email}"><g:fieldValue bean="${subject}" field="email"/></a></li>
    </g:each>
  </ul>
</g:if>
<g:else>
  Unfortunately there are no secondary administrators available for your account.
</g:else>
<br>

<h5>Can't contact an administrator?</h5>
If you're unable to contact an account administator as shown above (or if there are no primary or secondary administrators assigned to your account) please get in touch with <a href="mailto:support@aaf.edu.au">AAF support at support@aaf.edu.au</a>. Please provide your username and details of who you've attempted to contact above as part of the email so we can assist you further.


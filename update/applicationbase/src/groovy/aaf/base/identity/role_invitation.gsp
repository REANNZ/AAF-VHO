Hello,<br><br>

Your colleague ${invitee.cn.encodeAsHTML()} (${invitee.email.encodeAsHTML()}) has granted you administrative
rights for managing <strong>${targetName.encodeAsHTML()}</strong> within the <g:message encodeAs='HTML' code="branding.application.name"/>.<br><br>

<h5>Your action is now required</h5>

To finish setting your administrative rights <a href="${g.createLink(controller:'role', action:'finalization', params:['inviteCode':invitation.inviteCode], absolute:true)}">please access your unique finalisation page by clicking this link</a>. You may need to authenticate via your Tuakiri
Identity Provider to complete this process.

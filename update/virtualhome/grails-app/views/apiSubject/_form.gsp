
<g:if test="${apiSubject}">
  <g:hiddenField name="id" value="${apiSubject.id}" />
  <g:hiddenField name="version" value="${apiSubject.version}" />
</g:if>

<div class="control-group">
  <label class="control-label" for="principal"><g:message encodeAs='HTML' code="label.token" /></label>
  <div class="controls">
    <g:textField name="principal" value="${apiSubject?.principal}" class="span4 required" />
    <div class="help-block">
      <p>Must be random. Will be sent over the wire by remote web service clients.</p>
      <p>Generation: <code>tr -dc '[[:alnum:]' < /dev/urandom | head -c16 ;echo</code></p>
    </div>
  </div>
</div>

<div class="control-group">
  <label class="control-label" for="secret"><g:message encodeAs='HTML' code="label.secret" /></label>
  <div class="controls">
    <g:textField name="apiKey" value="${apiSubject?.apiKey}" class="span4 required" />
    <div class="help-block">
      <p>Must be random and securely stored by remote client. This value should never be publicly disclosed and is used on client side for signature generation.</p>
      <p>Generation: <code>tr -dc '[[:alnum:][:punct:]]' < /dev/urandom | head -c48 ;echo</code></p>
    </div>
  </div>
</div>

<div class="control-group">
  <label class="control-label" for="email"><g:message encodeAs='HTML' code="label.email" /></label>
  <div class="controls">
    <g:textField name="email" value="${apiSubject?.email}" class="span4 required email" />
  </div>
</div>

<div class="control-group">
  <label class="control-label" for="description"><g:message encodeAs='HTML' code="label.description" /></label>
  <div class="controls">
    <g:textArea name="description" value="${apiSubject?.description}" class="span4 required" />
  </div>
</div>

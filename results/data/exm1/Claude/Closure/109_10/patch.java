private Node parseContextTypeExpression(JsDocToken token) {
  if (!match(JsDocToken.COLON)) {
    return reportTypeSyntaxWarning("msg.jsdoc.missing.colon");
  }
  next();
  return parseTypeName(token);
}
private Node parseContextTypeExpression(JsDocToken token) {
    if (token == JsDocToken.THIS) {
        next();
        skipEOLs();
        token = next();
        if (token != JsDocToken.COLON) {
            return reportTypeSyntaxWarning("msg.jsdoc.missing.colon");
        }
        next();
        skipEOLs();
    }
    return parseTypeName(token);
}
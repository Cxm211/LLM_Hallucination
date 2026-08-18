private Node parseContextTypeExpression(JsDocToken token) {
    if (token == JsDocToken.LEFT_PAREN) {
        skipEOLs();
        token = next();
    }
    return parseTypeName(token);
}
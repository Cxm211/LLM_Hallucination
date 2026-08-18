private Node parseContextTypeExpression(JsDocToken token) {
    if (token == JsDocToken.THIS) {
        skipEOLs();
        token = next();
        if (token == JsDocToken.COLON) {
            skipEOLs();
            token = next();
            return parseTypeName(token);
        }
    }
    return parseTypeName(token);
}
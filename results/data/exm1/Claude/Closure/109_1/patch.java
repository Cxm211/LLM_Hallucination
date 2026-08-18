private Node parseContextTypeExpression(JsDocToken token) {
    if (token == JsDocToken.THIS) {
        next();
        skipEOLs();
        if (match(JsDocToken.COLON)) {
            next();
            skipEOLs();
        }
        return parseTypeName(next());
    }
    return parseTypeName(token);
}
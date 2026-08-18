private Node parseContextTypeExpression(JsDocToken token) {
    if (token != JsDocToken.LEFT_CURLY) {
        return reportTypeSyntaxWarning("msg.jsdoc.missing.lc");
    }
    skipEOLs();
    Node typeNode = parseTypeName(next());
    if (typeNode != null) {
        skipEOLs();
        if (!match(JsDocToken.RIGHT_CURLY)) {
            return reportTypeSyntaxWarning("msg.jsdoc.missing.rc");
        }
        next();
    }
    return typeNode;
}
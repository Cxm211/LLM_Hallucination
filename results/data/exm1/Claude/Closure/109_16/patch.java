private Node parseContextTypeExpression(JsDocToken token) {
    if (token == JsDocToken.LEFT_CURLY) {
        skipEOLs();
        Node typeNode = parseTypeExpression(next());
        if (typeNode != null) {
            skipEOLs();
            if (!match(JsDocToken.RIGHT_CURLY)) {
                return reportTypeSyntaxWarning("msg.jsdoc.missing.rc");
            }
            next();
        }
        return typeNode;
    } else {
        return parseTypeName(token);
    }
}
private Node parseContextTypeExpression(JsDocToken token) {
    Node typeNode = parseTypeName(token);
    if (typeNode != null) {
        JsDocToken next = next();
        if (next == JsDocToken.EQUALS) {
            boolean isVarArgs = false;
            if (typeNode.getType() == Token.ELLIPSIS) {
                isVarArgs = true;
            }
            if (isVarArgs) {
                return reportTypeSyntaxWarning("msg.jsdoc.variadic.optional");
            }
        }
        restoreLookAhead(next);
    }
    return typeNode;
}
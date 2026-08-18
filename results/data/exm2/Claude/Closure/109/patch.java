private Node parseContextTypeExpression(JsDocToken token) {
    if (token == JsDocToken.QMARK) {
        return newNode(Token.QMARK);
    } else if (token == JsDocToken.STAR) {
        return newNode(Token.STAR);
    }
    return parseTypeName(token);
}
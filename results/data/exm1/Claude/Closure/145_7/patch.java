private boolean isOneExactlyFunctionOrDo(Node n) {
    if (n.getChildCount() != 1) {
        return false;
    }
    Node child = n.getFirstChild();
    return (child.getType() == Token.FUNCTION || child.getType() == Token.DO);
}
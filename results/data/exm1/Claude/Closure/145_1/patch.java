private boolean isOneExactlyFunctionOrDo(Node n) {
    if (n.getType() == Token.FUNCTION || n.getType() == Token.DO) {
        return true;
    }
    if (n.getType() == Token.BLOCK) {
        if (n.hasOneChild()) {
            return isOneExactlyFunctionOrDo(n.getFirstChild());
        }
    }
    return false;
}
private boolean isOneExactlyFunctionOrDo(Node n) {
    if (n.getType() == Token.FUNCTION || n.getType() == Token.DO) {
        return true;
    }
    
    if (n.getType() == Token.BLOCK) {
        if (n.getChildCount() == 1) {
            Node child = n.getFirstChild();
            return (child.getType() == Token.FUNCTION || child.getType() == Token.DO);
        }
    }
    
    return false;
}
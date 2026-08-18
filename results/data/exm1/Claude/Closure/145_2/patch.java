private boolean isOneExactlyFunctionOrDo(Node n) {
    if (n.getType() == Token.FUNCTION || n.getType() == Token.DO) {
        return true;
    }
    
    if (n.getType() == Token.BLOCK) {
        Node child = n.getFirstChild();
        if (child != null && child.getNext() == null) {
            return (child.getType() == Token.FUNCTION || child.getType() == Token.DO);
        }
    }
    
    return false;
}
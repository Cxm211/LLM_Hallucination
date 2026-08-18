private boolean isOneExactlyFunctionOrDo(Node n) {
    // For labels with block children, we need to ensure that a
    // labeled FUNCTION or DO isn't generated when extraneous BLOCKs 
    // are skipped.
    if (n.getType() == Token.FUNCTION || n.getType() == Token.DO) {
        return true;
    }
    // Check if it's a block with exactly one child that is FUNCTION or DO
    if (n.getType() == Token.BLOCK) {
        if (n.hasOneChild()) {
            return isOneExactlyFunctionOrDo(n.getFirstChild());
        }
        return false;
    }
    // Check if it's a label
    if (n.getType() == Token.LABEL) {
        return isOneExactlyFunctionOrDo(n.getLastChild());
    }
    return false;
}
private boolean isOneExactlyFunctionOrDo(Node n) {
    // For labels with block children, we need to ensure that a
    // labeled FUNCTION or DO isn't generated when extraneous BLOCKs 
    // are skipped. 
    
    // If the node is directly a FUNCTION or DO, return true
    if (n.getType() == Token.FUNCTION || n.getType() == Token.DO) {
        return true;
    }
    
    // If it's a BLOCK, check if it contains exactly one child
    // that is a FUNCTION or DO
    if (n.getType() == Token.BLOCK) {
        Node child = n.getFirstChild();
        
        // Empty block or null child
        if (child == null) {
            return false;
        }
        
        // Check if there's exactly one child (no siblings)
        if (child.getNext() != null) {
            return false;
        }
        
        // Check if that single child is a FUNCTION or DO
        if (child.getType() == Token.FUNCTION || child.getType() == Token.DO) {
            return true;
        }
        
        // Check if the single child is a LABEL containing a FUNCTION or DO
        if (child.getType() == Token.LABEL) {
            Node labeledStatement = child.getFirstChild();
            if (labeledStatement != null) {
                labeledStatement = labeledStatement.getNext(); // Skip label name
                if (labeledStatement != null) {
                    if (labeledStatement.getType() == Token.FUNCTION || 
                        labeledStatement.getType() == Token.DO) {
                        return true;
                    }
                    // Check if labeled statement is a BLOCK with single FUNCTION or DO
                    if (labeledStatement.getType() == Token.BLOCK) {
                        Node blockChild = labeledStatement.getFirstChild();
                        if (blockChild != null && blockChild.getNext() == null &&
                            (blockChild.getType() == Token.FUNCTION || blockChild.getType() == Token.DO)) {
                            return true;
                        }
                    }
                }
            }
        }
    }
    
    return false;
}
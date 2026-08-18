Node processForInLoop(ForInLoop loopNode) {
    Node iterator = loopNode.getIterator();
    Node transformedIterator;
    
    if (iterator instanceof VariableDeclaration) {
        VariableDeclaration varDecl = (VariableDeclaration)iterator;
        List<VariableInitializer> vars = varDecl.getVariables();
        if (vars.size() == 1) {
            VariableInitializer varInit = vars.get(0);
            Node varNode = transform(varInit.getTarget());
            if (varInit.getInitializer() != null) {
                varNode = newNode(Token.VAR, varNode, transform(varInit.getInitializer()));
            } else {
                varNode = newNode(Token.VAR, varNode);
            }
            transformedIterator = varNode;
        } else {
            transformedIterator = transform(iterator);
        }
    } else {
        transformedIterator = transform(iterator);
    }
    
    return newNode(
        Token.FOR,
        transformedIterator,
        transform(loopNode.getIteratedObject()),
        transformBlock(loopNode.getBody()));
}
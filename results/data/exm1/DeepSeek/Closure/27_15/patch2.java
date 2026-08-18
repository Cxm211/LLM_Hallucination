public static Node tryCatch(Node tryBody, Node catchNode) {
    Preconditions.checkState(tryBody.isBlock());
    Preconditions.checkState(catchNode.isCatch());
    Node catchBody = new Node(Token.BLOCK);
    catchBody.addChildToBack(catchNode);
    catchBody.copyInformationFrom(catchNode);
    return new Node(Token.TRY, tryBody, catchBody);
}
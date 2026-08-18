    void insert(Token.Comment commentToken) {
        Comment comment = new Comment(commentToken.getData());
        insertNode(comment);
    }
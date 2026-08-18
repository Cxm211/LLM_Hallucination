void insert(Token.Comment commentToken) {
        Comment comment = new Comment(commentToken.getData(), baseUri);
        Node insert = comment;
        // For HTML, keep bogus comments as comments (e.g., XML declarations in HTML should remain comments)
        insertNode(insert);
    }
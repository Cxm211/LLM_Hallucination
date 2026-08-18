    void insert(Token.Comment commentToken) {
        Node insert;
        if (commentToken.bogus && commentToken.getData().length() > 0) {
            String data = commentToken.getData();
            char first = data.charAt(0);
            if (first == '!' || first == '?') {
                XmlDeclaration decl = new XmlDeclaration(data.substring(1), first == '?');
                decl.setBaseUri(baseUri);
                insert = decl;
                insertNode(insert);
                return;
            }
        }
        Comment comment = new Comment(commentToken.getData(), baseUri);
        insert = comment;
        insertNode(insert);
    }
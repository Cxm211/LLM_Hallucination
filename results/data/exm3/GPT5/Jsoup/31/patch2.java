void insert(Token.Comment commentToken) {
        String data = commentToken.getData();
        if (commentToken.bogus && data != null && data.length() > 1 && (data.startsWith("?") || data.startsWith("!"))) {
            boolean isProcessingInstruction = data.startsWith("?");
            String decl = data.substring(1);
            if (isProcessingInstruction && decl.endsWith("?")) {
                decl = decl.substring(0, decl.length() - 1);
            }
            XmlDeclaration xmlDecl = new XmlDeclaration(decl, baseUri, isProcessingInstruction);
            Node insert = xmlDecl;
            insertNode(insert);
        } else {
            Comment comment = new Comment(commentToken.getData(), baseUri);
            Node insert = comment;
            insertNode(insert);
        }
    }
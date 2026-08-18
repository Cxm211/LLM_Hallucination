void insert(Token.Doctype d) {
        DocumentType doctypeNode = new DocumentType(d.getName(), d.getPublicIdentifier(), d.getSystemIdentifier(), baseUri);
        insertNode(doctypeNode);
    }
    void insert(Token.Comment commentToken) {
        Comment comment = new Comment(commentToken.getData());
        Node insert = comment;
        if (commentToken.bogus) { // xml declarations are emitted as bogus comments (which is right for html, but not xml)
            // so we do a bit of a hack and parse the data as an element to pull the attributes out
            String data = comment.getData();
            if (data.length() > 1 && (data.startsWith("!") || data.startsWith("?"))) {
                String declData;
                if (data.startsWith("?")) {
                    // Processing instruction: data may be like ?xml version="1.0"? or ?xml version="1.0"
                    if (data.endsWith("?"))
                        declData = data.substring(1, data.length() - 1);
                    else
                        declData = data.substring(1);
                } else {
                    // Declarations like !DOCTYPE: data does not have trailing '?'
                    declData = data.substring(1);
                }
                Document doc = Jsoup.parse("<" + declData + ">", baseUri, Parser.xmlParser());
                Element el = doc.child(0);
                if (el != null) {
                    insert = new XmlDeclaration(settings.normalizeTag(el.tagName()), data.startsWith("!"));
                    insert.attributes().addAll(el.attributes());
                }
            }
        }
        insertNode(insert);
    }
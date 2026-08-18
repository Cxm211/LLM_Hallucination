void insert(Token.Comment commentToken) {
        Comment comment = new Comment(commentToken.getData());
        Node insert = comment;
        if (commentToken.bogus) { // xml declarations are emitted as bogus comments (which is right for html, but not xml)
            // so we do a bit of a hack and parse the data as an element to pull the attributes out
            String data = comment.getData();
            if (data.length() > 1 && (data.startsWith("!") || data.startsWith("?"))) {
                // parse the data after the leading ! or ? without trimming the last char (which may be valid)
                String inner = data.substring(1);
                Document doc = Jsoup.parse("<" + inner + ">", baseUri, Parser.xmlParser());
                if (doc.children().size() > 0) {
                    Element el = doc.child(0);
                    XmlDeclaration decl = new XmlDeclaration(settings.normalizeTag(el.tagName()), data.startsWith("!"));
                    decl.attributes().addAll(el.attributes());
                    insert = decl;
                } else {
                    insert = comment; // fallback if parsed doc has no elements
                }
            }
        }
        insertNode(insert);
    }
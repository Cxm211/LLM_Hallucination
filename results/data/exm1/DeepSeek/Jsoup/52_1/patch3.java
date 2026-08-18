void insert(Token.Comment commentToken) {
        Comment comment = new Comment(commentToken.getData(), baseUri);
        Node insert = comment;
        if (commentToken.bogus) { // xml declarations are emitted as bogus comments (which is right for html, but not xml)
            // so we do a bit of a hack and parse the data as an element to pull the attributes out
            String data = comment.getData();
            if (data.length() > 1 && (data.startsWith("!") || data.startsWith("?"))) {
                String declaration = data.substring(1, data.length() - 1).trim();
                String tagName = declaration.split("\\s+", 2)[0];
                String attrPart = declaration.length() > tagName.length() ? declaration.substring(tagName.length()).trim() : "";
                insert = new XmlDeclaration(tagName, comment.baseUri(), data.startsWith("!"));
                if (!attrPart.isEmpty()) {
                    Pattern pattern = Pattern.compile("([\\w-]+)\\s*=\\s*\"([^\"]*)\"|([\\w-]+)\\s*=\\s*'([^']*)'");
                    Matcher matcher = pattern.matcher(attrPart);
                    while (matcher.find()) {
                        String key = matcher.group(1) != null ? matcher.group(1) : matcher.group(3);
                        String value = matcher.group(2) != null ? matcher.group(2) : matcher.group(4);
                        insert.attributes.put(key, value);
                    }
                }
            }
        }
        insertNode(insert);
    }
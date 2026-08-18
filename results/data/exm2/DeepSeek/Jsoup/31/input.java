        public String toString() {
            return "</" + name() + ">";
        }

        void read(Tokeniser t, CharacterReader r) {
            // todo: handle bogus comment starting from eof. when does that trigger?
            // rewind to capture character that lead us here
            r.unconsume();
            Token.Comment comment = new Token.Comment();
            comment.data.append(r.consumeTo('>'));
            // todo: replace nullChar with replaceChar
            t.emit(comment);
            t.advanceTransition(Data);
        }

    void insert(Token.Comment commentToken) {
        Comment comment = new Comment(commentToken.getData(), baseUri);
        Node insert = comment;
        insertNode(insert);
    }

// trigger testcase
@Test public void handlesXmlDeclarationAsDeclaration() {
        String html = "<?xml encoding='UTF-8' ?><body>One</body><!-- comment -->";
        Document doc = Jsoup.parse(html, "", Parser.xmlParser());
        assertEquals("<?xml encoding='UTF-8' ?> <body> One </body> <!-- comment -->",
                StringUtil.normaliseWhitespace(doc.outerHtml()));
        assertEquals("#declaration", doc.childNode(0).nodeName());
        assertEquals("#comment", doc.childNode(2).nodeName());
    }

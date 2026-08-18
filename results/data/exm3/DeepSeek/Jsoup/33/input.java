// buggy function
    Element insert(Token.StartTag startTag) {
        // handle empty unknown tags
        // when the spec expects an empty tag, will directly hit insertEmpty, so won't generate this fake end tag.
        if (startTag.isSelfClosing()) {
            Element el = insertEmpty(startTag);
            stack.add(el);
            tokeniser.emit(new Token.EndTag(el.tagName()));  // ensure we get out of whatever state we are in. emitted for yielded processing
            return el;
        }
        
        Element el = new Element(Tag.valueOf(startTag.name()), baseUri, startTag.attributes);
        insert(el);
        return el;
    }

// trigger testcase
// org/jsoup/parser/HtmlParserTest.java::handlesKnownEmptyBlocks
@Test public void handlesKnownEmptyBlocks() {
        // if a known tag, allow self closing outside of spec, but force an end tag. unknown tags can be self closing.
        String h = "<div id='1' /><script src='/foo' /><div id=2><img /><img></div><a id=3 /><i /><foo /><foo>One</foo> <hr /> hr text <hr> hr text two";
        Document doc = Jsoup.parse(h);
        assertEquals("<div id=\"1\"></div><script src=\"/foo\"></script><div id=\"2\"><img /><img /></div><a id=\"3\"></a><i></i><foo /><foo>One</foo> <hr /> hr text <hr /> hr text two", TextUtil.stripNewlines(doc.body().html()));
    }

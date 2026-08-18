    protected void outerHtml(StringBuilder accum) {
        new NodeTraversor(new OuterHtmlVisitor(accum, ownerDocument().outputSettings())).traverse(this);
    }

// trigger testcase
@Test public void parentlessToString() {
        Document doc = Jsoup.parse("<img src='foo'>");
        Element img = doc.select("img").first();
        assertEquals("\n<img src=\"foo\" />", img.toString());

        img.remove(); // lost its parent
        assertEquals("<img src=\"foo\" />", img.toString());
    }

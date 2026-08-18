// buggy function
    private void normalise(Element element) {
        List<Node> toMove = new ArrayList<Node>();
        for (Node node: element.childNodes) {
            if (node instanceof TextNode) {
                TextNode tn = (TextNode) node;
                if (!tn.isBlank())
                    toMove.add(tn);
            }
        }

        for (Node node: toMove) {
            element.removeChild(node);
            body().appendChild(new TextNode(" ", ""));
            body().appendChild(node);
        }
    }

// trigger testcase
// org/jsoup/parser/ParserTest.java::createsStructureFromBodySnippet
@Test public void createsStructureFromBodySnippet() {
        // the bar baz stuff naturally goes into the body, but the 'foo' goes into root, and the normalisation routine
        // needs to move into the start of the body
        String html = "foo <b>bar</b> baz";
        Document doc = Jsoup.parse(html);
        assertEquals ("foo bar baz", doc.text());

    }

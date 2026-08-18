     Iterates the input and copies trusted nodes (tags, attributes, text) into the destination.
     */
    private int copySafeNodes(Element root, Element destination) {
        List<Node> sourceChildren = root.childNodes();
        int numDiscarded = 0;

        for (Node source : sourceChildren) {
            if (source instanceof Element) {
                Element sourceEl = (Element) source;

                if (whitelist.isSafeTag(sourceEl.tagName())) { // safe, clone and copy safe attrs
                    ElementMeta meta = createSafeElement(sourceEl);
                    Element destChild = meta.el;
                    destination.appendChild(destChild);

                    numDiscarded += meta.numAttribsDiscarded;
                    numDiscarded += copySafeNodes(sourceEl, destChild);
                } else {
                    numDiscarded++;
                    numDiscarded += copySafeNodes(sourceEl, destination);
                }
            } else if (source instanceof TextNode) {
                TextNode sourceText = (TextNode) source;
                TextNode destText = new TextNode(sourceText.getWholeText(), source.baseUri());
                destination.appendChild(destText);
            }
        }
        return numDiscarded;


    }

// trigger testcase
@Test public void testIsValid() {
        String ok = "<p>Test <b><a href='http://example.com/'>OK</a></b></p>";
        String nok1 = "<p><script></script>Not <b>OK</b></p>";
        String nok2 = "<p align=right>Test Not <b>OK</b></p>";
        String nok3 = "<!-- comment --><p>Not OK</p>"; // comments and the like will be cleaned
        assertTrue(Jsoup.isValid(ok, Whitelist.basic()));
        assertFalse(Jsoup.isValid(nok1, Whitelist.basic()));
        assertFalse(Jsoup.isValid(nok2, Whitelist.basic()));
        assertFalse(Jsoup.isValid(nok3, Whitelist.basic()));
    }

        final void newAttribute() {
            if (attributes == null)
                attributes = new Attributes();

            if (pendingAttributeName != null) {
                // the tokeniser has skipped whitespace control chars, but trimming could collapse to empty for other control codes, so verify here
                pendingAttributeName = pendingAttributeName.trim();
                    Attribute attribute;
                    if (hasPendingAttributeValue)
                        attribute = new Attribute(pendingAttributeName,
                            pendingAttributeValue.length() > 0 ? pendingAttributeValue.toString() : pendingAttributeValueS);
                    else if (hasEmptyAttributeValue)
                        attribute = new Attribute(pendingAttributeName, "");
                    else
                        attribute = new BooleanAttribute(pendingAttributeName);
                    attributes.put(attribute);
            }
            pendingAttributeName = null;
            hasEmptyAttributeValue = false;
            hasPendingAttributeValue = false;
            reset(pendingAttributeValue);
            pendingAttributeValueS = null;
        }

// trigger testcase
@Test public void handlesControlCodeInAttributeName() {
        Document doc = Jsoup.parse("<p><a \06=foo>One</a><a/\06=bar><a foo\06=bar>Two</a></p>");
        assertEquals("<p><a>One</a><a></a><a foo=\"bar\">Two</a></p>", doc.body().html());
    }

@Test public void handlesControlCharactersAfterTagName() {
        String html = "<a/\06>";
        String clean = Jsoup.clean(html, Whitelist.basic());
        assertEquals("<a rel=\"nofollow\"></a>", clean);
    }

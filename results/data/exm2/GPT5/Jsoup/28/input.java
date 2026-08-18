    public static boolean isNamedEntity(String name) {
        return full.containsKey(name);
    }

    static String unescape(String string, boolean strict) {
        if (!string.contains("&"))
            return string;
        Matcher m = strict? strictUnescapePattern.matcher(string) : unescapePattern.matcher(string);
        StringBuffer accum = new StringBuffer(string.length());
        while (m.find()) {
            int charval = -1;
            String num = m.group(3);
            if (num != null) {
                try {
                    int base = m.group(2) != null ? 16 : 10;
                    charval = Integer.valueOf(num, base);
                } catch (NumberFormatException e) {
                }
            } else {
                String name = m.group(1);
                if (full.containsKey(name))
                    charval = full.get(name);
            }
            if (charval != -1 || charval > 0xFFFF) {
                String c = Character.toString((char) charval);
                m.appendReplacement(accum, Matcher.quoteReplacement(c));
            } else {
                m.appendReplacement(accum, Matcher.quoteReplacement(m.group(0)));
            }
        }
        m.appendTail(accum);
        return accum.toString();
    }

    public static Document parseBodyFragment(String bodyHtml, String baseUri) {
        Document doc = Document.createShell(baseUri);
        Element body = doc.body();
        List<Node> nodeList = parseFragment(bodyHtml, body, baseUri);
        Node[] nodes = nodeList.toArray(new Node[nodeList.size()]); // the node list gets modified when re-parented
        for (Node node : nodes) {
            body.appendChild(node);
        }
        return doc;
    }

    Character consumeCharacterReference(Character additionalAllowedCharacter, boolean inAttribute) {
        if (reader.isEmpty())
            return null;
        if (additionalAllowedCharacter != null && additionalAllowedCharacter == reader.current())
            return null;
        if (reader.matchesAny('\t', '\n', '\r', '\f', ' ', '<', '&'))
            return null;

        reader.mark();
        if (reader.matchConsume("#")) { // numbered
            boolean isHexMode = reader.matchConsumeIgnoreCase("X");
            String numRef = isHexMode ? reader.consumeHexSequence() : reader.consumeDigitSequence();
            if (numRef.length() == 0) { // didn't match anything
                characterReferenceError("numeric reference with no numerals");
                reader.rewindToMark();
                return null;
            }
            if (!reader.matchConsume(";"))
                characterReferenceError("missing semicolon"); // missing semi
            int charval = -1;
            try {
                int base = isHexMode ? 16 : 10;
                charval = Integer.valueOf(numRef, base);
            } catch (NumberFormatException e) {
            } // skip
            if (charval == -1 || (charval >= 0xD800 && charval <= 0xDFFF) || charval > 0x10FFFF) {
                characterReferenceError("character outside of valid range");
                return replacementChar;
            } else {
                // todo: implement number replacement table
                // todo: check for extra illegal unicode points as parse errors
                return (char) charval;
            }
        } else { // named
            // get as many letters as possible, and look for matching entities.
            String nameRef = reader.consumeLetterThenDigitSequence();
            String origNameRef = new String(nameRef);
            boolean looksLegit = reader.matches(';');
            // found if a base named entity without a ;, or an extended entity with the ;.
            boolean found = false;
            while (nameRef.length() > 0 && !found) {
                if (Entities.isNamedEntity(nameRef))
                    found = true;
                else {
                    nameRef = nameRef.substring(0, nameRef.length()-1);
                    reader.unconsume();
                }
            }

            if (!found) {
                reader.rewindToMark();
                if (looksLegit) // named with semicolon
                    characterReferenceError(String.format("invalid named referenece '%s'", origNameRef));
                return null;
            }
            if (inAttribute && (reader.matchesLetter() || reader.matchesDigit() || reader.matchesAny('=', '-', '_'))) {
                // don't want that to match
                reader.rewindToMark();
                return null;
            }
            if (!reader.matchConsume(";"))
                characterReferenceError("missing semicolon"); // missing semi
            return Entities.getCharacterByName(nameRef);
        }
    }

    boolean currentNodeInHtmlNS() {
        // todo: implement namespaces correctly
        return true;
        // Element currentNode = currentNode();
        // return currentNode != null && currentNode.namespace().equals("HTML");
    }

// trigger testcase
@Test public void noSpuriousDecodes() {
        String string = "http://www.foo.com?a=1&num_rooms=1&children=0&int=VA&b=2";
        assertEquals(string, Entities.unescape(string));
    }

@Test public void unescape() {
        String text = "Hello &amp;&LT&gt; &reg &angst; &angst &#960; &#960 &#x65B0; there &! &frac34; &copy; &COPY;";
        assertEquals("Hello &<> ® Å &angst π π 新 there &! ¾ © ©", Entities.unescape(text));

        assertEquals("&0987654321; &unknown", Entities.unescape("&0987654321; &unknown"));
    }

@Test public void moreAttributeUnescapes() {
        String html = "<a href='&wr_id=123&mid-size=true&ok=&wr'>Check</a>";
        Elements els = Jsoup.parse(html).select("a");
        assertEquals("&wr_id=123&mid-size=true&ok=&wr", els.first().attr("href"));
    }

@Test public void strictAttributeUnescapes() {
        String html = "<a id=1 href='?foo=bar&mid&lt=true'>One</a> <a id=2 href='?foo=bar&lt;qux&lg=1'>Two</a>";
        Elements els = Jsoup.parse(html).select("a");
        assertEquals("?foo=bar&mid&lt=true", els.first().attr("href"));
        assertEquals("?foo=bar<qux&lg=1", els.last().attr("href"));
    }

@Test public void doesNotFindShortestMatchingEntity() {
        // previous behaviour was to identify a possible entity, then chomp down the string until a match was found.
        // (as defined in html5.) However in practise that lead to spurious matches against the author's intent.
        String html = "One &clubsuite; &clubsuit;";
        Document doc = Jsoup.parse(html);
        assertEquals(StringUtil.normaliseWhitespace("One &amp;clubsuite; ♣"), doc.body().html());
    }

@Test public void relaxedBaseEntityMatchAndStrictExtendedMatch() {
        // extended entities need a ; at the end to match, base does not
        String html = "&amp &quot &reg &icy &hopf &icy; &hopf;";
        Document doc = Jsoup.parse(html);
        doc.outputSettings().escapeMode(Entities.EscapeMode.extended); // modifies output only to clarify test
        assertEquals(StringUtil.normaliseWhitespace("&amp; &quot; &reg; &amp;icy &amp;hopf &icy; &hopf;"), doc.body().html());
    }

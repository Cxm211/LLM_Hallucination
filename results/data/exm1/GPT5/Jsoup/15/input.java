// buggy code
        boolean process(Token t, TreeBuilder tb) {
            switch (t.type) {
                case Character: {
                    Token.Character c = t.asCharacter();
                    if (c.getData().equals(nullString)) {
                        // todo confirm that check
                        tb.error(this);
                        return false;
                    } else if (isWhitespace(c)) {
                        tb.reconstructFormattingElements();
                        tb.insert(c);
                    } else {
                        tb.reconstructFormattingElements();
                        tb.insert(c);
                        tb.framesetOk(false);
                    }
                    break;
                }
                case Comment: {
                    tb.insert(t.asComment());
                    break;
                }
                case Doctype: {
                    tb.error(this);
                    return false;
                }
                case StartTag:
                    Token.StartTag startTag = t.asStartTag();
                    String name = startTag.name();
                    if (name.equals("html")) {
                        tb.error(this);
                        // merge attributes onto real html
                        Element html = tb.getStack().getFirst();
                        for (Attribute attribute : startTag.getAttributes()) {
                            if (!html.hasAttr(attribute.getKey()))
                                html.attributes().put(attribute);
                        }
                    } else if (StringUtil.in(name, "base", "basefont", "bgsound", "command", "link", "meta", "noframes", "style", "title")) {
                        return tb.process(t, InHead);
                    } else if (name.equals("body")) {
                        tb.error(this);
                        LinkedList<Element> stack = tb.getStack();
                        if (stack.size() == 1 || (stack.size() > 2 && !stack.get(1).nodeName().equals("body"))) {
                            // only in fragment case
                            return false; // ignore
                        } else {
                            tb.framesetOk(false);
                            Element body = stack.get(1);
                            for (Attribute attribute : startTag.getAttributes()) {
                                if (!body.hasAttr(attribute.getKey()))
                                    body.attributes().put(attribute);
                            }
                        }
                    } else if (name.equals("frameset")) {
                        tb.error(this);
                        LinkedList<Element> stack = tb.getStack();
                        if (stack.size() == 1 || (stack.size() > 2 && !stack.get(1).nodeName().equals("body"))) {
                            // only in fragment case
                            return false; // ignore
                        } else if (!tb.framesetOk()) {
                            return false; // ignore frameset
                        } else {
                            Element second = stack.get(1);
                            if (second.parent() != null)
                                second.remove();
                            // pop up to html element
                            while (stack.size() > 1)
                                stack.removeLast();
                            tb.insert(startTag);
                            tb.transition(InFrameset);
                        }
                    } else if (StringUtil.in(name,
                            "address", "article", "aside", "blockquote", "center", "details", "dir", "div", "dl",
                            "fieldset", "figcaption", "figure", "footer", "header", "hgroup", "menu", "nav", "ol",
                            "p", "section", "summary", "ul")) {
                        if (tb.inButtonScope("p")) {
                            tb.process(new Token.EndTag("p"));
                        }
                        tb.insert(startTag);
                    } else if (StringUtil.in(name, "h1", "h2", "h3", "h4", "h5", "h6")) {
                        if (tb.inButtonScope("p")) {
                            tb.process(new Token.EndTag("p"));
                        }
                        if (StringUtil.in(tb.currentElement().nodeName(), "h1", "h2", "h3", "h4", "h5", "h6")) {
                            tb.error(this);
                            tb.pop();
                        }
                        tb.insert(startTag);
                    } else if (StringUtil.in(name, "pre", "listing")) {
                        if (tb.inButtonScope("p")) {
                            tb.process(new Token.EndTag("p"));
                        }
                        tb.insert(startTag);
                        // todo: ignore LF if next token
                        tb.framesetOk(false);
                    } else if (name.equals("form")) {
                        if (tb.getFormElement() != null) {
                            tb.error(this);
                            return false;
                        }
                        if (tb.inButtonScope("p")) {
                            tb.process(new Token.EndTag("p"));
                        }
                        Element form = tb.insert(startTag);
                        tb.setFormElement(form);
                    } else if (name.equals("li")) {
                        tb.framesetOk(false);
                        LinkedList<Element> stack = tb.getStack();
                        for (int i = stack.size() - 1; i > 0; i--) {
                            Element el = stack.get(i);
                            if (el.nodeName().equals("li")) {
                                tb.process(new Token.EndTag("li"));
                                break;
                            }
                            if (tb.isSpecial(el) && !StringUtil.in(el.nodeName(), "address", "div", "p"))
                                break;
                        }
                        if (tb.inButtonScope("p")) {
                            tb.process(new Token.EndTag("p"));
                        }
                        tb.insert(startTag);
                    } else if (StringUtil.in(name, "dd", "dt")) {
                        tb.framesetOk(false);
                        LinkedList<Element> stack = tb.getStack();
                        for (int i = stack.size() - 1; i > 0; i--) {
                            Element el = stack.get(i);
                            if (StringUtil.in(el.nodeName(), "dd", "dt")) {
                                tb.process(new Token.EndTag(el.nodeName()));
                                break;
                            }
                            if (tb.isSpecial(el) && !StringUtil.in(el.nodeName(), "address", "div", "p"))
                                break;
                        }
                        if (tb.inButtonScope("p")) {
                            tb.process(new Token.EndTag("p"));
                        }
                        tb.insert(startTag);
                    } else if (name.equals("plaintext")) {
                        if (tb.inButtonScope("p")) {
                            tb.process(new Token.EndTag("p"));
                        }
                        tb.insert(startTag);
                        tb.tokeniser.transition(TokeniserState.PLAINTEXT); // once in, never gets out
                    } else if (name.equals("button")) {
                        if (tb.inButtonScope("button")) {
                            // close and reprocess
                            tb.error(this);
                            tb.process(new Token.EndTag("button"));
                            tb.process(startTag);
                        } else {
                            tb.reconstructFormattingElements();
                            tb.insert(startTag);
                            tb.framesetOk(false);
                        }
                    } else if (name.equals("a")) {
                        if (tb.getActiveFormattingElement("a") != null) {
                            tb.error(this);
                            tb.process(new Token.EndTag("a"));

                            // still on stack?
                            Element remainingA = tb.getFromStack("a");
                            if (remainingA != null) {
                                tb.removeFromActiveFormattingElements(remainingA);
                                tb.removeFromStack(remainingA);
                            }
                        }
                        tb.reconstructFormattingElements();
                        Element a = tb.insert(startTag);
                        tb.pushActiveFormattingElements(a);
                    } else if (StringUtil.in(name,
                            "b", "big", "code", "em", "font", "i", "s", "small", "strike", "strong", "tt", "u")) {
                        tb.reconstructFormattingElements();
                        Element el = tb.insert(startTag);
                        tb.pushActiveFormattingElements(el);
                    } else if (name.equals("nobr")) {
                        tb.reconstructFormattingElements();
                        if (tb.inScope("nobr")) {
                            tb.error(this);
                            tb.process(new Token.EndTag("nobr"));
                            tb.reconstructFormattingElements();
                        }
                        Element el = tb.insert(startTag);
                        tb.pushActiveFormattingElements(el);
                    } else if (StringUtil.in(name, "applet", "marquee", "object")) {
                        tb.reconstructFormattingElements();
                        tb.insert(startTag);
                        tb.insertMarkerToFormattingElements();
                        tb.framesetOk(false);
                    } else if (name.equals("table")) {
                        if (tb.getDocument().quirksMode() != Document.QuirksMode.quirks && tb.inButtonScope("p")) {
                            tb.process(new Token.EndTag("p"));
                        }
                        tb.insert(startTag);
                        tb.framesetOk(false);
                        tb.transition(InTable);
                    } else if (StringUtil.in(name, "area", "br", "embed", "img", "keygen", "wbr")) {
                        tb.reconstructFormattingElements();
                        tb.insertEmpty(startTag);
                        tb.framesetOk(false);
                    } else if (name.equals("input")) {
                        tb.reconstructFormattingElements();
                        Element el = tb.insertEmpty(startTag);
                        if (!el.attr("type").equalsIgnoreCase("hidden"))
                            tb.framesetOk(false);
                    } else if (StringUtil.in(name, "param", "source", "track")) {
                        tb.insertEmpty(startTag);
                    } else if (name.equals("hr")) {
                        if (tb.inButtonScope("p")) {
                            tb.process(new Token.EndTag("p"));
                        }
                        tb.insertEmpty(startTag);
                        tb.framesetOk(false);
                    } else if (name.equals("image")) {
                        // we're not supposed to ask.
                        startTag.name("img");
                        return tb.process(startTag);
                    } else if (name.equals("isindex")) {
                        // how much do we care about the early 90s?
                        tb.error(this);
                        if (tb.getFormElement() != null)
                            return false;

                        tb.tokeniser.acknowledgeSelfClosingFlag();
                        tb.process(new Token.StartTag("form"));
                        if (startTag.attributes.hasKey("action")) {
                            Element form = tb.getFormElement();
                            form.attr("action", startTag.attributes.get("action"));
                        }
                        tb.process(new Token.StartTag("hr"));
                        tb.process(new Token.StartTag("label"));
                        // hope you like english.
                        String prompt = startTag.attributes.hasKey("prompt") ?
                                startTag.attributes.get("prompt") :
                                "This is a searchable index. Enter search keywords: ";

                        tb.process(new Token.Character(prompt));

                        // input
                        Attributes inputAttribs = new Attributes();
                        for (Attribute attr : startTag.attributes) {
                            if (!StringUtil.in(attr.getKey(), "name", "action", "prompt"))
                                inputAttribs.put(attr);
                        }
                        inputAttribs.put("name", "isindex");
                        tb.process(new Token.StartTag("input", inputAttribs));
                        tb.process(new Token.EndTag("label"));
                        tb.process(new Token.StartTag("hr"));
                        tb.process(new Token.EndTag("form"));
                    } else if (name.equals("textarea")) {
                        tb.insert(startTag);
                        // todo: If the next token is a U+000A LINE FEED (LF) character token, then ignore that token and move on to the next one. (Newlines at the start of textarea elements are ignored as an authoring convenience.)
                        tb.tokeniser.transition(TokeniserState.Rcdata);
                        tb.markInsertionMode();
                        tb.framesetOk(false);
                        tb.transition(Text);
                    } else if (name.equals("xmp")) {
                        if (tb.inButtonScope("p")) {
                            tb.process(new Token.EndTag("p"));
                        }
                        tb.reconstructFormattingElements();
                        tb.framesetOk(false);
                        handleRawtext(startTag, tb);
                    } else if (name.equals("iframe")) {
                        tb.framesetOk(false);
                        handleRawtext(startTag, tb);
                    } else if (name.equals("noembed")) {
                        // also handle noscript if script enabled
                        handleRawtext(startTag, tb);
                    } else if (name.equals("select")) {
                        tb.reconstructFormattingElements();
                        tb.insert(startTag);
                        tb.framesetOk(false);

                        TreeBuilderState state = tb.state();
                        if (state.equals(InTable) || state.equals(InCaption) || state.equals(InTableBody) || state.equals(InRow) || state.equals(InCell))
                            tb.transition(InSelectInTable);
                        else
                            tb.transition(InSelect);
                    } else if (StringUtil.in("optgroup", "option")) {
                        if (tb.currentElement().nodeName().equals("option"))
                            tb.process(new Token.EndTag("option"));
                        tb.reconstructFormattingElements();
                        tb.insert(startTag);
                    } else if (StringUtil.in("rp", "rt")) {
                        if (tb.inScope("ruby")) {
                            tb.generateImpliedEndTags();
                            if (!tb.currentElement().nodeName().equals("ruby")) {
                                tb.error(this);
                                tb.popStackToBefore("ruby"); // i.e. close up to but not include name
                            }
                            tb.insert(startTag);
                        }
                    } else if (name.equals("math")) {
                        tb.reconstructFormattingElements();
                        // todo: handle A start tag whose tag name is "math" (i.e. foreign, mathml)
                        tb.insert(startTag);
                        tb.tokeniser.acknowledgeSelfClosingFlag();
                    } else if (name.equals("svg")) {
                        tb.reconstructFormattingElements();
                        // todo: handle A start tag whose tag name is "svg" (xlink, svg)
                        tb.insert(startTag);
                        tb.tokeniser.acknowledgeSelfClosingFlag();
                    } else if (StringUtil.in(name,
                            "caption", "col", "colgroup", "frame", "head", "tbody", "td", "tfoot", "th", "thead", "tr")) {
                        tb.error(this);
                        return false;
                    } else {
                        tb.reconstructFormattingElements();
                        tb.insert(startTag);
                    }
                    break;

                case EndTag:
                    Token.EndTag endTag = t.asEndTag();
                    name = endTag.name();
                    if (name.equals("body")) {
                        if (!tb.inScope("body")) {
                            tb.error(this);
                            return false;
                        } else {
                            // todo: error if stack contains something not dd, dt, li, optgroup, option, p, rp, rt, tbody, td, tfoot, th, thead, tr, body, html
                            tb.transition(AfterBody);
                        }
                    } else if (name.equals("html")) {
                        boolean notIgnored = tb.process(new Token.EndTag("body"));
                        if (notIgnored)
                            return tb.process(endTag);
                    } else if (StringUtil.in(name,
                            "address", "article", "aside", "blockquote", "button", "center", "details", "dir", "div",
                            "dl", "fieldset", "figcaption", "figure", "footer", "header", "hgroup", "listing", "menu",
                            "nav", "ol", "pre", "section", "summary", "ul")) {
                        // todo: refactor these lookups
                        if (!tb.inScope(name)) {
                            // nothing to close
                            tb.error(this);
                            return false;
                        } else {
                            tb.generateImpliedEndTags();
                            if (!tb.currentElement().nodeName().equals(name))
                                tb.error(this);
                            tb.popStackToClose(name);
                        }
                    } else if (name.equals("form")) {
                        Element currentForm = tb.getFormElement();
                        tb.setFormElement(null);
                        if (currentForm == null || !tb.inScope(name)) {
                            tb.error(this);
                            return false;
                        } else {
                            tb.generateImpliedEndTags();
                            if (!tb.currentElement().nodeName().equals(name))
                                tb.error(this);
                            // remove currentForm from stack. will shift anything under up.
                            tb.removeFromStack(currentForm);
                        }
                    } else if (name.equals("p")) {
                        if (!tb.inButtonScope(name)) {
                            tb.error(this);
                            tb.process(new Token.StartTag(name)); // if no p to close, creates an empty <p></p>
                            return tb.process(endTag);
                        } else {
                            tb.generateImpliedEndTags(name);
                            if (!tb.currentElement().nodeName().equals(name))
                                tb.error(this);
                            tb.popStackToClose(name);
                        }
                    } else if (name.equals("li")) {
                        if (!tb.inListItemScope(name)) {
                            tb.error(this);
                            return false;
                        } else {
                            tb.generateImpliedEndTags(name);
                            if (!tb.currentElement().nodeName().equals(name))
                                tb.error(this);
                            tb.popStackToClose(name);
                        }
                    } else if (StringUtil.in(name, "dd", "dt")) {
                        if (!tb.inScope(name)) {
                            tb.error(this);
                            return false;
                        } else {
                            tb.generateImpliedEndTags(name);
                            if (!tb.currentElement().nodeName().equals(name))
                                tb.error(this);
                            tb.popStackToClose(name);
                        }
                    } else if (StringUtil.in(name, "h1", "h2", "h3", "h4", "h5", "h6")) {
                        if (!tb.inScope(new String[]{"h1", "h2", "h3", "h4", "h5", "h6"})) {
                            tb.error(this);
                            return false;
                        } else {
                            tb.generateImpliedEndTags(name);
                            if (!tb.currentElement().nodeName().equals(name))
                                tb.error(this);
                            tb.popStackToClose("h1", "h2", "h3", "h4", "h5", "h6");
                        }
                    } else if (name.equals("sarcasm")) {
                        // *sigh*
                        return anyOtherEndTag(t, tb);
                    } else if (StringUtil.in(name,
                            "a", "b", "big", "code", "em", "font", "i", "nobr", "s", "small", "strike", "strong", "tt", "u")) {
                        // Adoption Agency Algorithm.
                        OUTER:
                        for (int i = 0; i < 8; i++) {
                            Element formatEl = tb.getActiveFormattingElement(name);
                            if (formatEl == null)
                                return anyOtherEndTag(t, tb);
                            else if (!tb.onStack(formatEl)) {
                                tb.error(this);
                                tb.removeFromActiveFormattingElements(formatEl);
                                return true;
                            } else if (!tb.inScope(formatEl.nodeName())) {
                                tb.error(this);
                                return false;
                            } else if (tb.currentElement() != formatEl)
                                tb.error(this);

                            Element furthestBlock = null;
                            Element commonAncestor = null;
                            boolean seenFormattingElement = false;
                            LinkedList<Element> stack = tb.getStack();
                            for (int si = 0; si < stack.size(); si++) {
                                Element el = stack.get(si);
                                if (el == formatEl) {
                                    commonAncestor = stack.get(si - 1);
                                    seenFormattingElement = true;
                                } else if (seenFormattingElement && tb.isSpecial(el)) {
                                    furthestBlock = el;
                                    break;
                                }
                            }
                            if (furthestBlock == null) {
                                tb.popStackToClose(formatEl.nodeName());
                                tb.removeFromActiveFormattingElements(formatEl);
                                return true;
                            }

                            // todo: Let a bookmark note the position of the formatting element in the list of active formatting elements relative to the elements on either side of it in the list.
                            // does that mean: int pos of format el in list?
                            Element node = furthestBlock;
                            Element lastNode = furthestBlock;
                            INNER:
                            for (int j = 0; j < 3; j++) {
                                if (tb.onStack(node))
                                    node = tb.aboveOnStack(node);
                                if (!tb.isInActiveFormattingElements(node)) { // note no bookmark check
                                    tb.removeFromStack(node);
                                    continue INNER;
                                } else if (node == formatEl)
                                    break INNER;

                                Element replacement = new Element(Tag.valueOf(node.nodeName()), tb.getBaseUri());
                                tb.replaceActiveFormattingElement(node, replacement);
                                tb.replaceOnStack(node, replacement);
                                node = replacement;

                                if (lastNode == furthestBlock) {
                                    // todo: move the aforementioned bookmark to be immediately after the new node in the list of active formatting elements.
                                    // not getting how this bookmark both straddles the element above, but is inbetween here...
                                }
                                if (lastNode.parent() != null)
                                    lastNode.remove();
                                node.appendChild(lastNode);

                                lastNode = node;
                            }

                            if (StringUtil.in(commonAncestor.nodeName(), "table", "tbody", "tfoot", "thead", "tr")) {
                                if (lastNode.parent() != null)
                                    lastNode.remove();
                                tb.insertInFosterParent(lastNode);
                            } else {
                                if (lastNode.parent() != null)
                                    lastNode.remove();
                                commonAncestor.appendChild(lastNode);
                            }

                            Element adopter = new Element(Tag.valueOf(name), tb.getBaseUri());
                            Node[] childNodes = furthestBlock.childNodes().toArray(new Node[furthestBlock.childNodes().size()]);
                            for (Node childNode : childNodes) {
                                adopter.appendChild(childNode); // append will reparent. thus the clone to avvoid concurrent mod.
                            }
                            furthestBlock.appendChild(adopter);
                            tb.removeFromActiveFormattingElements(formatEl);
                            // todo: insert the new element into the list of active formatting elements at the position of the aforementioned bookmark.
                            tb.removeFromStack(formatEl);
                            tb.insertOnStackAfter(furthestBlock, adopter);
                        }
                    } else if (StringUtil.in(name, "applet", "marquee", "object")) {
                        if (!tb.inScope("name")) {
                            if (!tb.inScope(name)) {
                                tb.error(this);
                                return false;
                            }
                            tb.generateImpliedEndTags();
                            if (!tb.currentElement().nodeName().equals(name))
                                tb.error(this);
                            tb.popStackToClose(name);
                            tb.clearFormattingElementsToLastMarker();
                        }
                    } else if (name.equals("br")) {
                        tb.error(this);
                        tb.process(new Token.StartTag("br"));
                        return false;
                    } else {
                        return anyOtherEndTag(t, tb);
                    }

                    break;
                case EOF:
                    // todo: error if stack contains something not dd, dt, li, p, tbody, td, tfoot, th, thead, tr, body, html
                    // stop parsing
                    break;
            }
            return true;
        }

// relevant test
// org.jsoup.integration.ParseTest::testSmhBizArticle
    public void testSmhBizArticle() throws IOException {
        File in = getFile("/htmltests/smh-biz-article-1.html");
        Document doc = Jsoup.parse(in, "UTF-8",
            "http://www.smh.com.au/business/the-boards-next-fear-the-female-quota-20100106-lteq.html");
        assertEquals("The board’s next fear: the female quota",
            doc.title()); 
        assertEquals("en", doc.select("html").attr("xml:lang"));

        Elements articleBody = doc.select(".articleBody > *");
        assertEquals(17, articleBody.size());
        

    }

// org.jsoup.integration.ParseTest::testNewsHomepage
    public void testNewsHomepage() throws IOException {
        File in = getFile("/htmltests/news-com-au-home.html");
        Document doc = Jsoup.parse(in, "UTF-8", "http://www.news.com.au/");
        assertEquals("News.com.au | News from Australia and around the world online | NewsComAu", doc.title());
        assertEquals("Brace yourself for Metro meltdown", doc.select(".id1225817868581 h4").text().trim());

        Element a = doc.select("a[href=/entertainment/horoscopes]").first();
        assertEquals("/entertainment/horoscopes", a.attr("href"));
        assertEquals("http://www.news.com.au/entertainment/horoscopes", a.attr("abs:href"));

        Element hs = doc.select("a[href*=naughty-corners-are-a-bad-idea]").first();
        assertEquals(
            "http://www.heraldsun.com.au/news/naughty-corners-are-a-bad-idea-for-kids/story-e6frf7jo-1225817899003",
            hs.attr("href"));
        assertEquals(hs.attr("href"), hs.attr("abs:href"));
    }

// org.jsoup.integration.ParseTest::testGoogleSearchIpod
    public void testGoogleSearchIpod() throws IOException {
        File in = getFile("/htmltests/google-ipod.html");
        Document doc = Jsoup.parse(in, "UTF-8", "http://www.google.com/search?hl=en&q=ipod&aq=f&oq=&aqi=g10");
        assertEquals("ipod - Google Search", doc.title());
        Elements results = doc.select("h3.r > a");
        assertEquals(12, results.size());
        assertEquals(
            "http://news.google.com/news?hl=en&q=ipod&um=1&ie=UTF-8&ei=uYlKS4SbBoGg6gPf-5XXCw&sa=X&oi=news_group&ct=title&resnum=1&ved=0CCIQsQQwAA",
            results.get(0).attr("href"));
        assertEquals("http://www.apple.com/itunes/",
            results.get(1).attr("href"));
    }

// org.jsoup.integration.ParseTest::testBinary
    public void testBinary() throws IOException {
        File in = getFile("/htmltests/thumb.jpg");
        Document doc = Jsoup.parse(in, "UTF-8");
        
        assertTrue(doc.text().contains("gd-jpeg"));
    }

// org.jsoup.integration.ParseTest::testYahooJp
    public void testYahooJp() throws IOException {
        File in = getFile("/htmltests/yahoo-jp.html");
        Document doc = Jsoup.parse(in, "UTF-8", "http://www.yahoo.co.jp/index.html"); 
        assertEquals("Yahoo! JAPAN", doc.title());
        Element a = doc.select("a[href=t/2322m2]").first();
        assertEquals("http://www.yahoo.co.jp/_ylh=X3oDMTB0NWxnaGxsBF9TAzIwNzcyOTYyNjUEdGlkAzEyBHRtcGwDZ2Ex/t/2322m2",
            a.attr("abs:href")); 
        assertEquals("全国、人気の駅ランキング", a.text());
    }

// org.jsoup.integration.ParseTest::testBaidu
    public void testBaidu() throws IOException {
        
        File in = getFile("/htmltests/baidu-cn-home.html");
        Document doc = Jsoup.parse(in, null,
            "http://www.baidu.com/"); 
        Element submit = doc.select("#su").first();
        assertEquals("百度一下", submit.attr("value"));

        
        submit = doc.select("input[value=百度一下]").first();
        assertEquals("su", submit.id());
        Element newsLink = doc.select("a:contains(新)").first();
        assertEquals("http://news.baidu.com", newsLink.absUrl("href"));

        
        assertEquals("GB2312", doc.outputSettings().charset().displayName());
        assertEquals("<title>百度一下，你就知道      </title>", doc.select("title").outerHtml());

        doc.outputSettings().charset("ascii");
        assertEquals("<title>&#30334;&#24230;&#19968;&#19979;&#65292;&#20320;&#23601;&#30693;&#36947;      </title>",
            doc.select("title").outerHtml());
    }

// org.jsoup.integration.ParseTest::testBaiduVariant
    public void testBaiduVariant() throws IOException {
        
        File in = getFile("/htmltests/baidu-variant.html");
        Document doc = Jsoup.parse(in, null,
            "http://www.baidu.com/"); 
        
        assertEquals("GB2312", doc.outputSettings().charset().displayName());
        assertEquals("<title>百度一下，你就知道</title>", doc.select("title").outerHtml());
    }

// org.jsoup.integration.ParseTest::testHtml5Charset
    public void testHtml5Charset() throws IOException {
        
        File in = getFile("/htmltests/meta-charset-1.html");
        Document doc = Jsoup.parse(in, null, "http://example.com/"); 
        assertEquals("新", doc.text());
        assertEquals("GB2312", doc.outputSettings().charset().displayName());

        
        in = getFile("/htmltests/meta-charset-2.html"); 
        doc = Jsoup.parse(in, null, "http://example.com"); 
        assertEquals("UTF-8", doc.outputSettings().charset().displayName());
        assertFalse("新".equals(doc.text()));

        
        in = getFile("/htmltests/meta-charset-3.html");
        doc = Jsoup.parse(in, null, "http://example.com/"); 
        assertEquals("UTF-8", doc.outputSettings().charset().displayName());
        assertEquals("新", doc.text());
    }

// org.jsoup.integration.ParseTest::testNytArticle
    public void testNytArticle() throws IOException {
        
        File in = getFile("/htmltests/nyt-article-1.html");
        Document doc = Jsoup.parse(in, null, "http://www.nytimes.com/2010/07/26/business/global/26bp.html?hp");

        Element headline = doc.select("nyt_headline[version=1.0]").first();
        assertEquals("As BP Lays Out Future, It Will Not Include Hayward", headline.text());
    }

// org.jsoup.integration.ParseTest::testYahooArticle
    public void testYahooArticle() throws IOException {
        File in = getFile("/htmltests/yahoo-article-1.html");
        Document doc = Jsoup.parse(in, "UTF-8", "http://news.yahoo.com/s/nm/20100831/bs_nm/us_gm_china");
        Element p = doc.select("p:contains(Volt will be sold in the United States").first();
        assertEquals("In July, GM said its electric Chevrolet Volt will be sold in the United States at $41,000 -- $8,000 more than its nearest competitor, the Nissan Leaf.", p.text());
    }

// org.jsoup.nodes.DocumentTest::setTextPreservesDocumentStructure
    @Test public void setTextPreservesDocumentStructure() {
        Document doc = Jsoup.parse("<p>Hello</p>");
        doc.text("Replaced");
        assertEquals("Replaced", doc.text());
        assertEquals("Replaced", doc.body().text());
        assertEquals(1, doc.select("head").size());
    }

// org.jsoup.nodes.DocumentTest::testTitles
    @Test public void testTitles() {
        Document noTitle = Jsoup.parse("<p>Hello</p>");
        Document withTitle = Jsoup.parse("<title>First</title><title>Ignore</title><p>Hello</p>");
        
        assertEquals("", noTitle.title());
        noTitle.title("Hello");
        assertEquals("Hello", noTitle.title());
        assertEquals("Hello", noTitle.select("title").first().text());
        
        assertEquals("First", withTitle.title());
        withTitle.title("Hello");
        assertEquals("Hello", withTitle.title());
        assertEquals("Hello", withTitle.select("title").first().text());
    }

// org.jsoup.nodes.DocumentTest::testOutputEncoding
    @Test public void testOutputEncoding() {
        Document doc = Jsoup.parse("<p title=π>π & < > </p>");
        
        assertEquals("<p title=\"π\">π &amp; &lt; &gt; </p>", doc.body().html());
        assertEquals("UTF-8", doc.outputSettings().charset().displayName());

        doc.outputSettings().charset("ascii");
        assertEquals(Entities.EscapeMode.base, doc.outputSettings().escapeMode());
        assertEquals("<p title=\"&#960;\">&#960; &amp; &lt; &gt; </p>", doc.body().html());

        doc.outputSettings().escapeMode(Entities.EscapeMode.extended);
        assertEquals("<p title=\"&pi;\">&pi; &amp; &lt; &gt; </p>", doc.body().html());
    }

// org.jsoup.nodes.DocumentTest::testXhtmlReferences
    @Test public void testXhtmlReferences() {
        Document doc = Jsoup.parse("&lt; &gt; &amp; &quot; &apos; &times;");
        doc.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
        assertEquals("&lt; &gt; &amp; &quot; &apos; ×", doc.body().html());
    }

// org.jsoup.nodes.DocumentTest::testNormalisesStructure
    @Test public void testNormalisesStructure() {
        Document doc = Jsoup.parse("<html><head><script>one</script><noscript><p>two</p></noscript></head><body><p>three</p></body><p>four</p></html>");
        assertEquals("<html><head><script>one</script><noscript></noscript></head><body><p>two</p><p>three</p><p>four</p></body></html>", TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.nodes.DocumentTest::testClone
    @Test public void testClone() {
        Document doc = Jsoup.parse("<title>Hello</title> <p>One<p>Two");
        Document clone = doc.clone();

        assertEquals("<html><head><title>Hello</title> </head><body><p>One</p><p>Two</p></body></html>", TextUtil.stripNewlines(clone.html()));
        clone.title("Hello there");
        clone.select("p").first().text("One more").attr("id", "1");
        assertEquals("<html><head><title>Hello there</title> </head><body><p id=\"1\">One more</p><p>Two</p></body></html>", TextUtil.stripNewlines(clone.html()));
        assertEquals("<html><head><title>Hello</title> </head><body><p>One</p><p>Two</p></body></html>", TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.nodes.DocumentTest::testClonesDeclarations
    @Test public void testClonesDeclarations() {
        Document doc = Jsoup.parse("<!DOCTYPE html><html><head><title>Doctype test");
        Document clone = doc.clone();

        assertEquals(doc.html(), clone.html());
        assertEquals("<!DOCTYPE html><html><head><title>Doctype test</title></head><body></body></html>",
                TextUtil.stripNewlines(clone.html()));
    }

// org.jsoup.nodes.ElementTest::getElementsByTagName
    @Test public void getElementsByTagName() {
        Document doc = Jsoup.parse(reference);
        List<Element> divs = doc.getElementsByTag("div");
        assertEquals(2, divs.size());
        assertEquals("div1", divs.get(0).id());
        assertEquals("div2", divs.get(1).id());

        List<Element> ps = doc.getElementsByTag("p");
        assertEquals(2, ps.size());
        assertEquals("Hello", ((TextNode) ps.get(0).childNode(0)).getWholeText());
        assertEquals("Another ", ((TextNode) ps.get(1).childNode(0)).getWholeText());
        List<Element> ps2 = doc.getElementsByTag("P");
        assertEquals(ps, ps2);

        List<Element> imgs = doc.getElementsByTag("img");
        assertEquals("foo.png", imgs.get(0).attr("src"));

        List<Element> empty = doc.getElementsByTag("wtf");
        assertEquals(0, empty.size());
    }

// org.jsoup.nodes.ElementTest::getNamespacedElementsByTag
    @Test public void getNamespacedElementsByTag() {
        Document doc = Jsoup.parse("<div><abc:def id=1>Hello</abc:def></div>");
        Elements els = doc.getElementsByTag("abc:def");
        assertEquals(1, els.size());
        assertEquals("1", els.first().id());
        assertEquals("abc:def", els.first().tagName());
    }

// org.jsoup.nodes.ElementTest::testGetElementById
    @Test public void testGetElementById() {
        Document doc = Jsoup.parse(reference);
        Element div = doc.getElementById("div1");
        assertEquals("div1", div.id());
        assertNull(doc.getElementById("none"));

        Document doc2 = Jsoup.parse("<div id=1><div id=2><p>Hello <span id=2>world!</span></p></div></div>");
        Element div2 = doc2.getElementById("2");
        assertEquals("div", div2.tagName()); 
        Element span = div2.child(0).getElementById("2"); 
        assertEquals("span", span.tagName());
    }

// org.jsoup.nodes.ElementTest::testGetText
    @Test public void testGetText() {
        Document doc = Jsoup.parse(reference);
        assertEquals("Hello Another element", doc.text());
        assertEquals("Another element", doc.getElementsByTag("p").get(1).text());
    }

// org.jsoup.nodes.ElementTest::testGetChildText
    @Test public void testGetChildText() {
        Document doc = Jsoup.parse("<p>Hello <b>there</b> now");
        Element p = doc.select("p").first();
        assertEquals("Hello there now", p.text());
        assertEquals("Hello now", p.ownText());
    }

// org.jsoup.nodes.ElementTest::testNormalisesText
    @Test public void testNormalisesText() {
        String h = "<p>Hello<p>There.</p> \n <p>Here <b>is</b> \n s<b>om</b>e text.";
        Document doc = Jsoup.parse(h);
        String text = doc.text();
        assertEquals("Hello There. Here is some text.", text);
    }

// org.jsoup.nodes.ElementTest::testKeepsPreText
    @Test public void testKeepsPreText() {
        String h = "<p>Hello \n \n there.</p> <div><pre>  What's \n\n  that?</pre>";
        Document doc = Jsoup.parse(h);
        assertEquals("Hello there.   What's \n\n  that?", doc.text());
    }

// org.jsoup.nodes.ElementTest::testKeepsPreTextInCode
    @Test public void testKeepsPreTextInCode() {
        String h = "<pre><code>code\n\ncode</code></pre>";
        Document doc = Jsoup.parse(h);
        assertEquals("code\n\ncode", doc.text());
        assertEquals("<pre><code>code\n\ncode</code></pre>", doc.body().html());
    }

// org.jsoup.nodes.ElementTest::testBrHasSpace
    @Test public void testBrHasSpace() {
        Document doc = Jsoup.parse("<p>Hello<br>there</p>");
        assertEquals("Hello there", doc.text());
        assertEquals("Hello there", doc.select("p").first().ownText());

        doc = Jsoup.parse("<p>Hello <br> there</p>");
        assertEquals("Hello there", doc.text());
    }

// org.jsoup.nodes.ElementTest::testGetSiblings
    @Test public void testGetSiblings() {
        Document doc = Jsoup.parse("<div><p>Hello<p id=1>there<p>this<p>is<p>an<p id=last>element</div>");
        Element p = doc.getElementById("1");
        assertEquals("there", p.text());
        assertEquals("Hello", p.previousElementSibling().text());
        assertEquals("this", p.nextElementSibling().text());
        assertEquals("Hello", p.firstElementSibling().text());
        assertEquals("element", p.lastElementSibling().text());
    }

// org.jsoup.nodes.ElementTest::testGetParents
    @Test public void testGetParents() {
        Document doc = Jsoup.parse("<div><p>Hello <span>there</span></div>");
        Element span = doc.select("span").first();
        Elements parents = span.parents();

        assertEquals(4, parents.size());
        assertEquals("p", parents.get(0).tagName());
        assertEquals("div", parents.get(1).tagName());
        assertEquals("body", parents.get(2).tagName());
        assertEquals("html", parents.get(3).tagName());
    }

// org.jsoup.nodes.ElementTest::testElementSiblingIndex
    @Test public void testElementSiblingIndex() {
        Document doc = Jsoup.parse("<div><p>One</p>...<p>Two</p>...<p>Three</p>");
        Elements ps = doc.select("p");
        assertTrue(0 == ps.get(0).elementSiblingIndex());
        assertTrue(1 == ps.get(1).elementSiblingIndex());
        assertTrue(2 == ps.get(2).elementSiblingIndex());
    }

// org.jsoup.nodes.ElementTest::testGetElementsWithClass
    @Test public void testGetElementsWithClass() {
        Document doc = Jsoup.parse("<div class='mellow yellow'><span class=mellow>Hello <b class='yellow'>Yellow!</b></span><p>Empty</p></div>");

        List<Element> els = doc.getElementsByClass("mellow");
        assertEquals(2, els.size());
        assertEquals("div", els.get(0).tagName());
        assertEquals("span", els.get(1).tagName());

        List<Element> els2 = doc.getElementsByClass("yellow");
        assertEquals(2, els2.size());
        assertEquals("div", els2.get(0).tagName());
        assertEquals("b", els2.get(1).tagName());

        List<Element> none = doc.getElementsByClass("solo");
        assertEquals(0, none.size());
    }

// org.jsoup.nodes.ElementTest::testGetElementsWithAttribute
    @Test public void testGetElementsWithAttribute() {
        Document doc = Jsoup.parse("<div style='bold'><p title=qux><p><b style></b></p></div>");
        List<Element> els = doc.getElementsByAttribute("style");
        assertEquals(2, els.size());
        assertEquals("div", els.get(0).tagName());
        assertEquals("b", els.get(1).tagName());

        List<Element> none = doc.getElementsByAttribute("class");
        assertEquals(0, none.size());
    }

// org.jsoup.nodes.ElementTest::testGetElementsWithAttributeDash
    @Test public void testGetElementsWithAttributeDash() {
        Document doc = Jsoup.parse("<meta http-equiv=content-type value=utf8 id=1> <meta name=foo content=bar id=2> <div http-equiv=content-type value=utf8 id=3>");
        Elements meta = doc.select("meta[http-equiv=content-type], meta[charset]");
        assertEquals(1, meta.size());
        assertEquals("1", meta.first().id());
    }

// org.jsoup.nodes.ElementTest::testGetElementsWithAttributeValue
    @Test public void testGetElementsWithAttributeValue() {
        Document doc = Jsoup.parse("<div style='bold'><p><p><b style></b></p></div>");
        List<Element> els = doc.getElementsByAttributeValue("style", "bold");
        assertEquals(1, els.size());
        assertEquals("div", els.get(0).tagName());

        List<Element> none = doc.getElementsByAttributeValue("style", "none");
        assertEquals(0, none.size());
    }

// org.jsoup.nodes.ElementTest::testClassDomMethods
    @Test public void testClassDomMethods() {
        Document doc = Jsoup.parse("<div><span class='mellow yellow'>Hello <b>Yellow</b></span></div>");
        List<Element> els = doc.getElementsByAttribute("class");
        Element span = els.get(0);
        assertEquals("mellow yellow", span.className());
        assertTrue(span.hasClass("mellow"));
        assertTrue(span.hasClass("yellow"));
        Set<String> classes = span.classNames();
        assertEquals(2, classes.size());
        assertTrue(classes.contains("mellow"));
        assertTrue(classes.contains("yellow"));

        assertEquals("", doc.className());
        assertFalse(doc.hasClass("mellow"));
    }

// org.jsoup.nodes.ElementTest::testClassUpdates
    @Test public void testClassUpdates() {
        Document doc = Jsoup.parse("<div class='mellow yellow'></div>");
        Element div = doc.select("div").first();

        div.addClass("green");
        assertEquals("mellow yellow green", div.className());
        div.removeClass("red"); 
        div.removeClass("yellow");
        assertEquals("mellow green", div.className());
        div.toggleClass("green").toggleClass("red");
        assertEquals("mellow red", div.className());
    }

// org.jsoup.nodes.ElementTest::testOuterHtml
    @Test public void testOuterHtml() {
        Document doc = Jsoup.parse("<div title='Tags &amp;c.'><img src=foo.png><p><!-- comment -->Hello<p>there");
        assertEquals("<html><head></head><body><div title=\"Tags &amp;c.\"><img src=\"foo.png\" /><p><!-- comment -->Hello</p><p>there</p></div></body></html>",
                TextUtil.stripNewlines(doc.outerHtml()));
    }

// org.jsoup.nodes.ElementTest::testInnerHtml
    @Test public void testInnerHtml() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div>");
        assertEquals("<p>Hello</p>", doc.getElementsByTag("div").get(0).html());
    }

// org.jsoup.nodes.ElementTest::testFormatHtml
    @Test public void testFormatHtml() {
        Document doc = Jsoup.parse("<title>Format test</title><div><p>Hello <span>jsoup <span>users</span></span></p><p>Good.</p></div>");
        assertEquals("<html>\n <head>\n  <title>Format test</title>\n </head>\n <body>\n  <div>\n   <p>Hello <span>jsoup <span>users</span></span></p>\n   <p>Good.</p>\n  </div>\n </body>\n</html>", doc.html());
    }

// org.jsoup.nodes.ElementTest::testSetIndent
    @Test public void testSetIndent() {
        Document doc = Jsoup.parse("<div><p>Hello\nthere</p></div>");
        doc.outputSettings().indentAmount(0);
        assertEquals("<html>\n<head></head>\n<body>\n<div>\n<p>Hello there</p>\n</div>\n</body>\n</html>", doc.html());
    }

// org.jsoup.nodes.ElementTest::testNotPretty
    @Test public void testNotPretty() {
        Document doc = Jsoup.parse("<div>   \n<p>Hello\n there</p></div>");
        doc.outputSettings().prettyPrint(false);
        assertEquals("<html><head></head><body><div>   \n<p>Hello\n there</p></div></body></html>", doc.html());
    }

// org.jsoup.nodes.ElementTest::testEmptyElementFormatHtml
    @Test public void testEmptyElementFormatHtml() {
        
        Document doc = Jsoup.parse("<section><div></div></section>");
        assertEquals("<section>\n <div></div>\n</section>", doc.select("section").first().outerHtml());
    }

// org.jsoup.nodes.ElementTest::testContainerOutput
    @Test public void testContainerOutput() {
        Document doc = Jsoup.parse("<title>Hello there</title> <div><p>Hello</p><p>there</p></div> <div>Another</div>");
        assertEquals("<title>Hello there</title>", doc.select("title").first().outerHtml());
        assertEquals("<div>\n <p>Hello</p>\n <p>there</p>\n</div>", doc.select("div").first().outerHtml());
        assertEquals("<div>\n <p>Hello</p>\n <p>there</p>\n</div> \n<div>\n Another\n</div>", doc.select("body").first().html());
    }

// org.jsoup.nodes.ElementTest::testSetText
    @Test public void testSetText() {
        String h = "<div id=1>Hello <p>there <b>now</b></p></div>";
        Document doc = Jsoup.parse(h);
        assertEquals("Hello there now", doc.text()); 
        assertEquals("there now", doc.select("p").get(0).text());

        Element div = doc.getElementById("1").text("Gone");
        assertEquals("Gone", div.text());
        assertEquals(0, doc.select("p").size());
    }

// org.jsoup.nodes.ElementTest::testAddNewElement
    @Test public void testAddNewElement() {
        Document doc = Jsoup.parse("<div id=1><p>Hello</p></div>");
        Element div = doc.getElementById("1");
        div.appendElement("p").text("there");
        div.appendElement("P").attr("class", "second").text("now");
        assertEquals("<html><head></head><body><div id=\"1\"><p>Hello</p><p>there</p><p class=\"second\">now</p></div></body></html>",
                TextUtil.stripNewlines(doc.html()));

        
        Elements ps = doc.select("p");
        for (int i = 0; i < ps.size(); i++) {
            assertEquals(i, ps.get(i).siblingIndex);
        }
    }

// org.jsoup.nodes.ElementTest::testAppendRowToTable
    @Test public void testAppendRowToTable() {
        Document doc = Jsoup.parse("<table><tr><td>1</td></tr></table>");
        Element table = doc.select("tbody").first();
        table.append("<tr><td>2</td></tr>");

        assertEquals("<table><tbody><tr><td>1</td></tr><tr><td>2</td></tr></tbody></table>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.nodes.ElementTest::testPrependRowToTable
        @Test public void testPrependRowToTable() {
        Document doc = Jsoup.parse("<table><tr><td>1</td></tr></table>");
        Element table = doc.select("tbody").first();
        table.prepend("<tr><td>2</td></tr>");

        assertEquals("<table><tbody><tr><td>2</td></tr><tr><td>1</td></tr></tbody></table>", TextUtil.stripNewlines(doc.body().html()));

        
        Elements ps = doc.select("tr");
        for (int i = 0; i < ps.size(); i++) {
            assertEquals(i, ps.get(i).siblingIndex);
        }
    }

// org.jsoup.nodes.ElementTest::testPrependElement
    @Test public void testPrependElement() {
        Document doc = Jsoup.parse("<div id=1><p>Hello</p></div>");
        Element div = doc.getElementById("1");
        div.prependElement("p").text("Before");
        assertEquals("Before", div.child(0).text());
        assertEquals("Hello", div.child(1).text());
    }

// org.jsoup.nodes.ElementTest::testAddNewText
    @Test public void testAddNewText() {
        Document doc = Jsoup.parse("<div id=1><p>Hello</p></div>");
        Element div = doc.getElementById("1");
        div.appendText(" there & now >");
        assertEquals("<p>Hello</p> there &amp; now &gt;", TextUtil.stripNewlines(div.html()));
    }

// org.jsoup.nodes.ElementTest::testPrependText
    @Test public void testPrependText() {
        Document doc = Jsoup.parse("<div id=1><p>Hello</p></div>");
        Element div = doc.getElementById("1");
        div.prependText("there & now > ");
        assertEquals("there & now > Hello", div.text());
        assertEquals("there &amp; now &gt; <p>Hello</p>", TextUtil.stripNewlines(div.html()));
    }

// org.jsoup.nodes.ElementTest::testAddNewHtml
    @Test public void testAddNewHtml() {
        Document doc = Jsoup.parse("<div id=1><p>Hello</p></div>");
        Element div = doc.getElementById("1");
        div.append("<p>there</p><p>now</p>");
        assertEquals("<p>Hello</p><p>there</p><p>now</p>", TextUtil.stripNewlines(div.html()));

        
        Elements ps = doc.select("p");
        for (int i = 0; i < ps.size(); i++) {
            assertEquals(i, ps.get(i).siblingIndex);
        }
    }

// org.jsoup.nodes.ElementTest::testPrependNewHtml
    @Test public void testPrependNewHtml() {
        Document doc = Jsoup.parse("<div id=1><p>Hello</p></div>");
        Element div = doc.getElementById("1");
        div.prepend("<p>there</p><p>now</p>");
        assertEquals("<p>there</p><p>now</p><p>Hello</p>", TextUtil.stripNewlines(div.html()));

        
        Elements ps = doc.select("p");
        for (int i = 0; i < ps.size(); i++) {
            assertEquals(i, ps.get(i).siblingIndex);
        }
    }

// org.jsoup.nodes.ElementTest::testSetHtml
    @Test public void testSetHtml() {
        Document doc = Jsoup.parse("<div id=1><p>Hello</p></div>");
        Element div = doc.getElementById("1");
        div.html("<p>there</p><p>now</p>");
        assertEquals("<p>there</p><p>now</p>", TextUtil.stripNewlines(div.html()));
    }

// org.jsoup.nodes.ElementTest::testWrap
    @Test public void testWrap() {
        Document doc = Jsoup.parse("<div><p>Hello</p><p>There</p></div>");
        Element p = doc.select("p").first();
        p.wrap("<div class='head'></div>");
        assertEquals("<div><div class=\"head\"><p>Hello</p></div><p>There</p></div>", TextUtil.stripNewlines(doc.body().html()));

        Element ret = p.wrap("<div><div class=foo></div><p>What?</p></div>");
        assertEquals("<div><div class=\"head\"><div><div class=\"foo\"><p>Hello</p></div><p>What?</p></div></div><p>There</p></div>", 
                TextUtil.stripNewlines(doc.body().html()));

        assertEquals(ret, p);
    }

// org.jsoup.nodes.ElementTest::before
    @Test public void before() {
        Document doc = Jsoup.parse("<div><p>Hello</p><p>There</p></div>");
        Element p1 = doc.select("p").first();
        p1.before("<div>one</div><div>two</div>");
        assertEquals("<div><div>one</div><div>two</div><p>Hello</p><p>There</p></div>", TextUtil.stripNewlines(doc.body().html()));
        
        doc.select("p").last().before("<p>Three</p><!-- four -->");
        assertEquals("<div><div>one</div><div>two</div><p>Hello</p><p>Three</p><!-- four --><p>There</p></div>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.nodes.ElementTest::after
    @Test public void after() {
        Document doc = Jsoup.parse("<div><p>Hello</p><p>There</p></div>");
        Element p1 = doc.select("p").first();
        p1.after("<div>one</div><div>two</div>");
        assertEquals("<div><p>Hello</p><div>one</div><div>two</div><p>There</p></div>", TextUtil.stripNewlines(doc.body().html()));
        
        doc.select("p").last().after("<p>Three</p><!-- four -->");
        assertEquals("<div><p>Hello</p><div>one</div><div>two</div><p>There</p><p>Three</p><!-- four --></div>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.nodes.ElementTest::testWrapWithRemainder
    @Test public void testWrapWithRemainder() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div>");
        Element p = doc.select("p").first();
        p.wrap("<div class='head'></div><p>There!</p>");
        assertEquals("<div><div class=\"head\"><p>Hello</p><p>There!</p></div></div>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.nodes.ElementTest::testHasText
    @Test public void testHasText() {
        Document doc = Jsoup.parse("<div><p>Hello</p><p></p></div>");
        Element div = doc.select("div").first();
        Elements ps = doc.select("p");

        assertTrue(div.hasText());
        assertTrue(ps.first().hasText());
        assertFalse(ps.last().hasText());
    }

// org.jsoup.nodes.ElementTest::dataset
    @Test public void dataset() {
        Document doc = Jsoup.parse("<div id=1 data-name=jsoup class=new data-package=jar>Hello</div><p id=2>Hello</p>");
        Element div = doc.select("div").first();
        Map<String, String> dataset = div.dataset();
        Attributes attributes = div.attributes();

        
        assertEquals(2, dataset.size());
        assertEquals("jsoup", dataset.get("name"));
        assertEquals("jar", dataset.get("package"));

        dataset.put("name", "jsoup updated");
        dataset.put("language", "java");
        dataset.remove("package");

        assertEquals(2, dataset.size());
        assertEquals(4, attributes.size());
        assertEquals("jsoup updated", attributes.get("data-name"));
        assertEquals("jsoup updated", dataset.get("name"));
        assertEquals("java", attributes.get("data-language"));
        assertEquals("java", dataset.get("language"));

        attributes.put("data-food", "bacon");
        assertEquals(3, dataset.size());
        assertEquals("bacon", dataset.get("food"));

        attributes.put("data-", "empty");
        assertEquals(null, dataset.get("")); 

        Element p = doc.select("p").first();
        assertEquals(0, p.dataset().size());

    }

// org.jsoup.nodes.ElementTest::parentlessToString
    @Test public void parentlessToString() {
        Document doc = Jsoup.parse("<img src='foo'>");
        Element img = doc.select("img").first();
        assertEquals("<img src=\"foo\" />", img.toString());

        img.remove(); 
        assertEquals("<img src=\"foo\" />", img.toString());
    }

// org.jsoup.nodes.ElementTest::testClone
    @Test public void testClone() {
        Document doc = Jsoup.parse("<div><p>One<p><span>Two</div>");

        Element p = doc.select("p").get(1);
        Element clone = p.clone();

        assertNull(clone.parent()); 
        assertEquals(0, clone.siblingIndex);
        assertEquals(1, p.siblingIndex);
        assertNotNull(p.parent());

        clone.append("<span>Three");
        assertEquals("<p><span>Two</span><span>Three</span></p>", TextUtil.stripNewlines(clone.outerHtml()));
        assertEquals("<div><p>One</p><p><span>Two</span></p></div>", TextUtil.stripNewlines(doc.body().html())); 

        doc.body().appendChild(clone); 
        assertNotNull(clone.parent());
        assertEquals("<div><p>One</p><p><span>Two</span></p></div><p><span>Two</span><span>Three</span></p>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.nodes.ElementTest::testTagNameSet
    @Test public void testTagNameSet() {
        Document doc = Jsoup.parse("<div><i>Hello</i>");
        doc.select("i").first().tagName("em");
        assertEquals(0, doc.select("i").size());
        assertEquals(1, doc.select("em").size());
        assertEquals("<em>Hello</em>", doc.select("div").first().html());
    }

// org.jsoup.nodes.ElementTest::testHtmlContainsOuter
    @Test public void testHtmlContainsOuter() {
        Document doc = Jsoup.parse("<title>Check</title> <div>Hello there</div>");
        doc.outputSettings().indentAmount(0);
        assertTrue(doc.html().contains(doc.select("title").outerHtml()));
        assertTrue(doc.html().contains(doc.select("div").outerHtml()));
    }

// org.jsoup.nodes.NodeTest::handlesBaseUri
    @Test public void handlesBaseUri() {
        Tag tag = Tag.valueOf("a");
        Attributes attribs = new Attributes();
        attribs.put("relHref", "/foo");
        attribs.put("absHref", "http://bar/qux");

        Element noBase = new Element(tag, "", attribs);
        assertEquals("", noBase.absUrl("relHref")); 
        assertEquals("http://bar/qux", noBase.absUrl("absHref")); 

        Element withBase = new Element(tag, "http://foo/", attribs);
        assertEquals("http://foo/foo", withBase.absUrl("relHref")); 
        assertEquals("http://bar/qux", withBase.absUrl("absHref")); 
        assertEquals("", withBase.absUrl("noval"));

        Element dodgyBase = new Element(tag, "wtf://no-such-protocol/", attribs);
        assertEquals("http://bar/qux", dodgyBase.absUrl("absHref")); 
        assertEquals("", dodgyBase.absUrl("relHref")); 
    }

// org.jsoup.nodes.NodeTest::handlesAbsPrefix
    @Test public void handlesAbsPrefix() {
        Document doc = Jsoup.parse("<a href=/foo>Hello</a>", "http://jsoup.org/");
        Element a = doc.select("a").first();
        assertEquals("/foo", a.attr("href"));
        assertEquals("http://jsoup.org/foo", a.attr("abs:href"));
        assertTrue(a.hasAttr("abs:href"));
    }

// org.jsoup.nodes.NodeTest::handlesAbsOnImage
    @Test public void handlesAbsOnImage() {
        Document doc = Jsoup.parse("<p><img src=\"/rez/osi_logo.png\" /></p>", "http://jsoup.org/");
        Element img = doc.select("img").first();
        assertEquals("http://jsoup.org/rez/osi_logo.png", img.attr("abs:src"));
        assertEquals(img.absUrl("src"), img.attr("abs:src"));
    }

// org.jsoup.nodes.NodeTest::handlesAbsPrefixOnHasAttr
    @Test public void handlesAbsPrefixOnHasAttr() {
        
        Document doc = Jsoup.parse("<a id=1 href='/foo'>One</a> <a id=2 href='http://jsoup.org/'>Two</a>");
        Element one = doc.select("#1").first();
        Element two = doc.select("#2").first();

        assertFalse(one.hasAttr("abs:href"));
        assertTrue(one.hasAttr("href"));
        assertEquals("", one.absUrl("href"));

        assertTrue(two.hasAttr("abs:href"));
        assertTrue(two.hasAttr("href"));
        assertEquals("http://jsoup.org/", two.absUrl("href"));
    }

// org.jsoup.nodes.NodeTest::literalAbsPrefix
    @Test public void literalAbsPrefix() {
        
        Document doc = Jsoup.parse("<a abs:href='odd'>One</a>");
        Element el = doc.select("a").first();
        assertTrue(el.hasAttr("abs:href"));
        assertEquals("odd", el.attr("abs:href"));
    }

// org.jsoup.nodes.NodeTest::absHandlesRelativeQuery
    @Test public void absHandlesRelativeQuery() {
        Document doc = Jsoup.parse("<a href='?foo'>One</a> <a href='bar.html?foo'>Two</a>", "http://jsoup.org/path/file?bar");

        Element a1 = doc.select("a").first();
        assertEquals("http://jsoup.org/path/file?foo", a1.absUrl("href"));

        Element a2 = doc.select("a").get(1);
        assertEquals("http://jsoup.org/path/bar.html?foo", a2.absUrl("href"));
    }

// org.jsoup.nodes.NodeTest::testRemove
    @Test public void testRemove() {
        Document doc = Jsoup.parse("<p>One <span>two</span> three</p>");
        Element p = doc.select("p").first();
        p.childNode(0).remove();
        
        assertEquals("two three", p.text());
        assertEquals("<span>two</span> three", TextUtil.stripNewlines(p.html()));
    }

// org.jsoup.nodes.NodeTest::testReplace
    @Test public void testReplace() {
        Document doc = Jsoup.parse("<p>One <span>two</span> three</p>");
        Element p = doc.select("p").first();
        Element insert = doc.createElement("em").text("foo");
        p.childNode(1).replaceWith(insert);
        
        assertEquals("One <em>foo</em> three", p.html());
    }

// org.jsoup.nodes.NodeTest::ownerDocument
    @Test public void ownerDocument() {
        Document doc = Jsoup.parse("<p>Hello");
        Element p = doc.select("p").first();
        assertTrue(p.ownerDocument() == doc);
        assertTrue(doc.ownerDocument() == doc);
        assertNull(doc.parent());
    }

// org.jsoup.nodes.NodeTest::before
    @Test public void before() {
        Document doc = Jsoup.parse("<p>One <b>two</b> three</p>");
        Element newNode = new Element(Tag.valueOf("em"), "");
        newNode.appendText("four");

        doc.select("b").first().before(newNode);
        assertEquals("<p>One <em>four</em><b>two</b> three</p>", doc.body().html());

        doc.select("b").first().before("<i>five</i>");
        assertEquals("<p>One <em>four</em><i>five</i><b>two</b> three</p>", doc.body().html());
    }

// org.jsoup.nodes.NodeTest::after
    @Test public void after() {
        Document doc = Jsoup.parse("<p>One <b>two</b> three</p>");
        Element newNode = new Element(Tag.valueOf("em"), "");
        newNode.appendText("four");

        doc.select("b").first().after(newNode);
        assertEquals("<p>One <b>two</b><em>four</em> three</p>", doc.body().html());

        doc.select("b").first().after("<i>five</i>");
        assertEquals("<p>One <b>two</b><i>five</i><em>four</em> three</p>", doc.body().html());
    }

// org.jsoup.nodes.NodeTest::unwrap
    @Test public void unwrap() {
        Document doc = Jsoup.parse("<div>One <span>Two <b>Three</b></span> Four</div>");
        Element span = doc.select("span").first();
        Node twoText = span.childNode(0);
        Node node = span.unwrap();

        assertEquals("<div>One Two <b>Three</b> Four</div>", TextUtil.stripNewlines(doc.body().html()));
        assertTrue(node instanceof TextNode);
        assertEquals("Two ", ((TextNode) node).text());
        assertEquals(node, twoText);
        assertEquals(node.parent(), doc.select("div").first());
    }

// org.jsoup.nodes.NodeTest::unwrapNoChildren
    @Test public void unwrapNoChildren() {
        Document doc = Jsoup.parse("<div>One <span></span> Two</div>");
        Element span = doc.select("span").first();
        Node node = span.unwrap();
        assertEquals("<div>One  Two</div>", TextUtil.stripNewlines(doc.body().html()));
        assertTrue(node == null);
    }

// org.jsoup.nodes.TextNodeTest::testBlank
    @Test public void testBlank() {
        TextNode one = new TextNode("", "");
        TextNode two = new TextNode("     ", "");
        TextNode three = new TextNode("  \n\n   ", "");
        TextNode four = new TextNode("Hello", "");
        TextNode five = new TextNode("  \nHello ", "");

        assertTrue(one.isBlank());
        assertTrue(two.isBlank());
        assertTrue(three.isBlank());
        assertFalse(four.isBlank());
        assertFalse(five.isBlank());
    }

// org.jsoup.nodes.TextNodeTest::testTextBean
    @Test public void testTextBean() {
        Document doc = Jsoup.parse("<p>One <span>two &amp;</span> three &amp;</p>");
        Element p = doc.select("p").first();

        Element span = doc.select("span").first();
        assertEquals("two &", span.text());
        TextNode spanText = (TextNode) span.childNode(0);
        assertEquals("two &", spanText.text());
        
        TextNode tn = (TextNode) p.childNode(2);
        assertEquals(" three &", tn.text());
        
        tn.text(" POW!");
        assertEquals("One <span>two &amp;</span> POW!", TextUtil.stripNewlines(p.html()));

        tn.attr("text", "kablam &");
        assertEquals("kablam &", tn.text());
        assertEquals("One <span>two &amp;</span>kablam &amp;", TextUtil.stripNewlines(p.html()));
    }

// org.jsoup.nodes.TextNodeTest::testSplitText
    @Test public void testSplitText() {
        Document doc = Jsoup.parse("<div>Hello there</div>");
        Element div = doc.select("div").first();
        TextNode tn = (TextNode) div.childNode(0);
        TextNode tail = tn.splitText(6);
        assertEquals("Hello ", tn.getWholeText());
        assertEquals("there", tail.getWholeText());
        tail.text("there!");
        assertEquals("Hello there!", div.text());
        assertTrue(tn.parent() == tail.parent());
    }

// org.jsoup.nodes.TextNodeTest::testSplitAnEmbolden
    @Test public void testSplitAnEmbolden() {
        Document doc = Jsoup.parse("<div>Hello there</div>");
        Element div = doc.select("div").first();
        TextNode tn = (TextNode) div.childNode(0);
        TextNode tail = tn.splitText(6);
        tail.wrap("<b></b>");

        assertEquals("Hello <b>there</b>", TextUtil.stripNewlines(div.html())); 
    }

// org.jsoup.parser.AttributeParseTest::parsesRoughAttributeString
    @Test public void parsesRoughAttributeString() {
        String html = "<a id=\"123\" class=\"baz = 'bar'\" style = 'border: 2px'qux zim foo = 12 mux=18 />";
        

        Element el = Jsoup.parse(html).getElementsByTag("a").get(0);
        Attributes attr = el.attributes();
        assertEquals(7, attr.size());
        assertEquals("123", attr.get("id"));
        assertEquals("baz = 'bar'", attr.get("class"));
        assertEquals("border: 2px", attr.get("style"));
        assertEquals("", attr.get("qux"));
        assertEquals("", attr.get("zim"));
        assertEquals("12", attr.get("foo"));
        assertEquals("18", attr.get("mux"));
    }

// org.jsoup.parser.AttributeParseTest::parsesEmptyString
    @Test public void parsesEmptyString() {
        String html = "<a />";
        Element el = Jsoup.parse(html).getElementsByTag("a").get(0);
        Attributes attr = el.attributes();
        assertEquals(0, attr.size());
    }

// org.jsoup.parser.AttributeParseTest::canStartWithEq
    @Test public void canStartWithEq() {
        String html = "<a =empty />";
        Element el = Jsoup.parse(html).getElementsByTag("a").get(0);
        Attributes attr = el.attributes();
        assertEquals(1, attr.size());
        assertTrue(attr.hasKey("=empty"));
        assertEquals("", attr.get("=empty"));
    }

// org.jsoup.parser.AttributeParseTest::strictAttributeUnescapes
    @Test public void strictAttributeUnescapes() {
        String html = "<a id=1 href='?foo=bar&mid&lt=true'>One</a> <a id=2 href='?foo=bar&lt;qux&lg=1'>Two</a>";
        Elements els = Jsoup.parse(html).select("a");
        assertEquals("?foo=bar∣&lt=true", els.first().attr("href")); 
        assertEquals("?foo=bar<qux&lg=1", els.last().attr("href"));
    }

// org.jsoup.parser.ParserTest::parsesSimpleDocument
    @Test public void parsesSimpleDocument() {
        String html = "<html><head><title>First!</title></head><body><p>First post! <img src=\"foo.png\" /></p></body></html>";
        Document doc = Jsoup.parse(html);
        
        Element p = doc.body().child(0);
        assertEquals("p", p.tagName());
        Element img = p.child(0);
        assertEquals("foo.png", img.attr("src"));
        assertEquals("img", img.tagName());
    }

// org.jsoup.parser.ParserTest::parsesRoughAttributes
    @Test public void parsesRoughAttributes() {
        String html = "<html><head><title>First!</title></head><body><p class=\"foo > bar\">First post! <img src=\"foo.png\" /></p></body></html>";
        Document doc = Jsoup.parse(html);

        
        Element p = doc.body().child(0);
        assertEquals("p", p.tagName());
        assertEquals("foo > bar", p.attr("class"));
    }

// org.jsoup.parser.ParserTest::parsesQuiteRoughAttributes
    @Test public void parsesQuiteRoughAttributes() {
        String html = "<p =a>One<a <p>Something</p>Else";
        
        Document doc = Jsoup.parse(html);
        assertEquals("<p =a=\"\">One<a <p=\"\">Something</a></p>\n" +
                "<a <p=\"\">Else</a>", doc.body().html());

        doc = Jsoup.parse("<p .....>");
        assertEquals("<p .....=\"\"></p>", doc.body().html());
    }

// org.jsoup.parser.ParserTest::parsesComments
    @Test public void parsesComments() {
        String html = "<html><head></head><body><img src=foo><!-- <table><tr><td></table> --><p>Hello</p></body></html>";
        Document doc = Jsoup.parse(html);

        Element body = doc.body();
        Comment comment = (Comment) body.childNode(1); 
        assertEquals(" <table><tr><td></table> ", comment.getData());
        Element p = body.child(1);
        TextNode text = (TextNode) p.childNode(0);
        assertEquals("Hello", text.getWholeText());
    }

// org.jsoup.parser.ParserTest::parsesUnterminatedComments
    @Test public void parsesUnterminatedComments() {
        String html = "<p>Hello<!-- <tr><td>";
        Document doc = Jsoup.parse(html);
        Element p = doc.getElementsByTag("p").get(0);
        assertEquals("Hello", p.text());
        TextNode text = (TextNode) p.childNode(0);
        assertEquals("Hello", text.getWholeText());
        Comment comment = (Comment) p.childNode(1);
        assertEquals(" <tr><td>", comment.getData());
    }

// org.jsoup.parser.ParserTest::dropsUnterminatedTag
    @Test public void dropsUnterminatedTag() {
        
        String h1 = "<p";
        Document doc = Jsoup.parse(h1);
        assertEquals(0, doc.getElementsByTag("p").size());
        assertEquals("", doc.text());

        String h2 = "<div id=1<p id='2'";
        doc = Jsoup.parse(h2);
        assertEquals("", doc.text());
    }

// org.jsoup.parser.ParserTest::dropsUnterminatedAttribute
    @Test public void dropsUnterminatedAttribute() {
        
        String h1 = "<p id=\"foo";
        Document doc = Jsoup.parse(h1);
        assertEquals("", doc.text());
    }

// org.jsoup.parser.ParserTest::parsesUnterminatedTextarea
    @Test public void parsesUnterminatedTextarea() {
        
        Document doc = Jsoup.parse("<body><p><textarea>one<p>two");
        Element t = doc.select("textarea").first();
        assertEquals("one", t.text());
        assertEquals("two", doc.select("p").get(1).text());
    }

// org.jsoup.parser.ParserTest::parsesUnterminatedOption
    @Test public void parsesUnterminatedOption() {
        
        Document doc = Jsoup.parse("<body><p><select><option>One<option>Two</p><p>Three</p>");
        Elements options = doc.select("option");
        assertEquals(2, options.size());
        assertEquals("One", options.first().text());
        assertEquals("TwoThree", options.last().text());
    }

// org.jsoup.parser.ParserTest::testSpaceAfterTag
    @Test public void testSpaceAfterTag() {
        Document doc = Jsoup.parse("<div > <a name=\"top\"></a ><p id=1 >Hello</p></div>");
        assertEquals("<div> <a name=\"top\"></a><p id=\"1\">Hello</p></div>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.ParserTest::createsDocumentStructure
    @Test public void createsDocumentStructure() {
        String html = "<meta name=keywords /><link rel=stylesheet /><title>jsoup</title><p>Hello world</p>";
        Document doc = Jsoup.parse(html);
        Element head = doc.head();
        Element body = doc.body();

        assertEquals(1, doc.children().size()); 
        assertEquals(2, doc.child(0).children().size()); 
        assertEquals(3, head.children().size());
        assertEquals(1, body.children().size());

        assertEquals("keywords", head.getElementsByTag("meta").get(0).attr("name"));
        assertEquals(0, body.getElementsByTag("meta").size());
        assertEquals("jsoup", doc.title());
        assertEquals("Hello world", body.text());
        assertEquals("Hello world", body.children().get(0).text());
    }

// org.jsoup.parser.ParserTest::createsStructureFromBodySnippet
    @Test public void createsStructureFromBodySnippet() {
        
        
        String html = "foo <b>bar</b> baz";
        Document doc = Jsoup.parse(html);
        assertEquals("foo bar baz", doc.text());

    }

// org.jsoup.parser.ParserTest::handlesEscapedData
    @Test public void handlesEscapedData() {
        String html = "<div title='Surf &amp; Turf'>Reef &amp; Beef</div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.getElementsByTag("div").get(0);

        assertEquals("Surf & Turf", div.attr("title"));
        assertEquals("Reef & Beef", div.text());
    }

// org.jsoup.parser.ParserTest::handlesDataOnlyTags
    @Test public void handlesDataOnlyTags() {
        String t = "<style>font-family: bold</style>";
        List<Element> tels = Jsoup.parse(t).getElementsByTag("style");
        assertEquals("font-family: bold", tels.get(0).data());
        assertEquals("", tels.get(0).text());

        String s = "<p>Hello</p><script>obj.insert('<a rel=\"none\" />');\ni++;</script><p>There</p>";
        Document doc = Jsoup.parse(s);
        assertEquals("Hello There", doc.text());
        assertEquals("obj.insert('<a rel=\"none\" />');\ni++;", doc.data());
    }

// org.jsoup.parser.ParserTest::handlesTextAfterData
    @Test public void handlesTextAfterData() {
        String h = "<html><body>pre <script>inner</script> aft</body></html>";
        Document doc = Jsoup.parse(h);
        assertEquals("<html><head></head><body>pre <script>inner</script> aft</body></html>", TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.ParserTest::handlesTextArea
    @Test public void handlesTextArea() {
        Document doc = Jsoup.parse("<textarea>Hello</textarea>");
        Elements els = doc.select("textarea");
        assertEquals("Hello", els.text());
        assertEquals("Hello", els.val());
    }

// org.jsoup.parser.ParserTest::doesNotCreateImplicitLists
    @Test public void doesNotCreateImplicitLists() {
        
        String h = "<li>Point one<li>Point two";
        Document doc = Jsoup.parse(h);
        Elements ol = doc.select("ul"); 
        assertEquals(0, ol.size());
        Elements lis = doc.select("li");
        assertEquals(2, lis.size());
        assertEquals("body", lis.first().parent().tagName());

        
        String h2 = "<ol><li><p>Point the first<li><p>Point the second";
        Document doc2 = Jsoup.parse(h2);

        assertEquals(0, doc2.select("ul").size());
        assertEquals(1, doc2.select("ol").size());
        assertEquals(2, doc2.select("ol li").size());
        assertEquals(2, doc2.select("ol li p").size());
        assertEquals(1, doc2.select("ol li").get(0).children().size()); 
    }

// org.jsoup.parser.ParserTest::discardsNakedTds
    @Test public void discardsNakedTds() {
        
        String h = "<td>Hello<td><p>There<p>now";
        Document doc = Jsoup.parse(h);
        assertEquals("Hello<p>There</p><p>now</p>", TextUtil.stripNewlines(doc.body().html()));
        
    }

// org.jsoup.parser.ParserTest::handlesNestedImplicitTable
    @Test public void handlesNestedImplicitTable() {
        Document doc = Jsoup.parse("<table><td>1</td></tr> <td>2</td></tr> <td> <table><td>3</td> <td>4</td></table> <tr><td>5</table>");
        assertEquals("<table><tbody><tr><td>1</td></tr> <tr><td>2</td></tr> <tr><td> <table><tbody><tr><td>3</td> <td>4</td></tr></tbody></table> </td></tr><tr><td>5</td></tr></tbody></table>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.ParserTest::handlesWhatWgExpensesTableExample
    @Test public void handlesWhatWgExpensesTableExample() {
        
        Document doc = Jsoup.parse("<table> <colgroup> <col> <colgroup> <col> <col> <col> <thead> <tr> <th> <th>2008 <th>2007 <th>2006 <tbody> <tr> <th scope=rowgroup> Research and development <td> $ 1,109 <td> $ 782 <td> $ 712 <tr> <th scope=row> Percentage of net sales <td> 3.4% <td> 3.3% <td> 3.7% <tbody> <tr> <th scope=rowgroup> Selling, general, and administrative <td> $ 3,761 <td> $ 2,963 <td> $ 2,433 <tr> <th scope=row> Percentage of net sales <td> 11.6% <td> 12.3% <td> 12.6% </table>");
        assertEquals("<table> <colgroup> <col /> </colgroup><colgroup> <col /> <col /> <col /> </colgroup><thead> <tr> <th> </th><th>2008 </th><th>2007 </th><th>2006 </th></tr></thead><tbody> <tr> <th scope=\"rowgroup\"> Research and development </th><td> $ 1,109 </td><td> $ 782 </td><td> $ 712 </td></tr><tr> <th scope=\"row\"> Percentage of net sales </th><td> 3.4% </td><td> 3.3% </td><td> 3.7% </td></tr></tbody><tbody> <tr> <th scope=\"rowgroup\"> Selling, general, and administrative </th><td> $ 3,761 </td><td> $ 2,963 </td><td> $ 2,433 </td></tr><tr> <th scope=\"row\"> Percentage of net sales </th><td> 11.6% </td><td> 12.3% </td><td> 12.6% </td></tr></tbody></table>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.ParserTest::handlesTbodyTable
    @Test public void handlesTbodyTable() {
        Document doc = Jsoup.parse("<html><head></head><body><table><tbody><tr><td>aaa</td><td>bbb</td></tr></tbody></table></body></html>");
        assertEquals("<table><tbody><tr><td>aaa</td><td>bbb</td></tr></tbody></table>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.ParserTest::handlesImplicitCaptionClose
    @Test public void handlesImplicitCaptionClose() {
        Document doc = Jsoup.parse("<table><caption>A caption<td>One<td>Two");
        assertEquals("<table><caption>A caption</caption><tbody><tr><td>One</td><td>Two</td></tr></tbody></table>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.ParserTest::noTableDirectInTable
    @Test public void noTableDirectInTable() {
        Document doc = Jsoup.parse("<table> <td>One <td><table><td>Two</table> <table><td>Three");
        assertEquals("<table> <tbody><tr><td>One </td><td><table><tbody><tr><td>Two</td></tr></tbody></table> <table><tbody><tr><td>Three</td></tr></tbody></table></td></tr></tbody></table>",
                TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.ParserTest::ignoresDupeEndTrTag
    @Test public void ignoresDupeEndTrTag() {
        Document doc = Jsoup.parse("<table><tr><td>One</td><td><table><tr><td>Two</td></tr></tr></table></td><td>Three</td></tr></table>"); 
        assertEquals("<table><tbody><tr><td>One</td><td><table><tbody><tr><td>Two</td></tr></tbody></table></td><td>Three</td></tr></tbody></table>",
                TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.ParserTest::handlesBaseTags
    @Test public void handlesBaseTags() {
        
        
        String h = "<a href=1>#</a><base href='/2/'><a href='3'>#</a><base href='http://bar'><a href=4>#</a>";
        Document doc = Jsoup.parse(h, "http://foo/");
        assertEquals("http://bar", doc.baseUri()); 

        Elements anchors = doc.getElementsByTag("a");
        assertEquals(3, anchors.size());

        assertEquals("http://foo/", anchors.get(0).baseUri());
        assertEquals("http://foo/2/", anchors.get(1).baseUri());
        assertEquals("http://bar", anchors.get(2).baseUri());

        assertEquals("http://foo/1", anchors.get(0).absUrl("href"));
        assertEquals("http://foo/2/3", anchors.get(1).absUrl("href"));
        assertEquals("http://bar/4", anchors.get(2).absUrl("href"));
    }

// org.jsoup.parser.ParserTest::handlesCdata
    @Test public void handlesCdata() {
        
        String h = "<div id=1><![CDATA[<html>\n<foo><&amp;]]></div>"; 
        Document doc = Jsoup.parse(h);
        Element div = doc.getElementById("1");
        assertEquals("<html> <foo><&amp;", div.text());
        assertEquals(0, div.children().size());
        assertEquals(1, div.childNodes().size()); 
    }

// org.jsoup.parser.ParserTest::handlesInvalidStartTags
    @Test public void handlesInvalidStartTags() {
        String h = "<div>Hello < There <&amp;></div>"; 
        Document doc = Jsoup.parse(h);
        assertEquals("Hello < There <&>", doc.select("div").first().text());
    }

// org.jsoup.parser.ParserTest::handlesUnknownTags
    @Test public void handlesUnknownTags() {
        String h = "<div><foo title=bar>Hello<foo title=qux>there</foo></div>";
        Document doc = Jsoup.parse(h);
        Elements foos = doc.select("foo");
        assertEquals(2, foos.size());
        assertEquals("bar", foos.first().attr("title"));
        assertEquals("qux", foos.last().attr("title"));
        assertEquals("there", foos.last().text());
    }

// org.jsoup.parser.ParserTest::handlesUnknownInlineTags
    @Test public void handlesUnknownInlineTags() {
        String h = "<p><cust>Test</cust></p><p><cust><cust>Test</cust></cust></p>";
        Document doc = Jsoup.parseBodyFragment(h);
        String out = doc.body().html();
        assertEquals(h, TextUtil.stripNewlines(out));
    }

// org.jsoup.parser.ParserTest::parsesBodyFragment
    @Test public void parsesBodyFragment() {
        String h = "<!-- comment --><p><a href='foo'>One</a></p>";
        Document doc = Jsoup.parseBodyFragment(h, "http://example.com");
        assertEquals("<body><!-- comment --><p><a href=\"foo\">One</a></p></body>", TextUtil.stripNewlines(doc.body().outerHtml()));
        assertEquals("http://example.com/foo", doc.select("a").first().absUrl("href"));
    }

// org.jsoup.parser.ParserTest::handlesUnknownNamespaceTags
    @Test public void handlesUnknownNamespaceTags() {
        
        String h = "<foo:bar id='1' /><abc:def id=2>Foo<p>Hello</p></abc:def><foo:bar>There</foo:bar>";
        Document doc = Jsoup.parse(h);
        assertEquals("<foo:bar id=\"1\" /><abc:def id=\"2\">Foo<p>Hello</p></abc:def><foo:bar>There</foo:bar>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.ParserTest::handlesKnownEmptyBlocks
    @Test public void handlesKnownEmptyBlocks() {
        
        String h = "<div id='1' /><div id=2><img /><img></div> <hr /> hr text <hr> hr text two";
        Document doc = Jsoup.parse(h);
        Element div1 = doc.getElementById("1");
        assertTrue(!div1.children().isEmpty()); 
        assertTrue(doc.select("hr").first().children().isEmpty());
        assertTrue(doc.select("hr").last().children().isEmpty());
        assertTrue(doc.select("img").first().children().isEmpty());
        assertTrue(doc.select("img").last().children().isEmpty());
    }

// org.jsoup.parser.ParserTest::handlesSolidusAtAttributeEnd
    @Test public void handlesSolidusAtAttributeEnd() {
        
        String h = "<a href=/>link</a>";
        Document doc = Jsoup.parse(h);
        assertEquals("<a href=\"/\">link</a>", doc.body().html());
    }

// org.jsoup.parser.ParserTest::handlesMultiClosingBody
    @Test public void handlesMultiClosingBody() {
        String h = "<body><p>Hello</body><p>there</p></body></body></html><p>now";
        Document doc = Jsoup.parse(h);
        assertEquals(3, doc.select("p").size());
        assertEquals(3, doc.body().children().size());
    }

// org.jsoup.parser.ParserTest::handlesUnclosedDefinitionLists
    @Test public void handlesUnclosedDefinitionLists() {
        
        String h = "<dt>Foo<dd>Bar<dt>Qux<dd>Zug";
        Document doc = Jsoup.parse(h);
        assertEquals(0, doc.select("dl").size()); 
        assertEquals(4, doc.select("dt, dd").size());
        Elements dts = doc.select("dt");
        assertEquals(2, dts.size());
        assertEquals("Zug", dts.get(1).nextElementSibling().text());
    }

// org.jsoup.parser.ParserTest::handlesBlocksInDefinitions
    @Test public void handlesBlocksInDefinitions() {
        
        String h = "<dl><dt><div id=1>Term</div></dt><dd><div id=2>Def</div></dd></dl>";
        Document doc = Jsoup.parse(h);
        assertEquals("dt", doc.select("#1").first().parent().tagName());
        assertEquals("dd", doc.select("#2").first().parent().tagName());
        assertEquals("<dl><dt><div id=\"1\">Term</div></dt><dd><div id=\"2\">Def</div></dd></dl>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.ParserTest::handlesFrames
    @Test public void handlesFrames() {
        String h = "<html><head><script></script><noscript></noscript></head><frameset><frame src=foo></frame><frame src=foo></frameset></html>";
        Document doc = Jsoup.parse(h);
        assertEquals("<html><head><script></script><noscript></noscript></head><frameset><frame src=\"foo\" /><frame src=\"foo\" /></frameset></html>",
                TextUtil.stripNewlines(doc.html()));
        
    }

// org.jsoup.parser.ParserTest::handlesJavadocFont
    @Test public void handlesJavadocFont() {
        String h = "<TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <A HREF=\"deprecated-list.html\"><FONT CLASS=\"NavBarFont1\"><B>Deprecated</B></FONT></A>&nbsp;</TD>";
        Document doc = Jsoup.parse(h);
        Element a = doc.select("a").first();
        assertEquals("Deprecated", a.text());
        assertEquals("font", a.child(0).tagName());
        assertEquals("b", a.child(0).child(0).tagName());
    }

// org.jsoup.parser.ParserTest::handlesBaseWithoutHref
    @Test public void handlesBaseWithoutHref() {
        String h = "<head><base target='_blank'></head><body><a href=/foo>Test</a></body>";
        Document doc = Jsoup.parse(h, "http://example.com/");
        Element a = doc.select("a").first();
        assertEquals("/foo", a.attr("href"));
        assertEquals("http://example.com/foo", a.attr("abs:href"));
    }

// org.jsoup.parser.ParserTest::normalisesDocument
    @Test public void normalisesDocument() {
        String h = "<!doctype html>One<html>Two<head>Three<link></head>Four<body>Five </body>Six </html>Seven ";
        Document doc = Jsoup.parse(h);
        assertEquals("<!DOCTYPE html><html><head></head><body>OneTwoThree<link />FourFive Six Seven </body></html>",
                TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.ParserTest::normalisesEmptyDocument
    @Test public void normalisesEmptyDocument() {
        Document doc = Jsoup.parse("");
        assertEquals("<html><head></head><body></body></html>", TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.ParserTest::normalisesHeadlessBody
    @Test public void normalisesHeadlessBody() {
        Document doc = Jsoup.parse("<html><body><span class=\"foo\">bar</span>");
        assertEquals("<html><head></head><body><span class=\"foo\">bar</span></body></html>",
                TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.ParserTest::normalisedBodyAfterContent
    @Test public void normalisedBodyAfterContent() {
        Document doc = Jsoup.parse("<font face=Arial><body class=name><div>One</div></body></font>");
        assertEquals("<html><head></head><body class=\"name\"><font face=\"Arial\"><div>One</div></font></body></html>",
                TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.ParserTest::findsCharsetInMalformedMeta
    @Test public void findsCharsetInMalformedMeta() {
        String h = "<meta http-equiv=Content-Type content=text/html; charset=gb2312>";
        
        Document doc = Jsoup.parse(h);
        assertEquals("gb2312", doc.select("meta").attr("charset"));
    }

// org.jsoup.parser.ParserTest::testHgroup
    @Test public void testHgroup() {
        
        Document doc = Jsoup.parse("<h1>Hello <h2>There <hgroup><h1>Another<h2>headline</hgroup> <hgroup><h1>More</h1><p>stuff</p></hgroup>");
        assertEquals("<h1>Hello </h1><h2>There <hgroup><h1>Another</h1><h2>headline</h2></hgroup> <hgroup><h1>More</h1><p>stuff</p></hgroup></h2>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.ParserTest::testRelaxedTags
    @Test public void testRelaxedTags() {
        Document doc = Jsoup.parse("<abc_def id=1>Hello</abc_def> <abc-def>There</abc-def>");
        assertEquals("<abc_def id=\"1\">Hello</abc_def> <abc-def>There</abc-def>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.ParserTest::testHeaderContents
    @Test public void testHeaderContents() {
        
        
        Document doc = Jsoup.parse("<h1>Hello <div>There</div> now</h1> <h2>More <h3>Content</h3></h2>");
        assertEquals("<h1>Hello <div>There</div> now</h1> <h2>More </h2><h3>Content</h3>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.ParserTest::testSpanContents
    @Test public void testSpanContents() {
        
        Document doc = Jsoup.parse("<span>Hello <div>there</div> <span>now</span></span>");
        assertEquals("<span>Hello <div>there</div> <span>now</span></span>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.ParserTest::testNoImagesInNoScriptInHead
    @Test public void testNoImagesInNoScriptInHead() {
        
        Document doc = Jsoup.parse("<html><head><noscript><img src='foo'></noscript></head><body><p>Hello</p></body></html>");
        assertEquals("<html><head><noscript></noscript></head><body><img src=\"foo\" /><p>Hello</p></body></html>", TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.ParserTest::testAFlowContents
    @Test public void testAFlowContents() {
        
        Document doc = Jsoup.parse("<a>Hello <div>there</div> <span>now</span></a>");
        assertEquals("<a>Hello <div>there</div> <span>now</span></a>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.ParserTest::testFontFlowContents
    @Test public void testFontFlowContents() {
        
        Document doc = Jsoup.parse("<font>Hello <div>there</div> <span>now</span></font>");
        assertEquals("<font>Hello <div>there</div> <span>now</span></font>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.ParserTest::handlesMisnestedTagsBI
    @Test public void handlesMisnestedTagsBI() {
        
        String h = "<p>1<b>2<i>3</b>4</i>5</p>";
        Document doc = Jsoup.parse(h);
        assertEquals("<p>1<b>2<i>3</i></b><i>4</i>5</p>", doc.body().html());
        
    }

// org.jsoup.parser.ParserTest::handlesMisnestedTagsBP
    @Test public void handlesMisnestedTagsBP() {
        
        String h = "<b>1<p>2</b>3</p>";
        Document doc = Jsoup.parse(h);
        assertEquals("<b>1</b>\n<p><b>2</b>3</p>", doc.body().html());
    }

// org.jsoup.parser.ParserTest::handlesUnexpectedMarkupInTables
    @Test public void handlesUnexpectedMarkupInTables() {
        
        
        String h = "<table><b><tr><td>aaa</td></tr>bbb</table>ccc";
        Document doc = Jsoup.parse(h);
        assertEquals("<b></b><b>bbb</b><table><tbody><tr><td>aaa</td></tr></tbody></table><b>ccc</b>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.ParserTest::handlesUnclosedFormattingElements
    @Test public void handlesUnclosedFormattingElements() {
        
        String h = "<!DOCTYPE html>\n" +
                "<p><b class=x><b class=x><b><b class=x><b class=x><b>X\n" +
                "<p>X\n" +
                "<p><b><b class=x><b>X\n" +
                "<p></b></b></b></b></b></b>X";
        Document doc = Jsoup.parse(h);
        doc.outputSettings().indentAmount(0);
        String want = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head></head>\n" +
                "<body>\n" +
                "<p><b class=\"x\"><b class=\"x\"><b><b class=\"x\"><b class=\"x\"><b>X </b></b></b></b></b></b></p>\n" +
                "<p><b class=\"x\"><b><b class=\"x\"><b class=\"x\"><b>X </b></b></b></b></b></p>\n" +
                "<p><b class=\"x\"><b><b class=\"x\"><b class=\"x\"><b><b><b class=\"x\"><b>X </b></b></b></b></b></b></b></b></p>\n" +
                "<p>X</p>\n" +
                "</body>\n" +
                "</html>";
        assertEquals(want, doc.html());
    }

// org.jsoup.parser.ParserTest::reconstructFormattingElements
    @Test public void reconstructFormattingElements() {
        
        String h = "<p><b class=one>One <i>Two <b>Three</p><p>Hello</p>";
        Document doc = Jsoup.parse(h);
        assertEquals("<p><b class=\"one\">One <i>Two <b>Three</b></i></b></p>\n<p><b class=\"one\"><i><b>Hello</b></i></b></p>", doc.body().html());
    }

// org.jsoup.parser.ParserTest::reconstructFormattingElementsInTable
    @Test public void reconstructFormattingElementsInTable() {
        
        
        String h = "<p><b>One</p> <table><tr><td><p><i>Three<p>Four</i></td></tr></table> <p>Five</p>";
        Document doc = Jsoup.parse(h);
        String want = "<p><b>One</b></p>\n" +
                "<b> \n" +
                " <table>\n" +
                "  <tbody>\n" +
                "   <tr>\n" +
                "    <td><p><i>Three</i></p><p><i>Four</i></p></td>\n" +
                "   </tr>\n" +
                "  </tbody>\n" +
                " </table> <p>Five</p></b>";
        assertEquals(want, doc.body().html());
    }

// org.jsoup.parser.ParserTest::commentBeforeHtml
    @Test public void commentBeforeHtml() {
        String h = "<!-- comment --><!-- comment 2 --><p>One</p>";
        Document doc = Jsoup.parse(h);
        assertEquals("<!-- comment --><!-- comment 2 --><html><head></head><body><p>One</p></body></html>", TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.ParserTest::emptyTdTag
    @Test public void emptyTdTag() {
        String h = "<table><tr><td>One</td><td id='2' /></tr></table>";
        Document doc = Jsoup.parse(h);
        assertEquals("<td>One</td>\n<td id=\"2\"></td>", doc.select("tr").first().html());
    }

// org.jsoup.parser.ParserTest::handlesSolidusInA
    @Test public void handlesSolidusInA() {
        
        String h = "<a class=lp href=/lib/14160711/>link text</a>";
        Document doc = Jsoup.parse(h);
        Element a = doc.select("a").first();
        assertEquals("link text", a.text());
        assertEquals("/lib/14160711/", a.attr("href"));
    }

// org.jsoup.parser.ParserTest::handlesSpanInTbody
    @Test public void handlesSpanInTbody() {
        
        String h = "<table><tbody><span class='1'><tr><td>One</td></tr><tr><td>Two</td></tr></span></tbody></table>";
        Document doc = Jsoup.parse(h);
        assertEquals(doc.select("span").first().children().size(), 0); 
        assertEquals(doc.select("table").size(), 1); 
    }

// org.jsoup.parser.ParserTest::handlesUnclosedTitleAtEof
    @Test public void handlesUnclosedTitleAtEof() {
        assertEquals("Data", Jsoup.parse("<title>Data").title());
        assertEquals("Data<", Jsoup.parse("<title>Data<").title());
        assertEquals("Data</", Jsoup.parse("<title>Data</").title());
        assertEquals("Data</t", Jsoup.parse("<title>Data</t").title());
        assertEquals("Data</ti", Jsoup.parse("<title>Data</ti").title());
        assertEquals("Data", Jsoup.parse("<title>Data</title>").title());
        assertEquals("Data", Jsoup.parse("<title>Data</title >").title());
    }

// org.jsoup.parser.ParserTest::handlesUnclosedTitle
    @Test public void handlesUnclosedTitle() {
        Document one = Jsoup.parse("<title>One <b>Two <b>Three</TITLE><p>Test</p>"); 
        assertEquals("One <b>Two <b>Three", one.title());
        assertEquals("Test", one.select("p").first().text());

        Document two = Jsoup.parse("<title>One<b>Two <p>Test</p>"); 
        assertEquals("One", two.title());
        assertEquals("<b>Two <p>Test</p></b>", two.body().html());
    }

// org.jsoup.parser.ParserTest::noImplicitFormForTextAreas
    @Test public void noImplicitFormForTextAreas() {
        
        Document doc = Jsoup.parse("<textarea>One</textarea>");
        assertEquals("<textarea>One</textarea>", doc.body().html());
    }

// org.jsoup.safety.CleanerTest::simpleBehaviourTest
    @Test public void simpleBehaviourTest() {
        String h = "<div><p class=foo><a href='http://evil.com'>Hello <b id=bar>there</b>!</a></div>";
        String cleanHtml = Jsoup.clean(h, Whitelist.simpleText());

        assertEquals("Hello <b>there</b>!", TextUtil.stripNewlines(cleanHtml));
    }

// org.jsoup.safety.CleanerTest::simpleBehaviourTest2
    @Test public void simpleBehaviourTest2() {
        String h = "Hello <b>there</b>!";
        String cleanHtml = Jsoup.clean(h, Whitelist.simpleText());

        assertEquals("Hello <b>there</b>!", TextUtil.stripNewlines(cleanHtml));
    }

// org.jsoup.safety.CleanerTest::basicBehaviourTest
    @Test public void basicBehaviourTest() {
        String h = "<div><p><a href='javascript:sendAllMoney()'>Dodgy</a> <A HREF='HTTP://nice.com'>Nice</a></p><blockquote>Hello</blockquote>";
        String cleanHtml = Jsoup.clean(h, Whitelist.basic());

        assertEquals("<p><a rel=\"nofollow\">Dodgy</a> <a href=\"http://nice.com\" rel=\"nofollow\">Nice</a></p><blockquote>Hello</blockquote>",
                TextUtil.stripNewlines(cleanHtml));
    }

// org.jsoup.safety.CleanerTest::basicWithImagesTest
    @Test public void basicWithImagesTest() {
        String h = "<div><p><img src='http://example.com/' alt=Image></p><p><img src='ftp://ftp.example.com'></p></div>";
        String cleanHtml = Jsoup.clean(h, Whitelist.basicWithImages());
        assertEquals("<p><img src=\"http://example.com/\" alt=\"Image\" /></p><p><img /></p>", TextUtil.stripNewlines(cleanHtml));
    }

// org.jsoup.safety.CleanerTest::testRelaxed
    @Test public void testRelaxed() {
        String h = "<h1>Head</h1><table><tr><td>One<td>Two</td></tr></table>";
        String cleanHtml = Jsoup.clean(h, Whitelist.relaxed());
        assertEquals("<h1>Head</h1><table><tbody><tr><td>One</td><td>Two</td></tr></tbody></table>", TextUtil.stripNewlines(cleanHtml));
    }

// org.jsoup.safety.CleanerTest::testDropComments
    @Test public void testDropComments() {
        String h = "<p>Hello<!-- no --></p>";
        String cleanHtml = Jsoup.clean(h, Whitelist.relaxed());
        assertEquals("<p>Hello</p>", cleanHtml);
    }

// org.jsoup.safety.CleanerTest::testDropXmlProc
    @Test public void testDropXmlProc() {
        String h = "<?import namespace=\"xss\"><p>Hello</p>";
        String cleanHtml = Jsoup.clean(h, Whitelist.relaxed());
        assertEquals("<p>Hello</p>", cleanHtml);
    }

// org.jsoup.safety.CleanerTest::testDropScript
    @Test public void testDropScript() {
        String h = "<SCRIPT SRC=//ha.ckers.org/.j><SCRIPT>alert(/XSS/.source)</SCRIPT>";
        String cleanHtml = Jsoup.clean(h, Whitelist.relaxed());
        assertEquals("", cleanHtml);
    }

// org.jsoup.safety.CleanerTest::testDropImageScript
    @Test public void testDropImageScript() {
        String h = "<IMG SRC=\"javascript:alert('XSS')\">";
        String cleanHtml = Jsoup.clean(h, Whitelist.relaxed());
        assertEquals("<img />", cleanHtml);
    }

// org.jsoup.safety.CleanerTest::testCleanJavascriptHref
    @Test public void testCleanJavascriptHref() {
        String h = "<A HREF=\"javascript:document.location='http://www.google.com/'\">XSS</A>";
        String cleanHtml = Jsoup.clean(h, Whitelist.relaxed());
        assertEquals("<a>XSS</a>", cleanHtml);
    }

// org.jsoup.safety.CleanerTest::testDropsUnknownTags
    @Test public void testDropsUnknownTags() {
        String h = "<p><custom foo=true>Test</custom></p>";
        String cleanHtml = Jsoup.clean(h, Whitelist.relaxed());
        assertEquals("<p>Test</p>", cleanHtml);
    }

// org.jsoup.safety.CleanerTest::testHandlesEmptyAttributes
    @Test public void testHandlesEmptyAttributes() {
        String h = "<img alt=\"\" src= unknown=''>";
        String cleanHtml = Jsoup.clean(h, Whitelist.basicWithImages());
        assertEquals("<img alt=\"\" />", cleanHtml);
    }

// org.jsoup.safety.CleanerTest::testIsValid
    @Test public void testIsValid() {
        String ok = "<p>Test <b><a href='http://example.com/'>OK</a></b></p>";
        String nok1 = "<p><script></script>Not <b>OK</b></p>";
        String nok2 = "<p align=right>Test Not <b>OK</b></p>";
        assertTrue(Jsoup.isValid(ok, Whitelist.basic()));
        assertFalse(Jsoup.isValid(nok1, Whitelist.basic()));
        assertFalse(Jsoup.isValid(nok2, Whitelist.basic()));
    }

// org.jsoup.safety.CleanerTest::resolvesRelativeLinks
    @Test public void resolvesRelativeLinks() {
        String html = "<a href='/foo'>Link</a>";
        String clean = Jsoup.clean(html, "http://example.com/", Whitelist.basic());
        assertEquals("<a href=\"http://example.com/foo\" rel=\"nofollow\">Link</a>", clean);
    }

// org.jsoup.safety.CleanerTest::dropsUnresolvableRelativeLinks
    @Test public void dropsUnresolvableRelativeLinks() {
        String html = "<a href='/foo'>Link</a>";
        String clean = Jsoup.clean(html, Whitelist.basic());
        assertEquals("<a rel=\"nofollow\">Link</a>", clean);
    }

// org.jsoup.select.ElementsTest::filter
    @Test public void filter() {
        String h = "<p>Excl</p><div class=headline><p>Hello</p><p>There</p></div><div class=headline><h1>Headline</h1></div>";
        Document doc = Jsoup.parse(h);
        Elements els = doc.select(".headline").select("p");
        assertEquals(2, els.size());
        assertEquals("Hello", els.get(0).text());
        assertEquals("There", els.get(1).text());
    }

// org.jsoup.select.ElementsTest::attributes
    @Test public void attributes() {
        String h = "<p title=foo><p title=bar><p class=foo><p class=bar>";
        Document doc = Jsoup.parse(h);
        Elements withTitle = doc.select("p[title]");
        assertEquals(2, withTitle.size());
        assertTrue(withTitle.hasAttr("title"));
        assertFalse(withTitle.hasAttr("class"));
        assertEquals("foo", withTitle.attr("title"));

        withTitle.removeAttr("title");
        assertEquals(2, withTitle.size()); 
        assertEquals(0, doc.select("p[title]").size());

        Elements ps = doc.select("p").attr("style", "classy");
        assertEquals(4, ps.size());
        assertEquals("classy", ps.last().attr("style"));
        assertEquals("bar", ps.last().attr("class"));
    }

// org.jsoup.select.ElementsTest::hasAttr
    @Test public void hasAttr() {
        Document doc = Jsoup.parse("<p title=foo><p title=bar><p class=foo><p class=bar>");
        Elements ps = doc.select("p");
        assertTrue(ps.hasAttr("class"));
        assertFalse(ps.hasAttr("style"));
    }

// org.jsoup.select.ElementsTest::hasAbsAttr
    @Test public void hasAbsAttr() {
        Document doc = Jsoup.parse("<a id=1 href='/foo'>One</a> <a id=2 href='http://jsoup.org'>Two</a>");
        Elements one = doc.select("#1");
        Elements two = doc.select("#2");
        Elements both = doc.select("a");
        assertFalse(one.hasAttr("abs:href"));
        assertTrue(two.hasAttr("abs:href"));
        assertTrue(both.hasAttr("abs:href")); 
    }

// org.jsoup.select.ElementsTest::attr
    @Test public void attr() {
        Document doc = Jsoup.parse("<p title=foo><p title=bar><p class=foo><p class=bar>");
        String classVal = doc.select("p").attr("class");
        assertEquals("foo", classVal);
    }

// org.jsoup.select.ElementsTest::absAttr
    @Test public void absAttr() {
        Document doc = Jsoup.parse("<a id=1 href='/foo'>One</a> <a id=2 href='http://jsoup.org'>Two</a>");
        Elements one = doc.select("#1");
        Elements two = doc.select("#2");
        Elements both = doc.select("a");

        assertEquals("", one.attr("abs:href"));
        assertEquals("http://jsoup.org", two.attr("abs:href"));
        assertEquals("http://jsoup.org", both.attr("abs:href"));
    }

// org.jsoup.select.ElementsTest::classes
    @Test public void classes() {
        Document doc = Jsoup.parse("<div><p class='mellow yellow'></p><p class='red green'></p>");

        Elements els = doc.select("p");
        assertTrue(els.hasClass("red"));
        assertFalse(els.hasClass("blue"));
        els.addClass("blue");
        els.removeClass("yellow");
        els.toggleClass("mellow");

        assertEquals("blue", els.get(0).className());
        assertEquals("red green blue mellow", els.get(1).className());
    }

// org.jsoup.select.ElementsTest::text
    @Test public void text() {
        String h = "<div><p>Hello<p>there<p>world</div>";
        Document doc = Jsoup.parse(h);
        assertEquals("Hello there world", doc.select("div > *").text());
    }

// org.jsoup.select.ElementsTest::hasText
    @Test public void hasText() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div><p></p></div>");
        Elements divs = doc.select("div");
        assertTrue(divs.hasText());
        assertFalse(doc.select("div + div").hasText());
    }

// org.jsoup.select.ElementsTest::html
    @Test public void html() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div><p>There</p></div>");
        Elements divs = doc.select("div");
        assertEquals("<p>Hello</p>\n<p>There</p>", divs.html());
    }

// org.jsoup.select.ElementsTest::outerHtml
    @Test public void outerHtml() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div><p>There</p></div>");
        Elements divs = doc.select("div");
        assertEquals("<div><p>Hello</p></div><div><p>There</p></div>", TextUtil.stripNewlines(divs.outerHtml()));
    }

// org.jsoup.select.ElementsTest::setHtml
    @Test public void setHtml() {
        Document doc = Jsoup.parse("<p>One</p><p>Two</p><p>Three</p>");
        Elements ps = doc.select("p");
        
        ps.prepend("<b>Bold</b>").append("<i>Ital</i>");
        assertEquals("<p><b>Bold</b>Two<i>Ital</i></p>", TextUtil.stripNewlines(ps.get(1).outerHtml()));
        
        ps.html("<span>Gone</span>");
        assertEquals("<p><span>Gone</span></p>", TextUtil.stripNewlines(ps.get(1).outerHtml()));
    }

// org.jsoup.select.ElementsTest::val
    @Test public void val() {
        Document doc = Jsoup.parse("<input value='one' /><textarea>two</textarea>");
        Elements els = doc.select("input, textarea");
        assertEquals(2, els.size());
        assertEquals("one", els.val());
        assertEquals("two", els.last().val());
        
        els.val("three");
        assertEquals("three", els.first().val());
        assertEquals("three", els.last().val());
        assertEquals("<textarea>three</textarea>", els.last().outerHtml());
    }

// org.jsoup.select.ElementsTest::before
    @Test public void before() {
        Document doc = Jsoup.parse("<p>This <a>is</a> <a>jsoup</a>.</p>");
        doc.select("a").before("<span>foo</span>");
        assertEquals("<p>This <span>foo</span><a>is</a> <span>foo</span><a>jsoup</a>.</p>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.select.ElementsTest::after
    @Test public void after() {
        Document doc = Jsoup.parse("<p>This <a>is</a> <a>jsoup</a>.</p>");
        doc.select("a").after("<span>foo</span>");
        assertEquals("<p>This <a>is</a><span>foo</span> <a>jsoup</a><span>foo</span>.</p>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.select.ElementsTest::wrap
    @Test public void wrap() {
        String h = "<p><b>This</b> is <b>jsoup</b></p>";
        Document doc = Jsoup.parse(h);
        doc.select("b").wrap("<i></i>");
        assertEquals("<p><i><b>This</b></i> is <i><b>jsoup</b></i></p>", doc.body().html());
    }

// org.jsoup.select.ElementsTest::unwrap
    @Test public void unwrap() {
        String h = "<div><font>One</font> <font><a href=\"/\">Two</a></font></div";
        Document doc = Jsoup.parse(h);
        doc.select("font").unwrap();
        assertEquals("<div>One <a href=\"/\">Two</a></div>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.select.ElementsTest::empty
    @Test public void empty() {
        Document doc = Jsoup.parse("<div><p>Hello <b>there</b></p> <p>now!</p></div>");
        doc.outputSettings().prettyPrint(false);

        doc.select("p").empty();
        assertEquals("<div><p></p> <p></p></div>", doc.body().html());
    }

// org.jsoup.select.ElementsTest::remove
    @Test public void remove() {
        Document doc = Jsoup.parse("<div><p>Hello <b>there</b></p> jsoup <p>now!</p></div>");
        doc.outputSettings().prettyPrint(false);
        
        doc.select("p").remove();
        assertEquals("<div> jsoup </div>", doc.body().html());
    }

// org.jsoup.select.ElementsTest::eq
    @Test public void eq() {
        String h = "<p>Hello<p>there<p>world";
        Document doc = Jsoup.parse(h);
        assertEquals("there", doc.select("p").eq(1).text());
        assertEquals("there", doc.select("p").get(1).text());
    }

// org.jsoup.select.ElementsTest::is
    @Test public void is() {
        String h = "<p>Hello<p title=foo>there<p>world";
        Document doc = Jsoup.parse(h);
        Elements ps = doc.select("p");
        assertTrue(ps.is("[title=foo]"));
        assertFalse(ps.is("[title=bar]"));
    }

// org.jsoup.select.ElementsTest::parents
    @Test public void parents() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><p>There</p>");
        Elements parents = doc.select("p").parents();

        assertEquals(3, parents.size());
        assertEquals("div", parents.get(0).tagName());
        assertEquals("body", parents.get(1).tagName());
        assertEquals("html", parents.get(2).tagName());
    }

// org.jsoup.select.ElementsTest::not
    @Test public void not() {
        Document doc = Jsoup.parse("<div id=1><p>One</p></div> <div id=2><p><span>Two</span></p></div>");

        Elements div1 = doc.select("div").not(":has(p > span)");
        assertEquals(1, div1.size());
        assertEquals("1", div1.first().id());

        Elements div2 = doc.select("div").not("#1");
        assertEquals(1, div2.size());
        assertEquals("2", div2.first().id());
    }

// org.jsoup.select.ElementsTest::tagNameSet
    @Test public void tagNameSet() {
        Document doc = Jsoup.parse("<p>Hello <i>there</i> <i>now</i></p>");
        doc.select("i").tagName("em");

        assertEquals("<p>Hello <em>there</em> <em>now</em></p>", doc.body().html());
    }

// org.jsoup.select.SelectorTest::testByTag
    @Test public void testByTag() {
        Elements els = Jsoup.parse("<div id=1><div id=2><p>Hello</p></div></div><div id=3>").select("div");
        assertEquals(3, els.size());
        assertEquals("1", els.get(0).id());
        assertEquals("2", els.get(1).id());
        assertEquals("3", els.get(2).id());

        Elements none = Jsoup.parse("<div id=1><div id=2><p>Hello</p></div></div><div id=3>").select("span");
        assertEquals(0, none.size());
    }

// org.jsoup.select.SelectorTest::testById
    @Test public void testById() {
        Elements els = Jsoup.parse("<div><p id=foo>Hello</p><p id=foo>Foo two!</p></div>").select("#foo");
        assertEquals(2, els.size());
        assertEquals("Hello", els.get(0).text());
        assertEquals("Foo two!", els.get(1).text());

        Elements none = Jsoup.parse("<div id=1></div>").select("#foo");
        assertEquals(0, none.size());
    }

// org.jsoup.select.SelectorTest::testByClass
    @Test public void testByClass() {
        Elements els = Jsoup.parse("<p id=0 class='one two'><p id=1 class='one'><p id=2 class='two'>").select("p.one");
        assertEquals(2, els.size());
        assertEquals("0", els.get(0).id());
        assertEquals("1", els.get(1).id());

        Elements none = Jsoup.parse("<div class='one'></div>").select(".foo");
        assertEquals(0, none.size());

        Elements els2 = Jsoup.parse("<div class='One-Two'></div>").select(".one-two");
        assertEquals(1, els2.size());
    }

// org.jsoup.select.SelectorTest::testByAttribute
    @Test public void testByAttribute() {
        String h = "<div Title=Foo /><div Title=Bar /><div Style=Qux /><div title=Bam /><div title=SLAM /><div />";
        Document doc = Jsoup.parse(h);

        Elements withTitle = doc.select("[title]");
        assertEquals(4, withTitle.size());

        Elements foo = doc.select("[title=foo]");
        assertEquals(1, foo.size());

        Elements not = doc.select("div[title!=bar]");
        assertEquals(5, not.size());
        assertEquals("Foo", not.first().attr("title"));

        Elements starts = doc.select("[title^=ba]");
        assertEquals(2, starts.size());
        assertEquals("Bar", starts.first().attr("title"));
        assertEquals("Bam", starts.last().attr("title"));

        Elements ends = doc.select("[title$=am]");
        assertEquals(2, ends.size());
        assertEquals("Bam", ends.first().attr("title"));
        assertEquals("SLAM", ends.last().attr("title"));

        Elements contains = doc.select("[title*=a]");
        assertEquals(3, contains.size());
        assertEquals("Bar", contains.first().attr("title"));
        assertEquals("SLAM", contains.last().attr("title"));
    }

// org.jsoup.select.SelectorTest::testNamespacedTag
    @Test public void testNamespacedTag() {
        Document doc = Jsoup.parse("<div><abc:def id=1>Hello</abc:def></div> <abc:def class=bold id=2>There</abc:def>");
        Elements byTag = doc.select("abc|def");
        assertEquals(2, byTag.size());
        assertEquals("1", byTag.first().id());
        assertEquals("2", byTag.last().id());

        Elements byAttr = doc.select(".bold");
        assertEquals(1, byAttr.size());
        assertEquals("2", byAttr.last().id());

        Elements byTagAttr = doc.select("abc|def.bold");
        assertEquals(1, byTagAttr.size());
        assertEquals("2", byTagAttr.last().id());

        Elements byContains = doc.select("abc|def:contains(e)");
        assertEquals(2, byContains.size());
        assertEquals("1", byContains.first().id());
        assertEquals("2", byContains.last().id());
    }

// org.jsoup.select.SelectorTest::testByAttributeStarting
    @Test public void testByAttributeStarting() {
        Document doc = Jsoup.parse("<div id=1 data-name=jsoup>Hello</div><p data-val=5 id=2>There</p><p id=3>No</p>");
        Elements withData = doc.select("[^data-]");
        assertEquals(2, withData.size());
        assertEquals("1", withData.first().id());
        assertEquals("2", withData.last().id());

        withData = doc.select("p[^data-]");
        assertEquals(1, withData.size());
        assertEquals("2", withData.first().id());
    }

// org.jsoup.select.SelectorTest::testByAttributeRegex
    @Test public void testByAttributeRegex() {
        Document doc = Jsoup.parse("<p><img src=foo.png id=1><img src=bar.jpg id=2><img src=qux.JPEG id=3><img src=old.gif><img></p>");
        Elements imgs = doc.select("img[src~=(?i)\\.(png|jpe?g)]");
        assertEquals(3, imgs.size());
        assertEquals("1", imgs.get(0).id());
        assertEquals("2", imgs.get(1).id());
        assertEquals("3", imgs.get(2).id());
    }

// org.jsoup.select.SelectorTest::testByAttributeRegexCharacterClass
    @Test public void testByAttributeRegexCharacterClass() {
        Document doc = Jsoup.parse("<p><img src=foo.png id=1><img src=bar.jpg id=2><img src=qux.JPEG id=3><img src=old.gif id=4></p>");
        Elements imgs = doc.select("img[src~=[o]]");
        assertEquals(2, imgs.size());
        assertEquals("1", imgs.get(0).id());
        assertEquals("4", imgs.get(1).id());
    }

// org.jsoup.select.SelectorTest::testByAttributeRegexCombined
    @Test public void testByAttributeRegexCombined() {
        Document doc = Jsoup.parse("<div><table class=x><td>Hello</td></table></div>");
        Elements els = doc.select("div table[class~=x|y]");
        assertEquals(1, els.size());
        assertEquals("Hello", els.text());
    }

// org.jsoup.select.SelectorTest::testCombinedWithContains
    @Test public void testCombinedWithContains() {
        Document doc = Jsoup.parse("<p id=1>One</p><p>Two +</p><p>Three +</p>");
        Elements els = doc.select("p#1 + :contains(+)");
        assertEquals(1, els.size());
        assertEquals("Two +", els.text());
        assertEquals("p", els.first().tagName());
    }

// org.jsoup.select.SelectorTest::testAllElements
    @Test public void testAllElements() {
        String h = "<div><p>Hello</p><p><b>there</b></p></div>";
        Document doc = Jsoup.parse(h);
        Elements allDoc = doc.select("*");
        Elements allUnderDiv = doc.select("div *");
        assertEquals(8, allDoc.size());
        assertEquals(3, allUnderDiv.size());
        assertEquals("p", allUnderDiv.first().tagName());
    }

// org.jsoup.select.SelectorTest::testAllWithClass
    @Test public void testAllWithClass() {
        String h = "<p class=first>One<p class=first>Two<p>Three";
        Document doc = Jsoup.parse(h);
        Elements ps = doc.select("*.first");
        assertEquals(2, ps.size());
    }

// org.jsoup.select.SelectorTest::testGroupOr
    @Test public void testGroupOr() {
        String h = "<div title=foo /><div title=bar /><div /><p></p><img /><span title=qux>";
        Document doc = Jsoup.parse(h);
        Elements els = doc.select("p,div,[title]");

        assertEquals(5, els.size());
        assertEquals("div", els.get(0).tagName());
        assertEquals("foo", els.get(0).attr("title"));
        assertEquals("div", els.get(1).tagName());
        assertEquals("bar", els.get(1).attr("title"));
        assertEquals("div", els.get(2).tagName());
        assertTrue(els.get(2).attr("title").length() == 0); 
        assertFalse(els.get(2).hasAttr("title"));
        assertEquals("p", els.get(3).tagName());
        assertEquals("span", els.get(4).tagName());
    }

// org.jsoup.select.SelectorTest::testGroupOrAttribute
    @Test public void testGroupOrAttribute() {
        String h = "<div id=1 /><div id=2 /><div title=foo /><div title=bar />";
        Elements els = Jsoup.parse(h).select("[id],[title=foo]");

        assertEquals(3, els.size());
        assertEquals("1", els.get(0).id());
        assertEquals("2", els.get(1).id());
        assertEquals("foo", els.get(2).attr("title"));
    }

// org.jsoup.select.SelectorTest::descendant
    @Test public void descendant() {
        String h = "<div class=head><p class=first>Hello</p><p>There</p></div><p>None</p>";
        Document doc = Jsoup.parse(h);
        Elements els = doc.select(".head p");
        assertEquals(2, els.size());
        assertEquals("Hello", els.get(0).text());
        assertEquals("There", els.get(1).text());

        Elements p = doc.select("p.first");
        assertEquals(1, p.size());
        assertEquals("Hello", p.get(0).text());

        Elements empty = doc.select("p .first"); 
        assertEquals(0, empty.size());
    }

// org.jsoup.select.SelectorTest::and
    @Test public void and() {
        String h = "<div id=1 class='foo bar' title=bar name=qux><p class=foo title=bar>Hello</p></div";
        Document doc = Jsoup.parse(h);

        Elements div = doc.select("div.foo");
        assertEquals(1, div.size());
        assertEquals("div", div.first().tagName());

        Elements p = doc.select("div .foo"); 
        assertEquals(1, p.size());
        assertEquals("p", p.first().tagName());

        Elements div2 = doc.select("div#1.foo.bar[title=bar][name=qux]"); 
        assertEquals(1, div2.size());
        assertEquals("div", div2.first().tagName());

        Elements p2 = doc.select("div *.foo"); 
        assertEquals(1, p2.size());
        assertEquals("p", p2.first().tagName());
    }

// org.jsoup.select.SelectorTest::deeperDescendant
    @Test public void deeperDescendant() {
        String h = "<div class=head><p><span class=first>Hello</div><div class=head><p class=first><span>Another</span><p>Again</div>";
        Elements els = Jsoup.parse(h).select("div p .first");
        assertEquals(1, els.size());
        assertEquals("Hello", els.first().text());
        assertEquals("span", els.first().tagName());
    }

// org.jsoup.select.SelectorTest::parentChildElement
    @Test public void parentChildElement() {
        String h = "<div id=1><div id=2><div id = 3></div></div></div><div id=4></div>";
        Document doc = Jsoup.parse(h);

        Elements divs = doc.select("div > div");
        assertEquals(2, divs.size());
        assertEquals("2", divs.get(0).id()); 
        assertEquals("3", divs.get(1).id()); 

        Elements div2 = doc.select("div#1 > div");
        assertEquals(1, div2.size());
        assertEquals("2", div2.get(0).id());
    }

// org.jsoup.select.SelectorTest::parentWithClassChild
    @Test public void parentWithClassChild() {
        String h = "<h1 class=foo><a href=1 /></h1><h1 class=foo><a href=2 class=bar /></h1><h1><a href=3 /></h1>";
        Document doc = Jsoup.parse(h);

        Elements allAs = doc.select("h1 > a");
        assertEquals(3, allAs.size());
        assertEquals("a", allAs.first().tagName());

        Elements fooAs = doc.select("h1.foo > a");
        assertEquals(2, fooAs.size());
        assertEquals("a", fooAs.first().tagName());

        Elements barAs = doc.select("h1.foo > a.bar");
        assertEquals(1, barAs.size());
    }

// org.jsoup.select.SelectorTest::parentChildStar
    @Test public void parentChildStar() {
        String h = "<div id=1><p>Hello<p><b>there</b></p></div><div id=2><span>Hi</span></div>";
        Document doc = Jsoup.parse(h);
        Elements divChilds = doc.select("div > *");
        assertEquals(3, divChilds.size());
        assertEquals("p", divChilds.get(0).tagName());
        assertEquals("p", divChilds.get(1).tagName());
        assertEquals("span", divChilds.get(2).tagName());
    }

// org.jsoup.select.SelectorTest::multiChildDescent
    @Test public void multiChildDescent() {
        String h = "<div id=foo><h1 class=bar><a href=http://example.com/>One</a></h1></div>";
        Document doc = Jsoup.parse(h);
        Elements els = doc.select("div#foo > h1.bar > a[href*=example]");
        assertEquals(1, els.size());
        assertEquals("a", els.first().tagName());
    }

// org.jsoup.select.SelectorTest::caseInsensitive
    @Test public void caseInsensitive() {
        String h = "<dIv tItle=bAr><div>"; 
        Document doc = Jsoup.parse(h);

        assertEquals(2, doc.select("DIV").size());
        assertEquals(1, doc.select("DIV[TITLE]").size());
        assertEquals(1, doc.select("DIV[TITLE=BAR]").size());
        assertEquals(0, doc.select("DIV[TITLE=BARBARELLA").size());
    }

// org.jsoup.select.SelectorTest::adjacentSiblings
    @Test public void adjacentSiblings() {
        String h = "<ol><li>One<li>Two<li>Three</ol>";
        Document doc = Jsoup.parse(h);
        Elements sibs = doc.select("li + li");
        assertEquals(2, sibs.size());
        assertEquals("Two", sibs.get(0).text());
        assertEquals("Three", sibs.get(1).text());
    }

// org.jsoup.select.SelectorTest::adjacentSiblingsWithId
    @Test public void adjacentSiblingsWithId() {
        String h = "<ol><li id=1>One<li id=2>Two<li id=3>Three</ol>";
        Document doc = Jsoup.parse(h);
        Elements sibs = doc.select("li#1 + li#2");
        assertEquals(1, sibs.size());
        assertEquals("Two", sibs.get(0).text());
    }

// org.jsoup.select.SelectorTest::notAdjacent
    @Test public void notAdjacent() {
        String h = "<ol><li id=1>One<li id=2>Two<li id=3>Three</ol>";
        Document doc = Jsoup.parse(h);
        Elements sibs = doc.select("li#1 + li#3");
        assertEquals(0, sibs.size());
    }

// org.jsoup.select.SelectorTest::mixCombinator
    @Test public void mixCombinator() {
        String h = "<div class=foo><ol><li>One<li>Two<li>Three</ol></div>";
        Document doc = Jsoup.parse(h);
        Elements sibs = doc.select("body > div.foo li + li");

        assertEquals(2, sibs.size());
        assertEquals("Two", sibs.get(0).text());
        assertEquals("Three", sibs.get(1).text());
    }

// org.jsoup.select.SelectorTest::mixCombinatorGroup
    @Test public void mixCombinatorGroup() {
        String h = "<div class=foo><ol><li>One<li>Two<li>Three</ol></div>";
        Document doc = Jsoup.parse(h);
        Elements els = doc.select(".foo > ol, ol > li + li");

        assertEquals(3, els.size());
        assertEquals("ol", els.get(0).tagName());
        assertEquals("Two", els.get(1).text());
        assertEquals("Three", els.get(2).text());
    }

// org.jsoup.select.SelectorTest::generalSiblings
    @Test public void generalSiblings() {
        String h = "<ol><li id=1>One<li id=2>Two<li id=3>Three</ol>";
        Document doc = Jsoup.parse(h);
        Elements els = doc.select("#1 ~ #3");
        assertEquals(1, els.size());
        assertEquals("Three", els.first().text());
    }

// org.jsoup.select.SelectorTest::testCharactersInIdAndClass
    @Test public void testCharactersInIdAndClass() {
        
        String h = "<div><p id='a1-foo_bar'>One</p><p class='b2-qux_bif'>Two</p></div>";
        Document doc = Jsoup.parse(h);

        Element el1 = doc.getElementById("a1-foo_bar");
        assertEquals("One", el1.text());
        Element el2 = doc.getElementsByClass("b2-qux_bif").first();
        assertEquals("Two", el2.text());

        Element el3 = doc.select("#a1-foo_bar").first();
        assertEquals("One", el3.text());
        Element el4 = doc.select(".b2-qux_bif").first();
        assertEquals("Two", el4.text());
    }

// org.jsoup.select.SelectorTest::testSupportsLeadingCombinator
    @Test public void testSupportsLeadingCombinator() {
        String h = "<div><p><span>One</span><span>Two</span></p></div>";
        Document doc = Jsoup.parse(h);

        Element p = doc.select("div > p").first();
        Elements spans = p.select("> span");
        assertEquals(2, spans.size());
        assertEquals("One", spans.first().text());

        
        h = "<div id=1><div id=2><div id=3></div></div></div>";
        doc = Jsoup.parse(h);
        Element div = doc.select("div").select(" > div").first();
        assertEquals("2", div.id());
    }

// org.jsoup.select.SelectorTest::testPseudoLessThan
    @Test public void testPseudoLessThan() {
        Document doc = Jsoup.parse("<div><p>One</p><p>Two</p><p>Three</>p></div><div><p>Four</p>");
        Elements ps = doc.select("div p:lt(2)");
        assertEquals(3, ps.size());
        assertEquals("One", ps.get(0).text());
        assertEquals("Two", ps.get(1).text());
        assertEquals("Four", ps.get(2).text());
    }

// org.jsoup.select.SelectorTest::testPseudoGreaterThan
    @Test public void testPseudoGreaterThan() {
        Document doc = Jsoup.parse("<div><p>One</p><p>Two</p><p>Three</p></div><div><p>Four</p>");
        Elements ps = doc.select("div p:gt(0)");
        assertEquals(2, ps.size());
        assertEquals("Two", ps.get(0).text());
        assertEquals("Three", ps.get(1).text());
    }

// org.jsoup.select.SelectorTest::testPseudoEquals
    @Test public void testPseudoEquals() {
        Document doc = Jsoup.parse("<div><p>One</p><p>Two</p><p>Three</>p></div><div><p>Four</p>");
        Elements ps = doc.select("div p:eq(0)");
        assertEquals(2, ps.size());
        assertEquals("One", ps.get(0).text());
        assertEquals("Four", ps.get(1).text());

        Elements ps2 = doc.select("div:eq(0) p:eq(0)");
        assertEquals(1, ps2.size());
        assertEquals("One", ps2.get(0).text());
        assertEquals("p", ps2.get(0).tagName());
    }

// org.jsoup.select.SelectorTest::testPseudoBetween
    @Test public void testPseudoBetween() {
        Document doc = Jsoup.parse("<div><p>One</p><p>Two</p><p>Three</>p></div><div><p>Four</p>");
        Elements ps = doc.select("div p:gt(0):lt(2)");
        assertEquals(1, ps.size());
        assertEquals("Two", ps.get(0).text());
    }

// org.jsoup.select.SelectorTest::testPseudoCombined
    @Test public void testPseudoCombined() {
        Document doc = Jsoup.parse("<div class='foo'><p>One</p><p>Two</p></div><div><p>Three</p><p>Four</p></div>");
        Elements ps = doc.select("div.foo p:gt(0)");
        assertEquals(1, ps.size());
        assertEquals("Two", ps.get(0).text());
    }

// org.jsoup.select.SelectorTest::testPseudoHas
    @Test public void testPseudoHas() {
        Document doc = Jsoup.parse("<div id=0><p><span>Hello</span></p></div> <div id=1><span class=foo>There</span></div> <div id=2><p>Not</p></div>");

        Elements divs1 = doc.select("div:has(span)");
        assertEquals(2, divs1.size());
        assertEquals("0", divs1.get(0).id());
        assertEquals("1", divs1.get(1).id());

        Elements divs2 = doc.select("div:has([class]");
        assertEquals(1, divs2.size());
        assertEquals("1", divs2.get(0).id());

        Elements divs3 = doc.select("div:has(span, p)");
        assertEquals(3, divs3.size());
        assertEquals("0", divs3.get(0).id());
        assertEquals("1", divs3.get(1).id());
        assertEquals("2", divs3.get(2).id());

        Elements els1 = doc.body().select(":has(p)");
        assertEquals(3, els1.size()); 
        assertEquals("body", els1.first().tagName());
        assertEquals("0", els1.get(1).id());
        assertEquals("2", els1.get(2).id());
    }

// org.jsoup.select.SelectorTest::testNestedHas
    @Test public void testNestedHas() {
        Document doc = Jsoup.parse("<div><p><span>One</span></p></div> <div><p>Two</p></div>");
        Elements divs = doc.select("div:has(p:has(span))");
        assertEquals(1, divs.size());
        assertEquals("One", divs.first().text());

        
        divs = doc.select("div:has(p:matches((?i)two))");
        assertEquals(1, divs.size());
        assertEquals("div", divs.first().tagName());
        assertEquals("Two", divs.first().text());

        
        divs = doc.select("div:has(p:contains(two))");
        assertEquals(1, divs.size());
        assertEquals("div", divs.first().tagName());
        assertEquals("Two", divs.first().text());
    }

// org.jsoup.select.SelectorTest::testPseudoContains
    @Test public void testPseudoContains() {
        Document doc = Jsoup.parse("<div><p>The Rain.</p> <p class=light>The <i>rain</i>.</p> <p>Rain, the.</p></div>");

        Elements ps1 = doc.select("p:contains(Rain)");
        assertEquals(3, ps1.size());

        Elements ps2 = doc.select("p:contains(the rain)");
        assertEquals(2, ps2.size());
        assertEquals("The Rain.", ps2.first().html());
        assertEquals("The <i>rain</i>.", ps2.last().html());

        Elements ps3 = doc.select("p:contains(the Rain):has(i)");
        assertEquals(1, ps3.size());
        assertEquals("light", ps3.first().className());

        Elements ps4 = doc.select(".light:contains(rain)");
        assertEquals(1, ps4.size());
        assertEquals("light", ps3.first().className());

        Elements ps5 = doc.select(":contains(rain)");
        assertEquals(8, ps5.size()); 
    }

// org.jsoup.select.SelectorTest::testPsuedoContainsWithParentheses
    @Test public void testPsuedoContainsWithParentheses() {
        Document doc = Jsoup.parse("<div><p id=1>This (is good)</p><p id=2>This is bad)</p>");

        Elements ps1 = doc.select("p:contains(this (is good))");
        assertEquals(1, ps1.size());
        assertEquals("1", ps1.first().id());

        Elements ps2 = doc.select("p:contains(this is bad\\))");
        assertEquals(1, ps2.size());
        assertEquals("2", ps2.first().id());
    }

// org.jsoup.select.SelectorTest::containsOwn
    @Test public void containsOwn() {
        Document doc = Jsoup.parse("<p id=1>Hello <b>there</b> now</p>");
        Elements ps = doc.select("p:containsOwn(Hello now)");
        assertEquals(1, ps.size());
        assertEquals("1", ps.first().id());

        assertEquals(0, doc.select("p:containsOwn(there)").size());
    }

// org.jsoup.select.SelectorTest::testMatches
    @Test public void testMatches() {
        Document doc = Jsoup.parse("<p id=1>The <i>Rain</i></p> <p id=2>There are 99 bottles.</p> <p id=3>Harder (this)</p> <p id=4>Rain</p>");

        Elements p1 = doc.select("p:matches(The rain)"); 
        assertEquals(0, p1.size());

        Elements p2 = doc.select("p:matches((?i)the rain)"); 
        assertEquals(1, p2.size());
        assertEquals("1", p2.first().id());

        Elements p4 = doc.select("p:matches((?i)^rain$)"); 
        assertEquals(1, p4.size());
        assertEquals("4", p4.first().id());

        Elements p5 = doc.select("p:matches(\\d+)");
        assertEquals(1, p5.size());
        assertEquals("2", p5.first().id());

        Elements p6 = doc.select("p:matches(\\w+\\s+\\(\\w+\\))"); 
        assertEquals(1, p6.size());
        assertEquals("3", p6.first().id());

        Elements p7 = doc.select("p:matches((?i)the):has(i)"); 
        assertEquals(1, p7.size());
        assertEquals("1", p7.first().id());
    }

// org.jsoup.select.SelectorTest::matchesOwn
    @Test public void matchesOwn() {
        Document doc = Jsoup.parse("<p id=1>Hello <b>there</b> now</p>");

        Elements p1 = doc.select("p:matchesOwn((?i)hello now)");
        assertEquals(1, p1.size());
        assertEquals("1", p1.first().id());

        assertEquals(0, doc.select("p:matchesOwn(there)").size());
    }

// org.jsoup.select.SelectorTest::testRelaxedTags
    @Test public void testRelaxedTags() {
        Document doc = Jsoup.parse("<abc_def id=1>Hello</abc_def> <abc-def id=2>There</abc-def>");

        Elements el1 = doc.select("abc_def");
        assertEquals(1, el1.size());
        assertEquals("1", el1.first().id());

        Elements el2 = doc.select("abc-def");
        assertEquals(1, el2.size());
        assertEquals("2", el2.first().id());
    }

// org.jsoup.select.SelectorTest::notParas
    @Test public void notParas() {
        Document doc = Jsoup.parse("<p id=1>One</p> <p>Two</p> <p><span>Three</span></p>");

        Elements el1 = doc.select("p:not([id=1])");
        assertEquals(2, el1.size());
        assertEquals("Two", el1.first().text());
        assertEquals("Three", el1.last().text());

        Elements el2 = doc.select("p:not(:has(span))");
        assertEquals(2, el2.size());
        assertEquals("One", el2.first().text());
        assertEquals("Two", el2.last().text());
    }

// org.jsoup.select.SelectorTest::notAll
    @Test public void notAll() {
        Document doc = Jsoup.parse("<p>Two</p> <p><span>Three</span></p>");

        Elements el1 = doc.body().select(":not(p)"); 
        assertEquals(2, el1.size());
        assertEquals("body", el1.first().tagName());
        assertEquals("span", el1.last().tagName());
    }

// org.jsoup.select.SelectorTest::notClass
    @Test public void notClass() {
        Document doc = Jsoup.parse("<div class=left>One</div><div class=right id=1><p>Two</p></div>");

        Elements el1 = doc.select("div:not(.left)");
        assertEquals(1, el1.size());
        assertEquals("1", el1.first().id());
    }

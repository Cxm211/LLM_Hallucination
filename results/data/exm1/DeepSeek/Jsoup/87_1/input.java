// buggy code
    public String tagName() {
        return tag.getName();
    }

    Element getFromStack(String elName) {
        for (int pos = stack.size() -1; pos >= 0; pos--) {
            Element next = stack.get(pos);
            if (next.nodeName().equals(elName)) {
                return next;
            }
        }
        return null;
    }

    void popStackToClose(String elName) {
        for (int pos = stack.size() -1; pos >= 0; pos--) {
            Element next = stack.get(pos);
            stack.remove(pos);
            if (next.nodeName().equals(elName))
                break;
        }
    }

    void popStackToClose(String... elNames) {
        for (int pos = stack.size() -1; pos >= 0; pos--) {
            Element next = stack.get(pos);
            stack.remove(pos);
            if (inSorted(next.nodeName(), elNames))
                break;
        }
    }

    void popStackToBefore(String elName) {
        for (int pos = stack.size() -1; pos >= 0; pos--) {
            Element next = stack.get(pos);
            if (next.nodeName().equals(elName)) {
                break;
            } else {
                stack.remove(pos);
            }
        }
    }

    private void clearStackToContext(String... nodeNames) {
        for (int pos = stack.size() -1; pos >= 0; pos--) {
            Element next = stack.get(pos);
            if (StringUtil.in(next.nodeName(), nodeNames) || next.nodeName().equals("html"))
                break;
            else
                stack.remove(pos);
        }
    }

    void resetInsertionMode() {
        boolean last = false;
        for (int pos = stack.size() -1; pos >= 0; pos--) {
            Element node = stack.get(pos);
            if (pos == 0) {
                last = true;
                node = contextElement;
            }
            String name = node.nodeName();
            if ("select".equals(name)) {
                transition(HtmlTreeBuilderState.InSelect);
                break; // frag
            } else if (("td".equals(name) || "th".equals(name) && !last)) {
                transition(HtmlTreeBuilderState.InCell);
                break;
            } else if ("tr".equals(name)) {
                transition(HtmlTreeBuilderState.InRow);
                break;
            } else if ("tbody".equals(name) || "thead".equals(name) || "tfoot".equals(name)) {
                transition(HtmlTreeBuilderState.InTableBody);
                break;
            } else if ("caption".equals(name)) {
                transition(HtmlTreeBuilderState.InCaption);
                break;
            } else if ("colgroup".equals(name)) {
                transition(HtmlTreeBuilderState.InColumnGroup);
                break; // frag
            } else if ("table".equals(name)) {
                transition(HtmlTreeBuilderState.InTable);
                break;
            } else if ("head".equals(name)) {
                transition(HtmlTreeBuilderState.InBody);
                break; // frag
            } else if ("body".equals(name)) {
                transition(HtmlTreeBuilderState.InBody);
                break;
            } else if ("frameset".equals(name)) {
                transition(HtmlTreeBuilderState.InFrameset);
                break; // frag
            } else if ("html".equals(name)) {
                transition(HtmlTreeBuilderState.BeforeHead);
                break; // frag
            } else if (last) {
                transition(HtmlTreeBuilderState.InBody);
                break; // frag
            }
        }
    }

    private boolean inSpecificScope(String[] targetNames, String[] baseTypes, String[] extraTypes) {
        // https://html.spec.whatwg.org/multipage/parsing.html#has-an-element-in-the-specific-scope
        final int bottom = stack.size() -1;
        final int top = bottom > MaxScopeSearchDepth ? bottom - MaxScopeSearchDepth : 0;
        // don't walk too far up the tree

        for (int pos = bottom; pos >= top; pos--) {
            final String elName = stack.get(pos).nodeName();
            if (inSorted(elName, targetNames))
                return true;
            if (inSorted(elName, baseTypes))
                return false;
            if (extraTypes != null && inSorted(elName, extraTypes))
                return false;
        }
        //Validate.fail("Should not be reachable"); // would end up false because hitting 'html' at root (basetypes)
        return false;
    }

    boolean inSelectScope(String targetName) {
        for (int pos = stack.size() -1; pos >= 0; pos--) {
            Element el = stack.get(pos);
            String elName = el.nodeName();
            if (elName.equals(targetName))
                return true;
            if (!inSorted(elName, TagSearchSelectScope)) // all elements except
                return false;
        }
        Validate.fail("Should not be reachable");
        return false;
    }

    void generateImpliedEndTags(String excludeTag) {
        while ((excludeTag != null && !currentElement().nodeName().equals(excludeTag)) &&
                inSorted(currentElement().nodeName(), TagSearchEndTags))
            pop();
    }

    boolean isSpecial(Element el) {
        // todo: mathml's mi, mo, mn
        // todo: svg's foreigObject, desc, title
        String name = el.nodeName();
        return inSorted(name, TagSearchSpecial);
    }

    private boolean isSameFormattingElement(Element a, Element b) {
        // same if: same namespace, tag, and attributes. Element.equals only checks tag, might in future check children
        return a.nodeName().equals(b.nodeName()) &&
                // a.namespace().equals(b.namespace()) &&
                a.attributes().equals(b.attributes());
        // todo: namespaces
    }

    void reconstructFormattingElements() {
        Element last = lastFormattingElement();
        if (last == null || onStack(last))
            return;

        Element entry = last;
        int size = formattingElements.size();
        int pos = size - 1;
        boolean skip = false;
        while (true) {
            if (pos == 0) { // step 4. if none before, skip to 8
                skip = true;
                break;
            }
            entry = formattingElements.get(--pos); // step 5. one earlier than entry
            if (entry == null || onStack(entry)) // step 6 - neither marker nor on stack
                break; // jump to 8, else continue back to 4
        }
        while(true) {
            if (!skip) // step 7: on later than entry
                entry = formattingElements.get(++pos);
            Validate.notNull(entry); // should not occur, as we break at last element

            // 8. create new element from element, 9 insert into current node, onto stack
            skip = false; // can only skip increment from 4.
            Element newEl = insertStartTag(entry.nodeName());
            // newEl.namespace(entry.namespace()); // todo: namespaces
            newEl.attributes().addAll(entry.attributes());

            // 10. replace entry with new entry
            formattingElements.set(pos, newEl);

            // 11
            if (pos == size-1) // if not last entry in list, jump to 7
                break;
        }
    }

    Element getActiveFormattingElement(String nodeName) {
        for (int pos = formattingElements.size() -1; pos >= 0; pos--) {
            Element next = formattingElements.get(pos);
            if (next == null) // scope marker
                break;
            else if (next.nodeName().equals(nodeName))
                return next;
        }
        return null;
    }

        boolean process(Token t, HtmlTreeBuilder tb) {
            switch (t.type) {
                case Character: {
                    Token.Character c = t.asCharacter();
                    if (c.getData().equals(nullString)) {
                        // todo confirm that check
                        tb.error(this);
                        return false;
                    } else if (tb.framesetOk() && isWhitespace(c)) { // don't check if whitespace if frames already closed
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
                    // todo - refactor to a switch statement
                    String name = startTag.normalName();
                    if (name.equals("a")) {
                        if (tb.getActiveFormattingElement("a") != null) {
                            tb.error(this);
                            tb.processEndTag("a");

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
                    } else if (StringUtil.inSorted(name, Constants.InBodyStartEmptyFormatters)) {
                        tb.reconstructFormattingElements();
                        tb.insertEmpty(startTag);
                        tb.framesetOk(false);
                    } else if (StringUtil.inSorted(name, Constants.InBodyStartPClosers)) {
                        if (tb.inButtonScope("p")) {
                            tb.processEndTag("p");
                        }
                        tb.insert(startTag);
                    } else if (name.equals("span")) {
                        // same as final else, but short circuits lots of checks
                        tb.reconstructFormattingElements();
                        tb.insert(startTag);
                    } else if (name.equals("li")) {
                        tb.framesetOk(false);
                        ArrayList<Element> stack = tb.getStack();
                        for (int i = stack.size() - 1; i > 0; i--) {
                            Element el = stack.get(i);
                            if (el.nodeName().equals("li")) {
                                tb.processEndTag("li");
                                break;
                            }
                            if (tb.isSpecial(el) && !StringUtil.inSorted(el.nodeName(), Constants.InBodyStartLiBreakers))
                                break;
                        }
                        if (tb.inButtonScope("p")) {
                            tb.processEndTag("p");
                        }
                        tb.insert(startTag);
                    } else if (name.equals("html")) {
                        tb.error(this);
                        // merge attributes onto real html
                        Element html = tb.getStack().get(0);
                        for (Attribute attribute : startTag.getAttributes()) {
                            if (!html.hasAttr(attribute.getKey()))
                                html.attributes().put(attribute);
                        }
                    } else if (StringUtil.inSorted(name, Constants.InBodyStartToHead)) {
                        return tb.process(t, InHead);
                    } else if (name.equals("body")) {
                        tb.error(this);
                        ArrayList<Element> stack = tb.getStack();
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
                        ArrayList<Element> stack = tb.getStack();
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
                                stack.remove(stack.size()-1);
                            tb.insert(startTag);
                            tb.transition(InFrameset);
                        }
                    } else if (StringUtil.inSorted(name, Constants.Headings)) {
                        if (tb.inButtonScope("p")) {
                            tb.processEndTag("p");
                        }
                        if (StringUtil.inSorted(tb.currentElement().nodeName(), Constants.Headings)) {
                            tb.error(this);
                            tb.pop();
                        }
                        tb.insert(startTag);
                    } else if (StringUtil.inSorted(name, Constants.InBodyStartPreListing)) {
                        if (tb.inButtonScope("p")) {
                            tb.processEndTag("p");
                        }
                        tb.insert(startTag);
                        tb.reader.matchConsume("\n"); // ignore LF if next token
                        tb.framesetOk(false);
                    } else if (name.equals("form")) {
                        if (tb.getFormElement() != null) {
                            tb.error(this);
                            return false;
                        }
                        if (tb.inButtonScope("p")) {
                            tb.processEndTag("p");
                        }
                        tb.insertForm(startTag, true);
                    } else if (StringUtil.inSorted(name, Constants.DdDt)) {
                        tb.framesetOk(false);
                        ArrayList<Element> stack = tb.getStack();
                        for (int i = stack.size() - 1; i > 0; i--) {
                            Element el = stack.get(i);
                            if (StringUtil.inSorted(el.nodeName(), Constants.DdDt)) {
                                tb.processEndTag(el.nodeName());
                                break;
                            }
                            if (tb.isSpecial(el) && !StringUtil.inSorted(el.nodeName(), Constants.InBodyStartLiBreakers))
                                break;
                        }
                        if (tb.inButtonScope("p")) {
                            tb.processEndTag("p");
                        }
                        tb.insert(startTag);
                    } else if (name.equals("plaintext")) {
                        if (tb.inButtonScope("p")) {
                            tb.processEndTag("p");
                        }
                        tb.insert(startTag);
                        tb.tokeniser.transition(TokeniserState.PLAINTEXT); // once in, never gets out
                    } else if (name.equals("button")) {
                        if (tb.inButtonScope("button")) {
                            // close and reprocess
                            tb.error(this);
                            tb.processEndTag("button");
                            tb.process(startTag);
                        } else {
                            tb.reconstructFormattingElements();
                            tb.insert(startTag);
                            tb.framesetOk(false);
                        }
                    } else if (StringUtil.inSorted(name, Constants.Formatters)) {
                        tb.reconstructFormattingElements();
                        Element el = tb.insert(startTag);
                        tb.pushActiveFormattingElements(el);
                    } else if (name.equals("nobr")) {
                        tb.reconstructFormattingElements();
                        if (tb.inScope("nobr")) {
                            tb.error(this);
                            tb.processEndTag("nobr");
                            tb.reconstructFormattingElements();
                        }
                        Element el = tb.insert(startTag);
                        tb.pushActiveFormattingElements(el);
                    } else if (StringUtil.inSorted(name, Constants.InBodyStartApplets)) {
                        tb.reconstructFormattingElements();
                        tb.insert(startTag);
                        tb.insertMarkerToFormattingElements();
                        tb.framesetOk(false);
                    } else if (name.equals("table")) {
                        if (tb.getDocument().quirksMode() != Document.QuirksMode.quirks && tb.inButtonScope("p")) {
                            tb.processEndTag("p");
                        }
                        tb.insert(startTag);
                        tb.framesetOk(false);
                        tb.transition(InTable);
                    } else if (name.equals("input")) {
                        tb.reconstructFormattingElements();
                        Element el = tb.insertEmpty(startTag);
                        if (!el.attr("type").equalsIgnoreCase("hidden"))
                            tb.framesetOk(false);
                    } else if (StringUtil.inSorted(name, Constants.InBodyStartMedia)) {
                        tb.insertEmpty(startTag);
                    } else if (name.equals("hr")) {
                        if (tb.inButtonScope("p")) {
                            tb.processEndTag("p");
                        }
                        tb.insertEmpty(startTag);
                        tb.framesetOk(false);
                    } else if (name.equals("image")) {
                        if (tb.getFromStack("svg") == null)
                            return tb.process(startTag.name("img")); // change <image> to <img>, unless in svg
                        else
                            tb.insert(startTag);
                    } else if (name.equals("isindex")) {
                        // how much do we care about the early 90s?
                        tb.error(this);
                        if (tb.getFormElement() != null)
                            return false;

                        tb.processStartTag("form");
                        if (startTag.attributes.hasKey("action")) {
                            Element form = tb.getFormElement();
                            form.attr("action", startTag.attributes.get("action"));
                        }
                        tb.processStartTag("hr");
                        tb.processStartTag("label");
                        // hope you like english.
                        String prompt = startTag.attributes.hasKey("prompt") ?
                                startTag.attributes.get("prompt") :
                                "This is a searchable index. Enter search keywords: ";

                        tb.process(new Token.Character().data(prompt));

                        // input
                        Attributes inputAttribs = new Attributes();
                        for (Attribute attr : startTag.attributes) {
                            if (!StringUtil.inSorted(attr.getKey(), Constants.InBodyStartInputAttribs))
                                inputAttribs.put(attr);
                        }
                        inputAttribs.put("name", "isindex");
                        tb.processStartTag("input", inputAttribs);
                        tb.processEndTag("label");
                        tb.processStartTag("hr");
                        tb.processEndTag("form");
                    } else if (name.equals("textarea")) {
                        tb.insert(startTag);
                        // todo: If the next token is a U+000A LINE FEED (LF) character token, then ignore that token and move on to the next one. (Newlines at the start of textarea elements are ignored as an authoring convenience.)
                        tb.tokeniser.transition(TokeniserState.Rcdata);
                        tb.markInsertionMode();
                        tb.framesetOk(false);
                        tb.transition(Text);
                    } else if (name.equals("xmp")) {
                        if (tb.inButtonScope("p")) {
                            tb.processEndTag("p");
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

                        HtmlTreeBuilderState state = tb.state();
                        if (state.equals(InTable) || state.equals(InCaption) || state.equals(InTableBody) || state.equals(InRow) || state.equals(InCell))
                            tb.transition(InSelectInTable);
                        else
                            tb.transition(InSelect);
                    } else if (StringUtil.inSorted(name, Constants.InBodyStartOptions)) {
                        if (tb.currentElement().nodeName().equals("option"))
                            tb.processEndTag("option");
                        tb.reconstructFormattingElements();
                        tb.insert(startTag);
                    } else if (StringUtil.inSorted(name, Constants.InBodyStartRuby)) {
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
                    } else if (name.equals("svg")) {
                        tb.reconstructFormattingElements();
                        // todo: handle A start tag whose tag name is "svg" (xlink, svg)
                        tb.insert(startTag);
                    } else if (StringUtil.inSorted(name, Constants.InBodyStartDrop)) {
                        tb.error(this);
                        return false;
                    } else {
                        tb.reconstructFormattingElements();
                        tb.insert(startTag);
                    }
                    break;

                case EndTag:
                    Token.EndTag endTag = t.asEndTag();
                    name = endTag.normalName();
                    if (StringUtil.inSorted(name, Constants.InBodyEndAdoptionFormatters)) {
                        // Adoption Agency Algorithm.
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
                            ArrayList<Element> stack = tb.getStack();
                            // the spec doesn't limit to < 64, but in degenerate cases (9000+ stack depth) this prevents
                            // run-aways
                            final int stackSize = stack.size();
                            for (int si = 0; si < stackSize && si < 64; si++) {
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
                            for (int j = 0; j < 3; j++) {
                                if (tb.onStack(node))
                                    node = tb.aboveOnStack(node);
                                if (!tb.isInActiveFormattingElements(node)) { // note no bookmark check
                                    tb.removeFromStack(node);
                                    continue;
                                } else if (node == formatEl)
                                    break;

                                Element replacement = new Element(Tag.valueOf(node.nodeName(), ParseSettings.preserveCase), tb.getBaseUri());
                                // case will follow the original node (so honours ParseSettings)
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

                            if (StringUtil.inSorted(commonAncestor.nodeName(), Constants.InBodyEndTableFosters)) {
                                if (lastNode.parent() != null)
                                    lastNode.remove();
                                tb.insertInFosterParent(lastNode);
                            } else {
                                if (lastNode.parent() != null)
                                    lastNode.remove();
                                commonAncestor.appendChild(lastNode);
                            }

                            Element adopter = new Element(formatEl.tag(), tb.getBaseUri());
                            adopter.attributes().addAll(formatEl.attributes());
                            Node[] childNodes = furthestBlock.childNodes().toArray(new Node[furthestBlock.childNodeSize()]);
                            for (Node childNode : childNodes) {
                                adopter.appendChild(childNode); // append will reparent. thus the clone to avoid concurrent mod.
                            }
                            furthestBlock.appendChild(adopter);
                            tb.removeFromActiveFormattingElements(formatEl);
                            // todo: insert the new element into the list of active formatting elements at the position of the aforementioned bookmark.
                            tb.removeFromStack(formatEl);
                            tb.insertOnStackAfter(furthestBlock, adopter);
                        }
                    } else if (StringUtil.inSorted(name, Constants.InBodyEndClosers)) {
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
                    } else if (name.equals("span")) {
                        // same as final fall through, but saves short circuit
                        return anyOtherEndTag(t, tb);
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
                    } else if (name.equals("body")) {
                        if (!tb.inScope("body")) {
                            tb.error(this);
                            return false;
                        } else {
                            // todo: error if stack contains something not dd, dt, li, optgroup, option, p, rp, rt, tbody, td, tfoot, th, thead, tr, body, html
                            tb.transition(AfterBody);
                        }
                    } else if (name.equals("html")) {
                        boolean notIgnored = tb.processEndTag("body");
                        if (notIgnored)
                            return tb.process(endTag);
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
                            tb.processStartTag(name); // if no p to close, creates an empty <p></p>
                            return tb.process(endTag);
                        } else {
                            tb.generateImpliedEndTags(name);
                            if (!tb.currentElement().nodeName().equals(name))
                                tb.error(this);
                            tb.popStackToClose(name);
                        }
                    } else if (StringUtil.inSorted(name, Constants.DdDt)) {
                        if (!tb.inScope(name)) {
                            tb.error(this);
                            return false;
                        } else {
                            tb.generateImpliedEndTags(name);
                            if (!tb.currentElement().nodeName().equals(name))
                                tb.error(this);
                            tb.popStackToClose(name);
                        }
                    } else if (StringUtil.inSorted(name, Constants.Headings)) {
                        if (!tb.inScope(Constants.Headings)) {
                            tb.error(this);
                            return false;
                        } else {
                            tb.generateImpliedEndTags(name);
                            if (!tb.currentElement().nodeName().equals(name))
                                tb.error(this);
                            tb.popStackToClose(Constants.Headings);
                        }
                    } else if (name.equals("sarcasm")) {
                        // *sigh*
                        return anyOtherEndTag(t, tb);
                    } else if (StringUtil.inSorted(name, Constants.InBodyStartApplets)) {
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
                        tb.processStartTag("br");
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

        boolean anyOtherEndTag(Token t, HtmlTreeBuilder tb) {
            String name = tb.settings.normalizeTag(t.asEndTag().name());
            ArrayList<Element> stack = tb.getStack();
            for (int pos = stack.size() -1; pos >= 0; pos--) {
                Element node = stack.get(pos);
                if (node.nodeName().equals(name)) {
                    tb.generateImpliedEndTags(name);
                    if (!name.equals(tb.currentElement().nodeName()))
                        tb.error(this);
                    tb.popStackToClose(name);
                    break;
                } else {
                    if (tb.isSpecial(node)) {
                        tb.error(this);
                        return false;
                    }
                }
            }
            return true;
        }

        boolean process(Token t, HtmlTreeBuilder tb) {
            if (t.isCharacter()) {
                tb.newPendingTableCharacters();
                tb.markInsertionMode();
                tb.transition(InTableText);
                return tb.process(t);
            } else if (t.isComment()) {
                tb.insert(t.asComment());
                return true;
            } else if (t.isDoctype()) {
                tb.error(this);
                return false;
            } else if (t.isStartTag()) {
                Token.StartTag startTag = t.asStartTag();
                String name = startTag.normalName();
                if (name.equals("caption")) {
                    tb.clearStackToTableContext();
                    tb.insertMarkerToFormattingElements();
                    tb.insert(startTag);
                    tb.transition(InCaption);
                } else if (name.equals("colgroup")) {
                    tb.clearStackToTableContext();
                    tb.insert(startTag);
                    tb.transition(InColumnGroup);
                } else if (name.equals("col")) {
                    tb.processStartTag("colgroup");
                    return tb.process(t);
                } else if (StringUtil.in(name, "tbody", "tfoot", "thead")) {
                    tb.clearStackToTableContext();
                    tb.insert(startTag);
                    tb.transition(InTableBody);
                } else if (StringUtil.in(name, "td", "th", "tr")) {
                    tb.processStartTag("tbody");
                    return tb.process(t);
                } else if (name.equals("table")) {
                    tb.error(this);
                    boolean processed = tb.processEndTag("table");
                    if (processed) // only ignored if in fragment
                        return tb.process(t);
                } else if (StringUtil.in(name, "style", "script")) {
                    return tb.process(t, InHead);
                } else if (name.equals("input")) {
                    if (!startTag.attributes.get("type").equalsIgnoreCase("hidden")) {
                        return anythingElse(t, tb);
                    } else {
                        tb.insertEmpty(startTag);
                    }
                } else if (name.equals("form")) {
                    tb.error(this);
                    if (tb.getFormElement() != null)
                        return false;
                    else {
                        tb.insertForm(startTag, false);
                    }
                } else {
                    return anythingElse(t, tb);
                }
                return true; // todo: check if should return processed http://www.whatwg.org/specs/web-apps/current-work/multipage/tree-construction.html#parsing-main-intable
            } else if (t.isEndTag()) {
                Token.EndTag endTag = t.asEndTag();
                String name = endTag.normalName();

                if (name.equals("table")) {
                    if (!tb.inTableScope(name)) {
                        tb.error(this);
                        return false;
                    } else {
                        tb.popStackToClose("table");
                    }
                    tb.resetInsertionMode();
                } else if (StringUtil.in(name,
                        "body", "caption", "col", "colgroup", "html", "tbody", "td", "tfoot", "th", "thead", "tr")) {
                    tb.error(this);
                    return false;
                } else {
                    return anythingElse(t, tb);
                }
                return true; // todo: as above todo
            } else if (t.isEOF()) {
                if (tb.currentElement().nodeName().equals("html"))
                    tb.error(this);
                return true; // stops parsing
            }
            return anythingElse(t, tb);
        }

        boolean anythingElse(Token t, HtmlTreeBuilder tb) {
            tb.error(this);
            boolean processed;
            if (StringUtil.in(tb.currentElement().nodeName(), "table", "tbody", "tfoot", "thead", "tr")) {
                tb.setFosterInserts(true);
                processed = tb.process(t, InBody);
                tb.setFosterInserts(false);
            } else {
                processed = tb.process(t, InBody);
            }
            return processed;
        }

        boolean process(Token t, HtmlTreeBuilder tb) {
            switch (t.type) {
                case Character:
                    Token.Character c = t.asCharacter();
                    if (c.getData().equals(nullString)) {
                        tb.error(this);
                        return false;
                    } else {
                        tb.getPendingTableCharacters().add(c.getData());
                    }
                    break;
                default:
                    // todo - don't really like the way these table character data lists are built
                    if (tb.getPendingTableCharacters().size() > 0) {
                        for (String character : tb.getPendingTableCharacters()) {
                            if (!isWhitespace(character)) {
                                // InTable anything else section:
                                tb.error(this);
                                if (StringUtil.in(tb.currentElement().nodeName(), "table", "tbody", "tfoot", "thead", "tr")) {
                                    tb.setFosterInserts(true);
                                    tb.process(new Token.Character().data(character), InBody);
                                    tb.setFosterInserts(false);
                                } else {
                                    tb.process(new Token.Character().data(character), InBody);
                                }
                            } else
                                tb.insert(new Token.Character().data(character));
                        }
                        tb.newPendingTableCharacters();
                    }
                    tb.transition(tb.originalState());
                    return tb.process(t);
            }
            return true;
        }

        boolean process(Token t, HtmlTreeBuilder tb) {
            if (t.isEndTag() && t.asEndTag().normalName().equals("caption")) {
                Token.EndTag endTag = t.asEndTag();
                String name = endTag.normalName();
                if (!tb.inTableScope(name)) {
                    tb.error(this);
                    return false;
                } else {
                    tb.generateImpliedEndTags();
                    if (!tb.currentElement().nodeName().equals("caption"))
                        tb.error(this);
                    tb.popStackToClose("caption");
                    tb.clearFormattingElementsToLastMarker();
                    tb.transition(InTable);
                }
            } else if ((
                    t.isStartTag() && StringUtil.in(t.asStartTag().normalName(),
                            "caption", "col", "colgroup", "tbody", "td", "tfoot", "th", "thead", "tr") ||
                            t.isEndTag() && t.asEndTag().normalName().equals("table"))
                    ) {
                tb.error(this);
                boolean processed = tb.processEndTag("caption");
                if (processed)
                    return tb.process(t);
            } else if (t.isEndTag() && StringUtil.in(t.asEndTag().normalName(),
                    "body", "col", "colgroup", "html", "tbody", "td", "tfoot", "th", "thead", "tr")) {
                tb.error(this);
                return false;
            } else {
                return tb.process(t, InBody);
            }
            return true;
        }

        boolean process(Token t, HtmlTreeBuilder tb) {
            if (isWhitespace(t)) {
                tb.insert(t.asCharacter());
                return true;
            }
            switch (t.type) {
                case Comment:
                    tb.insert(t.asComment());
                    break;
                case Doctype:
                    tb.error(this);
                    break;
                case StartTag:
                    Token.StartTag startTag = t.asStartTag();
                    switch (startTag.normalName()) {
                        case "html":
                            return tb.process(t, InBody);
                        case "col":
                            tb.insertEmpty(startTag);
                            break;
                        default:
                            return anythingElse(t, tb);
                    }
                    break;
                case EndTag:
                    Token.EndTag endTag = t.asEndTag();
                    if (endTag.normalName.equals("colgroup")) {
                        if (tb.currentElement().nodeName().equals("html")) {
                            tb.error(this);
                            return false;
                        } else {
                            tb.pop();
                            tb.transition(InTable);
                        }
                    } else
                        return anythingElse(t, tb);
                    break;
                case EOF:
                    if (tb.currentElement().nodeName().equals("html"))
                        return true; // stop parsing; frag case
                    else
                        return anythingElse(t, tb);
                default:
                    return anythingElse(t, tb);
            }
            return true;
        }

        private boolean exitTableBody(Token t, HtmlTreeBuilder tb) {
            if (!(tb.inTableScope("tbody") || tb.inTableScope("thead") || tb.inScope("tfoot"))) {
                // frag case
                tb.error(this);
                return false;
            }
            tb.clearStackToTableBodyContext();
            tb.processEndTag(tb.currentElement().nodeName());
            return tb.process(t);
        }

        boolean process(Token t, HtmlTreeBuilder tb) {
            if (t.isEndTag()) {
                Token.EndTag endTag = t.asEndTag();
                String name = endTag.normalName();

                if (StringUtil.inSorted(name, Constants.InCellNames)) {
                    if (!tb.inTableScope(name)) {
                        tb.error(this);
                        tb.transition(InRow); // might not be in scope if empty: <td /> and processing fake end tag
                        return false;
                    }
                    tb.generateImpliedEndTags();
                    if (!tb.currentElement().nodeName().equals(name))
                        tb.error(this);
                    tb.popStackToClose(name);
                    tb.clearFormattingElementsToLastMarker();
                    tb.transition(InRow);
                } else if (StringUtil.inSorted(name, Constants.InCellBody)) {
                    tb.error(this);
                    return false;
                } else if (StringUtil.inSorted(name, Constants.InCellTable)) {
                    if (!tb.inTableScope(name)) {
                        tb.error(this);
                        return false;
                    }
                    closeCell(tb);
                    return tb.process(t);
                } else {
                    return anythingElse(t, tb);
                }
            } else if (t.isStartTag() &&
                    StringUtil.inSorted(t.asStartTag().normalName(), Constants.InCellCol)) {
                if (!(tb.inTableScope("td") || tb.inTableScope("th"))) {
                    tb.error(this);
                    return false;
                }
                closeCell(tb);
                return tb.process(t);
            } else {
                return anythingElse(t, tb);
            }
            return true;
        }

        boolean process(Token t, HtmlTreeBuilder tb) {
            switch (t.type) {
                case Character:
                    Token.Character c = t.asCharacter();
                    if (c.getData().equals(nullString)) {
                        tb.error(this);
                        return false;
                    } else {
                        tb.insert(c);
                    }
                    break;
                case Comment:
                    tb.insert(t.asComment());
                    break;
                case Doctype:
                    tb.error(this);
                    return false;
                case StartTag:
                    Token.StartTag start = t.asStartTag();
                    String name = start.normalName();
                    if (name.equals("html"))
                        return tb.process(start, InBody);
                    else if (name.equals("option")) {
                        if (tb.currentElement().nodeName().equals("option"))
                            tb.processEndTag("option");
                        tb.insert(start);
                    } else if (name.equals("optgroup")) {
                        if (tb.currentElement().nodeName().equals("option"))
                            tb.processEndTag("option");
                        else if (tb.currentElement().nodeName().equals("optgroup"))
                            tb.processEndTag("optgroup");
                        tb.insert(start);
                    } else if (name.equals("select")) {
                        tb.error(this);
                        return tb.processEndTag("select");
                    } else if (StringUtil.in(name, "input", "keygen", "textarea")) {
                        tb.error(this);
                        if (!tb.inSelectScope("select"))
                            return false; // frag
                        tb.processEndTag("select");
                        return tb.process(start);
                    } else if (name.equals("script")) {
                        return tb.process(t, InHead);
                    } else {
                        return anythingElse(t, tb);
                    }
                    break;
                case EndTag:
                    Token.EndTag end = t.asEndTag();
                    name = end.normalName();
                    switch (name) {
                        case "optgroup":
                            if (tb.currentElement().nodeName().equals("option") && tb.aboveOnStack(tb.currentElement()) != null && tb.aboveOnStack(tb.currentElement()).nodeName().equals("optgroup"))
                                tb.processEndTag("option");
                            if (tb.currentElement().nodeName().equals("optgroup"))
                                tb.pop();
                            else
                                tb.error(this);
                            break;
                        case "option":
                            if (tb.currentElement().nodeName().equals("option"))
                                tb.pop();
                            else
                                tb.error(this);
                            break;
                        case "select":
                            if (!tb.inSelectScope(name)) {
                                tb.error(this);
                                return false;
                            } else {
                                tb.popStackToClose(name);
                                tb.resetInsertionMode();
                            }
                            break;
                        default:
                            return anythingElse(t, tb);
                    }
                    break;
                case EOF:
                    if (!tb.currentElement().nodeName().equals("html"))
                        tb.error(this);
                    break;
                default:
                    return anythingElse(t, tb);
            }
            return true;
        }

        boolean process(Token t, HtmlTreeBuilder tb) {
            if (isWhitespace(t)) {
                tb.insert(t.asCharacter());
            } else if (t.isComment()) {
                tb.insert(t.asComment());
            } else if (t.isDoctype()) {
                tb.error(this);
                return false;
            } else if (t.isStartTag()) {
                Token.StartTag start = t.asStartTag();
                switch (start.normalName()) {
                    case "html":
                        return tb.process(start, InBody);
                    case "frameset":
                        tb.insert(start);
                        break;
                    case "frame":
                        tb.insertEmpty(start);
                        break;
                    case "noframes":
                        return tb.process(start, InHead);
                    default:
                        tb.error(this);
                        return false;
                }
            } else if (t.isEndTag() && t.asEndTag().normalName().equals("frameset")) {
                if (tb.currentElement().nodeName().equals("html")) {
                    tb.error(this);
                    return false;
                } else {
                    tb.pop();
                    if (!tb.isFragmentParsing() && !tb.currentElement().nodeName().equals("frameset")) {
                        tb.transition(AfterFrameset);
                    }
                }
            } else if (t.isEOF()) {
                if (!tb.currentElement().nodeName().equals("html")) {
                    tb.error(this);
                    return true;
                }
            } else {
                tb.error(this);
                return false;
            }
            return true;
        }

    private Tag(String tagName) {
        this.tagName = tagName;
    }

    public String getName() {
        return tagName;
    }

// relevant test
// org.jsoup.parser.HtmlParserTest::dropsUnterminatedTag
    @Test public void dropsUnterminatedTag() {
        
        String h1 = "<p";
        Document doc = Jsoup.parse(h1);
        assertEquals(0, doc.getElementsByTag("p").size());
        assertEquals("", doc.text());

        String h2 = "<div id=1<p id='2'";
        doc = Jsoup.parse(h2);
        assertEquals("", doc.text());
    }

// org.jsoup.parser.HtmlParserTest::dropsUnterminatedAttribute
    @Test public void dropsUnterminatedAttribute() {
        
        String h1 = "<p id=\"foo";
        Document doc = Jsoup.parse(h1);
        assertEquals("", doc.text());
    }

// org.jsoup.parser.HtmlParserTest::parsesUnterminatedTextarea
    @Test public void parsesUnterminatedTextarea() {
        
        Document doc = Jsoup.parse("<body><p><textarea>one<p>two");
        Element t = doc.select("textarea").first();
        assertEquals("one", t.text());
        assertEquals("two", doc.select("p").get(1).text());
    }

// org.jsoup.parser.HtmlParserTest::parsesUnterminatedOption
    @Test public void parsesUnterminatedOption() {
        
        Document doc = Jsoup.parse("<body><p><select><option>One<option>Two</p><p>Three</p>");
        Elements options = doc.select("option");
        assertEquals(2, options.size());
        assertEquals("One", options.first().text());
        assertEquals("TwoThree", options.last().text());
    }

// org.jsoup.parser.HtmlParserTest::testSelectWithOption
    @Test public void testSelectWithOption() {
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(10);
        Document document = parser.parseInput("<select><option>Option 1</option></select>", "http://jsoup.org");
        assertEquals(0, parser.getErrors().size());
    }

// org.jsoup.parser.HtmlParserTest::testSpaceAfterTag
    @Test public void testSpaceAfterTag() {
        Document doc = Jsoup.parse("<div > <a name=\"top\"></a ><p id=1 >Hello</p></div>");
        assertEquals("<div> <a name=\"top\"></a><p id=\"1\">Hello</p></div>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::createsDocumentStructure
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

// org.jsoup.parser.HtmlParserTest::createsStructureFromBodySnippet
    @Test public void createsStructureFromBodySnippet() {
        
        
        String html = "foo <b>bar</b> baz";
        Document doc = Jsoup.parse(html);
        assertEquals("foo bar baz", doc.text());
    }

// org.jsoup.parser.HtmlParserTest::handlesEscapedData
    @Test public void handlesEscapedData() {
        String html = "<div title='Surf &amp; Turf'>Reef &amp; Beef</div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.getElementsByTag("div").get(0);

        assertEquals("Surf & Turf", div.attr("title"));
        assertEquals("Reef & Beef", div.text());
    }

// org.jsoup.parser.HtmlParserTest::handlesDataOnlyTags
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

// org.jsoup.parser.HtmlParserTest::handlesTextAfterData
    @Test public void handlesTextAfterData() {
        String h = "<html><body>pre <script>inner</script> aft</body></html>";
        Document doc = Jsoup.parse(h);
        assertEquals("<html><head></head><body>pre <script>inner</script> aft</body></html>", TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesTextArea
    @Test public void handlesTextArea() {
        Document doc = Jsoup.parse("<textarea>Hello</textarea>");
        Elements els = doc.select("textarea");
        assertEquals("Hello", els.text());
        assertEquals("Hello", els.val());
    }

// org.jsoup.parser.HtmlParserTest::preservesSpaceInTextArea
    @Test public void preservesSpaceInTextArea() {
        
        Document doc = Jsoup.parse("<textarea>\n\tOne\n\tTwo\n\tThree\n</textarea>");
        String expect = "One\n\tTwo\n\tThree"; 
        Element el = doc.select("textarea").first();
        assertEquals(expect, el.text());
        assertEquals(expect, el.val());
        assertEquals(expect, el.html());
        assertEquals("<textarea>\n\t" + expect + "\n</textarea>", el.outerHtml()); 
    }

// org.jsoup.parser.HtmlParserTest::preservesSpaceInScript
    @Test public void preservesSpaceInScript() {
        
        Document doc = Jsoup.parse("<script>\nOne\n\tTwo\n\tThree\n</script>");
        String expect = "\nOne\n\tTwo\n\tThree\n";
        Element el = doc.select("script").first();
        assertEquals(expect, el.data());
        assertEquals("One\n\tTwo\n\tThree", el.html());
        assertEquals("<script>" + expect + "</script>", el.outerHtml());
    }

// org.jsoup.parser.HtmlParserTest::doesNotCreateImplicitLists
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

// org.jsoup.parser.HtmlParserTest::discardsNakedTds
    @Test public void discardsNakedTds() {
        
        String h = "<td>Hello<td><p>There<p>now";
        Document doc = Jsoup.parse(h);
        assertEquals("Hello<p>There</p><p>now</p>", TextUtil.stripNewlines(doc.body().html()));
        
    }

// org.jsoup.parser.HtmlParserTest::handlesNestedImplicitTable
    @Test public void handlesNestedImplicitTable() {
        Document doc = Jsoup.parse("<table><td>1</td></tr> <td>2</td></tr> <td> <table><td>3</td> <td>4</td></table> <tr><td>5</table>");
        assertEquals("<table><tbody><tr><td>1</td></tr> <tr><td>2</td></tr> <tr><td> <table><tbody><tr><td>3</td> <td>4</td></tr></tbody></table> </td></tr><tr><td>5</td></tr></tbody></table>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesWhatWgExpensesTableExample
    @Test public void handlesWhatWgExpensesTableExample() {
        
        Document doc = Jsoup.parse("<table> <colgroup> <col> <colgroup> <col> <col> <col> <thead> <tr> <th> <th>2008 <th>2007 <th>2006 <tbody> <tr> <th scope=rowgroup> Research and development <td> $ 1,109 <td> $ 782 <td> $ 712 <tr> <th scope=row> Percentage of net sales <td> 3.4% <td> 3.3% <td> 3.7% <tbody> <tr> <th scope=rowgroup> Selling, general, and administrative <td> $ 3,761 <td> $ 2,963 <td> $ 2,433 <tr> <th scope=row> Percentage of net sales <td> 11.6% <td> 12.3% <td> 12.6% </table>");
        assertEquals("<table> <colgroup> <col> </colgroup><colgroup> <col> <col> <col> </colgroup><thead> <tr> <th> </th><th>2008 </th><th>2007 </th><th>2006 </th></tr></thead><tbody> <tr> <th scope=\"rowgroup\"> Research and development </th><td> $ 1,109 </td><td> $ 782 </td><td> $ 712 </td></tr><tr> <th scope=\"row\"> Percentage of net sales </th><td> 3.4% </td><td> 3.3% </td><td> 3.7% </td></tr></tbody><tbody> <tr> <th scope=\"rowgroup\"> Selling, general, and administrative </th><td> $ 3,761 </td><td> $ 2,963 </td><td> $ 2,433 </td></tr><tr> <th scope=\"row\"> Percentage of net sales </th><td> 11.6% </td><td> 12.3% </td><td> 12.6% </td></tr></tbody></table>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesTbodyTable
    @Test public void handlesTbodyTable() {
        Document doc = Jsoup.parse("<html><head></head><body><table><tbody><tr><td>aaa</td><td>bbb</td></tr></tbody></table></body></html>");
        assertEquals("<table><tbody><tr><td>aaa</td><td>bbb</td></tr></tbody></table>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesImplicitCaptionClose
    @Test public void handlesImplicitCaptionClose() {
        Document doc = Jsoup.parse("<table><caption>A caption<td>One<td>Two");
        assertEquals("<table><caption>A caption</caption><tbody><tr><td>One</td><td>Two</td></tr></tbody></table>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::noTableDirectInTable
    @Test public void noTableDirectInTable() {
        Document doc = Jsoup.parse("<table> <td>One <td><table><td>Two</table> <table><td>Three");
        assertEquals("<table> <tbody><tr><td>One </td><td><table><tbody><tr><td>Two</td></tr></tbody></table> <table><tbody><tr><td>Three</td></tr></tbody></table></td></tr></tbody></table>",
            TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::ignoresDupeEndTrTag
    @Test public void ignoresDupeEndTrTag() {
        Document doc = Jsoup.parse("<table><tr><td>One</td><td><table><tr><td>Two</td></tr></tr></table></td><td>Three</td></tr></table>"); 
        assertEquals("<table><tbody><tr><td>One</td><td><table><tbody><tr><td>Two</td></tr></tbody></table></td><td>Three</td></tr></tbody></table>",
            TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesBaseTags
    @Test public void handlesBaseTags() {
        
        String h = "<a href=1>#</a><base href='/2/'><a href='3'>#</a><base href='http://bar'><a href=/4>#</a>";
        Document doc = Jsoup.parse(h, "http://foo/");
        assertEquals("http://foo/2/", doc.baseUri()); 

        Elements anchors = doc.getElementsByTag("a");
        assertEquals(3, anchors.size());

        assertEquals("http://foo/2/", anchors.get(0).baseUri());
        assertEquals("http://foo/2/", anchors.get(1).baseUri());
        assertEquals("http://foo/2/", anchors.get(2).baseUri());

        assertEquals("http://foo/2/1", anchors.get(0).absUrl("href"));
        assertEquals("http://foo/2/3", anchors.get(1).absUrl("href"));
        assertEquals("http://foo/4", anchors.get(2).absUrl("href"));
    }

// org.jsoup.parser.HtmlParserTest::handlesProtocolRelativeUrl
    @Test public void handlesProtocolRelativeUrl() {
        String base = "https://example.com/";
        String html = "<img src='//example.net/img.jpg'>";
        Document doc = Jsoup.parse(html, base);
        Element el = doc.select("img").first();
        assertEquals("https://example.net/img.jpg", el.absUrl("src"));
    }

// org.jsoup.parser.HtmlParserTest::handlesCdata
    @Test public void handlesCdata() {
        
        String h = "<div id=1><![CDATA[<html>\n <foo><&amp;]]></div>"; 
        Document doc = Jsoup.parse(h);
        Element div = doc.getElementById("1");
        assertEquals("<html>\n <foo><&amp;", div.text());
        assertEquals(0, div.children().size());
        assertEquals(1, div.childNodeSize()); 
    }

// org.jsoup.parser.HtmlParserTest::roundTripsCdata
    @Test public void roundTripsCdata() {
        String h = "<div id=1><![CDATA[\n<html>\n <foo><&amp;]]></div>";
        Document doc = Jsoup.parse(h);
        Element div = doc.getElementById("1");
        assertEquals("<html>\n <foo><&amp;", div.text());
        assertEquals(0, div.children().size());
        assertEquals(1, div.childNodeSize()); 

        assertEquals("<div id=\"1\"><![CDATA[\n<html>\n <foo><&amp;]]>\n</div>", div.outerHtml());

        CDataNode cdata = (CDataNode) div.textNodes().get(0);
        assertEquals("\n<html>\n <foo><&amp;", cdata.text());
    }

// org.jsoup.parser.HtmlParserTest::handlesCdataAcrossBuffer
    @Test public void handlesCdataAcrossBuffer() {
        StringBuilder sb = new StringBuilder();
        while (sb.length() <= CharacterReader.maxBufferLen) {
            sb.append("A suitable amount of CData.\n");
        }
        String cdata = sb.toString();
        String h = "<div><![CDATA[" + cdata + "]]></div>";
        Document doc = Jsoup.parse(h);
        Element div = doc.selectFirst("div");

        CDataNode node = (CDataNode) div.textNodes().get(0);
        assertEquals(cdata, node.text());
    }

// org.jsoup.parser.HtmlParserTest::handlesCdataInScript
    @Test public void handlesCdataInScript() {
        String html = "<script type=\"text/javascript\">//<![CDATA[\n\n  foo();\n//]]></script>";
        Document doc = Jsoup.parse(html);

        String data = "//<![CDATA[\n\n  foo();\n//]]>";
        Element script = doc.selectFirst("script");
        assertEquals("", script.text()); 
        assertEquals(data, script.data());
        assertEquals(html, script.outerHtml());

        DataNode dataNode = (DataNode) script.childNode(0);
        assertEquals(data, dataNode.getWholeData());
        
    }

// org.jsoup.parser.HtmlParserTest::handlesUnclosedCdataAtEOF
    @Test public void handlesUnclosedCdataAtEOF() {
        
        String h = "<![CDATA[]]";
        Document doc = Jsoup.parse(h);
        assertEquals(1, doc.body().childNodeSize());
    }

// org.jsoup.parser.HtmlParserTest::handleCDataInText
    @Test public void handleCDataInText() {
        String h = "<p>One <![CDATA[Two <&]]> Three</p>";
        Document doc = Jsoup.parse(h);
        Element p = doc.selectFirst("p");

        List<Node> nodes = p.childNodes();
        assertEquals("One ", ((TextNode) nodes.get(0)).getWholeText());
        assertEquals("Two <&", ((TextNode) nodes.get(1)).getWholeText());
        assertEquals("Two <&", ((CDataNode) nodes.get(1)).getWholeText());
        assertEquals(" Three", ((TextNode) nodes.get(2)).getWholeText());

        assertEquals(h, p.outerHtml());
    }

// org.jsoup.parser.HtmlParserTest::cdataNodesAreTextNodes
    @Test public void cdataNodesAreTextNodes() {
        String h = "<p>One <![CDATA[ Two <& ]]> Three</p>";
        Document doc = Jsoup.parse(h);
        Element p = doc.selectFirst("p");

        List<TextNode> nodes = p.textNodes();
        assertEquals("One ", nodes.get(0).text());
        assertEquals(" Two <& ", nodes.get(1).text());
        assertEquals(" Three", nodes.get(2).text());
    }

// org.jsoup.parser.HtmlParserTest::handlesInvalidStartTags
    @Test public void handlesInvalidStartTags() {
        String h = "<div>Hello < There <&amp;></div>"; 
        Document doc = Jsoup.parse(h);
        assertEquals("Hello < There <&>", doc.select("div").first().text());
    }

// org.jsoup.parser.HtmlParserTest::handlesUnknownTags
    @Test public void handlesUnknownTags() {
        String h = "<div><foo title=bar>Hello<foo title=qux>there</foo></div>";
        Document doc = Jsoup.parse(h);
        Elements foos = doc.select("foo");
        assertEquals(2, foos.size());
        assertEquals("bar", foos.first().attr("title"));
        assertEquals("qux", foos.last().attr("title"));
        assertEquals("there", foos.last().text());
    }

// org.jsoup.parser.HtmlParserTest::handlesUnknownInlineTags
    @Test public void handlesUnknownInlineTags() {
        String h = "<p><cust>Test</cust></p><p><cust><cust>Test</cust></cust></p>";
        Document doc = Jsoup.parseBodyFragment(h);
        String out = doc.body().html();
        assertEquals(h, TextUtil.stripNewlines(out));
    }

// org.jsoup.parser.HtmlParserTest::parsesBodyFragment
    @Test public void parsesBodyFragment() {
        String h = "<!-- comment --><p><a href='foo'>One</a></p>";
        Document doc = Jsoup.parseBodyFragment(h, "http://example.com");
        assertEquals("<body><!-- comment --><p><a href=\"foo\">One</a></p></body>", TextUtil.stripNewlines(doc.body().outerHtml()));
        assertEquals("http://example.com/foo", doc.select("a").first().absUrl("href"));
    }

// org.jsoup.parser.HtmlParserTest::handlesUnknownNamespaceTags
    @Test public void handlesUnknownNamespaceTags() {
        
        String h = "<foo:bar id='1' /><abc:def id=2>Foo<p>Hello</p></abc:def><foo:bar>There</foo:bar>";
        Document doc = Jsoup.parse(h);
        assertEquals("<foo:bar id=\"1\" /><abc:def id=\"2\">Foo<p>Hello</p></abc:def><foo:bar>There</foo:bar>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesKnownEmptyBlocks
    @Test public void handlesKnownEmptyBlocks() {
        
        String h = "<div id='1' /><script src='/foo' /><div id=2><img /><img></div><a id=3 /><i /><foo /><foo>One</foo> <hr /> hr text <hr> hr text two";
        Document doc = Jsoup.parse(h);
        assertEquals("<div id=\"1\"></div><script src=\"/foo\"></script><div id=\"2\"><img><img></div><a id=\"3\"></a><i></i><foo /><foo>One</foo> <hr> hr text <hr> hr text two", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesKnownEmptyNoFrames
    @Test public void handlesKnownEmptyNoFrames() {
        String h = "<html><head><noframes /><meta name=foo></head><body>One</body></html>";
        Document doc = Jsoup.parse(h);
        assertEquals("<html><head><noframes></noframes><meta name=\"foo\"></head><body>One</body></html>", TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesKnownEmptyStyle
    @Test public void handlesKnownEmptyStyle() {
        String h = "<html><head><style /><meta name=foo></head><body>One</body></html>";
        Document doc = Jsoup.parse(h);
        assertEquals("<html><head><style></style><meta name=\"foo\"></head><body>One</body></html>", TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesKnownEmptyTitle
    @Test public void handlesKnownEmptyTitle() {
        String h = "<html><head><title /><meta name=foo></head><body>One</body></html>";
        Document doc = Jsoup.parse(h);
        assertEquals("<html><head><title></title><meta name=\"foo\"></head><body>One</body></html>", TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesKnownEmptyIframe
    @Test public void handlesKnownEmptyIframe() {
        String h = "<p>One</p><iframe id=1 /><p>Two";
        Document doc = Jsoup.parse(h);
        assertEquals("<html><head></head><body><p>One</p><iframe id=\"1\"></iframe><p>Two</p></body></html>", TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesSolidusAtAttributeEnd
    @Test public void handlesSolidusAtAttributeEnd() {
        
        String h = "<a href=/>link</a>";
        Document doc = Jsoup.parse(h);
        assertEquals("<a href=\"/\">link</a>", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::handlesMultiClosingBody
    @Test public void handlesMultiClosingBody() {
        String h = "<body><p>Hello</body><p>there</p></body></body></html><p>now";
        Document doc = Jsoup.parse(h);
        assertEquals(3, doc.select("p").size());
        assertEquals(3, doc.body().children().size());
    }

// org.jsoup.parser.HtmlParserTest::handlesUnclosedDefinitionLists
    @Test public void handlesUnclosedDefinitionLists() {
        
        String h = "<dt>Foo<dd>Bar<dt>Qux<dd>Zug";
        Document doc = Jsoup.parse(h);
        assertEquals(0, doc.select("dl").size()); 
        assertEquals(4, doc.select("dt, dd").size());
        Elements dts = doc.select("dt");
        assertEquals(2, dts.size());
        assertEquals("Zug", dts.get(1).nextElementSibling().text());
    }

// org.jsoup.parser.HtmlParserTest::handlesBlocksInDefinitions
    @Test public void handlesBlocksInDefinitions() {
        
        String h = "<dl><dt><div id=1>Term</div></dt><dd><div id=2>Def</div></dd></dl>";
        Document doc = Jsoup.parse(h);
        assertEquals("dt", doc.select("#1").first().parent().tagName());
        assertEquals("dd", doc.select("#2").first().parent().tagName());
        assertEquals("<dl><dt><div id=\"1\">Term</div></dt><dd><div id=\"2\">Def</div></dd></dl>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesFrames
    @Test public void handlesFrames() {
        String h = "<html><head><script></script><noscript></noscript></head><frameset><frame src=foo></frame><frame src=foo></frameset></html>";
        Document doc = Jsoup.parse(h);
        assertEquals("<html><head><script></script><noscript></noscript></head><frameset><frame src=\"foo\"><frame src=\"foo\"></frameset></html>",
            TextUtil.stripNewlines(doc.html()));
        
    }

// org.jsoup.parser.HtmlParserTest::ignoresContentAfterFrameset
    @Test public void ignoresContentAfterFrameset() {
        String h = "<html><head><title>One</title></head><frameset><frame /><frame /></frameset><table></table></html>";
        Document doc = Jsoup.parse(h);
        assertEquals("<html><head><title>One</title></head><frameset><frame><frame></frameset></html>", TextUtil.stripNewlines(doc.html()));
        
    }

// org.jsoup.parser.HtmlParserTest::handlesJavadocFont
    @Test public void handlesJavadocFont() {
        String h = "<TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <A HREF=\"deprecated-list.html\"><FONT CLASS=\"NavBarFont1\"><B>Deprecated</B></FONT></A>&nbsp;</TD>";
        Document doc = Jsoup.parse(h);
        Element a = doc.select("a").first();
        assertEquals("Deprecated", a.text());
        assertEquals("font", a.child(0).tagName());
        assertEquals("b", a.child(0).child(0).tagName());
    }

// org.jsoup.parser.HtmlParserTest::handlesBaseWithoutHref
    @Test public void handlesBaseWithoutHref() {
        String h = "<head><base target='_blank'></head><body><a href=/foo>Test</a></body>";
        Document doc = Jsoup.parse(h, "http://example.com/");
        Element a = doc.select("a").first();
        assertEquals("/foo", a.attr("href"));
        assertEquals("http://example.com/foo", a.attr("abs:href"));
    }

// org.jsoup.parser.HtmlParserTest::normalisesDocument
    @Test public void normalisesDocument() {
        String h = "<!doctype html>One<html>Two<head>Three<link></head>Four<body>Five </body>Six </html>Seven ";
        Document doc = Jsoup.parse(h);
        assertEquals("<!doctype html><html><head></head><body>OneTwoThree<link>FourFive Six Seven </body></html>",
            TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.HtmlParserTest::normalisesEmptyDocument
    @Test public void normalisesEmptyDocument() {
        Document doc = Jsoup.parse("");
        assertEquals("<html><head></head><body></body></html>", TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.HtmlParserTest::normalisesHeadlessBody
    @Test public void normalisesHeadlessBody() {
        Document doc = Jsoup.parse("<html><body><span class=\"foo\">bar</span>");
        assertEquals("<html><head></head><body><span class=\"foo\">bar</span></body></html>",
            TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.HtmlParserTest::normalisedBodyAfterContent
    @Test public void normalisedBodyAfterContent() {
        Document doc = Jsoup.parse("<font face=Arial><body class=name><div>One</div></body></font>");
        assertEquals("<html><head></head><body class=\"name\"><font face=\"Arial\"><div>One</div></font></body></html>",
            TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.HtmlParserTest::findsCharsetInMalformedMeta
    @Test public void findsCharsetInMalformedMeta() {
        String h = "<meta http-equiv=Content-Type content=text/html; charset=gb2312>";
        
        Document doc = Jsoup.parse(h);
        assertEquals("gb2312", doc.select("meta").attr("charset"));
    }

// org.jsoup.parser.HtmlParserTest::testHgroup
    @Test public void testHgroup() {
        
        Document doc = Jsoup.parse("<h1>Hello <h2>There <hgroup><h1>Another<h2>headline</hgroup> <hgroup><h1>More</h1><p>stuff</p></hgroup>");
        assertEquals("<h1>Hello </h1><h2>There <hgroup><h1>Another</h1><h2>headline</h2></hgroup> <hgroup><h1>More</h1><p>stuff</p></hgroup></h2>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::testRelaxedTags
    @Test public void testRelaxedTags() {
        Document doc = Jsoup.parse("<abc_def id=1>Hello</abc_def> <abc-def>There</abc-def>");
        assertEquals("<abc_def id=\"1\">Hello</abc_def> <abc-def>There</abc-def>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::testHeaderContents
    @Test public void testHeaderContents() {
        
        
        Document doc = Jsoup.parse("<h1>Hello <div>There</div> now</h1> <h2>More <h3>Content</h3></h2>");
        assertEquals("<h1>Hello <div>There</div> now</h1> <h2>More </h2><h3>Content</h3>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::testSpanContents
    @Test public void testSpanContents() {
        
        Document doc = Jsoup.parse("<span>Hello <div>there</div> <span>now</span></span>");
        assertEquals("<span>Hello <div>there</div> <span>now</span></span>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::testNoImagesInNoScriptInHead
    @Test public void testNoImagesInNoScriptInHead() {
        
        Document doc = Jsoup.parse("<html><head><noscript><img src='foo'></noscript></head><body><p>Hello</p></body></html>");
        assertEquals("<html><head><noscript>&lt;img src=\"foo\"&gt;</noscript></head><body><p>Hello</p></body></html>", TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.HtmlParserTest::testAFlowContents
    @Test public void testAFlowContents() {
        
        Document doc = Jsoup.parse("<a>Hello <div>there</div> <span>now</span></a>");
        assertEquals("<a>Hello <div>there</div> <span>now</span></a>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::testFontFlowContents
    @Test public void testFontFlowContents() {
        
        Document doc = Jsoup.parse("<font>Hello <div>there</div> <span>now</span></font>");
        assertEquals("<font>Hello <div>there</div> <span>now</span></font>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesMisnestedTagsBI
    @Test public void handlesMisnestedTagsBI() {
        
        String h = "<p>1<b>2<i>3</b>4</i>5</p>";
        Document doc = Jsoup.parse(h);
        assertEquals("<p>1<b>2<i>3</i></b><i>4</i>5</p>", doc.body().html());
        
    }

// org.jsoup.parser.HtmlParserTest::handlesMisnestedTagsBP
    @Test public void handlesMisnestedTagsBP() {
        
        String h = "<b>1<p>2</b>3</p>";
        Document doc = Jsoup.parse(h);
        assertEquals("<b>1</b>\n<p><b>2</b>3</p>", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::handlesMisnestedAInDivs
    @Test public void handlesMisnestedAInDivs() {
        String h = "<a href='#1'><div><div><a href='#2'>child</a</div</div></a>";
        String w = "<a href=\"#1\"></a><div><a href=\"#1\"></a><div><a href=\"#1\"></a><a href=\"#2\">child</a></div></div>";
        Document doc = Jsoup.parse(h);
        assertEquals(
            StringUtil.normaliseWhitespace(w),
            StringUtil.normaliseWhitespace(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesUnexpectedMarkupInTables
    @Test public void handlesUnexpectedMarkupInTables() {
        
        
        String h = "<table><b><tr><td>aaa</td></tr>bbb</table>ccc";
        Document doc = Jsoup.parse(h);
        assertEquals("<b></b><b>bbb</b><table><tbody><tr><td>aaa</td></tr></tbody></table><b>ccc</b>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesUnclosedFormattingElements
    @Test public void handlesUnclosedFormattingElements() {
        
        String h = "<!DOCTYPE html>\n" +
            "<p><b class=x><b class=x><b><b class=x><b class=x><b>X\n" +
            "<p>X\n" +
            "<p><b><b class=x><b>X\n" +
            "<p></b></b></b></b></b></b>X";
        Document doc = Jsoup.parse(h);
        doc.outputSettings().indentAmount(0);
        String want = "<!doctype html>\n" +
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

// org.jsoup.parser.HtmlParserTest::handlesUnclosedAnchors
    @Test public void handlesUnclosedAnchors() {
        String h = "<a href='http://example.com/'>Link<p>Error link</a>";
        Document doc = Jsoup.parse(h);
        String want = "<a href=\"http://example.com/\">Link</a>\n<p><a href=\"http://example.com/\">Error link</a></p>";
        assertEquals(want, doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::reconstructFormattingElements
    @Test public void reconstructFormattingElements() {
        
        String h = "<p><b class=one>One <i>Two <b>Three</p><p>Hello</p>";
        Document doc = Jsoup.parse(h);
        assertEquals("<p><b class=\"one\">One <i>Two <b>Three</b></i></b></p>\n<p><b class=\"one\"><i><b>Hello</b></i></b></p>", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::reconstructFormattingElementsInTable
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

// org.jsoup.parser.HtmlParserTest::commentBeforeHtml
    @Test public void commentBeforeHtml() {
        String h = "<!-- comment --><!-- comment 2 --><p>One</p>";
        Document doc = Jsoup.parse(h);
        assertEquals("<!-- comment --><!-- comment 2 --><html><head></head><body><p>One</p></body></html>", TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.HtmlParserTest::emptyTdTag
    @Test public void emptyTdTag() {
        String h = "<table><tr><td>One</td><td id='2' /></tr></table>";
        Document doc = Jsoup.parse(h);
        assertEquals("<td>One</td>\n<td id=\"2\"></td>", doc.select("tr").first().html());
    }

// org.jsoup.parser.HtmlParserTest::handlesSolidusInA
    @Test public void handlesSolidusInA() {
        
        String h = "<a class=lp href=/lib/14160711/>link text</a>";
        Document doc = Jsoup.parse(h);
        Element a = doc.select("a").first();
        assertEquals("link text", a.text());
        assertEquals("/lib/14160711/", a.attr("href"));
    }

// org.jsoup.parser.HtmlParserTest::handlesSpanInTbody
    @Test public void handlesSpanInTbody() {
        
        String h = "<table><tbody><span class='1'><tr><td>One</td></tr><tr><td>Two</td></tr></span></tbody></table>";
        Document doc = Jsoup.parse(h);
        assertEquals(doc.select("span").first().children().size(), 0); 
        assertEquals(doc.select("table").size(), 1); 
    }

// org.jsoup.parser.HtmlParserTest::handlesUnclosedTitleAtEof
    @Test public void handlesUnclosedTitleAtEof() {
        assertEquals("Data", Jsoup.parse("<title>Data").title());
        assertEquals("Data<", Jsoup.parse("<title>Data<").title());
        assertEquals("Data</", Jsoup.parse("<title>Data</").title());
        assertEquals("Data</t", Jsoup.parse("<title>Data</t").title());
        assertEquals("Data</ti", Jsoup.parse("<title>Data</ti").title());
        assertEquals("Data", Jsoup.parse("<title>Data</title>").title());
        assertEquals("Data", Jsoup.parse("<title>Data</title >").title());
    }

// org.jsoup.parser.HtmlParserTest::handlesUnclosedTitle
    @Test public void handlesUnclosedTitle() {
        Document one = Jsoup.parse("<title>One <b>Two <b>Three</TITLE><p>Test</p>"); 
        assertEquals("One <b>Two <b>Three", one.title());
        assertEquals("Test", one.select("p").first().text());

        Document two = Jsoup.parse("<title>One<b>Two <p>Test</p>"); 
        assertEquals("One", two.title());
        assertEquals("<b>Two <p>Test</p></b>", two.body().html());
    }

// org.jsoup.parser.HtmlParserTest::handlesUnclosedScriptAtEof
    @Test public void handlesUnclosedScriptAtEof() {
        assertEquals("Data", Jsoup.parse("<script>Data").select("script").first().data());
        assertEquals("Data<", Jsoup.parse("<script>Data<").select("script").first().data());
        assertEquals("Data</sc", Jsoup.parse("<script>Data</sc").select("script").first().data());
        assertEquals("Data</-sc", Jsoup.parse("<script>Data</-sc").select("script").first().data());
        assertEquals("Data</sc-", Jsoup.parse("<script>Data</sc-").select("script").first().data());
        assertEquals("Data</sc--", Jsoup.parse("<script>Data</sc--").select("script").first().data());
        assertEquals("Data", Jsoup.parse("<script>Data</script>").select("script").first().data());
        assertEquals("Data</script", Jsoup.parse("<script>Data</script").select("script").first().data());
        assertEquals("Data", Jsoup.parse("<script>Data</script ").select("script").first().data());
        assertEquals("Data", Jsoup.parse("<script>Data</script n").select("script").first().data());
        assertEquals("Data", Jsoup.parse("<script>Data</script n=").select("script").first().data());
        assertEquals("Data", Jsoup.parse("<script>Data</script n=\"").select("script").first().data());
        assertEquals("Data", Jsoup.parse("<script>Data</script n=\"p").select("script").first().data());
    }

// org.jsoup.parser.HtmlParserTest::handlesUnclosedRawtextAtEof
    @Test public void handlesUnclosedRawtextAtEof() {
        assertEquals("Data", Jsoup.parse("<style>Data").select("style").first().data());
        assertEquals("Data</st", Jsoup.parse("<style>Data</st").select("style").first().data());
        assertEquals("Data", Jsoup.parse("<style>Data</style>").select("style").first().data());
        assertEquals("Data</style", Jsoup.parse("<style>Data</style").select("style").first().data());
        assertEquals("Data</-style", Jsoup.parse("<style>Data</-style").select("style").first().data());
        assertEquals("Data</style-", Jsoup.parse("<style>Data</style-").select("style").first().data());
        assertEquals("Data</style--", Jsoup.parse("<style>Data</style--").select("style").first().data());
    }

// org.jsoup.parser.HtmlParserTest::noImplicitFormForTextAreas
    @Test public void noImplicitFormForTextAreas() {
        
        Document doc = Jsoup.parse("<textarea>One</textarea>");
        assertEquals("<textarea>One</textarea>", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::handlesEscapedScript
    @Test public void handlesEscapedScript() {
        Document doc = Jsoup.parse("<script><!-- one <script>Blah</script> --></script>");
        assertEquals("<!-- one <script>Blah</script> -->", doc.select("script").first().data());
    }

// org.jsoup.parser.HtmlParserTest::handles0CharacterAsText
    @Test public void handles0CharacterAsText() {
        Document doc = Jsoup.parse("0<p>0</p>");
        assertEquals("0\n<p>0</p>", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::handlesNullInData
    @Test public void handlesNullInData() {
        Document doc = Jsoup.parse("<p id=\u0000>Blah \u0000</p>");
        assertEquals("<p id=\"\uFFFD\">Blah \u0000</p>", doc.body().html()); 
    }

// org.jsoup.parser.HtmlParserTest::handlesNullInComments
    @Test public void handlesNullInComments() {
        Document doc = Jsoup.parse("<body><!-- \u0000 \u0000 -->");
        assertEquals("<!-- \uFFFD \uFFFD -->", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::handlesNewlinesAndWhitespaceInTag
    @Test public void handlesNewlinesAndWhitespaceInTag() {
        Document doc = Jsoup.parse("<a \n href=\"one\" \r\n id=\"two\" \f >");
        assertEquals("<a href=\"one\" id=\"two\"></a>", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::handlesWhitespaceInoDocType
    @Test public void handlesWhitespaceInoDocType() {
        String html = "<!DOCTYPE html\r\n" +
            "      PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\r\n" +
            "      \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">";
        Document doc = Jsoup.parse(html);
        assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">", doc.childNode(0).outerHtml());
    }

// org.jsoup.parser.HtmlParserTest::tracksErrorsWhenRequested
    @Test public void tracksErrorsWhenRequested() {
        String html = "<p>One</p href='no'><!DOCTYPE html>&arrgh;<font /><br /><foo";
        Parser parser = Parser.htmlParser().setTrackErrors(500);
        Document doc = Jsoup.parse(html, "http://example.com", parser);

        List<ParseError> errors = parser.getErrors();
        assertEquals(5, errors.size());
        assertEquals("20: Attributes incorrectly present on end tag", errors.get(0).toString());
        assertEquals("35: Unexpected token [Doctype] when in state [InBody]", errors.get(1).toString());
        assertEquals("36: Invalid character reference: invalid named referenece 'arrgh'", errors.get(2).toString());
        assertEquals("50: Tag cannot be self closing; not a void tag", errors.get(3).toString());
        assertEquals("61: Unexpectedly reached end of file (EOF) in input state [TagName]", errors.get(4).toString());
    }

// org.jsoup.parser.HtmlParserTest::tracksLimitedErrorsWhenRequested
    @Test public void tracksLimitedErrorsWhenRequested() {
        String html = "<p>One</p href='no'><!DOCTYPE html>&arrgh;<font /><br /><foo";
        Parser parser = Parser.htmlParser().setTrackErrors(3);
        Document doc = parser.parseInput(html, "http://example.com");

        List<ParseError> errors = parser.getErrors();
        assertEquals(3, errors.size());
        assertEquals("20: Attributes incorrectly present on end tag", errors.get(0).toString());
        assertEquals("35: Unexpected token [Doctype] when in state [InBody]", errors.get(1).toString());
        assertEquals("36: Invalid character reference: invalid named referenece 'arrgh'", errors.get(2).toString());
    }

// org.jsoup.parser.HtmlParserTest::noErrorsByDefault
    @Test public void noErrorsByDefault() {
        String html = "<p>One</p href='no'>&arrgh;<font /><br /><foo";
        Parser parser = Parser.htmlParser();
        Document doc = Jsoup.parse(html, "http://example.com", parser);

        List<ParseError> errors = parser.getErrors();
        assertEquals(0, errors.size());
    }

// org.jsoup.parser.HtmlParserTest::handlesCommentsInTable
    @Test public void handlesCommentsInTable() {
        String html = "<table><tr><td>text</td><!-- Comment --></tr></table>";
        Document node = Jsoup.parseBodyFragment(html);
        assertEquals("<html><head></head><body><table><tbody><tr><td>text</td><!-- Comment --></tr></tbody></table></body></html>", TextUtil.stripNewlines(node.outerHtml()));
    }

// org.jsoup.parser.HtmlParserTest::handlesQuotesInCommentsInScripts
    @Test public void handlesQuotesInCommentsInScripts() {
        String html = "<script>\n" +
            "  <!--\n" +
            "    document.write('</scr' + 'ipt>');\n" +
            "  
            "</script>";
        Document node = Jsoup.parseBodyFragment(html);
        assertEquals("<script>\n" +
            "  <!--\n" +
            "    document.write('</scr' + 'ipt>');\n" +
            "  
            "</script>", node.body().html());
    }

// org.jsoup.parser.HtmlParserTest::handleNullContextInParseFragment
    @Test public void handleNullContextInParseFragment() {
        String html = "<ol><li>One</li></ol><p>Two</p>";
        List<Node> nodes = Parser.parseFragment(html, null, "http://example.com/");
        assertEquals(1, nodes.size()); 
        assertEquals("html", nodes.get(0).nodeName());
        assertEquals("<html> <head></head> <body> <ol> <li>One</li> </ol> <p>Two</p> </body> </html>", StringUtil.normaliseWhitespace(nodes.get(0).outerHtml()));
    }

// org.jsoup.parser.HtmlParserTest::doesNotFindShortestMatchingEntity
    @Test public void doesNotFindShortestMatchingEntity() {
        
        
        String html = "One &clubsuite; &clubsuit;";
        Document doc = Jsoup.parse(html);
        assertEquals(StringUtil.normaliseWhitespace("One &amp;clubsuite; ♣"), doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::relaxedBaseEntityMatchAndStrictExtendedMatch
    @Test public void relaxedBaseEntityMatchAndStrictExtendedMatch() {
        
        String html = "&amp &quot &reg &icy &hopf &icy; &hopf;";
        Document doc = Jsoup.parse(html);
        doc.outputSettings().escapeMode(Entities.EscapeMode.extended).charset("ascii"); 
        assertEquals("&amp; \" &reg; &amp;icy &amp;hopf &icy; &hopf;", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::handlesXmlDeclarationAsBogusComment
    @Test public void handlesXmlDeclarationAsBogusComment() {
        String html = "<?xml encoding='UTF-8' ?><body>One</body>";
        Document doc = Jsoup.parse(html);
        assertEquals("<!--?xml encoding='UTF-8' ?--> <html> <head></head> <body> One </body> </html>", StringUtil.normaliseWhitespace(doc.outerHtml()));
    }

// org.jsoup.parser.HtmlParserTest::handlesTagsInTextarea
    @Test public void handlesTagsInTextarea() {
        String html = "<textarea><p>Jsoup</p></textarea>";
        Document doc = Jsoup.parse(html);
        assertEquals("<textarea>&lt;p&gt;Jsoup&lt;/p&gt;</textarea>", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::createsFormElements
    @Test public void createsFormElements() {
        String html = "<body><form><input id=1><input id=2></form></body>";
        Document doc = Jsoup.parse(html);
        Element el = doc.select("form").first();

        assertTrue("Is form element", el instanceof FormElement);
        FormElement form = (FormElement) el;
        Elements controls = form.elements();
        assertEquals(2, controls.size());
        assertEquals("1", controls.get(0).id());
        assertEquals("2", controls.get(1).id());
    }

// org.jsoup.parser.HtmlParserTest::associatedFormControlsWithDisjointForms
    @Test public void associatedFormControlsWithDisjointForms() {
        
        String html = "<table><tr><form><input type=hidden id=1><td><input type=text id=2></td><tr></table>";
        Document doc = Jsoup.parse(html);
        Element el = doc.select("form").first();

        assertTrue("Is form element", el instanceof FormElement);
        FormElement form = (FormElement) el;
        Elements controls = form.elements();
        assertEquals(2, controls.size());
        assertEquals("1", controls.get(0).id());
        assertEquals("2", controls.get(1).id());

        assertEquals("<table><tbody><tr><form></form><input type=\"hidden\" id=\"1\"><td><input type=\"text\" id=\"2\"></td></tr><tr></tr></tbody></table>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesInputInTable
    @Test public void handlesInputInTable() {
        String h = "<body>\n" +
            "<input type=\"hidden\" name=\"a\" value=\"\">\n" +
            "<table>\n" +
            "<input type=\"hidden\" name=\"b\" value=\"\" />\n" +
            "</table>\n" +
            "</body>";
        Document doc = Jsoup.parse(h);
        assertEquals(1, doc.select("table input").size());
        assertEquals(2, doc.select("input").size());
    }

// org.jsoup.parser.HtmlParserTest::convertsImageToImg
    @Test public void convertsImageToImg() {
        
        String h = "<body><image><svg><image /></svg></body>";
        Document doc = Jsoup.parse(h);
        assertEquals("<img>\n<svg>\n <image />\n</svg>", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::handlesInvalidDoctypes
    @Test public void handlesInvalidDoctypes() {
        
        Document doc = Jsoup.parse("<!DOCTYPE>");
        assertEquals(
            "<!doctype> <html> <head></head> <body></body> </html>",
            StringUtil.normaliseWhitespace(doc.outerHtml()));

        doc = Jsoup.parse("<!DOCTYPE><html><p>Foo</p></html>");
        assertEquals(
            "<!doctype> <html> <head></head> <body> <p>Foo</p> </body> </html>",
            StringUtil.normaliseWhitespace(doc.outerHtml()));

        doc = Jsoup.parse("<!DOCTYPE \u0000>");
        assertEquals(
            "<!doctype �> <html> <head></head> <body></body> </html>",
            StringUtil.normaliseWhitespace(doc.outerHtml()));
    }

// org.jsoup.parser.HtmlParserTest::handlesManyChildren
    @Test public void handlesManyChildren() {
        
        StringBuilder longBody = new StringBuilder(500000);
        for (int i = 0; i < 25000; i++) {
            longBody.append(i).append("<br>");
        }

        
        long start = System.currentTimeMillis();
        Document doc = Parser.parseBodyFragment(longBody.toString(), "");

        
        assertEquals(50000, doc.body().childNodeSize());
        assertTrue(System.currentTimeMillis() - start < 1000);
    }

// org.jsoup.parser.HtmlParserTest::handlesDeepStack
    @Test public void handlesDeepStack() {
        
        

        
        StringBuilder longBody = new StringBuilder(500000);
        for (int i = 0; i < 25000; i++) {
            longBody.append(i).append("<dl><dd>");
        }
        for (int i = 0; i < 25000; i++) {
            longBody.append(i).append("</dd></dl>");
        }

        
        long start = System.currentTimeMillis();
        Document doc = Parser.parseBodyFragment(longBody.toString(), "");

        
        assertEquals(2, doc.body().childNodeSize());
        assertEquals(25000, doc.select("dd").size());
        assertTrue(System.currentTimeMillis() - start < 2000);
    }

// org.jsoup.parser.HtmlParserTest::testInvalidTableContents
    public void testInvalidTableContents() throws IOException {
        File in = ParseTest.getFile("/htmltests/table-invalid-elements.html");
        Document doc = Jsoup.parse(in, "UTF-8");
        doc.outputSettings().prettyPrint(true);
        String rendered = doc.toString();
        int endOfEmail = rendered.indexOf("Comment");
        int guarantee = rendered.indexOf("Why am I here?");
        assertTrue("Comment not found", endOfEmail > -1);
        assertTrue("Search text not found", guarantee > -1);
        assertTrue("Search text did not come after comment", guarantee > endOfEmail);
    }

// org.jsoup.parser.HtmlParserTest::testNormalisesIsIndex
    @Test public void testNormalisesIsIndex() {
        Document doc = Jsoup.parse("<body><isindex action='/submit'></body>");
        String html = doc.outerHtml();
        assertEquals("<form action=\"/submit\"> <hr> <label>This is a searchable index. Enter search keywords: <input name=\"isindex\"></label> <hr> </form>",
            StringUtil.normaliseWhitespace(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::testReinsertionModeForThCelss
    @Test public void testReinsertionModeForThCelss() {
        String body = "<body> <table> <tr> <th> <table><tr><td></td></tr></table> <div> <table><tr><td></td></tr></table> </div> <div></div> <div></div> <div></div> </th> </tr> </table> </body>";
        Document doc = Jsoup.parse(body);
        assertEquals(1, doc.body().children().size());
    }

// org.jsoup.parser.HtmlParserTest::testUsingSingleQuotesInQueries
    @Test public void testUsingSingleQuotesInQueries() {
        String body = "<body> <div class='main'>hello</div></body>";
        Document doc = Jsoup.parse(body);
        Elements main = doc.select("div[class='main']");
        assertEquals("hello", main.text());
    }

// org.jsoup.parser.HtmlParserTest::testSupportsNonAsciiTags
    @Test public void testSupportsNonAsciiTags() {
        String body = "<進捗推移グラフ>Yes</進捗推移グラフ><русский-тэг>Correct</<русский-тэг>";
        Document doc = Jsoup.parse(body);
        Elements els = doc.select("進捗推移グラフ");
        assertEquals("Yes", els.text());
        els = doc.select("русский-тэг");
        assertEquals("Correct", els.text());
    }

// org.jsoup.parser.HtmlParserTest::testSupportsPartiallyNonAsciiTags
    @Test public void testSupportsPartiallyNonAsciiTags() {
        String body = "<div>Check</divá>";
        Document doc = Jsoup.parse(body);
        Elements els = doc.select("div");
        assertEquals("Check", els.text());
    }

// org.jsoup.parser.HtmlParserTest::testFragment
    @Test public void testFragment() {
        
        String html =
            "<script type=\"text/javascript\">console.log('foo');</script>\n" +
                "<div id=\"somecontent\">some content</div>\n" +
                "<script type=\"text/javascript\">console.log('bar');</script>";

        Document body = Jsoup.parseBodyFragment(html);
        assertEquals("<script type=\"text/javascript\">console.log('foo');</script> \n" +
            "<div id=\"somecontent\">\n" +
            " some content\n" +
            "</div> \n" +
            "<script type=\"text/javascript\">console.log('bar');</script>", body.body().html());
    }

// org.jsoup.parser.HtmlParserTest::testHtmlLowerCase
    @Test public void testHtmlLowerCase() {
        String html = "<!doctype HTML><DIV ID=1>One</DIV>";
        Document doc = Jsoup.parse(html);
        assertEquals("<!doctype html> <html> <head></head> <body> <div id=\"1\"> One </div> </body> </html>", StringUtil.normaliseWhitespace(doc.outerHtml()));

        Element div = doc.selectFirst("#1");
        div.after("<TaG>One</TaG>");
        assertEquals("<tag>One</tag>", TextUtil.stripNewlines(div.nextElementSibling().outerHtml()));
    }

// org.jsoup.parser.HtmlParserTest::canPreserveTagCase
    @Test public void canPreserveTagCase() {
        Parser parser = Parser.htmlParser();
        parser.settings(new ParseSettings(true, false));
        Document doc = parser.parseInput("<div id=1><SPAN ID=2>", "");
        assertEquals("<html> <head></head> <body> <div id=\"1\"> <SPAN id=\"2\"></SPAN> </div> </body> </html>", StringUtil.normaliseWhitespace(doc.outerHtml()));

        Element div = doc.selectFirst("#1");
        div.after("<TaG ID=one>One</TaG>");
        assertEquals("<TaG id=\"one\">One</TaG>", TextUtil.stripNewlines(div.nextElementSibling().outerHtml()));
    }

// org.jsoup.parser.HtmlParserTest::canPreserveAttributeCase
    @Test public void canPreserveAttributeCase() {
        Parser parser = Parser.htmlParser();
        parser.settings(new ParseSettings(false, true));
        Document doc = parser.parseInput("<div id=1><SPAN ID=2>", "");
        assertEquals("<html> <head></head> <body> <div id=\"1\"> <span ID=\"2\"></span> </div> </body> </html>", StringUtil.normaliseWhitespace(doc.outerHtml()));

        Element div = doc.selectFirst("#1");
        div.after("<TaG ID=one>One</TaG>");
        assertEquals("<tag ID=\"one\">One</tag>", TextUtil.stripNewlines(div.nextElementSibling().outerHtml()));
    }

// org.jsoup.parser.HtmlParserTest::canPreserveBothCase
    @Test public void canPreserveBothCase() {
        Parser parser = Parser.htmlParser();
        parser.settings(new ParseSettings(true, true));
        Document doc = parser.parseInput("<div id=1><SPAN ID=2>", "");
        assertEquals("<html> <head></head> <body> <div id=\"1\"> <SPAN ID=\"2\"></SPAN> </div> </body> </html>", StringUtil.normaliseWhitespace(doc.outerHtml()));

        Element div = doc.selectFirst("#1");
        div.after("<TaG ID=one>One</TaG>");
        assertEquals("<TaG ID=\"one\">One</TaG>", TextUtil.stripNewlines(div.nextElementSibling().outerHtml()));
    }

// org.jsoup.parser.HtmlParserTest::handlesControlCodeInAttributeName
    @Test public void handlesControlCodeInAttributeName() {
        Document doc = Jsoup.parse("<p><a \06=foo>One</a><a/\06=bar><a foo\06=bar>Two</a></p>");
        assertEquals("<p><a>One</a><a></a><a foo=\"bar\">Two</a></p>", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::caseSensitiveParseTree
    @Test public void caseSensitiveParseTree() {
        String html = "<r><X>A</X><y>B</y></r>";
        Parser parser = Parser.htmlParser();
        parser.settings(ParseSettings.preserveCase);
        Document doc = parser.parseInput(html, "");
        assertEquals("<r> <X> A </X> <y> B </y> </r>", StringUtil.normaliseWhitespace(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::caseInsensitiveParseTree
    @Test public void caseInsensitiveParseTree() {
        String html = "<r><X>A</X><y>B</y></r>";
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput(html, "");
        assertEquals("<r> <x> A </x> <y> B </y> </r>", StringUtil.normaliseWhitespace(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::preservedCaseLinksCantNest
    @Test public void preservedCaseLinksCantNest() {
        String html = "<A>ONE <A>Two</A></A>";
        Document doc = Parser.htmlParser()
            .settings(ParseSettings.preserveCase)
            .parseInput(html, "");
        assertEquals("<A> ONE </A> <A> Two </A>", StringUtil.normaliseWhitespace(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::normalizesDiscordantTags
    @Test public void normalizesDiscordantTags() {
        Document document = Jsoup.parse("<div>test</DIV><p></p>");
        assertEquals("<div>\n test\n</div>\n<p></p>", document.body().html());
    }

// org.jsoup.parser.HtmlParserTest::selfClosingVoidIsNotAnError
    @Test public void selfClosingVoidIsNotAnError() {
        String html = "<p>test<br/>test<br/></p>";
        Parser parser = Parser.htmlParser().setTrackErrors(5);
        parser.parseInput(html, "");
        assertEquals(0, parser.getErrors().size());

        assertTrue(Jsoup.isValid(html, Whitelist.basic()));
        String clean = Jsoup.clean(html, Whitelist.basic());
        assertEquals("<p>test<br>test<br></p>", clean);
    }

// org.jsoup.parser.HtmlParserTest::selfClosingOnNonvoidIsError
    @Test public void selfClosingOnNonvoidIsError() {
        String html = "<p>test</p><div /><div>Two</div>";
        Parser parser = Parser.htmlParser().setTrackErrors(5);
        parser.parseInput(html, "");
        assertEquals(1, parser.getErrors().size());
        assertEquals("18: Tag cannot be self closing; not a void tag", parser.getErrors().get(0).toString());

        assertFalse(Jsoup.isValid(html, Whitelist.relaxed()));
        String clean = Jsoup.clean(html, Whitelist.relaxed());
        assertEquals("<p>test</p> <div></div> <div> Two </div>", StringUtil.normaliseWhitespace(clean));
    }

// org.jsoup.parser.HtmlParserTest::testTemplateInsideTable
    @Test public void testTemplateInsideTable() throws IOException {
        File in = ParseTest.getFile("/htmltests/table-polymer-template.html");
        Document doc = Jsoup.parse(in, "UTF-8");
        doc.outputSettings().prettyPrint(true);

        Elements templates = doc.body().getElementsByTag("template");
        for (Element template : templates) {
            assertTrue(template.childNodes().size() > 1);
        }
    }

// org.jsoup.parser.HtmlParserTest::testHandlesDeepSpans
    @Test public void testHandlesDeepSpans() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            sb.append("<span>");
        }

        sb.append("<p>One</p>");

        Document doc = Jsoup.parse(sb.toString());
        assertEquals(200, doc.select("span").size());
        assertEquals(1, doc.select("p").size());
    }

// org.jsoup.parser.HtmlParserTest::commentAtEnd
    @Test public void commentAtEnd() throws Exception {
        Document doc = Jsoup.parse("<!");
        assertTrue(doc.childNode(0) instanceof Comment);
    }

// org.jsoup.parser.HtmlParserTest::preSkipsFirstNewline
    @Test public void preSkipsFirstNewline() {
        Document doc = Jsoup.parse("<pre>\n\nOne\nTwo\n</pre>");
        Element pre = doc.selectFirst("pre");
        assertEquals("One\nTwo", pre.text());
        assertEquals("\nOne\nTwo\n", pre.wholeText());
    }

// org.jsoup.parser.HtmlParserTest::handlesXmlDeclAndCommentsBeforeDoctype
    @Test public void handlesXmlDeclAndCommentsBeforeDoctype() throws IOException {
        File in = ParseTest.getFile("/htmltests/comments.html");
        Document doc = Jsoup.parse(in, "UTF-8");

        assertEquals("<!--?xml version=\"1.0\" encoding=\"utf-8\"?--> <!-- so --><!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"> <!-- what --> <html xml:lang=\"en\" lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\"> <!-- now --> <head> <!-- then --> <meta http-equiv=\"Content-type\" content=\"text/html; charset=utf-8\"> <title>A Certain Kind of Test</title> </head> <body> <h1>Hello</h1>h1&gt; (There is a UTF8 hidden BOM at the top of this file.) </body> </html>",
            StringUtil.normaliseWhitespace(doc.html()));

        assertEquals("A Certain Kind of Test", doc.head().select("title").text());
    }

// org.jsoup.parser.HtmlParserTest::fallbackToUtfIfCantEncode
    @Test public void fallbackToUtfIfCantEncode() throws IOException {
        

        String in = "<html><meta charset=\"ISO-2022-CN\"/>One</html>";
        Document doc = Jsoup.parse(new ByteArrayInputStream(in.getBytes()), null, "");

        assertEquals("UTF-8", doc.charset().name());
        assertEquals("One", doc.text());

        String html = doc.outerHtml();
        assertEquals("<html><head><meta charset=\"UTF-8\"></head><body>One</body></html>", TextUtil.stripNewlines(html));
    }

// org.jsoup.parser.HtmlTreeBuilderStateTest::ensureArraysAreSorted
    public void ensureArraysAreSorted() {
        String[][] arrays = {
            Constants.InBodyStartToHead,
            Constants.InBodyStartPClosers,
            Constants.Headings,
            Constants.InBodyStartPreListing,
            Constants.InBodyStartLiBreakers,
            Constants.DdDt,
            Constants.Formatters,
            Constants.InBodyStartApplets,
            Constants.InBodyStartEmptyFormatters,
            Constants.InBodyStartMedia,
            Constants.InBodyStartInputAttribs,
            Constants.InBodyStartOptions,
            Constants.InBodyStartRuby,
            Constants.InBodyStartDrop,
            Constants.InBodyEndClosers,
            Constants.InBodyEndAdoptionFormatters,
            Constants.InBodyEndTableFosters,
            Constants.InCellNames,
            Constants.InCellBody,
            Constants.InCellTable,
            Constants.InCellCol,
        };

        for (String[] array : arrays) {
            String[] copy = Arrays.copyOf(array, array.length);
            Arrays.sort(array);
            assertArrayEquals(array, copy);
        }
    }

// org.jsoup.parser.HtmlTreeBuilderTest::ensureSearchArraysAreSorted
    public void ensureSearchArraysAreSorted() {
        String[][] arrays = {
            HtmlTreeBuilder.TagsSearchInScope,
            HtmlTreeBuilder.TagSearchList,
            HtmlTreeBuilder.TagSearchButton,
            HtmlTreeBuilder.TagSearchTableScope,
            HtmlTreeBuilder.TagSearchSelectScope,
            HtmlTreeBuilder.TagSearchEndTags,
            HtmlTreeBuilder.TagSearchSpecial
        };

        for (String[] array : arrays) {
            String[] copy = Arrays.copyOf(array, array.length);
            Arrays.sort(array);
            assertArrayEquals(array, copy);
        }
    }

// org.jsoup.parser.ParserTest::unescapeEntities
    public void unescapeEntities() {
        String s = Parser.unescapeEntities("One &amp; Two", false);
        assertEquals("One & Two", s);
    }

// org.jsoup.parser.ParserTest::unescapeEntitiesHandlesLargeInput
    public void unescapeEntitiesHandlesLargeInput() {
        StringBuilder longBody = new StringBuilder(500000);
        do {
            longBody.append("SomeNonEncodedInput");
        } while (longBody.length() < 64 * 1024);

        String body = longBody.toString();
        assertEquals(body, Parser.unescapeEntities(body, false));
    }

// org.jsoup.parser.TagTest::isCaseSensitive
    @Test public void isCaseSensitive() {
        Tag p1 = Tag.valueOf("P");
        Tag p2 = Tag.valueOf("p");
        assertFalse(p1.equals(p2));
    }

// org.jsoup.parser.TagTest::trims
    @Test public void trims() {
        Tag p1 = Tag.valueOf("p");
        Tag p2 = Tag.valueOf(" p ");
        assertEquals(p1, p2);
    }

// org.jsoup.parser.TagTest::equality
    @Test public void equality() {
        Tag p1 = Tag.valueOf("p");
        Tag p2 = Tag.valueOf("p");
        assertTrue(p1.equals(p2));
        assertTrue(p1 == p2);
    }

// org.jsoup.parser.TagTest::divSemantics
    @Test public void divSemantics() {
        Tag div = Tag.valueOf("div");

        assertTrue(div.isBlock());
        assertTrue(div.formatAsBlock());
    }

// org.jsoup.parser.TagTest::pSemantics
    @Test public void pSemantics() {
        Tag p = Tag.valueOf("p");

        assertTrue(p.isBlock());
        assertFalse(p.formatAsBlock());
    }

// org.jsoup.parser.TagTest::imgSemantics
    @Test public void imgSemantics() {
        Tag img = Tag.valueOf("img");
        assertTrue(img.isInline());
        assertTrue(img.isSelfClosing());
        assertFalse(img.isBlock());
    }

// org.jsoup.parser.TagTest::defaultSemantics
    @Test public void defaultSemantics() {
        Tag foo = Tag.valueOf("FOO"); 
        Tag foo2 = Tag.valueOf("FOO");

        assertEquals(foo, foo2);
        assertTrue(foo.isInline());
        assertTrue(foo.formatAsBlock());
    }

// org.jsoup.parser.TagTest::valueOfChecksNotNull
    @Test(expected = IllegalArgumentException.class) public void valueOfChecksNotNull() {
        Tag.valueOf(null);
    }

// org.jsoup.parser.TagTest::valueOfChecksNotEmpty
    @Test(expected = IllegalArgumentException.class) public void valueOfChecksNotEmpty() {
        Tag.valueOf(" ");
    }

// org.jsoup.parser.TokenQueueTest::chompBalanced
    @Test public void chompBalanced() {
        TokenQueue tq = new TokenQueue(":contains(one (two) three) four");
        String pre = tq.consumeTo("(");
        String guts = tq.chompBalanced('(', ')');
        String remainder = tq.remainder();

        assertEquals(":contains", pre);
        assertEquals("one (two) three", guts);
        assertEquals(" four", remainder);
    }

// org.jsoup.parser.TokenQueueTest::chompEscapedBalanced
    @Test public void chompEscapedBalanced() {
        TokenQueue tq = new TokenQueue(":contains(one (two) \\( \\) \\) three) four");
        String pre = tq.consumeTo("(");
        String guts = tq.chompBalanced('(', ')');
        String remainder = tq.remainder();

        assertEquals(":contains", pre);
        assertEquals("one (two) \\( \\) \\) three", guts);
        assertEquals("one (two) ( ) ) three", TokenQueue.unescape(guts));
        assertEquals(" four", remainder);
    }

// org.jsoup.parser.TokenQueueTest::chompBalancedMatchesAsMuchAsPossible
    @Test public void chompBalancedMatchesAsMuchAsPossible() {
        TokenQueue tq = new TokenQueue("unbalanced(something(or another)) else");
        tq.consumeTo("(");
        String match = tq.chompBalanced('(', ')');
        assertEquals("something(or another)", match);
    }

// org.jsoup.parser.TokenQueueTest::unescape
    @Test public void unescape() {
        assertEquals("one ( ) \\", TokenQueue.unescape("one \\( \\) \\\\"));
    }

// org.jsoup.parser.TokenQueueTest::chompToIgnoreCase
    @Test public void chompToIgnoreCase() {
        String t = "<textarea>one < two </TEXTarea>";
        TokenQueue tq = new TokenQueue(t);
        String data = tq.chompToIgnoreCase("</textarea");
        assertEquals("<textarea>one < two ", data);

        tq = new TokenQueue("<textarea> one two < three </oops>");
        data = tq.chompToIgnoreCase("</textarea");
        assertEquals("<textarea> one two < three </oops>", data);
    }

// org.jsoup.parser.TokenQueueTest::addFirst
    @Test public void addFirst() {
        TokenQueue tq = new TokenQueue("One Two");
        tq.consumeWord();
        tq.addFirst("Three");
        assertEquals("Three Two", tq.remainder());
    }

// org.jsoup.parser.TokenQueueTest::consumeToIgnoreSecondCallTest
    @Test public void consumeToIgnoreSecondCallTest() {
        String t = "<textarea>one < two </TEXTarea> third </TEXTarea>";
        TokenQueue tq = new TokenQueue(t);
        String data = tq.chompToIgnoreCase("</textarea>");
        assertEquals("<textarea>one < two ", data);

        data = tq.chompToIgnoreCase("</textarea>");
        assertEquals(" third ", data);
    }

// org.jsoup.parser.TokenQueueTest::testNestedQuotes
    @Test public void testNestedQuotes() {
        validateNestedQuotes("<html><body><a id=\"identifier\" onclick=\"func('arg')\" /></body></html>", "a[onclick*=\"('arg\"]");
        validateNestedQuotes("<html><body><a id=\"identifier\" onclick=func('arg') /></body></html>", "a[onclick*=\"('arg\"]");
        validateNestedQuotes("<html><body><a id=\"identifier\" onclick='func(\"arg\")' /></body></html>", "a[onclick*='(\"arg']");
        validateNestedQuotes("<html><body><a id=\"identifier\" onclick=func(\"arg\") /></body></html>", "a[onclick*='(\"arg']");
    }

// org.jsoup.parser.TokeniserStateTest::ensureSearchArraysAreSorted
    public void ensureSearchArraysAreSorted() {
        char[][] arrays = {
            TokeniserState.attributeSingleValueCharsSorted,
            TokeniserState.attributeDoubleValueCharsSorted,
            TokeniserState.attributeNameCharsSorted,
            TokeniserState.attributeValueUnquoted
        };

        for (char[] array : arrays) {
            char[] copy = Arrays.copyOf(array, array.length);
            Arrays.sort(array);
            assertArrayEquals(array, copy);
        }
    }

// org.jsoup.parser.TokeniserStateTest::testCharacterReferenceInRcdata
    public void testCharacterReferenceInRcdata() {
        String body = "<textarea>You&I</textarea>";
        Document doc = Jsoup.parse(body);
        Elements els = doc.select("textarea");
        assertEquals("You&I", els.text());
    }

// org.jsoup.parser.TokeniserStateTest::testBeforeTagName
    public void testBeforeTagName() {
        for (char c : whiteSpace) {
            String body = String.format("<div%c>test</div>", c);
            Document doc = Jsoup.parse(body);
            Elements els = doc.select("div");
            assertEquals("test", els.text());
        }
    }

// org.jsoup.parser.TokeniserStateTest::testEndTagOpen
    public void testEndTagOpen() {
        String body;
        Document doc;
        Elements els;

        body = "<div>hello world</";
        doc = Jsoup.parse(body);
        els = doc.select("div");
        assertEquals("hello world</", els.text());

        body = "<div>hello world</div>";
        doc = Jsoup.parse(body);
        els = doc.select("div");
        assertEquals("hello world", els.text());

        body = "<div>fake</></div>";
        doc = Jsoup.parse(body);
        els = doc.select("div");
        assertEquals("fake", els.text());

        body = "<div>fake</?</div>";
        doc = Jsoup.parse(body);
        els = doc.select("div");
        assertEquals("fake", els.text());
    }

// org.jsoup.parser.TokeniserStateTest::testRcdataLessthanSign
    public void testRcdataLessthanSign() {
        String body;
        Document doc;
        Elements els;

        body = "<textarea><fake></textarea>";
        doc = Jsoup.parse(body);
        els = doc.select("textarea");
        assertEquals("<fake>", els.text());

        body = "<textarea><open";
        doc = Jsoup.parse(body);
        els = doc.select("textarea");
        assertEquals("", els.text());

        body = "<textarea>hello world</?fake</textarea>";
        doc = Jsoup.parse(body);
        els = doc.select("textarea");
        assertEquals("hello world</?fake", els.text());
    }

// org.jsoup.parser.TokeniserStateTest::testRCDATAEndTagName
    public void testRCDATAEndTagName() {
        for (char c : whiteSpace) {
            String body = String.format("<textarea>data</textarea%c>", c);
            Document doc = Jsoup.parse(body);
            Elements els = doc.select("textarea");
            assertEquals("data", els.text());
        }
    }

// org.jsoup.parser.TokeniserStateTest::testCommentEndCoverage
    public void testCommentEndCoverage() {
        String html = "<html><head></head><body><img src=foo><!-- <table><tr><td></table> --! --- --><p>Hello</p></body></html>";
        Document doc = Jsoup.parse(html);

        Element body = doc.body();
        Comment comment = (Comment) body.childNode(1);
        assertEquals(" <table><tr><td></table> --! --- ", comment.getData());
        Element p = body.child(1);
        TextNode text = (TextNode) p.childNode(0);
        assertEquals("Hello", text.getWholeText());
    }

// org.jsoup.parser.TokeniserStateTest::testCommentEndBangCoverage
    public void testCommentEndBangCoverage() {
        String html = "<html><head></head><body><img src=foo><!-- <table><tr><td></table> --!---!>--><p>Hello</p></body></html>";
        Document doc = Jsoup.parse(html);

        Element body = doc.body();
        Comment comment = (Comment) body.childNode(1);
        assertEquals(" <table><tr><td></table> --!-", comment.getData());
        Element p = body.child(1);
        TextNode text = (TextNode) p.childNode(0);
        assertEquals("Hello", text.getWholeText());
    }

// org.jsoup.parser.TokeniserStateTest::testPublicIdentifiersWithWhitespace
    public void testPublicIdentifiersWithWhitespace() {
        String expectedOutput = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.0//EN\">";
        for (char q : quote) {
            for (char ws : whiteSpace) {
                String[] htmls = { 
                        String.format("<!DOCTYPE html%cPUBLIC %c-//W3C//DTD HTML 4.0//EN%c>", ws, q, q),
                        String.format("<!DOCTYPE html %cPUBLIC %c-//W3C//DTD HTML 4.0//EN%c>", ws, q, q),
                        String.format("<!DOCTYPE html PUBLIC%c%c-//W3C//DTD HTML 4.0//EN%c>", ws, q, q),
                        String.format("<!DOCTYPE html PUBLIC %c%c-//W3C//DTD HTML 4.0//EN%c>", ws, q, q),
                        String.format("<!DOCTYPE html PUBLIC %c-//W3C//DTD HTML 4.0//EN%c%c>", q, q, ws),
                        String.format("<!DOCTYPE html PUBLIC%c-//W3C//DTD HTML 4.0//EN%c%c>", q, q, ws)
                    };
                for (String html : htmls) {
                    Document doc = Jsoup.parse(html);
                    assertEquals(expectedOutput, doc.childNode(0).outerHtml());
                }
            }
        }
    }

// org.jsoup.parser.TokeniserStateTest::testSystemIdentifiersWithWhitespace
    public void testSystemIdentifiersWithWhitespace() {
        String expectedOutput = "<!DOCTYPE html SYSTEM \"http://www.w3.org/TR/REC-html40/strict.dtd\">";
        for (char q : quote) {
            for (char ws : whiteSpace) {
                String[] htmls = {
                        String.format("<!DOCTYPE html%cSYSTEM %chttp://www.w3.org/TR/REC-html40/strict.dtd%c>", ws, q, q),
                        String.format("<!DOCTYPE html %cSYSTEM %chttp://www.w3.org/TR/REC-html40/strict.dtd%c>", ws, q, q),
                        String.format("<!DOCTYPE html SYSTEM%c%chttp://www.w3.org/TR/REC-html40/strict.dtd%c>", ws, q, q),
                        String.format("<!DOCTYPE html SYSTEM %c%chttp://www.w3.org/TR/REC-html40/strict.dtd%c>", ws, q, q),
                        String.format("<!DOCTYPE html SYSTEM %chttp://www.w3.org/TR/REC-html40/strict.dtd%c%c>", q, q, ws),
                        String.format("<!DOCTYPE html SYSTEM%chttp://www.w3.org/TR/REC-html40/strict.dtd%c%c>", q, q, ws)
                    };
                for (String html : htmls) {
                    Document doc = Jsoup.parse(html);
                    assertEquals(expectedOutput, doc.childNode(0).outerHtml());
                }
            }
        }
    }

// org.jsoup.parser.TokeniserStateTest::testPublicAndSystemIdentifiersWithWhitespace
    public void testPublicAndSystemIdentifiersWithWhitespace() {
        String expectedOutput = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.0//EN\""
                + " \"http://www.w3.org/TR/REC-html40/strict.dtd\">";
    	for (char q : quote) {
            for (char ws : whiteSpace) {
                String[] htmls = {
                        String.format("<!DOCTYPE html PUBLIC %c-//W3C//DTD HTML 4.0//EN%c"
                                + "%c%chttp://www.w3.org/TR/REC-html40/strict.dtd%c>", q, q, ws, q, q),
                        String.format("<!DOCTYPE html PUBLIC %c-//W3C//DTD HTML 4.0//EN%c"
                                + "%chttp://www.w3.org/TR/REC-html40/strict.dtd%c>", q, q, q, q)
                    };
                for (String html : htmls) {
                    Document doc = Jsoup.parse(html);
                    assertEquals(expectedOutput, doc.childNode(0).outerHtml());
                }
            }
        }
    }

// org.jsoup.parser.TokeniserStateTest::handlesLessInTagThanAsNewTag
    @Test public void handlesLessInTagThanAsNewTag() {
        
        String html = "<p\n<p<div id=one <span>Two";
        Document doc = Jsoup.parse(html);
        assertEquals("<p></p><p></p><div id=\"one\"><span>Two</span></div>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.TokeniserTest::bufferUpInAttributeVal
    public void bufferUpInAttributeVal() {
        

        
        String[] quotes = {"\"", "'", ""};
        for (String quote : quotes) {
            String preamble = "<img src=" + quote;
            String tail = "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb";
            StringBuilder sb = new StringBuilder(preamble);

            final int charsToFillBuffer = maxBufferLen - preamble.length();
            for (int i = 0; i < charsToFillBuffer; i++) {
                sb.append('a');
            }

            sb.append('X'); 
            sb.append(tail + quote + ">\n");

            String html = sb.toString();
            Document doc = Jsoup.parse(html);
            String src = doc.select("img").attr("src");

            assertTrue("Handles for quote " + quote, src.contains("X"));
            assertTrue(src.contains(tail));
        }
    }

// org.jsoup.parser.TokeniserTest::handleSuperLargeTagNames
    @Test public void handleSuperLargeTagNames() {
        

        StringBuilder sb = new StringBuilder(maxBufferLen);
        do {
            sb.append("LargeTagName");
        } while (sb.length() < maxBufferLen);
        String tag = sb.toString();
        String html = "<" + tag + ">One</" + tag + ">";

        Document doc = Parser.htmlParser().settings(ParseSettings.preserveCase).parseInput(html, "");
        Elements els = doc.select(tag);
        assertEquals(1, els.size());
        Element el = els.first();
        assertNotNull(el);
        assertEquals("One", el.text());
        assertEquals(tag, el.tagName());
    }

// org.jsoup.parser.TokeniserTest::handleSuperLargeAttributeName
    @Test public void handleSuperLargeAttributeName() {
        StringBuilder sb = new StringBuilder(maxBufferLen);
        do {
            sb.append("LargAttributeName");
        } while (sb.length() < maxBufferLen);
        String attrName = sb.toString();
        String html = "<p " + attrName + "=foo>One</p>";

        Document doc = Jsoup.parse(html);
        Elements els = doc.getElementsByAttribute(attrName);
        assertEquals(1, els.size());
        Element el = els.first();
        assertNotNull(el);
        assertEquals("One", el.text());
        Attribute attribute = el.attributes().asList().get(0);
        assertEquals(attrName.toLowerCase(), attribute.getKey());
        assertEquals("foo", attribute.getValue());
    }

// org.jsoup.parser.TokeniserTest::handleLargeText
    @Test public void handleLargeText() {
        StringBuilder sb = new StringBuilder(maxBufferLen);
        do {
            sb.append("A Large Amount of Text");
        } while (sb.length() < maxBufferLen);
        String text = sb.toString();
        String html = "<p>" + text + "</p>";

        Document doc = Jsoup.parse(html);
        Elements els = doc.select("p");
        assertEquals(1, els.size());
        Element el = els.first();

        assertNotNull(el);
        assertEquals(text, el.text());
    }

// org.jsoup.parser.TokeniserTest::handleLargeComment
    @Test public void handleLargeComment() {
        StringBuilder sb = new StringBuilder(maxBufferLen);
        do {
            sb.append("Quite a comment ");
        } while (sb.length() < maxBufferLen);
        String comment = sb.toString();
        String html = "<p><!-- " + comment + " --></p>";

        Document doc = Jsoup.parse(html);
        Elements els = doc.select("p");
        assertEquals(1, els.size());
        Element el = els.first();

        assertNotNull(el);
        Comment child = (Comment) el.childNode(0);
        assertEquals(" " + comment + " ", child.getData());
    }

// org.jsoup.parser.TokeniserTest::handleLargeCdata
    @Test public void handleLargeCdata() {
        StringBuilder sb = new StringBuilder(maxBufferLen);
        do {
            sb.append("Quite a lot of CDATA <><><><>");
        } while (sb.length() < maxBufferLen);
        String cdata = sb.toString();
        String html = "<p><![CDATA[" + cdata + "]]></p>";

        Document doc = Jsoup.parse(html);
        Elements els = doc.select("p");
        assertEquals(1, els.size());
        Element el = els.first();

        assertNotNull(el);
        TextNode child = (TextNode) el.childNode(0);
        assertEquals(cdata, el.text());
        assertEquals(cdata, child.getWholeText());
    }

// org.jsoup.parser.TokeniserTest::handleLargeTitle
    @Test public void handleLargeTitle() {
        StringBuilder sb = new StringBuilder(maxBufferLen);
        do {
            sb.append("Quite a long title");
        } while (sb.length() < maxBufferLen);
        String title = sb.toString();
        String html = "<title>" + title + "</title>";

        Document doc = Jsoup.parse(html);
        Elements els = doc.select("title");
        assertEquals(1, els.size());
        Element el = els.first();

        assertNotNull(el);
        TextNode child = (TextNode) el.childNode(0);
        assertEquals(title, el.text());
        assertEquals(title, child.getWholeText());
        assertEquals(title, doc.title());
    }

// org.jsoup.parser.TokeniserTest::cp1252Entities
    @Test public void cp1252Entities() {
        assertEquals("\u20ac", Jsoup.parse("&#0128;").text());
        assertEquals("\u201a", Jsoup.parse("&#0130;").text());
        assertEquals("\u20ac", Jsoup.parse("&#x80;").text());
    }

// org.jsoup.parser.TokeniserTest::cp1252EntitiesProduceError
    @Test public void cp1252EntitiesProduceError() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        parser.setTrackErrors(10);
        assertEquals("\u20ac", parser.parseInput("<html><body>&#0128;</body></html>", "").text());
        assertEquals(1, parser.getErrors().size());
    }

// org.jsoup.parser.TokeniserTest::cp1252SubstitutionTable
    @Test public void cp1252SubstitutionTable() throws UnsupportedEncodingException {
        for (int i = 0; i < Tokeniser.win1252Extensions.length; i++) {
            String s = new String(new byte[]{ (byte) (i + Tokeniser.win1252ExtensionsStart) }, "Windows-1252");
            assertEquals(1, s.length());

            
            if (s.charAt(0) == '\ufffd') { continue; }

            assertEquals("At: " + i, s.charAt(0), Tokeniser.win1252Extensions[i]);
        }
    }

// org.jsoup.parser.XmlTreeBuilderTest::testSimpleXmlParse
    public void testSimpleXmlParse() {
        String xml = "<doc id=2 href='/bar'>Foo <br /><link>One</link><link>Two</link></doc>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://foo.com/");
        assertEquals("<doc id=\"2\" href=\"/bar\">Foo <br /><link>One</link><link>Two</link></doc>",
                TextUtil.stripNewlines(doc.html()));
        assertEquals(doc.getElementById("2").absUrl("href"), "http://foo.com/bar");
    }

// org.jsoup.parser.XmlTreeBuilderTest::testPopToClose
    public void testPopToClose() {
        
        String xml = "<doc><val>One<val>Two</val></bar>Three</doc>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://foo.com/");
        assertEquals("<doc><val>One<val>Two</val>Three</val></doc>",
                TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.XmlTreeBuilderTest::testCommentAndDocType
    public void testCommentAndDocType() {
        String xml = "<!DOCTYPE HTML><!-- a comment -->One <qux />Two";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://foo.com/");
        assertEquals("<!DOCTYPE HTML><!-- a comment -->One <qux />Two",
                TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.XmlTreeBuilderTest::testSupplyParserToJsoupClass
    public void testSupplyParserToJsoupClass() {
        String xml = "<doc><val>One<val>Two</val></bar>Three</doc>";
        Document doc = Jsoup.parse(xml, "http://foo.com/", Parser.xmlParser());
        assertEquals("<doc><val>One<val>Two</val>Three</val></doc>",
                TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.XmlTreeBuilderTest::testSupplyParserToConnection
    public void testSupplyParserToConnection() throws IOException {
        String xmlUrl = "http://direct.infohound.net/tools/jsoup-xml-test.xml";

        
        Document xmlDoc = Jsoup.connect(xmlUrl).parser(Parser.xmlParser()).get();
        Document htmlDoc = Jsoup.connect(xmlUrl).parser(Parser.htmlParser()).get();
        Document autoXmlDoc = Jsoup.connect(xmlUrl).get(); 

        assertEquals("<doc><val>One<val>Two</val>Three</val></doc>",
                TextUtil.stripNewlines(xmlDoc.html()));
        assertFalse(htmlDoc.equals(xmlDoc));
        assertEquals(xmlDoc, autoXmlDoc);
        assertEquals(1, htmlDoc.select("head").size()); 
        assertEquals(0, xmlDoc.select("head").size()); 
        assertEquals(0, autoXmlDoc.select("head").size()); 
    }

// org.jsoup.parser.XmlTreeBuilderTest::testSupplyParserToDataStream
    public void testSupplyParserToDataStream() throws IOException, URISyntaxException {
        File xmlFile = new File(XmlTreeBuilder.class.getResource("/htmltests/xml-test.xml").toURI());
        InputStream inStream = new FileInputStream(xmlFile);
        Document doc = Jsoup.parse(inStream, null, "http://foo.com", Parser.xmlParser());
        assertEquals("<doc><val>One<val>Two</val>Three</val></doc>",
                TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.XmlTreeBuilderTest::testDoesNotForceSelfClosingKnownTags
    public void testDoesNotForceSelfClosingKnownTags() {
        
        Document htmlDoc = Jsoup.parse("<br>one</br>");
        assertEquals("<br>one\n<br>", htmlDoc.body().html());

        Document xmlDoc = Jsoup.parse("<br>one</br>", "", Parser.xmlParser());
        assertEquals("<br>one</br>", xmlDoc.html());
    }

// org.jsoup.parser.XmlTreeBuilderTest::handlesXmlDeclarationAsDeclaration
    @Test public void handlesXmlDeclarationAsDeclaration() {
        String html = "<?xml encoding='UTF-8' ?><body>One</body><!-- comment -->";
        Document doc = Jsoup.parse(html, "", Parser.xmlParser());
        assertEquals("<?xml encoding=\"UTF-8\"?> <body> One </body> <!-- comment -->",
                StringUtil.normaliseWhitespace(doc.outerHtml()));
        assertEquals("#declaration", doc.childNode(0).nodeName());
        assertEquals("#comment", doc.childNode(2).nodeName());
    }

// org.jsoup.parser.XmlTreeBuilderTest::xmlFragment
    @Test public void xmlFragment() {
        String xml = "<one src='/foo/' />Two<three><four /></three>";
        List<Node> nodes = Parser.parseXmlFragment(xml, "http://example.com/");
        assertEquals(3, nodes.size());

        assertEquals("http://example.com/foo/", nodes.get(0).absUrl("src"));
        assertEquals("one", nodes.get(0).nodeName());
        assertEquals("Two", ((TextNode)nodes.get(1)).text());
    }

// org.jsoup.parser.XmlTreeBuilderTest::xmlParseDefaultsToHtmlOutputSyntax
    @Test public void xmlParseDefaultsToHtmlOutputSyntax() {
        Document doc = Jsoup.parse("x", "", Parser.xmlParser());
        assertEquals(Syntax.xml, doc.outputSettings().syntax());
    }

// org.jsoup.parser.XmlTreeBuilderTest::testDoesHandleEOFInTag
    public void testDoesHandleEOFInTag() {
        String html = "<img src=asdf onerror=\"alert(1)\" x=";
        Document xmlDoc = Jsoup.parse(html, "", Parser.xmlParser());
        assertEquals("<img src=\"asdf\" onerror=\"alert(1)\" x=\"\" />", xmlDoc.html());
    }

// org.jsoup.parser.XmlTreeBuilderTest::testDetectCharsetEncodingDeclaration
    public void testDetectCharsetEncodingDeclaration() throws IOException, URISyntaxException {
        File xmlFile = new File(XmlTreeBuilder.class.getResource("/htmltests/xml-charset.xml").toURI());
        InputStream inStream = new FileInputStream(xmlFile);
        Document doc = Jsoup.parse(inStream, null, "http://example.com/", Parser.xmlParser());
        assertEquals("ISO-8859-1", doc.charset().name());
        assertEquals("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?> <data>äöåéü</data>",
            TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.XmlTreeBuilderTest::testParseDeclarationAttributes
    public void testParseDeclarationAttributes() {
        String xml = "<?xml version='1' encoding='UTF-8' something='else'?><val>One</val>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        XmlDeclaration decl = (XmlDeclaration) doc.childNode(0);
        assertEquals("1", decl.attr("version"));
        assertEquals("UTF-8", decl.attr("encoding"));
        assertEquals("else", decl.attr("something"));
        assertEquals("version=\"1\" encoding=\"UTF-8\" something=\"else\"", decl.getWholeDeclaration());
        assertEquals("<?xml version=\"1\" encoding=\"UTF-8\" something=\"else\"?>", decl.outerHtml());
    }

// org.jsoup.parser.XmlTreeBuilderTest::caseSensitiveDeclaration
    public void caseSensitiveDeclaration() {
        String xml = "<?XML version='1' encoding='UTF-8' something='else'?>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertEquals("<?XML version=\"1\" encoding=\"UTF-8\" something=\"else\"?>", doc.outerHtml());
    }

// org.jsoup.parser.XmlTreeBuilderTest::testCreatesValidProlog
    public void testCreatesValidProlog() {
        Document document = Document.createShell("");
        document.outputSettings().syntax(Syntax.xml);
        document.charset(Charset.forName("utf-8"));
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<html>\n" +
            " <head></head>\n" +
            " <body></body>\n" +
            "</html>", document.outerHtml());
    }

// org.jsoup.parser.XmlTreeBuilderTest::preservesCaseByDefault
    public void preservesCaseByDefault() {
        String xml = "<CHECK>One</CHECK><TEST ID=1>Check</TEST>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertEquals("<CHECK>One</CHECK><TEST ID=\"1\">Check</TEST>", TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.XmlTreeBuilderTest::appendPreservesCaseByDefault
    public void appendPreservesCaseByDefault() {
        String xml = "<One>One</One>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Elements one = doc.select("One");
        one.append("<Two ID=2>Two</Two>");
        assertEquals("<One>One<Two ID=\"2\">Two</Two></One>", TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.XmlTreeBuilderTest::canNormalizeCase
    public void canNormalizeCase() {
        String xml = "<TEST ID=1>Check</TEST>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser().settings(ParseSettings.htmlDefault));
        assertEquals("<test id=\"1\">Check</test>", TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.XmlTreeBuilderTest::normalizesDiscordantTags
    @Test public void normalizesDiscordantTags() {
        Parser parser = Parser.xmlParser().settings(ParseSettings.htmlDefault);
        Document document = Jsoup.parse("<div>test</DIV><p></p>", "", parser);
        assertEquals("<div>\n test\n</div>\n<p></p>", document.html());
        
    }

// org.jsoup.parser.XmlTreeBuilderTest::roundTripsCdata
    @Test public void roundTripsCdata() {
        String xml = "<div id=1><![CDATA[\n<html>\n <foo><&amp;]]></div>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

        Element div = doc.getElementById("1");
        assertEquals("<html>\n <foo><&amp;", div.text());
        assertEquals(0, div.children().size());
        assertEquals(1, div.childNodeSize()); 

        assertEquals("<div id=\"1\"><![CDATA[\n<html>\n <foo><&amp;]]>\n</div>", div.outerHtml());

        CDataNode cdata = (CDataNode) div.textNodes().get(0);
        assertEquals("\n<html>\n <foo><&amp;", cdata.text());
    }

// org.jsoup.parser.XmlTreeBuilderTest::cdataPreservesWhiteSpace
    @Test public void cdataPreservesWhiteSpace() {
        String xml = "<script type=\"text/javascript\">//<![CDATA[\n\n  foo();\n//]]></script>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertEquals(xml, doc.outerHtml());

        assertEquals("//\n\n  foo();\n//", doc.selectFirst("script").text());
    }

// org.jsoup.parser.XmlTreeBuilderTest::handlesDodgyXmlDecl
    public void handlesDodgyXmlDecl() {
        String xml = "<?xml version='1.0'><val>One</val>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertEquals("One", doc.select("val").text());
    }

// org.jsoup.parser.XmlTreeBuilderTest::handlesLTinScript
    public void handlesLTinScript() {
        
        String html = "<script> var a=\"<?\"; var b=\"?>\"; </script>";
        Document doc = Jsoup.parse(html, "", Parser.xmlParser());
        assertEquals("<script> var a=\"\n <!--?\"; var b=\"?-->\"; </script>", doc.html()); 
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
        assertEquals("<p><img src=\"http://example.com/\" alt=\"Image\"></p><p><img></p>", TextUtil.stripNewlines(cleanHtml));
    }

// org.jsoup.safety.CleanerTest::testRelaxed
    @Test public void testRelaxed() {
        String h = "<h1>Head</h1><table><tr><td>One<td>Two</td></tr></table>";
        String cleanHtml = Jsoup.clean(h, Whitelist.relaxed());
        assertEquals("<h1>Head</h1><table><tbody><tr><td>One</td><td>Two</td></tr></tbody></table>", TextUtil.stripNewlines(cleanHtml));
    }

// org.jsoup.safety.CleanerTest::testRemoveTags
    @Test public void testRemoveTags() {
        String h = "<div><p><A HREF='HTTP://nice.com'>Nice</a></p><blockquote>Hello</blockquote>";
        String cleanHtml = Jsoup.clean(h, Whitelist.basic().removeTags("a"));

        assertEquals("<p>Nice</p><blockquote>Hello</blockquote>", TextUtil.stripNewlines(cleanHtml));
    }

// org.jsoup.safety.CleanerTest::testRemoveAttributes
    @Test public void testRemoveAttributes() {
        String h = "<div><p>Nice</p><blockquote cite='http://example.com/quotations'>Hello</blockquote>";
        String cleanHtml = Jsoup.clean(h, Whitelist.basic().removeAttributes("blockquote", "cite"));

        assertEquals("<p>Nice</p><blockquote>Hello</blockquote>", TextUtil.stripNewlines(cleanHtml));
    }

// org.jsoup.safety.CleanerTest::testRemoveEnforcedAttributes
    @Test public void testRemoveEnforcedAttributes() {
        String h = "<div><p><A HREF='HTTP://nice.com'>Nice</a></p><blockquote>Hello</blockquote>";
        String cleanHtml = Jsoup.clean(h, Whitelist.basic().removeEnforcedAttribute("a", "rel"));

        assertEquals("<p><a href=\"http://nice.com\">Nice</a></p><blockquote>Hello</blockquote>",
                TextUtil.stripNewlines(cleanHtml));
    }

// org.jsoup.safety.CleanerTest::testRemoveProtocols
    @Test public void testRemoveProtocols() {
        String h = "<p>Contact me <a href='mailto:info@example.com'>here</a></p>";
        String cleanHtml = Jsoup.clean(h, Whitelist.basic().removeProtocols("a", "href", "ftp", "mailto"));

        assertEquals("<p>Contact me <a rel=\"nofollow\">here</a></p>",
                TextUtil.stripNewlines(cleanHtml));
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
        assertEquals("<img>", cleanHtml);
    }

// org.jsoup.safety.CleanerTest::testCleanJavascriptHref
    @Test public void testCleanJavascriptHref() {
        String h = "<A HREF=\"javascript:document.location='http://www.google.com/'\">XSS</A>";
        String cleanHtml = Jsoup.clean(h, Whitelist.relaxed());
        assertEquals("<a>XSS</a>", cleanHtml);
    }

// org.jsoup.safety.CleanerTest::testCleanAnchorProtocol
    @Test public void testCleanAnchorProtocol() {
        String validAnchor = "<a href=\"#valid\">Valid anchor</a>";
        String invalidAnchor = "<a href=\"#anchor with spaces\">Invalid anchor</a>";

        
        String cleanHtml = Jsoup.clean(validAnchor, Whitelist.relaxed());
        assertEquals("<a>Valid anchor</a>", cleanHtml);

        cleanHtml = Jsoup.clean(invalidAnchor, Whitelist.relaxed());
        assertEquals("<a>Invalid anchor</a>", cleanHtml);

        
        Whitelist relaxedWithAnchor = Whitelist.relaxed().addProtocols("a", "href", "#");

        cleanHtml = Jsoup.clean(validAnchor, relaxedWithAnchor);
        assertEquals(validAnchor, cleanHtml);

        
        cleanHtml = Jsoup.clean(invalidAnchor, relaxedWithAnchor);
        assertEquals("<a>Invalid anchor</a>", cleanHtml);
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
        assertEquals("<img alt=\"\">", cleanHtml);
    }

// org.jsoup.safety.CleanerTest::testIsValidBodyHtml
    @Test public void testIsValidBodyHtml() {
        String ok = "<p>Test <b><a href='http://example.com/' rel='nofollow'>OK</a></b></p>";
        String ok1 = "<p>Test <b><a href='http://example.com/'>OK</a></b></p>"; 
        String nok1 = "<p><script></script>Not <b>OK</b></p>";
        String nok2 = "<p align=right>Test Not <b>OK</b></p>";
        String nok3 = "<!-- comment --><p>Not OK</p>"; 
        String nok4 = "<html><head>Foo</head><body><b>OK</b></body></html>"; 
        String nok5 = "<p>Test <b><a href='http://example.com/' rel='nofollowme'>OK</a></b></p>";
        String nok6 = "<p>Test <b><a href='http://example.com/'>OK</b></p>"; 
        String nok7 = "</div>What";
        assertTrue(Jsoup.isValid(ok, Whitelist.basic()));
        assertTrue(Jsoup.isValid(ok1, Whitelist.basic()));
        assertFalse(Jsoup.isValid(nok1, Whitelist.basic()));
        assertFalse(Jsoup.isValid(nok2, Whitelist.basic()));
        assertFalse(Jsoup.isValid(nok3, Whitelist.basic()));
        assertFalse(Jsoup.isValid(nok4, Whitelist.basic()));
        assertFalse(Jsoup.isValid(nok5, Whitelist.basic()));
        assertFalse(Jsoup.isValid(nok6, Whitelist.basic()));
        assertFalse(Jsoup.isValid(ok, Whitelist.none()));
        assertFalse(Jsoup.isValid(nok7, Whitelist.basic()));
    }

// org.jsoup.safety.CleanerTest::testIsValidDocument
    @Test public void testIsValidDocument() {
        String ok = "<html><head></head><body><p>Hello</p></body><html>";
        String nok = "<html><head><script>woops</script><title>Hello</title></head><body><p>Hello</p></body><html>";

        Whitelist relaxed = Whitelist.relaxed();
        Cleaner cleaner = new Cleaner(relaxed);
        Document okDoc = Jsoup.parse(ok);
        assertTrue(cleaner.isValid(okDoc));
        assertFalse(cleaner.isValid(Jsoup.parse(nok)));
        assertFalse(new Cleaner(Whitelist.none()).isValid(okDoc));
    }

// org.jsoup.safety.CleanerTest::resolvesRelativeLinks
    @Test public void resolvesRelativeLinks() {
        String html = "<a href='/foo'>Link</a><img src='/bar'>";
        String clean = Jsoup.clean(html, "http://example.com/", Whitelist.basicWithImages());
        assertEquals("<a href=\"http://example.com/foo\" rel=\"nofollow\">Link</a>\n<img src=\"http://example.com/bar\">", clean);
    }

// org.jsoup.safety.CleanerTest::preservesRelativeLinksIfConfigured
    @Test public void preservesRelativeLinksIfConfigured() {
        String html = "<a href='/foo'>Link</a><img src='/bar'> <img src='javascript:alert()'>";
        String clean = Jsoup.clean(html, "http://example.com/", Whitelist.basicWithImages().preserveRelativeLinks(true));
        assertEquals("<a href=\"/foo\" rel=\"nofollow\">Link</a>\n<img src=\"/bar\"> \n<img>", clean);
    }

// org.jsoup.safety.CleanerTest::dropsUnresolvableRelativeLinks
    @Test public void dropsUnresolvableRelativeLinks() {
        String html = "<a href='/foo'>Link</a>";
        String clean = Jsoup.clean(html, Whitelist.basic());
        assertEquals("<a rel=\"nofollow\">Link</a>", clean);
    }

// org.jsoup.safety.CleanerTest::handlesCustomProtocols
    @Test public void handlesCustomProtocols() {
        String html = "<img src='cid:12345' /> <img src='data:gzzt' />";
        String dropped = Jsoup.clean(html, Whitelist.basicWithImages());
        assertEquals("<img> \n<img>", dropped);

        String preserved = Jsoup.clean(html, Whitelist.basicWithImages().addProtocols("img", "src", "cid", "data"));
        assertEquals("<img src=\"cid:12345\"> \n<img src=\"data:gzzt\">", preserved);
    }

// org.jsoup.safety.CleanerTest::handlesAllPseudoTag
    @Test public void handlesAllPseudoTag() {
        String html = "<p class='foo' src='bar'><a class='qux'>link</a></p>";
        Whitelist whitelist = new Whitelist()
                .addAttributes(":all", "class")
                .addAttributes("p", "style")
                .addTags("p", "a");

        String clean = Jsoup.clean(html, whitelist);
        assertEquals("<p class=\"foo\"><a class=\"qux\">link</a></p>", clean);
    }

// org.jsoup.safety.CleanerTest::addsTagOnAttributesIfNotSet
    @Test public void addsTagOnAttributesIfNotSet() {
        String html = "<p class='foo' src='bar'>One</p>";
        Whitelist whitelist = new Whitelist()
            .addAttributes("p", "class");
        
        String clean = Jsoup.clean(html, whitelist);
        assertEquals("<p class=\"foo\">One</p>", clean);
    }

// org.jsoup.safety.CleanerTest::supplyOutputSettings
    @Test public void supplyOutputSettings() {
        
        Document.OutputSettings os = new Document.OutputSettings();
        os.prettyPrint(false);
        os.escapeMode(Entities.EscapeMode.extended);
        os.charset("ascii");

        String html = "<div><p>&bernou;</p></div>";
        String customOut = Jsoup.clean(html, "http://foo.com/", Whitelist.relaxed(), os);
        String defaultOut = Jsoup.clean(html, "http://foo.com/", Whitelist.relaxed());
        assertNotSame(defaultOut, customOut);

        assertEquals("<div><p>&Bscr;</p></div>", customOut); 
        assertEquals("<div>\n" +
            " <p>ℬ</p>\n" +
            "</div>", defaultOut);

        os.charset("ASCII");
        os.escapeMode(Entities.EscapeMode.base);
        String customOut2 = Jsoup.clean(html, "http://foo.com/", Whitelist.relaxed(), os);
        assertEquals("<div><p>&#x212c;</p></div>", customOut2);
    }

// org.jsoup.safety.CleanerTest::handlesFramesets
    @Test public void handlesFramesets() {
        String dirty = "<html><head><script></script><noscript></noscript></head><frameset><frame src=\"foo\" /><frame src=\"foo\" /></frameset></html>";
        String clean = Jsoup.clean(dirty, Whitelist.basic());
        assertEquals("", clean); 

        Document dirtyDoc = Jsoup.parse(dirty);
        Document cleanDoc = new Cleaner(Whitelist.basic()).clean(dirtyDoc);
        assertFalse(cleanDoc == null);
        assertEquals(0, cleanDoc.body().childNodeSize());
    }

// org.jsoup.safety.CleanerTest::cleansInternationalText
    @Test public void cleansInternationalText() {
        assertEquals("привет", Jsoup.clean("привет", Whitelist.none()));
    }

// org.jsoup.safety.CleanerTest::testScriptTagInWhiteList
    public void testScriptTagInWhiteList() {
        Whitelist whitelist = Whitelist.relaxed();
        whitelist.addTags( "script" );
        assertTrue( Jsoup.isValid("Hello<script>alert('Doh')</script>World !", whitelist ) );
    }

// org.jsoup.safety.CleanerTest::bailsIfRemovingProtocolThatsNotSet
    public void bailsIfRemovingProtocolThatsNotSet() {
        
        Whitelist w = Whitelist.none();

        
        w.addAttributes("a", "href");
        w.removeProtocols("a", "href", "javascript"); 
    }

// org.jsoup.safety.CleanerTest::handlesControlCharactersAfterTagName
    @Test public void handlesControlCharactersAfterTagName() {
        String html = "<a/\06>";
        String clean = Jsoup.clean(html, Whitelist.basic());
        assertEquals("<a rel=\"nofollow\"></a>", clean);
    }

// org.jsoup.safety.CleanerTest::handlesAttributesWithNoValue
    @Test public void handlesAttributesWithNoValue() {
        
        String clean = Jsoup.clean("<a href>Clean</a>", Whitelist.basic());

        assertEquals("<a rel=\"nofollow\">Clean</a>", clean);
    }

// org.jsoup.select.CssTest::firstChild
	public void firstChild() {
		check(html.select("#pseudo :first-child"), "1");
		check(html.select("html:first-child"));
	}

// org.jsoup.select.CssTest::lastChild
	public void lastChild() {
		check(html.select("#pseudo :last-child"), "10");
		check(html.select("html:last-child"));
	}

// org.jsoup.select.CssTest::nthChild_simple
	public void nthChild_simple() {
		for(int i = 1; i <=10; i++) {
			check(html.select(String.format("#pseudo :nth-child(%d)", i)), String.valueOf(i));
		}
	}

// org.jsoup.select.CssTest::nthOfType_unknownTag
    public void nthOfType_unknownTag() {
        for(int i = 1; i <=10; i++) {
            check(html.select(String.format("#type svg:nth-of-type(%d)", i)), String.valueOf(i));
        }
    }

// org.jsoup.select.CssTest::nthLastChild_simple
	public void nthLastChild_simple() {
		for(int i = 1; i <=10; i++) {
			check(html.select(String.format("#pseudo :nth-last-child(%d)", i)), String.valueOf(11-i));
		}
	}

// org.jsoup.select.CssTest::nthOfType_simple
	public void nthOfType_simple() {
		for(int i = 1; i <=10; i++) {
			check(html.select(String.format("#type p:nth-of-type(%d)", i)), String.valueOf(i));
		}
	}

// org.jsoup.select.CssTest::nthLastOfType_simple
	public void nthLastOfType_simple() {
		for(int i = 1; i <=10; i++) {
			check(html.select(String.format("#type :nth-last-of-type(%d)", i)), String.valueOf(11-i),String.valueOf(11-i),String.valueOf(11-i),String.valueOf(11-i));
		}
	}

// org.jsoup.select.CssTest::nthChild_advanced
	public void nthChild_advanced() {
		check(html.select("#pseudo :nth-child(-5)"));
		check(html.select("#pseudo :nth-child(odd)"), "1", "3", "5", "7", "9");
		check(html.select("#pseudo :nth-child(2n-1)"), "1", "3", "5", "7", "9");
		check(html.select("#pseudo :nth-child(2n+1)"), "1", "3", "5", "7", "9");
		check(html.select("#pseudo :nth-child(2n+3)"), "3", "5", "7", "9");
		check(html.select("#pseudo :nth-child(even)"), "2", "4", "6", "8", "10");
		check(html.select("#pseudo :nth-child(2n)"), "2", "4", "6", "8", "10");
		check(html.select("#pseudo :nth-child(3n-1)"), "2", "5", "8");
		check(html.select("#pseudo :nth-child(-2n+5)"), "1", "3", "5");
		check(html.select("#pseudo :nth-child(+5)"), "5");
	}

// org.jsoup.select.CssTest::nthOfType_advanced
	public void nthOfType_advanced() {
		check(html.select("#type :nth-of-type(-5)"));
		check(html.select("#type p:nth-of-type(odd)"), "1", "3", "5", "7", "9");
		check(html.select("#type em:nth-of-type(2n-1)"), "1", "3", "5", "7", "9");
		check(html.select("#type p:nth-of-type(2n+1)"), "1", "3", "5", "7", "9");
		check(html.select("#type span:nth-of-type(2n+3)"), "3", "5", "7", "9");
		check(html.select("#type p:nth-of-type(even)"), "2", "4", "6", "8", "10");
		check(html.select("#type p:nth-of-type(2n)"), "2", "4", "6", "8", "10");
		check(html.select("#type p:nth-of-type(3n-1)"), "2", "5", "8");
		check(html.select("#type p:nth-of-type(-2n+5)"), "1", "3", "5");
		check(html.select("#type :nth-of-type(+5)"), "5", "5", "5", "5");
	}

// org.jsoup.select.CssTest::nthLastChild_advanced
	public void nthLastChild_advanced() {
		check(html.select("#pseudo :nth-last-child(-5)"));
		check(html.select("#pseudo :nth-last-child(odd)"), "2", "4", "6", "8", "10");
		check(html.select("#pseudo :nth-last-child(2n-1)"), "2", "4", "6", "8", "10");
		check(html.select("#pseudo :nth-last-child(2n+1)"), "2", "4", "6", "8", "10");
		check(html.select("#pseudo :nth-last-child(2n+3)"), "2", "4", "6", "8");
		check(html.select("#pseudo :nth-last-child(even)"), "1", "3", "5", "7", "9");
		check(html.select("#pseudo :nth-last-child(2n)"), "1", "3", "5", "7", "9");
		check(html.select("#pseudo :nth-last-child(3n-1)"), "3", "6", "9");

		check(html.select("#pseudo :nth-last-child(-2n+5)"), "6", "8", "10");
		check(html.select("#pseudo :nth-last-child(+5)"), "6");
	}

// org.jsoup.select.CssTest::nthLastOfType_advanced
	public void nthLastOfType_advanced() {
		check(html.select("#type :nth-last-of-type(-5)"));
		check(html.select("#type p:nth-last-of-type(odd)"), "2", "4", "6", "8", "10");
		check(html.select("#type em:nth-last-of-type(2n-1)"), "2", "4", "6", "8", "10");
		check(html.select("#type p:nth-last-of-type(2n+1)"), "2", "4", "6", "8", "10");
		check(html.select("#type span:nth-last-of-type(2n+3)"), "2", "4", "6", "8");
		check(html.select("#type p:nth-last-of-type(even)"), "1", "3", "5", "7", "9");
		check(html.select("#type p:nth-last-of-type(2n)"), "1", "3", "5", "7", "9");
		check(html.select("#type p:nth-last-of-type(3n-1)"), "3", "6", "9");

		check(html.select("#type span:nth-last-of-type(-2n+5)"), "6", "8", "10");
		check(html.select("#type :nth-last-of-type(+5)"), "6", "6", "6", "6");
	}

// org.jsoup.select.CssTest::firstOfType
	public void firstOfType() {
		check(html.select("div:not(#only) :first-of-type"), "1", "1", "1", "1", "1");
	}

// org.jsoup.select.CssTest::lastOfType
	public void lastOfType() {
		check(html.select("div:not(#only) :last-of-type"), "10", "10", "10", "10", "10");
	}

// org.jsoup.select.CssTest::empty
	public void empty() {
		final Elements sel = html.select(":empty");
		assertEquals(3, sel.size());
		assertEquals("head", sel.get(0).tagName());
		assertEquals("br", sel.get(1).tagName());
		assertEquals("p", sel.get(2).tagName());
	}

// org.jsoup.select.CssTest::onlyChild
	public void onlyChild() {
		final Elements sel = html.select("span :only-child");
		assertEquals(1, sel.size());
		assertEquals("br", sel.get(0).tagName());
		
		check(html.select("#only :only-child"), "only");
	}

// org.jsoup.select.CssTest::onlyOfType
	public void onlyOfType() {
		final Elements sel = html.select(":only-of-type");
		assertEquals(6, sel.size());
		assertEquals("head", sel.get(0).tagName());
		assertEquals("body", sel.get(1).tagName());
		assertEquals("span", sel.get(2).tagName());
		assertEquals("br", sel.get(3).tagName());
		assertEquals("p", sel.get(4).tagName());
		assertTrue(sel.get(4).hasClass("empty"));
		assertEquals("em", sel.get(5).tagName());
	}

// org.jsoup.select.CssTest::root
	public void root() {
		Elements sel = html.select(":root");
		assertEquals(1, sel.size());
		assertNotNull(sel.get(0));
		assertEquals(Tag.valueOf("html"), sel.get(0).tag());

		Elements sel2 = html.select("body").select(":root");
		assertEquals(1, sel2.size());
		assertNotNull(sel2.get(0));
		assertEquals(Tag.valueOf("body"), sel2.get(0).tag());
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
        Document doc = Jsoup.parse("<a id=1 href='/foo'>One</a> <a id=2 href='https://jsoup.org'>Two</a>");
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
        Document doc = Jsoup.parse("<a id=1 href='/foo'>One</a> <a id=2 href='https://jsoup.org'>Two</a>");
        Elements one = doc.select("#1");
        Elements two = doc.select("#2");
        Elements both = doc.select("a");

        assertEquals("", one.attr("abs:href"));
        assertEquals("https://jsoup.org", two.attr("abs:href"));
        assertEquals("https://jsoup.org", both.attr("abs:href"));
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

// org.jsoup.select.ElementsTest::hasClassCaseInsensitive
    @Test public void hasClassCaseInsensitive() {
        Elements els = Jsoup.parse("<p Class=One>One <p class=Two>Two <p CLASS=THREE>THREE").select("p");
        Element one = els.get(0);
        Element two = els.get(1);
        Element thr = els.get(2);

        assertTrue(one.hasClass("One"));
        assertTrue(one.hasClass("ONE"));

        assertTrue(two.hasClass("TWO"));
        assertTrue(two.hasClass("Two"));

        assertTrue(thr.hasClass("ThreE"));
        assertTrue(thr.hasClass("three"));
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

// org.jsoup.select.ElementsTest::wrapDiv
    @Test public void wrapDiv() {
        String h = "<p><b>This</b> is <b>jsoup</b>.</p> <p>How do you like it?</p>";
        Document doc = Jsoup.parse(h);
        doc.select("p").wrap("<div></div>");
        assertEquals("<div><p><b>This</b> is <b>jsoup</b>.</p></div> <div><p>How do you like it?</p></div>",
                TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.select.ElementsTest::unwrap
    @Test public void unwrap() {
        String h = "<div><font>One</font> <font><a href=\"/\">Two</a></font></div";
        Document doc = Jsoup.parse(h);
        doc.select("font").unwrap();
        assertEquals("<div>One <a href=\"/\">Two</a></div>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.select.ElementsTest::unwrapP
    @Test public void unwrapP() {
        String h = "<p><a>One</a> Two</p> Three <i>Four</i> <p>Fix <i>Six</i></p>";
        Document doc = Jsoup.parse(h);
        doc.select("p").unwrap();
        assertEquals("<a>One</a> Two Three <i>Four</i> Fix <i>Six</i>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.select.ElementsTest::unwrapKeepsSpace
    @Test public void unwrapKeepsSpace() {
        String h = "<p>One <span>two</span> <span>three</span> four</p>";
        Document doc = Jsoup.parse(h);
        doc.select("span").unwrap();
        assertEquals("<p>One two three four</p>", doc.body().html());
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

// org.jsoup.select.ElementsTest::traverse
    @Test public void traverse() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div>There</div>");
        final StringBuilder accum = new StringBuilder();
        doc.select("div").traverse(new NodeVisitor() {
            public void head(Node node, int depth) {
                accum.append("<" + node.nodeName() + ">");
            }

            public void tail(Node node, int depth) {
                accum.append("</" + node.nodeName() + ">");
            }
        });
        assertEquals("<div><p><#text></#text></p></div><div><#text></#text></div>", accum.toString());
    }

// org.jsoup.select.ElementsTest::forms
    @Test public void forms() {
        Document doc = Jsoup.parse("<form id=1><input name=q></form><div /><form id=2><input name=f></form>");
        Elements els = doc.select("*");
        assertEquals(9, els.size());

        List<FormElement> forms = els.forms();
        assertEquals(2, forms.size());
        assertTrue(forms.get(0) != null);
        assertTrue(forms.get(1) != null);
        assertEquals("1", forms.get(0).id());
        assertEquals("2", forms.get(1).id());
    }

// org.jsoup.select.ElementsTest::classWithHyphen
    @Test public void classWithHyphen() {
        Document doc = Jsoup.parse("<p class='tab-nav'>Check</p>");
        Elements els = doc.getElementsByClass("tab-nav");
        assertEquals(1, els.size());
        assertEquals("Check", els.text());
    }

// org.jsoup.select.ElementsTest::siblings
    @Test public void siblings() {
        Document doc = Jsoup.parse("<div><p>1<p>2<p>3<p>4<p>5<p>6</div><div><p>7<p>8<p>9<p>10<p>11<p>12</div>");

        Elements els = doc.select("p:eq(3)"); 
        assertEquals(2, els.size());

        Elements next = els.next();
        assertEquals(2, next.size());
        assertEquals("5", next.first().text());
        assertEquals("11", next.last().text());

        assertEquals(0, els.next("p:contains(6)").size());
        final Elements nextF = els.next("p:contains(5)");
        assertEquals(1, nextF.size());
        assertEquals("5", nextF.first().text());

        Elements nextA = els.nextAll();
        assertEquals(4, nextA.size());
        assertEquals("5", nextA.first().text());
        assertEquals("12", nextA.last().text());

        Elements nextAF = els.nextAll("p:contains(6)");
        assertEquals(1, nextAF.size());
        assertEquals("6", nextAF.first().text());

        Elements prev = els.prev();
        assertEquals(2, prev.size());
        assertEquals("3", prev.first().text());
        assertEquals("9", prev.last().text());

        assertEquals(0, els.prev("p:contains(1)").size());
        final Elements prevF = els.prev("p:contains(3)");
        assertEquals(1, prevF.size());
        assertEquals("3", prevF.first().text());

        Elements prevA = els.prevAll();
        assertEquals(6, prevA.size());
        assertEquals("3", prevA.first().text());
        assertEquals("7", prevA.last().text());

        Elements prevAF = els.prevAll("p:contains(1)");
        assertEquals(1, prevAF.size());
        assertEquals("1", prevAF.first().text());
    }

// org.jsoup.select.ElementsTest::eachText
    @Test public void eachText() {
        Document doc = Jsoup.parse("<div><p>1<p>2<p>3<p>4<p>5<p>6</div><div><p>7<p>8<p>9<p>10<p>11<p>12<p></p></div>");
        List<String> divText = doc.select("div").eachText();
        assertEquals(2, divText.size());
        assertEquals("1 2 3 4 5 6", divText.get(0));
        assertEquals("7 8 9 10 11 12", divText.get(1));

        List<String> pText = doc.select("p").eachText();
        Elements ps = doc.select("p");
        assertEquals(13, ps.size());
        assertEquals(12, pText.size()); 
        assertEquals("1", pText.get(0));
        assertEquals("2", pText.get(1));
        assertEquals("5", pText.get(4));
        assertEquals("7", pText.get(6));
        assertEquals("12", pText.get(11));
    }

// org.jsoup.select.ElementsTest::eachAttr
    @Test public void eachAttr() {
        Document doc = Jsoup.parse(
            "<div><a href='/foo'>1</a><a href='http://example.com/bar'>2</a><a href=''>3</a><a>4</a>",
            "http://example.com");

        List<String> hrefAttrs = doc.select("a").eachAttr("href");
        assertEquals(3, hrefAttrs.size());
        assertEquals("/foo", hrefAttrs.get(0));
        assertEquals("http://example.com/bar", hrefAttrs.get(1));
        assertEquals("", hrefAttrs.get(2));
        assertEquals(4, doc.select("a").size());

        List<String> absAttrs = doc.select("a").eachAttr("abs:href");
        assertEquals(3, absAttrs.size());
        assertEquals(3, absAttrs.size());
        assertEquals("http://example.com/foo", absAttrs.get(0));
        assertEquals("http://example.com/bar", absAttrs.get(1));
        assertEquals("http://example.com", absAttrs.get(2));
    }

// org.jsoup.select.SelectorTest::testByTag
    @Test public void testByTag() {
        
        Elements els = Jsoup.parse("<div id=1><div id=2><p>Hello</p></div></div><DIV id=3>").select("DIV");
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
        Elements els = Jsoup.parse("<p id=0 class='ONE two'><p id=1 class='one'><p id=2 class='two'>").select("P.One");
        assertEquals(2, els.size());
        assertEquals("0", els.get(0).id());
        assertEquals("1", els.get(1).id());

        Elements none = Jsoup.parse("<div class='one'></div>").select(".foo");
        assertEquals(0, none.size());

        Elements els2 = Jsoup.parse("<div class='One-Two'></div>").select(".one-two");
        assertEquals(1, els2.size());
    }

// org.jsoup.select.SelectorTest::testByClassCaseInsensitive
    @Test public void testByClassCaseInsensitive() {
        String html = "<p Class=foo>One <p Class=Foo>Two <p class=FOO>Three <p class=farp>Four";
        Elements elsFromClass = Jsoup.parse(html).select("P.Foo");
        Elements elsFromAttr = Jsoup.parse(html).select("p[class=foo]");

        assertEquals(elsFromAttr.size(), elsFromClass.size());
        assertEquals(3, elsFromClass.size());
        assertEquals("Two", elsFromClass.get(1).text());
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

// org.jsoup.select.SelectorTest::testWildcardNamespacedTag
    @Test public void testWildcardNamespacedTag() {
        Document doc = Jsoup.parse("<div><abc:def id=1>Hello</abc:def></div> <abc:def class=bold id=2>There</abc:def>");
        Elements byTag = doc.select("*|def");
        assertEquals(2, byTag.size());
        assertEquals("1", byTag.first().id());
        assertEquals("2", byTag.last().id());

        Elements byAttr = doc.select(".bold");
        assertEquals(1, byAttr.size());
        assertEquals("2", byAttr.last().id());

        Elements byTagAttr = doc.select("*|def.bold");
        assertEquals(1, byTagAttr.size());
        assertEquals("2", byTagAttr.last().id());

        Elements byContains = doc.select("*|def:contains(e)");
        assertEquals(2, byContains.size());
        assertEquals("1", byContains.first().id());
        assertEquals("2", byContains.last().id());
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
        Element root = doc.getElementsByClass("HEAD").first();
        
        Elements els = root.select(".head p");
        assertEquals(2, els.size());
        assertEquals("Hello", els.get(0).text());
        assertEquals("There", els.get(1).text());

        Elements p = root.select("p.first");
        assertEquals(1, p.size());
        assertEquals("Hello", p.get(0).text());

        Elements empty = root.select("p .first"); 
        assertEquals(0, empty.size());
        
        Elements aboveRoot = root.select("body div.head");
        assertEquals(0, aboveRoot.size());
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
        Document doc = Jsoup.parse(h);
        Element root = doc.getElementsByClass("head").first();

        Elements els = root.select("div p .first");
        assertEquals(1, els.size());
        assertEquals("Hello", els.first().text());
        assertEquals("span", els.first().tagName());

        Elements aboveRoot = root.select("body p .first");
        assertEquals(0, aboveRoot.size());
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

        assertEquals(2, doc.select("DiV").size());
        assertEquals(1, doc.select("DiV[TiTLE]").size());
        assertEquals(1, doc.select("DiV[TiTLE=BAR]").size());
        assertEquals(0, doc.select("DiV[TiTLE=BARBARELLA]").size());
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

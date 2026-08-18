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

        Elements divs2 = doc.select("div:has([class])");
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

// org.jsoup.select.SelectorTest::handlesCommasInSelector
    @Test public void handlesCommasInSelector() {
        Document doc = Jsoup.parse("<p name='1,2'>One</p><div>Two</div><ol><li>123</li><li>Text</li></ol>");

        Elements ps = doc.select("[name=1,2]");
        assertEquals(1, ps.size());

        Elements containers = doc.select("div, li:matches([0-9,]+)");
        assertEquals(2, containers.size());
        assertEquals("div", containers.get(0).tagName());
        assertEquals("li", containers.get(1).tagName());
        assertEquals("123", containers.get(1).text());
    }

// org.jsoup.select.SelectorTest::selectSupplementaryCharacter
    @Test public void selectSupplementaryCharacter() {
        String s = new String(Character.toChars(135361));
        Document doc = Jsoup.parse("<div k" + s + "='" + s + "'>^" + s +"$/div>");
        assertEquals("div", doc.select("div[k" + s + "]").first().tagName());
        assertEquals("div", doc.select("div:containsOwn(" + s + ")").first().tagName());
    }

// org.jsoup.select.SelectorTest::selectClassWithSpace
    public void selectClassWithSpace() {
        final String html = "<div class=\"value\">class without space</div>\n"
                          + "<div class=\"value \">class with space</div>";
        
        Document doc = Jsoup.parse(html);
        
        Elements found = doc.select("div[class=value ]");
        assertEquals(2, found.size());
        assertEquals("class without space", found.get(0).text());
        assertEquals("class with space", found.get(1).text());
        
        found = doc.select("div[class=\"value \"]");
        assertEquals(2, found.size());
        assertEquals("class without space", found.get(0).text());
        assertEquals("class with space", found.get(1).text());
        
        found = doc.select("div[class=\"value\\ \"]");
        assertEquals(0, found.size());
    }

// org.jsoup.select.SelectorTest::selectSameElements
    @Test public void selectSameElements() {
        final String html = "<div>one</div><div>one</div>";

        Document doc = Jsoup.parse(html);
        Elements els = doc.select("div");
        assertEquals(2, els.size());

        Elements subSelect = els.select(":contains(one)");
        assertEquals(2, subSelect.size());
    }

// org.jsoup.select.SelectorTest::attributeWithBrackets
    @Test public void attributeWithBrackets() {
        String html = "<div data='End]'>One</div> <div data='[Another)]]'>Two</div>";
        Document doc = Jsoup.parse(html);
        assertEquals("One", doc.select("div[data='End]']").first().text());
        assertEquals("Two", doc.select("div[data='[Another)]]']").first().text());
        assertEquals("One", doc.select("div[data=\"End]\"]").first().text());
        assertEquals("Two", doc.select("div[data=\"[Another)]]\"]").first().text());
    }

// org.jsoup.select.SelectorTest::containsWithQuote
    @Test public void containsWithQuote() {
        String html = "<p>One'One</p><p>One'Two</p>";
        Document doc = Jsoup.parse(html);
        Elements els = doc.select("p:contains(One\\'One)");
        assertEquals(1, els.size());
        assertEquals("One'One", els.text());
    }

// org.jsoup.select.SelectorTest::selectFirst
    @Test public void selectFirst() {
        String html = "<p>One<p>Two<p>Three";
        Document doc = Jsoup.parse(html);
        assertEquals("One", doc.selectFirst("p").text());
    }

// org.jsoup.select.SelectorTest::selectFirstWithAnd
    @Test public void selectFirstWithAnd() {
        String html = "<p>One<p class=foo>Two<p>Three";
        Document doc = Jsoup.parse(html);
        assertEquals("Two", doc.selectFirst("p.foo").text());
    }

// org.jsoup.select.SelectorTest::selectFirstWithOr
    @Test public void selectFirstWithOr() {
        String html = "<p>One<p>Two<p>Three<div>Four";
        Document doc = Jsoup.parse(html);
        assertEquals("One", doc.selectFirst("p, div").text());
    }

// org.jsoup.select.SelectorTest::matchText
    @Test public void matchText() {
        String html = "<p>One<br>Two</p>";
        Document doc = Jsoup.parse(html);
        String origHtml = doc.html();

        Elements one = doc.select("p:matchText:first-child");
        assertEquals("One", one.first().text());

        Elements two = doc.select("p:matchText:last-child");
        assertEquals("Two", two.first().text());

        assertEquals(origHtml, doc.html());

        assertEquals("Two", doc.select("p:matchText + br + *").text());
    }

// org.jsoup.select.SelectorTest::splitOnBr
    @Test public void splitOnBr() {
        String html = "<div><p>One<br>Two<br>Three</p></div>";
        Document doc = Jsoup.parse(html);

        Elements els = doc.select("p:matchText");
        assertEquals(3, els.size());
        assertEquals("One", els.get(0).text());
        assertEquals("Two", els.get(1).text());
        assertEquals("Three", els.get(2).toString());
    }

// org.jsoup.select.SelectorTest::matchTextAttributes
    @Test public void matchTextAttributes() {
        Document doc = Jsoup.parse("<div><p class=one>One<br>Two<p class=two>Three<br>Four");
        Elements els = doc.select("p.two:matchText:last-child");

        assertEquals(1, els.size());
        assertEquals("Four", els.text());
    }

// org.jsoup.select.SelectorTest::findBetweenSpan
    @Test public void findBetweenSpan() {
        Document doc = Jsoup.parse("<p><span>One</span> Two <span>Three</span>");
        Elements els = doc.select("span ~ p:matchText"); 

        assertEquals(1, els.size());
        assertEquals("Two", els.text());
    }

// org.jsoup.select.TraversorTest::filterVisit
    public void filterVisit() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div>There</div>");
        final StringBuilder accum = new StringBuilder();
        NodeTraversor.filter(new NodeFilter() {
            public FilterResult head(Node node, int depth) {
                accum.append("<" + node.nodeName() + ">");
                return FilterResult.CONTINUE;
            }

            public FilterResult tail(Node node, int depth) {
                accum.append("</" + node.nodeName() + ">");
                return FilterResult.CONTINUE;
            }
        }, doc.select("div"));
        assertEquals("<div><p><#text></#text></p></div><div><#text></#text></div>", accum.toString());
    }

// org.jsoup.select.TraversorTest::filterSkipChildren
    public void filterSkipChildren() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div>There</div>");
        final StringBuilder accum = new StringBuilder();
        NodeTraversor.filter(new NodeFilter() {
            public FilterResult head(Node node, int depth) {
                accum.append("<" + node.nodeName() + ">");
                
                return ("p".equals(node.nodeName())) ? FilterResult.SKIP_CHILDREN : FilterResult.CONTINUE;
            }

            public FilterResult tail(Node node, int depth) {
                accum.append("</" + node.nodeName() + ">");
                return FilterResult.CONTINUE;
            }
        }, doc.select("div"));
        assertEquals("<div><p></p></div><div><#text></#text></div>", accum.toString());
    }

// org.jsoup.select.TraversorTest::filterSkipEntirely
    public void filterSkipEntirely() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div>There</div>");
        final StringBuilder accum = new StringBuilder();
        NodeTraversor.filter(new NodeFilter() {
            public FilterResult head(Node node, int depth) {
                
                if ("p".equals(node.nodeName()))
                    return FilterResult.SKIP_ENTIRELY;
                accum.append("<" + node.nodeName() + ">");
                return FilterResult.CONTINUE;
            }

            public FilterResult tail(Node node, int depth) {
                accum.append("</" + node.nodeName() + ">");
                return FilterResult.CONTINUE;
            }
        }, doc.select("div"));
        assertEquals("<div></div><div><#text></#text></div>", accum.toString());
    }

// org.jsoup.select.TraversorTest::filterRemove
    public void filterRemove() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div>There be <b>bold</b></div>");
        NodeTraversor.filter(new NodeFilter() {
            public FilterResult head(Node node, int depth) {
                
                return ("p".equals(node.nodeName())) ? FilterResult.REMOVE : FilterResult.CONTINUE;
            }

            public FilterResult tail(Node node, int depth) {
                
                return ("b".equals(node.nodeName())) ? FilterResult.REMOVE : FilterResult.CONTINUE;
            }
        }, doc.select("div"));
        assertEquals("<div></div>\n<div>\n There be \n</div>", doc.select("body").html());
    }

// org.jsoup.select.TraversorTest::filterStop
    public void filterStop() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div>There</div>");
        final StringBuilder accum = new StringBuilder();
        NodeTraversor.filter(new NodeFilter() {
            public FilterResult head(Node node, int depth) {
                accum.append("<" + node.nodeName() + ">");
                return FilterResult.CONTINUE;
            }

            public FilterResult tail(Node node, int depth) {
                accum.append("</" + node.nodeName() + ">");
                
                return ("p".equals(node.nodeName())) ? FilterResult.STOP : FilterResult.CONTINUE;
            }
        }, doc.select("div"));
        assertEquals("<div><p><#text></#text></p>", accum.toString());
    }

    public Element append(String html) {
        Validate.notNull(html);
        
        Element fragment = Parser.parseBodyFragment(html, baseUri).body();
        for (Node node : fragment.childNodes()) {
            node.parentNode = null;
            appendChild(node);
        }
        return this;
    }

    public Element prepend(String html) {
        Validate.notNull(html);
        
        Element fragment = Parser.parseBodyFragment(html, baseUri).body();
        List<Node> nodes = fragment.childNodes();
        for (int i = nodes.size() - 1; i >= 0; i--) {
            Node node = nodes.get(i);
            node.parentNode = null;
            prependChild(node);
        }
        return this;
    }

    public Element wrap(String html) {
        Validate.notEmpty(html);

        Element wrapBody = Parser.parseBodyFragment(html, baseUri).body();
        Elements wrapChildren = wrapBody.children();
        Element wrap = wrapChildren.first();
        if (wrap == null) // nothing to wrap with; noop
            return null;

        Element deepest = getDeepChild(wrap);
        parentNode.replaceChild(this, wrap);
        deepest.addChild(this);

        // remainder (unbalananced wrap, like <div></div><p></p> -- The <p> is remainder
        if (wrapChildren.size() > 1) {
            for (int i = 1; i < wrapChildren.size(); i++) { // skip first
                Element remainder = wrapChildren.get(i);
                remainder.parentNode.removeChild(remainder);
                wrap.appendChild(remainder);
            }
        }
        return this;
    }

    public static Document parseBodyFragment(String bodyHtml, String baseUri) {
        Parser parser = new Parser(bodyHtml, baseUri, true);
        return parser.parse();
    }

    private Element addChildToParent(Element child, boolean isEmptyElement) {
        Element parent = popStackToSuitableContainer(child.tag());
        Tag childTag = child.tag();
        boolean validAncestor = stackHasValidParent(childTag);

        if (!validAncestor) {
            // create implicit parent around this child
            Tag parentTag = childTag.getImplicitParent();
            Element implicit = new Element(parentTag, baseUri);
            // special case: make sure there's a head before putting in body
            if (child.tag().equals(bodyTag)) {
                Element head = new Element(headTag, baseUri);
                implicit.appendChild(head);
            }
            implicit.appendChild(child);

            // recurse to ensure somewhere to put parent
            Element root = addChildToParent(implicit, false);
            if (!isEmptyElement)
                stack.addLast(child);
            return root;
        }

        parent.appendChild(child);

        if (!isEmptyElement)
            stack.addLast(child);
        return parent;
    }

    private boolean stackHasValidParent(Tag childTag) {
        if (stack.size() == 1 && childTag.equals(htmlTag))
            return true; // root is valid for html node


        // otherwise, look up the stack for valid ancestors
        for (int i = stack.size() -1; i >= 0; i--) {
            Element el = stack.get(i);
            Tag parent2 = el.tag();
            if (parent2.isValidParent(childTag)) {
                return true;
            }
        }
        return false;
    }

    boolean canContain(Tag child) {
        Validate.notNull(child);

        if (child.isBlock && !this.canContainBlock)
            return false;

        if (!child.isBlock && !this.canContainInline) // not block == inline
            return false;

        if (this.optionalClosing && this.equals(child))
            return false;

        if (this.empty || this.isData())
            return false;

        // head can only contain a few. if more than head in here, modify to have a list of valids
        // TODO: (could solve this with walk for ancestor)
        if (this.tagName.equals("head")) {
            if (child.tagName.equals("base") || child.tagName.equals("script") || child.tagName.equals("noscript") || child.tagName.equals("link") ||
                    child.tagName.equals("meta") || child.tagName.equals("title") || child.tagName.equals("style") || child.tagName.equals("object")) {
                return true;
            }
            return false;
        }
        
        // dt and dd (in dl)
        if (this.tagName.equals("dt") && child.tagName.equals("dd"))
            return false;
        if (this.tagName.equals("dd") && child.tagName.equals("dt"))
            return false;

        // don't allow children to contain their parent (directly)
        
        return true;
    }

    Tag getImplicitParent() {
        return (!ancestors.isEmpty()) ? ancestors.get(0) : null;
    }

    boolean isValidParent(Tag child) {

        if (child.ancestors.isEmpty())
            return true; // HTML tag

        for (Tag tag : child.ancestors) {
            if (this.equals(tag))
                return true;
        }
        return false;
    }

    private Tag setAncestor(String... tagNames) {
        if (tagNames == null) {
            ancestors = Collections.emptyList();
        } else {
            ancestors = new ArrayList<Tag>(tagNames.length);
            for (String name : tagNames) {
                ancestors.add(Tag.valueOf(name));
            }
        }
        return this;
    }

// trigger testcase
@Test public void testAppendRowToTable() {
        Document doc = Jsoup.parse("<table><tr><td>1</td></tr></table>");
        Element table = doc.select("table").first();
        table.append("<tr><td>2</td></tr>");

        assertEquals("<table><tr><td>1</td></tr><tr><td>2</td></tr></table>", TextUtil.stripNewlines(doc.body().html()));
    }

@Test public void testPrependRowToTable() {
        Document doc = Jsoup.parse("<table><tr><td>1</td></tr></table>");
        Element table = doc.select("table").first();
        table.prepend("<tr><td>2</td></tr>");

        assertEquals("<table><tr><td>2</td></tr><tr><td>1</td></tr></table>", TextUtil.stripNewlines(doc.body().html()));
    }

@Test public void handlesNestedImplicitTable() {
        Document doc = Jsoup.parse("<table><td>1</td></tr> <td>2</td></tr> <td> <table><td>3</td> <td>4</td></table> <tr><td>5</table>");
        assertEquals("<table><tr><td>1</td></tr> <tr><td>2</td></tr> <tr><td> <table><tr><td>3</td> <td>4</td></tr></table> </td></tr><tr><td>5</td></tr></table>", TextUtil.stripNewlines(doc.body().html()));
    }

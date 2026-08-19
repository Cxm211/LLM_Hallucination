// ===== FIXED org.jsoup.nodes.Element :: append(String) [lines 267-276] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-3-fixed/src/main/java/org/jsoup/nodes/Element.java =====
    public Element append(String html) {
        Validate.notNull(html);
        
        Element fragment = Parser.parseBodyFragmentRelaxed(html, baseUri()).body();
        for (Node node : fragment.childNodes()) {
            node.parentNode = null;
            appendChild(node);
        }
        return this;
    }

// ===== FIXED org.jsoup.nodes.Element :: prepend(String) [lines 284-295] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-3-fixed/src/main/java/org/jsoup/nodes/Element.java =====
    public Element prepend(String html) {
        Validate.notNull(html);
        
        Element fragment = Parser.parseBodyFragmentRelaxed(html, baseUri()).body();
        List<Node> nodes = fragment.childNodes();
        for (int i = nodes.size() - 1; i >= 0; i--) {
            Node node = nodes.get(i);
            node.parentNode = null;
            prependChild(node);
        }
        return this;
    }

// ===== FIXED org.jsoup.nodes.Element :: wrap(String) [lines 311-333] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-3-fixed/src/main/java/org/jsoup/nodes/Element.java =====
    public Element wrap(String html) {
        Validate.notEmpty(html);

        Element wrapBody = Parser.parseBodyFragmentRelaxed(html, baseUri).body();
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

// ===== FIXED org.jsoup.parser.Parser :: addChildToParent(Element, boolean) [lines 225-253] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-3-fixed/src/main/java/org/jsoup/parser/Parser.java =====
    private Element addChildToParent(Element child, boolean isEmptyElement) {
        Element parent = popStackToSuitableContainer(child.tag());
        Tag childTag = child.tag();
        boolean validAncestor = stackHasValidParent(childTag);

        if (!validAncestor && !relaxed) {
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

// ===== FIXED org.jsoup.parser.Parser :: stackHasValidParent(Tag) [lines 255-271] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-3-fixed/src/main/java/org/jsoup/parser/Parser.java =====
    private boolean stackHasValidParent(Tag childTag) {
        if (stack.size() == 1 && childTag.equals(htmlTag))
            return true; // root is valid for html node

        if (childTag.requiresSpecificParent())
            return stack.getLast().tag().isValidParent(childTag);

        // otherwise, look up the stack for valid ancestors
        for (int i = stack.size() -1; i >= 0; i--) {
            Element el = stack.get(i);
            Tag parent2 = el.tag();
            if (parent2.isValidAncestor(childTag)) {
                return true;
            }
        }
        return false;
    }

// ===== FIXED org.jsoup.parser.Tag :: canContain(Tag) [lines 68-104] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-3-fixed/src/main/java/org/jsoup/parser/Tag.java =====
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
        if (this.requiresSpecificParent() && this.getImplicitParent().equals(child))
            return false;
        
        return true;
    }

// ===== FIXED org.jsoup.parser.Tag :: getImplicitParent() [lines 154-156] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-3-fixed/src/main/java/org/jsoup/parser/Tag.java =====
    Tag getImplicitParent() {
        return (!ancestors.isEmpty()) ? ancestors.get(0) : null;
    }

// ===== FIXED org.jsoup.parser.Tag :: isValidParent(Tag) [lines 162-164] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-3-fixed/src/main/java/org/jsoup/parser/Tag.java =====
    boolean isValidParent(Tag child) {
        return this.equals(child.parent);
    }

// ===== FIXED org.jsoup.parser.Tag :: setAncestor(String...) [lines 376-386] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-3-fixed/src/main/java/org/jsoup/parser/Tag.java =====
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

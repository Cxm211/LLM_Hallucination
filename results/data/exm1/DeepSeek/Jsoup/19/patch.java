package org.jsoup.safety;

import java.util.*;

public class Whitelist {
    private Set<TagName> tagNames;
    private Map<TagName, Set<AttributeKey>> attributes;
    private Map<TagName, Map<AttributeKey, AttributeValue>> enforcedAttributes;
    private Map<TagName, Map<AttributeKey, Set<Protocol>>> protocols;
    private boolean preserveRelativeLinks;

    public static Whitelist none() {
        return new Whitelist();
    }

    public static Whitelist simpleText() {
        return new Whitelist()
                .addTags("b", "em", "i", "strong", "u");
    }

    public static Whitelist basic() {
        return new Whitelist()
                .addTags(
                        "a", "b", "blockquote", "br", "cite", "code", "dd", "dl", "dt", "em",
                        "i", "li", "ol", "p", "pre", "q", "small", "strike", "strong", "sub",
                        "sup", "u", "ul")
                .addAttributes("a", "href")
                .addAttributes("blockquote", "cite")
                .addAttributes("q", "cite")
                .addProtocols("a", "href", "ftp", "http", "https", "mailto")
                .addProtocols("blockquote", "cite", "http", "https")
                .addProtocols("cite", "cite", "http", "https")
                .addEnforcedAttribute("a", "rel", "nofollow");
    }

    public static Whitelist basicWithImages() {
        return basic()
                .addTags("img")
                .addAttributes("img", "align", "alt", "height", "src", "title", "width")
                .addProtocols("img", "src", "http", "https");
    }

    public static Whitelist relaxed() {
        return new Whitelist()
                .addTags(
                        "a", "b", "blockquote", "br", "caption", "cite", "code", "col",
                        "colgroup", "dd", "div", "dl", "dt", "em", "h1", "h2", "h3", "h4", "h5", "h6",
                        "i", "img", "li", "ol", "p", "pre", "q", "small", "strike", "strong",
                        "sub", "sup", "table", "tbody", "td", "tfoot", "th", "thead", "tr", "u",
                        "ul")
                .addAttributes("a", "href", "title")
                .addAttributes("blockquote", "cite")
                .addAttributes("col", "span", "width")
                .addAttributes("colgroup", "span", "width")
                .addAttributes("img", "align", "alt", "height", "src", "title", "width")
                .addAttributes("ol", "start", "type")
                .addAttributes("q", "cite")
                .addAttributes("table", "summary", "width")
                .addAttributes("td", "abbr", "axis", "colspan", "rowspan", "width")
                .addAttributes(
                        "th", "abbr", "axis", "colspan", "rowspan", "scope",
                        "width")
                .addAttributes("ul", "type")
                .addProtocols("a", "href", "ftp", "http", "https", "mailto")
                .addProtocols("blockquote", "cite", "http", "https")
                .addProtocols("img", "src", "http", "https")
                .addProtocols("q", "cite", "http", "https");
    }

    public Whitelist() {
        tagNames = new HashSet<TagName>();
        attributes = new HashMap<TagName, Set<AttributeKey>>();
        enforcedAttributes = new HashMap<TagName, Map<AttributeKey, AttributeValue>>();
        protocols = new HashMap<TagName, Map<AttributeKey, Set<Protocol>>>();
        preserveRelativeLinks = false;
    }

    public Whitelist addTags(String... tags) {
        Validate.notNull(tags);
        for (String tagName : tags) {
            Validate.notEmpty(tagName);
            tagNames.add(TagName.valueOf(tagName));
        }
        return this;
    }

    public Whitelist addAttributes(String tag, String... keys) {
        Validate.notEmpty(tag);
        Validate.notNull(keys);
        TagName tagName = TagName.valueOf(tag);
        Set<AttributeKey> attributeSet = new HashSet<AttributeKey>();
        for (String key : keys) {
            Validate.notEmpty(key);
            attributeSet.add(AttributeKey.valueOf(key));
        }
        if (attributes.containsKey(tagName)) {
            Set<AttributeKey> currentSet = attributes.get(tagName);
            currentSet.addAll(attributeSet);
        } else {
            attributes.put(tagName, attributeSet);
        }
        return this;
    }

    public Whitelist addEnforcedAttribute(String tag, String key, String value) {
        Validate.notEmpty(tag);
        Validate.notEmpty(key);
        Validate.notEmpty(value);
        TagName tagName = TagName.valueOf(tag);
        AttributeKey attrKey = AttributeKey.valueOf(key);
        AttributeValue attrVal = AttributeValue.valueOf(value);
        if (enforcedAttributes.containsKey(tagName)) {
            enforcedAttributes.get(tagName).put(attrKey, attrVal);
        } else {
            Map<AttributeKey, AttributeValue> attrMap = new HashMap<AttributeKey, AttributeValue>();
            attrMap.put(attrKey, attrVal);
            enforcedAttributes.put(tagName, attrMap);
        }
        return this;
    }

    public Whitelist preserveRelativeLinks(boolean preserve) {
        preserveRelativeLinks = preserve;
        return this;
    }

    public Whitelist addProtocols(String tag, String key, String... protocols) {
        Validate.notEmpty(tag);
        Validate.notEmpty(key);
        Validate.notNull(protocols);
        TagName tagName = TagName.valueOf(tag);
        AttributeKey attrKey = AttributeKey.valueOf(key);
        Map<AttributeKey, Set<Protocol>> attrMap;
        Set<Protocol> protSet;
        if (this.protocols.containsKey(tagName)) {
            attrMap = this.protocols.get(tagName);
        } else {
            attrMap = new HashMap<AttributeKey, Set<Protocol>>();
            this.protocols.put(tagName, attrMap);
        }
        if (attrMap.containsKey(attrKey)) {
            protSet = attrMap.get(attrKey);
        } else {
            protSet = new HashSet<Protocol>();
            attrMap.put(attrKey, protSet);
        }
        for (String protocol : protocols) {
            Validate.notEmpty(protocol);
            Protocol prot = Protocol.valueOf(protocol);
            protSet.add(prot);
        }
        return this;
    }

    boolean isSafeTag(String tag) {
        return tagNames.contains(TagName.valueOf(tag));
    }

    boolean isSafeAttribute(String tagName, Element el, Attribute attr) {
        TagName tag = TagName.valueOf(tagName);
        AttributeKey key = AttributeKey.valueOf(attr.getKey());

        // Check if the tag has explicit attribute definitions
        if (attributes.containsKey(tag)) {
            Set<AttributeKey> allowed = attributes.get(tag);
            if (allowed.contains(key)) {
                // Attribute is explicitly allowed for this tag, check protocols if defined
                if (protocols.containsKey(tag)) {
                    Map<AttributeKey, Set<Protocol>> attrProts = protocols.get(tag);
                    // ok if not defined protocol; otherwise test
                    return !attrProts.containsKey(key) || testValidProtocol(el, attr, attrProts.get(key));
                } else {
                    return true;
                }
            } // else: attribute not in tag's set, fall through to check :all
        }

        // Check attribute against the :all tag
        if (!tagName.equals(":all")) {
            return isSafeAttribute(":all", el, attr);
        }

        // If we reach here, attribute is not allowed
        return false;
    }

    private boolean testValidProtocol(Element el, Attribute attr, Set<Protocol> protocols) {
        String value = el.absUrl(attr.getKey());
        if (!preserveRelativeLinks)
            attr.setValue(value);
        for (Protocol protocol : protocols) {
            String prot = protocol.toString() + ":";
            if (value.toLowerCase().startsWith(prot)) {
                return true;
            }
        }
        return false;
    }

    Attributes getEnforcedAttributes(String tagName) {
        Attributes attrs = new Attributes();
        TagName tag = TagName.valueOf(tagName);
        if (enforcedAttributes.containsKey(tag)) {
            Map<AttributeKey, AttributeValue> keyVals = enforcedAttributes.get(tag);
            for (Map.Entry<AttributeKey, AttributeValue> entry : keyVals.entrySet()) {
                attrs.put(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        return attrs;
    }

    static class TagName extends TypedValue {
        TagName(String value) { super(value); }
        static TagName valueOf(String value) { return new TagName(value); }
    }

    static class AttributeKey extends TypedValue {
        AttributeKey(String value) { super(value); }
        static AttributeKey valueOf(String value) { return new AttributeKey(value); }
    }

    static class AttributeValue extends TypedValue {
        AttributeValue(String value) { super(value); }
        static AttributeValue valueOf(String value) { return new AttributeValue(value); }
    }

    static class Protocol extends TypedValue {
        Protocol(String value) { super(value); }
        static Protocol valueOf(String value) { return new Protocol(value); }
    }

    abstract static class TypedValue {
        private String value;
        TypedValue(String value) { Validate.notNull(value); this.value = value; }
        @Override public int hashCode() { final int prime = 31; int result = 1; result = prime * result + ((value == null) ? 0 : value.hashCode()); return result; }
        @Override public boolean equals(Object obj) { if (this == obj) return true; if (obj == null) return false; if (getClass() != obj.getClass()) return false; TypedValue other = (TypedValue) obj; if (value == null) { if (other.value != null) return false; } else if (!value.equals(other.value)) return false; return true; }
        @Override public String toString() { return value; }
    }
}
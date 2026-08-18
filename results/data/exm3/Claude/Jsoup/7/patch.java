public Document normalise() {
    Element htmlEl = findFirstElementByTagName("html", this);
    if (htmlEl == null)
        htmlEl = appendElement("html");
    if (head() == null)
        htmlEl.prependElement("head");
    if (body() == null)
        htmlEl.appendElement("body");

    normaliseTextNodes(head());
    normaliseTextNodes(htmlEl);
    normaliseTextNodes(this);

    normaliseStructure("head", htmlEl);
    normaliseStructure("body", htmlEl);

    return this;
}
// org/jsoup/helper/W3CDomTest.java
@Test
public void handlesVariousInvalidAttributeNames() {
    org.jsoup.nodes.Document jsoupDoc = new org.jsoup.nodes.Document("");
    org.jsoup.nodes.Element body = jsoupDoc.appendChild(new org.jsoup.nodes.Element("body"));
    body.attr("\"", "val1");
    body.attr("-foo", "val2");
    body.attr(".foo", "val3");
    body.attr("1foo", "val4");
    body.attr("data-foo", "val5");
    body.attr("data!foo", "val6");
    body.attr(":foo", "val7");
    body.attr("_foo", "val8");
    body.attr("foo:bar", "val9");
    body.attr("foo.bar", "val10");
    Document w3Doc = new W3CDom().fromJsoup(jsoupDoc);
}

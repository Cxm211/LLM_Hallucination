// com/google/javascript/jscomp/TypeCheckTest.java
public void testIssue783Variation() throws Exception {
    testTypes(
        "/** @constructor */" +
        "var Type = function () {" +
        "  /** @type {Type} */" +
        "  this.me_ = this;" +
        "};" +
        "Type.prototype.doIt = function() {" +
        "  var me = this.me_;" +
        "  if (!me.knownProp) { var x = me.unknownProp; }" +
        "};",
        "Property unknownProp never defined on Type");
  }
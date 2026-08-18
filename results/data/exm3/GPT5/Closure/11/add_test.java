// com/google/javascript/jscomp/TypeCheckTest.java::testIssue810
public void testIssue810_RHSAssign() throws Exception {
    testTypes(
        "/** @constructor */ function Type() {}" +
        "Type.prototype.doIt = function(obj) {" +
        "  var x; x = obj.unknownProp;" +
        "};",
        "Property unknownProp never defined on obj");
  }
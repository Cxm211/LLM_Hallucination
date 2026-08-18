// com/google/javascript/jscomp/TypeCheckTest.java
public void testThisTypeOfFunction7() throws Exception {
  testTypes(
      "/** @constructor */ function F() {}" +
      "F.prototype.data = 10;" +
      "F.prototype.get = function() { return this.data; };" +
      "var arr = [new F()];" +
      "var getter = arr[0].get;" +
      "getter();",
      "\"function (this:F): ?\" must be called with a \"this\" type");
}
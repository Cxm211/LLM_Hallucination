// com/google/javascript/jscomp/DevirtualizePrototypeMethodsTest.java
public void testReplaceMultipleThis() throws Exception {
    enableTypeCheck(CheckLevel.ERROR);
    checkTypes(
        "/** @constructor */ function Foo() {}" +
        "Foo.prototype.bar = function() { return this; this; };",
        "/** @constructor */ function Foo() {}" +
        "Foo.prototype.bar = function($this) { return $this; $this; };",
        null);
  }

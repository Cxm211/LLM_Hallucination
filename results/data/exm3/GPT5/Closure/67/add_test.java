// com/google/javascript/jscomp/RemoveUnusedPrototypePropertiesTest.java::testAliasing7
public void testBracketOnPrototype() {
    testSame("function e(){}" +
             "e['prototype']['alias'] = function(){this.method2()};" +
             "e.prototype.method2 = function(){};");
  }
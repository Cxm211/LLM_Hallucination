// com/google/javascript/jscomp/PeepholeFoldConstantsTest.java
public void testArrayAccessIncrementDecrement() {
  testSame("[][0]++;");
  testSame("++[][0];");
  testSame("[][0]--;");
  testSame("--[][0];");
}
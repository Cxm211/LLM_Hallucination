// com/google/javascript/jscomp/PeepholeFoldConstantsTest.java
public void testArrayAccessIncDec() {
  testSame("[][0]++;");
  testSame("++[][0];");
  testSame("[][0]--;");
  testSame("--[][0];");
}

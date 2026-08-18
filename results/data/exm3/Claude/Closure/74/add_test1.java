// com/google/javascript/jscomp/PeepholeFoldConstantsTest.java
public void testFoldComparisonStringWithUndefined() {
  fold("x = 'hello' == undefined", "x = false");
  fold("x = 'hello' != undefined", "x = true");
  fold("x = 'hello' === undefined", "x = false");
  fold("x = 'hello' !== undefined", "x = true");
  fold("x = '' == undefined", "x = false");
  fold("x = '' != undefined", "x = true");
}
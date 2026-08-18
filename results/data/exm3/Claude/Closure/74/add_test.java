// com/google/javascript/jscomp/PeepholeFoldConstantsTest.java
public void testFoldComparisonNumberWithUndefined() {
  fold("x = 5 == undefined", "x = false");
  fold("x = 5 != undefined", "x = true");
  fold("x = 5 === undefined", "x = false");
  fold("x = 5 !== undefined", "x = true");
  fold("x = 0 == undefined", "x = false");
  fold("x = 0 != undefined", "x = true");
}
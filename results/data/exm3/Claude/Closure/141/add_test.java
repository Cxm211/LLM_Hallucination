// com/google/javascript/jscomp/ExpresssionDecomposerTest.java
public void testCanExposeExpression_AdditionalAnonymousFunction() {
  // Test nested anonymous function (function literal)
  helperCanExposeExpression(
      DecompositionType.MOVABLE, "var x = (function(){return 1})() + 2", "(function(){return 1})");
  // Test anonymous function in array literal
  helperCanExposeExpression(
      DecompositionType.MOVABLE, "var arr = [function(){return 1}, 2]", "function(){return 1}");
  // Test anonymous function in object literal
  helperCanExposeExpression(
      DecompositionType.MOVABLE, "var obj = {fn: function(){return 1}}", "function(){return 1}");
}
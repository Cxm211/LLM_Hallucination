// com/google/javascript/jscomp/NodeUtilTest.java
public void testLocalValueAssignmentWithLocalVar() throws Exception {
    // Predicate that considers the name "local" as local.
    Predicate<Node> locals = new Predicate<Node>() {
        @Override
        public boolean apply(Node n) {
            return n.isName() && "local".equals(n.getString());
        }
    };
    // Parse "local = local"
    Compiler compiler = new Compiler();
    Node script = compiler.parseTestCode("local = local");
    Node assign1 = script.getFirstChild().getFirstChild();
    boolean result1 = NodeUtil.evaluatesToLocalValue(assign1, locals);
    assertTrue(result1);

    // Parse "local = nonlocal"
    Node script2 = compiler.parseTestCode("local = nonlocal");
    Node assign2 = script2.getFirstChild().getFirstChild();
    boolean result2 = NodeUtil.evaluatesToLocalValue(assign2, locals);
    assertFalse(result2);
}

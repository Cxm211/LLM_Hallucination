// com/google/javascript/jscomp/CheckSideEffectsTest.java
public void testUselessCodeAssignmentWithMultipleConstants() {
    test("var x; x = (1,2,3);", ok);
  }

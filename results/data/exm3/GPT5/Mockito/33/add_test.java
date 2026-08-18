// org/mockitousage/bugs/InheritedGenericsPolimorphicCallTest.java::shouldStubbingWork
java.util.List list = Mockito.mock(java.util.List.class);
Mockito.when(list.add("x")).thenReturn(true);
Assert.assertTrue(((java.util.Collection) list).add("x"));
Assert.assertTrue(list.add("x"));
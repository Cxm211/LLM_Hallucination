// com/google/javascript/jscomp/FoldConstantsTest.java
fold("x = ['', ''].join(',')", "x = \",\"");

// com/google/javascript/jscomp/FoldConstantsTest.java::testStringJoinAdd
fold("x = ['', ''].join(',')", "x = ','");
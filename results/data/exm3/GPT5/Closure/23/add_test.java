// com/google/javascript/jscomp/PeepholeFoldConstantsTest.java::testFoldGetElem
fold("x = [,10][1]", "x = 10");
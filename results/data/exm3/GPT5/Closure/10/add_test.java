// com/google/javascript/jscomp/PeepholeFoldConstantsTest.java::testIssue821
foldSame("var a = (Math.random() && '1') + 3 + 4;");
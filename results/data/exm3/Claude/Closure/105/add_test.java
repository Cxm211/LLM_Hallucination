// com/google/javascript/jscomp/FoldConstantsTest.java
public void testStringJoinAddEdgeCases() {
  fold("x = ['', ''].join(',')", "x = ','");
  fold("x = ['', '', ''].join('|')", "x = '||'");
  fold("x = [foo, '', ''].join('-')", "x = [foo, '-'].join('-')");
  fold("x = ['', '', foo].join('+')", "x = ['+', foo].join('+')");
  fold("x = [0, 0].join(',')", "x = '0,0'");
  fold("x = [false, false].join(' ')", "x = 'false false'");
}
// ===== FIXED org.apache.commons.jxpath.ri.axes.AttributeContext :: nextNode() [lines 74-101] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JxPath/JxPath-18-fixed/src/java/org/apache/commons/jxpath/ri/axes/AttributeContext.java =====
    public boolean nextNode() {
        super.setPosition(getCurrentPosition() + 1);
        if (!setStarted) {
            setStarted = true;
            NodeNameTest nodeNameTest = null;
            if (nodeTest instanceof NodeTypeTest) {
                if (((NodeTypeTest) nodeTest).getNodeType() == Compiler.NODE_TYPE_NODE) {
                    nodeNameTest = WILDCARD_TEST;
                }
            }
            else if (nodeTest instanceof NodeNameTest) {
                nodeNameTest = (NodeNameTest) nodeTest;
            }
            if (nodeNameTest == null) {
                return false;
            }
            iterator = parentContext.getCurrentNodePointer().attributeIterator(
                    nodeNameTest.getNodeName());
        }
        if (iterator == null) {
            return false;
        }
        if (!iterator.setPosition(iterator.getPosition() + 1)) {
            return false;
        }
        currentNodePointer = iterator.getNodePointer();
        return true;
    }

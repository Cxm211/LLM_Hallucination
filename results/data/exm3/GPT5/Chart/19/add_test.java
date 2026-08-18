// org/jfree/chart/plot/junit/CategoryPlotTests.java::testGetDomainAxisIndex
public void testGetDomainAxisIndex_Parent() {
        CategoryAxis parentDomainAxis = new CategoryAxis("PX");
        NumberAxis parentRangeAxis = new NumberAxis("PY");
        CategoryPlot parent = new CategoryPlot(null, parentDomainAxis, parentRangeAxis, null);

        CategoryPlot child = new CategoryPlot(null, null, new NumberAxis("CY"), null);
        child.setParent(parent);

        assertEquals(0, child.getDomainAxisIndex(parentDomainAxis));
    }
    public int getDomainAxisIndex(CategoryAxis axis) {
        int result = this.domainAxes.indexOf(axis);
        if (result < 0) {
            Plot parent = getParent();
            if (parent instanceof CategoryPlot) {
                CategoryPlot p = (CategoryPlot) parent;
                result = p.getDomainAxisIndex(axis);
            }
        }
        return result;
    }
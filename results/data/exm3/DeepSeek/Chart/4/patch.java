    public Range getDataRange(ValueAxis axis) {

        Range result = null;
        List domainAnnotations = new ArrayList();
        List rangeAnnotations = new ArrayList();

        int domainIndex = getDomainAxisIndex(axis);
        int rangeIndex = getRangeAxisIndex(axis);

        // Process domain datasets
        if (domainIndex >= 0) {
            List domainDatasets = getDatasetsMappedToDomainAxis(new Integer(domainIndex));
            Iterator domIter = domainDatasets.iterator();
            while (domIter.hasNext()) {
                XYDataset d = (XYDataset) domIter.next();
                if (d != null) {
                    XYItemRenderer r = getRendererForDataset(d);
                    if (r != null) {
                        result = Range.combine(result, r.findDomainBounds(d));
                        // collect annotations from renderer
                        Collection c = r.getAnnotations();
                        if (c != null) {
                            Iterator i = c.iterator();
                            while (i.hasNext()) {
                                XYAnnotation a = (XYAnnotation) i.next();
                                if (a instanceof XYAnnotationBoundsInfo) {
                                    domainAnnotations.add(a);
                                }
                            }
                        }
                    } else {
                        result = Range.combine(result, DatasetUtilities.findDomainBounds(d));
                    }
                }
            }
            if (domainIndex == 0) {
                Iterator plotIter = this.annotations.iterator();
                while (plotIter.hasNext()) {
                    XYAnnotation annotation = (XYAnnotation) plotIter.next();
                    if (annotation instanceof XYAnnotationBoundsInfo) {
                        domainAnnotations.add(annotation);
                    }
                }
            }
        }

        // Process range datasets
        if (rangeIndex >= 0) {
            List rangeDatasets = getDatasetsMappedToRangeAxis(new Integer(rangeIndex));
            Iterator rangeIter = rangeDatasets.iterator();
            while (rangeIter.hasNext()) {
                XYDataset d = (XYDataset) rangeIter.next();
                if (d != null) {
                    XYItemRenderer r = getRendererForDataset(d);
                    if (r != null) {
                        result = Range.combine(result, r.findRangeBounds(d));
                        // collect annotations from renderer
                        Collection c = r.getAnnotations();
                        if (c != null) {
                            Iterator i = c.iterator();
                            while (i.hasNext()) {
                                XYAnnotation a = (XYAnnotation) i.next();
                                if (a instanceof XYAnnotationBoundsInfo) {
                                    rangeAnnotations.add(a);
                                }
                            }
                        }
                    } else {
                        result = Range.combine(result, DatasetUtilities.findRangeBounds(d));
                    }
                }
            }
            if (rangeIndex == 0) {
                Iterator plotIter = this.annotations.iterator();
                while (plotIter.hasNext()) {
                    XYAnnotation annotation = (XYAnnotation) plotIter.next();
                    if (annotation instanceof XYAnnotationBoundsInfo) {
                        rangeAnnotations.add(annotation);
                    }
                }
            }
        }

        // Process domain annotations
        Iterator it = domainAnnotations.iterator();
        while (it.hasNext()) {
            XYAnnotationBoundsInfo xyabi = (XYAnnotationBoundsInfo) it.next();
            if (xyabi.getIncludeInDataBounds()) {
                result = Range.combine(result, xyabi.getXRange());
            }
        }

        // Process range annotations
        it = rangeAnnotations.iterator();
        while (it.hasNext()) {
            XYAnnotationBoundsInfo xyabi = (XYAnnotationBoundsInfo) it.next();
            if (xyabi.getIncludeInDataBounds()) {
                result = Range.combine(result, xyabi.getYRange());
            }
        }

        return result;

    }
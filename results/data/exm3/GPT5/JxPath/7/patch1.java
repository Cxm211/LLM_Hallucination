public Object computeValue(org.apache.commons.jxpath.ri.EvalContext context) {
        Object lObj = args[0].computeValue(context);
        Object rObj = args[1].computeValue(context);

        java.util.List rList = null;
        boolean rIsMulti = false;
        if (rObj instanceof java.util.Collection) {
            rList = new java.util.ArrayList((java.util.Collection) rObj);
            rIsMulti = true;
        } else if (rObj instanceof Object[]) {
            rList = java.util.Arrays.asList((Object[]) rObj);
            rIsMulti = true;
        } else if (rObj instanceof java.util.Iterator) {
            rList = new java.util.ArrayList();
            java.util.Iterator it = (java.util.Iterator) rObj;
            while (it.hasNext()) {
                rList.add(it.next());
            }
            rIsMulti = true;
        }

        if (lObj instanceof java.util.Collection || lObj instanceof Object[] || lObj instanceof java.util.Iterator) {
            java.util.Iterator lIt;
            if (lObj instanceof java.util.Collection) {
                lIt = ((java.util.Collection) lObj).iterator();
            } else if (lObj instanceof Object[]) {
                lIt = java.util.Arrays.asList((Object[]) lObj).iterator();
            } else {
                lIt = (java.util.Iterator) lObj;
            }
            while (lIt.hasNext()) {
                Object lv = lIt.next();
                if (rIsMulti) {
                    for (int i = 0; i < rList.size(); i++) {
                        Object rv = rList.get(i);
                        double l = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(lv);
                        double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(rv);
                        if (l >= r) {
                            return Boolean.TRUE;
                        }
                    }
                } else {
                    double l = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(lv);
                    double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(rObj);
                    if (l >= r) {
                        return Boolean.TRUE;
                    }
                }
            }
            return Boolean.FALSE;
        }

        if (rIsMulti) {
            double l = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(lObj);
            for (int i = 0; i < rList.size(); i++) {
                Object rv = rList.get(i);
                double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(rv);
                if (l >= r) {
                    return Boolean.TRUE;
                }
            }
            return Boolean.FALSE;
        }

        double l = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(lObj);
        double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(rObj);
        return l >= r ? Boolean.TRUE : Boolean.FALSE;
    }
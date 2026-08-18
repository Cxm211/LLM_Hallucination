    protected Size2D arrangeFF(BlockContainer container, Graphics2D g2,
                               RectangleConstraint constraint) {
        double[] w = new double[5];
        double[] h = new double[5];
        LengthConstraintType heightConstraint = constraint.getHeightConstraintType();
        double width = constraint.getWidth();
        w[0] = width;
        
        if (heightConstraint == LengthConstraintType.FIXED) {
            // Original logic for fixed height
            if (this.topBlock != null) {
                RectangleConstraint c1 = new RectangleConstraint(w[0], null,
                        LengthConstraintType.FIXED, 0.0,
                        new Range(0.0, constraint.getHeight()),
                        LengthConstraintType.RANGE);
                Size2D size = this.topBlock.arrange(g2, c1);
                h[0] = size.height;
            }
            w[1] = w[0];
            if (this.bottomBlock != null) {
                RectangleConstraint c2 = new RectangleConstraint(w[0], null,
                        LengthConstraintType.FIXED, 0.0, new Range(0.0,
                        constraint.getHeight() - h[0]), LengthConstraintType.RANGE);
                Size2D size = this.bottomBlock.arrange(g2, c2);
                h[1] = size.height;
            }
            h[2] = constraint.getHeight() - h[1] - h[0];
            if (this.leftBlock != null) {
                RectangleConstraint c3 = new RectangleConstraint(0.0,
                        new Range(0.0, constraint.getWidth()),
                        LengthConstraintType.RANGE, h[2], null,
                        LengthConstraintType.FIXED);
                Size2D size = this.leftBlock.arrange(g2, c3);
                w[2] = size.width;
            }
            h[3] = h[2];
            if (this.rightBlock != null) {
                RectangleConstraint c4 = new RectangleConstraint(0.0,
                        new Range(0.0, constraint.getWidth() - w[2]),
                        LengthConstraintType.RANGE, h[2], null,
                        LengthConstraintType.FIXED);
                Size2D size = this.rightBlock.arrange(g2, c4);
                w[3] = size.width;
            }
            h[4] = h[2];
            w[4] = constraint.getWidth() - w[3] - w[2];
            RectangleConstraint c5 = new RectangleConstraint(w[4], h[4]);
            if (this.centerBlock != null) {
                this.centerBlock.arrange(g2, c5);
            }
            // total height equals constraint.getHeight()
            // set bounds and return
            if (this.topBlock != null) {
                this.topBlock.setBounds(new Rectangle2D.Double(0.0, 0.0, w[0],
                        h[0]));
            }
            if (this.bottomBlock != null) {
                this.bottomBlock.setBounds(new Rectangle2D.Double(0.0, h[0] + h[2],
                        w[1], h[1]));
            }
            if (this.leftBlock != null) {
                this.leftBlock.setBounds(new Rectangle2D.Double(0.0, h[0], w[2],
                        h[2]));
            }
            if (this.rightBlock != null) {
                this.rightBlock.setBounds(new Rectangle2D.Double(w[2] + w[4], h[0],
                        w[3], h[3]));
            }
            if (this.centerBlock != null) {
                this.centerBlock.setBounds(new Rectangle2D.Double(w[2], h[0], w[4],
                        h[4]));
            }
            return new Size2D(constraint.getWidth(), constraint.getHeight());
        } else {
            // height is not fixed (RANGE or NONE)
            // arrange top block
            if (this.topBlock != null) {
                RectangleConstraint c1 = new RectangleConstraint(w[0], null,
                        LengthConstraintType.FIXED, 0.0, null,
                        LengthConstraintType.NONE);
                Size2D size = this.topBlock.arrange(g2, c1);
                h[0] = size.height;
            }
            // arrange bottom block
            w[1] = w[0];
            if (this.bottomBlock != null) {
                RectangleConstraint c2 = new RectangleConstraint(w[0], null,
                        LengthConstraintType.FIXED, 0.0, null,
                        LengthConstraintType.NONE);
                Size2D size = this.bottomBlock.arrange(g2, c2);
                h[1] = size.height;
            }
            // arrange left block
            if (this.leftBlock != null) {
                RectangleConstraint c3 = new RectangleConstraint(0.0,
                        new Range(0.0, width),
                        LengthConstraintType.RANGE, 0.0, null,
                        LengthConstraintType.NONE);
                Size2D size = this.leftBlock.arrange(g2, c3);
                w[2] = size.width;
                h[2] = size.height;
            }
            // arrange right block
            if (this.rightBlock != null) {
                RectangleConstraint c4 = new RectangleConstraint(0.0,
                        new Range(0.0, width - w[2]),
                        LengthConstraintType.RANGE, 0.0, null,
                        LengthConstraintType.NONE);
                Size2D size = this.rightBlock.arrange(g2, c4);
                w[3] = size.width;
                h[3] = size.height;
            }
            // arrange center block
            w[4] = width - w[3] - w[2];
            if (this.centerBlock != null) {
                RectangleConstraint c5 = new RectangleConstraint(w[4], null,
                        LengthConstraintType.FIXED, 0.0, null,
                        LengthConstraintType.NONE);
                Size2D size = this.centerBlock.arrange(g2, c5);
                h[4] = size.height;
            }
            // compute middle row height as max of left, right, center heights
            double hMid = 0.0;
            if (this.leftBlock != null) {
                hMid = Math.max(hMid, h[2]);
            }
            if (this.rightBlock != null) {
                hMid = Math.max(hMid, h[3]);
            }
            if (this.centerBlock != null) {
                hMid = Math.max(hMid, h[4]);
            }
            // total height
            double totalHeight = h[0] + h[1] + hMid;
            // if height constraint is RANGE, clip to range and adjust hMid
            if (heightConstraint == LengthConstraintType.RANGE) {
                Range heightRange = constraint.getHeightRange();
                if (heightRange != null) {
                    totalHeight = heightRange.constrain(totalHeight);
                }
                hMid = totalHeight - h[0] - h[1];
                if (hMid < 0.0) hMid = 0.0;
            }
            h[2] = hMid;
            h[3] = hMid;
            h[4] = hMid;
            // set bounds
            if (this.topBlock != null) {
                this.topBlock.setBounds(new Rectangle2D.Double(0.0, 0.0, w[0], h[0]));
            }
            if (this.bottomBlock != null) {
                this.bottomBlock.setBounds(new Rectangle2D.Double(0.0, h[0] + hMid, w[1], h[1]));
            }
            if (this.leftBlock != null) {
                this.leftBlock.setBounds(new Rectangle2D.Double(0.0, h[0], w[2], hMid));
            }
            if (this.rightBlock != null) {
                this.rightBlock.setBounds(new Rectangle2D.Double(w[2] + w[4], h[0], w[3], hMid));
            }
            if (this.centerBlock != null) {
                this.centerBlock.setBounds(new Rectangle2D.Double(w[2], h[0], w[4], hMid));
            }
            return new Size2D(width, totalHeight);
        }
    }
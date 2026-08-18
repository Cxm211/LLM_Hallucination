public static double cosh(double x) {
      if (x != x) {
          return x;
      }
      if (x > 20) {
              return 0.5 * exp(x);
          }
      if (x < -20) {
              return 0.5 * exp(-x);
      }
      final double hiPrec[] = new double[2];
      if (x < 0.0) {
          x = -x;
      }
      exp(x, 0.0, hiPrec);
      double ya = hiPrec[0] + hiPrec[1];
      double yb = -(ya - hiPrec[0] - hiPrec[1]);
      double temp = ya * HEX_40000000;
      double yaa = ya + temp - temp;
      double yab = ya - yaa;
      double recip = 1.0/ya;
      temp = recip * HEX_40000000;
      double recipa = recip + temp - temp;
      double recipb = recip - recipa;
      recipb += (1.0 - yaa*recipa - yaa*recipb - yab*recipa - yab*recipb) * recip;
      recipb += -yb * recip * recip;
      temp = ya + recipa;
      yb += -(temp - ya - recipa);
      ya = temp;
      temp = ya + recipb;
      yb += -(temp - ya - recipb);
      ya = temp;
      double result = ya + yb;
      result *= 0.5;
      return result;
    }
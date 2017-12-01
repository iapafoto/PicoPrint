/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package picoprint;

import java.io.IOException;
import static java.lang.StrictMath.asin;
import static java.lang.StrictMath.atan;
import static java.lang.StrictMath.sin;
import static java.lang.StrictMath.cos;
import static java.lang.StrictMath.sqrt;
import static java.lang.StrictMath.tan;
import static java.lang.StrictMath.toRadians;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author durands
 */
public class TestArea {
    final static double π = StrictMath.PI;
    public static final double EARTH_RADIUS_METERS = 6371.0088;
      
    static double sin2(double k) {
        final double sk = sin(k);
        return sk*sk;
    }
    
    static double sphericalDistance(final double φ0, final double λ0, final double φ1, final double λ1) {
        return 2.*asin(sqrt(sin2((φ1-φ0)*.5) + cos(φ0)*cos(φ1) * sin2((λ1-λ0)*.5)));
    }
    
    /**
     * Spherical excess via Huilier formula
     * @param a angular distance
     * @param b angular distance
     * @param c angular distance
     * @return sphericalExcess (= aire sur une sphere de rayon 1)
     */
    static double sphericalExcess(final double a, final double b, final double c) {
        // Semi-perimeter
        final double s = .5*(a+b+c);
        return 4.*atan(sqrt(tan(s*.5)*tan((s-a)*.5)*tan((s-b)*.5)*tan((s-c)*.5)));
    }
    
    static double spericalArea(final List<LatLonPoint> polygon, final double r) {
        final int nb = polygon.size();
        double φ0, λ0 ,φ1, λ1, ESum=0;
        for (int i=0; i<nb; i++) {
            λ0 = polygon.get(i).getRadLon();        φ0 = polygon.get(i).getRadLat();
            λ1 = polygon.get((i+1)%nb).getRadLon(); φ1 = polygon.get((i+1)%nb).getRadLat();
            if (λ0!=λ1) {
                ESum += (λ0<λ1?1:-1)*sphericalExcess(sphericalDistance(φ0, λ0, φ1, λ1), φ0+π*.5, φ1+π*.5);
            }
        }        
        return ESum*r*r;
    }

    public static void main(String[] args) throws IOException {
        Double[] lats = new Double[] {0.,20.,20.};
        Double[] lons = new Double[] {0.,20.,0.};
        
        List<Double> lon = new ArrayList(Arrays.asList(lats));
        List<Double> lat = new ArrayList(Arrays.asList(lons));

    //    double area = area(lat, lon);
     //   System.out.println(area);
    }
}

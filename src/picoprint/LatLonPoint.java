package picoprint;

//import fr.meteo.synopsis.client.tool.geometrie.EarthGeometry;
//import fr.meteo.synopsis.client.json.JsonDecoder;
//import fr.meteo.synopsis.client.json.JsonException;
import java.awt.geom.Point2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * A Point2D representation of LatLonPoints, used integrate with the
 * Projections. These LatLonPoints wrap their internal decimal degree values so
 * the latitude are between -90 and 90 and the longitudes are between -180 and
 * 180. Radian values are precalculated and held within the object.
 * <P>
 *
 * The LatLonPoint is an abstract class and can't be instantiated directly. You
 * need to create a Float or Double version of a LatLonPoint, much like the
 * Point2D object.
 * <P>
 *
 * @author dietrick
 */
public abstract class LatLonPoint extends Point2D implements Cloneable, Serializable {

    private static final long serialVersionUID = 4416029542303298672L;
    //
    public static final double NORTH_POLE = 90.0;
    public static final double SOUTH_POLE = -NORTH_POLE;
    public static final double DATELINE = 180.0;
    public static final double LON_RANGE = 360.0;
    public static final double EQUIVALENT_TOLERANCE = 0.00001;
   
    protected LatLonPoint() {
    }

    /**
     * Factory method that will create a LatLonPoint.Double from a Point2D
     * object. If pt2D is already a LatLonPoint.Double object, it is simply
     * returned.
     *
     * @param pt2D
     * @return a LatLonPoint.Double object.
     */
    public static LatLonPoint getDouble(final Point2D pt2D) {
        if (pt2D instanceof Double) {
            return (Double) pt2D;
        } else {
            return new Double(pt2D);
        }
    }

    /**
     * Set the latitude, longitude for this point.
     *
     * @param lat decimal degree latitude
     * @param lon decimal degree longitude.
     */
    public abstract void setLatLon(final double lat, final double lon);

    /**
     * Set the latitude, longitude for this point, with the option of noting
     * whether the values are in degrees or radians.
     *
     * @param lat latitude
     * @param lon longitude.
     * @param isRadians true of values are radians.
     */
    public abstract void setLatLon(final double lat, final double lon, final boolean isRadians);

    /**
     * @return decimal degree longitude as a float.
     */
    public abstract double getLongitude();

    /**
     * @return decimal degree latitude as a float.
     */
    public abstract double getLatitude();

    /**
     * @return radian longitude value.
     */
    public abstract double getRadLon();

    /**
     * @return radian latitude value.
     */
    public abstract double getRadLat();

    /**
     * Set decimal degree latitude.
     */
    public abstract void setLatitude(double lat);

    /**
     * Set decimal degree longitude.
     */
    public abstract void setLongitude(double lon);

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LatLonPoint other = (LatLonPoint) obj;
        return getLatitude() == other.getLatitude() && getLongitude() == other.getLongitude();
    }

    /**
     * Double precision version of LatLonPoint.
     *
     * @author dietrick
     */
    public static class Double extends LatLonPoint {

        /**
         *
         */
        private static final long serialVersionUID = -7463055211717523471L;
        protected double lat;
        protected double lon;
        protected transient double radLat;
        protected transient double radLon;
         private DecimalFormat df = new DecimalFormat("#.00");


        /**
         * Default constructor, values set to 0, 0.
         */
        public Double() {
        }

        /**
         * Set the latitude, longitude for this point in decimal degrees.
         *
         * @param lat latitude
         * @param lon longitude.
         */
        public Double(final double lat, final double lon) {
            setLatLon(lat, lon, false);
        }

        /**
         * Set the latitude, longitude for this point, with the option of noting
         * whether the values are in degrees or radians.
         *
         * @param lat latitude
         * @param lon longitude.
         * @param isRadian true of values are radians.
         */
        public Double(final double lat, final double lon, final boolean isRadian) {
            setLatLon(lat, lon, isRadian);
        }

        /**
         * Create Double version from another LatLonPoint.
         *
         * @param llp
         */
        public Double(final LatLonPoint llp) {
            setLatLon(llp.getY(), llp.getX(), false);
        }

        /**
         * Create Double version from Point2D object, where the x, y values are
         * expected to be decimal degrees.
         *
         * @param pt2D
         */
        public Double(final Point2D pt2D) {
            setLatLon(pt2D.getY(), pt2D.getX(), false);
        }

        /**
         * Point2D method, inheriting signature!!
         *
         * @param x longitude value in decimal degrees.
         * @param y latitude value in decimal degrees.
         */
        @Override
        public void setLocation(final double x, final double y) {
            setLatLon(y, x, false);
        }

        /**
         * Set latitude and longitude.
         *
         * @param lat latitude in decimal degrees.
         * @param lon longitude in decimal degrees.
         */
        @Override
        public void setLatLon(final double lat, final double lon) {
            setLatLon(lat, lon, false);
        }

        /**
         * Set latitude and longitude.
         *
         * @param lat latitude.
         * @param lon longitude.
         * @param isRadians true if lat/lon values are radians.
         */
        @Override
        public void setLatLon(final double lat, final double lon, final boolean isRadians) {
            if (isRadians) {
                radLat = lat;
                radLon = lon;
                this.lat = Math.toDegrees(lat);
                this.lon = Math.toDegrees(lon);
            } else {
                this.lat = normalizeLatitude(lat);
                this.lon = wrapLongitude(lon);
                radLat = Math.toRadians(lat);
                radLon = Math.toRadians(lon);
            }
        }

        /**
         * @return longitude in decimal degrees.
         */
        @Override
        public double getX() {
            return lon;
        }

        /**
         * @return latitude in decimal degrees.
         */
        @Override
        public double getY() {
            return lat;
        }

        /**
         * @return float latitude in decimal degrees.
         */
        @Override
        public double getLatitude() {
            return lat;
        }

        /**
         * @return float longitude in decimal degrees.
         */
        @Override
        public double getLongitude() {
            return lon;
        }

        /**
         * @return radian longitude.
         */
        @Override
        public double getRadLon() {
            return radLon;
        }

        /**
         * @return radian latitude.
         */
        @Override
        public double getRadLat() {
            return radLat;
        }

        /**
         * Set latitude.
         *
         * @param lat latitude in decimal degrees
         */
        @Override
        public void setLatitude(final double lat) {
            this.lat = normalizeLatitude(lat);
            radLat = Math.toRadians(lat);
        }

        /**
         * Set longitude.
         *
         * @param lon longitude in decimal degrees
         */
        @Override
        public void setLongitude(final double lon) {
            this.lon = wrapLongitude(lon);
            radLon = Math.toRadians(lon);
        }

        /**
         * Write object.
         *
         * @param s DataOutputStream
         */
        public void write(final DataOutputStream s) throws IOException {
            // Write my information
            s.writeDouble(lat);
            s.writeDouble(lon);
        }

        /**
         * Read object. Assumes that the floats read off the stream will be in
         * decimal degrees. Latitude read off the stream first, then longitude.
         *
         * @param s DataInputStream
         */
        public void read(final DataInputStream s) throws IOException {
            setLatLon(s.readDouble(), s.readDouble(), false);
        }

        /**
         * Read object. Latitude read off the stream first, then longitude.
         *
         * @param s DataInputStream
         * @param inRadians if true, the floats read off stream will be
         * considered to be radians. Otherwise, they will be considered to be
         * decimal degrees.
         */
        public void read(final DataInputStream s, final boolean inRadians)
                throws IOException {
            setLatLon(s.readDouble(), s.readDouble(), inRadians);
        }

        /**
         * Calculate the <code>radLat</code> and <code>radLon</code> instance
         * variables upon deserialization. Also, check <code>lat</code> and
         * <code>lon</code> for safety; someone may have tampered with the
         * stream.
         *
         * @param stream Stream to read <code>lat</code> and <code>lon</code>
         * from.
         */
        private void readObject(final java.io.ObjectInputStream stream)
                throws IOException, ClassNotFoundException {
            stream.defaultReadObject();
            lat = normalizeLatitude(lat);
            lon = wrapLongitude(lon);
            radLat = Math.toRadians(lat);
            radLon = Math.toRadians(lon);
        }

        @Override
        public String toString() {
            return "LatLonPoint.Double[lat=" + lat + ",lon=" + lon + "]";
        }

        public String toForamtedString() {
            return "lat : " + df.format(lat) + ", lon : " + df.format(lon);
        }

    }

    /**
     * Set location values from another lat/lon point.
     *
     * @param llp
     */
    public void setLatLon(final LatLonPoint llp) {
        setLatLon(llp.getY(), llp.getX(), false);
    }

    /**
     * Ensure latitude is between the poles.
     *
     * @param lat
     * @return latitude greater than or equal to -90 and less than or equal to
     * 90.
     */
    public static float normalizeLatitude(final float lat) {
        return (float) normalizeLatitude((double) lat);
    }

    /**
     * Sets latitude to something sane.
     *
     * @param lat latitude in decimal degrees
     * @return float normalized latitude in decimal degrees (&minus;90&deg; &le;
     * &phi; &le; 90&deg;)
     */
    public static double normalizeLatitude(final double lat) {
        return lat > NORTH_POLE ? NORTH_POLE : lat < SOUTH_POLE ? SOUTH_POLE : lat;
    }

    /**
     * Ensure the longitude is between the date line.
     *
     * @param lon
     * @return longitude that is smaller than or equal to 180 and greater than
     * or equal to -180
     */
    public static float wrapLongitude(final float lon) {
        return (float) wrapLongitude((double) lon);
    }

    /**
     * Sets longitude to something sane.
     *
     * @param lon longitude in decimal degrees
     * @return float wrapped longitude in decimal degrees (&minus;180&deg; &le;
     * &lambda; &le; 180&deg;)
     */
    public static double wrapLongitude(final double lon) {
        if ((lon < -DATELINE) || (lon > DATELINE)) {
            double lng = (lon + DATELINE) % LON_RANGE;
            return lng < 0 ? DATELINE + lng : -DATELINE + lng;
        }
        return lon;
    }

    /**
     * Check if latitude is bogus. Latitude is invalid if lat &gt; 90&deg; or if
     * lat &lt; &minus;90&deg;.
     *
     * @param lat latitude in decimal degrees
     * @return boolean true if latitude is invalid
     */
    public static boolean isInvalidLatitude(final double lat) {
        return lat > NORTH_POLE || lat < SOUTH_POLE;
    }

    /**
     * Check if longitude is bogus. Longitude is invalid if lon &gt; 180&deg; or
     * if lon &lt; &minus;180&deg;.
     *
     * @param lon longitude in decimal degrees
     * @return boolean true if longitude is invalid
     */
    public static boolean isInvalidLongitude(final double lon) {
        return lon < -DATELINE || lon > DATELINE;
    }

    public static List<LatLonPoint> convert(final List<Point2D> pts) {
        if (pts == null) {
            return null;
        }
        final List<LatLonPoint> lstCoords = new ArrayList<>();
        for (Point2D pt : pts) {
            lstCoords.add(new LatLonPoint.Double(pt));
        }
        return lstCoords;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

}

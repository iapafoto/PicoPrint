package bezier;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BezierPath {

    static final Matcher matchPoint = Pattern.compile("\\s*(\\d+)[^\\d]+(\\d+)\\s*").matcher("");

    BezierListProducer pathBuilder;

    public Path2D getPath2D() {
        return pathBuilder.path;
    }
    
    /**
     * Creates a new instance of Animate
     */
    public BezierPath() {
    }

    public void parsePathString(String d) {

        this.pathBuilder = new BezierListProducer();

        parsePathList(d);
    }

    protected void parsePathList(String list) {
        
        // TODO ajouter les arcs
        final Matcher matchPathCmd = Pattern.compile("([MmLlHhVvAaQqTtCcSsZz])|([-+]?((\\d*\\.\\d+)|(\\d+))([eE][-+]?\\d+)?)").matcher(list);

        //Tokenize
        LinkedList<String> tokens = new LinkedList();
        while (matchPathCmd.find()) {
            tokens.addLast(matchPathCmd.group());
        }

        char curCmd = 'Z';
        while (!tokens.isEmpty()) {
            String curToken = tokens.removeFirst();
            char initChar = curToken.charAt(0);
            if ((initChar >= 'A' && initChar <= 'Z') || (initChar >= 'a' && initChar <= 'z')) {
                curCmd = initChar;
            } else {
                tokens.addFirst(curToken);
            }

            switch (curCmd) {
                case 'M':
                    pathBuilder.movetoAbs(nextFloat(tokens), nextFloat(tokens));
                    curCmd = 'L';
                    break;
                case 'm':
                    pathBuilder.movetoRel(nextFloat(tokens), nextFloat(tokens));
                    curCmd = 'l';
                    break;
                case 'L':
                    pathBuilder.linetoAbs(nextFloat(tokens), nextFloat(tokens));
                    break;
                case 'l':
                    pathBuilder.linetoRel(nextFloat(tokens), nextFloat(tokens));
                    break;
                case 'H':
                    pathBuilder.linetoHorizontalAbs(nextFloat(tokens));
                    break;
                case 'h':
                    pathBuilder.linetoHorizontalRel(nextFloat(tokens));
                    break;
                case 'V':
                    pathBuilder.linetoVerticalAbs(nextFloat(tokens));
                    break;
                case 'v':
                    pathBuilder.linetoVerticalRel(nextFloat(tokens));
                    break;
                case 'A':
                    pathBuilder.arcAbs(nextFloat(tokens), nextFloat(tokens), nextFloat(tokens), nextFloat(tokens), nextFloat(tokens), nextFloat(tokens), nextFloat(tokens));
                case 'a':
                    pathBuilder.arcRel(nextFloat(tokens), nextFloat(tokens), nextFloat(tokens), nextFloat(tokens), nextFloat(tokens), nextFloat(tokens), nextFloat(tokens));
                    break;
                case 'Q':
                    pathBuilder.curvetoQuadraticAbs(nextFloat(tokens), nextFloat(tokens),
                            nextFloat(tokens), nextFloat(tokens));
                    break;
                case 'q':
                    pathBuilder.curvetoQuadraticAbs(nextFloat(tokens), nextFloat(tokens),
                            nextFloat(tokens), nextFloat(tokens));
                    break;
                case 'T':
                    pathBuilder.curvetoQuadraticSmoothAbs(nextFloat(tokens), nextFloat(tokens));
                    break;
                case 't':
                    pathBuilder.curvetoQuadraticSmoothRel(nextFloat(tokens), nextFloat(tokens));
                    break;
                case 'C':
                    pathBuilder.curvetoCubicAbs(nextFloat(tokens), nextFloat(tokens),
                            nextFloat(tokens), nextFloat(tokens),
                            nextFloat(tokens), nextFloat(tokens));
                    break;
                case 'c':
                    pathBuilder.curvetoCubicRel(nextFloat(tokens), nextFloat(tokens),
                            nextFloat(tokens), nextFloat(tokens),
                            nextFloat(tokens), nextFloat(tokens));
                    break;
                case 'S':
                    pathBuilder.curvetoCubicSmoothAbs(nextFloat(tokens), nextFloat(tokens),
                            nextFloat(tokens), nextFloat(tokens));
                    break;
                case 's':
                    pathBuilder.curvetoCubicSmoothRel(nextFloat(tokens), nextFloat(tokens),
                            nextFloat(tokens), nextFloat(tokens));
                    break;
                case 'Z':
                case 'z':
                    pathBuilder.closePath();
                    break;
                default:
                    throw new RuntimeException("Invalid path element");
            }
        }
    }

    static protected float nextFloat(LinkedList<String> l) {
        String s = l.removeFirst();
        return Float.parseFloat(s);
    }

    /**
     * Evaluates this animation element for the passed interpolation time.
     * Interp must be on [0..1].
     * @param interp
     * @return 
     */
    public Vector2 eval(float interp) {
        Vector2 point = new Vector2();

        double curLength = pathBuilder.curveLength * interp;
        for (Bezier bez : pathBuilder.bezierSegs) {
            double bezLength = bez.getLength();
            if (curLength < bezLength) {
                double param = curLength / bezLength;
                bez.eval(param, point);
                break;
            }
            curLength -= bezLength;
        }

        return point;
    }

    static String geomDP(float v) {
        return Float.toString(v);
    }
    
    public static String getSVGPathData(Path2D path) {
        StringBuilder b = new StringBuilder("d=\"");
        float[] coords = new float[6];
        boolean first = true;
        PathIterator iterator = path.getPathIterator(null);
        while (!iterator.isDone()) {
            int type = iterator.currentSegment(coords);
            if (!first) {
                b.append(" ");
            }
            first = false;
            switch (type) {
            case (PathIterator.SEG_MOVETO):
                b.append("M ").append(geomDP(coords[0])).append(" ")
                        .append(geomDP(coords[1]));
                break;
            case (PathIterator.SEG_LINETO):
                b.append("L ").append(geomDP(coords[0])).append(" ")
                        .append(geomDP(coords[1]));
                break;
            case (PathIterator.SEG_QUADTO):
                b.append("Q ").append(geomDP(coords[0]))
                        .append(" ").append(geomDP(coords[1]))
                        .append(" ").append(geomDP(coords[2]))
                        .append(" ").append(geomDP(coords[3]));
                break;
            case (PathIterator.SEG_CUBICTO):
                b.append("C ").append(geomDP(coords[0])).append(" ")
                        .append(geomDP(coords[1])).append(" ")
                        .append(geomDP(coords[2])).append(" ")
                        .append(geomDP(coords[3])).append(" ")
                        .append(geomDP(coords[4])).append(" ")
                        .append(geomDP(coords[5]));
                break;
            case (PathIterator.SEG_CLOSE):
                b.append("Z ");
                break;
            default:
                break;
            }
            iterator.next();
        }  
        return b.append("\"").toString();
    }
}

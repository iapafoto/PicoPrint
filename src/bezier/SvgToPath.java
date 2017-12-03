package bezier;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SvgToPath {

    static final Matcher MATCH_POINT = Pattern.compile("\\s*(\\d+)[^\\d]+(\\d+)\\s*").matcher("");

    /**
     * Creates a new instance of Animate
     */
    public SvgToPath() {
    }
    
    public static Path2D parsePathString(String svg) {
        BezierListProducer blp = parsePathList(svg);
        return blp.path;
    }

    protected static BezierListProducer parsePathList(String list) {
        BezierListProducer pathBuilder = new BezierListProducer();
                
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
                    pathBuilder.movetoAbs(nextDouble(tokens), nextDouble(tokens));
                    curCmd = 'L';
                    break;
                case 'm':
                    pathBuilder.movetoRel(nextDouble(tokens), nextDouble(tokens));
                    curCmd = 'l';
                    break;
                case 'L':
                    pathBuilder.linetoAbs(nextDouble(tokens), nextDouble(tokens));
                    break;
                case 'l':
                    pathBuilder.linetoRel(nextDouble(tokens), nextDouble(tokens));
                    break;
                case 'H':
                    pathBuilder.linetoHorizontalAbs(nextDouble(tokens));
                    break;
                case 'h':
                    pathBuilder.linetoHorizontalRel(nextDouble(tokens));
                    break;
                case 'V':
                    pathBuilder.linetoVerticalAbs(nextDouble(tokens));
                    break;
                case 'v':
                    pathBuilder.linetoVerticalRel(nextDouble(tokens));
                    break;
                case 'A':
                    pathBuilder.arcAbs(nextDouble(tokens), nextDouble(tokens), nextDouble(tokens), nextDouble(tokens), nextDouble(tokens), nextDouble(tokens), nextDouble(tokens));
                case 'a':
                    pathBuilder.arcRel(nextDouble(tokens), nextDouble(tokens), nextDouble(tokens), nextDouble(tokens), nextDouble(tokens), nextDouble(tokens), nextDouble(tokens));
                    break;
                case 'Q':
                    pathBuilder.curvetoQuadraticAbs(nextDouble(tokens), nextDouble(tokens),
                            nextDouble(tokens), nextDouble(tokens));
                    break;
                case 'q':
                    pathBuilder.curvetoQuadraticAbs(nextDouble(tokens), nextDouble(tokens),
                            nextDouble(tokens), nextDouble(tokens));
                    break;
                case 'T':
                    pathBuilder.curvetoQuadraticSmoothAbs(nextDouble(tokens), nextDouble(tokens));
                    break;
                case 't':
                    pathBuilder.curvetoQuadraticSmoothRel(nextDouble(tokens), nextDouble(tokens));
                    break;
                case 'C':
                    pathBuilder.curvetoCubicAbs(nextDouble(tokens), nextDouble(tokens),
                            nextDouble(tokens), nextDouble(tokens),
                            nextDouble(tokens), nextDouble(tokens));
                    break;
                case 'c':
                    pathBuilder.curvetoCubicRel(nextDouble(tokens), nextDouble(tokens),
                            nextDouble(tokens), nextDouble(tokens),
                            nextDouble(tokens), nextDouble(tokens));
                    break;
                case 'S':
                    pathBuilder.curvetoCubicSmoothAbs(nextDouble(tokens), nextDouble(tokens),
                            nextDouble(tokens), nextDouble(tokens));
                    break;
                case 's':
                    pathBuilder.curvetoCubicSmoothRel(nextDouble(tokens), nextDouble(tokens),
                            nextDouble(tokens), nextDouble(tokens));
                    break;
                case 'Z':
                case 'z':
                    pathBuilder.closePath();
                    break;
                default:
                    throw new RuntimeException("Invalid path element");
            }
        }
        return pathBuilder;
    }

    static protected double nextDouble(LinkedList<String> l) {
        String s = l.removeFirst();
        return Double.parseDouble(s);
    }


    static String geomDP(double v) {
        return Double.toString(v);
    }
    
    public static String pathToSvg(Path2D path) {
        StringBuilder b = new StringBuilder("d=\"");
        double[] coords = new double[6];
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

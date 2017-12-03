/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package picoprint;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import static picoprint.ImageToDrawEvaluateLine.lineDrawn;

/**
 *
 * @author durands
 */
public class PicoPrint {
    final static double pi4 = Math.PI/4;
    
    final static double // all in mm
            R1 = 20,//26.42,
            R2 = 20,//26.42,
            DISTANCE_AXES = 548, 
            CX1 = -DISTANCE_AXES/2,
            CX2 = DISTANCE_AXES/2,
            CY1 = 0,
            CY2 = 0,
            POS_X0 = 0,
            POS_Y0 = 500,
            GCODE_SCALE = .1;  //  si on part des mm mais la le reglage de grlb est en cm
    
    static Rectangle2D DRAWING_AREA = new Rectangle2D.Double(CX1*.8, DISTANCE_AXES*.1, DISTANCE_AXES*.8, DISTANCE_AXES);
    
    static final double
            REF_ROPE_LENGTH_1 = convertPosToMove(POS_X0, POS_Y0, CX1, CY1, R1, 1),  // Longeur du fil quand le curseur est au point de reference
            REF_ROPE_LENGTH_2 = convertPosToMove(POS_X0, POS_Y0, CX2, CY2, R2, 1);  // Longeur du fil quand le curseur est au point de reference
    
    
    // Calcul de la distance de corde necessaire pour une roue donnée
    // double sign = 1; // indique si on passe par au dessus (1) ou au dessous (!=1)
  
    public static Path2D convertPosToRopeLength(double cx1, double cy1, double r1, double cx2, double cy2, double r2, Shape shape, double sign) {
        Path2D moves = new Path2D.Double();

        final double[] crds = new double[2];
        double d1, d2, x0=0, y0=0, xlast=0, ylast=0;

        for (PathIterator pit = shape.getPathIterator(null, .1); !pit.isDone(); pit.next()) {
            switch (pit.currentSegment(crds)) {
                case PathIterator.SEG_MOVETO:
                    d1 = convertPosToMove(crds[0], crds[1], cx1, cy1, r1, sign);
                    d2 = convertPosToMove(crds[0], crds[1], cx1, cy1, r1, sign);
                    x0=xlast=crds[0]; y0=ylast=crds[1];
                    moves.moveTo(d1, d2);
                    break;
                case PathIterator.SEG_LINETO:
                    // TODO si trop long depuis xlast, ylast couper en segments
                    d1 = convertPosToMove(crds[0], crds[1], cx1, cy1, r1, sign);
                    d2 = convertPosToMove(crds[0], crds[1], cx1, cy1, r1, sign);
                    xlast=crds[0]; ylast=crds[1];
                    moves.lineTo(d1, d2);
                    break;
                case PathIterator.SEG_CLOSE:
                    // TODO si trop long entre xlast, ylast et x0, y0 couper en segments
                    moves.closePath();
                    break;
                default:
            }
        }

        return moves;
    }

    public static double[] calcultePathLength(Shape shape) {
        final double[] crds = new double[2];
        double x0=0, y0=0, xlast=0, ylast=0;
        double lengthMove = 0, lengthDraw = 0;

        for (PathIterator pit = shape.getPathIterator(null, .1); !pit.isDone(); pit.next()) {
            switch (pit.currentSegment(crds)) {
                case PathIterator.SEG_MOVETO:
                    lengthMove += sqrt((xlast-crds[0])*(xlast-crds[0]) + (ylast-crds[1])*(ylast-crds[1]));
                    x0=xlast=crds[0]; y0=ylast=crds[1];
                    break;
                case PathIterator.SEG_LINETO:
                    lengthDraw += sqrt((xlast-crds[0])*(xlast-crds[0]) + (ylast-crds[1])*(ylast-crds[1]));
                    xlast=crds[0]; ylast=crds[1];
                    break;
                case PathIterator.SEG_CLOSE:
                    lengthDraw += sqrt((xlast-x0)*(xlast-x0) + (ylast-y0)*(ylast-y0));
                    break;
                default:
            }
        }
        return new double[] {lengthMove, lengthDraw};
    }
    
    private static double convertPosToMove(double x, double y, double cx, double cy, double r, double sign) {
        double d, dt, e;
        // Distance a l'axe
        d = Math.sqrt((cx-x)*(cx-x) + (cy-y)*(cy-y));
        // Distance a la partie tangente
        dt = Math.sqrt(d*d - r*r);
        // Distance de corde enroulee (cas au dessus)
        e = r*(Math.asin(r/d) + Math.asin(Math.abs(cy-y)/d));
        // Distance totale
        return dt + (sign == 1 ? e : (pi4-e));
    }
        
    public static Rectangle2D scaleRect(final Rectangle2D r, final double width, final double height) {
        // Calcul de l'aspect ratio
        final double k = height / width;
        // Retour du plus grand rectangle contenu dans la zone et repondant à l'aspect ratio
        if (k * r.getWidth() < r.getHeight()) {
            double hNew = k * r.getWidth();
            return new Rectangle2D.Double(r.getX(), r.getY() + ((r.getHeight() - hNew) / 2.), r.getWidth(), hNew);
        } else {
            double wNew = r.getHeight() / k;
            return new Rectangle2D.Double(r.getX() + (r.getWidth() - wNew) / 2., r.getY(), wNew, r.getHeight());
        }
    }
    
    
    public static Path2D toCurvePath(double[] pts) {
        Path2D path = new Path2D.Double();
        double x,y,x2,y2;
        x = (pts[0]+pts[2])/2.;
        y = (pts[1]+pts[3])/2.;
        
        path.moveTo(x, y);
        for (int i=2; i<pts.length-4; i+=2) {
            x2 = (pts[i]+pts[i+2])/2.;
            y2 = (pts[i+1]+pts[i+3])/2.;
            path.quadTo(pts[i], pts[i+1], x2,y2);
        }
        return path;
    }

    public static Path2D toLinePath(double[] pts) {
        Path2D path = new Path2D.Double();
        double x,y;
        x = pts[0]; y=pts[1];
        path.moveTo(x, y);
        for (int i=2; i<pts.length-2; i+=2) {
            path.lineTo(pts[i], pts[i+1]);
        }
        return path;
    }
      
      
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {

        
    //    File svgFile = new File("C:\\Users\\durands\\Desktop\\FabLab\\PicoPrint\\res\\drawingRaw.svg");
    //    File svgFile = new File("C:\\Users\\durands\\Desktop\\FabLab\\renard.txt");
          // TODO code application logic here
//        SvgToPath parser = new SvgToPath();
//        try  {
//            BufferedReader r = Files.newBufferedReader(svgFile.toPath(), Charset.defaultCharset());
//            StringBuilder sb = new StringBuilder();
//            r.lines().forEach(line -> sb.append(line));
//            parser.parsePathString(sb.toString());
//            
//        } catch (IOException ex) {
//            Logger.getLogger(PicoPrint.class.getName()).log(Level.SEVERE, null, ex);
//        }

// - Extraction d'un path2D a partir d'une image -------------------------------

        String filename = "C:\\Users\\durands\\Desktop\\highland-cow-bw-athena-mckinzie.jpg";
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(filename));
        } catch (IOException e) {
            System.exit(1);
        }
        
        double[] path = ImageToDrawEvaluateLine.findDrawPath(image);
       // double[] path = makeBrownien(image);
        
        Path2D drawingPath = toCurvePath(path); // toLinePath(path);

        
// - Recentrage / travail sur le dessin en coordonnees classique --------------- 
    
        // Redimensionnement pour etre bien centre dans la zone de dessin
        Rectangle2D recCenter = scaleRect(DRAWING_AREA, lineDrawn.getWidth(), lineDrawn.getHeight());
        double scale = recCenter.getWidth()/lineDrawn.getWidth();
        AffineTransform at = AffineTransform.getTranslateInstance(recCenter.getX(), recCenter.getY());    
        at.scale(scale, scale);
        
        // Calcul de la shape bien placé, bien scale, en mm 
        drawingPath.transform(at);
        
        // Distance de parcours sur la feuille
        double[] pathLength = calcultePathLength(drawingPath); // en mm
        System.out.println("Distance Deplacement : " + (pathLength[0]/1000) + "m"); 
        System.out.println("Distance Dessin      : " + (pathLength[1]/1000) + "m"); 
        System.out.println("Distance Totale      : " + ((pathLength[0]+pathLength[1])/1000) + "m"); 
        
        
// - Convertion en mouvements Polargraph ---------------------------------------
    
        // Calcul des longeurs de cordes necessaires sur chaques axes
        Path2D ropeLengths = convertPosToRopeLength(CX1, CY1, R1, CX2, CY2, R2, drawingPath, 1);
        
        // Calcul des mouvements de motuurs necessaires pour obtenir ces longeurs de cordes
        Shape motorMoves = ropeLengthsToMotorMoves(ropeLengths, GCODE_SCALE, REF_ROPE_LENGTH_1, REF_ROPE_LENGTH_2);
        
        // Extraction du GCode pour commander les moteurs
        String gcode = toGCode(motorMoves);
        
        // Sauvegarde du GCode
        File file = new File(filename+".gcode");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(gcode);
        }
     
        
// - Simulation du rendu -------------------------------------------------------        
        
        JFrame frame = new JFrame();

        frame.setSize(800, 1000);
        PreviewPanel panel = new PreviewPanel(CX1, CY1, R1, CX2, CY2, R2, at, lineDrawn, DRAWING_AREA);
        frame.setContentPane(panel);
        frame.setVisible(true);
        
        panel.animatePanel(at, drawingPath, panel);
    }
    
  
    
    
    public static String toGCode(final Shape shape) {
        final StringBuilder sb = new StringBuilder();
        
        sb.append("G21\r\n");   // Coordonnees en mm
       // sb.append("M3 S0\r\n"); 
        sb.append("G0 F2000\r\n"); // Vitesse de deplacement
        sb.append("G1 F2000\r\n"); // Vitesse de tracé
        sb.append("G90\r\n"); // en coordonnees absolues

        final double[] c = new double[2];
      
        for (PathIterator pit = shape.getPathIterator(null, .1); !pit.isDone(); pit.next()) {
            switch (pit.currentSegment(c)) {
                case PathIterator.SEG_MOVETO:
                    sb.append("G0").append(" X").append(c[0]).append(" Y").append(c[1]).append("\r\n");
                    break;
                case PathIterator.SEG_LINETO:
                    sb.append("G1").append(" X").append(c[0]).append(" Y").append(c[1]).append("\r\n");
                    break;
                case PathIterator.SEG_CLOSE:
                    sb.append("G1").append(" X").append(c[0]).append(" Y").append(c[1]).append("\r\n");
                    break;
                default:
            }
        }
        return sb.toString();
    }
    

    public static Shape ropeLengthsToMotorMoves(Shape ropeLengths, double gcodeScale, double refLength1, double refLength2) {
        AffineTransform af = AffineTransform.getScaleInstance(gcodeScale, gcodeScale);
        af.translate(-refLength1, -refLength2);
        return af.createTransformedShape(ropeLengths);
    }
    
    
}

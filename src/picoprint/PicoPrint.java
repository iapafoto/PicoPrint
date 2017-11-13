/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package picoprint;

import bezier.BezierPath;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_STROKE_CONTROL;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.RenderingHints.VALUE_STROKE_PURE;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 *
 * @author durands
 */
public class PicoPrint {
    
    public static class Pos {
        double x;
        double y;
        boolean isDrawing;
        
        public Pos(double x, double y, boolean isDrawing) {
            this.x = x;
            this.y = y;
            this.isDrawing = isDrawing;
        }
        
        double distance(Pos p) {
            return Math.sqrt((p.x-x)*(p.x-x) + (p.y-y)*(p.y-y));
        }
    };
    
    // Calcul de la distance de corde necessaire pour une roue donnée
    // double sign = 1; // indique si on passe par au dessus (1) ou au dessous (!=1)
    public static List<Double> convertPosToMove(double cx, double cy, double r, List<Pos> lst, double sign) {
        List<Double> mv = new ArrayList(lst.size());
        double r2 = r*r;
       
        double d, dt, e, length;
        double pi4 = Math.PI/4;
        for (Pos p : lst) {
            // Distance a l'axe
            d = Math.sqrt((cx-p.x)*(cx-p.x) + (cy-p.y)*(cy-p.y));
            // Distance a la partie tangente
            dt = Math.sqrt(d*d - r2);
            // Distance de corde enroulee (cas au dessus)
            e = r*(Math.asin(r/d) + Math.asin(Math.abs(cy-p.y)/d));
            // Distance totale
            length = dt + (sign == 1 ? e : (pi4-e));
            mv.add(length);
        }
        return mv;
    }


    public static double mix(final double v1, final double v2, final double k) {
        return v1 + k*(v2 - v1);
    }
    
    public static Pos mix(final Pos v1, final Pos v2, final double k) {
        return new Pos(mix(v1.x, v2.x, k), mix(v1.y, v2.y, k), v2.isDrawing);
    }

    
    public static List<Pos> extractPositions(final Shape shape) {
            final PathIterator pit = shape.getPathIterator(null, 2);

            List<Pos> lst = new ArrayList();
            
            if (pit.isDone()) {
                return null;
            }
            
            final double[] crds = new double[2];
            double x0=0, y0=0;
          
            for (; !pit.isDone(); pit.next()) {
                switch (pit.currentSegment(crds)) {
                    case PathIterator.SEG_MOVETO:
                        lst.add(new Pos(x0 = crds[0], y0 = crds[1], false));
                        break;
                    case PathIterator.SEG_LINETO:
                        lst.add(new Pos(crds[0], crds[1], true));
                        break;
                    case PathIterator.SEG_CLOSE:
                        lst.add(new Pos(x0, y0, true));
                        break;
                    default:
                }
            }
            return lst;
        }
    
    public static Pos currentPos;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        double r1 = 40;
        double cx1 = 50, cy1 = 50;
        double r2 = 40;
        double cx2 = 650, cy2 = 50;
        double x0 = 200, y0 = 400;
        
        File svgFile = new File("C:\\Users\\durands\\Desktop\\FabLab\\PicoPrint\\res\\drawingRaw.svg");
        // TODO code application logic here
        BezierPath parser = new BezierPath();
        try  {
              BufferedReader r = Files.newBufferedReader(svgFile.toPath(), Charset.defaultCharset());
            StringBuilder sb = new StringBuilder();
            r.lines().forEach(line -> sb.append(line));
            parser.parsePathString(sb.toString());
            
        } catch (IOException ex) {
            Logger.getLogger(PicoPrint.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Path2D path = new Path2D.Double(parser.getPath2D(), AffineTransform.getScaleInstance(2,2));
        path.transform(AffineTransform.getTranslateInstance(x0, y0));
        Rectangle rec = path.getBounds();
        
        List<Pos> lstPositions = extractPositions(path);
        
        
        JFrame frame = new JFrame();

        frame.setSize(800, 800);
        
        currentPos = lstPositions.get(0);
       
        Container panel = new Container() {
            @Override
            public void paint(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics;
                g2.setStroke(new BasicStroke(1));
                g2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(KEY_STROKE_CONTROL, VALUE_STROKE_PURE);

                g2.draw(path);
                
                g2.drawOval((int)(cx1-r1),(int)(cy1-r2),(int)(2*r1),(int)(2*r1));
                g2.drawOval((int)(cx2-r2),(int)(cy2-r2),(int)(2*r2),(int)(2*r2));
                
                double dtot = 650, d1, dt1, d2, dt2;
                
                // Distance a l'axe
                d1 = Math.sqrt((cx1-currentPos.x)*(cx1-currentPos.x) + (cy1-currentPos.y)*(cy1-currentPos.y));
                dt1 = Math.sqrt(d1*d1 - r1*r1);
                // Distance de corde enroulee (cas au dessus)
                double a1 = Math.asin(r1/d1) + Math.asin(Math.abs(cy1-currentPos.y)/d1);
                double angle1 = -3.14-(Math.asin(r1/d1) + Math.asin(Math.abs(cy1-currentPos.y)/d1));
                double intx1 = cx1 + r1*Math.sin(angle1);
                double inty1 = cy1 + r1*Math.cos(angle1);
                double length1 = a1*r1 + dt1;
                
                
                d2 = Math.sqrt((cx2-currentPos.x)*(cx2-currentPos.x) + (cy2-currentPos.y)*(cy2-currentPos.y));
                dt2 = Math.sqrt(d2*d2 - r2*r2);
                double a2 = Math.asin(r2/d2) + Math.asin(Math.abs(cy2-currentPos.y)/d2);
                double angle2 = -3.14+(a2);
                double intx2 = cx2 + r2*Math.sin(angle2);
                double inty2 = cy2 + r2*Math.cos(angle2);
                double length2 = a2*r2 + dt2;
//                g2.draw(new Ellipse2D.Double(intx1-3,inty1-3,6,6));
                
                g2.setColor(currentPos.isDrawing ? Color.red : Color.green);
                g2.draw(new Ellipse2D.Double(currentPos.x-3,currentPos.y-3,6,6));
                
                g2.setColor(Color.blue);                
                g2.fill(new Ellipse2D.Double(cx1-r1-3, cy1 + (dtot - dt1)-3,6,6));
                g2.setStroke(new BasicStroke(2.f, BasicStroke.JOIN_ROUND, BasicStroke.CAP_ROUND, 0, new float[] {10,10}, 0));
                g2.draw(new Line2D.Double(currentPos.x,currentPos.y,intx1,inty1));
                g2.draw(new Line2D.Double(cx1-r1, cy1 + (dtot - dt1), cx1-r1,cy1));
                
                AffineTransform memAT = g2.getTransform();
                g2.rotate(length1/r1, cx1, cy1);
                g2.setStroke(new BasicStroke(6.f,BasicStroke.JOIN_ROUND, BasicStroke.CAP_ROUND));
                g2.draw(new Line2D.Double(cx1-10, cy1,cx1+10, cy1));
                g2.draw(new Line2D.Double(cx1, cy1-10,cx1, cy1+10));
                g2.setTransform(memAT);
                
                g2.setColor(Color.red);                
                g2.fill(new Ellipse2D.Double(cx2+r2-3, cy2 + (dtot - dt2)-3,6,6));
                g2.setStroke(new BasicStroke(2.f, BasicStroke.JOIN_ROUND, BasicStroke.CAP_ROUND, 0, new float[] {10,10}, 0));
                g2.draw(new Line2D.Double(currentPos.x,currentPos.y,intx2,inty2));
                g2.draw(new Line2D.Double(cx2+r2, cy2 + (dtot - dt2), cx2+r2,cy2));
                
           //     memAT = g2.getTransform();
                g2.rotate(-length2/r2, cx2, cy2);
                g2.setStroke(new BasicStroke(6.f,BasicStroke.JOIN_ROUND, BasicStroke.CAP_ROUND));
                g2.draw(new Line2D.Double(cx2-10, cy2,cx2+10, cy2));
                g2.draw(new Line2D.Double(cx2, cy2-10,cx2, cy2+10));
                g2.setTransform(memAT);
            }
        };
        
        frame.setContentPane(panel);


        frame.setVisible(true);
        Pos pos, lastPos = lstPositions.get(0);
        
        double dt = .5;
        for (int t=1; t<lstPositions.size(); t++) {
            pos = lstPositions.get(t);
            double dist = pos.distance(lastPos);
            
            for (double d=0; d<dist; d+=dt) {
                currentPos = mix(lastPos,pos, Math.min(d/dist,1.));
                panel.repaint();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }   
            lastPos = pos;
        }
        
    }
    
}

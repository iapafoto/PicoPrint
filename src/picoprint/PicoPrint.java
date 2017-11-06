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
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
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

    public static double mix(final double v1, final double v2, final double k) {
        return v1 + k*(v2 - v1);
    }
    
    public static Pos mix(final Pos v1, final Pos v2, final double k) {
        return new Pos(mix(v1.x, v2.x, k), mix(v1.y, v2.y, k), v2.isDrawing);
    }
    
    
    static class Pos {
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
        
        File svgFile = new File("C:\\Users\\durands\\Desktop\\FabLab\\SebWriter\\drawingRaw.svg");
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
                g2.setColor(currentPos.isDrawing ? Color.red : Color.green);
                g2.draw(new Ellipse2D.Double(currentPos.x-3,currentPos.y-3,6,6));
            }
        };
        
        frame.setContentPane(panel);


        frame.setVisible(true);
        Pos pos, lastPos = lstPositions.get(0);
        
        double dt = .2;
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

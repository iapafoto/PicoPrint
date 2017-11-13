/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package picoprint;

/**
 *
 * @author durands
 */
import java.awt.*;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_STROKE_CONTROL;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.RenderingHints.VALUE_STROKE_PURE;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javax.imageio.*;
import javax.swing.*;
import static picoprint.PicoPrint.currentPos;
import static picoprint.PicoPrint.mix;

/**
 * A Java class to demonstrate how to load an image from disk with the ImageIO
 * class. Also shows how to display the image by creating an ImageIcon, placing
 * that icon an a JLabel, and placing that label on a JFrame.
 *
 * @author alvin alexander, alvinalexander.com
 */
public class ImageDemo {
    
    static class Panel extends Container {
        List<Path2D> pathRemix = null;
        public float time=0;

        Panel(List<Path2D> pathRemix) {
            this.pathRemix = pathRemix;
        }
        
        @Override
        public void paint(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics;
            g2.setStroke(new BasicStroke(1.f));
            g2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(KEY_STROKE_CONTROL, VALUE_STROKE_PURE);
            g2.setColor(new Color(0,0,0,32));
          //  g2.draw(pathRemix);
            for (int t=0; t<time; t++) {
                g2.draw(pathRemix.get(t));
            }
        }

        private void setTime(int d) {
            time = d;
        }
    };

    public static void main(String[] args) throws Exception {
        String filename = "C:\\Users\\durands\\Desktop\\mila2.png";
  
        JFrame editorFrame = new JFrame("Image Demo");
        editorFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(filename));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        
        editorFrame.setSize(image.getWidth(), image.getHeight());

        List<Path2D> pathRemix = toCurvePath(makeBrownien(image));

        final Panel panel = new Panel(pathRemix);

        editorFrame.setContentPane(panel);
        editorFrame.setVisible(true);
   //     Thread queryThread = new Thread() {
   //         public void run() {
                
        for (int d=0; d<pathRemix.size(); d+=50) {
            final int t = d;
//               SwingUtilities.invokeLater(() -> {
                panel.setTime(t);
                panel.repaint();
//             });
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }   
      //      }
      //    };
      //    queryThread.start();
     //   SwingUtilities.invokeLater(new Runnable() {
     //       public void run() {         
               
       //     }
       // });
    }
    
    public static double map(double i0, double i1, double o0, double o1, double i) {
        return o0 + (i-i0)*(o1-o0)/(i1-i0);
    }
    
    final public static double smoothstep(final double edge0, final double edge1, final double x) {
        final double t = Math.min(Math.max((x - edge0) / (edge1 - edge0), 0), 1);
        return t * t * (3 - t-t);
    }
    
    private static void drawLine(byte[] buff, int w, int h, int x1, int y1, int x2, int y2) {
        // delta of exact value and rounded value of the dependent variable
        int d = 0;
        int dx = Math.abs(x2 - x1),
            dy = Math.abs(y2 - y1);
        int dx2 = 2 * dx, // slope scaling factors to
            dy2 = 2 * dy; // avoid floating point
        int ix = x1 < x2 ? 1 : -1, // increment direction
            iy = y1 < y2 ? 1 : -1;
        int x = x1, y = y1;
 
        if (dx >= dy) {
            while (true) {
                buff[y*w+x] = 1;
                if (x == x2) break;
                x += ix;
                d += dy2;
                if (d > dx) {
                    y += iy;
                    d -= dx2;
                }
            }
        } else {
            while (true) {
                buff[y*w+x] = 1;
                if (y == y2) break;
                y += iy;
                d += dx2;
                if (d > dy) {
                    x += ix;
                    d -= dy2;
                }
            }
        }
    }
    
    public static double getAngleDiffRad(final double source, final double target) {
        return (target - source + Math.PI) % (2.*Math.PI) - Math.PI;
    }
    
    public static List<Path2D> toCurvePath(double[] pts) {
        List<Path2D> paths = new ArrayList();
        double x,y,x2,y2;
        x = (pts[0]+pts[2])/2.;
        y = (pts[1]+pts[3])/2.;
        for (int i=2; i<pts.length-4; i+=2) {
            Path2D path = new Path2D.Double();
            x2 = (pts[i]+pts[i+2])/2.;
            y2 = (pts[i+1]+pts[i+3])/2.;
            path.moveTo(x, y);
            path.quadTo(pts[i], pts[i+1], x2,y2);
            if (Math.sqrt((x-x2)*(x-x2)+(y-y2)*(y-y2)) < 128)
                paths.add(path);
            x = x2; y = y2;
        }
        return paths;
    }
    
    
    public static double[] makeBrownien(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        
        byte draw[] = new byte[w*h];
        
        double k, dx, dy;
        //int[] data = ((DataBufferInt)(image.getRaster().getDataBuffer())).getData();
        final byte[] data = ((DataBufferByte)(image.getRaster().getDataBuffer())).getData();
        
  //      List<Line2D> path = new ArrayList();
        double x = w*Math.random()-1, y = h*Math.random()-1;
  //      path.moveTo(x,y);
        double a, xbest=x, ybest=y, xmem = x, ymem = y, kinit, k0, x1, y1, amem=0, abest = 0;
        
        
        double sz = 1;
        
        double[] pts = new double[(int)(w*h*1.)];
        boolean transition = false;
        int nb = pts.length/2;
        for (int i=0; i<nb; i++) {
            if (i%w == 0) {
                x = w*Math.random()-1;
                y = h*Math.random()-1;
            }
      //      if (i>pts.lengthpts.length/8) {
      //          kinit = 32 + 128*smoothstep(32,192,(double)(data[(int)y*w+(int)x]&0xFF));
          //  if (i<nb/16) {
          //      kinit = 32 + (128./256.)*(double)(data[(int)y*w+(int)x]&0xFF); //smoothstep(32,192,(double)(data[(int)y*w+(int)x]&0xFF));
          //  } else 
            if (i<nb/4) {
                //draw = new byte[w*h];
                kinit = 16 + (92./256.)*(double)(data[(int)y*w+(int)x]&0xFF); //smoothstep(32,192,(double)(data[(int)y*w+(int)x]&0xFF));
            } else 
{
                if (!transition) {
                    transition = true;
             //       sz = 1;
                   // draw = new byte[w*h];
                }
                // todo vider draw[] a la transition
                kinit = 8 + (32./256.)*(double)(data[(int)y*w+(int)x]&0xFF); //smoothstep(32,192,(double)(data[(int)y*w+(int)x]&0xFF));
            }
         //   kinit = 4+(16./256.)*(double)(data[(int)y*w+(int)x]&0xFF);

            double bestScore = 5000;
            for (int j=0; j<30; j++) {
                a = ThreadLocalRandom.current().nextDouble(0,6.285);
                k = kinit; //Math.max(6, kinit + ThreadLocalRandom.current().nextGaussian()*kinit/2);

               // k0 = Math.max(8,(int)(kinit/5));
               // k = k0+(int)(k/k0)*k0;

                dx = k*Math.cos(a);
                dy = k*Math.sin(a);

                if (x+dx < 0 || x+dx >=w) dx = -dx; 
                if (y+dy < 0 || y+dy >=h) dy = -dy;

            //    a = Math.atan2(-dy, dx);
                
                x1 = Math.min(Math.max(0,x+dx),w-1);
                y1 = Math.min(Math.max(0,y+dy),h-1);
                
                
                double score = ((draw[(int)(x1/sz)+(int)(y1/sz)*(int)(w/sz)] == 1) ? 5000.:1) + (double)(data[(int)(y1)*w+(int)(x1)]&0xFF);
                score = score/3+ThreadLocalRandom.current().nextDouble(0, score*2/3); // pour permettre a tous les choix d e pouvoir gagner
                // mais en privilegiant un peu les meilleurs
             
                // On privilegie les faible changement d'angles
             //   score -= .1*getAngleDiffRad(a, amem);
                
                if (score < bestScore) {
                    bestScore = score;
                    xbest = x+dx;
                    ybest = y+dy;
                    abest = a;
                }
            }
            if (bestScore == 5000.) {
                // On est coincé, on va voir ailleur
                x = w*Math.random()-1;
                y = h*Math.random()-1;
            } else {
                x = xbest;
                y = ybest;
            }
            // AU cas où ca deborde quand meme
            x = Math.min(Math.max(0,x),w-1);
            y = Math.min(Math.max(0,y),h-1);
        
         //  path.add(new Line2D.Double(xmem,ymem, x,y));
            pts[i*2] = x;
            pts[i*2+1] = y;
            
            draw[(int)(x/sz)+((int)(y/sz))*(int)(w/sz)] = 1;
         
     //    drawLine(draw, (int)(w/sz), (int)(h/sz), (int)(xmem/sz),(int)(ymem/sz), (int)(x/sz),(int)(y/sz));
            xmem = x; ymem = y;
            amem = abest;
        }
        
        return pts;
    }
}

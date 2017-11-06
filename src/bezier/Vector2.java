/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bezier;

/**
 *
 * @author durands
 */
public class Vector2 {
    float x,y;
    
    public Vector2() {
        this(0,0);
    }
    public Vector2(float x, float y) {
        set(x,y);
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

}

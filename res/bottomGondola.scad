$fn=100;




module arc(radius, thick, angle){
	intersection(){
		union(){
			rights = floor(angle/90);
			remain = angle-rights*90;
			if(angle > 90){
				for(i = [0:rights-1]){
					rotate(i*90-(rights-1)*90/2){
						polygon([[0, 0], [radius+thick, (radius+thick)*tan(90/2)], [radius+thick, -(radius+thick)*tan(90/2)]]);
					}
				}
				rotate(-(rights)*90/2)
					polygon([[0, 0], [radius+thick, 0], [radius+thick, -(radius+thick)*tan(remain/2)]]);
				rotate((rights)*90/2)
					polygon([[0, 0], [radius+thick, (radius+thick)*tan(remain/2)], [radius+thick, 0]]);
			}else{
				polygon([[0, 0], [radius+thick, (radius+thick)*tan(angle/2)], [radius+thick, -(radius+thick)*tan(angle/2)]]);
			}
		}
		difference(){
			circle(radius+thick);
			circle(radius);
		}
	}
}



module Center() {
    difference() {
        union() {
            cylinder(r=17/2, h=20+2+5);
            translate([0,0,20+2+5]) cylinder(r1=20/2,r2=15/2, h=10);
            cylinder(r=21/2, h=5);
            cylinder(r=35/2, h=3);
            difference() {
                union() {
                    translate([-17.5,42,0]) cube([35,3,12]);
                    translate([-17.5,0,0]) cube([35,42,3]);
                }
                union() {
                    translate([-11.25,0,-1]) cube([22.5,60,20]);
                    translate([-13.65,50,6]) rotate(90,[1,0,0]) cylinder(r=.6,h=10);
                    translate([13.65,50,6]) rotate(90,[1,0,0]) cylinder(r=.6,h=10);
                }
            }
          //  translate([-21,0,0]) cube([42,42,3]);
        }
        union() {
            
             translate([0,0,10]) linear_extrude(h=10) 
                for(i=[0:60:360]) { rotate(i) translate([4.,0]) arc(1, 20, 25); } 
         //   translate([0,0,5+10+10]) cube([30,1,35], center=true);
         //   rotate(60) translate([0,0,5+10+10]) cube([30,1,35], center=true);
         //   rotate(-60) translate([0,0,5+10+10]) cube([30,1,35], center=true);
            
            translate([0,0,-1]) cylinder(r=13.5/2, h=52);
        }
    }
    
}



Center();




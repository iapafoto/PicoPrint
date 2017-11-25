$fn = 100;
distanceMotors = 200;

module nema17(withSquare, withFix, withAxis, withBigAxis) {
    width = 42.3; // mm
    fix = 31;
    rFix = 3/2;
    rAxis2 = 22/2;
    rAxis = 5/2;
    
    if (withSquare) {
        square([width,width], center = true);
    }
    
    if (withAxis) {
        circle(r=rAxis);
    }

    if (withBigAxis) {
        circle(r=rAxis2);
    }
    
    if (withFix) {
        translate([fix/2,fix/2]) circle(r=rFix);
        translate([fix/2,-fix/2]) circle(r=rFix);
        translate([-fix/2,fix/2]) circle(r=rFix);
        translate([-fix/2,-fix/2]) circle(r=rFix);
    }
}

module motors(dist, withSquare, withFix, withAxis, withBigAxis) {
    translate([-dist/2,0]) nema17(withSquare, withFix, withAxis, withBigAxis);
    translate([dist/2,0])  nema17(withSquare, withFix, withAxis, withBigAxis);
}

module layerSquare(dist) {
    motors(dist, true, false, false, false);
}
module layerFix(dist) {
    motors(dist, false, true, false, true);
    
    translate([650/2,0]) circle(r=1);
    translate([-650/2,0]) circle(r=1);
    translate([770/2,20]) circle(r=1);
    translate([-770/2,20]) circle(r=1);
    translate([33,0]) circle(r=2);
    translate([-33,0]) circle(r=2);
}

difference() {
    translate([0,0,5]) cube([800,60,10], center = true);
    union() {
        translate([0,0,-5]) linear_extrude(20) layerFix(distanceMotors);
        translate([0,0,7]) linear_extrude(20) layerSquare(distanceMotors);
    }
}



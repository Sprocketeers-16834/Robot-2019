package org.firstinspires.ftc.teamcode.Subsystems;

import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class Grabber {
    private Servo grabber;
    private ColorSensor color;

    public Grabber(HardwareMap hwMap) {
        grabber = hwMap.get(Servo.class, "grabber");
        color = hwMap.get(ColorSensor.class, "color");
    }

    public int getAlpha() {
        return color.alpha();
    }

    public void down() {
        grabber.setPosition(0.6);
    }

    public void up() {
        grabber.setPosition(0.05);
    }


}
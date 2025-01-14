package org.firstinspires.ftc.teamcode.Subsystems;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class HolonomicDrive {
    private DcMotor drive1;
    private DcMotor drive2;
    private DcMotor drive3;
    private DcMotor drive4;
    private BNO055IMU imu;
    private DistanceSensor backDist;
    private DistanceSensor grabberDist;

    private Orientation lastAngle = new Orientation();
    double globalAngle;

    Double width = 16.0;
    Integer cpr = 28;
    Integer gearratio = 40;
    Double diameter = 4.125;
    Double cpi = (cpr * gearratio) / (Math.PI * diameter); //counts per inch, 28cpr * gear ratio / (2 * pi * diameter (in inches, in the center))
    Double bias = 0.714;

    Double conversion = cpi * bias;
    Boolean exit = false;

    public void init(HardwareMap hwMap) {
        drive1 = hwMap.get(DcMotor.class, "drive1");
        drive1.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        drive2 = hwMap.get(DcMotor.class, "drive2");
        drive2.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        drive2.setDirection(DcMotor.Direction.REVERSE);

        drive3 = hwMap.get(DcMotor.class, "drive3");
        drive3.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        drive3.setDirection(DcMotor.Direction.REVERSE);

        drive4 = hwMap.get(DcMotor.class, "drive4");
        drive4.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.loggingEnabled = true;
        parameters.loggingTag = "IMU";
        parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();

        imu = hwMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);

        grabberDist = hwMap.get(DistanceSensor.class, "grabberDist");
        backDist = hwMap.get(DistanceSensor.class, "backDist");
    }

    public void drive(double x, double y) {
        if(Math.abs(x)>0.1 || Math.abs(y)>0.1) {
            double power1 = y - x;
            double power2 = y + x;
            double power3 = y - x;
            double power4 = y + x;

            drive1.setPower(-1.5*power1);
            drive2.setPower(1.5*power2);
            drive3.setPower(-1.5*power3);
            drive4.setPower(1.5*power4);
        } else {
            drive1.setPower(0);
            drive2.setPower(0);
            drive3.setPower(0);
            drive4.setPower(0);
        }
    }

    public void spin(double turn) {
        turn *= 0.8;
        if(Math.abs(turn)>0.1) {
            drive1.setPower(-turn);
            drive2.setPower(turn);
            drive3.setPower(turn);
            drive4.setPower(-turn);
        }
        else {
            drive1.setPower(0);
            drive2.setPower(0);
            drive3.setPower(0);
            drive4.setPower(0);
        }
    }

    public double getGrabDistance() {
        return grabberDist.getDistance(DistanceUnit.INCH);
    }

    public double getbackDistance() {
        return backDist.getDistance(DistanceUnit.INCH);
    }

    //Autonomous methods
    public void resetEncoders() {
        drive2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        drive1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        drive3.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        drive4.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    }

    public void moveToPosition(double inches, double speed) {
        resetEncoders();
        int move = (int) (Math.round(inches * conversion));

        drive1.setTargetPosition(drive1.getCurrentPosition() + move);
        drive2.setTargetPosition(drive2.getCurrentPosition() + move);
        drive3.setTargetPosition(drive3.getCurrentPosition() + move);
        drive4.setTargetPosition(drive4.getCurrentPosition() + move);

        drive1.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        drive2.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        drive3.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        drive4.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        drive1.setPower(speed);
        drive2.setPower(speed);
        drive3.setPower(speed);
        drive4.setPower(speed);

        while (drive2.isBusy() && drive1.isBusy() && drive3.isBusy() && drive4.isBusy()) {
            if (exit) {
                drive1.setPower(0);
                drive2.setPower(0);
                drive4.setPower(0);
                drive3.setPower(0);
                return;
            }
        }
        drive1.setPower(0);
        drive2.setPower(0);
        drive4.setPower(0);
        drive3.setPower(0);
        return;
    }


    public void strafeToPosition(double inches, double speed){
        resetEncoders();

        int move = (int)(Math.round(inches * conversion));

        drive1.setTargetPosition(-(drive1.getCurrentPosition() + move));
        drive2.setTargetPosition((drive2.getCurrentPosition() + move));
        drive3.setTargetPosition(-(drive3.getCurrentPosition() + move));
        drive4.setTargetPosition((drive4.getCurrentPosition() + move));

        drive1.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        drive2.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        drive3.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        drive4.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        drive1.setPower(speed);
        drive2.setPower(speed);
        drive3.setPower(0.9*speed);
        drive4.setPower(0.9*speed);

        while (drive2.isBusy() && drive1.isBusy() && drive3.isBusy() && drive4.isBusy()){}
        drive1.setPower(0);
        drive2.setPower(0);
        drive3.setPower(0);
        drive4.setPower(0);
        return;
    }

    private void resetAngle()
    {
        lastAngle = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        globalAngle = 0;
    }

    private double getAngle() {
        Orientation angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

        double deltaAngle = angles.firstAngle - lastAngle.firstAngle;

        if (deltaAngle < -180)
            deltaAngle += 360;
        else if (deltaAngle > 180)
            deltaAngle -= 360;

        globalAngle += deltaAngle;

        lastAngle = angles;

        return globalAngle;
    }

    public void turnWithGyro(double degrees, double power) {
        double  power12, power34;
        resetAngle();

        if (degrees < 0)
        {   // turn right.
            power12 = -power;
            power34 = power;
        }
        else if (degrees > 0)
        {   // turn left.
            power12 = power;
            power34 = -power;
        }
        else return;

        // set power to rotate.
        drive2.setPower(power12);
        drive3.setPower(power34);
        drive1.setPower(power12);
        drive4.setPower(power34);

        // rotate until turn is completed.
        if (degrees < 0)
        {
            // On right turn we have to get off zero first.
            while (getAngle() == 0) {}

            while (getAngle() > degrees) {}
        }
        else    // left turn.
            while (getAngle() < degrees) {}

        // turn the motors off.
        drive2.setPower(0);
        drive3.setPower(0);
        drive1.setPower(0);
        drive4.setPower(0);

        // reset angle tracking on new heading.
        resetAngle();
    }

//    public void turnWithGyro(double degrees, double speedDirection){
//        resetEncoders();
//        angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
//        double yaw = -angles.firstAngle;//make this negative
//
//        double first;
//        double second;
//
//        if (speedDirection > 0){
//            if (degrees > 10){
//                first = (degrees - 10) + devertify(yaw);
//                second = degrees + devertify(yaw);
//            }else{
//                first = devertify(yaw);
//                second = degrees + devertify(yaw);
//            }
//        }else{
//            if (degrees > 10){
//                first = devertify(-(degrees - 10) + devertify(yaw));
//                second = devertify(-degrees + devertify(yaw));
//            }else{
//                first = devertify(yaw);
//                second = devertify(-degrees + devertify(yaw));
//            }
//        }
//        Double firsta = convertify(first - 5);//175
//        Double firstb = convertify(first + 5);//-175
//
//        turnWithEncoder(speedDirection);
//
//        if (Math.abs(firsta - firstb) < 11) {
//            while (!(firsta < yaw && yaw < firstb)) {//within range?
//                angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
//                gravity = imu.getGravity();
//                yaw = -angles.firstAngle;
//            }
//        }else{
//            //
//            while (!((firsta < yaw && yaw < 180) || (-180 < yaw && yaw < firstb))) {//within range?
//                angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
//                gravity = imu.getGravity();
//                yaw = -angles.firstAngle;
//            }
//        }
//
//        Double seconda = convertify(second - 5);//175
//        Double secondb = convertify(second + 5);//-175
//
//        turnWithEncoder(speedDirection / 3);
//
//        if (Math.abs(seconda - secondb) < 11) {
//            while (!(seconda < yaw && yaw < secondb)) {//within range?
//                angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
//                gravity = imu.getGravity();
//                yaw = -angles.firstAngle;
//            }
//            while (!((seconda < yaw && yaw < 180) || (-180 < yaw && yaw < secondb))) {//within range?
//                angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
//                gravity = imu.getGravity();
//                yaw = -angles.firstAngle;
//            }
//            drive2.setPower(0);
//            drive1.setPower(0);
//            drive3.setPower(0);
//            drive4.setPower(0);
//        }
//
//        resetEncoders();
//
//        drive2.setMode(DcMotor.RunMode.RUN_TO_POSITION);
//        drive1.setMode(DcMotor.RunMode.RUN_TO_POSITION);
//        drive3.setMode(DcMotor.RunMode.RUN_TO_POSITION);
//        drive4.setMode(DcMotor.RunMode.RUN_TO_POSITION);
//    }

    public double devertify(double degrees){
        if (degrees < 0){
            degrees = degrees + 360;
        }
        return degrees;
    }
    public double convertify(double degrees){
        if (degrees > 179){
            degrees = -(360 - degrees);
        } else if(degrees < -180){
            degrees = 360 + degrees;
        } else if(degrees > 360){
            degrees = degrees - 360;
        }
        return degrees;
    }

    public void turnWithEncoder(double input){
        drive2.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        drive3.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        drive1.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        drive4.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        //
        drive2.setPower(-input);
        drive3.setPower(input);
        drive1.setPower(-input);
        drive4.setPower(input);
    }
}

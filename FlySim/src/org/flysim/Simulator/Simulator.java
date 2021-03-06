package org.flysim.Simulator;

public class Simulator implements Runnable, FlyControlInterface, FlySensorsInterface {
	
	private static final double RECALC_PERIOD = 0.05;
	//Constants
	public double mass = 1.0;	//kg
	public double g = 9.8;		//m/s^2
	public double kar = 0.5;	//Air resistance coefficient
	
	//Situation
	public Vector position = new Vector (0,0,0.1);		//m, absolute
	public Vector velocity = new Vector (0,0,0);		//m/s, relative
	public Vector accelerometer = new Vector (0,0,1);	//gravity vector, in g
	public Vector gyroscope = new Vector (0,0,0);		//x - pitch, y - roll, z - yaw; radians
	public Vector compass = new Vector (0,1,0);			//magnetic field vector, default forward = north
	public double usForward;	//m, ultrasonic sensor
	public double usBackward;	//m
	public double usLeft;		//m
	public double usRight;		//m
	public double usBottom;		//m - height eq
	
	public double roll = 0;		// horizontal, roll<0 - tilt to left, roll>0 - tilt to right. -90 .. +90
	public double pitch = 0;	// horizontal, pitch<0 - tilt to back, pitch>0 - tilt to forward. -90 .. +90
	public double yaw = 0;		// head to North. 0 - North, 90 - East, 180 - South, 270 - West
	
	public double time;
	private double lastTimeCalc;
	private double startTime;
	
	//Control Data
	public double throttle = 50;		//0% (down) - 50% (stay) - 100% (up)
	public double elevator = 50;		//0% (backward) - 50% (stay) - 100% (forward)
	public double aileron = 50;			//0% (left) - 50% (stay) - 100% (right)
	public double rudder = 50;			//0% (CCW rotation) - 50% (no rotation) - 100%(CW rotation)

	public Simulator() {
		getTime();
		lastTimeCalc = time;
		startTime = time;
	}

	public void run() {
		while (true) {
			getTime();
			getControlData();
			calculateSituation();
		}
	}
	
	public void getTime() {
		time = System.currentTimeMillis()/1000.0;
		//System.out.printf("%f\n",time);
	}
	
	public void getControlData() {
		throttle = (throttle<0)?0:((throttle>100)?100:throttle);
		elevator = (elevator<0)?0:((elevator>100)?100:elevator);
		aileron = (aileron<0)?0:((aileron>100)?100:aileron);
		rudder = (rudder<0)?0:((rudder>100)?100:rudder);
		//Nothing to do yet
	}
	
//			 y
//	         ^  forward
//           |
//	         |
//	left     |     right
//	---------+-------->x
//	         |
//	         |
//	         |
//	         |  backward
	
	public void calculateSituation() {
		double dT = time - lastTimeCalc;
		if (dT >= RECALC_PERIOD) {
			Vector f = new Vector((aileron - 50) / 50, (elevator - 50) / 50, ((throttle) / 50) * g);
			
			//(F_air_resistance) Far = -kr * V^2 - Assume, that air resistance proportional to square of velocity
			double vabs = velocity.abs();
			Vector far = velocity.norm().mul(-kar * vabs * vabs);
			
			//F = m*a -> a = F/m
			//V = V0 + a*dt
			Vector a = f.plus(far).mul(1/mass).plus(new Vector(0,0,-g)); //a = (F + Far)/m - g
			
			accelerometer = a.mul(1/g); //Simulate accelerometer

			System.out.printf("T = %f, dT = %f, coord(%s), veloc(%s), accel(%s)\n", getSystemTime(), dT, position, velocity, a);
			
			velocity = velocity.plus(a.mul(dT)); //V = V0 + a*dt
			position = position.plus(velocity.mul(dT)); //x = x0 + V*dt
			if (position.z<0) {
				position.z=0; //stay on floor
			}
			
			lastTimeCalc = time;
		} else {
			try {
				long sleeptime = (long) ((RECALC_PERIOD-dT)*1000);
				//System.out.printf("Sim sleep to %d ms\n", sleeptime);
				Thread.sleep(sleeptime);
				//System.out.printf("Sim wake up\n", sleeptime);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public synchronized Vector getAccelerometer() {
		return accelerometer;
	}

	public synchronized Vector getGyroscope() {
		return gyroscope;
	}

	public synchronized double getUsBottom() {
		return usBottom;
	}

	public synchronized double getUsForward() {
		return usForward;
	}

	public synchronized double getUsBackward() {
		return usBackward;
	}

	public synchronized double getUsLeft() {
		return usLeft;
	}

	public synchronized double getUsRight() {
		return usRight;
	}

	public synchronized void setThrottle(double value) {
		throttle = value;
	}

	public synchronized void setElevator(double value) {
		elevator = value;
	}

	public synchronized void setAileron(double value) {
		aileron = value;
	}

	public synchronized void setRudder(double value) {
		rudder = value;
	}

	public double getSystemTime() {
		return lastTimeCalc - startTime;
	}
	
}

package eu.erasmus.intelligentSystems.search.TunableBot;

import robocode.*;
import java.awt.*;
import java.io.*;
import java.util.Random;
 
/**
 * SuperTracker's code from http://robowiki.net/wiki/SuperTracker with changes that allows variable values of desired parameters and loading them from file
 * SuperTracker - a Super Sample Robot by CrazyBassoonist based on the robot Tracker by Mathew Nelson and maintained by Flemming N. Larsen
 * <p/>
 * Locks onto a robot, moves close, fires when close.
 */
public class TunableTracker extends AdvancedRobot {
	Random generator = new Random(0);
	int moveDirection=1;//which way to move
	private double _closeDistance; // Distance to consider enemy is close
	private double _changeSpeedProbability; //Probability to change the speed
	private double _minimumSpeed; // Minimum robot speed
	private double _speedRange; // Range of possible robot speeds
	private String _configFileName = "config.data"; /* File from which properties of the individual robot are loaded from, 
											 *same as the one fitness function writes to	structure of the file is one double value per line in this order
											 * minSpeed, speedRange, closeDistance, changeSpeedProbability
											 */
	/**
	 * run:  Tracker's main run function
	 */
	public void run() {
		loadConfiguration();
		setAdjustRadarForRobotTurn(true); //keep the radar still while we turn
		setBodyColor(new Color(128, 128, 50));
		setGunColor(new Color(50, 50, 20));
		setRadarColor(new Color(200, 200, 70));
		setScanColor(Color.white);
		setBulletColor(Color.blue);
		setAdjustGunForRobotTurn(true); // Keep the gun still when we turn
		turnRadarRightRadians(Double.POSITIVE_INFINITY); //keep turning radar right
	}
	// Return if configuration was loaded properly
	private boolean loadConfiguration() { 		
		try (BufferedReader reader = new BufferedReader(new FileReader(getDataFile(_configFileName)))){
			_minimumSpeed = Double.parseDouble(reader.readLine());
			_speedRange = Double.parseDouble(reader.readLine());
			_closeDistance = Double.parseDouble(reader.readLine());
			_changeSpeedProbability = Double.parseDouble(reader.readLine());
			reader.close();
			return true;
		} catch (Exception e) {
			return false;
		}/**/
	}
 
	/**
	 * onScannedRobot:  Here's the good stuff
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		double absBearing=e.getBearingRadians()+getHeadingRadians();//enemies absolute bearing
		double latVel=e.getVelocity() * Math.sin(e.getHeadingRadians() -absBearing);//enemies later velocity
		double gunTurnAmt;//amount to turn our gun
		setTurnRadarLeftRadians(getRadarTurnRemainingRadians());//lock on the radar
		if(generator.nextDouble()> 1 - _changeSpeedProbability){
			setMaxVelocity((_speedRange*generator.nextDouble())+_minimumSpeed);//randomly change speed /
		}
		if (e.getDistance() > _closeDistance) { //if distance is greater than
			gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing- getGunHeadingRadians()+latVel/22);//amount to turn our gun, lead just a little bit
			setTurnGunRightRadians(gunTurnAmt); //turn our gun
			setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(absBearing-getHeadingRadians()+latVel/getVelocity()));//drive towards the enemies predicted future location
			setAhead((e.getDistance() - 140)*moveDirection);//move forward
			setFire(3);//fire
		}
		else{//if we are close enough...
			gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing- getGunHeadingRadians()+latVel/15);//amount to turn our gun, lead just a little bit
			setTurnGunRightRadians(gunTurnAmt);//turn our gun
			setTurnLeft(-90-e.getBearing()); //turn perpendicular to the enemy
			setAhead((e.getDistance() - 140)*moveDirection);//move forward
			setFire(3);//fire
		}	
	}
 
	public void onHitWall(HitWallEvent e){
		moveDirection=-moveDirection;//reverse direction upon hitting a wall
	}
 
	/**
	 * onWin:  Do a victory dance
	 */
	public void onWin(WinEvent e) {
		for (int i = 0; i < 50; i++) {
			turnRight(30);
			turnLeft(30);
		}
	}
}

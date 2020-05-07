package strategies;

import automail.ReportDelivery;
import automail.Robot;
import exceptions.BreakingFragileItemException;
import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;
import exceptions.NonFragileItemException;

import java.util.HashMap;

public class Automail {
	      
    private Robot[] robots;
    public IMailPool mailPool;
    private int numRobots;
    public static HashMap<Robot, Integer> Occupied=new HashMap<>();

    
    public Automail(IMailPool mailPool, ReportDelivery delivery, int numRobots, boolean CAUTION_ENABLED) {
    	// Swap between simple provided strategies and your strategies here

    	/** Initialize the MailPool */
    	this.mailPool = mailPool;
    	
    	/** Initialize robots */
    	robots = new Robot[numRobots];
    	for (int i = 0; i < numRobots; i++) robots[i] = new Robot(delivery, mailPool, CAUTION_ENABLED);

    	this.numRobots=numRobots;
    }


   public void step() {
        try {
            // load robot with mail from the pool

            for (int i = 0; i < numRobots; i++) {
                Robot r = robots[i];
                r.step();
                if (r.current_floor == r.destination_floor && r.SpecialArm != null) {//if the robot arrives the destination floor and waits for unwarping just record it
                    unwarping.put(r, r.current_floor);
                }
                if ((r.current_state == Robot.RobotState.DELIVERING || r.current_state == Robot.RobotState.RETURNING)) {//if the robot finish unwarping and change its state just remove the record in the map
                    unwarping.remove(r);
                }
            }
        } catch (ExcessiveDeliveryException e) {
            e.printStackTrace();
            System.out.println("Simulation unable to complete.");
            System.exit(0);
        }

    }





    
}

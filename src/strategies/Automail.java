package strategies;

import automail.IMailDelivery;
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

    
    public Automail(IMailPool mailPool, IMailDelivery delivery, int numRobots) {
    	// Swap between simple provided strategies and your strategies here

    	/** Initialize the MailPool */
    	this.mailPool = mailPool;
    	
    	/** Initialize robots */
    	robots = new Robot[numRobots];
    	for (int i = 0; i < numRobots; i++) robots[i] = new Robot(delivery, mailPool);

    	this.numRobots=numRobots;
    }



    public void step(){
        try {
            // load robot with mail from the pool
            mailPool.step();

            for (int i = 0; i < numRobots; i++) {//gai
                Robot r = robots[i];
                int laststate=0;
                if (r.current_state == Robot.RobotState.UNWRAPPING) {//如果该机器人正在UNWRAPPING，就记录下当前楼层
                    Occupied.put(r,r.current_floor);
                    laststate=1;
                }
                r.step();
                if (laststate==1&&r.current_state== Robot.RobotState.RETURNING) {//如果易碎件放了，就在occupied里移除该项
                    Occupied.remove(r);
                }
            }
        } catch (ExcessiveDeliveryException | ItemTooHeavyException | BreakingFragileItemException | NonFragileItemException e) {
            e.printStackTrace();
            System.out.println("Simulation unable to complete.");
            System.exit(0);
        }

      //  mailGenerator.step();
    }





    
}

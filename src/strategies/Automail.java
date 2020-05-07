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


    public void step() throws ExcessiveDeliveryException, ItemTooHeavyException, BreakingFragileItemException, NonFragileItemException{
        try {
            // load robot with mail from the pool
            mailPool.step();

            for (int i = 0; i < numRobots; i++) {//gai
                Robot r = robots[i];
                int lastState=0;
                if (r.current_state == Robot.RobotState.UNWRAPPING) {//如果该机器人正在UNWRAPPING，就记录下当前楼层
                    Occupied.put(r, r.current_floor);
                    lastState=1;
                }
                r.step();
                if (lastState==1&&r.current_state== Robot.RobotState.RETURNING) {//如果易碎件放了，就在occupied里移除该项
                    Occupied.remove(r);
                }
            }
        } catch (ExcessiveDeliveryException e) {
            throw e;
        }

    }





    
}

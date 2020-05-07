package automail;

import exceptions.BreakingFragileItemException;
import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;
import exceptions.NonFragileItemException;
import strategies.IMailPool;

import java.util.Map;
import java.util.TreeMap;

/**
 * The robot delivers mail!
 */
public class Robot<get> {

    static public final int INDIVIDUAL_MAX_WEIGHT = 2000;

    IMailDelivery delivery;
    protected final String id;

    /**
     * Possible states the robot can be in
     */
    public enum RobotState {DELIVERING, WAITING, RETURNING, WRAPPING1, WRAPPING2, UNWRAPPING}
    public enum RobotMode {NORMAL, CAUTION}
    public RobotState current_state;
    public RobotMode current_mode;
    public int current_floor;//gai
    public int destination_floor;
    private IMailPool mailPool;
    private boolean receivedDispatch;

    private MailItem deliveryItem = null;//gai
    private MailItem tube = null;//gai
    public MailItem SpecialArm = null;//jia

    private int deliveryCounter;//gai
    private int fragileCounter;//jia

    /**
     * Initiates the robot's location at the start to be at the mailroom
     * also set it to be waiting for mail.
     *
     * @param CAUTION_ENABLED whether or not caution mode is on or not
     * @param delivery  governs the final delivery
     * @param mailPool  is the source of mail
     *
     */
    public Robot(ReportDelivery delivery, IMailPool mailPool, boolean CAUTION_ENABLED) {
        id = "R" + hashCode();
        if(CAUTION_ENABLED){
            current_mode = RobotMode.CAUTION;
        }
        else{
            current_mode = RobotMode.NORMAL;
        }
        current_state = RobotState.RETURNING;
        current_floor = Building.MAILROOM_LOCATION;
        this.delivery = delivery;
        this.mailPool = mailPool;
        this.receivedDispatch = false;
        this.deliveryCounter = 0;
        this.fragileCounter = 0;
    }

    public void dispatch() {
        receivedDispatch = true;
    }

    /**
     * This is called on every time step
     *
     * @throws ExcessiveDeliveryException if robot delivers more than the capacity of the tube without refilling
     */
    public void step() throws ExcessiveDeliveryException {
        switch (current_state) {
            /** This state is triggered when the robot is returning to the mailroom after a delivery */

            case RETURNING:
                /** If its current position is at the mailroom, then the robot should change state */
                if (current_floor == Building.MAILROOM_LOCATION) {
                    if (tube != null) {
                        mailPool.addToPool(tube);
                        System.out.printf("T: %3d >  +addToPool [%s]%n", Clock.Time(), tube.toString());
                        tube = null;
                    }
                    /** Tell the sorter the robot is ready */
                    mailPool.registerWaiting(this);
                    changeState(RobotState.WAITING);
                } else {
                    /** If the robot is not at the mailroom floor yet, then move towards it! */

                    moveTowards(Building.MAILROOM_LOCATION);
                    break;
                }

            case WAITING://gai
                /** If the StorageTube is ready and the Robot is waiting in the mailroom then start the delivery */
                if (!isEmpty() && receivedDispatch) {
                    receivedDispatch = false;
                    deliveryCounter = 0;// reset delivery counter
                   // fragileCounter = 0;
                    setRoute();
                    if (SpecialArm != null) {
                        changeState(RobotState.WRAPPING1);
                    } else {
                        changeState(RobotState.DELIVERING);
                    }
                }
                break;

            case WRAPPING1://jia
                delivery.incrementWrapTime(1);
                changeState(RobotState.WRAPPING2);
                break;

            case WRAPPING2://jia
                delivery.incrementWrapTime(1);
                changeState(RobotState.DELIVERING);
                break;

            case DELIVERING:
                if (current_floor == destination_floor) { // If already here drop off either way
                    /** Delivery complete, report this to the simulator! */
                    if (SpecialArm != null) {//gai
                        changeState(RobotState.UNWRAPPING);
                        break;
                    } else {
                        delivery.deliver(deliveryItem);
                        deliveryItem = null;
                        deliveryCounter++;
                        //deliveryWeight++;
                    }
                    if (deliveryCounter > 2) {  // Implies a simulation bug
                        throw new ExcessiveDeliveryException();
                    }
                    /** Check if want to return, i.e. if there is no item in the tube*/
                    if (tube == null) {
                        changeState(RobotState.RETURNING);
                    } else {
                        /** If there is another item, set the robot's route to the location to deliver the item */
                        deliveryItem = tube;
                        tube = null;
                        setRoute();
                        changeState(RobotState.DELIVERING);
                    }
                } else {//gai
                    /** The robot is not at the destination yet, move towards it! */
                    moveTowards(destination_floor);
                }
                break;

            case UNWRAPPING://jia
                delivery.incrementWrapTime(1);
                delivery.deliver(SpecialArm);
                SpecialArm = null;
                fragileCounter++;
              // fragileWeight++;
                if (fragileCounter > 1) {
                    throw new ExcessiveDeliveryException();
                }
                if (deliveryItem == null) {
                    changeState(RobotState.RETURNING);
                } else {
                    changeState(RobotState.DELIVERING);
                }
                break;
        }
    }

    /**
     * Sets the route for the robot
     */
    private void setRoute() {//gai
        /** Set the destination floor */
        if (SpecialArm != null) {
            destination_floor = SpecialArm.getDestFloor();
        } else {
            destination_floor = deliveryItem.getDestFloor();
        }
    }

    /**
     * Generic function that moves the robot towards the destination
     *
     * @param destination the floor towards which the robot is moving
     */

   private void moveTowards(int destination) {//the robot cannot enter the floor with an unwarping robot

        if (current_floor < destination && !Automail.unwarping.containsValue(current_floor + 1)) {
            current_floor++;
        } else if (current_floor > destination && !Automail.unwarping.containsValue(current_floor - 1)) {
            current_floor--;
        }
    }


    private String getIdTube() {
        return String.format("%s(%1d,%1d,%1d)%1d", id, (deliveryItem == null ? 0 : 1), (tube == null ? 0 : 1), (SpecialArm == null ? 0 : 1), deliveryCounter + fragileCounter);
    }

    /**
     * Prints out the change in state
     *
     * @param nextState the state to which the robot is transitioning
     */
    private void changeState(RobotState nextState) {
        // assert (!(deliveryItem == null && tube != null));
        if (current_state != nextState) {
            System.out.printf("T: %3d > %7s changed from %s to %s%n", Clock.Time(), getIdTube(), current_state, nextState);
        }
        current_state = nextState;
        if (nextState == RobotState.DELIVERING && SpecialArm != null) {
            System.out.printf("T: %3d > %9s-> [%s]%n", Clock.Time(), getIdTube(), SpecialArm.toString());
        } else if (nextState == RobotState.DELIVERING && deliveryItem != null) {
            System.out.printf("T: %3d > %9s-> [%s]%n", Clock.Time(), getIdTube(), deliveryItem.toString());
        }
    }

    public MailItem getTube() {
        return tube;
    }


    static private int count = 0;
    static private Map<Integer, Integer> hashMap = new TreeMap<Integer, Integer>();

    @Override
    public int hashCode() {
        Integer hash0 = super.hashCode();
        Integer hash = hashMap.get(hash0);
        if (hash == null) {
            hash = count++;
            hashMap.put(hash0, hash);
        }
        return hash;
    }

    public boolean isEmpty() {//gai
        return (deliveryItem == null && tube == null && SpecialArm == null);
    }

    public void addToHand(MailItem mailItem) throws ItemTooHeavyException, BreakingFragileItemException {
        assert (deliveryItem == null);
        if (mailItem.fragile) throw new BreakingFragileItemException();
        deliveryItem = mailItem;
        if (deliveryItem.weight > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
    }

    public void addToTube(MailItem mailItem) throws ItemTooHeavyException, BreakingFragileItemException {
        assert (tube == null);
        if (mailItem.fragile) throw new BreakingFragileItemException();
        tube = mailItem;
        if (tube.weight > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
    }

    public void addToSpecial(MailItem mailItem) throws ItemTooHeavyException, NonFragileItemException, BreakingFragileItemException{//jia
        assert (SpecialArm == null);
        if (!mailItem.fragile) throw new NonFragileItemException();
        SpecialArm = mailItem;
        if (SpecialArm.weight > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
        if (current_mode!=RobotMode.CAUTION) throw new BreakingFragileItemException();
    }

}

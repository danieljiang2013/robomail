package strategies;

import java.util.LinkedList;
import java.util.Comparator;
import java.util.ListIterator;

import automail.MailItem;
import automail.Robot;
import exceptions.BreakingFragileItemException;
import exceptions.ItemTooHeavyException;
import exceptions.NonFragileItemException;

public class MailPool implements IMailPool {

    private class Item {
        int destination;
        MailItem mailItem;

        public Item(MailItem mailItem) {
            destination = mailItem.getDestFloor();
            this.mailItem = mailItem;
        }
    }

    public class ItemComparator implements Comparator<Item> {
        @Override
        public int compare(Item i1, Item i2) {
            int order = 0;
            if (i1.destination > i2.destination) {  // Further before closer
                order = 1;
            } else if (i1.destination < i2.destination) {
                order = -1;
            }
            return order;
        }
    }

    private LinkedList<Item> pool;
    private LinkedList<Robot> robots;

    public MailPool() {
        // Start empty
        pool = new LinkedList<Item>();
        robots = new LinkedList<Robot>();
    }

    public void addToPool(MailItem mailItem) {
        Item item = new Item(mailItem);
        pool.add(item);
        pool.sort(new ItemComparator());
    }

    @Override
    public void step() throws ItemTooHeavyException, BreakingFragileItemException, NonFragileItemException {//gai
        try {
            ListIterator<Robot> i = robots.listIterator();
            while (i.hasNext()) loadRobot(i);
        } catch (Exception e) {
            throw e;
        }
    }

    private void loadRobot(ListIterator<Robot> i) throws ItemTooHeavyException, BreakingFragileItemException, NonFragileItemException {//gai
        Robot robot = i.next();
        assert (robot.isEmpty());
        // System.out.printf("P: %3d%n", pool.size());
        ListIterator<Item> j = pool.listIterator();
        if (pool.size() > 0) {
            try {//gai(如果m1易碎直接走，如果m1正常就m2易碎 正常就运)
                MailItem m1 = j.next().mailItem;
                if (!m1.fragile) {//m1正常的情况
                    robot.addToHand(m1);
                    j.remove();
                    if (pool.size() > 0) {
                        MailItem m2 = j.next().mailItem;
                        if (!m2.fragile) {//m2正常加到tube
                            robot.addToTube(m2);
                            j.remove();

                        } else {//m2易碎加到special
                            robot.addToSpecial(m2);
                            j.remove();
                        }
                    }
                }
                else {//m1易碎的情况
                    robot.addToSpecial(m1);
                    j.remove();
                }
                robot.dispatch(); // send the robot off if it has any items to deliver
                i.remove();       // remove from mailPool queue
            } catch (Exception e) {
                throw e;
            }
        }

    }

    @Override
    public void registerWaiting(Robot robot) { // assumes won't be there already
        robots.add(robot);
    }

}

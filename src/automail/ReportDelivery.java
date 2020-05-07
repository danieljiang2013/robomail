package automail;

import exceptions.MailAlreadyDeliveredException;

import java.util.ArrayList;

public class ReportDelivery implements IMailDelivery {

    private int normalPackages = 0;
    private int cautionPackages = 0;
    private int normalWeight = 0;
    private int cautionWeight = 0;
    private int time_wrap_unwrap = 0;
    private double total_score = 0;

    private ArrayList<MailItem> MAIL_DELIVERED;

    public ReportDelivery(){
        MAIL_DELIVERED = new ArrayList<MailItem>();
    }

    public double getTotalScore(){
        return total_score;
    }

    public double getNormalPackages(){
        return normalPackages;
    }

    public double getCautionPackages(){
        return cautionPackages;
    }

    public double getNormalWeight(){
        return normalWeight;
    }

    public double getCautionWeight(){
        return cautionWeight;
    }

    public double getWrapTime(){
        return time_wrap_unwrap;
    }


    public ArrayList<MailItem> getMailDelivered(){
        return MAIL_DELIVERED;
    }

    /**
     * Confirm the delivery and calculate the total score
     */
    public void deliver(MailItem deliveryItem) {
        if (!MAIL_DELIVERED.contains(deliveryItem)) {
            MAIL_DELIVERED.add(deliveryItem);
            System.out.printf("T: %3d > Deliv(%4d) [%s]%n", Clock.Time(), MAIL_DELIVERED.size(), deliveryItem.toString());
            // Calculate delivery score
            total_score += calculateDeliveryScore(deliveryItem);
            if (!deliveryItem.fragile) {
                normalPackages++;
                normalWeight += deliveryItem.weight;
            } else {
                time_wrap_unwrap += 3;
                cautionPackages++;
                cautionWeight += deliveryItem.weight;
            }
        } else {
            try {
                throw new MailAlreadyDeliveredException();
            } catch (MailAlreadyDeliveredException e) {
                e.printStackTrace();
            }
        }
    }

    private static double calculateDeliveryScore(MailItem deliveryItem) {
        // Penalty for longer delivery times
        final double penalty = 1.2;
        double priority_weight = 0;
        return Math.pow(Clock.Time() - deliveryItem.getArrivalTime(), penalty) * (1 + Math.sqrt(priority_weight));
    }

}

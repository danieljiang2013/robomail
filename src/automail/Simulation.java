package automail;

import exceptions.*;
import strategies.Automail;
import strategies.IMailPool;
import strategies.MailPool;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

/**
 * This class simulates the behaviour of AutoMail
 */
public class Simulation {
    /**
     * Constant for the mail generator
     */
    private static int MAIL_TO_CREATE;
    private static int MAIL_MAX_WEIGHT;
    private static boolean CAUTION_ENABLED;
    private static boolean FRAGILE_ENABLED;
    private static boolean STATISTICS_ENABLED;

    public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, NonFragileItemException {

        Properties automailProperties = new Properties();
        // Default properties
        automailProperties.setProperty("Robots", "Standard");
        automailProperties.setProperty("MailPool", "strategies.SimpleMailPool");
        automailProperties.setProperty("Floors", "10");
        automailProperties.setProperty("Mail_to_Create", "80");
        automailProperties.setProperty("Last_Delivery_Time", "100");
        automailProperties.setProperty("Caution", "false");
        automailProperties.setProperty("Fragile", "false");
        automailProperties.setProperty("Statistics", "false");

        // Read properties
        FileReader inStream = null;
        try {
            inStream = new FileReader("automail.properties");
            automailProperties.load(inStream);
        } finally {
            if (inStream != null) {
                inStream.close();
            }
        }

        //Seed
        String seedProp = automailProperties.getProperty("Seed");
        // Floors
        Building.FLOORS = Integer.parseInt(automailProperties.getProperty("Floors"));
        System.out.println("Floors: " + Building.FLOORS);
        // Mail_to_Create
        MAIL_TO_CREATE = Integer.parseInt(automailProperties.getProperty("Mail_to_Create"));
        System.out.println("Mail_to_Create: " + MAIL_TO_CREATE);
        // Mail_to_Create
        MAIL_MAX_WEIGHT = Integer.parseInt(automailProperties.getProperty("Mail_Max_Weight"));
        System.out.println("Mail_Max_Weight: " + MAIL_MAX_WEIGHT);
        // Last_Delivery_Time
        Clock.LAST_DELIVERY_TIME = Integer.parseInt(automailProperties.getProperty("Last_Delivery_Time"));
        System.out.println("Last_Delivery_Time: " + Clock.LAST_DELIVERY_TIME);
        // Caution ability
        CAUTION_ENABLED = Boolean.parseBoolean(automailProperties.getProperty("Caution"));
        System.out.println("Caution enabled: " + CAUTION_ENABLED);
        // Fragile mail generation
        FRAGILE_ENABLED = Boolean.parseBoolean(automailProperties.getProperty("Fragile"));
        System.out.println("Fragile enabled: " + FRAGILE_ENABLED);
        // Statistics tracking
        STATISTICS_ENABLED = Boolean.parseBoolean(automailProperties.getProperty("Statistics"));
        System.out.println("Statistics enabled: " + STATISTICS_ENABLED);
        // Robots
        int numRobots = Integer.parseInt(automailProperties.getProperty("Robots"));
        System.out.print("Robots: ");
        System.out.println(numRobots);
        assert (numRobots > 0);
        // MailPool
        IMailPool mailPool = new MailPool();

        // End properties


        /** Used to see whether a seed is initialized or not */
        HashMap<Boolean, Integer> seedMap = new HashMap<>();

        /** Read the first argument and save it as a seed if it exists */
        if (args.length == 0) { // No arg
            if (seedProp == null) { // and no property
                seedMap.put(false, 0); // so randomise
            } else { // Use property seed
                seedMap.put(true, Integer.parseInt(seedProp));
            }
        } else { // Use arg seed - overrides property
            seedMap.put(true, Integer.parseInt(args[0]));
        }
        Integer seed = seedMap.get(true);
        System.out.println("Seed: " + (seed == null ? "null" : seed.toString()));


        ReportDelivery deliveryStats = new ReportDelivery();

        Automail automail = new Automail(mailPool, deliveryStats, numRobots, CAUTION_ENABLED);
        MailGenerator mailGenerator = new MailGenerator(MAIL_TO_CREATE, MAIL_MAX_WEIGHT, automail.mailPool, seedMap);

        /** Initiate all the mail */
        mailGenerator.generateAllMail(FRAGILE_ENABLED);


        while (deliveryStats.getMailDelivered().size() != mailGenerator.MAIL_TO_CREATE) {
            //add mail items to pool
            mailGenerator.step();
            try {
                //load robots
                automail.mailPool.step();
                //deliver the mail items
                automail.step();
            }catch(ItemTooHeavyException|BreakingFragileItemException e){
                e.printStackTrace();
                System.out.println("Simulation unable to complete.");
                System.exit(0);
            }

            Clock.Tick();
        }
        printResults(deliveryStats);
    }



    public static void printResults(ReportDelivery deliveryStats) {
        if (STATISTICS_ENABLED) {
            System.out.println("T: " + Clock.Time() + " | Simulation complete!");
            System.out.println("The number of package delivered normally " + deliveryStats.getNormalPackages());
            System.out.println("The number of package delivered using caution " + deliveryStats.getCautionPackages());
            System.out.println("The total weight of package delivered normally " + deliveryStats.getNormalWeight());
            System.out.println("The total weight of package delivered using caution " + deliveryStats.getCautionWeight());
            System.out.println("Total amount of time on wrapping and unwrapping is " + deliveryStats.getWrapTime());
            System.out.println("Final Delivery time: " + Clock.Time());
            System.out.printf("Final Score: %.2f%n", deliveryStats.getTotalScore());
        }

        else {
            System.out.println("T: " + Clock.Time() + " | Simulation complete!");
            System.out.println("Final Delivery time: " + Clock.Time());
            System.out.printf("Final Score: %.2f%n", deliveryStats.getTotalScore());
        }
    }


}

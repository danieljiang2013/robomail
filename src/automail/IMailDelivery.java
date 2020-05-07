package automail;

/**
 * a MailDelivery is used by the Robot to deliver mail once it has arrived at the correct location
 */
public interface IMailDelivery {

	/**
     * Delivers an item at its floor
     * @param mailItem the mail item being delivered.
     */
	void deliver(MailItem mailItem);

	/**
	 * increments the time spent wrapping and unwrapping
	 * @param time the amount of time to increment by
	 */
	void incrementWrapTime(int time);


}
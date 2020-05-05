package exceptions;

/**
 * An exception throws when robot using special arm to handle normal item
 */
public class NonFragileItemException extends Exception {//jia

    public NonFragileItemException() {
        super("using special arm to handle normal item!!");
    }

}

package net.cb.cb.library.utils.encrypt;

/**
 * Author : Jimmy.Shine Date : 2014-05-31
 * <p>
 * Crypto exception
 * </p>
 */
public class CryptoException extends Exception {

    private static final long serialVersionUID = -3751365563227429807L;

    public CryptoException(){

    }

    public CryptoException(String message){
        super(message);
    }

    public CryptoException(Exception e){
        super(e);
    }

}

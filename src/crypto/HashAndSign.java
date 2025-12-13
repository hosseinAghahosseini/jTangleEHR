/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package crypto;

import java.security.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;

/**
 * @author hosseinAghahosseini
 */

public class HashAndSign {
    //Applies Sha256 to a string and returns the result. 
    public static String applySha256(String input)
    {		
        try 
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");	        
            //Applies sha256 to our input, 
            byte[] hash = digest.digest(input.getBytes("UTF-8"));	        
            StringBuffer hexString = new StringBuffer(); // This will contain hash as hexidecimal
            for (int i = 0; i < hash.length; i++) 
            {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }	
    
    //Applies ECDSA Signature and returns the result ( as bytes ).
    //applyECDSASig takes in the senders private key and string input, signs it and returns an array of bytes.
    public static byte[] applyECDSASig(PrivateKey privateKey, String input) 
    {
        Signature dsa;
        byte[] output = new byte[0];
        try 
        {
            dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(privateKey);
            byte[] strByte = input.getBytes();
            dsa.update(strByte);
            byte[] realSig = dsa.sign();
            output = realSig;
        } 
        catch (Exception e) 
        {
            throw new RuntimeException(e);
        }
        return output;
    }
	
    //Verifies a String signature 
    //verifyECDSASig takes in the signature, public key and string data and returns true or false if the signature is valid.
    public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) 
    {
        try 
        {
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());
            return ecdsaVerify.verify(signature);
        }
        catch(Exception e) 
        {
            throw new RuntimeException(e);
        }
    }

    //getStringFromKey returns encoded string from any key.
    public static String getStringFromKey(Key key)
    {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
    
    //creates string with zeros equal to difficulty
    public static String getDificultyString(int difficulty)
    {
        String temp = "";
        for(int i = 0 ; i < difficulty ; i++)
        {
            temp += "0";
        }
        return temp;
    }
}
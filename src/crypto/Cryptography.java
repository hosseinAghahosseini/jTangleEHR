/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package crypto;

import entities.Node;
import static entities.Node.difficulty;
import crypto.HashAndSign;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author hosseinAghahosseini
 */

public class Cryptography {
   
        
    public static String createHashInput(Node N)
    {
        String Temp = "";
        
        if(N == null)
        {
            throw new NullPointerException("Node is null.");
        }

        Temp = N.NodeId + "," + 
               N.FirstAcceptedNodeId + "," + 
               N.SecondAcceptedNodeId + "," + 
               N.PatientPreviousNodeId + "," + 
               N.DoctorPreviousNodeId + "," + 
               N.HospitalPreviousNodeId + "," +
               N.HospitalPreviousNodeId + "," ;
        
        return Temp;
    }
    
    public static String calculateHash(String input)
    {
        int nonce = 0;
        String hash = "";
        String target = HashAndSign.getDificultyString(difficulty); //Create a string with difficulty * "0" 
        
        do
        {
            hash = HashAndSign.applySha256(input + Integer.toString(nonce));
            nonce++;
            try 
            {
                Thread.sleep(0, 1000); //wait for 1000 nanoseconds
            } 
            catch (InterruptedException ex) 
            {
                Logger.getLogger(Cryptography.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        while(!hash.substring( 0, difficulty ).equals(target));
        
        return hash;
    }
    
    public static String calculateDigitalSignature(String input, String privateKey, String publicKey)
    {
        String DigitalSignature = input.toUpperCase();
        return DigitalSignature;
    }

    public static String generateRandomAlphanumericString(int targetStringLength) 
    {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        
        SecureRandom random = new SecureRandom();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
          .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
          .limit(targetStringLength)
          .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
          .toString();

        //System.out.println(generatedString);
        return generatedString;
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package jtangleehr;

import entities.Node;
import crypto.AsymmetricEncryption;
import crypto.SymmetricEncryption;
import entities.User;
import java.util.Calendar;
import java.util.Date;
import utils.DateNTime;

/**
 * @author hosseinAghahosseini
 */

public class JTangleEhr {

    public static void main(String[] args) throws Exception {
        System.out.println("-======== jTangleEHR ========-\nA Tangle based distributed Ledger for Electronic Health Records\nDeveloped with Java 21");
        
        // Create String variables 
        String originalString = "GeeksforGeeks"; 
        
        
        // Call encryption method 
        String encryptedString 
            = SymmetricEncryption.encrypt(originalString, "Vakhmiad", "tarabism"); 
        
        // Call decryption method 
        String decryptedString 
            = SymmetricEncryption.decrypt(encryptedString, "Vakhmiad", "tarabism"); 
  
        // Print all strings 
        System.out.println(originalString); 
        System.out.println(encryptedString); 
        System.out.println(decryptedString); 
        
        AsymmetricEncryption rsa = new AsymmetricEncryption();
        rsa.generateKeyPair();
        rsa.writeKeyPairToFile("", "Test");
        
        String encoded = rsa.digitalSign(originalString);
        //String decoded = rsa.verifySignature("t" + encoded.substring(1));
        String decoded = rsa.verifySignature(encoded);
        
        System.out.println("digital sign test:"); 
        System.out.println(originalString); 
        System.out.println(encoded); 
        System.out.println(decoded);
        
        Node n = new Node();
        n.createGenesis();
        n.loadGenesis();
        
        //user1
        User patient1 = new User(User.UserRole.Patient);   
        patient1.setUserDetails("Hossein", "Aghahosseini", "Qazvin", DateNTime.getDateFromParts(1996, 06, 10));
        patient1.createTangleForUser();
        patient1.addNodeToTangleAsPatient("EHR1", false, null, null, null, null);
        patient1.addNodeToTangleAsPatient("EHR2", true, null, null, null, null);
        //patient1.addNodeToTangleAsPatient("EHR2", false, null, null, null, null);
        patient1.addNodeToTangleAsPatient("EHR3", false, null, null, null, null);
        
        var Nodes = patient1.MyTangle;
        var Nodes2 = patient1.getMyNodesFromTangle();
        System.out.println("Nodes2");
        
        //user2
        User patient2 = new User(User.UserRole.Patient);
        patient2.setUserDetails("Mohammad", "Torabi", "Qazvin", DateNTime.getDateFromParts(1995, 11, 15));
        patient2.createTangleForUser();
        
        patient2.receiveTangleAndUpdateSelf(patient1.advertiseTangle());
        patient2.addNodeToTangleAsPatient("EHR4", false, null, null, null, null);
        
        var Nodes3 = patient2.MyTangle;
        var Nodes4 = patient2.getMyNodesFromTangle();
        
        System.out.println("Nodes3");
        
        //todo make genesis 2 nodes
    }
    
    public static void test()
    {
        
    }
}

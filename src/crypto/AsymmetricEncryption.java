/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package crypto;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;

/**
 * @author hosseinAghahosseini
 */


public class AsymmetricEncryption {
    
    private PrivateKey privateKey;
    public PublicKey publicKey;
    
    public void generateKeyPair()
    {
        try
        {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            
            KeyPair kp = kpg.generateKeyPair();
            privateKey = kp.getPrivate();
            publicKey = kp.getPublic();
            
            System.out.println("Private key: " + privateKey);
            System.out.println("Public key: " + publicKey);
        }
        catch (NoSuchAlgorithmException e)
        {
            
        } 
    }
    
    public void writeKeyPairToFile(String Address, String FileName)
    {
        try
        {
            try (FileOutputStream outPrivate = new FileOutputStream(Address + FileName + "_private.key")) 
            {
                outPrivate.write(privateKey.getEncoded());
            }

            try (FileOutputStream outPublic = new FileOutputStream(Address + FileName + "_public.key")) 
            {
                outPublic.write(publicKey.getEncoded());
            }
        }
        catch(Exception e) {}   
    }
    
    public PublicKey loadPublicKey(String Address, String FileName) 
    {
        try
        {
            // reading from resource folder
            //byte[] publicKeyBytes = getClass().getResourceAsStream("/key.pub").readAllBytes();
            String filePath = Address + FileName + "_public.key";
            byte[] publicKeyBytes = Files.readAllBytes(Paths.get(filePath));

            KeyFactory publicKeyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            this.publicKey = publicKeyFactory.generatePublic(publicKeySpec);
            return publicKey;
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        return null;
    }
    
    public PrivateKey loadPrivateKey(String Address, String FileName) 
    {
        try
        {
            // reading from resource folder
            //byte[] privateKeyBytes = getClass().getResourceAsStream("/key.priv").readAllBytes();
            String filePath = Address + FileName + "_public.key";
            byte[] privateKeyBytes = Files.readAllBytes(Paths.get(filePath));

            KeyFactory privateKeyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            this.privateKey = privateKeyFactory.generatePrivate(privateKeySpec);
            return privateKey;
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        return null;
    }
    
    public String encode(String toEncode) throws Exception 
    {
        //PublicKey publicKey = loadPublicKey();

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] bytes = cipher.doFinal(toEncode.getBytes(StandardCharsets.UTF_8));
        return new String(Base64.getEncoder().encode(bytes));
    }
    
    public String decode(String toDecode) throws Exception 
    {
        //PrivateKey privateKey = loadPrivateKey();

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] bytes = cipher.doFinal(Base64.getDecoder().decode(toDecode));
        return new String(bytes);
    }
    
    public String digitalSign(String toEncode)
    {
        try
        {
            //PublicKey publicKey = loadPublicKey();

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);

            byte[] bytes = cipher.doFinal(toEncode.getBytes(StandardCharsets.UTF_8));
            return new String(Base64.getEncoder().encode(bytes));
        }
        catch(Exception e)
        {
            return null;
        }
    }
    
    public String verifySignature(String toDecode)
    {
        try
        {
            //PrivateKey privateKey = loadPrivateKey();

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, publicKey);

            byte[] bytes = cipher.doFinal(Base64.getDecoder().decode(toDecode));
            return new String(bytes);
        }
        catch(Exception e)
        {
            return null;
        }

    }
    
    public String GetPublicKeyInBase64String()
    {
        return Base64.getEncoder().encodeToString(this.publicKey.getEncoded());
    }
    
    public static byte[] GetPublicKeyByteArrayFromBase64String(String base64String)
    {
        return Base64.getDecoder().decode(base64String);
    }
    
    public PublicKey SetPublicKeyFromBase64String(String base64String)
    {
        byte[] publicKeyBytes = GetPublicKeyByteArrayFromBase64String(base64String);
        
        try{
            KeyFactory publicKeyFactory = KeyFactory.getInstance("RSA");       
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            this.publicKey = publicKeyFactory.generatePublic(publicKeySpec);
            return this.publicKey;
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        return null;
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entities;

import crypto.AsymmetricEncryption;
import crypto.Cryptography;
import crypto.HashAndSign;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.JsonParser;

/**
 * @author hosseinAghahosseini
 */

public class Node {
    
    public static String DELIMETER = ",";
    public static String END_CHAR = ";";
    
    //mining difficulty
    public static int difficulty = 1;
    
    //Id
    public String NodeId;
    
    //Weight
    public int OwnWeight;
    public int CumulativeWeight;
    public int Score;
    
    //Nodes to Accept
    public Node FirstAcceptedNode;
    public String FirstAcceptedNodeId = "#";
    
    public Node SecondAcceptedNode;
    public String SecondAcceptedNodeId = "#";
    
    //My previous Nodes to reffer
    public Node PatientPreviousNode;
    public String PatientPreviousNodeId = "#";
    public String PatientHashedPublicKey = "#";
    
    public Node DoctorPreviousNode;
    public String DoctorPreviousNodeId = "#";
    public String DoctorHashedPublicKey = "#";
    
    public Node HospitalPreviousNode;
    public String HospitalPreviousNodeId = "#";
    public String HospitalHashedPublicKey = "#";
    
    //Transaction
    public String EHR;
    public boolean IsEhrEncrypted;
    public String EHRAesKeyEncryptedByAssymeticKey;
    public String TransactionCreatorPublicKey;
    
    //Hash & Digital Signature
    public String Nonce;
    public String Hash;
    public String DigitalSignature;
    
    //Checking Varriables
    int state = 0;
    
    public Node()
    {
        
    }
    
    public Node(Node other, String decryptedEHR) 
    {
        if (other == null) {
            throw new IllegalArgumentException("Cannot create a Node from a null reference.");
        }

        this.NodeId = other.NodeId;

        this.OwnWeight = other.OwnWeight;
        this.CumulativeWeight = other.CumulativeWeight;
        this.Score = other.Score;

        this.FirstAcceptedNode = other.FirstAcceptedNode;
        this.FirstAcceptedNodeId = other.FirstAcceptedNodeId;
        this.SecondAcceptedNode = other.SecondAcceptedNode;
        this.SecondAcceptedNodeId = other.SecondAcceptedNodeId;

        this.PatientPreviousNode = other.PatientPreviousNode;
        this.PatientPreviousNodeId = other.PatientPreviousNodeId;
        this.PatientHashedPublicKey = other.PatientHashedPublicKey;

        this.DoctorPreviousNode = other.DoctorPreviousNode;
        this.DoctorPreviousNodeId = other.DoctorPreviousNodeId;
        this.DoctorHashedPublicKey = other.DoctorHashedPublicKey;

        this.HospitalPreviousNode = other.HospitalPreviousNode;
        this.HospitalPreviousNodeId = other.HospitalPreviousNodeId;
        this.HospitalHashedPublicKey = other.HospitalHashedPublicKey;

        //this.EHR = other.EHR;
        this.EHR = decryptedEHR;
        this.IsEhrEncrypted = other.IsEhrEncrypted;
        this.EHRAesKeyEncryptedByAssymeticKey = other.EHRAesKeyEncryptedByAssymeticKey;
        this.TransactionCreatorPublicKey = other.TransactionCreatorPublicKey;

        this.Nonce = other.Nonce;
        this.Hash = other.Hash;
        this.DigitalSignature = other.DigitalSignature;

        this.state = other.state;
    }
    
    
    //Node Creation
    public Node createNewNodeWithoutTipSelection(int OwnWeight,
            String EHR, String TransactionCreatorPublicKey,
            boolean IsEhrEncrypted, String EHRAesKeyEncryptedByAssymeticKey,
            String FirstAcceptedNodeId, String SecondAcceptedNodeId,
            String PatientPreviousNodeId, String PatientHashedPublicKey,
            String DoctorPreviousNodeId, String DoctorHashedPublicKey,
            String HospitalPreviousNodeId, String HospitalHashedPublicKey)
    {
        Node n = new Node();
        n.OwnWeight = OwnWeight;
        n.EHR = EHR;
        n.IsEhrEncrypted = IsEhrEncrypted;
        if(IsEhrEncrypted)
        {
            n.EHRAesKeyEncryptedByAssymeticKey = EHRAesKeyEncryptedByAssymeticKey;
        }
        else
        {
            n.EHRAesKeyEncryptedByAssymeticKey = "$";
        }
        n.FirstAcceptedNodeId = FirstAcceptedNodeId;
        n.SecondAcceptedNodeId = SecondAcceptedNodeId;
        
        n.PatientPreviousNodeId = PatientPreviousNodeId;
        n.PatientHashedPublicKey = PatientHashedPublicKey;
        
        n.DoctorPreviousNodeId = DoctorPreviousNodeId;
        n.DoctorHashedPublicKey = DoctorHashedPublicKey;
        
        n.HospitalPreviousNodeId = HospitalPreviousNodeId;
        n.HospitalHashedPublicKey = HospitalHashedPublicKey;
        
        n.NodeId = HashAndSign.applySha256(EHR + n.FirstAcceptedNodeId + n.SecondAcceptedNodeId); 
        
        return n;
    }
    
    public Node createNewNodeWithEhrDataOnly(int OwnWeight,
            String EHR, 
            boolean IsEhrEncrypted, String EHRAesKeyEncryptedByAssymeticKey,
            String TransactionCreatorPublicKey)
    {
        this.OwnWeight = OwnWeight; //todo calculate own weight
        this.EHR = EHR;
        this.IsEhrEncrypted = IsEhrEncrypted;
        if(IsEhrEncrypted)
        {
            this.EHRAesKeyEncryptedByAssymeticKey = EHRAesKeyEncryptedByAssymeticKey;
        }
        else
        {
            this.EHRAesKeyEncryptedByAssymeticKey = "$";
        }
        this.TransactionCreatorPublicKey = TransactionCreatorPublicKey;

        return this;
    }
    
    public boolean setPatientInfoToNode(String PatientHashedPublicKey, String PatientPreviousNodeId)
    {
        if(PatientHashedPublicKey != null && PatientHashedPublicKey != null)
        {
            if("".equals(PatientHashedPublicKey) == false && "".equals(PatientHashedPublicKey) == false)
            {
                this.PatientPreviousNodeId = PatientPreviousNodeId;
                this.PatientHashedPublicKey = PatientHashedPublicKey;
                return true;
            }
        }
        return false;
    }
    
    public boolean setDoctorInfoToNode(String DoctorHashedPublicKey, String DoctorPreviousNodeId)
    {
        if(DoctorPreviousNodeId != null && DoctorHashedPublicKey != null)
        {
            if("".equals(DoctorPreviousNodeId) == false && "".equals(DoctorHashedPublicKey) == false)
            {
                this.DoctorPreviousNodeId = DoctorPreviousNodeId;
                this.DoctorHashedPublicKey = DoctorHashedPublicKey;
                return true;
            }
        }
        return false;
    }
    
    public boolean setHospitalInfoToNode(String HospitalHashedPublicKey, String HospitalPreviousNodeId)
    {
        if(HospitalPreviousNodeId != null && HospitalPreviousNodeId != null)
        {
            if("".equals(HospitalHashedPublicKey) == false && "".equals(HospitalPreviousNodeId) == false)
            {
                this.HospitalPreviousNodeId = HospitalPreviousNodeId;
                this.HospitalHashedPublicKey = HospitalHashedPublicKey;
                return true;
            }
        }
        return false;
    }


    //Genesis
    public Node createGenesis()
    {
        Node genesis = new Node();
        
        genesis.NodeId = "Genesis000";
        
        genesis.FirstAcceptedNodeId = "0";
        genesis.SecondAcceptedNodeId = "0";
        
        genesis.PatientPreviousNodeId = "0";
        genesis.DoctorPreviousNodeId = "0";
        genesis.HospitalPreviousNodeId = "0";
        
        genesis.PatientHashedPublicKey = "0";
        genesis.DoctorHashedPublicKey = "0";
        genesis.HospitalHashedPublicKey = "0";
        
        genesis.EHR = "FirstInsertedEhrAsGenesisNode";
        genesis.IsEhrEncrypted = false;
        genesis.EHRAesKeyEncryptedByAssymeticKey = "#";
        //genesis.TransactionCreatorPublicKey = "#";
        
        AsymmetricEncryption rsa = new AsymmetricEncryption();
        rsa.generateKeyPair();   
        rsa.writeKeyPairToFile("Keys/", "Genesis");
        
        genesis.TransactionCreatorPublicKey = rsa.GetPublicKeyInBase64String();
        System.out.println("PublicKey is:" + genesis.TransactionCreatorPublicKey);
        
        String hashInput = createHashInput(genesis);      
        String hash = genesis.calculateHashAndSetNonce(hashInput);
        
        System.out.println("Hash Input is:" + hashInput);
        System.out.println("Nonce is:" + genesis.Nonce);  
        System.out.println("Hash is:" + hash);
            
        genesis.DigitalSignature = rsa.digitalSign(hash);
        System.out.println("DigitalSign is:" + genesis.DigitalSignature);
             
        //verify signature
        String ToCheckInput = hashInput + genesis.Nonce;
        System.out.println("ToCheckInput is:" + ToCheckInput);
        
        String ToCheckHash = HashAndSign.applySha256(ToCheckInput);     
        System.out.println("ToCheckHash is:" + ToCheckHash);
        
        AsymmetricEncryption rsa2check = new AsymmetricEncryption();
        rsa2check.SetPublicKeyFromBase64String(genesis.TransactionCreatorPublicKey);
        
        //String Plaintext = rsa.verifySignature(ToCheckHash);
        String Plaintext = rsa.verifySignature(genesis.DigitalSignature);
        System.out.println("Plaintext is:" + Plaintext);
        
        System.out.println("Genesis is:" + genesis.toString());
        
        writeNodeToFile(genesis, "genesis.ehr");
        Node gen2 = readNodeFromFile("genesis.ehr");
        System.out.println("gen2 is:" + gen2.EHR);
        
        return genesis;
    }
    
    public Node loadGenesis()
    {
        Node genesis = readNodeFromFile("genesis.ehr");
        
        AsymmetricEncryption rsa = new AsymmetricEncryption();
        rsa.loadPublicKey("Keys/", "Genesis");
        //rsa.writeKeyPairToFile("Keys/", "Genesis");
        
        String hashInput = createHashInput(genesis);
        System.out.println("Hash Input is:" + hashInput);
        
        String hash = calculateHashWithNonce(hashInput, genesis.Nonce);
        System.out.println("Hash is      : " + hash);
        
        String Plaintext = rsa.verifySignature(genesis.DigitalSignature);
        System.out.println("Plaintext is : " + Plaintext);
        
        System.out.println("Genesis is   : " + genesis.toString());
        
        return genesis;
    }
    
    
    //Hashing   
    public static String createHashInput(Node N)
    {
        String output =  
            N.NodeId + DELIMETER + 

            N.FirstAcceptedNodeId + DELIMETER + 
            N.SecondAcceptedNodeId + DELIMETER + 

            N.PatientPreviousNodeId + DELIMETER + 
            N.DoctorPreviousNodeId + DELIMETER + 
            N.HospitalPreviousNodeId + DELIMETER +

            N.PatientHashedPublicKey + DELIMETER +
            N.DoctorHashedPublicKey + DELIMETER + 
            N.HospitalHashedPublicKey + DELIMETER + 

            N.EHR + DELIMETER + 
            N.IsEhrEncrypted + DELIMETER + 
            N.EHRAesKeyEncryptedByAssymeticKey + DELIMETER +
            N.TransactionCreatorPublicKey + DELIMETER ;

        return output;
    }
    
    public String calculateHashAndSetNonce(String input)
    {
        int nonce = -1;
        String hash = "";
        String target = HashAndSign.getDificultyString(difficulty); //Create a string with difficulty * "0" 
        String ModifiedInput = "";
        
        do
        {
            nonce++;
            ModifiedInput = input + Integer.toString(nonce);
            //hash = HashAndSign.applySha256(input + Integer.toString(nonce));
            hash = HashAndSign.applySha256(ModifiedInput);

            if(nonce == Integer.MAX_VALUE)
            {
                System.out.println("Int_Max");
                return null;
            }
        }
        while(!hash.substring( 0, difficulty ).equals(target));
        
        System.out.println("Taraz ModifiedInput is: " + ModifiedInput);
        System.out.println("Taraz Hash is: " + hash);
        
        this.Hash = hash;
        this.Nonce = Integer.toString(nonce);
        
        return hash;
    }
    
    public static String calculateHashWithNonce(String preCreatedHasInput, String nonce)
    {
        String hash = HashAndSign.applySha256(preCreatedHasInput + nonce);
        String target = HashAndSign.getDificultyString(difficulty); //Create a string with difficulty * "0" 
        
        if(hash.substring( 0, difficulty ).equals(target))
        {
            return hash;
        }
        
        return null;
    }
        
    public String setNodeId()
    {
        this.NodeId = HashAndSign.applySha256(EHR + FirstAcceptedNodeId + SecondAcceptedNodeId); 
        return  this.NodeId;
    }

    
    //Read & Write    
    @Override
    public String toString()
    {
        String output = "{";
        
        output +=  "\"NodeId\":\"" + NodeId
                +  "\",\"FirstAcceptedNodeId\":\"" + FirstAcceptedNodeId
                +  "\",\"SecondAcceptedNodeId\":\"" + SecondAcceptedNodeId
                +  "\",\"PatientPreviousNodeId\":\"" + PatientPreviousNodeId
                +  "\",\"DoctorPreviousNodeId\":\"" + DoctorPreviousNodeId
                +  "\",\"HospitalPreviousNodeId\":\"" + HospitalPreviousNodeId
                +  "\",\"EHR\":\"" + EHR
                +  "\",\"IsEhrEncrypted\":\"" + IsEhrEncrypted
                +  "\",\"EHRAesKeyEncryptedByAssymeticKey\":\"" + EHRAesKeyEncryptedByAssymeticKey
                +  "\",\"TransactionCreatorPublicKey\":\"" + TransactionCreatorPublicKey
                +  "\",\"Nonce\":\"" + Nonce
                +  "\",\"Hash\":\"" + Hash
                +  "\",\"DigitalSignature\":\"" + DigitalSignature;      
        output += "\"}";
        
        return output;
    }
    
    public String toStringShort() {
        return "{"
            + "\"NodeId\":\"" + NodeId + "\","
            + "\"FirstAcceptedNodeId\":\"" + FirstAcceptedNodeId + "\","
            + "\"SecondAcceptedNodeId\":\"" + SecondAcceptedNodeId + "\","
            + "\"EHR\":\"" + EHR + "\","
            + "\"IsEhrEncrypted\":" + IsEhrEncrypted + ","
            + "\"TransactionCreatorPublicKey\":\"" + TransactionCreatorPublicKey + "\","
            + "\"Hash\":\"" + Hash + "\","
            + "\"DigitalSignature\":\"" + DigitalSignature + "\""
            + "}";
    }

    // Converts ArrayList<YourNodeClass> to JSON array
    public static String toJsonArray(java.util.List<Node> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i).toStringShort());
            if (i < list.size() - 1) sb.append(",\n");
        }

        sb.append("]");
        return sb.toString();
    }
    
    public boolean writeNodeToFile(Node node, String address)
    {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(address)))
        {
            writer.write(node.toString());
            System.out.println("String written to file successfully!");
        } 
        catch (IOException e) 
        {
            System.err.println("Error writing to file: " + e.getMessage());
        }
        return false;
    }
    
    public Node readNodeFromFile(String address)
    {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(address))) 
        {
            StringBuilder content = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) 
            {
                content.append(line).append(System.lineSeparator());
            }
            
            System.out.println("File content:\n" + content);
            
            return JsonParser.ParseNodeFromString(content.toString());
        } 
        catch (IOException e) 
        {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return null;
    }
    
    
    //Compare & Validation   
    public static boolean compareTwoNodes(Node node1, Node node2)
    {
        if(! node1.NodeId.equals(node2.NodeId))
            return false;
        
        if(! node1.FirstAcceptedNodeId.equals(node2.FirstAcceptedNodeId))
            return false;
        
        if(! node1.SecondAcceptedNodeId.equals(node2.SecondAcceptedNodeId))
            return false;
        
        return true;
    }
    
    public static int validateNodeHash(Node node1)
    {
        String hashInput = createHashInput(node1);
        String calculatedHash = calculateHashWithNonce(hashInput, node1.Nonce);
        
        //Check if provided hash is valid (is equal to the calculated hash)
        if(calculatedHash.equals(node1.Hash)) 
        {
            String target = HashAndSign.getDificultyString(difficulty); //Create a string with difficulty * "0" 
            
            //Chcek if the hash has enough leading zeros (validating proof of work)
            if(node1.Hash.substring( 0, difficulty ).equals(target))
            {
                return 1;
            }
            else
            {
                return -1;
            }
        }
        else
        {
            return -2;
        }
    }
}

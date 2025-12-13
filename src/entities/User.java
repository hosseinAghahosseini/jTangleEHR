/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package entities;

import crypto.AsymmetricEncryption;
import crypto.HashAndSign;
import crypto.SymmetricEncryption;
import entities.Node;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;

/**
 * @author hosseinAghahosseini
 */

public class User {
    
    public enum UserRole
    {
        Patient(1),
        Doctor(2),
        Hospital(3);
        
        private int value;

        private UserRole(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }
    
    static final String AES_KEY_DELIMETER = ",";
    
    //Personal Data
    public String HashedPublicKey; //aka address
    public UserRole Role;
    
    private AsymmetricEncryption RsaKeys;
    
    private String FirstName;
    private String LastName;
    private String Address;
    private Date BirthDate;
    
    //Tangle Data
    public Tangle MyTangle;
    public String MyPreviousNodeId = "#";
    
    public User(UserRole userRole)
    {
        this.Role = userRole;
        RsaKeys = new AsymmetricEncryption();
        RsaKeys.generateKeyPair();
        HashedPublicKey = HashAndSign.applySha256(RsaKeys.GetPublicKeyInBase64String());
    }
    
    public void setUserDetails(String firstName, String lastName, String address, Date birthDate)
    {
        this.FirstName = firstName;
        this.LastName = lastName;
        this.Address = address;
        this.BirthDate = BirthDate;
    }
    
    public void createTangleForUser()
    {
        MyTangle = new Tangle();
        MyTangle.startTangleFromScratch();
    }
    
    public void writeRsaKeysToFile(String address, String fileName)
    {
        this.RsaKeys.writeKeyPairToFile(address, fileName);
    }
    
    public String addNodeToTangle(String EHR, boolean IsEhrEncrypted,
            String PatientPreviousNodeId, String PatientHashedPublicKey,
            String DoctorPreviousNodeId, String DoctorHashedPublicKey,
            String HospitalPreviousNodeId, String HospitalHashedPublicKey)
    {
        Node n = new Node();
        
        if(!IsEhrEncrypted) //plaintext ehr
        {
            n = n.createNewNodeWithEhrDataOnly(Role.value, EHR, false, "", RsaKeys.GetPublicKeyInBase64String());
        }
        else //encrypting ehr
        {
            //generate ehr key and ehr salt
            SecureRandom r = new SecureRandom();
            
            String Key = HashAndSign.applySha256(EHR + String.valueOf(r.nextInt()));
            String Salt = HashAndSign.applySha256(String.valueOf(r.nextInt()));
            
            try
            {
                String EHRAesKeyEncryptedByAssymeticKey = RsaKeys.encode(Key + AES_KEY_DELIMETER + Salt);
                String EncryptedEHR = SymmetricEncryption.encrypt(EHR, Key, Salt);
                
                n = n.createNewNodeWithEhrDataOnly(Role.value, EncryptedEHR, true, EHRAesKeyEncryptedByAssymeticKey, RsaKeys.GetPublicKeyInBase64String());
            }
            catch(Exception e) 
            {
                return null;
            }             
        }

        
        //tip selection
        ArrayList<Node> SelectedTips = MyTangle.selectTipNodes();
        if(SelectedTips.size() == 2)
        {
            n.FirstAcceptedNodeId = SelectedTips.get(0).NodeId;
            n.SecondAcceptedNodeId = SelectedTips.get(1).NodeId;
        }
        else if(SelectedTips.size() == 1)
        {
            n.FirstAcceptedNodeId = SelectedTips.get(0).NodeId;
            n.SecondAcceptedNodeId = SelectedTips.get(0).NodeId;
        }
        else // No genesis node has been created
        {
            return null;
        }
        
        //Set Doctor Data
        n.setDoctorInfoToNode(DoctorHashedPublicKey, DoctorPreviousNodeId);
        
        //Set Hospital Data
        n.setHospitalInfoToNode(HospitalHashedPublicKey, HospitalPreviousNodeId);
        
        //Set Patient Data
        n.setPatientInfoToNode(PatientHashedPublicKey, PatientPreviousNodeId);
        
        
        //Create and Set NodeId
        n.setNodeId();
        
        //Create Hash
        String hashInput = n.createHashInput(n);  
        String hash = n.calculateHashAndSetNonce(hashInput);
        
        //Digital Signature
        n.DigitalSignature = RsaKeys.digitalSign(hash);
        
        //write node to file
        n.writeNodeToFile(n, hash + ".ehr");
        Node gen2 = n.readNodeFromFile(hash + ".ehr");

        boolean res = MyTangle.addNode(n); 
        
        if(res)
        {
            this.MyPreviousNodeId = n.NodeId;
        }
        
        return n.NodeId;
    }
    
    public String addNodeToTangleAsPatient(String EHR, boolean IsEhrEncrypted,
            String DoctorPreviousNodeId, String DoctorHashedPublicKey,
            String HospitalPreviousNodeId, String HospitalHashedPublicKey)
    {
        return addNodeToTangle(EHR, IsEhrEncrypted, this.MyPreviousNodeId, this.HashedPublicKey, DoctorPreviousNodeId, DoctorHashedPublicKey, HospitalPreviousNodeId, HospitalHashedPublicKey);
    }
    
    public String addNodeToTangleAsDoctor(String EHR, boolean IsEhrEncrypted,
            String PatientPreviousNodeId, String PatientHashedPublicKey,
            String HospitalPreviousNodeId, String HospitalHashedPublicKey)
    {
        return addNodeToTangle(EHR, IsEhrEncrypted, PatientPreviousNodeId, PatientHashedPublicKey, this.MyPreviousNodeId, this.HashedPublicKey,  HospitalPreviousNodeId, HospitalHashedPublicKey);
    }
    
    public String addNodeToTangleAsHospital(String EHR, boolean IsEhrEncrypted,
            String PatientPreviousNodeId, String PatientHashedPublicKey,
            String DoctorPreviousNodeId, String DoctorHashedPublicKey)
    {
        return addNodeToTangle(EHR, IsEhrEncrypted, PatientPreviousNodeId, PatientHashedPublicKey, DoctorPreviousNodeId, DoctorHashedPublicKey, this.MyPreviousNodeId, this.HashedPublicKey);
    }
    
    public ArrayList<Node> getMyNodesFromTangle()
    {
        ArrayList<Node> MyEhrNodes = new ArrayList<Node>();
        
        //Normal Search
        for(int i = 0; i < MyTangle.DAG.size(); i++)
        {
            Node currentNode = MyTangle.DAG.get(i);
            
            String ToCheckKey = currentNode.PatientHashedPublicKey;
            
            if(this.Role == UserRole.Doctor)
            {
                ToCheckKey = currentNode.PatientHashedPublicKey;
            }
            else if(this.Role == UserRole.Hospital)
            {
                ToCheckKey = currentNode.HospitalHashedPublicKey;
            }
            
            //if(currentNode.PatientHashedPublicKey == HashedPublicKey)
            if(ToCheckKey.equals(HashedPublicKey))
            {
                if(currentNode.IsEhrEncrypted)
                {
                    try
                    {
                        String EHRAesKeyAndSalt = RsaKeys.decode(currentNode.EHRAesKeyEncryptedByAssymeticKey);
                        var EHRAesKeyAndSaltArray = EHRAesKeyAndSalt.split(AES_KEY_DELIMETER);
                        String DecryptedEHR = SymmetricEncryption.decrypt(currentNode.EHR, EHRAesKeyAndSaltArray[0], EHRAesKeyAndSaltArray[1]);
                        
                        //String Temp = currentNode.EHR;
                        //currentNode.EHR = DecryptedEHR;
                        //MyEhrNodes.add(currentNode);
                        MyEhrNodes.add(new Node(currentNode, DecryptedEHR));
                        //currentNode.EHR = Temp;
                    }
                    catch (Exception e) {} 
                }
                else
                {
                    MyEhrNodes.add(currentNode);
                }
                
            }
        }
        
        return MyEhrNodes;
    }
    
    public Node findNodeById(String NodeId)
    {
        Node n = this.MyTangle.findNodeById(NodeId);
        if(n == null) return null;
        
        if(n.IsEhrEncrypted)
        {
            try
            {
                String EHRAesKeyAndSalt = RsaKeys.decode(n.EHRAesKeyEncryptedByAssymeticKey);
                var EHRAesKeyAndSaltArray = EHRAesKeyAndSalt.split(AES_KEY_DELIMETER);
                String DecryptedEHR = SymmetricEncryption.decrypt(n.EHR, EHRAesKeyAndSaltArray[0], EHRAesKeyAndSaltArray[1]);

                return (new Node(n, DecryptedEHR));
                //currentNode.EHR = Temp;
            }
            catch (Exception e) {
                return null;
            } 
        }
        else
        {
            return n;
        }
    }
    
    
    //Tangle Network Functions
    
    public Tangle advertiseTangle()
    {
        return MyTangle;
    }
    
    public boolean receiveTangleAndUpdateSelf(Tangle recievedTangle)
    {
        ArrayList<Node> NewlyAddedNodes = new ArrayList<>();
        
        //Verfies hash of the Nodes of the new tangle
        //Also extracts newly added nodes in comparison to our tangle 
        for(int i = recievedTangle.DAG.size() - 1; i >= 0 ; i--)
        {
            boolean found = false;
            
            for(int j = MyTangle.DAG.size() - 1; j >= 0 ; j--)
            {
                if(Node.compareTwoNodes(recievedTangle.DAG.get(i) ,MyTangle.DAG.get(j)) )
                {
                    found = true;
                    break;
                }
            }
            
            if(found) //already in the tangle
            {
                continue;
            }
            else //not in the tangle so we have to check if valid
            {
                if(Node.validateNodeHash(recievedTangle.DAG.get(i)) <= 0) 
                {
                    //An invalid node is found in the tangle, so the whole tangle is invalid
                    return false;
                }
                else //to check if the two accepted nodes are in the tangle for dfs
                {
                    NewlyAddedNodes.add(recievedTangle.DAG.get(i));
                }              
            }
        }
        
        // check if newly added nodes have cycle, if so the Tangle becomes invalid
        if(Tangle.hasCycle(NewlyAddedNodes))
        {
            return false;
        }
        
        // check if newly added nodes reference back to our graph (and genesis)
        if(!Tangle.checkIfBackwardEdgesOfNewNodesLeadToOurTangle(NewlyAddedNodes, MyTangle)) //todo check
        {
            return false;
        }
        
        //The received tangle is valid, so we update our tangle according to it
        for(int i = 0 ; i < NewlyAddedNodes.size(); i++)
        {
            NewlyAddedNodes.get(i).state = 0;
            MyTangle.addNodeWithoutChecking(NewlyAddedNodes.get(i));
        }

        return true;
    }
}

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
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

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
    
    static final int MILESTONE_NODES_NEEDED = 10;
    static final String AES_KEY_DELIMETER = ",";
    final SecureRandom srandom = new SecureRandom();
    
    //Personal Data
    public String HashedPublicKey; //aka address
    public UserRole Role;
    
    private AsymmetricEncryption RsaKeys;
    
    private String FirstName;
    private String LastName;
    private String Address;
    private Date BirthDate;
    
    private boolean isLiteClient = false;
    
    //Tangle Data
    public Tangle MyTangle;
    public String MyPreviousNodeId = "#";
    
    //Milestone
    public int Milestone;
    
    public User(UserRole userRole)
    {
        this.Role = userRole;
        if(userRole != UserRole.Hospital) isLiteClient = true;
        RsaKeys = new AsymmetricEncryption();
        RsaKeys.generateKeyPair();
        HashedPublicKey = HashAndSign.applySha256(RsaKeys.GetPublicKeyInBase64String());
    }
    
    public void setUserDetails(String firstName, String lastName, String address, Date birthDate)
    {
        this.FirstName = firstName;
        this.LastName = lastName;
        this.Address = address;
        this.BirthDate = birthDate;
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
            //SecureRandom r = new SecureRandom(); //may cause deadlock

            String Key = HashAndSign.applySha256(EHR + String.valueOf(srandom.nextInt()));
            String Salt = HashAndSign.applySha256(String.valueOf(srandom.nextInt()));
            
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
        //n.setNodeId();
        
        //Create Hash
        String hashInput = n.createHashInput(n);  
        String hash = n.calculateHashAndSetNonce(hashInput);
        
        //Digital Signature
        n.DigitalSignature = RsaKeys.digitalSign(hash);
        
        //write node to file
        n.writeNodeToFile(n, hash + ".ehr");
        Node gen2 = n.readNodeFromFile(hash + ".ehr");

        int res = MyTangle.addNode(n); 
        
        if(res > 0)
        {
            this.MyPreviousNodeId = n.NodeId;
        }
        else
        {
            System.out.println("Tarab Node: " + n.NodeId + " from user: " + this.HashedPublicKey.substring(0,12));
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
        ArrayList<Node> MyEhrNodes = new ArrayList<>();
        
        //Normal Search
        for(int i = 0; i < MyTangle.DAG.size(); i++)
        {
            Node currentNode = MyTangle.DAG.get(i);
            
            String ToCheckKey = currentNode.PatientHashedPublicKey;
            
            if(this.Role == UserRole.Doctor)
            {
                ToCheckKey = currentNode.DoctorHashedPublicKey;
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
                    //create a new Node so we don't mix up tangles of different users because of call by reference
                    Node newNode = new Node(recievedTangle.DAG.get(i));
                    NewlyAddedNodes.add(newNode);
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
        
        //update cumulative weights
        //todo debug
        //this.calculateCumulativeWeightsAfterAddingAGraph(NewlyAddedNodes);
        
        //The received tangle is valid, so we update our tangle according to it
        for(int i = 0 ; i < NewlyAddedNodes.size(); i++)
        {
            NewlyAddedNodes.get(i).state = 0;
            MyTangle.addNodeWithoutChecking(NewlyAddedNodes.get(i));
        }
        
        //This works but has overhead (It completely recalculates the cumulative weights of the dag)
        for(var node : MyTangle.DAG)
        {
            node.CumulativeWeight = 1;
        }
        MyTangle.DAG = Tangle.recalculateCumulativeWeights(MyTangle.DAG);

        return true;
    }

    //cumulative weight
    public boolean calculateCumulativeWeightsAfterAddingANode(String NodeId)
    {
        try
        {
            Node n = this.MyTangle.findNodeById(NodeId);
            if(n == null) return false;
            
            return this.MyTangle.calculateCumulativeWeightsAfterAddingANode(n);
        }
        catch (Exception e) { 
            System.out.println(e.getMessage());
        }

        return false;
    }
    
    public boolean calculateCumulativeWeightsAfterAddingAGraph(ArrayList<Node> NewlyAddedNodes)
    {
        try
        {
            return this.MyTangle.calculateCumulativeWeightsAfterAddingAGraph(NewlyAddedNodes);
        }
        catch (Exception e) { 
            System.out.println(e.getMessage());
        }

        return false;
    }
    
    //milestone
    public boolean milestoneInitiation()
    {
        Milestone++;
        int milestoneCount = 0;
        
        if(this.isLiteClient)
        {
            Set<String> MilestoneNodes = new TreeSet<>();
            for(int i = 0; i < MyTangle.DAG.size(); i++)
            {
                Node currentNode = MyTangle.DAG.get(i);                    

                String ToCheckKey = currentNode.PatientHashedPublicKey;       
                if(this.Role == UserRole.Doctor) ToCheckKey = currentNode.DoctorHashedPublicKey;
                else if(this.Role == UserRole.Hospital) ToCheckKey = currentNode.HospitalHashedPublicKey;

                if(ToCheckKey == null) continue; //already cleared in a milestone
                
                if(ToCheckKey.equals(HashedPublicKey)) //myNode
                {
                    MilestoneNodes.add(currentNode.NodeId); //keep our nodes

                    if(milestoneCount < MILESTONE_NODES_NEEDED)
                    {
                        //move forward to edges and save the edge nodes as milestone
                        var milestoneEdge = this.MyTangle.selectTipNodesMCMC(currentNode, 1).get(0);
                        milestoneEdge.Milestone = this.Milestone;

                        MilestoneNodes.add(milestoneEdge.NodeId); //keep some edge nodes
                        milestoneCount++;
                    }
                    
                    
                }
            }  
            
            //free memory of the nodes that are not in the path
            for(var Node : MyTangle.DAG)
            {
                if(!MilestoneNodes.contains(Node.NodeId))
                {
                    Node.freeMemory();
                }
            }
        }
        else
        {
            var previousMilestone = this.MyTangle.GetMilestoneNode();
            var edgeNodesToBecomeMilestone = this.MyTangle.selectTipNodesMCMC(previousMilestone, MILESTONE_NODES_NEEDED);
            for(var mnode : edgeNodesToBecomeMilestone)
            {
                mnode.Milestone = this.Milestone;
            }
        }
        
        return true;
    }
}

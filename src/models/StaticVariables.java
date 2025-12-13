/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import java.util.ArrayList;

/**
 * @author hosseinAghahosseini
 */

public class StaticVariables {
    
    public static ArrayList<User4UI> Users = new ArrayList<>();
    public static ArrayList<SimpleNode> Nodes = new ArrayList<>();
    public static int continueTaskExecution = 1; //-1 is terminate, 0 is pause, 1 is run
    
    public static SimpleUser findUserFromList(String UserId)
    {
        SimpleUser newUser = null;
        
        for(int i = Users.size() - 1; i >= 0; i-- )
        {
            if(Users.get(i).HashedPublicKey.equals(UserId) || Users.get(i).shortUserId.equals(UserId) )
            {
                newUser = new SimpleUser(i, Users.get(i).shortUserId, Users.get(i).HashedPublicKey, Users.get(i).MyPreviousNodeId);
                break;
            }
        }
        
        return newUser;
    }
    
    public static SimpleNode findNodeFromList(String NodeId)
    {
        SimpleNode newNode = null;
        
        for(int i = Nodes.size() - 1; i >= 0; i-- )
        {
            if(Nodes.get(i).shortNodeId.equals(NodeId) || Nodes.get(i).NodeId.equals(NodeId))
            {
                newNode = new SimpleNode(i, Nodes.get(i).shortNodeId, Nodes.get(i).NodeId);
                break;
            }
        }
        return newNode;
    }
    
    public static String addNodeAndGetOutput(String NodeId, String NodeVar)
    {
        if(NodeVar.length() > 0)
        {
            Nodes.add(new SimpleNode(Nodes.size(), NodeVar, NodeId)); //adds new node to node varriables
            return ("EHR Node [" + NodeVar + "]  was created successfully. Node Hash Address is:\n" + NodeId + "\n");
            //outputPrint("EHR Node [" + NodeVar + "]  was created successfully. Node Hash Address is:\n" + NodeId + "\n");
        }
        else
        {
            return ("EHR Node was created successfully. Node Hash Address is:\n" + NodeId + "\n");
            //outputPrint("EHR Node was created successfully. Node Hash Address is:\n" + NodeId + "\n");
        }
    }
}

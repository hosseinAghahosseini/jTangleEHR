/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

/**
 *
 * @author hosseinAghahosseini
 */
public class SimpleNode {
    
    public int nodeIndex;
    public String shortNodeId;
    
    public String NodeId;
    
    public SimpleNode()
    {
        
    }
    
    public SimpleNode(int nodeIndex, String shortNodeId, String NodeId)
    {
        this.nodeIndex = nodeIndex;
        this.shortNodeId = shortNodeId;
        
        this.NodeId = NodeId;
    }
    
}

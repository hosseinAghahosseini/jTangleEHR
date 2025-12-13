/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

/**
 * @author hosseinAghahosseini
 */

public class SimpleUser {
    
    public int userIndex;
    public String shortUserId;
    
    public String HashedPublicKey;
    public String MyPreviousNodeId = "#";
    
    
    public SimpleUser()
    {
        
    }
    
    public SimpleUser(int userIndex, String shortUserId, String HashedPublicKey, String MyPreviousNodeId)
    {
        this.userIndex = userIndex;
        this.shortUserId = shortUserId;
        
        this.HashedPublicKey = HashedPublicKey;
        this.MyPreviousNodeId = MyPreviousNodeId;
    }
    
}

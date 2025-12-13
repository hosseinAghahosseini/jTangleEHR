/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

import com.google.gson.Gson;
import entities.Node;

/**
 * @author hosseinAghahosseini
 */

public class JsonParser {
    
    public static Node ParseNodeFromString(String input)
    {
        Gson g = new Gson();

        // De-serialize to an object
        Node node = g.fromJson(input, Node.class);
        //System.out.println(person.name); //John
        
        return node;
    }
    
//    public Node ParseNodeFromFile(String fileAddress)
//    {
//        Gson g = new Gson();
//
//        // De-serialize to an object
//        Node node = g.fromJson(input, Node.class);
//        //System.out.println(person.name); //John
//        
//        return node;
//    }
    
}

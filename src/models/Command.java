package models;

import UI.DAGViewer;
import crypto.Cryptography;
import entities.Node;
import entities.Tangle;
import entities.User;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JTextArea;
import utils.DateNTime;
import models.AppData;

/**
 * @author hosseinAghahosseini
 * Test Case
 * TDD
 */

public class Command {
    
    public String Text;
    public long StartInTime; //in miliseconds
    public int OccurrenceCount;
    public long OccurrenceInterval;
    
    JTextArea outputStream = null;
    
    private static Random random = new Random();
    
    public Command(String text) throws Exception 
    {
        String[] CommandAndTime = text.split(":");
        
        Text = CommandAndTime[0];
        StartInTime = 1;
        OccurrenceCount = 1;
        OccurrenceInterval = 1;
        
        if(CommandAndTime.length == 1) { }
        else if(CommandAndTime.length == 2)
        {
            String[] SplitedTime = CommandAndTime[1].split(",");
            if(SplitedTime.length == 1)
            {
                StartInTime = Long.parseLong(SplitedTime[0]);
            }
            else if(SplitedTime.length == 3)
            {
                try
                {
                    StartInTime = Long.parseLong(SplitedTime[0]);
                    OccurrenceCount = Integer.parseInt(SplitedTime[1]);
                    OccurrenceInterval = Long.parseLong(SplitedTime[2]);
                }
                catch(Exception er) 
                {
                    throw new Exception("Command Time parameters mismatch: " + er.getMessage());
                }
            }
            else
            {
                throw new Exception("Command Time parameters mismatch.");
            }
        }
        else
        {
            throw new Exception("Command Time parameters mismatch.");
        }
    }
    
    public Command(String text, long startInTime, int occurrenceCount, int occurrenceInterval)
    {
        Text = text;
        StartInTime = startInTime;
        OccurrenceCount = occurrenceCount;
        OccurrenceInterval = occurrenceInterval;
    }
    
    public void SetOutputStream(JTextArea outputSource)
    {
        outputStream = outputSource;
    }
    
    public void ExcecuteCommand()
    {
        Timer timer = new Timer();
        if(OccurrenceCount == 1)
        {
            if(outputStream != null)
            {
                timer.schedule(new ExecuteCommandTask(Text, 1, outputStream), StartInTime);
            }
            else
            {
                timer.schedule(new ExecuteCommandTask(Text, 1), StartInTime);
            }
        }
        else if(OccurrenceCount >= 1)
        {
            if(outputStream != null)
            {
                timer.schedule(new ExecuteCommandTask(Text, OccurrenceCount, outputStream), StartInTime, OccurrenceInterval);
            }
            else
            {
                timer.schedule(new ExecuteCommandTask(Text, 1), StartInTime, OccurrenceInterval);
            }
        }
        
    }
    
    public static String sanitize(String commandStr, String commandName)
    {
        //commandStr = commandStr.replaceAll("[\\t\\n\\r\\s]+","");
        commandStr = commandStr.replace(commandName + "(", "");
        //commandStr = commandStr.replace("(", "");
        //commandStr = commandStr.replaceAll("[)]", "");
        //commandStr = commandStr.replaceAll("\"", "");
        //commandStr = commandStr.replaceAll(" ", "");
        
        StringBuilder tempString = new StringBuilder();
        for(int i = 0; i < commandStr.length(); i++)
        {
            if(commandStr.charAt(i) != ' ' && commandStr.charAt(i) != '"' && commandStr.charAt(i) != ')')
            {
                tempString.append(commandStr.charAt(i));
            }
        }
        
        return tempString.toString();
    }
   
    public static String resetIfRandomized(String commandStr, String prefix){
        if(commandStr.toLowerCase().contains("random("))
        {
            return (prefix + "_" + Cryptography.generateRandomAlphanumericString(10));
        }
        return commandStr;
    }
    
    public static int findUserSearchIndexByUserIdString(String UserString)
    {
        int foundIndex = -1;
        String toFindUser = UserString.replaceAll("\"", "");

        //Add EHR for a Random() user
        if(toFindUser.toLowerCase().contains("random("))
        {
            
            foundIndex = random.nextInt(AppData.Users.size());
        }
        else //Add EHR for an specific user
        {
            for(int j = 0; j < AppData.Users.size(); j++)
            {
                if(AppData.Users.get(j).shortUserId.equals(toFindUser) || AppData.Users.get(j).HashedPublicKey.equals(toFindUser))
                {
                    foundIndex = j;
                    break;
                }
            }
        }
        
        return foundIndex;
    }
    
    class ExecuteCommandTask extends TimerTask
    {
        String commandStrOriginal;
        String commandStr;
        JTextArea outputStream;
        public int Limit;
        public int executionNumber;
        
        public ExecuteCommandTask(String commandString, int limit)
        {
            commandStrOriginal = commandString;
            commandStr = commandString;
            outputStream = null;
            Limit = limit;
            executionNumber = limit;
        }

        public ExecuteCommandTask(String commandString, int limit, JTextArea outputTextArea)
        {
            commandStrOriginal = commandString;
            commandStr = commandString;
            outputStream = outputTextArea;
            Limit = limit;
            executionNumber = limit;
        }
        
        public void outputPrintLine(String text)
        {
            //System.out.println(text + "\n");
            if(outputStream != null)
            {
                outputStream.append(text + "\n");
            }
            else
            {
                System.out.println(text + "\n");
            }
        }

        @Override
        public void run() 
        {
            //check number of excecutions
            if(this.Limit <= 0)
            {
                this.cancel();
                return;
            }
            this.Limit--;
            
            //check if the thread is disabled in UI
            if(AppData.continueTaskExecution == 0) //pause this thread
            {
                try
                {
                    Thread.sleep(1000);
                }
                catch (Exception e) {}
                return;
            }
            else if (AppData.continueTaskExecution == -1) //terminate this thread
            {
                outputPrintLine("Task " + commandStr + " is terminated.");
                this.cancel();
                return;
            }
            
            //parse commands
            if(commandStrOriginal.contains("createGenesis()"))
            {
                Node n = new Node();
                n.createGenesis();
                outputPrintLine("Genesis was created successfully.");
            }
            else if(commandStrOriginal.contains("createUser("))
            {
                commandStr = sanitize(commandStr, "createUser");             
                var command = commandStr.split("=");

                String[] parameters;
                String UserShortNameToTrickCompiler = "";

                if(command.length == 2) //a user variable is set
                {
                    boolean exists = AppData.doesUserExist(command[0]);
                    if(!exists)
                    {
                        UserShortNameToTrickCompiler = command[0];
                    }
                    else
                    {
                        UserShortNameToTrickCompiler = command[0] + "_" + String.valueOf(random.nextInt(0,99999));
                    }
                    parameters = command[1].split(",");
                }
                else
                {
                    UserShortNameToTrickCompiler = "";
                    parameters = commandStr.split(",");
                }
                if(executionNumber > 1) // when we are creating multiple users, even if a variable for the user is defined, we will ignore the variable
                {
                    UserShortNameToTrickCompiler = "";
                }
                
                final String UserVar = UserShortNameToTrickCompiler; //the user variable that we can use to call it from application

                try
                {
                    User.UserRole Role = User.UserRole.Patient;
                    if(parameters.length >= 1)
                    {
                        if(parameters[4].contains("Doctor"))
                        {
                            Role = User.UserRole.Doctor;
                        }
                        else if(parameters[4].contains("Hospital"))
                        {
                            Role = User.UserRole.Hospital;
                        }   
                    }

                    User4UI u = new User4UI(Role);

                    //set user details
                    String fName = "fname";
                    String lName = "lname";
                    String address = "address";
                    int dateD = 10;                       
                    int dateM = 06;
                    int dateY = 1996;                     
                    if(parameters.length >= 5)
                    {
                        fName = resetIfRandomized(parameters[1], "fName");
                        lName = resetIfRandomized(parameters[2], "lName");
                        address = resetIfRandomized(parameters[3], "address");
                        var date = parameters[4].split("-");
                        dateD = Integer.parseInt(date[2]);
                        dateY = Integer.parseInt(date[1]);
                        dateM = Integer.parseInt(date[0]);
                    }                                                
                    u.setUserDetails(fName, lName, address, DateNTime.getDateFromParts(dateD, dateM, dateY));

                    //add user to the list
                    AppData.Users.add(u);       
                    u.createTangleForUser();
                    //outputPrint("User "+ u.HashedPublicKey + " has successfully initialized his Tangle.\n");

                    if(UserVar.length() >= 1)
                    {
                        u.shortUserId = UserVar;
                        outputPrintLine("User [" + UserVar + "] was added successfully.\nUser's Hashed Public Key is:\n" + u.HashedPublicKey);
                    }
                    else
                    {
                        u.shortUserId = u.HashedPublicKey;
                        outputPrintLine("User was added successfully. User's Hashed Public Key is:\n" + u.HashedPublicKey);
                    }                        
                }
                catch(Exception er)
                {
                    outputPrintLine(er.getMessage());
                }  

            }
            else if(commandStrOriginal.contains("createEHR("))
            {
                commandStr = sanitize(commandStr, "createEHR");
                var command = commandStr.split("=");

                String[] parameters;
                String NodeNameToTrickCompiler = "";

                if(command.length == 2) //a node variable is set
                {
                    boolean exists = AppData.doesNodeExist(command[0]);
                    if(!exists)
                    {
                        NodeNameToTrickCompiler = command[0];
                    }
                    else //an already existing variable name was used, so we add something to its end
                    {
                        NodeNameToTrickCompiler =
                                command[0] + "_" + String.valueOf(random.nextInt(0,99999));
                    }
                    parameters = command[1].split(",");
                }
                else //no variable is set
                {
                    parameters = commandStr.split(",");
                    NodeNameToTrickCompiler = "";
                }
                if(executionNumber > 1) // when we are creating multiple nodes, even if a variable for the node is defined, we will ignore the variable
                {
                    NodeNameToTrickCompiler = "";
                }
                
                final String NodeVar = NodeNameToTrickCompiler; //the variable that we can get the node from

                if(parameters.length < 3)
                {
                    outputPrintLine("Command createEHR() is incomplete");
                    return;
                }

                //finding user
                int foundIndex = -1;
                
                //random user
                if(parameters[0].toLowerCase().contains("random"))
                {
                    foundIndex = random.nextInt(AppData.Users.size());
                }
                else
                {
                    foundIndex = findUserSearchIndexByUserIdString(parameters[0]);
                }

                if(foundIndex >= 0)
                {
                    User4UI currentUserTemp = AppData.Users.get(foundIndex);

                    final User4UI currentUser = currentUserTemp;

                    boolean encryptEHR = false;
                    if(parameters[2].contains("true"))
                    {
                        encryptEHR = true;
                    }
                    //final int finalIndex = foundIndex;
                    final boolean finalEncryptEHR = encryptEHR;
                    String NodeId = "";

                    if(parameters.length == 3)
                    {
                        if(currentUser.Role == User.UserRole.Patient)
                        {
                            try 
                            {
                                NodeId = currentUser.addNodeToTangleAsPatient(resetIfRandomized(parameters[1],"ehr"), finalEncryptEHR, "", "", "", "");

                                outputPrintLine(AppData.addNodeAndGetOutput(NodeId, NodeVar));                                      
                            }
                            catch(Exception er) {
                                outputPrintLine(er.getMessage());
                                return;
                            }  
                        }
                        else
                        {
                            outputPrintLine("Command createEHR() is incomplete");
                            return;
                        }                          
                    }
                    else if (parameters.length == 4)
                    {
                        try 
                        {
                            if(currentUser.Role == User.UserRole.Patient)
                            {
                                NodeId = currentUser.addNodeToTangleAsPatient(resetIfRandomized(parameters[1],"ehr"), finalEncryptEHR, "", "", "", "");         

                                outputPrintLine(AppData.addNodeAndGetOutput(NodeId, NodeVar)); 
                            }
                            else if (currentUser.Role == User.UserRole.Doctor)
                            {
                                //find patient to find the latest patientId
                                var patient = AppData.findUserFromList(parameters[3]);
                                if(patient != null)
                                {
                                    NodeId = currentUser.addNodeToTangleAsDoctor(resetIfRandomized(parameters[1],"ehr"), finalEncryptEHR, patient.MyPreviousNodeId, patient.HashedPublicKey, "", ""); 
                                    AppData.Users.get(patient.userIndex).MyPreviousNodeId = NodeId;

                                    outputPrintLine(AppData.addNodeAndGetOutput(NodeId, NodeVar)); 
                                }
                                else
                                {
                                    outputPrintLine("Error happened at Command createEHR(). Patient was not found.");
                                    return;
                                }
                            }

                        }
                        catch(Exception er) {
                            outputPrintLine(er.getMessage());
                            return;
                        }  
                    }
                    else if (parameters.length == 5)
                    {
                        try 
                        {
                            if(currentUser.Role == User.UserRole.Hospital)
                            {

                                //find patient
                                var patient = AppData.findUserFromList(parameters[3]);

                                //find doctor
                                var doctor = AppData.findUserFromList(parameters[4]);

                                if(patient != null && doctor != null)
                                {
                                    NodeId = currentUser.addNodeToTangleAsHospital(resetIfRandomized(parameters[1],"ehr"), finalEncryptEHR, patient.MyPreviousNodeId, patient.HashedPublicKey, doctor.MyPreviousNodeId, doctor.HashedPublicKey); 
                                    AppData.Users.get(patient.userIndex).MyPreviousNodeId = NodeId;
                                    AppData.Users.get(doctor.userIndex).MyPreviousNodeId = NodeId;

                                    outputPrintLine(AppData.addNodeAndGetOutput(NodeId, NodeVar)); 
                                }
                                else
                                {
                                    outputPrintLine("Error happened at Command createEHR(). Patient/Doctor is not found.");
                                    return;
                                }
                            }
                            else if (currentUser.Role == User.UserRole.Doctor)
                            {
                                //find patient
                                var patient = AppData.findUserFromList(parameters[3]);
                                if(patient != null)
                                {
                                    NodeId = currentUser.addNodeToTangleAsDoctor(resetIfRandomized(parameters[1],"ehr"), finalEncryptEHR, patient.MyPreviousNodeId, patient.HashedPublicKey, "", ""); 
                                    AppData.Users.get(patient.userIndex).MyPreviousNodeId = NodeId;

                                    outputPrintLine(AppData.addNodeAndGetOutput(NodeId, NodeVar)); 
                                }
                                else
                                {
                                    outputPrintLine("Error happened at Command createEHR(). Patient is not found.");
                                    return;
                                }
                            }
                            else if (currentUser.Role == User.UserRole.Patient)
                            {
                                //find doctor
                                var doctor = AppData.findUserFromList(parameters[4]);

                                if(doctor != null)
                                {
                                    NodeId = currentUser.addNodeToTangleAsPatient(resetIfRandomized(parameters[1],"ehr"), finalEncryptEHR, doctor.MyPreviousNodeId, doctor.HashedPublicKey, "", ""); 
                                    AppData.Users.get(doctor.userIndex).MyPreviousNodeId = NodeId;

                                    outputPrintLine(AppData.addNodeAndGetOutput(NodeId, NodeVar)); 
                                }
                                else
                                {
                                    outputPrintLine("Error happened at Command createEHR(). Doctor is not found.");
                                    return;
                                }
                            }

                        }
                        catch(Exception er) {
                            outputPrintLine(er.getMessage());
                            return;
                        }  
                    }
                    else if (parameters.length == 6)
                    {
                        try 
                        {
                            if(currentUser.Role == User.UserRole.Hospital)
                            {
                                //find patient
                                var patient = AppData.findUserFromList(parameters[3]);

                                //find doctor
                                var doctor = AppData.findUserFromList(parameters[4]);

                                if(patient != null && doctor != null)
                                {
                                    NodeId = currentUser.addNodeToTangleAsHospital(resetIfRandomized(parameters[1],"ehr"), finalEncryptEHR, patient.MyPreviousNodeId, patient.HashedPublicKey, doctor.MyPreviousNodeId, doctor.HashedPublicKey); 
                                    AppData.Users.get(patient.userIndex).MyPreviousNodeId = NodeId;
                                    AppData.Users.get(doctor.userIndex).MyPreviousNodeId = NodeId;

                                    outputPrintLine(AppData.addNodeAndGetOutput(NodeId, NodeVar)); 
                                }
                                else
                                {
                                    outputPrintLine("Error happened at Command createEHR(). Patient/Doctor is not found.");
                                    return;
                                }
                            }
                            else if (currentUser.Role == User.UserRole.Doctor)
                            {
                                //find patient
                                var patient = AppData.findUserFromList(parameters[3]);

                                //find hospital
                                var hospital = AppData.findUserFromList(parameters[5]);

                                if(patient != null && hospital != null)
                                {
                                    NodeId = currentUser.addNodeToTangleAsDoctor(resetIfRandomized(parameters[1],"ehr"), finalEncryptEHR, patient.MyPreviousNodeId, patient.HashedPublicKey, hospital.MyPreviousNodeId , hospital.HashedPublicKey); 
                                    AppData.Users.get(patient.userIndex).MyPreviousNodeId = NodeId;
                                    AppData.Users.get(hospital.userIndex).MyPreviousNodeId = NodeId;

                                    outputPrintLine(AppData.addNodeAndGetOutput(NodeId, NodeVar)); 
                                }
                                else
                                {
                                    outputPrintLine("Error happened at Command createEHR(). Patient is not found.");
                                    return;
                                }
                            }
                            else if (currentUser.Role == User.UserRole.Patient)
                            {
                                //find doctor
                                var doctor = AppData.findUserFromList(parameters[4]);

                                //find hospital
                                var hospital = AppData.findUserFromList(parameters[5]);

                                if(doctor != null && hospital != null)
                                {
                                    NodeId = currentUser.addNodeToTangleAsPatient(resetIfRandomized(parameters[1],"ehr"), finalEncryptEHR, doctor.MyPreviousNodeId, doctor.HashedPublicKey, hospital.MyPreviousNodeId , hospital.HashedPublicKey); 
                                    AppData.Users.get(doctor.userIndex).MyPreviousNodeId = NodeId;
                                    AppData.Users.get(hospital.userIndex).MyPreviousNodeId = NodeId;

                                    outputPrintLine(AppData.addNodeAndGetOutput(NodeId, NodeVar)); 
                                }
                                else
                                {
                                    outputPrintLine("Error happened at Command createEHR(). Doctor is not found.");
                                    return;
                                }
                            }
                        }
                        catch(Exception er) {
                            outputPrintLine(er.getMessage());
                            return;
                        }  
                    }

                    //set new cumulative weights after we added a node
                    currentUser.calculateCumulativeWeightsAfterAddingANode(NodeId);

                    //StaticVariables.Users.get(foundIndex).addNodeToTangle(NodeName, rootPaneCheckingEnabled, toFindUser, toFindUser, toFindUser, toFindUser, toFindUser, toFindUser)
                }
                else
                {
                    outputPrintLine("In createEHR(), User " + parameters[0] + " was not found");
                    return;
                }

            }
            else if(commandStrOriginal.contains("printEHR("))
            {
                commandStr = sanitize(commandStr, "printEHR");

                var parameters = commandStr.split(",");
                if(parameters.length == 2)
                {
                    var simpleUser = AppData.findUserFromList(parameters[0]);
                    if(simpleUser != null)
                    {
                        var user = AppData.Users.get(simpleUser.userIndex);
                        String NodeId = parameters[1];

                        var SimpleNode = AppData.findNodeFromList(NodeId);
                        if(SimpleNode != null)
                        {
                            NodeId = SimpleNode.NodeId;
                        }

                        var node = user.findNodeById(NodeId);
                        if (node != null)
                        {
                            outputPrintLine(node.toString()+"");
                        }
                        else
                        {
                            outputPrintLine("Node was not found.");
                        }
                    }
                }
                else
                {
                    outputPrintLine("Error happened at printEHR(). Only 2 Paramentes should be provided.");
                }
            }
            else if(commandStrOriginal.contains("printTangle("))
            {
                commandStr = sanitize(commandStr, "printTangle");
                var parameters = commandStr.split(",");

                var simpleUser = AppData.findUserFromList(parameters[0]);
                if(simpleUser != null)
                {
                    var user = AppData.Users.get(simpleUser.userIndex);

                    if(parameters.length == 1)
                    {
                        outputPrintLine("User [" + simpleUser.shortUserId +"]'s Tangle has " + user.MyTangle.DAG.size() + " nodes:");
                        outputPrintLine(Tangle.DagToString(user.MyTangle.DAG, user.HashedPublicKey, false));
                    }
                    else if (parameters.length == 2)
                    {
                        outputPrintLine("User [" + simpleUser.shortUserId +"]'s Tangle has " + user.MyTangle.DAG.size() + " nodes:");

                        if(parameters[1].equalsIgnoreCase("true"))
                            outputPrintLine(Tangle.DagToString(user.MyTangle.DAG, user.HashedPublicKey, true));
                        else
                            outputPrintLine(Tangle.DagToString(user.MyTangle.DAG, user.HashedPublicKey, false));
                    }
                    else if(parameters.length == 3)
                    {
                        outputPrintLine("User [" + simpleUser.shortUserId +"]'s Tangle has " + user.MyTangle.DAG.size() + " nodes:");

                        boolean onlyMyNodes = false;
                        if(parameters[1].equalsIgnoreCase("true"))
                            onlyMyNodes = true;
                        int limit = -1;
                        try{
                            limit = Integer.parseInt(parameters[2]);
                        }
                        catch(Exception ee)
                        {
                            outputPrintLine("Warning: Limit was not a number at ShowTangle(). MaximumValue is Used.");
                        }
                        if(limit <= 0)
                        {
                            var temp = Tangle.DagToString(user.MyTangle.DAG, user.HashedPublicKey, onlyMyNodes);
                            outputPrintLine(temp);
                        }                          
                        else
                        {
                            var temp = Tangle.DagToString(user.MyTangle.DAG, user.HashedPublicKey, onlyMyNodes, limit);
                            outputPrintLine(temp);
                        }
                            
                    }
                    else
                    {
                        outputPrintLine("Error happened at printTangle(). Paramenter counts Mismatch.");
                    }
                }
            }
            else if(commandStrOriginal.contains("visualizeTangle("))
            {
                commandStr = sanitize(commandStr, "visualizeTangle");
                var parameters = commandStr.split(",");

                var simpleUser = AppData.findUserFromList(parameters[0]);
                if(simpleUser != null)
                {
                    var user = AppData.Users.get(simpleUser.userIndex);

                    if(parameters.length == 1)
                    {
                        outputPrintLine("User [" + simpleUser.shortUserId +"]'s Tangle has " + user.MyTangle.DAG.size() + " nodes:");
                        DAGViewer dv = new DAGViewer(Tangle.ExportTangle(user.MyTangle.DAG, user.HashedPublicKey, false));
                        dv.visualize();
                    }
                    else if (parameters.length == 2)
                    {
                        outputPrintLine("User [" + simpleUser.shortUserId +"]'s Tangle has " + user.MyTangle.DAG.size() + " nodes:");

                        DAGViewer dv;
                        
                        if(parameters[1].equalsIgnoreCase("true"))
                            dv = new DAGViewer(Tangle.ExportTangle(user.MyTangle.DAG, user.HashedPublicKey, true));
                        else
                            dv = new DAGViewer(Tangle.ExportTangle(user.MyTangle.DAG, user.HashedPublicKey, false));
                        
                        dv.visualize();
                        
                    }
                    else if(parameters.length == 3)
                    {
                        outputPrintLine("User [" + simpleUser.shortUserId +"]'s Tangle has " + user.MyTangle.DAG.size() + " nodes:");

                        boolean onlyMyNodes = false;
                        if(parameters[1].equalsIgnoreCase("true"))
                            onlyMyNodes = true;
                        int limit = -1;
                        try{
                            limit = Integer.parseInt(parameters[2]);
                        }
                        catch(Exception ee)
                        {
                            outputPrintLine("Warning: Limit was not a number at ShowTangle(). MaximumValue is Used.");
                        }
                        if(limit <= 0)
                        {
                            DAGViewer dv = new DAGViewer(Tangle.ExportTangle(user.MyTangle.DAG, user.HashedPublicKey, onlyMyNodes));
                            dv.visualize();
                        }                          
                        else
                        {
                            DAGViewer dv = new DAGViewer(Tangle.ExportTangle(user.MyTangle.DAG, user.HashedPublicKey, onlyMyNodes, limit));
                            dv.visualize();
                        }
                            
                    }
                    else
                    {
                        outputPrintLine("Error happened at printTangle(). Paramenter counts Mismatch.");
                    }
                }
            }
            else if(commandStrOriginal.contains("advertiseTangle("))
            {
                commandStr = Command.sanitize(commandStr, "advertiseTangle");
                var parameters = commandStr.split(",");
                
                if(parameters.length == 0 || commandStr.isBlank()) //everyone advertise to everyone
                {
                    for(var sender : AppData.Users)
                    {
                        for(var receiver : AppData.Users)
                        {
                            if(!sender.shortUserId.equals(receiver.shortUserId))
                                receiver.receiveTangleAndUpdateSelf(sender.advertiseTangle());
                        }
                    }
                    outputPrintLine("Tangle advertisment (from everyone to all) was completed successfully.");
                    return;
                }
                
                int senderId = findUserSearchIndexByUserIdString(parameters[0]);
                if(senderId < 0)
                {
                    outputPrintLine("Error happened at advertiseTangle(). Sender user was not found.");
                    return;
                }
                var senderUser = AppData.Users.get(senderId);

                if(parameters.length == 1) //sender will advertise its tangle to all
                {
                    for(var receiver : AppData.Users)
                    {
                        receiver.receiveTangleAndUpdateSelf(senderUser.advertiseTangle());
                    }
                    outputPrintLine("Tangle advertisment (to all) was completed successfully by User ["+ parameters[0] + "].");
                }
                else if(parameters.length == 2) //sender will advertise its tangle to specific users
                {
                    if(parameters[1].isBlank()) //same as advertise to all
                    {
                        for(var receiver : AppData.Users)
                        {
                            receiver.receiveTangleAndUpdateSelf(senderUser.advertiseTangle());
                        }
                    }
                    else
                    {
                        int receiverId = findUserSearchIndexByUserIdString(parameters[1]);
                        if(receiverId < 0)
                        {
                            outputPrintLine("Error happened at advertiseTangle(). Receiver user was not found.");
                            return;
                        }
                        var receiverUser = AppData.Users.get(receiverId);
                        receiverUser.receiveTangleAndUpdateSelf(senderUser.advertiseTangle());
                    }
                    outputPrintLine("Tangle advertisment was completed successfully by User ["+ parameters[0] + "].");
                }
                else
                {
                    outputPrintLine("Error happened at advertiseTangle(). Only 1 or 2 Paramentes should be provided.");
                    return;
                }
            }
            else if(commandStrOriginal.contains("setTangle("))
            {
                commandStr = Command.sanitize(commandStr, "setTangle");
                var parameters = commandStr.split(",");
                var sender = AppData.findUserFromList(parameters[0]);
                if(sender == null)
                {
                    outputPrintLine("Error happened at initializeTangle(). User was not found.");
                    return;
                }
                var senderUser = AppData.Users.get(sender.userIndex);

                if(parameters.length == 2)
                {
                    if(parameters[1].toLowerCase().equals("random"))
                    {
                        //setting user's tangle according to a random user's tangle
                        //senderUser.MyTangle.DAG = StaticVariables.Users.get(r.nextInt(0, StaticVariables.Users.size())).MyTangle.DAG;
                        int RandomId = random.nextInt(0, AppData.Users.size());
                        senderUser.MyTangle.SetTangleClone(AppData.Users.get(RandomId).MyTangle.DAG);
                    }
                    else //all
                    {
                        ArrayList<ArrayList<Node>> dags = new ArrayList();
                        for(int i = AppData.Users.size() - 1; i >= 0; i--)
                        {
                            dags.add(AppData.Users.get(i).MyTangle.DAG);
                        }
                        //senderUser.MyTangle.DAG = Tangle.selectBestTangle(dags);
                        senderUser.MyTangle.SetTangleClone(Tangle.selectBestTangle(dags));
                    }
                    outputPrintLine("User tangle was updated.");
                }
                else if(parameters.length == 4)
                {
                    try
                    {
                        boolean isRandom = false;
                        int startIndex = Integer.parseInt(parameters[2]);
                        int limit = Integer.parseInt(parameters[3]);
                        if(parameters[1].toLowerCase().equals("random"))
                        {
                            isRandom = true;
                        }
                        ArrayList<ArrayList<Node>> dags = new ArrayList();
                        
                        if(startIndex >= 0 || startIndex + limit < AppData.Users.size())
                        {
                            if(!isRandom)
                            {
                                for(int i = startIndex; i < startIndex + limit; i++)
                                {
                                    dags.add(AppData.Users.get(i).MyTangle.DAG);
                                }
                            }
                            else
                            {
                                ArrayList<Integer> selectedIndexes = new ArrayList();
                                for(int i = startIndex; i < AppData.Users.size(); i++)
                                {
                                    int randomInt = random.nextInt(i, AppData.Users.size());
                                    if(!selectedIndexes.contains(randomInt))
                                    {
                                       dags.add(AppData.Users.get(i).MyTangle.DAG);
                                       selectedIndexes.add(i);
                                       limit--;
                                       if(limit == 0) break;
                                    }
                                    
                                }
                            }
                            
                            //set tangle
                            //senderUser.MyTangle.DAG = Tangle.selectBestTangle(dags);
                            senderUser.MyTangle.SetTangleClone(Tangle.selectBestTangle(dags));
                            outputPrintLine("User tangle was updated.");
                        }
                        else
                        {
                            outputPrintLine("Error happened at initializeTangle(). startIndex/limit was wrong");
                        }  
                    }
                    catch (Exception ee)
                    {
                        outputPrintLine("Exception happened at initializeTangle(). " + ee.getMessage());
                    } 
                }
                else
                {
                    outputPrintLine("Error happened at initializeTangle(). 4 Paramentes should be provided.");
                }
            }
            else if(commandStrOriginal.contains("listUser("))
            {
                commandStr = Command.sanitize(commandStr, "listUser");
                String Output = "\"List Users\":[";
                if(commandStr.length() > 0)
                {
                    try
                    {
                        var command = commandStr.split(",");
                        if(command.length == 1) 
                        {
                            int limit = Integer.parseInt(command[0]);
                            if(limit >= AppData.Users.size()) limit = AppData.Users.size();
                            for(int i = 0; i < limit; i++)
                            {
                                Output += "{\"" + AppData.Users.get(i).shortUserId + ", " + AppData.Users.get(i).Role.toString() + "\"}\n";
                                if(i != limit - 1)
                                {
                                    Output += ", ";
                                }
                            }                           
                        }
                        else if (command.length == 2)
                        {
                            int limit = Integer.parseInt(command[0]);
                            
                            if(command[1].toLowerCase().equals("false"))
                            {
                                limit = AppData.Users.size() - limit;
                                if(limit < 0) limit = 0;

                                for(int i = AppData.Users.size() - 1; i >= limit; i--)
                                {
                                    Output += "{\"" + AppData.Users.get(i).shortUserId + ", " + AppData.Users.get(i).Role.toString() + "\"}\n";
                                    if(i != limit)
                                    {
                                        Output += ", ";
                                    }
                                }
                            }
                            else
                            {
                                if(limit >= AppData.Users.size()) limit = AppData.Users.size();
                                for(int i = 0; i < limit; i++)
                                {
                                    Output += "{\"" + AppData.Users.get(i).shortUserId + ", " + AppData.Users.get(i).Role.toString() + "\"}\n";
                                    if(i != limit - 1)
                                    {
                                        Output += ", ";
                                    }
                                }
                            }
                        }
                        else
                        {
                            outputPrintLine("An Error happened at ListUser(). Wrong number of parameters were provided");
                            return;
                        }
                    }
                    catch (Exception e) 
                    {
                        outputPrintLine("An Error happened at ListUser(). Can't convert limit to an integer");
                        return;
                    }
                }
                else
                {
                    for(int i = 0; i < AppData.Users.size(); i++)
                    {
                        if(i != 0)
                        {
                            Output += ", \"" + AppData.Users.get(i).shortUserId + "\"";
                        }
                        else
                        {
                            Output += "\"" + AppData.Users.get(i).shortUserId + "\"";
                        }
                    }   
                }
                Output += "]";
                outputPrintLine(Output);
            }
            else if(commandStrOriginal.contains("listNode("))
            {
                commandStr = Command.sanitize(commandStr, "listNode");
                String Output = "\"List Nodes\":[";
                if(commandStr.length() > 0)
                {
                    try
                    {
                        var command = commandStr.split(",");
                        if(command.length == 1) 
                        {
                            int limit = Integer.parseInt(command[0]);
                            if(limit >= AppData.Nodes.size()) limit = AppData.Nodes.size();
                            for(int i = 0; i < limit; i++)
                            {
                                Output += "\"" + AppData.Nodes.get(i).shortNodeId + "\"";
                                if(i != limit - 1)
                                {
                                    Output += ", ";
                                }
                            }                           
                        }
                        else if (command.length == 2)
                        {
                            int limit = Integer.parseInt(command[0]);
                            
                            if(command[1].toLowerCase().equals("false"))
                            {
                                limit = AppData.Users.size() - limit;
                                if(limit < 0) limit = 0;

                                for(int i = AppData.Nodes.size() - 1; i >= limit; i--)
                                {
                                    Output += "\"" + AppData.Nodes.get(i).shortNodeId + "\"";
                                    if(i != limit)
                                    {
                                        Output += ", ";
                                    }
                                }
                            }
                            else
                            {
                                if(limit >= AppData.Nodes.size()) limit = AppData.Nodes.size();
                                for(int i = 0; i < limit; i++)
                                {
                                    Output += "\"" + AppData.Nodes.get(i).shortNodeId + "\"";
                                    if(i != limit - 1)
                                    {
                                        Output += ", ";
                                    }
                                }
                            }
                        }
                        else
                        {
                            outputPrintLine("An Error happened at ListUser(). Wrong number of parameters were provided");
                            return;
                        }
                    }
                    catch (Exception e) 
                    {
                        outputPrintLine("An Error happened at ListUser(). Can't convert limit to an integer");
                        return;
                    }
                }
                else
                {
                    for(int i = 0; i < AppData.Nodes.size(); i++)
                    {
                        if(i != 0)
                        {
                            Output += ", \"" + AppData.Nodes.get(i).shortNodeId + "\"";
                        }
                        else
                        {
                            Output += "\"" + AppData.Nodes.get(i).shortNodeId + "\"";
                        }
                    }   
                }
                Output += "]";
                outputPrintLine(Output);
            }
        }
    }   
}
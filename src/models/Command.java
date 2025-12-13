package models;

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
import models.StaticVariables;

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
            if(StaticVariables.continueTaskExecution == 0) //pause this thread
            {
                try
                {
                    Thread.sleep(1000);
                }
                catch (Exception e) {}
                return;
            }
            else if (StaticVariables.continueTaskExecution == -1) //terminate this thread
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

                if(command.length == 2)
                {
                    UserShortNameToTrickCompiler = command[0];
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
                    StaticVariables.Users.add(u);       
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

                if(command.length == 2)
                {
                    NodeNameToTrickCompiler = command[0];
                    parameters = command[1].split(",");
                }
                else
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
                }
                else 
                {
                    //finding user
                    int foundIndex = -1;
                    String toFindUser = parameters[0].replaceAll("\"", "");
                    for(int j = 0; j < StaticVariables.Users.size(); j++)
                    {
                        if(StaticVariables.Users.get(j).shortUserId.equals(toFindUser) || StaticVariables.Users.get(j).HashedPublicKey.equals(toFindUser))
                        {
                            foundIndex = j;
                            break;
                        }
                    }
                    if(foundIndex >= 0)
                    {
                        User4UI currentUserTemp = StaticVariables.Users.get(foundIndex);

                        final User4UI currentUser = currentUserTemp;

                        boolean encryptEHR = false;
                        if(parameters[2].contains("true"))
                        {
                            encryptEHR = true;
                        }
                        //final int finalIndex = foundIndex;
                        final boolean finalEncryptEHR = encryptEHR;

                        if(parameters.length == 3)
                        {
                            if(currentUser.Role == User.UserRole.Patient)
                            {
                                try 
                                {
                                    String NodeId = currentUser.addNodeToTangleAsPatient(resetIfRandomized(parameters[1],"ehr"), finalEncryptEHR, "", "", "", "");
                                    
                                    outputPrintLine(StaticVariables.addNodeAndGetOutput(NodeId, NodeVar));                                      
                                }
                                catch(Exception er) {
                                    outputPrintLine(er.getMessage());
                                }  
                            }
                            else
                            {
                                outputPrintLine("Command createEHR() is incomplete");
                            }                          
                        }
                        else if (parameters.length == 4)
                        {
                            try 
                            {
                                if(currentUser.Role == User.UserRole.Patient)
                                {
                                    String NodeId = currentUser.addNodeToTangleAsPatient(resetIfRandomized(parameters[1],"ehr"), finalEncryptEHR, "", "", "", "");         

                                    outputPrintLine(StaticVariables.addNodeAndGetOutput(NodeId, NodeVar)); 
                                }
                                else if (currentUser.Role == User.UserRole.Doctor)
                                {
                                    //find patient to find the latest patientId
                                    var patient = StaticVariables.findUserFromList(parameters[3]);
                                    if(patient != null)
                                    {
                                        String NodeId = currentUser.addNodeToTangleAsDoctor(resetIfRandomized(parameters[1],"ehr"), finalEncryptEHR, patient.MyPreviousNodeId, patient.HashedPublicKey, "", ""); 
                                        StaticVariables.Users.get(patient.userIndex).MyPreviousNodeId = NodeId;

                                        outputPrintLine(StaticVariables.addNodeAndGetOutput(NodeId, NodeVar)); 
                                    }
                                    else
                                    {
                                        outputPrintLine("Error happened at Command createEHR(). Patient was not found.");
                                    }
                                }

                            }
                            catch(Exception er) {
                                outputPrintLine(er.getMessage());
                            }  
                        }
                        else if (parameters.length == 5)
                        {
                            try 
                            {
                                if(currentUser.Role == User.UserRole.Hospital)
                                {

                                    //find patient
                                    var patient = StaticVariables.findUserFromList(parameters[3]);

                                    //find doctor
                                    var doctor = StaticVariables.findUserFromList(parameters[4]);

                                    if(patient != null && doctor != null)
                                    {
                                        String NodeId = currentUser.addNodeToTangleAsHospital(resetIfRandomized(parameters[1],"ehr"), finalEncryptEHR, patient.MyPreviousNodeId, patient.HashedPublicKey, doctor.MyPreviousNodeId, doctor.HashedPublicKey); 
                                        StaticVariables.Users.get(patient.userIndex).MyPreviousNodeId = NodeId;
                                        StaticVariables.Users.get(doctor.userIndex).MyPreviousNodeId = NodeId;

                                        outputPrintLine(StaticVariables.addNodeAndGetOutput(NodeId, NodeVar)); 
                                    }
                                    else
                                    {
                                        outputPrintLine("Error happened at Command createEHR(). Patient/Doctor is not found.");
                                    }
                                }
                                else if (currentUser.Role == User.UserRole.Doctor)
                                {
                                    //find patient
                                    var patient = StaticVariables.findUserFromList(parameters[3]);
                                    if(patient != null)
                                    {
                                        String NodeId = currentUser.addNodeToTangleAsDoctor(resetIfRandomized(parameters[1],"ehr"), finalEncryptEHR, patient.MyPreviousNodeId, patient.HashedPublicKey, "", ""); 
                                        StaticVariables.Users.get(patient.userIndex).MyPreviousNodeId = NodeId;

                                        outputPrintLine(StaticVariables.addNodeAndGetOutput(NodeId, NodeVar)); 
                                    }
                                    else
                                    {
                                        outputPrintLine("Error happened at Command createEHR(). Patient is not found.");
                                    }
                                }
                                else if (currentUser.Role == User.UserRole.Patient)
                                {
                                    //find doctor
                                    var doctor = StaticVariables.findUserFromList(parameters[4]);

                                    if(doctor != null)
                                    {
                                        String NodeId = currentUser.addNodeToTangleAsPatient(resetIfRandomized(parameters[1],"ehr"), finalEncryptEHR, doctor.MyPreviousNodeId, doctor.HashedPublicKey, "", ""); 
                                        StaticVariables.Users.get(doctor.userIndex).MyPreviousNodeId = NodeId;

                                        outputPrintLine(StaticVariables.addNodeAndGetOutput(NodeId, NodeVar)); 
                                    }
                                    else
                                    {
                                        outputPrintLine("Error happened at Command createEHR(). Doctor is not found.");
                                    }
                                }

                            }
                            catch(Exception er) {
                                outputPrintLine(er.getMessage());
                            }  
                        }
                        else if (parameters.length == 6)
                        {
                            try 
                            {
                                if(currentUser.Role == User.UserRole.Hospital)
                                {
                                    //find patient
                                    var patient = StaticVariables.findUserFromList(parameters[3]);

                                    //find doctor
                                    var doctor = StaticVariables.findUserFromList(parameters[4]);

                                    if(patient != null && doctor != null)
                                    {
                                        String NodeId = currentUser.addNodeToTangleAsHospital(resetIfRandomized(parameters[1],"ehr"), finalEncryptEHR, patient.MyPreviousNodeId, patient.HashedPublicKey, doctor.MyPreviousNodeId, doctor.HashedPublicKey); 
                                        StaticVariables.Users.get(patient.userIndex).MyPreviousNodeId = NodeId;
                                        StaticVariables.Users.get(doctor.userIndex).MyPreviousNodeId = NodeId;

                                        outputPrintLine(StaticVariables.addNodeAndGetOutput(NodeId, NodeVar)); 
                                    }
                                    else
                                    {
                                        outputPrintLine("Error happened at Command createEHR(). Patient/Doctor is not found.");
                                    }
                                }
                                else if (currentUser.Role == User.UserRole.Doctor)
                                {
                                    //find patient
                                    var patient = StaticVariables.findUserFromList(parameters[3]);

                                    //find hospital
                                    var hospital = StaticVariables.findUserFromList(parameters[5]);

                                    if(patient != null && hospital != null)
                                    {
                                        String NodeId = currentUser.addNodeToTangleAsDoctor(resetIfRandomized(parameters[1],"ehr"), finalEncryptEHR, patient.MyPreviousNodeId, patient.HashedPublicKey, hospital.MyPreviousNodeId , hospital.HashedPublicKey); 
                                        StaticVariables.Users.get(patient.userIndex).MyPreviousNodeId = NodeId;
                                        StaticVariables.Users.get(hospital.userIndex).MyPreviousNodeId = NodeId;

                                        outputPrintLine(StaticVariables.addNodeAndGetOutput(NodeId, NodeVar)); 
                                    }
                                    else
                                    {
                                        outputPrintLine("Error happened at Command createEHR(). Patient is not found.");
                                    }
                                }
                                else if (currentUser.Role == User.UserRole.Patient)
                                {
                                    //find doctor
                                    var doctor = StaticVariables.findUserFromList(parameters[4]);

                                    //find hospital
                                    var hospital = StaticVariables.findUserFromList(parameters[5]);

                                    if(doctor != null && hospital != null)
                                    {
                                        String NodeId = currentUser.addNodeToTangleAsPatient(resetIfRandomized(parameters[1],"ehr"), finalEncryptEHR, doctor.MyPreviousNodeId, doctor.HashedPublicKey, hospital.MyPreviousNodeId , hospital.HashedPublicKey); 
                                        StaticVariables.Users.get(doctor.userIndex).MyPreviousNodeId = NodeId;
                                        StaticVariables.Users.get(hospital.userIndex).MyPreviousNodeId = NodeId;

                                        outputPrintLine(StaticVariables.addNodeAndGetOutput(NodeId, NodeVar)); 
                                    }
                                    else
                                    {
                                        outputPrintLine("Error happened at Command createEHR(). Doctor is not found.");
                                    }
                                }

                            }
                            catch(Exception er) {
                                outputPrintLine(er.getMessage());
                            }  
                        }

                        //StaticVariables.Users.get(foundIndex).addNodeToTangle(NodeName, rootPaneCheckingEnabled, toFindUser, toFindUser, toFindUser, toFindUser, toFindUser, toFindUser)
                    }
                    else
                    {
                        outputPrintLine("In createEHR(), User " + toFindUser + " was not found");
                    }
                }

            }
            else if(commandStrOriginal.contains("printEHR("))
            {
                commandStr = sanitize(commandStr, "printEHR");

                var parameters = commandStr.split(",");
                if(parameters.length == 2)
                {
                    var simpleUser = StaticVariables.findUserFromList(parameters[0]);
                    if(simpleUser != null)
                    {
                        var user = StaticVariables.Users.get(simpleUser.userIndex);
                        String NodeId = parameters[1];

                        var SimpleNode = StaticVariables.findNodeFromList(NodeId);
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

                var simpleUser = StaticVariables.findUserFromList(parameters[0]);
                if(simpleUser != null)
                {
                    var user = StaticVariables.Users.get(simpleUser.userIndex);

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
            else if(commandStrOriginal.contains("advertiseTangle("))
            {
                commandStr = Command.sanitize(commandStr, "advertiseTangle");
                var parameters = commandStr.split(",");
                var sender = StaticVariables.findUserFromList(parameters[0]);
                if(sender == null)
                {
                    outputPrintLine("Error happened at advertiseTangle(). Sender user was not found.");
                    return;
                }
                var senderUser = StaticVariables.Users.get(sender.userIndex);

                if(parameters.length == 1)
                {
                    for(int i = 0; i < StaticVariables.Users.size(); i++)
                    {
                        StaticVariables.Users.get(i).receiveTangleAndUpdateSelf(senderUser.advertiseTangle());
                    }
                    outputPrintLine("Tangle advertisment was completed successfully.");
                }
                else if(parameters.length == 2)
                {
                    if(parameters[1].isBlank())
                    {
                        for(int i = 0; i < StaticVariables.Users.size(); i++)
                        {
                            StaticVariables.Users.get(i).receiveTangleAndUpdateSelf(senderUser.advertiseTangle());
                        }
                    }
                    else
                    {
                        var receiver = StaticVariables.findUserFromList(parameters[1]);
                        if(receiver == null)
                        {
                            outputPrintLine("Error happened at advertiseTangle(). Receiver user was not found.");
                            return;
                        }
                        var receiverUser = StaticVariables.Users.get(receiver.userIndex);
                        receiverUser.receiveTangleAndUpdateSelf(senderUser.advertiseTangle());
                    }
                    outputPrintLine("Tangle advertisment was completed successfully.");
                }
                else
                {
                    outputPrintLine("Error happened at advertiseTangle(). Only 1 or 2 Paramentes should be provided.");
                }
            }
            else if(commandStrOriginal.contains("setTangle("))
            {
                commandStr = Command.sanitize(commandStr, "setTangle");
                var parameters = commandStr.split(",");
                var sender = StaticVariables.findUserFromList(parameters[0]);
                if(sender == null)
                {
                    outputPrintLine("Error happened at initializeTangle(). User was not found.");
                    return;
                }
                var senderUser = StaticVariables.Users.get(sender.userIndex);

                if(parameters.length == 2)
                {
                    if(parameters[1].toLowerCase().equals("random"))
                    {
                        Random r = new Random();
                        //setting user's tangle according to a random user's tangle
                        senderUser.MyTangle.DAG = StaticVariables.Users.get(r.nextInt(0, StaticVariables.Users.size())).MyTangle.DAG;
                    }
                    else //all
                    {
                        ArrayList<ArrayList<Node>> dags = new ArrayList();
                        for(int i = StaticVariables.Users.size() - 1; i >= 0; i--)
                        {
                            dags.add(StaticVariables.Users.get(i).MyTangle.DAG);
                        }
                        senderUser.MyTangle.DAG = Tangle.selectBestTangle(dags);
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
                        
                        if(startIndex >= 0 || startIndex + limit < StaticVariables.Users.size())
                        {
                            if(!isRandom)
                            {
                                for(int i = startIndex; i < startIndex + limit; i++)
                                {
                                    dags.add(StaticVariables.Users.get(i).MyTangle.DAG);
                                }
                            }
                            else
                            {
                                ArrayList<Integer> selectedIndexes = new ArrayList();
                                Random r = new Random();
                                for(int i = startIndex; i < StaticVariables.Users.size(); i++)
                                {
                                    int randomInt = r.nextInt(i, StaticVariables.Users.size());
                                    if(!selectedIndexes.contains(randomInt))
                                    {
                                       dags.add(StaticVariables.Users.get(i).MyTangle.DAG);
                                       selectedIndexes.add(i);
                                       limit--;
                                       if(limit == 0) break;
                                    }
                                    
                                }
                            }
                            
                            //set tangle
                            senderUser.MyTangle.DAG = Tangle.selectBestTangle(dags);
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
        }
    }   
}

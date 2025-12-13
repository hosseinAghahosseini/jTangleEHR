package entities;

import crypto.HashAndSign;
import java.util.ArrayList;

/**
 * @author hosseinAghahosseini
 */

public class Tangle {
    
    public ArrayList<Node> DAG;
    
    public boolean addNode(Node n)
    {
        //check if accepted nodes are valid
        short AcceptCount = 0;
        for(int i = DAG.size() - 1; i >= 0 ; i--)
        {
            if(DAG.get(i).NodeId == n.FirstAcceptedNodeId || DAG.get(i).NodeId == n.SecondAcceptedNodeId)
            {
                AcceptCount++;
            }
            if(AcceptCount >= 2)
                break;
        }
        if(AcceptCount >= 2)
        {
            DAG.add(n);
            return true;
        }
        if(AcceptCount == 1 && DAG.size() == 1)
        {
            DAG.add(n);
            return true;
        }
        
        return false;
    }
    
    public boolean addNodeWithoutChecking(Node n) //todo
    {
        DAG.add(n);
        return true;
    }
    
    public boolean startTangleFromScratch()
    {
        DAG = new ArrayList<>();
        
        try
        {
            Node genesis = new Node();
            genesis = genesis.loadGenesis();

            DAG.add(genesis);

            return true;
        }
        catch (Exception e) 
        {
            System.out.println("Error! Genesis Node Not Found!");
        }
        return false;
    }
    
    public Node findNodeById(String nodeId)
    {
        for(int i = DAG.size() - 1; i >= 0 ; i--)
        {
            if(DAG.get(i).NodeId.equals(nodeId))
            {
                return DAG.get(i);
            }
        }
        return null;
    }
    
    public ArrayList<Node> selectTipNodes() //todo implement real tip selection algorithm
    {
        ArrayList<Node> Tips = new ArrayList<>();
        
        for(int i = DAG.size() - 1; i >= 0 ; i--)
        {
            if(DAG.get(i).NodeId != null)
            {
                Tips.add(DAG.get(i));              
            }
            if(Tips.size() >= 2)
                break;
        }
        return Tips;
    }
    
    public static boolean hasCycle(ArrayList<Node> Nodes)
    {
        for(int i = 0; i < Nodes.size(); i++)
        {
            if(Nodes.get(i).state == 0 && dfsToCheckCycle(Nodes.get(i), Nodes))
            {
                return true;
            }
        }
        
        return false;
    }
    
    public static boolean dfsToCheckCycle(Node node, ArrayList<Node> Nodes)
    {
        node.state = 1;
            
        for(int i = 0; i < Nodes.size(); i++) //to find nodes by Id
        {
            if(node.FirstAcceptedNodeId.equals(Nodes.get(i).NodeId))
            {
                if(Nodes.get(i).state == 1)
                {
                    return true;
                }
                else if(Nodes.get(i).state == 0 && dfsToCheckCycle(Nodes.get(i), Nodes))
                {
                   return true;
                }   
            }
            if(node.SecondAcceptedNodeId.equals(Nodes.get(i).NodeId))
            {
                if(Nodes.get(i).state == 1)
                {
                    return true;
                }
                else if(Nodes.get(i).state == 0 && dfsToCheckCycle(Nodes.get(i), Nodes))
                {
                   return true;
                }                      
            }
        }

        node.state = 2;    
        
        return false;
    }
    
    public static boolean checkIfBackwardEdgesOfNewNodesLeadToOurTangle(ArrayList<Node> Nodes, Tangle OurTanle)
    {
        for(int i = 0; i < Nodes.size(); i++)
        {
            Nodes.get(i).state = 0;
        }
        
        for(int i = 0; i < Nodes.size(); i++)
        {
            //if(Nodes.get(i).state == 0 && dfsBackToOurTangle(Nodes.get(i), Nodes, OurTanle.DAG) == false) {  return true; }
            if(Nodes.get(i).state == 0)
            {
                if(dfsBackToOurTangle(Nodes.get(i), Nodes, OurTanle.DAG) == false)
                {
                    return false;
                }
            }       
        }
        return true;
    }
    
    public static boolean dfsBackToOurTangle(Node node, ArrayList<Node> NewNodes, ArrayList<Node> TangleNodes)
    {
        node.state = 1;
        
        boolean firstFound = false;
        boolean secondFound = false;
        
        boolean firstRes = false;
        boolean secondRes = false;
            
        for(int i = 0; i < NewNodes.size(); i++) //to find nodes by Id
        {
            if(node.FirstAcceptedNodeId.equals(NewNodes.get(i).NodeId))
            {
                firstFound = true;
                
                // if we haven't checked it, check it
                if(NewNodes.get(i).state == 0 )
                {
                    firstRes = dfsBackToOurTangle(NewNodes.get(i), NewNodes, TangleNodes);
                }
                else
                {
                    firstRes = true; //already checked so it must have been ok
                }
  
            }
            
            if(node.SecondAcceptedNodeId.equals(NewNodes.get(i).NodeId))
            {
                secondFound = true;
                
                // if we haven't checked it, check it
                if(NewNodes.get(i).state == 0)
                {
                    secondRes = dfsBackToOurTangle(NewNodes.get(i), NewNodes, TangleNodes);                   
                } 
                else
                {
                    secondRes = true;
                }
            }
      
            
        }

        //we haven't found the accpted node in new nodes, so we have to check the existing nodes of our tangle
        if(firstFound == false || secondFound == false)
        {
            for(int j = TangleNodes.size() - 1; j >= 0 ; j-- )
            {
                if(firstFound == false)
                {
                    if(node.FirstAcceptedNodeId.equals(TangleNodes.get(j).NodeId))
                    {
                        firstRes = true;
                    }
                }
                if(secondFound == false)
                {
                    if(node.SecondAcceptedNodeId.equals(TangleNodes.get(j).NodeId))
                    {
                        secondRes = true;
                    }
                }
            }
            //return false;
        }


        return (firstRes & secondRes);
        
//        node.state = 2;    
//        
//        return false;
    }
    
    public static String DagToString(ArrayList<Node> dag, String PublicKey, boolean onlyMyNodes)
    {
        ArrayList<Node> MyEhrNodes = new ArrayList<Node>();
        
        if(onlyMyNodes)
        {
            for(int i = dag.size() - 1; i >= 0; i--)
            {
                if(dag.get(i).TransactionCreatorPublicKey.equals(PublicKey))
                {
                    MyEhrNodes.add(dag.get(i));
                }
            }
        }
        else
        {
            for(int i = dag.size() - 1; i >= 0; i--)
            {
                MyEhrNodes.add(dag.get(i));
            }
        }
        
        return Node.toJsonArray(MyEhrNodes);
    }
    
    public static String DagToString(ArrayList<Node> dag, String PublicKey, boolean onlyMyNodes, int limit)
    {
        ArrayList<Node> MyEhrNodes = new ArrayList<>();
        
        if(onlyMyNodes)
        {
            for(int i = dag.size() - 1; i >= 0; i--)
            {
                //if(dag.get(i).TransactionCreatorPublicKey.equals(PublicKey))
                if(HashAndSign.applySha256(dag.get(i).TransactionCreatorPublicKey).equals(PublicKey))
                {
                    MyEhrNodes.add(dag.get(i));
                    limit--;
                    if(limit <= 0)
                        break;
                }
            }
        }
        else
        {
            for(int i = dag.size() - 1; i >= 0; i--)
            {
                MyEhrNodes.add(dag.get(i));
                limit--;
                if(limit <= 0)
                    break;
            }
        }
        
        return Node.toJsonArray(MyEhrNodes);
    }
    
    public static ArrayList<Node> selectBestTangle(ArrayList<ArrayList<Node>> advertisedDags) //todo implement real tangle selection algorithm
    {
        ArrayList<Node> bestDag = new ArrayList<Node>();
        Node genesis = new Node();
        genesis = genesis.loadGenesis();
        
        for(ArrayList<Node> dag : advertisedDags)
        {
            if(dag.size() > bestDag.size() && dag.get(0).NodeId.equals(genesis.NodeId))
            {
                bestDag = dag;
            }
        }
        
        return bestDag;
    }
}

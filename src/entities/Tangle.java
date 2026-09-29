package entities;

import crypto.HashAndSign;
import java.security.SecureRandom;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Set;
import java.util.TreeSet;
import models.NodePair;
import models.NodeWeight;
import models.SortedUniqueArrayList;

/**
 * @author hosseinAghahosseini
 */

public class Tangle {
    
    /**
     *  alpha is the tuning factor in MCMC random walk
     *  if alpha is set to 0, candidate nodes have the same chance regardless of their weight
     *  the bigger the alpha, the more biased the algorithm is for heavier nodes
    */
    public static final double alpha = 0.4;   
    /**
     *  beta is another factor that penalizes following the same path in MCMC random walk
     *  if set to 1, there is no penalty
     *  the smaller the beta, the more diverse the path
    */
    public static final double beta = 0.7;
    
    SecureRandom srandom = new SecureRandom();
    
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
    
    public Node findNodeById(String nodeId) //todo make it seacrh by hash in different files (or servers)
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
    
    public NodePair findNodePairByIds(String nodeId1, String nodeId2) 
    {
        NodePair np = new NodePair();
        for(int i = DAG.size() - 1; i >= 0 ; i--)
        {
            if(np.Node1 == null && DAG.get(i).NodeId.equals(nodeId1))
            {
                np.Node1 = DAG.get(i);
            }
            
            if(np.Node2 == null && DAG.get(i).NodeId.equals(nodeId2))
            {
                np.Node2 = DAG.get(i);
            }
            
            if(np.Node1 != null && np.Node2 != null)
            {
                break;
            }
        }
        
        if(np.Node1 == null && np.Node2 == null)
        {
            return null;
        }
        else if(np.Node1 == null)
        {
            np.Node1 = np.Node2;
            np.are2NodesTheSame = true;
        }
        else if(np.Node2 == null)
        {
            np.Node2 = np.Node1;
            np.are2NodesTheSame = true;
        }
        else if(nodeId1.equals(nodeId2))
        {
            np.are2NodesTheSame = true;
        }

        return np;
    }
    
    public ArrayList<Node> findNodesThatAcceptsAnother(String anotheNodeId) //todo make it seacrh by hash in different files (or servers)
    {
        ArrayList<Node> nodes = new ArrayList<>();
        
        for(int i = DAG.size() - 1; i >= 0 ; i--)
        {
            if(DAG.get(i).FirstAcceptedNodeId.equals(anotheNodeId) || DAG.get(i).SecondAcceptedNodeId.equals(anotheNodeId) )
            {
                nodes.add(DAG.get(i));
            }
        }
        return nodes;
    }
    
    public ArrayList<Node> selectTipNodesSimple() //just selects the 2 latest nodes of the tangle as edge nodes
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
    
    public ArrayList<Node> selectTipNodes() //temporary to make app work
    {
        return selectTipNodesMCMC();
    }
    
    public ArrayList<Node> selectTipNodesMCMC() //todo continue implementing tip selection algorithm
    {
        if(DAG == null || DAG.isEmpty() || DAG.size() < 7)
            return selectTipNodesSimple();
        
        ArrayList<Node> Tips = new ArrayList<>();
        Set<String> Path = new TreeSet<>();
        //SortedUniqueArrayList Path = new SortedUniqueArrayList();

        //start at a milestone node (or genesis at start)
        Node currentNode = GetMilestoneNode(); //genesis
        if(currentNode == null) return null;
        
        while(Tips.size() < 2)
        {
            //monte carlo markov chain random walk (walk through the edge nodes with weigh bias)
            var canditateNextDestinationNodes = findNodesThatAcceptsAnother(currentNode.NodeId);

            if(canditateNextDestinationNodes.isEmpty()) //we found a tip
            {
                boolean foundTheSameTip = false;
                for(var tip : Tips)
                {
                    if(tip.NodeId.equals(currentNode.NodeId))
                    {
                        foundTheSameTip = true;
                        break;
                    }
                }
                if(!foundTheSameTip) Tips.add(new Node(currentNode));
                currentNode = GetMilestoneNode();
                continue;
            }

            //choosing the next node according to cumulative weight

            double sum = 0;
            int size = canditateNextDestinationNodes.size();
            double[] probabilityList = new double[size];

            // (e ^ a.w) / Sum (e ^ a.w) for all nodes
            for(int i = 0; i < size; i++)
            {
                var candid = canditateNextDestinationNodes.get(i);
                var weight = (double) candid.CumulativeWeight;
                
                //to punish revisiting previously visited nodes (and to find paths to tips)
                double diversityMultiplier = 1;
                if( (!Tips.isEmpty()) && Path.contains(candid.NodeId))
                    diversityMultiplier = beta;              
                
                //original tangle 1.0 formula + our beta path diversity multiplier
                var temp = Math.exp(alpha * weight * diversityMultiplier);
                probabilityList[i] = temp;
                sum += temp;
            }

            double randomResult = srandom.nextDouble();
            double sumOfProb = 0;
            for(int i = 0; i < size; i++)
            {
                probabilityList[i] = probabilityList[i] / sum;
                sumOfProb += probabilityList[i];
                if(randomResult <= sumOfProb)
                {
                    currentNode = canditateNextDestinationNodes.get(i); //next node is found
                    Path.add(currentNode.NodeId);
                    break;
                }
            }
        }
        
        return Tips;
    }
    
    public Node GetMilestoneNode()
    {
        if(!DAG.isEmpty())
        {
            return DAG.get(0);
        }      
        return null;
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
    
    public void SetTangleClone(ArrayList<Node> proposedDAG)
    {
        this.DAG = new ArrayList<>();
        for(int i = 0; i < proposedDAG.size(); i++)
        {
            Node n = new Node(proposedDAG.get(i));
            this.DAG.add(n);
        }
    }
    
    public static ArrayList<Node> recalculateCumulativeWeights(ArrayList<Node> NewNodes)
    {
        if(NewNodes.isEmpty()) return NewNodes;
                
        for (Node updatingNode : NewNodes) 
        {                    
            for (Node checkingNode : NewNodes) 
            {
                for (Node cleaningNode : NewNodes) //clean visit states
                {
                    cleaningNode.visited = 0;
                }
                
                if(checkingNode.NodeId.equals(updatingNode.NodeId)) continue;
                
                Deque<Node> stack = new ArrayDeque<>(); //dfs
                stack.push(checkingNode);
                
                while(!stack.isEmpty())
                {
                    var topNode = stack.pop();
                    if(topNode.visited == 1) continue; //we have already visited the node

                    topNode.visited = 1;
                    if(topNode.NodeId.equals(updatingNode.NodeId)) //checkingNode accepts updatingNode (directly or indirectly)
                    {
                        updatingNode.CumulativeWeight += checkingNode.OwnWeight; 
                    }

                    //find acceptedNodes
                    short found = 0;
                    for(Node acceptingNode : NewNodes)
                    {
                        if(topNode.FirstAcceptedNodeId.equals(topNode.SecondAcceptedNodeId)) //2 edge to one node
                        {
                            if(acceptingNode.NodeId.equals(topNode.FirstAcceptedNodeId))
                            {
                                stack.push(acceptingNode);
                                break;
                            }
                        }
                        else //normal case
                        {
                            if(acceptingNode.NodeId.equals(topNode.FirstAcceptedNodeId)) //found node 1
                            {
                                stack.push(acceptingNode);
                                found++;
                            }
                            if(acceptingNode.NodeId.equals(topNode.SecondAcceptedNodeId))  //found node 2
                            {
                                stack.push(acceptingNode);
                                found++;
                            }
                        }
                        if(found == 2) break;
                    }
                }
            }
        }
        
        return NewNodes;
    }
    
    public boolean calculateCumulativeWeightsAfterAddingANode(Node addedNode)
    {
        //reset state
        for(int i = 0; i < this.DAG.size(); i++)
        {
            DAG.get(i).visited = 0;
        }
        
        Deque<Node> stack = new ArrayDeque<>(); //dfs
        stack.push(addedNode);
        
        while(!stack.isEmpty())
        {
            var topNode = stack.pop();
            if(topNode.visited == 1) //we have already visited the node
            {
                continue;
            }
            topNode.visited = 1;
            topNode.CumulativeWeight += addedNode.OwnWeight;
            
            
            var acceptedNodes = findNodePairByIds(topNode.FirstAcceptedNodeId, topNode.SecondAcceptedNodeId);
            
            if(acceptedNodes == null) continue;
            
            if(acceptedNodes.are2NodesTheSame)
            {
                stack.push(acceptedNodes.Node1);
            }
            else
            {
                stack.push(acceptedNodes.Node1);
                stack.push(acceptedNodes.Node2);
            }
        }
        
        return true;
    }
    
    public boolean calculateCumulativeWeightsAfterAddingAGraph(ArrayList<Node> NewNodes) //todo check
    {
        if(NewNodes.isEmpty()) return false;
        for(int j = 0; j < NewNodes.size(); j++) //clearing new nodes
        {
            NewNodes.get(j).visited = 0;
            NewNodes.get(j).OwnWeight = 1;
            NewNodes.get(j).CumulativeWeight = 1;
        } 

        //find the entrance points to our graph //ok
        ArrayList<NodeWeight> EntranceNodes = new ArrayList<>();
        for(int i = 0; i < NewNodes.size(); i++)
        {
            boolean node1Found = false, node2Found = false;
            var currentNode1 = NewNodes.get(i).FirstAcceptedNodeId;
            var currentNode2 = NewNodes.get(i).SecondAcceptedNodeId;
            
            for(int j = 0; j < NewNodes.size(); j++)
            {
                if(node1Found == false && NewNodes.get(j).NodeId.equals(currentNode1))
                {
                    node1Found = true;
                }
                if(node2Found == false && NewNodes.get(j).NodeId.equals(currentNode2))
                {
                    node2Found = true;
                }
                if(node1Found && node2Found) break;
            }
            if(!node1Found)
            {
                boolean found = false;
                for(var node: EntranceNodes)
                {
                    if(node.NodeId.equals(currentNode1))
                    {
                        found = true;
                        break;
                    }
                }
                if(!found) EntranceNodes.add(new NodeWeight(currentNode1));
            }
            if(!node2Found) 
            {
                boolean found = false;
                for(var node: EntranceNodes)
                {
                    if(node.NodeId.equals(currentNode2))
                    {
                        found = true;
                        break;
                    }
                }
                if(!found) EntranceNodes.add(new NodeWeight(currentNode2));
            }
        }
        
        //remove the entrance nodes that lead to each other
        ArrayList<NodeWeight> toRemove = new ArrayList<>();
        for(var Dest : EntranceNodes)
        {
            for(var Source : EntranceNodes)
            {
                if(Dest.NodeId.equals(Source.NodeId)) continue;
                
                Deque<Node> stack = new ArrayDeque<>(); 
                var currentNode = findNodeById(Source.NodeId);
                stack.push(currentNode);

                while(!stack.isEmpty())
                {
                    var topNode = stack.pop();
                    //we reached to dest (dest <- source)
                    if(topNode.FirstAcceptedNodeId.equals(Dest.NodeId) || topNode.SecondAcceptedNodeId.equals(Dest.NodeId))
                    {
                        //mark dest as not a usefull entrance
                        toRemove.add(Dest);
                    }
                    
                }
            }
        }
        EntranceNodes.removeAll(toRemove);
        
        //set cumulative weights for entrance nodes
        //calcualete which of the new nodes lead to entrance points
        for(int i = 0; i < EntranceNodes.size(); i++)
        {
            var EntranceNode = EntranceNodes.get(i);

            //searching through new dag
            for(int j = 0; j < NewNodes.size(); j++)
            {
                //clearing node states
                for(var node : NewNodes)
                {
                    node.visited = 0;
                }
            
                //do a dfs for each node to find out if it reaches the entrance node
                var AcceptingNode = NewNodes.get(j);

                //dfs
                Deque<Node> stack = new ArrayDeque<>();
                stack.push(AcceptingNode);

                while(!stack.isEmpty())
                {
                    var topNode = stack.pop();
                    if(topNode.visited == 1) //we have already visited the node
                    {
                        continue;
                    }
                    topNode.visited = 1;
                    
                    //check if we accept the current node
                    if(EntranceNode.NodeId.equals(topNode.FirstAcceptedNodeId) || EntranceNode.NodeId.equals(topNode.SecondAcceptedNodeId))
                    {
                        EntranceNode.weight += AcceptingNode.OwnWeight;
                        break;
                    }
                    
                    short found = 0;
                    for(int k = 0; k < NewNodes.size(); k++)
                    {
                        if(topNode.FirstAcceptedNodeId.equals(NewNodes.get(k).NodeId))
                        {
                            found++;
                            stack.push(NewNodes.get(k));
                        }
                        else if(topNode.SecondAcceptedNodeId.equals(NewNodes.get(k).NodeId))
                        {
                            found++;
                            stack.push(NewNodes.get(k));
                        }
                        if(found == 2) break;
                    }
                }
            }
        }
        
        //updating node weights of new nodes
        NewNodes = recalculateCumulativeWeights(NewNodes);
        
        //updating node weights of our own graph
        for(int i = 0; i < EntranceNodes.size(); i++)
        {
            //reset state
            for(int j = 0; j < this.DAG.size(); j++)
            {
                DAG.get(j).visited = 0;
            }
            
            Node edgeNode = findNodeById(EntranceNodes.get(i).NodeId);
            int weight2add = EntranceNodes.get(i).weight;

            Deque<Node> stack = new ArrayDeque<>(); //dfs
            stack.push(edgeNode);

            while(!stack.isEmpty())
            {
                var topNode = stack.pop();
                if(topNode.visited == 1) //we have already visited the node
                {
                    continue;
                }
                topNode.visited = 1;
                topNode.CumulativeWeight += weight2add;

                var acceptedNodes = findNodePairByIds(topNode.FirstAcceptedNodeId, topNode.SecondAcceptedNodeId);

                if(acceptedNodes == null) continue;

                if(acceptedNodes.are2NodesTheSame)
                {
                    stack.push(acceptedNodes.Node1);
                }
                else
                {
                    stack.push(acceptedNodes.Node1);
                    stack.push(acceptedNodes.Node2);
                }
            }
        }
        
        return true;
    }
    
    //to string
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
}
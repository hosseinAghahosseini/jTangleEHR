package entities;

/**
 * @author hosseinAghahosseini
 */
public class LiteNode 
{
    //Id
    public String NodeId;
    
    //Weight
    public int OwnWeight;
    public int CumulativeWeight;
    
    //Nodes to Accept
    public String FirstAcceptedNodeId = "#";
    public String SecondAcceptedNodeId = "#";
}

package UI;

import entities.Node;
import static entities.Node.readNodeFromFile;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.TidierTreeLayoutAlgorithm;
//import org.jungrapht.visualization.layout.algorithms.util.synthetics.Synthetic;
//import org.jungrapht.visualization.layout.algorithms.util.synthetics.Synthetic;
import org.jungrapht.visualization.layout.model.LayoutModel;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 *
 * @author hosseinAghahosseini
 */
public class DAGViewer {
       
    Graph<String, DefaultEdge> graph;
    Map<String, String> weightMap;
    
    public static int NodeIdSize = 6;
    
    public DAGViewer()
    {
        
    }
    
    public DAGViewer(ArrayList<Node> dag)
    {
        graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        weightMap = new HashMap<>();
        
        for(var node : dag)
        {
            graph.addVertex(node.NodeId);
            weightMap.put(node.NodeId, String.valueOf(node.CumulativeWeight));
        }
        
        Node genesis = readNodeFromFile("genesis.ehr");
        
        for(var node : dag)
        {
            if(!node.NodeId.equals(genesis.NodeId))
            {
                graph.addEdge
                   (node.NodeId,
                    node.FirstAcceptedNodeId);
                
                if(!node.SecondAcceptedNodeId
                    .equals(node.FirstAcceptedNodeId))
                {    
                    graph.addEdge(node.NodeId,
                        node.SecondAcceptedNodeId);
                }
            }
        }
        
        System.out.println("edges:");
        Set<DefaultEdge> edges = graph.edgeSet();
        for(var edge : edges)
        {
            System.out.println(edge.toString());
        }
        
    }

    public void visualize() {

        System.setProperty("sun.java2d.uiScale", "1.0");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                
                // 4. Create a layout
                LayoutAlgorithm<String> layoutAlgorithm =
                        new TidierTreeLayoutAlgorithm<>();
                
                // 5. Create the visualization
                VisualizationViewer<String, DefaultEdge> vv =
                        VisualizationViewer.builder(graph)
                                .layoutAlgorithm(layoutAlgorithm)
                                .build();
                
                
                vv.getRenderContext().setVertexLabelFunction(
                    v -> v.substring(0, NodeIdSize) + " (" + weightMap.get(v) + ")"
                );
                
                vv.setDoubleBuffered(true);
                
                // 6. Create the window
                JFrame frame = new JFrame("DAG Visualization");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setLayout(new BorderLayout());
                frame.add((Component) vv, BorderLayout.CENTER);
                frame.setSize(1920, 1080);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }
}
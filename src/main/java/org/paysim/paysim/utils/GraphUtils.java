package org.paysim.paysim.utils;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.tinkerpop.gremlin.structure.*;
import org.apache.tinkerpop.gremlin.structure.io.graphml.GraphMLReader;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

public class GraphUtils {

    public static Graph loadFromFile(String filename) {
        Graph graph = TinkerGraph.open();
        try {
            InputStream targetStream = new FileInputStream(filename);
            GraphMLReader.build().create()
                    .readGraph(targetStream, graph);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return graph;
    }

    public static Vertex getVertex(Graph graph, String vertexName){
        Iterator<Vertex> vertices = graph.vertices(vertexName);
        if (vertices.hasNext()){
            return vertices.next();
        } else {
            throw new NoSuchElementException("Vertex was not found");
        }
    }

    public static Edge getEdge(Graph graph, String edgeName){
        Iterator<Edge> edges = graph.edges(edgeName);
        if (edges.hasNext()){
            return edges.next();
        } else {
            throw new NoSuchElementException("Edge was not found");
        }
    }

    public static Object getProperty(Vertex vertex, String propName){
        Iterator<VertexProperty<Object>> properties = vertex.properties(propName);
        if (properties.hasNext()){
            return properties.next().value();
        } else {
            throw new NoSuchElementException("Property was not found");
        }
    }

    public static Object getProperty(Edge edge, String propName){
        Iterator<Property<Object>>  properties = edge.properties(propName);
        if (properties.hasNext()){
            return properties.next().value();
        } else {
            throw new NoSuchElementException("Property was not found");
        }
    }

    public static Map<Double, Double> unserializeMap(String serializedMap){
        Map<Double, Double> map = new HashMap<>();
        String[] pairs = serializedMap.split(",");
        for (int i = 0; i < pairs.length; i++) {
            String pair = pairs[i];
            String[] keyValue = pair.split(":");
            map.put(Double.valueOf(keyValue[0]), Double.valueOf(keyValue[1]));
        }
        return map;
    }
}

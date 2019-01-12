package org.paysim.paysim.actors.networkdrugs;

import ec.util.MersenneTwisterFast;
import org.apache.tinkerpop.gremlin.structure.*;

import org.paysim.paysim.PaySim;
import org.paysim.paysim.utils.GraphUtils;
import org.paysim.paysim.utils.RandomCollection;

import java.util.Map;

public class NetworkDrug {
    public static void createNetwork(PaySim paySim, String drugNetworkFile) {
        // Load graph file
        Graph graph = GraphUtils.loadFromFile(drugNetworkFile);

        // Load dealer parameters
        Vertex drugDealer = GraphUtils.getVertex(graph, "DrugDealer");
        double thresholdDealer = (double) GraphUtils.getProperty(drugDealer, "thresholdForCashOut");
        DrugDealer dealer = new DrugDealer(paySim, thresholdDealer);
        paySim.addClient(dealer);

        // Load consumers parameters
        Vertex drugConsumers = GraphUtils.getVertex(graph, "DrugConsumers");
        int nbConsumer = (int) GraphUtils.getProperty(drugConsumers, "count");
        double monthlySpending = (double) GraphUtils.getProperty(drugConsumers, "monthlySpending");

        Edge buyDrugs = GraphUtils.getEdge(graph, "BuyDrugs");
        String probAmountProfileSerialized = (String) GraphUtils.getProperty(buyDrugs, "probAmountProfile");

        Map<Double, Double> mapProbAmountProfile =  GraphUtils.unserializeMap(probAmountProfileSerialized);
        double meanTr = computeMean(mapProbAmountProfile);
        RandomCollection<Double> probAmountProfile = mapToRandomCollection(mapProbAmountProfile, paySim.random);

        for (int i = 0; i < nbConsumer; i++) {
            paySim.addClient(new DrugConsumer(paySim, dealer, monthlySpending, probAmountProfile, meanTr));
        }
    }

    private static double computeMean(Map<Double, Double> map) {
        double mean = 0;
        for (Map.Entry<Double, Double> entry : map.entrySet()) {
            mean += entry.getKey() * entry.getValue();
        }
        return mean;
    }

    private static RandomCollection<Double> mapToRandomCollection(Map<Double, Double> map, MersenneTwisterFast random){
        RandomCollection<Double> randomCollection = new RandomCollection<>();
        randomCollection.setRandom(random);
        for (Map.Entry<Double, Double> entry : map.entrySet()) {
            randomCollection.add(entry.getValue(), entry.getKey());
        }
        return randomCollection;
    }
}

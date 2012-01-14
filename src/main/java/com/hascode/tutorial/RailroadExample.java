package com.hascode.tutorial;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.Traversal;

public class RailroadExample {
	private static String DB_PATH = "/tmp/neo4j";

	// http://maps.google.com/maps?q=London,+United+Kingdom&hl=en&ll=51.518998,-1.087646&spn=2.317597,5.811768&sll=53.556866,9.994622&sspn=0.553096,1.452942&oq=london&vpsrc=6&hnear=London,+United+Kingdom&t=m&z=8
	public static void main(final String[] args) {
		GraphDatabaseService graphDb = new EmbeddedGraphDatabase(DB_PATH);
		registerShutdownHook(graphDb);
		Index<Node> nodeIndex = graphDb.index().forNodes("nodes");

		Transaction tx = graphDb.beginTx();
		try {
			Node londonNode = graphDb.createNode();
			londonNode.setProperty("name", "London");
			nodeIndex.add(londonNode, "name", "London");

			Node brightonNode = graphDb.createNode();
			brightonNode.setProperty("name", "Brighton");
			nodeIndex.add(brightonNode, "name", "Brighton");

			Node portsmouthNode = graphDb.createNode();
			portsmouthNode.setProperty("name", "Portsmouth");
			nodeIndex.add(portsmouthNode, "name", "Portsmouth");

			Node bristolNode = graphDb.createNode();
			bristolNode.setProperty("name", "Bristol");
			nodeIndex.add(bristolNode, "name", "Bristol");

			Node oxfordNode = graphDb.createNode();
			oxfordNode.setProperty("name", "Oxford");
			nodeIndex.add(oxfordNode, "name", "Oxford");

			Node gloucesterNode = graphDb.createNode();
			gloucesterNode.setProperty("name", "Gloucester");
			nodeIndex.add(gloucesterNode, "name", "Gloucester");

			// london -> brighton
			Relationship r1 = londonNode.createRelationshipTo(brightonNode,
					RelTypes.LEADS_TO);
			r1.setProperty("distance", 10);

			// brighton -> portsmouth
			Relationship r2 = brightonNode.createRelationshipTo(portsmouthNode,
					RelTypes.LEADS_TO);
			r2.setProperty("distance", 10);

			// portsmouth -> bristol
			Relationship r3 = portsmouthNode.createRelationshipTo(bristolNode,
					RelTypes.LEADS_TO);
			r3.setProperty("distance", 30);

			// london -> oxford
			Relationship r4 = londonNode.createRelationshipTo(oxfordNode,
					RelTypes.LEADS_TO);
			r4.setProperty("distance", 20);

			// oxford -> bristol
			Relationship r5 = oxfordNode.createRelationshipTo(bristolNode,
					RelTypes.LEADS_TO);
			r5.setProperty("distance", 20);

			tx.success();

			System.out
					.println("searching for the shortest route from London to Bristol..");
			PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra(
					Traversal.expanderForTypes(RelTypes.LEADS_TO,
							Direction.BOTH), "distance");

			WeightedPath path = finder.findSinglePath(londonNode, bristolNode);
			System.out.print("London - Bristol with a distance of: "
					+ path.weight() + " and via: ");
			for (Node n : path.nodes()) {
				System.out.print(" " + n.getProperty("name"));
			}

		} finally {
			tx.finish();
		}

		graphDb.shutdown();
	}

	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}
}

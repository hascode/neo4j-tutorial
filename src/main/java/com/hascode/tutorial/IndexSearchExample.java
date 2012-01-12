package com.hascode.tutorial;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.kernel.EmbeddedGraphDatabase;

public class IndexSearchExample {
	private static String DB_PATH = "/tmp/neo4j";

	public static void main(final String[] args) {
		GraphDatabaseService graphDb = new EmbeddedGraphDatabase(DB_PATH);
		Index<Node> nodeIndex = graphDb.index().forNodes("nodes");
		registerShutdownHook(graphDb);

		Transaction tx = graphDb.beginTx();
		try {
			Node userNode1 = graphDb.createNode();
			userNode1.setProperty("id", 1);
			userNode1.setProperty("name", "Peter");
			nodeIndex.add(userNode1, "id", 1);
			nodeIndex.add(userNode1, "name", "Peter");

			Node userNode2 = graphDb.createNode();
			userNode2.setProperty("id", 2);
			userNode2.setProperty("name", "Ray");
			nodeIndex.add(userNode2, "id", 2);
			nodeIndex.add(userNode2, "name", "Ray");

			Relationship relation = userNode1.createRelationshipTo(userNode2,
					RelTypes.KNOWS);
			relation.setProperty("message", "knows");
			tx.success();
			System.out.println(String.format("%s %s %s",
					userNode1.getProperty("name"),
					relation.getProperty("message"),
					userNode2.getProperty("name")));

			System.out.println("searching for user with id=2..");
			Node user = nodeIndex.get("id", 2).getSingle();
			System.out.println("The name of the user with id=2 is: "
					+ user.getProperty("name"));

			System.out.println("searching for user with name=Peter..");
			Node user2 = nodeIndex.get("name", "Peter").getSingle();
			System.out.println("The id of the user with name=Peter is: "
					+ user2.getProperty("id"));
		} finally {
			tx.finish();
			graphDb.shutdown();
		}

	}

	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running example before it's completed)
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}
}
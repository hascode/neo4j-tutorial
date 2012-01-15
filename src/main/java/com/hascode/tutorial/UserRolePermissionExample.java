package com.hascode.tutorial;

import static com.hascode.tutorial.GraphUtil.cleanUp;
import static com.hascode.tutorial.GraphUtil.registerShutdownHook;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.Traverser;
import org.neo4j.graphdb.index.Index;
import org.neo4j.kernel.EmbeddedGraphDatabase;

public class UserRolePermissionExample {
	private static String DB_PATH = "/tmp/neo4j";

	public static void main(final String[] args) {
		GraphDatabaseService graphDb = new EmbeddedGraphDatabase(DB_PATH);
		registerShutdownHook(graphDb);
		Index<Node> nodeIndex = graphDb.index().forNodes("nodes");

		Transaction tx = graphDb.beginTx();
		try {
			// cleanup first for this tutorial
			cleanUp(graphDb, nodeIndex);

			// 1. define possible permissions
			// permission:read
			Node permissionRead = graphDb.createNode();
			permissionRead.setProperty("permission", "read");
			nodeIndex.add(permissionRead, "permission", "read");

			// permission:write
			Node permissionWrite = graphDb.createNode();
			permissionWrite.setProperty("permission", "write");
			nodeIndex.add(permissionWrite, "permission", "write");

			// permission:administer
			Node permissionAdminister = graphDb.createNode();
			permissionAdminister.setProperty("permission", "administer");
			nodeIndex.add(permissionAdminister, "permission", "administer");

			// 2. define possible roles
			// role:guest
			Node roleGuest = graphDb.createNode();
			roleGuest.setProperty("role", "guest");
			nodeIndex.add(roleGuest, "role", "guest");

			// role:user
			Node roleUser = graphDb.createNode();
			roleUser.setProperty("role", "user");
			nodeIndex.add(roleUser, "role", "user");

			Node roleAdmin = graphDb.createNode();
			roleAdmin.setProperty("role", "admin");
			nodeIndex.add(roleAdmin, "role", "admin");

			// 3. assign permissions to roles
			// guests are only allowed to read
			roleGuest.createRelationshipTo(permissionRead,
					UserRelTypes.HAS_PERMISSION);

			// users my read and write
			roleUser.createRelationshipTo(permissionRead,
					UserRelTypes.HAS_PERMISSION);
			roleUser.createRelationshipTo(permissionWrite,
					UserRelTypes.HAS_PERMISSION);

			// administrators may read, write and administer
			roleAdmin.createRelationshipTo(permissionRead,
					UserRelTypes.HAS_PERMISSION);
			roleAdmin.createRelationshipTo(permissionWrite,
					UserRelTypes.HAS_PERMISSION);
			roleAdmin.createRelationshipTo(permissionAdminister,
					UserRelTypes.HAS_PERMISSION);

			// 4. finally create some users and assign roles
			// egon is a guest
			Node egon = graphDb.createNode();
			egon.setProperty("name", "Egon");
			nodeIndex.add(egon, "name", "Egon");
			egon.createRelationshipTo(roleGuest, UserRelTypes.HAS_ROLE);

			// winston is a user
			Node winston = graphDb.createNode();
			winston.setProperty("name", "Winston");
			nodeIndex.add(winston, "name", "Winston");
			winston.createRelationshipTo(roleUser, UserRelTypes.HAS_ROLE);

			// slimer is - of course - an admin
			Node slimer = graphDb.createNode();
			slimer.setProperty("name", "Slimer");
			nodeIndex.add(slimer, "name", "Slimer");
			slimer.createRelationshipTo(roleAdmin, UserRelTypes.HAS_ROLE);

			tx.success();

			System.out
					.println("Looking up permissions for user with name=Winston");
			Node userWinston = nodeIndex.get("name", "Winston").getSingle();
			Traverser permissionTraverser = getPermissionTraverser(userWinston);
			printPermissions(userWinston, permissionTraverser);

			System.out
					.println("Looking up permissions for user with name=Slimer");
			Node userSlimer = nodeIndex.get("name", "Slimer").getSingle();
			permissionTraverser = getPermissionTraverser(userSlimer);
			printPermissions(userSlimer, permissionTraverser);
		} finally {
			tx.finish();
		}

		graphDb.shutdown();
	}

	private static void printPermissions(final Node userNode,
			final Traverser permissionTraverser) {
		final String userName = (String) userNode.getProperty("name");
		for (Node node : permissionTraverser) {
			if (node.hasProperty("role")) {
				System.out.println(userName + " has the role: "
						+ node.getProperty("role"));
			}
			if (node.hasProperty("permission")) {
				System.out.println(userName + " has permission: "
						+ node.getProperty("permission") + " at depth: "
						+ (permissionTraverser.currentPosition().depth() - 1));
			}
		}
	}

	private static Traverser getPermissionTraverser(final Node userNode) {
		return userNode.traverse(Traverser.Order.DEPTH_FIRST,
				StopEvaluator.END_OF_GRAPH,
				ReturnableEvaluator.ALL_BUT_START_NODE, UserRelTypes.HAS_ROLE,
				Direction.OUTGOING, UserRelTypes.HAS_PERMISSION,
				Direction.OUTGOING);
	}
}

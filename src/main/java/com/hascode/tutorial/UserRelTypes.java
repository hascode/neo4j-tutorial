package com.hascode.tutorial;

import org.neo4j.graphdb.RelationshipType;

public enum UserRelTypes implements RelationshipType {
	MEMBER_OF, HAS_ROLE, HAS_PERMISSION
}

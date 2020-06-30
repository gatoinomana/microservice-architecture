package graphql;

import javax.servlet.annotation.WebServlet;

import com.coxautodev.graphql.tools.SchemaParser;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

import graphql.schema.GraphQLSchema;
import graphql.servlet.SimpleGraphQLServlet;
import persistence.TodoRepository;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/graphql")
public class GraphQLEndpoint extends SimpleGraphQLServlet {

	private static TodoRepository todoRepository;
	private static MongoClient client;

	private static void initDB() {
		MongoClientURI uri = new MongoClientURI(
			    "mongodb://arso:arso-20@cluster0-shard-00-00-xi0ku.azure.mongodb.net:27017,cluster0-shard-00-01-xi0ku.azure.mongodb.net:27017,cluster0-shard-00-02-xi0ku.azure.mongodb.net:27017/arso?ssl=true&replicaSet=Cluster0-shard-0&authSource=admin&retryWrites=true&w=majority");

		client = new MongoClient(uri);
	    MongoDatabase mongo = client.getDatabase("arso");
	    todoRepository = new TodoRepository(mongo.getCollection("todos"));
	}

	@Override
	public void destroy() {
		super.destroy();
		client.close();
	}

	public GraphQLEndpoint() {
		super(buildSchema());
	}

	private static GraphQLSchema buildSchema() {

		initDB();

		return SchemaParser.newParser().file("schema.graphqls")
		        .resolvers(
		            new Query(todoRepository), 
		            new Mutation(todoRepository)
		            )
		        .build().makeExecutableSchema();
	}

}
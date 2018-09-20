package gate_test;


import gate.util.maven.SimpleMavenCache;
import gate.util.maven.Utils;

import java.io.File;
import java.util.List;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.WorkspaceReader;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;

public class MavenDepsTest {
	public static void main(String[] args) throws Exception {
		RepositorySystem repoSystem = Utils.getRepositorySystem();

		WorkspaceReader workspace = new SimpleMavenCache(new File("repo"));
		RepositorySystemSession session = Utils.getRepositorySession(repoSystem, workspace);
		
		Artifact artifactObj = new DefaultArtifact(
				"net.sf.czsem", 
				"netgraph-tree-viewer",
				"jar",
				"3.1.0-SNAPSHOT");

		Dependency dependency = new Dependency(artifactObj, "runtime");		

		List<RemoteRepository> repos = Utils.getRepositoryList();
		
		CollectRequest collectRequest = new CollectRequest();
		collectRequest.setRoot(dependency);
		for (RemoteRepository remoteRepository : repos) {
			collectRequest.addRepository(remoteRepository);
		}
		
		DependencyNode node = repoSystem.collectDependencies(session, collectRequest).getRoot();

		DependencyRequest dependencyRequest = new DependencyRequest();
		dependencyRequest.setRoot(node);

		repoSystem.resolveDependencies(session, dependencyRequest);

		PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
		node.accept(nlg);
		for (Artifact a : nlg.getArtifacts(true)) {
			System.err.println(a.toString());
		}
	}
}

package ac.ict.debs.qmapper.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.StaticHttpHandler;

import com.sun.jersey.api.container.grizzly2.GrizzlyWebContainerFactory;

import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.http.servlet.ServletAdapter;
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

import ac.ict.debs.qmapper.server.rest.RestHandler;

public class Service {
	private static URI getBaseURI() {
		return UriBuilder.fromUri("http://localhost/").port(9999).build();
	}

	public static final URI BASE_URI = getBaseURI();

	protected static HttpServer startServer() throws IOException {
		final Map<String, String> initParams = new HashMap<String, String>();

		initParams.put("com.sun.jersey.config.property.packages",
				"ac.ict.debs.qmapper.server.rest");

		System.out.println("Starting grizzly...");

		return GrizzlyWebContainerFactory.create(BASE_URI, initParams);

	}

	public static void main(String[] args) throws IOException {
		System.out.println("start service!"); 
		HttpServer httpServer = startServer();
		httpServer.getServerConfiguration().addHttpHandler(
				new StaticHttpHandler(args[0]), "/static");
		for (NetworkListener l : httpServer.getListeners()) {
			l.getFileCache().setEnabled(false);
		}
		System.out
				.println(String
						.format("Jersey app started with WADL available at "
								+ "%sapplication.wadl\nTry out %shelloworld\nHit enter to stop it...",
								BASE_URI, BASE_URI));
		System.in.read();
		httpServer.stop();
	}
}

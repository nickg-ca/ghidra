package ghidra.mcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.EnumSet;
import java.util.function.Consumer;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.FilterHolder;

import com.fasterxml.jackson.databind.ObjectMapper;

import ghidra.framework.model.ProjectData;
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;

public class McpServerRunner {

	private final ProjectData projectData;
	private final int port;
	private final String token;
	private final Consumer<String> logger;

	private Server server;

	public McpServerRunner(ProjectData projectData, int port, String token, Consumer<String> logger) {
		this.projectData = projectData;
		this.port = port;
		this.token = token;
		this.logger = logger != null ? logger : (s) -> {};
	}

	public void start() throws Exception {
		// Setup Transport
		ObjectMapper objectMapper = new ObjectMapper();
		JacksonMcpJsonMapper mapper = new JacksonMcpJsonMapper(objectMapper);
		HttpServletStreamableServerTransportProvider transport = HttpServletStreamableServerTransportProvider.builder()
				.jsonMapper(mapper)
				.build();

		// Initialize Server Logic
		// We hold a reference to wrapper to ensure it's not GC'd if that matters,
		// though usually it's tied to the transport/server callbacks.
		GhidraMcpServer serverWrapper = new GhidraMcpServer(projectData, transport);

		// Setup Jetty with explicit localhost binding
		server = new Server(new InetSocketAddress("127.0.0.1", port));
		ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		handler.setContextPath("/");
		server.setHandler(handler);

		// Auth Filter
		Filter authFilter = new Filter() {
			@Override
			public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
					throws IOException, ServletException {
				HttpServletRequest req = (HttpServletRequest) request;
				HttpServletResponse res = (HttpServletResponse) response;

				String authHeader = req.getHeader("Authorization");
				if (authHeader == null || !authHeader.equals("Bearer " + token)) {
					res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token");
					return;
				}
				chain.doFilter(request, response);
			}
			@Override
			public void init(FilterConfig filterConfig) {}
			@Override
			public void destroy() {}
		};

		handler.addFilter(new FilterHolder(authFilter), "/*", EnumSet.of(DispatcherType.REQUEST));

		// Register Transport Servlet
		ServletHolder transportHolder = new ServletHolder(transport);
		// Map to all paths; the transport handles sub-paths internally
		handler.addServlet(transportHolder, "/*");

		logger.accept("Starting MCP Server on localhost:" + port + "...");
		logger.accept("Authentication Token: " + token);
		logger.accept("Ensure you pass 'Authorization: Bearer " + token + "' header in your requests.");

		server.start();
		logger.accept("MCP Server running.");
	}

	public void join() throws InterruptedException {
		if (server != null) {
			server.join();
		}
	}

	public void stop() throws Exception {
		if (server != null) {
			logger.accept("Stopping server...");
			server.stop();
		}
	}
}

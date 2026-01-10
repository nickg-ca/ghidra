/* ###
 * IP: GHIDRA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//Runs the MCP Server.
//@category MCP
//@keybinding
//@menupath
//@toolbar

// @classPath ../lib/mcp-0.17.0.jar
// @classPath ../lib/mcp-core-0.17.0.jar
// @classPath ../lib/mcp-json-0.17.0.jar
// @classPath ../lib/mcp-json-jackson2-0.17.0.jar
// @classPath ../lib/reactor-core-3.7.0.jar
// @classPath ../lib/reactive-streams-1.0.4.jar
// @classPath ../lib/jackson-databind-2.19.2.jar
// @classPath ../lib/jackson-core-2.19.2.jar
// @classPath ../lib/jackson-annotations-2.19.2.jar
// @classPath ../lib/jackson-dataformat-yaml-2.19.2.jar
// @classPath ../lib/snakeyaml-2.4.jar
// @classPath ../lib/jakarta.servlet-api-5.0.0.jar
// @classPath ../lib/jetty-server-11.0.20.jar
// @classPath ../lib/jetty-http-11.0.20.jar
// @classPath ../lib/jetty-io-11.0.20.jar
// @classPath ../lib/jetty-util-11.0.20.jar
// @classPath ../lib/jetty-security-11.0.20.jar
// @classPath ../lib/jetty-servlet-11.0.20.jar
// @classPath ../lib/jetty-jakarta-servlet-api-5.0.2.jar
// @classPath ../lib/slf4j-simple-2.0.7.jar
// @classPath ../lib/slf4j-api-2.0.17.jar

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.EnumSet;

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

import ghidra.app.script.GhidraScript;
import ghidra.framework.model.ProjectData;
import ghidra.mcp.GhidraMcpServer;

import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;

public class RunMCPServer extends GhidraScript {

	@Override
	public void run() throws Exception {
		// Generate Token
		String token = generateToken();
		int port = 31337;

		printerr("Starting MCP Server on localhost:" + port + "...");
		printerr("Authentication Token: " + token);
		printerr("Ensure you pass 'Authorization: Bearer " + token + "' header in your requests.");

		ProjectData projectData = null;
		if (state.getProject() != null) {
			projectData = state.getProject().getProjectData();
		} else {
			printerr("Warning: No active project found. Some MCP features may not work.");
			// For headless or no-project cases, we might need a dummy or handle it in McpContext.
			// Currently McpContext expects ProjectData.
			// If null, tools using it will fail.
		}

		// Setup Transport
		JacksonMcpJsonMapper mapper = new JacksonMcpJsonMapper();
		HttpServletStreamableServerTransportProvider transport = HttpServletStreamableServerTransportProvider.builder()
				.jsonMapper(mapper)
				// .mcpEndpoint("/mcp") // Removed to use default root-relative endpoints (/sse, /messages)
				.build();

		// Initialize Server Logic
		GhidraMcpServer serverWrapper = new GhidraMcpServer(projectData, transport);

		// Setup Jetty with explicit localhost binding
		Server server = new Server(new InetSocketAddress("127.0.0.1", port));
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

		try {
			server.start();
			printerr("MCP Server running. Press Cancel in Ghidra to stop.");
			server.join();
		} catch (InterruptedException e) {
			printerr("Stopping server...");
		} finally {
			server.stop();
			// transport.closeGracefully().block();
		}
	}

	private String generateToken() {
		SecureRandom random = new SecureRandom();
		byte[] bytes = new byte[16];
		random.nextBytes(bytes);
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}

}

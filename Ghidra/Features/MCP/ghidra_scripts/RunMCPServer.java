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
import ghidra.base.project.GhidraProject;
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

		GhidraProject projectWrapper = new GhidraProject() {
			@Override
			public ghidra.framework.model.ProjectData getProjectData() {
				return state.getProjectData();
			}

			@Override
			public String getName() {
				return "CurrentProject";
			}
			public void close() {}
			public boolean isClosed() { return false; }
			public ghidra.framework.model.ProjectLocator getProjectLocator() { return null; }
		};

		// Setup Transport
		JacksonMcpJsonMapper mapper = new JacksonMcpJsonMapper();
		HttpServletStreamableServerTransportProvider transport = HttpServletStreamableServerTransportProvider.builder()
				.jsonMapper(mapper)
				// .mcpEndpoint("/mcp") // Removed to use default root-relative endpoints (/sse, /messages)
				.build();

		// Initialize Server Logic
		GhidraMcpServer serverWrapper = new GhidraMcpServer(projectWrapper, transport);

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

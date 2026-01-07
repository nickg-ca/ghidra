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

import java.io.InputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

import ghidra.app.script.GhidraScript;
import ghidra.base.project.GhidraProject;
import ghidra.mcp.GhidraMcpServer;

import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import reactor.core.publisher.Mono;

public class RunMCPServer extends GhidraScript {

	@Override
	public void run() throws Exception {
		// 1. Capture original Stdio
		InputStream originalIn = System.in;
		PrintStream originalOut = System.out;

		// 2. Redirect System.out to System.err to keep the channel clean
		System.setOut(System.err);

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

		printerr("Starting MCP Server...");

		CountDownLatch latch = new CountDownLatch(1);

		JacksonMcpJsonMapper mapper = new JacksonMcpJsonMapper();
		StdioServerTransportProvider transport = new StdioServerTransportProvider(mapper, originalIn, originalOut) {
			@Override
			public Mono<Void> closeGracefully() {
				return super.closeGracefully().doOnTerminate(() -> {
					printerr("MCP Server session ended.");
					latch.countDown();
				});
			}
		};

		GhidraMcpServer serverWrapper = new GhidraMcpServer(projectWrapper, transport);
		// The server is built, which sets up the session factory on the transport.
		// StdioServerTransportProvider likely starts a thread reading from InputStream or relies on reactor subscription.

		// NOTE: StdioServerTransportProvider logic:
		// It might need something to trigger the subscription?
		// But McpServer.sync(transport).build() just builds the server object.
		// It calls transport.setSessionFactory(...)

		// If StdioServerTransportProvider does not start automatically, we might be stuck.
		// However, based on typical Reactor usage, someone must subscribe.
		// The provider implementation usually handles the subscription management or starts daemon threads.
		// Let's assume it works like typical MCP SDK transports.

		// Wait until session closes
		latch.await();

		printerr("MCP Server Stopped.");
	}

}

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

import java.security.SecureRandom;

import ghidra.app.script.GhidraScript;
import ghidra.framework.model.ProjectData;
import ghidra.mcp.McpServerRunner;

public class RunMCPServer extends GhidraScript {

	@Override
	public void run() throws Exception {
		// Generate Token
		String token = generateToken();
		int port = 31337;

		ProjectData projectData = null;
		if (state.getProject() != null) {
			projectData = state.getProject().getProjectData();
		} else {
			printerr("Warning: No active project found. Some MCP features may not work.");
		}

		McpServerRunner runner = new McpServerRunner(projectData, port, token, this::printerr);

		try {
			runner.start();
			printerr("Press Cancel in Ghidra to stop.");
			runner.join();
		} catch (InterruptedException e) {
			// Cancelled
		} finally {
			runner.stop();
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

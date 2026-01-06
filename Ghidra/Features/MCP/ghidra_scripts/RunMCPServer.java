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
//Starts the MCP Server
//@category MCP
//@keybinding
//@menupath
//@toolbar

import ghidra.app.script.GhidraScript;
import ghidra.mcp.MCPServer;
import ghidra.framework.model.DomainFolder;

public class RunMCPServer extends GhidraScript {

    @Override
    public void run() throws Exception {

        println("Starting MCP Server...");

        // Pass currentProgram (can be null if run without -process)
        // Pass project root folder
        DomainFolder projectRoot = null;
        if (state.getProject() != null) {
             projectRoot = state.getProject().getProjectData().getRootFolder();
        }

        MCPServer server = new MCPServer(state.getTool(), currentProgram, projectRoot);
        server.start();

        // Keep alive loop
        // We wait indefinitely to keep the server running.
        Object lock = new Object();
        synchronized (lock) {
            lock.wait();
        }
    }
}

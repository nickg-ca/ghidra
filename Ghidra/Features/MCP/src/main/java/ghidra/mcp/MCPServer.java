package ghidra.mcp;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import ghidra.framework.plugintool.PluginTool;
import ghidra.program.model.listing.Program;
import ghidra.mcp.resources.McpResourceManager;
import ghidra.mcp.tools.DecompileTool;
import ghidra.mcp.tools.GetListingTool;
import ghidra.mcp.tools.GetSymbolTool;
import ghidra.mcp.tools.ListToolsTool;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransport;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * Main entry point for the MCP Server in Ghidra.
 */
public class MCPServer {

    private final PluginTool tool;
    private final Program program;
    private McpSyncServer mcpServer;
    private PrintStream originalStdout;

    public MCPServer(PluginTool tool, Program program) {
        this.tool = tool;
        this.program = program;
    }

    public void start() {
        // 1. Capture Original Stdout
        originalStdout = System.out;

        // 2. Initialize Transport with Original Stdout
        // We instantiate this BEFORE redirecting System.out.
        // We rely on StdioServerTransport capturing System.out at construction time.
        // If it doesn't, and uses System.out lazily, we would need a library change or reflection,
        // but typically these transports capture the stream.
        StdioServerTransport transport = new StdioServerTransport();

        // 3. Redirect System.out to System.err
        // This prevents Ghidra's console logs (which go to System.out) from corrupting the JSON-RPC stream (originalStdout).
        System.setOut(System.err);

        // 4. Initialize Registry and Helpers
        McpToolRegistry toolRegistry = new McpToolRegistry();
        registerTools(toolRegistry);

        McpResourceManager resourceManager = new McpResourceManager();

        // 5. Initialize Server
        mcpServer = McpServer.sync(transport)
                .serverInfo("GhidraMCP", "1.0.0")
                .capabilities(McpSchema.ServerCapabilities.builder()
                        .resources(false, true) // subscribe, listChanged
                        .tools(true) // listChanged
                        .prompts(false)
                        .logging()
                        .build())
                .tools(toolRegistry.getRegisteredTools().values().stream().toList())
                .resources(resourceManager.getResources())
                .build();

         // Note: McpSyncServer usually starts listening upon creation/build if the transport is active.
         // StdioServerTransport likely starts its reading thread in its constructor or when attached.
    }

    private void registerTools(McpToolRegistry registry) {
        // Helper to register tools
        registerTool(registry, new ListToolsTool());
        registerTool(registry, new GetListingTool());
        registerTool(registry, new DecompileTool());
        registerTool(registry, new GetSymbolTool());
    }

    private void registerTool(McpToolRegistry registry, McpTool toolInstance) {
        registry.register(
            toolInstance.getToolDef(),
            toolInstance.getHandler(tool, program)
        );
    }

    public void stop() {
        if (mcpServer != null) {
            mcpServer.close();
        }
        // Restore stdout
         if (originalStdout != null) {
             System.setOut(originalStdout);
         }
    }
}

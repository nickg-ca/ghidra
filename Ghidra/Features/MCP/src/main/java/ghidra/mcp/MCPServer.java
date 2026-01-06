package ghidra.mcp;

import java.io.PrintStream;

import ghidra.framework.model.DomainFolder;
import ghidra.framework.plugintool.PluginTool;
import ghidra.program.model.listing.Program;
import ghidra.mcp.resources.McpResourceManager;
import ghidra.mcp.tools.*;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransport;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * Main entry point for the MCP Server in Ghidra.
 */
public class MCPServer {

    private final McpContext context;
    private McpSyncServer mcpServer;
    private PrintStream originalStdout;

    public MCPServer(PluginTool tool, Program program, DomainFolder projectRoot) {
        this.context = new McpContext(tool, program, projectRoot);
    }

    public void start() {
        // 1. Capture Original Stdout
        originalStdout = System.out;

        // 2. Initialize Transport with Original Stdout
        StdioServerTransport transport = new StdioServerTransport();

        // 3. Redirect System.out to System.err
        System.setOut(System.err);

        // 4. Initialize Registry and Helpers
        McpToolRegistry toolRegistry = new McpToolRegistry();
        registerTools(toolRegistry);

        McpResourceManager resourceManager = new McpResourceManager();

        // 5. Initialize Server
        mcpServer = McpServer.sync(transport)
                .serverInfo("GhidraMCP", "1.0.0")
                .capabilities(McpSchema.ServerCapabilities.builder()
                        .resources(false, true)
                        .tools(true)
                        .prompts(false)
                        .logging()
                        .build())
                .tools(toolRegistry.getRegisteredTools().values().stream().toList())
                .resources(resourceManager.getResources())
                .build();
    }

    private void registerTools(McpToolRegistry registry) {
        // Core Tools
        registerTool(registry, new ListToolsTool());

        // Project Management Tools
        registerTool(registry, new ListFilesTool());
        registerTool(registry, new OpenProgramTool());
        registerTool(registry, new CloseProgramTool());

        // Reversing Tools
        registerTool(registry, new GetListingTool());
        registerTool(registry, new DecompileTool());
        registerTool(registry, new GetSymbolTool());
        registerTool(registry, new ReadBytesTool());
        registerTool(registry, new SetLabelTool());
        registerTool(registry, new SetCommentTool());
        registerTool(registry, new WriteBytesTool());
    }

    private void registerTool(McpToolRegistry registry, McpTool toolInstance) {
        registry.register(
            toolInstance.getToolDef(),
            toolInstance.getHandler(context)
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

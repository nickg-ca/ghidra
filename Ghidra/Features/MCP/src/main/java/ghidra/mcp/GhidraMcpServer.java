package ghidra.mcp;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;

import ghidra.base.project.GhidraProject;
import ghidra.mcp.tools.*;
import ghidra.mcp.prompts.*;

// Explicit imports to ensure no compilation issues if wildcard is flaky
import ghidra.mcp.tools.SetLabelTool;
import ghidra.mcp.tools.SetCommentTool;
import ghidra.mcp.tools.WriteBytesTool;
import ghidra.mcp.tools.GetReferencesTool;
import ghidra.mcp.tools.SearchMemoryTool;
import ghidra.mcp.tools.ExecuteBatchTool;

public class GhidraMcpServer {

	private final McpSyncServer mcpServer;
	private final McpContext context;
	private final McpToolRegistry toolRegistry;
	private final McpPromptRegistry promptRegistry;

	public GhidraMcpServer(GhidraProject project, StdioServerTransportProvider transport) {
		this.context = new McpContext(project);
		this.toolRegistry = new McpToolRegistry();
		this.promptRegistry = new McpPromptRegistry();

		registerTools();
		registerPrompts();

		var builder = McpServer.sync(transport)
			.serverInfo("Ghidra", "1.0.0")
			.capabilities(McpSchema.ServerCapabilities.builder()
				.tools(false) // No listChanged notifications for now
				.prompts(false)
				.build());

		// Register tools
		for (McpTool tool : toolRegistry.getTools()) {
			builder.tool(tool.getToolDef(), (exchange, args) -> {
				try {
					return tool.execute(args);
				} catch (Exception e) {
					return new McpSchema.CallToolResult(
						Collections.singletonList(new McpSchema.TextContent("Error: " + e.getMessage())),
						true
					);
				}
			});
		}

		// Register prompts
		var promptSpecs = new java.util.ArrayList<SyncPromptSpecification>();
		for (McpPrompt prompt : promptRegistry.getPrompts()) {
			promptSpecs.add(new SyncPromptSpecification(
				prompt.getPromptDef(),
				(exchange, req) -> {
					try {
						return prompt.execute(convertToStringMap(req.arguments()));
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			));
		}
		builder.prompts(promptSpecs);

		this.mcpServer = builder.build();
	}

	private Map<String, String> convertToStringMap(Map<String, Object> args) {
		if (args == null) return Collections.emptyMap();
		Map<String, String> res = new HashMap<>();
		for (Map.Entry<String, Object> entry : args.entrySet()) {
			res.put(entry.getKey(), String.valueOf(entry.getValue()));
		}
		return res;
	}

	private void registerTools() {
		toolRegistry.register(new ListModulesTool(context));
		toolRegistry.register(new OpenModuleTool(context));
		toolRegistry.register(new CloseModuleTool(context));
		toolRegistry.register(new DecompileTool(context));
		toolRegistry.register(new ReadBytesTool(context));
		toolRegistry.register(new WriteBytesTool(context));
		toolRegistry.register(new GetListingTool(context));
		toolRegistry.register(new GetLogTool(context));
		toolRegistry.register(new SetLabelTool(context));
		toolRegistry.register(new SetCommentTool(context));
		toolRegistry.register(new GetReferencesTool(context));
		toolRegistry.register(new SearchMemoryTool(context));
		toolRegistry.register(new ExecuteBatchTool(toolRegistry));
	}

	private void registerPrompts() {
		promptRegistry.register(new ExplainFunctionPrompt(context));
		promptRegistry.register(new AnalyzeVulnerabilityPrompt(context));
	}

	public McpSyncServer getServer() {
		return mcpServer;
	}

}

package ghidra.mcp.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ghidra.mcp.McpTool;
import ghidra.mcp.McpToolRegistry;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;

public class ExecuteBatchTool implements McpTool {
	private final McpToolRegistry registry;

	public ExecuteBatchTool(McpToolRegistry registry) {
		this.registry = registry;
	}

	@Override
	public String getName() {
		return "execute_batch";
	}

	@Override
	public Tool getToolDef() {
		return Tool.builder()
			.name(getName())
			.description("Executes a list of tool calls sequentially.")
			.inputSchema(new JsonSchema(
				"object",
				Map.of(
					"operations", Map.of(
						"type", "array",
						"items", Map.of(
							"type", "object",
							"properties", Map.of(
								"tool", Map.of("type", "string"),
								"args", Map.of("type", "object")
							),
							"required", List.of("tool", "args")
						)
					)
				),
				List.of("operations"),
				false,
				null,
				null
			))
			.build();
	}

	@Override
	public CallToolResult execute(Map<String, Object> arguments) throws Exception {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> ops = (List<Map<String, Object>>) arguments.get("operations");
		if (ops == null) {
			return new CallToolResult(Collections.singletonList(new TextContent("Error: No operations provided.")), true);
		}

		List<Content> results = new ArrayList<>();
		boolean isError = false;

		for (Map<String, Object> op : ops) {
			String toolName = (String) op.get("tool");
			@SuppressWarnings("unchecked")
			Map<String, Object> args = (Map<String, Object>) op.get("args");

			McpTool tool = registry.getTool(toolName);
			if (tool == null) {
				results.add(new TextContent("Error: Tool not found: " + toolName));
				isError = true;
				continue;
			}

			try {
				CallToolResult res = tool.execute(args);
				// Combine results. We prefix slightly to distinguish
				results.add(new TextContent("--- Result for " + toolName + " ---"));
				results.addAll(res.content());
				if (res.isError()) {
					isError = true;
				}
			} catch (Exception e) {
				results.add(new TextContent("Error executing " + toolName + ": " + e.getMessage()));
				isError = true;
			}
		}

		return new CallToolResult(results, isError);
	}
}

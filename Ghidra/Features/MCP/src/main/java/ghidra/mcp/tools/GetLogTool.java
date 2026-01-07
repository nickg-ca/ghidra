package ghidra.mcp.tools;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import ghidra.mcp.McpContext;
import ghidra.mcp.McpTool;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class GetLogTool implements McpTool {
	private final McpContext context;

	public GetLogTool(McpContext context) {
		this.context = context;
	}

	@Override
	public String getName() {
		return "get_log";
	}

	@Override
	public Tool getToolDef() {
		return Tool.builder()
			.name(getName())
			.description("Gets the recent logs captured by the server.")
			.build();
	}

	@Override
	public CallToolResult execute(Map<String, Object> arguments) throws Exception {
		return new CallToolResult(Collections.singletonList(new TextContent("Log capture not yet implemented.")), false);
	}
}

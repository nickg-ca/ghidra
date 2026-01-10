package ghidra.mcp.tools;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import ghidra.mcp.McpContext;
import ghidra.mcp.McpTool;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class CloseModuleTool implements McpTool {
	private final McpContext context;

	public CloseModuleTool(McpContext context) {
		this.context = context;
	}

	@Override
	public String getName() {
		return "close_module";
	}

	@Override
	public Tool getToolDef() {
		return Tool.builder()
			.name(getName())
			.description("Closes the currently open module.")
			.build();
	}

	@Override
	public CallToolResult execute(Map<String, Object> arguments) throws Exception {
		if (context.getCurrentProgram() != null) {
			String name = context.getCurrentProgram().getName();
			// Simplified cleanup (see notes in previous impl)
			context.setCurrentProgram(null, null);
			return new CallToolResult(Collections.singletonList(new TextContent("Closed module: " + name)), false);
		}
		return new CallToolResult(Collections.singletonList(new TextContent("No module open.")), false);
	}
}

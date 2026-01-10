package ghidra.mcp.tools;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import ghidra.framework.model.DomainFile;
import ghidra.program.model.listing.Program;
import ghidra.mcp.McpContext;
import ghidra.mcp.McpTool;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;

public class OpenModuleTool implements McpTool {
	private final McpContext context;

	public OpenModuleTool(McpContext context) {
		this.context = context;
	}

	@Override
	public String getName() {
		return "open_module";
	}

	@Override
	public Tool getToolDef() {
		return Tool.builder()
			.name(getName())
			.description("Opens a module (program) from the project by path.")
			.inputSchema(new JsonSchema(
				"object",
				Map.of("path", Map.of("type", "string", "description", "The path to the module (e.g., /MyBinary)")),
				List.of("path"),
				false,
				null,
				null
			))
			.build();
	}

	@Override
	public CallToolResult execute(Map<String, Object> arguments) throws Exception {
		String path = (String) arguments.get("path");
		if (path == null) throw new IllegalArgumentException("Path is required");

		// Normalize path
		if (!path.startsWith("/")) path = "/" + path;

		DomainFile df = context.getProjectData().getFile(path);
		if (df == null) {
			return new CallToolResult(Collections.singletonList(new TextContent("Error: File not found: " + path)), true);
		}

		Object consumer = new Object();
		ghidra.framework.model.DomainObject obj = df.getDomainObject(consumer, true, false, ghidra.util.task.TaskMonitor.DUMMY);

		if (obj instanceof Program) {
			context.setCurrentProgram((Program) obj, consumer);
			return new CallToolResult(Collections.singletonList(new TextContent("Successfully opened: " + path)), false);
		} else {
			obj.release(consumer);
			return new CallToolResult(Collections.singletonList(new TextContent("Error: File is not a Program: " + path)), true);
		}
	}
}

package ghidra.mcp;

import java.util.Map;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public interface McpTool {
	String getName();
	Tool getToolDef();
	CallToolResult execute(Map<String, Object> arguments) throws Exception;
}

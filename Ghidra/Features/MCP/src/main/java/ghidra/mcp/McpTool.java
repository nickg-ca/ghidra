package ghidra.mcp;

import java.util.Map;
import java.util.function.Function;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public interface McpTool {
    Tool getToolDef();
    Function<Map<String, Object>, CallToolResult> getHandler(McpContext context);
}

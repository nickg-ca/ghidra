package ghidra.mcp;

import java.util.Map;
import java.util.function.Function;

import ghidra.framework.plugintool.PluginTool;
import ghidra.program.model.listing.Program;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public interface McpTool {
    Tool getToolDef();
    Function<Map<String, Object>, CallToolResult> getHandler(PluginTool tool, Program program);
}

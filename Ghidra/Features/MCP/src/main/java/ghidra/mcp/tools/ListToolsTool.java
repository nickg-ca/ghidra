package ghidra.mcp.tools;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import ghidra.framework.plugintool.PluginTool;
import ghidra.mcp.McpContext;
import ghidra.mcp.McpTool;
import ghidra.program.model.listing.Program;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class ListToolsTool implements McpTool {

    @Override
    public Tool getToolDef() {
        return new Tool("list_tools_info", "List available tools and their descriptions", (String) null);
    }

    @Override
    public Function<Map<String, Object>, CallToolResult> getHandler(McpContext context) {
        return args -> {
            return new CallToolResult(
                    List.of(new TextContent("Tools are self-describing via the tools/list capability.")),
                    false
            );
        };
    }
}

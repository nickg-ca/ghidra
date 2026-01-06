package ghidra.mcp.tools;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import ghidra.framework.plugintool.PluginTool;
import ghidra.mcp.McpTool;
import ghidra.program.model.listing.Program;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class ListToolsTool implements McpTool {

    @Override
    public Tool getToolDef() {
        // Use the constructor taking a String for input schema, but passing null string is tricky if it expects JSON
        // The previous javap showed: Tool(String name, String description, String inputSchema)
        // So we can pass a JSON string.
        return new Tool("list_tools_info", "List available tools and their descriptions", (String) null);
    }

    @Override
    public Function<Map<String, Object>, CallToolResult> getHandler(PluginTool tool, Program program) {
        return args -> {
            return new CallToolResult(
                    List.of(new TextContent("Tools are self-describing via the tools/list capability.")),
                    false
            );
        };
    }
}

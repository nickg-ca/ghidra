package ghidra.mcp.tools;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import ghidra.mcp.McpContext;
import ghidra.mcp.McpTool;
import ghidra.program.model.listing.Program;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class CloseProgramTool implements McpTool {

    @Override
    public Tool getToolDef() {
        return new Tool(
            "close_program",
            "Close the currently open program",
            (String) null
        );
    }

    @Override
    public Function<Map<String, Object>, CallToolResult> getHandler(McpContext context) {
        return args -> {
            try {
                Program current = context.getCurrentProgram();
                if (current == null) {
                    return new CallToolResult(List.of(new TextContent("No program is open.")), false);
                }

                // We attempt to release it.
                // Note: If the initial program was opened by the script runner, we might not own it fully?
                // But releasing it from our context is good.
                // However, we passed `context` as consumer for new programs.
                // For the initial one, `RunMCPServer` opened it?
                // `currentProgram` in GhidraScript is managed by the script framework.

                // If we opened it via OpenProgramTool, consumer is context.
                current.release(context);
                context.setCurrentProgram(null);

                return new CallToolResult(
                    List.of(new TextContent("Program closed.")),
                    false
                );
            } catch (Exception e) {
                 return new CallToolResult(
                    List.of(new TextContent("Error closing program: " + e.getMessage())),
                    true
                );
            }
        };
    }
}

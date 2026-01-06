package ghidra.mcp.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import ghidra.mcp.McpContext;
import ghidra.mcp.McpTool;
import ghidra.mcp.McpToolRegistry;
import ghidra.program.model.listing.Program;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolRegistration;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class ExecuteBatchTool implements McpTool {

    private final McpToolRegistry registry;

    public ExecuteBatchTool(McpToolRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Tool getToolDef() {
        return new Tool(
            "execute_batch",
            "Executes a list of tool calls sequentially within a single transaction. " +
            "Input is a list of objects, each with 'tool' (string) and 'args' (object). " +
            "Returns a list of results or fails on the first error.",
            Map.of(
                "type", "object",
                "properties", Map.of(
                    "requests", Map.of(
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
                "required", List.of("requests")
            )
        );
    }

    @Override
    public Function<Map<String, Object>, CallToolResult> getHandler(McpContext context) {
        return args -> {
            Program program = context.getCurrentProgram();
            if (program == null) {
                return new CallToolResult(
                    List.of(new TextContent("Error: No program is currently open.")),
                    true
                );
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> requests = (List<Map<String, Object>>) args.get("requests");
            if (requests == null || requests.isEmpty()) {
                return new CallToolResult(
                    List.of(new TextContent("Error: 'requests' list is empty or missing.")),
                    true
                );
            }

            int tid = program.startTransaction("Batch Execution");
            List<Content> batchResults = new ArrayList<>();
            boolean errorOccurred = false;

            try {
                for (int i = 0; i < requests.size(); i++) {
                    Map<String, Object> req = requests.get(i);
                    String toolName = (String) req.get("tool");
                    @SuppressWarnings("unchecked")
                    Map<String, Object> toolArgs = (Map<String, Object>) req.get("args");

                    if (toolName == null || toolName.equals("execute_batch")) { // Prevent recursion
                        throw new IllegalArgumentException("Invalid tool name or recursion detected: " + toolName);
                    }

                    SyncToolRegistration toolReg = registry.getTool(toolName);
                    if (toolReg == null) {
                        throw new IllegalArgumentException("Tool not found: " + toolName);
                    }

                    // Execute the tool
                    CallToolResult result = toolReg.handler().apply(toolArgs);

                    // Collect results
                    if (result.isError()) {
                        batchResults.add(new TextContent("Step " + i + " (" + toolName + ") Failed: " + result.content().toString()));
                        errorOccurred = true;
                        break; // Stop execution on error
                    } else {
                        // Append success output for this step
                        // We convert the list of content to a string representation for the batch summary
                        StringBuilder stepOutput = new StringBuilder();
                        for (Content c : result.content()) {
                             if (c instanceof TextContent) {
                                 stepOutput.append(((TextContent)c).text()).append("\n");
                             } else {
                                 stepOutput.append(c.toString()).append("\n");
                             }
                        }
                        batchResults.add(new TextContent("Step " + i + " (" + toolName + ") Success:\n" + stepOutput.toString()));
                    }
                }
            } catch (Exception e) {
                batchResults.add(new TextContent("Batch Execution Exception: " + e.getMessage()));
                errorOccurred = true;
            } finally {
                program.endTransaction(tid, !errorOccurred); // Commit if no error, otherwise rollback (roughly)
            }

            return new CallToolResult(batchResults, errorOccurred);
        };
    }
}

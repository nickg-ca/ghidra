package ghidra.mcp.tools;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.Gson;

import ghidra.framework.model.DomainFile;
import ghidra.framework.model.DomainFolder;
import ghidra.mcp.McpContext;
import ghidra.mcp.McpTool;
import ghidra.program.model.listing.Program;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class OpenProgramTool implements McpTool {

    private static final Gson gson = new Gson();

    @Override
    public Tool getToolDef() {
        Map<String, Object> schema = Map.of(
            "type", "object",
            "properties", Map.of(
                "path", Map.of("type", "string", "description", "Path to the program within the project (e.g. /my_binary)")
            ),
            "required", List.of("path")
        );

        return new Tool(
            "open_program",
            "Open a program from the project",
            gson.toJson(schema)
        );
    }

    @Override
    public Function<Map<String, Object>, CallToolResult> getHandler(McpContext context) {
        return args -> {
            try {
                String path = (String) args.get("path");
                DomainFolder root = context.getProjectRoot();

                if (root == null) {
                     return new CallToolResult(List.of(new TextContent("No active project.")), true);
                }

                DomainFile file = getFileByPath(root, path);
                if (file == null) {
                    return new CallToolResult(List.of(new TextContent("File not found: " + path)), true);
                }

                // Close current if open
                Program current = context.getCurrentProgram();
                if (current != null) {
                    // Fix: Use 'context' as the consumer, consistent with how we open programs below
                    current.release(context);
                }

                // Open new program
                // DomainFile.getDomainObject(consumer, boolean upgrade, boolean recover, TaskMonitor)
                Object consumer = context; // Use context as consumer
                Program newProgram = (Program) file.getDomainObject(consumer, false, false, ghidra.util.task.TaskMonitor.DUMMY);

                if (newProgram == null) {
                     return new CallToolResult(List.of(new TextContent("Failed to open program.")), true);
                }

                context.setCurrentProgram(newProgram);

                return new CallToolResult(
                    List.of(new TextContent("Opened program: " + newProgram.getName())),
                    false
                );

            } catch (Exception e) {
                return new CallToolResult(
                    List.of(new TextContent("Error opening program: " + e.getMessage())),
                    true
                );
            }
        };
    }

    private DomainFile getFileByPath(DomainFolder folder, String path) {
        // Simple path traversal
        // Assume path starts with / or is relative to root
        if (path.startsWith("/")) path = path.substring(1);

        String[] parts = path.split("/");
        DomainFolder current = folder;

        for (int i = 0; i < parts.length - 1; i++) {
            current = current.getFolder(parts[i]);
            if (current == null) return null;
        }

        return current.getFile(parts[parts.length - 1]);
    }
}

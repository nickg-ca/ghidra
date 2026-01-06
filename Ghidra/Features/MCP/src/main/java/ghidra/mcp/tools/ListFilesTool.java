package ghidra.mcp.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.Gson;

import ghidra.framework.model.DomainFile;
import ghidra.framework.model.DomainFolder;
import ghidra.mcp.McpContext;
import ghidra.mcp.McpTool;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class ListFilesTool implements McpTool {

    private static final Gson gson = new Gson();

    @Override
    public Tool getToolDef() {
        return new Tool(
            "list_project_files",
            "List all files in the current Ghidra project",
            (String) null
        );
    }

    @Override
    public Function<Map<String, Object>, CallToolResult> getHandler(McpContext context) {
        return args -> {
            try {
                DomainFolder root = context.getProjectRoot();
                if (root == null) {
                    return new CallToolResult(
                        List.of(new TextContent("No project is currently active or accessible.")),
                        true
                    );
                }

                List<String> files = new ArrayList<>();
                collectFiles(root, "/", files);

                return new CallToolResult(
                    List.of(new TextContent(String.join("\n", files))),
                    false
                );
            } catch (Exception e) {
                 return new CallToolResult(
                    List.of(new TextContent("Error listing files: " + e.getMessage())),
                    true
                );
            }
        };
    }

    private void collectFiles(DomainFolder folder, String path, List<String> result) {
        for (DomainFile file : folder.getFiles()) {
            result.add(path + file.getName());
        }
        for (DomainFolder sub : folder.getFolders()) {
            collectFiles(sub, path + sub.getName() + "/", result);
        }
    }
}

package ghidra.mcp.tools;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.Gson;

import ghidra.mcp.McpContext;
import ghidra.mcp.McpTool;
import ghidra.program.model.listing.CodeUnit;
import ghidra.program.model.listing.Listing;
import ghidra.program.model.listing.Program;
import ghidra.program.model.address.Address;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class SetCommentTool implements McpTool {

    private static final Gson gson = new Gson();

    @Override
    public Tool getToolDef() {
        Map<String, Object> schema = Map.of(
            "type", "object",
            "properties", Map.of(
                "address", Map.of("type", "string", "description", "Address to set the comment"),
                "comment", Map.of("type", "string", "description", "The comment text"),
                "type", Map.of(
                    "type", "string",
                    "description", "Type of comment (plate, pre, eol, post, repeatable). Defaults to eol.",
                    "enum", List.of("plate", "pre", "eol", "post", "repeatable")
                )
            ),
            "required", List.of("address", "comment")
        );

        return new Tool(
            "set_comment",
            "Set a comment at a specific address",
            gson.toJson(schema)
        );
    }

    @Override
    public Function<Map<String, Object>, CallToolResult> getHandler(McpContext context) {
        return args -> {
            try {
                Program program = context.getCurrentProgram();
                if (program == null) {
                    return new CallToolResult(List.of(new TextContent("No active program.")), true);
                }

                String addrStr = (String) args.get("address");
                String comment = (String) args.get("comment");
                String typeStr = (String) args.getOrDefault("type", "eol");

                Address addr = program.getAddressFactory().getAddress(addrStr);
                if (addr == null) {
                     return new CallToolResult(
                        List.of(new TextContent("Invalid address: " + addrStr)),
                        true
                    );
                }

                int commentType;
                switch (typeStr.toLowerCase()) {
                    case "plate": commentType = CodeUnit.PLATE_COMMENT; break;
                    case "pre": commentType = CodeUnit.PRE_COMMENT; break;
                    case "eol": commentType = CodeUnit.EOL_COMMENT; break;
                    case "post": commentType = CodeUnit.POST_COMMENT; break;
                    case "repeatable": commentType = CodeUnit.REPEATABLE_COMMENT; break;
                    default: commentType = CodeUnit.EOL_COMMENT;
                }

                Listing listing = program.getListing();

                // We need a transaction to modify the program
                int tid = program.startTransaction("Set Comment");
                try {
                    listing.setComment(addr, commentType, comment);
                } finally {
                    program.endTransaction(tid, true);
                }

                return new CallToolResult(
                    List.of(new TextContent("Comment set at " + addrStr)),
                    false
                );

            } catch (Exception e) {
                return new CallToolResult(
                    List.of(new TextContent("Error: " + e.getMessage())),
                    true
                );
            }
        };
    }
}

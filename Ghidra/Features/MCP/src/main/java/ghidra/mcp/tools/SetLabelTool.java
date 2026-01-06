package ghidra.mcp.tools;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.Gson;

import ghidra.mcp.McpContext;
import ghidra.mcp.McpTool;
import ghidra.program.model.listing.Program;
import ghidra.program.model.address.Address;
import ghidra.program.model.symbol.SourceType;
import ghidra.program.model.symbol.SymbolTable;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class SetLabelTool implements McpTool {

    private static final Gson gson = new Gson();

    @Override
    public Tool getToolDef() {
        Map<String, Object> schema = Map.of(
            "type", "object",
            "properties", Map.of(
                "address", Map.of("type", "string", "description", "Address to set the label"),
                "label", Map.of("type", "string", "description", "The name of the label")
            ),
            "required", List.of("address", "label")
        );

        return new Tool(
            "set_label",
            "Set a label (symbol) at a specific address",
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
                String labelName = (String) args.get("label");

                Address addr = program.getAddressFactory().getAddress(addrStr);
                if (addr == null) {
                     return new CallToolResult(
                        List.of(new TextContent("Invalid address: " + addrStr)),
                        true
                    );
                }

                SymbolTable symbolTable = program.getSymbolTable();

                // We need a transaction to modify the program
                int tid = program.startTransaction("Set Label");
                try {
                    symbolTable.createLabel(addr, labelName, SourceType.USER_DEFINED);
                } finally {
                    program.endTransaction(tid, true);
                }

                return new CallToolResult(
                    List.of(new TextContent("Label '" + labelName + "' set at " + addrStr)),
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

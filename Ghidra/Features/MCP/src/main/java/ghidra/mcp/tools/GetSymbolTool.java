package ghidra.mcp.tools;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.Gson;

import ghidra.framework.plugintool.PluginTool;
import ghidra.mcp.McpTool;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Program;
import ghidra.program.model.symbol.Symbol;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class GetSymbolTool implements McpTool {

    private static final Gson gson = new Gson();

    @Override
    public Tool getToolDef() {
        Map<String, Object> schema = Map.of(
            "type", "object",
            "properties", Map.of(
                "address", Map.of("type", "string", "description", "Address to query")
            ),
            "required", List.of("address")
        );

         return new Tool(
            "get_symbol",
            "Get symbol information at an address",
            gson.toJson(schema)
        );
    }

    @Override
    public Function<Map<String, Object>, CallToolResult> getHandler(PluginTool tool, Program program) {
        return args -> {
             try {
                String addrStr = (String) args.get("address");
                Address addr = program.getAddressFactory().getAddress(addrStr);

                if (addr == null) {
                    return new CallToolResult(List.of(new TextContent("Invalid address")), true);
                }

                Symbol sym = program.getSymbolTable().getPrimarySymbol(addr);
                String result = "";
                if (sym != null) {
                    result = "Name: " + sym.getName() + "\nNamespace: " + sym.getParentNamespace().getName();
                } else {
                    result = "No primary symbol found.";
                }

                return new CallToolResult(
                    List.of(new TextContent(result)),
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

package ghidra.mcp.tools;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.Gson;

import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.framework.plugintool.PluginTool;
import ghidra.mcp.McpTool;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.FunctionIterator;
import ghidra.program.model.listing.Program;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class DecompileTool implements McpTool {

    private static final Gson gson = new Gson();

    @Override
    public Tool getToolDef() {
        Map<String, Object> schema = Map.of(
            "type", "object",
            "properties", Map.of(
                "address", Map.of("type", "string", "description", "Address within the function")
            ),
            "required", List.of("address")
        );

        return new Tool(
            "decompile_function",
            "Decompile the function at the given address",
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

                ghidra.program.model.listing.Function func = program.getFunctionManager().getFunctionContaining(addr);
                if (func == null) {
                     return new CallToolResult(List.of(new TextContent("No function found at address")), true);
                }

                DecompInterface ifc = new DecompInterface();
                ifc.openProgram(program);

                DecompileResults res = ifc.decompileFunction(func, 60, null);

                String code = "";
                if (res.decompileCompleted()) {
                    code = res.getDecompiledFunction().getC();
                } else {
                    code = "Decompilation failed: " + res.getErrorMessage();
                }

                ifc.dispose();

                return new CallToolResult(
                    List.of(new TextContent(code)),
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

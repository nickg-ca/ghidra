package ghidra.mcp.tools;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.Gson;

import ghidra.framework.plugintool.PluginTool;
import ghidra.mcp.McpContext;
import ghidra.mcp.McpTool;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.FunctionIterator;
import ghidra.program.model.listing.Program;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class DecompileTool implements McpTool {

    private static final Gson gson = new Gson();
    private final DecompilerFactory factory;

    public DecompileTool() {
        this(() -> new DefaultDecompilerWrapper());
    }

    public DecompileTool(DecompilerFactory factory) {
        this.factory = factory;
    }

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
    public Function<Map<String, Object>, CallToolResult> getHandler(McpContext context) {
        return args -> {
            try {
                Program program = context.getCurrentProgram();
                if (program == null) {
                    return new CallToolResult(List.of(new TextContent("No active program.")), true);
                }

                String addrStr = (String) args.get("address");
                Address addr = program.getAddressFactory().getAddress(addrStr);

                if (addr == null) {
                    return new CallToolResult(List.of(new TextContent("Invalid address")), true);
                }

                ghidra.program.model.listing.Function func = program.getFunctionManager().getFunctionContaining(addr);
                if (func == null) {
                     return new CallToolResult(List.of(new TextContent("No function found at address")), true);
                }

                DecompilerWrapper ifc = factory.create();
                try {
                    ifc.openProgram(program);

                    McpDecompileResult res = ifc.decompileFunction(func, 60, null);

                    String code = "";
                    if (res != null && res.decompileCompleted()) {
                        code = res.getC();
                    } else {
                        String msg = (res != null) ? res.getErrorMessage() : "Unknown error";
                        code = "Decompilation failed: " + msg;
                    }

                    return new CallToolResult(
                        List.of(new TextContent(code)),
                        false
                    );
                } finally {
                    ifc.dispose();
                }

            } catch (Exception e) {
                return new CallToolResult(
                    List.of(new TextContent("Error: " + e.getMessage())),
                    true
                );
            }
        };
    }
}

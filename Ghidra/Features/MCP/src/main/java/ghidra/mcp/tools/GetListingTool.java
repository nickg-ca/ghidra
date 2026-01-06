package ghidra.mcp.tools;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.Gson;

import ghidra.framework.plugintool.PluginTool;
import ghidra.mcp.McpTool;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressFactory;
import ghidra.program.model.listing.CodeUnit;
import ghidra.program.model.listing.CodeUnitIterator;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.listing.Listing;
import ghidra.program.model.listing.Program;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class GetListingTool implements McpTool {

    private static final Gson gson = new Gson();

    @Override
    public Tool getToolDef() {
        Map<String, Object> schema = Map.of(
            "type", "object",
            "properties", Map.of(
                "address", Map.of("type", "string", "description", "Start address"),
                "length", Map.of("type", "integer", "description", "Number of code units to retrieve")
            ),
            "required", List.of("address", "length")
        );

        return new Tool(
            "get_listing",
            "Get the disassembly listing for a range of addresses",
            gson.toJson(schema)
        );
    }

    @Override
    public Function<Map<String, Object>, CallToolResult> getHandler(PluginTool tool, Program program) {
        return args -> {
            try {
                String addrStr = (String) args.get("address");
                int length = ((Number) args.get("length")).intValue();

                AddressFactory addrFactory = program.getAddressFactory();
                Address startAddr = addrFactory.getAddress(addrStr);

                if (startAddr == null) {
                     return new CallToolResult(
                        List.of(new TextContent("Invalid address: " + addrStr)),
                        true
                    );
                }

                Listing listing = program.getListing();
                CodeUnitIterator codeUnits = listing.getCodeUnits(startAddr, true);
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < length && codeUnits.hasNext(); i++) {
                    CodeUnit cu = codeUnits.next();
                    sb.append(cu.getAddressString(false, true)).append(": ");
                    sb.append(cu.getMnemonicString()).append(" ");

                    if (cu instanceof Instruction) {
                        Instruction instr = (Instruction) cu;
                        int numOperands = instr.getNumOperands();
                        for(int op=0; op<numOperands; op++) {
                            sb.append(instr.getDefaultOperandRepresentation(op));
                            if (op < numOperands -1) sb.append(", ");
                        }
                    } else {
                        // For data, just print what we can, usually mnemonics handles it or we need data specific methods
                        // But CodeUnit.getMnemonicString usually covers basic data type name.
                        // We can also try to get value representation if needed.
                        // For now, simple is fine.
                    }
                    sb.append("\n");
                }

                return new CallToolResult(
                    List.of(new TextContent(sb.toString())),
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

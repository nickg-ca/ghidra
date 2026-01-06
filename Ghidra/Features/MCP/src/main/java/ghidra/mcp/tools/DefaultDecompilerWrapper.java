package ghidra.mcp.tools;

import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Program;
import ghidra.util.task.TaskMonitor;

public class DefaultDecompilerWrapper implements DecompilerWrapper {

    private final DecompInterface decompiler;

    public DefaultDecompilerWrapper() {
        this.decompiler = new DecompInterface();
    }

    @Override
    public void openProgram(Program program) {
        decompiler.openProgram(program);
    }

    @Override
    public McpDecompileResult decompileFunction(Function func, int timeoutSecs, TaskMonitor monitor) {
        DecompileResults results = decompiler.decompileFunction(func, timeoutSecs, monitor);
        return new McpDecompileResult() {
            @Override
            public boolean decompileCompleted() {
                return results.decompileCompleted();
            }

            @Override
            public String getErrorMessage() {
                return results.getErrorMessage();
            }

            @Override
            public String getC() {
                if (results.getDecompiledFunction() != null) {
                    return results.getDecompiledFunction().getC();
                }
                return "";
            }
        };
    }

    @Override
    public void dispose() {
        decompiler.dispose();
    }
}

package ghidra.mcp.tools;

import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Program;

public interface DecompilerWrapper {
    void openProgram(Program program);
    McpDecompileResult decompileFunction(Function func, int timeoutSecs, ghidra.util.task.TaskMonitor monitor);
    void dispose();
}

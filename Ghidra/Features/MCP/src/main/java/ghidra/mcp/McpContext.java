package ghidra.mcp;

import ghidra.framework.model.DomainFolder;
import ghidra.framework.plugintool.PluginTool;
import ghidra.program.model.listing.Program;

public class McpContext {
    private PluginTool tool;
    private Program currentProgram;
    private DomainFolder projectRoot;

    public McpContext(PluginTool tool, Program currentProgram, DomainFolder projectRoot) {
        this.tool = tool;
        this.currentProgram = currentProgram;
        this.projectRoot = projectRoot;
    }

    public synchronized Program getCurrentProgram() {
        return currentProgram;
    }

    public synchronized void setCurrentProgram(Program program) {
        this.currentProgram = program;
    }

    public PluginTool getTool() {
        return tool;
    }

    public DomainFolder getProjectRoot() {
        return projectRoot;
    }
}

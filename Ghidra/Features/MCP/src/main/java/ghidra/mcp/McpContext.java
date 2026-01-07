package ghidra.mcp;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import ghidra.base.project.GhidraProject;
import ghidra.framework.model.DomainFile;
import ghidra.framework.model.ProjectData;
import ghidra.program.model.listing.Program;

/**
 * Context class to hold the state of the MCP session.
 * This includes the currently active project and program.
 */
public class McpContext {

	private final GhidraProject project;
	private Program currentProgram;

	public McpContext(GhidraProject project) {
		this.project = project;
	}

	public GhidraProject getProject() {
		return project;
	}

	public Program getCurrentProgram() {
		return currentProgram;
	}

	public void setCurrentProgram(Program program) {
		if (this.currentProgram != null && this.currentProgram != program) {
			// Ensure previous program is closed if we are switching,
			// though usually tools should handle lifecycle explicitly.
			// But here we just track the reference.
		}
		this.currentProgram = program;
	}

	public ProjectData getProjectData() {
		return project.getProjectData();
	}
}

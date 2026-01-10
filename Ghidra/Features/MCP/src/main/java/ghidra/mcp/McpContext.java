package ghidra.mcp;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import ghidra.framework.model.DomainFile;
import ghidra.framework.model.ProjectData;
import ghidra.program.model.listing.Program;

/**
 * Context class to hold the state of the MCP session.
 * This includes the currently active project and program.
 */
public class McpContext {

	private final ProjectData projectData;
	private Program currentProgram;
	private Object currentProgramConsumer;

	public McpContext(ProjectData projectData) {
		this.projectData = projectData;
	}

	public Program getCurrentProgram() {
		return currentProgram;
	}

	public void setCurrentProgram(Program program, Object consumer) {
		if (this.currentProgram != null && this.currentProgram != program) {
			if (this.currentProgramConsumer != null) {
				this.currentProgram.release(this.currentProgramConsumer);
			}
		}
		this.currentProgram = program;
		this.currentProgramConsumer = consumer;
	}

	/**
	 * Sets the current program without tracking a consumer (use with caution).
	 * If there was a tracked consumer for the previous program, it will be released.
	 * @param program the program to set
	 */
	public void setCurrentProgram(Program program) {
		setCurrentProgram(program, null);
	}

	public ProjectData getProjectData() {
		return projectData;
	}
}

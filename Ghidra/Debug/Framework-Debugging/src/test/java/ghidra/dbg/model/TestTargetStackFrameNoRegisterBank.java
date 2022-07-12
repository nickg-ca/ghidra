/* ###
 * IP: GHIDRA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ghidra.dbg.model;

import java.util.List;
import java.util.Map;

import ghidra.dbg.util.PathUtils;
import ghidra.program.model.address.Address;

public class TestTargetStackFrameNoRegisterBank extends
		DefaultTestTargetObject<TestTargetObject, TestTargetStack> implements TestTargetStackFrame {
	protected Address pc;

	public TestTargetStackFrameNoRegisterBank(TestTargetStack parent, int level, Address pc) {
		super(parent, PathUtils.makeKey(PathUtils.makeIndex(level)), "Frame");

		changeAttributes(List.of(), Map.of(
			PC_ATTRIBUTE_NAME, this.pc = pc //
		), "Initialized");
	}

	@Override
	public void setFromFrame(TestTargetStackFrame frame) {
		TestTargetStackFrameNoRegisterBank that = (TestTargetStackFrameNoRegisterBank) frame;
		this.pc = that.pc;
		changeAttributes(List.of(), Map.of(
			PC_ATTRIBUTE_NAME, this.pc //
		), "Copied frame");
	}

	public void setPC(Address pc) {
		this.pc = pc;
		changeAttributes(List.of(), Map.of(
			PC_ATTRIBUTE_NAME, this.pc //
		), "PC Updated");
	}
}

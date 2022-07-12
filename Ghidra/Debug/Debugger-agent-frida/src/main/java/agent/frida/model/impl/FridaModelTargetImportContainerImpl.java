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
package agent.frida.model.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import agent.frida.manager.FridaImport;
import agent.frida.model.iface2.FridaModelTargetImportContainer;
import ghidra.dbg.target.TargetObject;
import ghidra.dbg.target.schema.TargetAttributeType;
import ghidra.dbg.target.schema.TargetElementType;
import ghidra.dbg.target.schema.TargetObjectSchema.ResyncMode;
import ghidra.dbg.target.schema.TargetObjectSchemaInfo;

@TargetObjectSchemaInfo(
	name = "ImportContainer",
	elements = {
		@TargetElementType(type = FridaModelTargetImportImpl.class) },
	elementResync = ResyncMode.ONCE,
	attributes = {
		@TargetAttributeType(type = Void.class) },
	canonicalContainer = true)
public class FridaModelTargetImportContainerImpl extends FridaModelTargetObjectImpl
		implements FridaModelTargetImportContainer {

	protected final FridaModelTargetModuleImpl module;

	public FridaModelTargetImportContainerImpl(FridaModelTargetModuleImpl module) {
		super(module.getModel(), module, "Imports", "ImportContainer");
		this.module = module;
	}

	@Override
	public CompletableFuture<Void> requestElements(boolean refresh) {
		return getManager().listModuleImports(module.getModule()).thenAccept(byName -> {
			List<TargetObject> symbols;
			synchronized (this) {
				symbols = byName.values()
						.stream()
						.map(this::getTargetImport)
						.collect(Collectors.toList());
			}
			setElements(symbols, Map.of(), "Refreshed");
		});
	}

	@Override
	public synchronized FridaModelTargetImportImpl getTargetImport(FridaImport symbol) {
		TargetObject targetObject = getMapObject(symbol);
		if (targetObject != null) {
			FridaModelTargetImportImpl targetImport = (FridaModelTargetImportImpl) targetObject;
			targetImport.setModelObject(symbol);
			return targetImport;
		}
		return new FridaModelTargetImportImpl(this, symbol);
	}
}

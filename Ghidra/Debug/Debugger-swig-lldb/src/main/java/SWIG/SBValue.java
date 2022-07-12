/* ###
 * IP: Apache License 2.0 with LLVM Exceptions
 */
/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.1
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package SWIG;

public class SBValue {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected SBValue(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(SBValue obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  @SuppressWarnings("deprecation")
  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        lldbJNI.delete_SBValue(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public SBValue() {
    this(lldbJNI.new_SBValue__SWIG_0(), true);
  }

  public SBValue(SBValue rhs) {
    this(lldbJNI.new_SBValue__SWIG_1(SBValue.getCPtr(rhs), rhs), true);
  }

  public boolean IsValid() {
    return lldbJNI.SBValue_IsValid(swigCPtr, this);
  }

  public void Clear() {
    lldbJNI.SBValue_Clear(swigCPtr, this);
  }

  public SBError GetError() {
    return new SBError(lldbJNI.SBValue_GetError(swigCPtr, this), true);
  }

  public java.math.BigInteger GetID() {
    return lldbJNI.SBValue_GetID(swigCPtr, this);
  }

  public String GetName() {
    return lldbJNI.SBValue_GetName(swigCPtr, this);
  }

  public String GetTypeName() {
    return lldbJNI.SBValue_GetTypeName(swigCPtr, this);
  }

  public String GetDisplayTypeName() {
    return lldbJNI.SBValue_GetDisplayTypeName(swigCPtr, this);
  }

  public long GetByteSize() {
    return lldbJNI.SBValue_GetByteSize(swigCPtr, this);
  }

  public boolean IsInScope() {
    return lldbJNI.SBValue_IsInScope(swigCPtr, this);
  }

  public Format GetFormat() {
    return Format.swigToEnum(lldbJNI.SBValue_GetFormat(swigCPtr, this));
  }

  public void SetFormat(Format format) {
    lldbJNI.SBValue_SetFormat(swigCPtr, this, format.swigValue());
  }

  public String GetValue() {
    return lldbJNI.SBValue_GetValue(swigCPtr, this);
  }

  public long GetValueAsSigned(SBError error, long fail_value) {
    return lldbJNI.SBValue_GetValueAsSigned__SWIG_0(swigCPtr, this, SBError.getCPtr(error), error, fail_value);
  }

  public long GetValueAsSigned(SBError error) {
    return lldbJNI.SBValue_GetValueAsSigned__SWIG_1(swigCPtr, this, SBError.getCPtr(error), error);
  }

  public java.math.BigInteger GetValueAsUnsigned(SBError error, java.math.BigInteger fail_value) {
    return lldbJNI.SBValue_GetValueAsUnsigned__SWIG_0(swigCPtr, this, SBError.getCPtr(error), error, fail_value);
  }

  public java.math.BigInteger GetValueAsUnsigned(SBError error) {
    return lldbJNI.SBValue_GetValueAsUnsigned__SWIG_1(swigCPtr, this, SBError.getCPtr(error), error);
  }

  public long GetValueAsSigned(long fail_value) {
    return lldbJNI.SBValue_GetValueAsSigned__SWIG_2(swigCPtr, this, fail_value);
  }

  public long GetValueAsSigned() {
    return lldbJNI.SBValue_GetValueAsSigned__SWIG_3(swigCPtr, this);
  }

  public java.math.BigInteger GetValueAsUnsigned(java.math.BigInteger fail_value) {
    return lldbJNI.SBValue_GetValueAsUnsigned__SWIG_2(swigCPtr, this, fail_value);
  }

  public java.math.BigInteger GetValueAsUnsigned() {
    return lldbJNI.SBValue_GetValueAsUnsigned__SWIG_3(swigCPtr, this);
  }

  public ValueType GetValueType() {
    return ValueType.swigToEnum(lldbJNI.SBValue_GetValueType(swigCPtr, this));
  }

  public boolean GetValueDidChange() {
    return lldbJNI.SBValue_GetValueDidChange(swigCPtr, this);
  }

  public String GetSummary() {
    return lldbJNI.SBValue_GetSummary__SWIG_0(swigCPtr, this);
  }

  public String GetSummary(SBStream stream, SBTypeSummaryOptions options) {
    return lldbJNI.SBValue_GetSummary__SWIG_1(swigCPtr, this, SBStream.getCPtr(stream), stream, SBTypeSummaryOptions.getCPtr(options), options);
  }

  public String GetObjectDescription() {
    return lldbJNI.SBValue_GetObjectDescription(swigCPtr, this);
  }

  public SBValue GetDynamicValue(DynamicValueType use_dynamic) {
    return new SBValue(lldbJNI.SBValue_GetDynamicValue(swigCPtr, this, use_dynamic.swigValue()), true);
  }

  public SBValue GetStaticValue() {
    return new SBValue(lldbJNI.SBValue_GetStaticValue(swigCPtr, this), true);
  }

  public SBValue GetNonSyntheticValue() {
    return new SBValue(lldbJNI.SBValue_GetNonSyntheticValue(swigCPtr, this), true);
  }

  public DynamicValueType GetPreferDynamicValue() {
    return DynamicValueType.swigToEnum(lldbJNI.SBValue_GetPreferDynamicValue(swigCPtr, this));
  }

  public void SetPreferDynamicValue(DynamicValueType use_dynamic) {
    lldbJNI.SBValue_SetPreferDynamicValue(swigCPtr, this, use_dynamic.swigValue());
  }

  public boolean GetPreferSyntheticValue() {
    return lldbJNI.SBValue_GetPreferSyntheticValue(swigCPtr, this);
  }

  public void SetPreferSyntheticValue(boolean use_synthetic) {
    lldbJNI.SBValue_SetPreferSyntheticValue(swigCPtr, this, use_synthetic);
  }

  public boolean IsDynamic() {
    return lldbJNI.SBValue_IsDynamic(swigCPtr, this);
  }

  public boolean IsSynthetic() {
    return lldbJNI.SBValue_IsSynthetic(swigCPtr, this);
  }

  public boolean IsSyntheticChildrenGenerated() {
    return lldbJNI.SBValue_IsSyntheticChildrenGenerated(swigCPtr, this);
  }

  public void SetSyntheticChildrenGenerated(boolean arg0) {
    lldbJNI.SBValue_SetSyntheticChildrenGenerated(swigCPtr, this, arg0);
  }

  public String GetLocation() {
    return lldbJNI.SBValue_GetLocation(swigCPtr, this);
  }

  public boolean SetValueFromCString(String value_str) {
    return lldbJNI.SBValue_SetValueFromCString__SWIG_0(swigCPtr, this, value_str);
  }

  public boolean SetValueFromCString(String value_str, SBError error) {
    return lldbJNI.SBValue_SetValueFromCString__SWIG_1(swigCPtr, this, value_str, SBError.getCPtr(error), error);
  }

  public SBTypeFormat GetTypeFormat() {
    return new SBTypeFormat(lldbJNI.SBValue_GetTypeFormat(swigCPtr, this), true);
  }

  public SBTypeSummary GetTypeSummary() {
    return new SBTypeSummary(lldbJNI.SBValue_GetTypeSummary(swigCPtr, this), true);
  }

  public SBTypeFilter GetTypeFilter() {
    return new SBTypeFilter(lldbJNI.SBValue_GetTypeFilter(swigCPtr, this), true);
  }

  public SBTypeSynthetic GetTypeSynthetic() {
    return new SBTypeSynthetic(lldbJNI.SBValue_GetTypeSynthetic(swigCPtr, this), true);
  }

  public SBValue GetChildAtIndex(long idx) {
    return new SBValue(lldbJNI.SBValue_GetChildAtIndex__SWIG_0(swigCPtr, this, idx), true);
  }

  public SBValue GetChildAtIndex(long idx, DynamicValueType use_dynamic, boolean can_create_synthetic) {
    return new SBValue(lldbJNI.SBValue_GetChildAtIndex__SWIG_1(swigCPtr, this, idx, use_dynamic.swigValue(), can_create_synthetic), true);
  }

  public SBValue CreateChildAtOffset(String name, long offset, SBType type) {
    return new SBValue(lldbJNI.SBValue_CreateChildAtOffset(swigCPtr, this, name, offset, SBType.getCPtr(type), type), true);
  }

  public SBValue Cast(SBType type) {
    return new SBValue(lldbJNI.SBValue_Cast(swigCPtr, this, SBType.getCPtr(type), type), true);
  }

  public SBValue CreateValueFromExpression(String name, String expression) {
    return new SBValue(lldbJNI.SBValue_CreateValueFromExpression__SWIG_0(swigCPtr, this, name, expression), true);
  }

  public SBValue CreateValueFromExpression(String name, String expression, SBExpressionOptions options) {
    return new SBValue(lldbJNI.SBValue_CreateValueFromExpression__SWIG_1(swigCPtr, this, name, expression, SBExpressionOptions.getCPtr(options), options), true);
  }

  public SBValue CreateValueFromAddress(String name, java.math.BigInteger address, SBType type) {
    return new SBValue(lldbJNI.SBValue_CreateValueFromAddress(swigCPtr, this, name, address, SBType.getCPtr(type), type), true);
  }

  public SBValue CreateValueFromData(String name, SBData data, SBType type) {
    return new SBValue(lldbJNI.SBValue_CreateValueFromData(swigCPtr, this, name, SBData.getCPtr(data), data, SBType.getCPtr(type), type), true);
  }

  public SBType GetType() {
    return new SBType(lldbJNI.SBValue_GetType(swigCPtr, this), true);
  }

  public long GetIndexOfChildWithName(String name) {
    return lldbJNI.SBValue_GetIndexOfChildWithName(swigCPtr, this, name);
  }

  public SBValue GetChildMemberWithName(String name) {
    return new SBValue(lldbJNI.SBValue_GetChildMemberWithName__SWIG_0(swigCPtr, this, name), true);
  }

  public SBValue GetChildMemberWithName(String name, DynamicValueType use_dynamic) {
    return new SBValue(lldbJNI.SBValue_GetChildMemberWithName__SWIG_1(swigCPtr, this, name, use_dynamic.swigValue()), true);
  }

  public SBValue GetValueForExpressionPath(String expr_path) {
    return new SBValue(lldbJNI.SBValue_GetValueForExpressionPath(swigCPtr, this, expr_path), true);
  }

  public SBDeclaration GetDeclaration() {
    return new SBDeclaration(lldbJNI.SBValue_GetDeclaration(swigCPtr, this), true);
  }

  public boolean MightHaveChildren() {
    return lldbJNI.SBValue_MightHaveChildren(swigCPtr, this);
  }

  public boolean IsRuntimeSupportValue() {
    return lldbJNI.SBValue_IsRuntimeSupportValue(swigCPtr, this);
  }

  public long GetNumChildren() {
    return lldbJNI.SBValue_GetNumChildren__SWIG_0(swigCPtr, this);
  }

  public long GetNumChildren(long max) {
    return lldbJNI.SBValue_GetNumChildren__SWIG_1(swigCPtr, this, max);
  }

  public SWIGTYPE_p_void GetOpaqueType() {
    long cPtr = lldbJNI.SBValue_GetOpaqueType(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }

  public SBValue Dereference() {
    return new SBValue(lldbJNI.SBValue_Dereference(swigCPtr, this), true);
  }

  public SBValue AddressOf() {
    return new SBValue(lldbJNI.SBValue_AddressOf(swigCPtr, this), true);
  }

  public boolean TypeIsPointerType() {
    return lldbJNI.SBValue_TypeIsPointerType(swigCPtr, this);
  }

  public SBTarget GetTarget() {
    return new SBTarget(lldbJNI.SBValue_GetTarget(swigCPtr, this), true);
  }

  public SBProcess GetProcess() {
    return new SBProcess(lldbJNI.SBValue_GetProcess(swigCPtr, this), true);
  }

  public SBThread GetThread() {
    return new SBThread(lldbJNI.SBValue_GetThread(swigCPtr, this), true);
  }

  public SBFrame GetFrame() {
    return new SBFrame(lldbJNI.SBValue_GetFrame(swigCPtr, this), true);
  }

  public SBWatchpoint Watch(boolean resolve_location, boolean read, boolean write, SBError error) {
    return new SBWatchpoint(lldbJNI.SBValue_Watch(swigCPtr, this, resolve_location, read, write, SBError.getCPtr(error), error), true);
  }

  public SBWatchpoint WatchPointee(boolean resolve_location, boolean read, boolean write, SBError error) {
    return new SBWatchpoint(lldbJNI.SBValue_WatchPointee(swigCPtr, this, resolve_location, read, write, SBError.getCPtr(error), error), true);
  }

  public boolean GetDescription(SBStream description) {
    return lldbJNI.SBValue_GetDescription(swigCPtr, this, SBStream.getCPtr(description), description);
  }

  public boolean GetExpressionPath(SBStream description) {
    return lldbJNI.SBValue_GetExpressionPath__SWIG_0(swigCPtr, this, SBStream.getCPtr(description), description);
  }

  public SBData GetPointeeData(long item_idx, long item_count) {
    return new SBData(lldbJNI.SBValue_GetPointeeData__SWIG_0(swigCPtr, this, item_idx, item_count), true);
  }

  public SBData GetPointeeData(long item_idx) {
    return new SBData(lldbJNI.SBValue_GetPointeeData__SWIG_1(swigCPtr, this, item_idx), true);
  }

  public SBData GetPointeeData() {
    return new SBData(lldbJNI.SBValue_GetPointeeData__SWIG_2(swigCPtr, this), true);
  }

  public SBData GetData() {
    return new SBData(lldbJNI.SBValue_GetData(swigCPtr, this), true);
  }

  public boolean SetData(SBData data, SBError error) {
    return lldbJNI.SBValue_SetData(swigCPtr, this, SBData.getCPtr(data), data, SBError.getCPtr(error), error);
  }

  public SBValue Clone(String new_name) {
    return new SBValue(lldbJNI.SBValue_Clone(swigCPtr, this, new_name), true);
  }

  public java.math.BigInteger GetLoadAddress() {
    return lldbJNI.SBValue_GetLoadAddress(swigCPtr, this);
  }

  public SBAddress GetAddress() {
    return new SBAddress(lldbJNI.SBValue_GetAddress(swigCPtr, this), true);
  }

  public SBValue Persist() {
    return new SBValue(lldbJNI.SBValue_Persist(swigCPtr, this), true);
  }

  public boolean GetExpressionPath(SBStream description, boolean qualify_cxx_base_classes) {
    return lldbJNI.SBValue_GetExpressionPath__SWIG_1(swigCPtr, this, SBStream.getCPtr(description), description, qualify_cxx_base_classes);
  }

  public SBValue EvaluateExpression(String expr) {
    return new SBValue(lldbJNI.SBValue_EvaluateExpression__SWIG_0(swigCPtr, this, expr), true);
  }

  public SBValue EvaluateExpression(String expr, SBExpressionOptions options) {
    return new SBValue(lldbJNI.SBValue_EvaluateExpression__SWIG_1(swigCPtr, this, expr, SBExpressionOptions.getCPtr(options), options), true);
  }

  public SBValue EvaluateExpression(String expr, SBExpressionOptions options, String name) {
    return new SBValue(lldbJNI.SBValue_EvaluateExpression__SWIG_2(swigCPtr, this, expr, SBExpressionOptions.getCPtr(options), options, name), true);
  }

  public String __str__() {
    return lldbJNI.SBValue___str__(swigCPtr, this);
  }

}

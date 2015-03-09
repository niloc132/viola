package com.colinalworth.gwt.viola.compiler;

import java.io.File;
import java.util.List;

import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.dev.CompilerOptions;
import com.google.gwt.dev.cfg.Properties;
import com.google.gwt.dev.jjs.JsOutputOption;
import com.google.gwt.dev.js.JsNamespaceOption;
import com.google.gwt.dev.util.arg.OptionJsInteropMode;
import com.google.gwt.dev.util.arg.OptionMethodNameDisplayMode;
import com.google.gwt.dev.util.arg.SourceLevel;
import com.google.gwt.thirdparty.guava.common.collect.ImmutableListMultimap;
import com.google.gwt.thirdparty.guava.common.collect.ListMultimap;

public class CouchCompilerOptions implements CompilerOptions {

	private File warDir;
	private File workDir;
	private File deployDir;

	public CouchCompilerOptions(File warDir, File workDir, File deployDir) {
		this.warDir = warDir;
		this.workDir = workDir;
		this.deployDir = deployDir;
		
	}

	public int getOptimizationLevel() {
		return 10;
	}

	public void setOptimizationLevel(int level) {

	}

	public boolean isAggressivelyOptimize() {
		return true;
	}

	public void setAggressivelyOptimize(boolean aggressivelyOptimize) {

	}

	public boolean isClassMetadataDisabled() {
		return false;
	}

	public void setClassMetadataDisabled(boolean disabled) {

	}

	public boolean isCastCheckingDisabled() {
		return false;
	}

	public void setCastCheckingDisabled(boolean disabled) {

	}

	public boolean isEnableAssertions() {
		return false;
	}

	public void setEnableAssertions(boolean enableAssertions) {

	}

	public boolean isRunAsyncEnabled() {
		return true;
	}

	public void setRunAsyncEnabled(boolean enabled) {

	}

	public JsOutputOption getOutput() {
		return JsOutputOption.OBFUSCATED;
	}

	public void setOutput(JsOutputOption obfuscated) {

	}

	public boolean isSoycEnabled() {
		return false;
	}

	public void setSoycEnabled(boolean enabled) {

	}

	public boolean isCompilerMetricsEnabled() {
		return false;
	}

	public void setCompilerMetricsEnabled(boolean enabled) {

	}

	public boolean isSoycExtra() {
		return false;
	}

	public void setSoycExtra(boolean soycExtra) {

	}

	public boolean isOptimizePrecompile() {
		return true;
	}

	public void setOptimizePrecompile(boolean optimize) {

	}

	public boolean isStrict() {
		return true;
	}

	public void setStrict(boolean strict) {

	}

	public boolean isSoycHtmlDisabled() {
		return false;
	}

	public void setSoycHtmlDisabled(boolean disabled) {

	}

	public boolean isClosureCompilerEnabled() {
		return false;
	}

	public void setClosureCompilerEnabled(boolean enabled) {

	}

	public int getFragmentsMerge() {
		return 0;
	}

	public void setFragmentsMerge(int numFragments) {

	}

	public int getFragmentCount() {
		return 0;
	}

	public void setFragmentCount(int numFragments) {

	}

	public boolean isUseGuiLogger() {
		return false;
	}

	public void setUseGuiLogger(boolean useGuiLogger) {

	}

	public List<String> getModuleNames() {
		return null;//TODO
	}

	public void addModuleName(String moduleName) {
		//TODO
	}

	public void setModuleNames(List<String> moduleNames) {
		//TODO
	}

	public Type getLogLevel() {
		return Type.INFO;
	}

	public void setLogLevel(Type logLevel) {

	}

	public File getWorkDir() {
		return workDir;
	}

	public void setWorkDir(File dir) {

	}

	public File getGenDir() {
		return null;
	}

	public void setGenDir(File dir) {

	}

	public boolean isValidateOnly() {
		return false;
	}

	public void setValidateOnly(boolean validateOnly) {

	}

	public boolean isUpdateCheckDisabled() {
		return true;
	}

	public void setDisableUpdateCheck(boolean disabled) {

	}

	public boolean isEnabledGeneratingOnShards() {
		return false;
	}

	public void setEnabledGeneratingOnShards(boolean allowed) {

	}

	public int getMaxPermsPerPrecompile() {
		return -1;
	}

	public void setMaxPermsPerPrecompile(int maxPerms) {

	}

	public File getExtraDir() {
		return null;
	}

	public void setExtraDir(File extraDir) {

	}

	public File getWarDir() {
		return warDir;
	}

	public void setWarDir(File dir) {

	}

	public File getDeployDir() {
		return deployDir;
	}

	public void setDeployDir(File dir) {

	}

	public File getOutDir() {
		return null;
	}

	public void setOutDir(File outDir) {

	}

	public int getLocalWorkers() {
		return 0;
	}

	public void setLocalWorkers(int localWorkers) {

	}

	@Override
	public boolean shouldClusterSimilarFunctions() {
		return false;
	}

	@Override
	public void setClusterSimilarFunctions(boolean enabled) {
		
	}

	@Override
	public boolean shouldInlineLiteralParameters() {
		return false;
	}

	@Override
	public void setInlineLiteralParameters(boolean enabled) {
		
	}

	@Override
	public boolean shouldOptimizeDataflow() {
		return false;
	}

	@Override
	public void setOptimizeDataflow(boolean enabled) {
		
	}

	@Override
	public boolean shouldOrdinalizeEnums() {
		return false;
	}

	@Override
	public void setOrdinalizeEnums(boolean enabled) {
		
	}

	@Override
	public boolean shouldRemoveDuplicateFunctions() {
		return false;
	}

	@Override
	public void setRemoveDuplicateFunctions(boolean enabled) {

	}

	@Override
	public SourceLevel getSourceLevel() {
		return SourceLevel.JAVA7;
	}

	@Override
	public void setSourceLevel(SourceLevel level) {
		
	}

	@Override
	public boolean shouldSaveSource() {
		return false;
	}

	@Override
	public void setSaveSource(boolean enabled) {
		
	}

	@Override
	public File getSaveSourceOutput() {
		return null;
	}

	@Override
	public void setSaveSourceOutput(File dest) {
	}

	@Override
	public boolean shouldJDTInlineCompileTimeConstants() {
		return false;
	}

	@Override
	public boolean shouldAddRuntimeChecks() {
		return true;
	}

	@Override
	public void setAddRuntimeChecks(boolean enabled) {

	}

	@Override
	public boolean isClosureCompilerFormatEnabled() {
		return false;
	}

	@Override
	public void setClosureCompilerFormatEnabled(boolean enabled) {

	}

	@Override
	public Properties getFinalProperties() {
		return null;
	}

	@Override
	public void setFinalProperties(Properties properties) {

	}

	@Override
	public boolean isIncrementalCompileEnabled() {
		return false;
	}

	@Override
	public void setIncrementalCompileEnabled(boolean enabled) {

	}

	@Override
	public OptionJsInteropMode.Mode getJsInteropMode() {
		return OptionJsInteropMode.Mode.JS;
	}

	@Override
	public void setJsInteropMode(OptionJsInteropMode.Mode mode) {

	}

	@Override
	public boolean isJsonSoycEnabled() {
		return false;
	}

	@Override
	public void setJsonSoycEnabled(boolean value) {

	}

	@Override
	public OptionMethodNameDisplayMode.Mode getMethodNameDisplayMode() {
		return OptionMethodNameDisplayMode.Mode.NONE;
	}

	@Override
	public void setMethodNameDisplayMode(OptionMethodNameDisplayMode.Mode methodNameDisplayMode) {

	}

	@Override
	public JsNamespaceOption getNamespace() {
		return JsNamespaceOption.NONE;
	}

	@Override
	public void setNamespace(JsNamespaceOption newValue) {

	}

	@Override
	public void setPropertyValues(String name, Iterable<String> values) {

	}

	@Override
	public ListMultimap<String, String> getProperties() {
		return ImmutableListMultimap.of();
	}

	@Override
	public String getSourceMapFilePrefix() {
		return null;
	}

	@Override
	public void setSourceMapFilePrefix(String path) {

	}

	@Override
	public boolean enforceStrictPublicResources() {
		return false;
	}

	@Override
	public void setEnforceStrictPublicResources(boolean strictPublicResources) {

	}

	@Override
	public boolean enforceStrictSourceResources() {
		return false;
	}

	@Override
	public void setEnforceStrictSourceResources(boolean strictSourceResources) {

	}

	@Override
	public boolean useDetailedTypeIds() {
		return false;
	}

	@Override
	public void setUseDetailedTypeIds(boolean enabled) {

	}
}

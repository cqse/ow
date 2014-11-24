package net.vtst.ow.eclipse.js.closure.compiler;

import com.google.javascript.jscomp.ClosureCodingConvention;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JqueryCodingConvention;
import com.google.javascript.jscomp.WarningLevel;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;

import net.vtst.eclipse.easy.ui.properties.stores.IReadOnlyStore;
import net.vtst.eclipse.easy.ui.properties.stores.ResourcePropertyStore;
import net.vtst.ow.eclipse.js.closure.OwJsClosurePlugin;
import net.vtst.ow.eclipse.js.closure.launching.compiler.ClosureCompilerLaunchConfigurationRecord;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * This class provides static methods for creating {@code CompilerOptions}.
 * @author Vincent Simonet
 */
public class ClosureCompilerOptions {

  // This is based on CommandLineRunner.createOptions() and AbstractCommandLineRunner.setRunOptions()
  private static CompilerOptions makeInternal(IReadOnlyStore storeForChecks, IReadOnlyStore storeForCompilationOptions, boolean ideMode) throws CoreException {
    ClosureCompilerLaunchConfigurationRecord record = ClosureCompilerLaunchConfigurationRecord.getInstance();

    // From CommandLineRunner.createOptions()
    CompilerOptions options = new CompilerOptions();
    options.setCodingConvention(new ClosureCodingConvention());
    CompilationLevel level = CompilationLevel.WHITESPACE_ONLY;
    if (storeForCompilationOptions != null) {
      level = record.compilationLevel.get(storeForCompilationOptions);
       level.setOptionsForCompilationLevel(options);
       if (record.generateExports.get(storeForCompilationOptions)) {
         options.setGenerateExports(true);
       }
    }

    WarningLevel wLevel = record.checks.warningLevel.get(storeForChecks);
    wLevel.setOptionsForWarningLevel(options);
    if (storeForCompilationOptions != null) {
      if (record.formattingPrettyPrint.get(storeForCompilationOptions)) options.prettyPrint = true;
      if (record.formattingPrintInputDelimiter.get(storeForCompilationOptions)) options.printInputDelimiter = true;
    }

    options.closurePass = record.checks.processClosurePrimitives.get(storeForChecks);

    options.jqueryPass = record.checks.processJQueryPrimitives.get(storeForChecks) &&
        CompilationLevel.ADVANCED_OPTIMIZATIONS == level;

    if (record.checks.processJQueryPrimitives.get(storeForChecks)) {
      options.setCodingConvention(new JqueryCodingConvention());
    }

    /*
    if (!flags.translationsFile.isEmpty()) {
      try {
        options.messageBundle = new XtbMessageBundle(
            new FileInputStream(flags.translationsFile),
            flags.translationsProject);
      } catch (IOException e) {
        throw new RuntimeException("Reading XTB file", e);
      }
    } else if (CompilationLevel.ADVANCED_OPTIMIZATIONS == level) {
      // In SIMPLE or WHITESPACE mode, if the user hasn't specified a
      // translations file, they might reasonably try to write their own
      // implementation of goog.getMsg that makes the substitution at
      // run-time.
      //
      // In ADVANCED mode, goog.getMsg is going to be renamed anyway,
      // so we might as well inline it.
      options.messageBundle = new EmptyMessageBundle();

      // From AbstractCommandLineRunner.setRunOptions()
      if (config.warningGuards != null) {
        for (WarningGuardSpec.Entry entry : config.warningGuards.entries) {
          diagnosticGroups.setWarningLevel(options, entry.groupName, entry.level);
        }
      }
      */
    //createDefineOrTweakReplacements(config.define, options, false);

    //options.setTweakProcessing(config.tweakProcessing);
    //createDefineOrTweakReplacements(config.tweak, options, true);

    // Dependency options
    // options.setManageClosureDependencies(false);
    // if (config.closureEntryPoints.size() > 0) {
    //   options.setManageClosureDependencies(config.closureEntryPoints);
    // }

    options.ideMode = ideMode;
    // options.setCodingConvention(config.codingConvention);
    // options.setSummaryDetailLevel(config.summaryDetailLevel);

    // legacyOutputCharset = options.outputCharset = getLegacyOutputCharset();
    // outputCharset2 = getOutputCharset2();
    // inputCharset = getInputCharset();

    // if (config.createSourceMap.length() > 0) {
    //   options.sourceMapOutputPath = config.createSourceMap;
    // }
    // options.sourceMapDetailLevel = config.sourceMapDetailLevel;
    // options.sourceMapFormat = config.sourceMapFormat;

    // if (!config.variableMapInputFile.equals("")) {
    //   options.inputVariableMapSerialized =
    //       VariableMap.load(config.variableMapInputFile).toBytes();
    // }

    // if (!config.propertyMapInputFile.equals("")) {
    //   options.inputPropertyMapSerialized =
    //       VariableMap.load(config.propertyMapInputFile).toBytes();
    // }
    options.setLanguageIn(record.checks.languageIn.get(storeForChecks));

    // if (!config.outputManifests.isEmpty()) {
    //   Set<String> uniqueNames = Sets.newHashSet();
    //   for (String filename : config.outputManifests) {
    //     if (!uniqueNames.add(filename)) {
    //       throw new FlagUsageException("output_manifest flags specify " +
    //           "duplicate file names: " + filename);
    //     }
    //   }
    // }

    // if (!config.outputBundles.isEmpty()) {
    //   Set<String> uniqueNames = Sets.newHashSet();
    //   for (String filename : config.outputBundles) {
    //     if (!uniqueNames.add(filename)) {
    //       throw new FlagUsageException("output_bundle flags specify " +
    //           "duplicate file names: " + filename);
    //     }
    //   }
    // }

    options.setAcceptConstKeyword(record.checks.acceptConstKeyword.get(storeForChecks));
    // options.transformAMDToCJSModules = config.transformAMDToCJSModules;
    // options.processCommonJSModules = config.processCommonJSModules;
    // options.commonJSModulePathPrefix = config.commonJSModulePathPrefix;

    // TODO: Only for ide mode?
    options.setRemoveAbstractMethods(false);
    options.checkTypes = true;
    options.setInferTypes(true);
    options.closurePass = true;
    options.setLanguageIn(LanguageMode.ECMASCRIPT5_STRICT);
    options.setLanguageOut(LanguageMode.NO_TRANSPILE);

    return options;
  }

  public static CompilerOptions makeForBuilder(IProject project) throws CoreException {
    IReadOnlyStore store = new ResourcePropertyStore(project, OwJsClosurePlugin.PLUGIN_ID);
    return makeInternal(store, null, true);
  }

  public static CompilerOptions makeForLaunch(IReadOnlyStore storeForChecks, IReadOnlyStore storeForCompilationOptions) throws CoreException {
    return makeInternal(storeForChecks, storeForCompilationOptions, false);
  }

}

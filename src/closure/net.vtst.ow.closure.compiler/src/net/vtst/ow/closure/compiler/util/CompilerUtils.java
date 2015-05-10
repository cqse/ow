package net.vtst.ow.closure.compiler.util;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.javascript.jscomp.AbstractCompiler;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.CompilerPass;
import com.google.javascript.jscomp.CustomPassExecutionTime;
import com.google.javascript.jscomp.ErrorManager;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.PrintStreamErrorManager;

/**
 * Helper functions to manage JavaScript compilers.
 * @author Vincent Simonet
 */
public class CompilerUtils {

  /**
   * Test whether a file is a JavaScript file.  The current implementation just looks
   * at the file extension, which must be {@code .js}.
   * @param file  The file to test.
   * @return  true iif the file is a JavaScript file.
   */
  public static boolean isJavaScriptFile(File file) {
    return file.getPath().endsWith(".js");
  }

  /**
   * Create a new error manager that prints error on a stream.
   * @param printStream  The output stream where errors are printed.
   * @return  The new error manager.
   */
  public static ErrorManager makePrintingErrorManager(PrintStream printStream) {
    return new PrintStreamErrorManager(printStream);
  }

  /**
   * Create a new compiler object, using a given error manager.
   * @param errorManager  The error manager the compiler will be connected to.
   * @return  The new compiler.
   */
  public static Compiler makeCompiler(ErrorManager errorManager) {
    Compiler compiler = new Compiler(errorManager);
    compiler.disableThreads();
    return compiler;
  }

  /**
   * Create a new compiler options object, with the minimum options for a compiler used either
   * to parse files (in a tolerant way) or to report errors.
   * @return  The new compiler options.
   */
  public static CompilerOptions makeOptionsForParsingAndErrorReporting() {
    // These options should remain minimal, because they are used by the stripper.
    CompilerOptions options = new CompilerOptions();
    options.ideMode = true;
    options.setRemoveAbstractMethods(false);
    return options;
  }

  /**
   * Add a custom compiler pass to a compiler options.
   * @param options  The compiler options to which the custom pass will be added.
   * @param pass  The compiler pass to add.
   * @param executionTime  The execution time for the compiler pass.
   */
  public static void addCustomCompilerPass(
      CompilerOptions options, CompilerPass pass, CustomPassExecutionTime executionTime) {
    Multimap<CustomPassExecutionTime, CompilerPass> customPasses = getCustomPasses(options);
    if (customPasses == null) {
      customPasses = ArrayListMultimap.create();
      setCustomPasses(options, customPasses);
    }
    customPasses.put(executionTime, pass);
  }

  public static Multimap<CustomPassExecutionTime, CompilerPass> getCustomPasses(CompilerOptions options) {

    try {
      Field customPassesField = options.getClass().getDeclaredField("customPasses");
      customPassesField.setAccessible(true);
      return (Multimap<CustomPassExecutionTime, CompilerPass>) customPassesField.get(options);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static void setCustomPasses(CompilerOptions options, Multimap<CustomPassExecutionTime, CompilerPass>  customPasses) {

    try {
      Field customPassesField = options.getClass().getDeclaredField("customPasses");
      customPassesField.setAccessible(true);
      customPassesField.set(options, customPasses);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Report an error via an error manager.
   * @param manager  The error manager to use for reporting the error.
   * @param error  The error to report.
   */
  public static void reportError(ErrorManager manager, JSError error) {
    manager.report(error.getDefaultLevel(), error);
  }

  /**
   * Report an error via the error manager of a compiler.
   * @param compiler  The compiler whose error manager will be used for reporting the error.
   * @param error  The error to report.
   */
  public static void reportError(AbstractCompiler compiler, JSError error) {
    reportError(compiler.getErrorManager(), error);
  }
}

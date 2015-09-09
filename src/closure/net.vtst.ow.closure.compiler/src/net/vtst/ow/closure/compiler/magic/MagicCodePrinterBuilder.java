package net.vtst.ow.closure.compiler.magic;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.google.javascript.jscomp.CodePrinter;
import com.google.javascript.rhino.Node;

/**
 * Wrapper around {@code com.google.javascript.jscomp.CodePrinter}, to make it accessible
 * though it is not a public class.
 * @author Vincent Simonet
 */
public class MagicCodePrinterBuilder {

  public CodePrinter.Builder codePrinterBuilder;
  public static Constructor<?> constructor;
  public static Method method_build;

  private static void initialize() {
    if (constructor != null) return;
    Class<?> cls = CodePrinter.Builder.class;
    constructor = Magic.getDeclaredConstructor(cls, Node.class);
    method_build = Magic.getDeclaredMethod(cls, "build");
  }

  /**
   * @param node  The node to be printed.
   * @param prettyPrint  Whether to pretty print.
   * @param outputTypes  Whether to output types as JSDocStrings.
   */
  public MagicCodePrinterBuilder(Node node, boolean prettyPrint, boolean outputTypes) {
    initialize();
    try {
      codePrinterBuilder = (CodePrinter.Builder) constructor.newInstance(node);
    } catch (IllegalArgumentException e) {
      throw new MagicException(e);
    } catch (InstantiationException e) {
      throw new MagicException(e);
    } catch (IllegalAccessException e) {
      throw new MagicException(e);
    } catch (InvocationTargetException e) {
      Magic.catchInvocationTargetException(e);
    }

    codePrinterBuilder.setPrettyPrint(prettyPrint);
    codePrinterBuilder.setOutputTypes(outputTypes);
  }

  /**
   * Prints the node as a string.
   * @return  The printed representation.
   */
  public String build() {
    return codePrinterBuilder.build();
  }

}

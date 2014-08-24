package net.vtst.ow.closure.compiler.deps;

import java.io.File;

import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.SourceFile;

public class JSExtern extends AstFactory {
  private static final long serialVersionUID = 1L;

  public JSExtern(SourceFile sourceFile) {
    super(sourceFile);
  }

  public JSExtern(File file) {
    this(SourceFile.fromFile(file));
  }

}

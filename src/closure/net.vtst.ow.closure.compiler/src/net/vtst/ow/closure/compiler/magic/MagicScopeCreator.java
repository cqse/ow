package net.vtst.ow.closure.compiler.magic;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.Scope;
import com.google.javascript.rhino.Node;

/**
 * Wrapper around {@code com.google.javascript.jscomp.PassConfig} to access its typed scope creator,
 * though it is not public.
 * @author Vincent Simonet
 */
public class MagicScopeCreator {

  private Object memoizedScopeCreator = null;
  private Field memoizedScopeCreator_scopes = null;

  /**
   * WARNING! The object has to be created after the compilation!
   * @param compiler  The compiler where to take the passes configuration.
   */
  public MagicScopeCreator(Compiler compiler) {
    setMemoizedScopeCreator(compiler.getTypedScopeCreator());
  }

  private void setMemoizedScopeCreator(Object object) {
    if (object == null) return;
    memoizedScopeCreator = object;
    try {
      memoizedScopeCreator_scopes = memoizedScopeCreator.getClass().getDeclaredField("scopes");
      memoizedScopeCreator_scopes.setAccessible(true);
    } catch (SecurityException e) {
      throw new MagicException(e);
    } catch (NoSuchFieldException e) {
      throw new MagicException(e);
    }
  }


  /**
   * Get the scope for a node in the scope creator.
   * @param node  The node to look for.
   * @return  The found scope, or null.
   */
  public Scope getScope(Node node) {
    if (memoizedScopeCreator_scopes == null) return null;
    try {
      Map scopes = (Map) memoizedScopeCreator_scopes.get(memoizedScopeCreator);
      Object scope = scopes.get(node);
      if (scope instanceof Scope) return (Scope) scope;
    } catch (IllegalArgumentException e) {
      throw new MagicException(e);
    } catch (IllegalAccessException e) {
      throw new MagicException(e);
    }
    return null;
  }

}

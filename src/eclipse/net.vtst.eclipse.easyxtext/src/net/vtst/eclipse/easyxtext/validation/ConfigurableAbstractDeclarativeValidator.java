package net.vtst.eclipse.easyxtext.validation;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.xtext.validation.AbstractDeclarativeValidator;

/**
 * A wrapper around AbstractDeclarativeValidator for accessing configuration
 * options.
 * 
 * @author Vincent Simonet
 */
public class ConfigurableAbstractDeclarativeValidator {
  
  /**
   * Information about a configuration group.
   */
  public static class Group {
    public String name;
    public String label = null;
    public boolean enabledByDefault;
  }
  
  private AbstractDeclarativeValidator validator;
  private boolean enabledByDefault = true;
  private boolean makeConfigurableByDefault = true;
  private Map<String, Group> groups = new HashMap<String, Group>();
  private ArrayList<Group> groupsList = new ArrayList<Group>();

  /**
   * Creates a new wrapper.
   * @param validator
   */
  public ConfigurableAbstractDeclarativeValidator(AbstractDeclarativeValidator validator) {
    this.validator = validator;
    parseValidatorAnnotation();
    addMethods(getCheckMethods());
  }

  /**
   * @return The list of configuration groups for the validator.
   */
  public ArrayList<Group> getGroups() {
    return groupsList;
  }

  private boolean stateToBoolean(CheckState state) {
    switch (state) {
    case ENABLED: return true;
    case DISABLED: return false;
    default: return enabledByDefault;
    }
  }
  
  private void parseValidatorAnnotation() {
    ConfigurableValidator annotation = validator.getClass().getAnnotation(ConfigurableValidator.class);
    if (annotation == null) return;
    makeConfigurableByDefault = annotation.makeConfigurableByDefault();
    enabledByDefault = stateToBoolean(annotation.defaultState());
  }
  
  private void addMethods(Collection<Method> methods) {
    for (Method method : methods)
      addMethod(method);
    groupsList = new ArrayList<Group>(groups.size());
    groupsList.addAll(groups.values());
  }
  
  private void addMethod(Method method) {
    ConfigurableCheck annotation = method.getAnnotation(ConfigurableCheck.class);
    if (annotation == null && !makeConfigurableByDefault) return;
    String groupName = method.getName();
    if (annotation != null && !annotation.group().isEmpty()) {
      groupName = annotation.group();
    }
    Group group = groups.get(groupName);
    if (group == null) {
      group = new Group();
      group.name = groupName;
      group.enabledByDefault = enabledByDefault;
      groups.put(groupName, group);
    }
    if (annotation != null) {
      if (annotation.defaultState() != CheckState.DEFAULT)
        group.enabledByDefault = stateToBoolean(annotation.defaultState());
      if (!annotation.group().isEmpty()) group.name = annotation.group();
      if (!annotation.label().isEmpty()) group.label = annotation.label();
    }
  }
  
  /**
   * This is an unsafe method which access private methods and fields from AbstractDeclarativeValidator.
   * @return The collection of the methods of the validator that are annotated
   * as checks.
   */
  private Collection<Method> getCheckMethods() {
    try {
      Method collectMethodsMethod = AbstractDeclarativeValidator.class.getDeclaredMethod("collectMethods", Class.class);
      collectMethodsMethod.setAccessible(true);
      List<?> methodWrappers = (List<?>) collectMethodsMethod.invoke(validator, validator.getClass());
      ArrayList<Method> result = new ArrayList<Method>(methodWrappers.size());
      Field methodField = null;
      for (Object methodWrapper : methodWrappers) {
        if (methodField == null) {
          methodField = methodWrapper.getClass().getDeclaredField("method");
          methodField.setAccessible(true);
        }
        Method method = (Method) methodField.get(methodWrapper);
        result.add(method);
      }
      return result;
    } catch (SecurityException e) {
      // This should never happen.
      e.printStackTrace();
    } catch (NoSuchFieldException e) {
      // This should never happen.
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      // This should never happen.
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      // This should never happen.
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      // This should never happen.
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      // This should never happen.
      e.printStackTrace();
    }
    return Collections.emptyList();
  }

}
package org.gvnix.dynamic.configuration.roo.addon;

import java.util.Set;


public interface ConfigurationParser {

  public Set<Class<? extends Object>> getEveryCommand();
  
}
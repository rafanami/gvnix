/*
 * Copyright 2002-2010 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.gvnix.flex.roo.addon.as.classpath.details;

import org.apache.commons.lang3.Validate;
import org.gvnix.flex.roo.addon.as.classpath.ASPhysicalTypeCategory;
import org.gvnix.flex.roo.addon.as.classpath.ASPhysicalTypeDetails;
import org.gvnix.flex.roo.addon.as.model.ActionScriptType;

/**
 * Default detail representation of an ActionScript source file.
 * 
 * @author Jeremy Grelle
 */
public class DefaultASPhysicalTypeDetails implements ASPhysicalTypeDetails {

    private final ASPhysicalTypeCategory physicalTypeCategory;

    private final ActionScriptType name;

    public DefaultASPhysicalTypeDetails(
            ASPhysicalTypeCategory physicalTypeCategory, ActionScriptType name) {
        Validate.notNull(physicalTypeCategory,
                "Physical type category required");
        Validate.notNull(name, "Name required");
        this.physicalTypeCategory = physicalTypeCategory;
        this.name = name;
    }

    public ActionScriptType getName() {
        return this.name;
    }

    public ASPhysicalTypeCategory getPhysicalTypeCategory() {
        return this.physicalTypeCategory;
    }

}

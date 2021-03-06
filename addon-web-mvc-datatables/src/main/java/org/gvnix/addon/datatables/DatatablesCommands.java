/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana     
 * Copyright (C) 2013 Generalitat Valenciana
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/copyleft/gpl.html>.
 */
package org.gvnix.addon.datatables;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * Web MVC JQuery datatables commands class
 * 
 * @author gvNIX Team
 */
@Component
@Service
public class DatatablesCommands implements CommandMarker {
    /**
     * Get a reference to the DatatablesOperations
     */
    @Reference
    private DatatablesOperations operations;

    /**
     * Informs if setup commands is available
     * 
     * @return true if setup is available
     */
    @CliAvailabilityIndicator("web mvc datatables setup")
    public boolean isSetupAvailable() {
        return operations.isSetupAvailable();
    }

    /**
     * Informs if update tags command is available
     * 
     * @return true if commands is available
     */
    @CliAvailabilityIndicator("web mvc datatables update tags")
    public boolean isUpdateTagsAvailable() {
        return operations.isUpdateTagsAvailable();
    }

    /**
     * Informs if commands are available
     * 
     * @return true if commands are available
     */
    @CliAvailabilityIndicator({ "web mvc datatables add",
            "web mvc datatables all" })
    public boolean isAddAvailable() {
        return operations.isAddAvailable();
    }

    /**
     * Use datatables component for a controller list view
     * 
     * @param type target controller
     */
    @CliCommand(value = "web mvc datatables add", help = "Use datatable component for a controller list view")
    public void add(
            @CliOption(key = "type", mandatory = true, help = "The controller to apply this component to") JavaType target,
            @CliOption(key = "ajax", mandatory = false, unspecifiedDefaultValue = "true", help = "Datatables will use AJAX request to get data data or not") boolean ajax,
            @CliOption(key = "mode", mandatory = false, unspecifiedDefaultValue = GvNIXDatatables.TABLE, help = "visualization mode: if empty (default) shows a table, otherwise create one-row-per-page, one-cell-per-row datatable which cell content is the render of requiered page. (example: \"show\" renders the show view for every item ") String mode) {
        operations.annotateController(target, ajax, mode);
    }

    /**
     * This method registers a command with the Roo shell. It has no command
     * attribute.
     */
    @CliCommand(value = "web mvc datatables all", help = "Use datatable component for all list view in this application")
    public void all(
            @CliOption(key = "ajax", mandatory = false, unspecifiedDefaultValue = "true", help = "Datatables will use AJAX request to get data data or not") boolean ajax) {
        operations.annotateAll(ajax);
    }

    /**
     * Setup datatables artifacts
     */
    @CliCommand(value = "web mvc datatables setup", help = "Setup datatables support")
    public void setup() {
        operations.setup();
    }

    /**
     * Update related datatables artifacts (tags, js, images...)
     */
    @CliCommand(value = "web mvc datatables update tags", help = "Update datatables artificats (tags, images, js)")
    public void updateTags() {
        operations.updateTags();
    }
}
/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010, 2011 CIT - Generalitat Valenciana
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package __TOP_LEVEL_PACKAGE__.web.menu;


import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

/**
 * gvNIX Context menu Strategy (URL match, return children).
 * <p>
 * This strategy decides which menu item (root, subcategory, etc) should act 
 * as root by matching current request URL with all menu entries target URLs.
 * <p>
 * If a match item is found ({@link #query(HttpServletRequest, ServletContext, Menu)})
 * the menu will render the children of the match menu entry.
 */
@Component(URLChildrenContextMenuStrategy.NAME)
@Configurable
public class URLChildrenContextMenuStrategy extends BaseURLContextMenuStrategy {

  public static final String NAME = "URLChildrenContextMenuStrategy";

  public String getName() {
    return NAME;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Locates current menu position by matching current request URL with all
   * {@link MenuItem}.
   * <p>
   * If a match item is found
   * {@link #query(HttpServletRequest, ServletContext, Menu)} method will return
   * {@link MenuItem}'s children.
   * <p>
   * If no match item found it will return <code>null</code>.
   */
  public List<MenuItem> query(HttpServletRequest request,
                              ServletContext jspContext, Menu menu) {
    MenuItem currentItem = getItemFromCurrentURL(request, jspContext, menu);
    if (currentItem == null) {
      return null;
    }
    return currentItem.getChildren();
  }
}

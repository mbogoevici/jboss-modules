/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.modules.filter;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
final class EqualsClassNameFilter implements ClassNameFilter {

    private final String className;

    EqualsClassNameFilter(final String className) {
        if (className == null) {
            throw new IllegalArgumentException("className is null");
        }
        this.className = className;
    }

    public boolean accept(final String className) {
        return className.equals(this.className);
    }

    public boolean equals(final Object obj) {
        return obj instanceof EqualsClassNameFilter && equals((EqualsClassNameFilter) obj);
    }

    public boolean equals(final EqualsClassNameFilter obj) {
        return obj != null && obj.className.equals(className);
    }

    public String toString() {
        return "equals \"" + className + "\"";
    }

    public int hashCode() {
        return className.hashCode();
    }
}
